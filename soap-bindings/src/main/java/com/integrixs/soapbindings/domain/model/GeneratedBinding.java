package com.integrixs.soapbindings.domain.model;

import com.integrixs.soapbindings.domain.enums.GenerationStatus;
import com.integrixs.soapbindings.domain.enums.ClassType;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Domain model representing generated SOAP binding classes
 */
public class GeneratedBinding {

    private String bindingId;
    private String generationId;
    private String wsdlId;
    private String packageName;
    private String serviceName;
    private String outputDirectory;
    private Map<String, ClassInfo> generatedClasses = new HashMap<>();
    private Map<String, String> generatedClassesContent = new HashMap<>();
    private LocalDateTime generatedAt;
    private LocalDateTime generationTime;
    private String generatorVersion;
    private GenerationStatus status;
    private String errorMessage;
    private String className;
    private String fullQualifiedName;
    private ClassType classType;
    private String sourceFile;
    private boolean compiled;

    // Default constructor
    public GeneratedBinding() {
    }

    // All args constructor
    public GeneratedBinding(String bindingId, String generationId, String wsdlId, String packageName, String serviceName, String outputDirectory, Map<String, ClassInfo> generatedClasses, Map<String, String> generatedClassesContent, LocalDateTime generatedAt, LocalDateTime generationTime, String generatorVersion, GenerationStatus status, String errorMessage, String className, String fullQualifiedName, ClassType classType, String sourceFile, boolean compiled) {
        this.bindingId = bindingId;
        this.generationId = generationId;
        this.wsdlId = wsdlId;
        this.packageName = packageName;
        this.serviceName = serviceName;
        this.outputDirectory = outputDirectory;
        this.generatedClasses = generatedClasses != null ? generatedClasses : new HashMap<>();
        this.generatedClassesContent = generatedClassesContent != null ? generatedClassesContent : new HashMap<>();
        this.generatedAt = generatedAt;
        this.generationTime = generationTime;
        this.generatorVersion = generatorVersion;
        this.status = status;
        this.errorMessage = errorMessage;
        this.className = className;
        this.fullQualifiedName = fullQualifiedName;
        this.classType = classType;
        this.sourceFile = sourceFile;
        this.compiled = compiled;
    }

