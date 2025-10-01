package com.integrixs.backend.plugin.security;

import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.jar.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Security scanner for plugin JAR files
 */
@Component
public class PluginSecurityScanner {

    // Dangerous class patterns

    private static final Logger log = LoggerFactory.getLogger(PluginSecurityScanner.class);

    private static final Set<String> DANGEROUS_CLASSES = Set.of(
        "java.lang.Runtime",
        "java.lang.ProcessBuilder",
        "java.lang.reflect.AccessibleObject",
        "sun.misc.Unsafe",
        "java.security.AllPermission"
   );

    // Dangerous method patterns
    private static final Set<String> DANGEROUS_METHODS = Set.of(
        "exec",
        "load",
        "loadLibrary",
        "setSecurityManager",
        "setAccessible",
        "doPrivileged"
   );

    // Suspicious patterns in code
    private static final List<Pattern> SUSPICIOUS_PATTERNS = List.of(
        Pattern.compile("Runtime\\.getRuntime\\(\\)"),
        Pattern.compile("ProcessBuilder"),
        Pattern.compile("setSecurityManager"),
        Pattern.compile("System\\.exit"),
        Pattern.compile("deleteOnExit"),
        Pattern.compile("sun\\.misc\\.Unsafe"),
        Pattern.compile("reflection\\.setAccessible"),
        Pattern.compile("URLClassLoader"),
        Pattern.compile("defineClass")
   );

    // File patterns that should not be in plugins
    private static final Set<String> FORBIDDEN_FILES = Set.of(
        ".exe", ".dll", ".so", ".dylib",
        ".sh", ".bat", ".cmd",
        ".key", ".pem", ".p12", ".jks"
   );

    /**
     * Perform comprehensive security scan on plugin JAR
     */
    public SecurityScanResult scanPlugin(Path jarPath) {
        log.info("Starting security scan for plugin: {}", jarPath);

        SecurityScanResult.SecurityScanResultBuilder resultBuilder = SecurityScanResult.builder()
                .jarPath(jarPath.toString())
                .scanDate(new Date());

        List<SecurityIssue> issues = new ArrayList<>();

        try {
            // Calculate checksum
            String checksum = calculateChecksum(jarPath);
            resultBuilder.checksum(checksum);

            // Check file size
            long fileSize = Files.size(jarPath);
            resultBuilder.fileSize(fileSize);
            if(fileSize > 50 * 1024 * 1024) { // 50MB limit
                issues.add(SecurityIssue.builder()
                        .severity(Severity.WARNING)
                        .type(IssueType.FILE_SIZE)
                        .description("JAR file exceeds 50MB size limit")
                        .build());
            }

            // Scan JAR contents
            try(JarFile jarFile = new JarFile(jarPath.toFile())) {
                // Check manifest
                scanManifest(jarFile, issues);

                // Scan all entries
                Enumeration<JarEntry> entries = jarFile.entries();
                while(entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    scanJarEntry(jarFile, entry, issues);
                }

                // Check for native libraries
                checkForNativeLibraries(jarFile, issues);

                // Check for executable files
                checkForExecutables(jarFile, issues);

                // Verify signatures if signed
                verifySignatures(jarFile, issues);
            }

            // Determine overall risk level
            RiskLevel riskLevel = calculateRiskLevel(issues);
            resultBuilder.riskLevel(riskLevel);

            // Set verdict
            boolean passed = riskLevel != RiskLevel.CRITICAL &&
                           issues.stream().noneMatch(i -> i.getSeverity() == Severity.CRITICAL);
            resultBuilder.passed(passed);

        } catch(Exception e) {
            log.error("Error scanning plugin", e);
            issues.add(SecurityIssue.builder()
                    .severity(Severity.CRITICAL)
                    .type(IssueType.SCAN_ERROR)
                    .description("Failed to scan plugin: " + e.getMessage())
                    .build());
            resultBuilder.passed(false);
            resultBuilder.riskLevel(RiskLevel.CRITICAL);
        }

        resultBuilder.issues(issues);
        SecurityScanResult result = resultBuilder.build();

        log.info("Security scan completed. Risk level: {}, Issues found: {}",
                result.getRiskLevel(), issues.size());

        return result;
    }

