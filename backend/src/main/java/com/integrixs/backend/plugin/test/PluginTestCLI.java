package com.integrixs.backend.plugin.test;

import com.integrixs.backend.plugin.api.AdapterPlugin;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * Command-line interface for testing plugins
 */
@Slf4j
public class PluginTestCLI {
    
    public static void main(String[] args) {
        if (args.length < 1) {
            printUsage();
            System.exit(1);
        }
        
        String command = args[0];
        
        try {
            switch (command) {
                case "test":
                    if (args.length < 2) {
                        System.err.println("Error: JAR file path required");
                        printUsage();
                        System.exit(1);
                    }
                    testPlugin(args[1], parseConfig(args, 2));
                    break;
                    
                case "validate":
                    if (args.length < 2) {
                        System.err.println("Error: JAR file path required");
                        printUsage();
                        System.exit(1);
                    }
                    validatePlugin(args[1]);
                    break;
                    
                case "info":
                    if (args.length < 2) {
                        System.err.println("Error: JAR file path required");
                        printUsage();
                        System.exit(1);
                    }
                    showPluginInfo(args[1]);
                    break;
                    
                default:
                    System.err.println("Unknown command: " + command);
                    printUsage();
                    System.exit(1);
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    private static void printUsage() {
        System.out.println("Integrix Plugin Test CLI");
        System.out.println();
        System.out.println("Usage:");
        System.out.println("  java -jar plugin-test-cli.jar <command> [options]");
        System.out.println();
        System.out.println("Commands:");
        System.out.println("  test <jar-file> [key=value...]    Run tests on a plugin");
        System.out.println("  validate <jar-file>               Validate plugin structure");
        System.out.println("  info <jar-file>                   Show plugin information");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  java -jar plugin-test-cli.jar test my-plugin.jar apiKey=12345 baseUrl=http://api.example.com");
        System.out.println("  java -jar plugin-test-cli.jar validate my-plugin.jar");
        System.out.println("  java -jar plugin-test-cli.jar info my-plugin.jar");
    }
    
    private static void testPlugin(String jarPath, Map<String, Object> config) throws Exception {
        System.out.println("Testing plugin: " + jarPath);
        System.out.println("Configuration: " + config);
        System.out.println();
        
        // Load plugin
        Class<? extends AdapterPlugin> pluginClass = loadPluginClass(jarPath);
        
        // Start mock service if needed
        MockExternalService mockService = null;
        if (config.containsKey("useMockService")) {
            mockService = new MockExternalService();
            mockService.start();
            config.put("baseUrl", mockService.getBaseUrl());
            System.out.println("Started mock service at: " + mockService.getBaseUrl());
            System.out.println();
        }
        
        try {
            // Run tests
            PluginTestRunner runner = new PluginTestRunner(pluginClass);
            PluginTestRunner.TestReport report = runner.runAllTests(config);
            
            // Print results
            report.printSummary();
            
            // Exit with appropriate code
            System.exit(report.getFailedTests() > 0 ? 1 : 0);
            
        } finally {
            if (mockService != null) {
                mockService.stop();
            }
        }
    }
    
    private static void validatePlugin(String jarPath) throws Exception {
        System.out.println("Validating plugin: " + jarPath);
        System.out.println();
        
        File jarFile = new File(jarPath);
        if (!jarFile.exists()) {
            throw new IllegalArgumentException("JAR file not found: " + jarPath);
        }
        
        boolean isValid = true;
        List<String> issues = new ArrayList<>();
        
        try (JarFile jar = new JarFile(jarFile)) {
            // Check manifest
            Manifest manifest = jar.getManifest();
            if (manifest == null) {
                issues.add("No manifest found");
                isValid = false;
            } else {
                String pluginClass = manifest.getMainAttributes().getValue("Plugin-Class");
                if (pluginClass == null) {
                    issues.add("Plugin-Class not specified in manifest");
                    isValid = false;
                }
                
                String pluginId = manifest.getMainAttributes().getValue("Plugin-Id");
                if (pluginId == null) {
                    issues.add("Plugin-Id not specified in manifest");
                    isValid = false;
                }
                
                String pluginVersion = manifest.getMainAttributes().getValue("Plugin-Version");
                if (pluginVersion == null) {
                    issues.add("Plugin-Version not specified in manifest");
                    isValid = false;
                }
            }
            
            // Check for plugin.properties
            if (jar.getEntry("plugin.properties") == null) {
                issues.add("plugin.properties file not found");
                // Not critical, just a warning
            }
        }
        
        // Try to load the plugin class
        try {
            Class<? extends AdapterPlugin> pluginClass = loadPluginClass(jarPath);
            AdapterPlugin instance = pluginClass.getDeclaredConstructor().newInstance();
            
            // Validate metadata
            if (instance.getMetadata() == null) {
                issues.add("Plugin metadata is null");
                isValid = false;
            }
        } catch (Exception e) {
            issues.add("Failed to load plugin: " + e.getMessage());
            isValid = false;
        }
        
        // Print results
        if (isValid) {
            System.out.println("✓ Plugin is valid");
        } else {
            System.out.println("✗ Plugin validation failed");
            System.out.println();
            System.out.println("Issues found:");
            for (String issue : issues) {
                System.out.println("  - " + issue);
            }
            System.exit(1);
        }
    }
    
    private static void showPluginInfo(String jarPath) throws Exception {
        System.out.println("Plugin Information");
        System.out.println("=================");
        System.out.println();
        
        // Load plugin
        Class<? extends AdapterPlugin> pluginClass = loadPluginClass(jarPath);
        AdapterPlugin instance = pluginClass.getDeclaredConstructor().newInstance();
        
        // Show metadata
        var metadata = instance.getMetadata();
        System.out.println("ID: " + metadata.getId());
        System.out.println("Name: " + metadata.getName());
        System.out.println("Version: " + metadata.getVersion());
        System.out.println("Vendor: " + metadata.getVendor());
        System.out.println("Description: " + metadata.getDescription());
        System.out.println("Category: " + metadata.getCategory());
        System.out.println("License: " + metadata.getLicense());
        System.out.println();
        
        System.out.println("Capabilities:");
        if (metadata.getCapabilities() != null) {
            metadata.getCapabilities().forEach((key, value) -> 
                System.out.println("  - " + key + ": " + value)
            );
        }
        System.out.println();
        
        System.out.println("Supported Protocols:");
        if (metadata.getSupportedProtocols() != null) {
            metadata.getSupportedProtocols().forEach(p -> 
                System.out.println("  - " + p)
            );
        }
        System.out.println();
        
        System.out.println("Authentication Methods:");
        if (metadata.getAuthenticationMethods() != null) {
            metadata.getAuthenticationMethods().forEach(a -> 
                System.out.println("  - " + a)
            );
        }
        System.out.println();
        
        // Show configuration schema
        var schema = instance.getConfigurationSchema();
        if (schema != null && schema.getSections() != null) {
            System.out.println("Configuration Schema:");
            for (var section : schema.getSections()) {
                System.out.println("  Section: " + section.getTitle());
                if (section.getFields() != null) {
                    for (var field : section.getFields()) {
                        System.out.println("    - " + field.getName() + 
                            " (" + field.getType() + ")" + 
                            (field.isRequired() ? " [required]" : ""));
                    }
                }
            }
        }
    }
    
    private static Class<? extends AdapterPlugin> loadPluginClass(String jarPath) throws Exception {
        File jarFile = new File(jarPath);
        if (!jarFile.exists()) {
            throw new IllegalArgumentException("JAR file not found: " + jarPath);
        }
        
        URL jarUrl = jarFile.toURI().toURL();
        URLClassLoader classLoader = new URLClassLoader(
            new URL[]{jarUrl},
            PluginTestCLI.class.getClassLoader()
        );
        
        // Read plugin class from manifest
        String pluginClassName;
        try (JarFile jar = new JarFile(jarFile)) {
            Manifest manifest = jar.getManifest();
            if (manifest == null) {
                throw new IllegalArgumentException("No manifest found in JAR");
            }
            
            pluginClassName = manifest.getMainAttributes().getValue("Plugin-Class");
            if (pluginClassName == null) {
                throw new IllegalArgumentException("Plugin-Class not specified in manifest");
            }
        }
        
        // Load the class
        Class<?> clazz = classLoader.loadClass(pluginClassName);
        if (!AdapterPlugin.class.isAssignableFrom(clazz)) {
            throw new IllegalArgumentException("Class does not implement AdapterPlugin: " + pluginClassName);
        }
        
        return (Class<? extends AdapterPlugin>) clazz;
    }
    
    private static Map<String, Object> parseConfig(String[] args, int startIndex) {
        Map<String, Object> config = new HashMap<>();
        
        for (int i = startIndex; i < args.length; i++) {
            String arg = args[i];
            int equalIndex = arg.indexOf('=');
            
            if (equalIndex > 0) {
                String key = arg.substring(0, equalIndex);
                String value = arg.substring(equalIndex + 1);
                
                // Try to parse as number
                try {
                    if (value.contains(".")) {
                        config.put(key, Double.parseDouble(value));
                    } else {
                        config.put(key, Long.parseLong(value));
                    }
                } catch (NumberFormatException e) {
                    // Try to parse as boolean
                    if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) {
                        config.put(key, Boolean.parseBoolean(value));
                    } else {
                        // Store as string
                        config.put(key, value);
                    }
                }
            }
        }
        
        return config;
    }
}