    // Getters
    public String getBindingId() { return bindingId; }
    public String getGenerationId() { return generationId; }
    public String getWsdlId() { return wsdlId; }
    public String getPackageName() { return packageName; }
    public String getServiceName() { return serviceName; }
    public String getOutputDirectory() { return outputDirectory; }
    public Map<String, ClassInfo> getGeneratedClasses() { return generatedClasses; }
    public Map<String, String> getGeneratedClassesContent() { return generatedClassesContent; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public LocalDateTime getGenerationTime() { return generationTime; }
    public String getGeneratorVersion() { return generatorVersion; }
    public GenerationStatus getStatus() { return status; }
    public String getErrorMessage() { return errorMessage; }
    public String getClassName() { return className; }
    public String getFullQualifiedName() { return fullQualifiedName; }
    public ClassType getClassType() { return classType; }
    public String getSourceFile() { return sourceFile; }
    public boolean isCompiled() { return compiled; }

    // Setters
    public void setBindingId(String bindingId) { this.bindingId = bindingId; }
    public void setGenerationId(String generationId) { this.generationId = generationId; }
    public void setWsdlId(String wsdlId) { this.wsdlId = wsdlId; }
    public void setPackageName(String packageName) { this.packageName = packageName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public void setOutputDirectory(String outputDirectory) { this.outputDirectory = outputDirectory; }
    public void setGeneratedClasses(Map<String, ClassInfo> generatedClasses) { this.generatedClasses = generatedClasses; }
    public void setGeneratedClassesContent(Map<String, String> generatedClassesContent) { this.generatedClassesContent = generatedClassesContent; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
    public void setGenerationTime(LocalDateTime generationTime) { this.generationTime = generationTime; }
    public void setGeneratorVersion(String generatorVersion) { this.generatorVersion = generatorVersion; }
    public void setStatus(GenerationStatus status) { this.status = status; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public void setClassName(String className) { this.className = className; }
    public void setFullQualifiedName(String fullQualifiedName) { this.fullQualifiedName = fullQualifiedName; }
    public void setClassType(ClassType classType) { this.classType = classType; }
    public void setSourceFile(String sourceFile) { this.sourceFile = sourceFile; }
    public void setCompiled(boolean compiled) { this.compiled = compiled; }

    // Builder
    public static GeneratedBindingBuilder builder() {
        return new GeneratedBindingBuilder();
    }

    public static class GeneratedBindingBuilder {
        private String bindingId;
        private String generationId;
        private String wsdlId;
        private String packageName;
        private String serviceName;
        private String outputDirectory;
        private Map<String, ClassInfo> generatedClasses = new HashMap<>();
        private Map<String, String> generatedClassesContent = new HashMap<>();
        private LocalDateTime generatedAt;
        private LocalDateTime generationTime;
        private String generatorVersion;
        private GenerationStatus status;
        private String errorMessage;
        private String className;
        private String fullQualifiedName;
        private ClassType classType;
        private String sourceFile;
        private boolean compiled;

        public GeneratedBindingBuilder bindingId(String bindingId) {
            this.bindingId = bindingId;
            return this;
        }

        public GeneratedBindingBuilder generationId(String generationId) {
            this.generationId = generationId;
            return this;
        }

        public GeneratedBindingBuilder wsdlId(String wsdlId) {
            this.wsdlId = wsdlId;
            return this;
        }

        public GeneratedBindingBuilder packageName(String packageName) {
            this.packageName = packageName;
            return this;
        }

        public GeneratedBindingBuilder serviceName(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        public GeneratedBindingBuilder outputDirectory(String outputDirectory) {
            this.outputDirectory = outputDirectory;
            return this;
        }

        public GeneratedBindingBuilder generatedClasses(Map<String, ClassInfo> generatedClasses) {
            this.generatedClasses = generatedClasses;
            return this;
        }

        public GeneratedBindingBuilder generatedClassesContent(Map<String, String> generatedClassesContent) {
            this.generatedClassesContent = generatedClassesContent;
            return this;
        }

        public GeneratedBindingBuilder generatedAt(LocalDateTime generatedAt) {
            this.generatedAt = generatedAt;
            return this;
        }

        public GeneratedBindingBuilder generationTime(LocalDateTime generationTime) {
            this.generationTime = generationTime;
            return this;
        }

        public GeneratedBindingBuilder generatorVersion(String generatorVersion) {
            this.generatorVersion = generatorVersion;
            return this;
        }

        public GeneratedBindingBuilder status(GenerationStatus status) {
            this.status = status;
            return this;
        }

        public GeneratedBindingBuilder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public GeneratedBindingBuilder className(String className) {
            this.className = className;
            return this;
        }

        public GeneratedBindingBuilder fullQualifiedName(String fullQualifiedName) {
            this.fullQualifiedName = fullQualifiedName;
            return this;
        }

        public GeneratedBindingBuilder classType(ClassType classType) {
            this.classType = classType;
            return this;
        }

        public GeneratedBindingBuilder sourceFile(String sourceFile) {
            this.sourceFile = sourceFile;
            return this;
        }

        public GeneratedBindingBuilder compiled(boolean compiled) {
            this.compiled = compiled;
            return this;
        }

        public GeneratedBinding build() {
            return new GeneratedBinding(bindingId, generationId, wsdlId, packageName, serviceName, outputDirectory, generatedClasses, generatedClassesContent, generatedAt, generationTime, generatorVersion, status, errorMessage, className, fullQualifiedName, classType, sourceFile, compiled);
        }
    }

    // Additional methods
    public boolean isSuccessful() {
        return status == GenerationStatus.SUCCESS;
    }

    public ClassInfo getServiceInterface() {
        // Return the first SERVICE type class info
        return generatedClasses.values().stream()
                .filter(c -> c.getClassName() != null && c.getClassName().endsWith("Service"))
                .findFirst()
                .orElse(null);
    }
}