    /**
     * Scan manifest file
     */
    private void scanManifest(JarFile jarFile, List<SecurityIssue> issues) {
        try {
            Manifest manifest = jarFile.getManifest();
            if(manifest == null) {
                issues.add(SecurityIssue.builder()
                        .severity(Severity.WARNING)
                        .type(IssueType.MISSING_MANIFEST)
                        .description("JAR file has no manifest")
                        .build());
                return;
            }

            Attributes attrs = manifest.getMainAttributes();

            // Check for required attributes
            String pluginClass = attrs.getValue("Plugin - Class");
            if(pluginClass == null) {
                issues.add(SecurityIssue.builder()
                        .severity(Severity.HIGH)
                        .type(IssueType.INVALID_MANIFEST)
                        .description("Missing required Plugin - Class attribute")
                        .build());
            }

            // Check for suspicious permissions
            String permissions = attrs.getValue("Permissions");
            if("all - permissions".equals(permissions)) {
                issues.add(SecurityIssue.builder()
                        .severity(Severity.CRITICAL)
                        .type(IssueType.DANGEROUS_PERMISSION)
                        .description("Plugin requests all permissions")
                        .build());
            }

        } catch(IOException e) {
            log.error("Error reading manifest", e);
        }
    }

    /**
     * Scan individual JAR entry
     */
    private void scanJarEntry(JarFile jarFile, JarEntry entry, List<SecurityIssue> issues) {
        String entryName = entry.getName();

        // Check for forbidden file types
        for(String forbidden : FORBIDDEN_FILES) {
            if(entryName.endsWith(forbidden)) {
                issues.add(SecurityIssue.builder()
                        .severity(Severity.CRITICAL)
                        .type(IssueType.FORBIDDEN_FILE)
                        .description("Forbidden file type found: " + entryName)
                        .location(entryName)
                        .build());
            }
        }

        // Scan class files
        if(entryName.endsWith(".class")) {
            scanClassFile(jarFile, entry, issues);
        }

        // Check for hidden files
        if(entryName.startsWith(".") || entryName.contains("/.")) {
            issues.add(SecurityIssue.builder()
                    .severity(Severity.WARNING)
                    .type(IssueType.HIDDEN_FILE)
                    .description("Hidden file found: " + entryName)
                    .location(entryName)
                    .build());
        }
    }

    /**
     * Scan class file for dangerous code
     */
    private void scanClassFile(JarFile jarFile, JarEntry entry, List<SecurityIssue> issues) {
        try(InputStream is = jarFile.getInputStream(entry)) {
            byte[] classBytes = is.readAllBytes();
            String className = entry.getName().replace('/', '.').replace(".class", "");

            // Check for dangerous class usage
            for(String dangerous : DANGEROUS_CLASSES) {
                if(containsReference(classBytes, dangerous)) {
                    issues.add(SecurityIssue.builder()
                            .severity(Severity.HIGH)
                            .type(IssueType.DANGEROUS_CLASS)
                            .description("Uses dangerous class: " + dangerous)
                            .location(className)
                            .build());
                }
            }

            // Check for dangerous method calls
            for(String method : DANGEROUS_METHODS) {
                if(containsReference(classBytes, method)) {
                    issues.add(SecurityIssue.builder()
                            .severity(Severity.HIGH)
                            .type(IssueType.DANGEROUS_METHOD)
                            .description("Calls dangerous method: " + method)
                            .location(className)
                            .build());
                }
            }

        } catch(IOException e) {
            log.error("Error scanning class file: " + entry.getName(), e);
        }
    }

    /**
     * Check for native libraries
     */
    private void checkForNativeLibraries(JarFile jarFile, List<SecurityIssue> issues) {
        jarFile.stream()
                .filter(entry -> entry.getName().matches(".*\\.(dll|so|dylib|jnilib)$"))
                .forEach(entry -> {
                    issues.add(SecurityIssue.builder()
                            .severity(Severity.HIGH)
                            .type(IssueType.NATIVE_CODE)
                            .description("Contains native library: " + entry.getName())
                            .location(entry.getName())
                            .build());
                });
    }

