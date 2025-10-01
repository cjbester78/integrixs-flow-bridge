package com.integrixs.soapbindings.domain.model;

/**
 * Information about a generated class
 */
public class ClassInfo {

    private String className;
    private String packageName;
    private String filePath;

    // Default constructor
    public ClassInfo() {
    }

    // All args constructor
    public ClassInfo(String className, String packageName, String filePath) {
        this.className = className;
        this.packageName = packageName;
        this.filePath = filePath;
    }

    // Getters
    public String getClassName() { return className; }
    public String getPackageName() { return packageName; }
    public String getFilePath() { return filePath; }

    // Setters
    public void setClassName(String className) { this.className = className; }
    public void setPackageName(String packageName) { this.packageName = packageName; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    // Additional methods
    public String getFullQualifiedName() {
        return packageName != null && !packageName.isEmpty() ? packageName + "." + className : className;
    }

    // Builder
    public static ClassInfoBuilder builder() {
        return new ClassInfoBuilder();
    }

    public static class ClassInfoBuilder {
        private String className;
        private String packageName;
        private String filePath;

        public ClassInfoBuilder className(String className) {
            this.className = className;
            return this;
        }

        public ClassInfoBuilder packageName(String packageName) {
            this.packageName = packageName;
            return this;
        }

        public ClassInfoBuilder filePath(String filePath) {
            this.filePath = filePath;
            return this;
        }

        public ClassInfo build() {
            return new ClassInfo(className, packageName, filePath);
        }
    }
}