    /**
     * Check for executable files
     */
    private void checkForExecutables(JarFile jarFile, List<SecurityIssue> issues) {
        jarFile.stream()
                .filter(entry -> entry.getName().matches(".*\\.(exe|sh|bat|cmd)$"))
                .forEach(entry -> {
                    issues.add(SecurityIssue.builder()
                            .severity(Severity.CRITICAL)
                            .type(IssueType.EXECUTABLE_FILE)
                            .description("Contains executable file: " + entry.getName())
                            .location(entry.getName())
                            .build());
                });
    }

    /**
     * Verify JAR signatures
     */
    private void verifySignatures(JarFile jarFile, List<SecurityIssue> issues) {
        try {
            // Check if JAR is signed
            boolean isSigned = jarFile.stream()
                    .anyMatch(entry -> entry.getName().startsWith("META - INF/") &&
                             (entry.getName().endsWith(".SF") || entry.getName().endsWith(".RSA") ||
                              entry.getName().endsWith(".DSA")));

            if(!isSigned) {
                issues.add(SecurityIssue.builder()
                        .severity(Severity.INFO)
                        .type(IssueType.UNSIGNED_JAR)
                        .description("JAR file is not digitally signed")
                        .build());
            }

        } catch(Exception e) {
            log.error("Error verifying signatures", e);
        }
    }

    /**
     * Calculate file checksum
     */
    private String calculateChecksum(Path file) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try(InputStream is = Files.newInputStream(file)) {
            byte[] buffer = new byte[8192];
            int read;
            while((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
        }

        byte[] hash = digest.digest();
        StringBuilder hexString = new StringBuilder();
        for(byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if(hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * Check if bytes contain reference to string
     */
    private boolean containsReference(byte[] classBytes, String reference) {
        // Simple string search - in production, use proper bytecode analysis
        String bytesAsString = new String(classBytes);
        return bytesAsString.contains(reference);
    }

    /**
     * Calculate overall risk level
     */
    private RiskLevel calculateRiskLevel(List<SecurityIssue> issues) {
        if(issues.stream().anyMatch(i -> i.getSeverity() == Severity.CRITICAL)) {
            return RiskLevel.CRITICAL;
        }

        long highCount = issues.stream()
                .filter(i -> i.getSeverity() == Severity.HIGH)
                .count();

        if(highCount >= 3) {
            return RiskLevel.HIGH;
        } else if(highCount >= 1) {
            return RiskLevel.MEDIUM;
        }

        long warningCount = issues.stream()
                .filter(i -> i.getSeverity() == Severity.WARNING)
                .count();

        if(warningCount >= 5) {
            return RiskLevel.MEDIUM;
        } else if(warningCount >= 1) {
            return RiskLevel.LOW;
        }

        return RiskLevel.MINIMAL;
    }

    /**
     * Security scan result
     */
            public static class SecurityScanResult {
        private String jarPath;
        private Date scanDate;
        private boolean passed;
        private RiskLevel riskLevel;
        private List<SecurityIssue> issues;
        private String checksum;
        private long fileSize;

        public String getSummary() {
            Map<Severity, Long> severityCounts = issues.stream()
                    .collect(Collectors.groupingBy(SecurityIssue::getSeverity, Collectors.counting()));

            return String.format("Risk: %s, Critical: %d, High: %d, Warning: %d, Info: %d",
                    riskLevel,
                    severityCounts.getOrDefault(Severity.CRITICAL, 0L),
                    severityCounts.getOrDefault(Severity.HIGH, 0L),
                    severityCounts.getOrDefault(Severity.WARNING, 0L),
                    severityCounts.getOrDefault(Severity.INFO, 0L));
        }

        // Getters and Setters
        public String getJarPath() {
            return jarPath;
        }

        public void setJarPath(String jarPath) {
            this.jarPath = jarPath;
        }

        public Date getScanDate() {
            return scanDate;
        }

        public void setScanDate(Date scanDate) {
            this.scanDate = scanDate;
        }

        public boolean isPassed() {
            return passed;
        }

        public void setPassed(boolean passed) {
            this.passed = passed;
        }

        public RiskLevel getRiskLevel() {
            return riskLevel;
        }

        public void setRiskLevel(RiskLevel riskLevel) {
            this.riskLevel = riskLevel;
        }

        public List<SecurityIssue> getIssues() {
            return issues;
        }

        public void setIssues(List<SecurityIssue> issues) {
            this.issues = issues;
        }

        public String getChecksum() {
            return checksum;
        }

        public void setChecksum(String checksum) {
            this.checksum = checksum;
        }

        public long getFileSize() {
            return fileSize;
        }

        public void setFileSize(long fileSize) {
            this.fileSize = fileSize;
        }

        // Builder pattern
        public static SecurityScanResultBuilder builder() {
            return new SecurityScanResultBuilder();
        }

        public static class SecurityScanResultBuilder {
            private String jarPath;
            private Date scanDate;
            private boolean passed;
            private RiskLevel riskLevel;
            private List<SecurityIssue> issues;
            private String checksum;
            private long fileSize;

            public SecurityScanResultBuilder jarPath(String jarPath) {
                this.jarPath = jarPath;
                return this;
            }

            public SecurityScanResultBuilder scanDate(Date scanDate) {
                this.scanDate = scanDate;
                return this;
            }

            public SecurityScanResultBuilder passed(boolean passed) {
                this.passed = passed;
                return this;
            }

            public SecurityScanResultBuilder riskLevel(RiskLevel riskLevel) {
                this.riskLevel = riskLevel;
                return this;
            }

            public SecurityScanResultBuilder issues(List<SecurityIssue> issues) {
                this.issues = issues;
                return this;
            }

            public SecurityScanResultBuilder checksum(String checksum) {
                this.checksum = checksum;
                return this;
            }

            public SecurityScanResultBuilder fileSize(long fileSize) {
                this.fileSize = fileSize;
                return this;
            }

            public SecurityScanResult build() {
                SecurityScanResult result = new SecurityScanResult();
                result.jarPath = this.jarPath;
                result.scanDate = this.scanDate;
                result.passed = this.passed;
                result.riskLevel = this.riskLevel;
                result.issues = this.issues;
                result.checksum = this.checksum;
                result.fileSize = this.fileSize;
                return result;
            }
        }
    }

    /**
     * Security issue
     */
            public static class SecurityIssue {
        private Severity severity;
        private IssueType type;
        private String description;
        private String location;
        private String recommendation;

        // Getters and Setters
        public Severity getSeverity() {
            return severity;
        }

        public void setSeverity(Severity severity) {
            this.severity = severity;
        }

        public IssueType getType() {
            return type;
        }

        public void setType(IssueType type) {
            this.type = type;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public String getRecommendation() {
            return recommendation;
        }

        public void setRecommendation(String recommendation) {
            this.recommendation = recommendation;
        }

        // Builder pattern
        public static SecurityIssueBuilder builder() {
            return new SecurityIssueBuilder();
        }

        public static class SecurityIssueBuilder {
            private Severity severity;
            private IssueType type;
            private String description;
            private String location;
            private String recommendation;

            public SecurityIssueBuilder severity(Severity severity) {
                this.severity = severity;
                return this;
            }

            public SecurityIssueBuilder type(IssueType type) {
                this.type = type;
                return this;
            }

            public SecurityIssueBuilder description(String description) {
                this.description = description;
                return this;
            }

            public SecurityIssueBuilder location(String location) {
                this.location = location;
                return this;
            }

            public SecurityIssueBuilder recommendation(String recommendation) {
                this.recommendation = recommendation;
                return this;
            }

            public SecurityIssue build() {
                SecurityIssue issue = new SecurityIssue();
                issue.severity = this.severity;
                issue.type = this.type;
                issue.description = this.description;
                issue.location = this.location;
                issue.recommendation = this.recommendation;
                return issue;
            }
        }
    }

    /**
     * Issue severity
     */
    public enum Severity {
        CRITICAL,
        HIGH,
        WARNING,
        INFO
    }

    /**
     * Issue type
     */
    public enum IssueType {
        DANGEROUS_CLASS,
        DANGEROUS_METHOD,
        DANGEROUS_PERMISSION,
        FORBIDDEN_FILE,
        EXECUTABLE_FILE,
        NATIVE_CODE,
        HIDDEN_FILE,
        UNSIGNED_JAR,
        INVALID_MANIFEST,
        MISSING_MANIFEST,
        FILE_SIZE,
        SCAN_ERROR
    }

    /**
     * Overall risk level
     */
    public enum RiskLevel {
        MINIMAL,
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }
}
