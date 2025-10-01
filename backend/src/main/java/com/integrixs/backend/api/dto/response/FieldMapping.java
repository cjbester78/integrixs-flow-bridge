package com.integrixs.backend.api.dto.response;

public class FieldMapping {

    private String sourcePath;
    private String targetPath;
    private String sourceType;
    private String targetType;
    private boolean compatible;
    private boolean transformationRequired;
    private String transformationHint;

    // Default constructor
    public FieldMapping() {
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public String getTargetPath() {
        return targetPath;
    }

    public void setTargetPath(String targetPath) {
        this.targetPath = targetPath;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public boolean isCompatible() {
        return compatible;
    }

    public void setCompatible(boolean compatible) {
        this.compatible = compatible;
    }

    public boolean isTransformationRequired() {
        return transformationRequired;
    }

    public void setTransformationRequired(boolean transformationRequired) {
        this.transformationRequired = transformationRequired;
    }

    public String getTransformationHint() {
        return transformationHint;
    }

    public void setTransformationHint(String transformationHint) {
        this.transformationHint = transformationHint;
    }

    // Builder
    public static FieldMappingBuilder builder() {
        return new FieldMappingBuilder();
    }

    public static class FieldMappingBuilder {
        private String sourcePath;
        private String targetPath;
        private String sourceType;
        private String targetType;
        private boolean compatible;
        private boolean transformationRequired;
        private String transformationHint;

        public FieldMappingBuilder sourcePath(String sourcePath) {
            this.sourcePath = sourcePath;
            return this;
        }

        public FieldMappingBuilder targetPath(String targetPath) {
            this.targetPath = targetPath;
            return this;
        }

        public FieldMappingBuilder sourceType(String sourceType) {
            this.sourceType = sourceType;
            return this;
        }

        public FieldMappingBuilder targetType(String targetType) {
            this.targetType = targetType;
            return this;
        }

        public FieldMappingBuilder compatible(boolean compatible) {
            this.compatible = compatible;
            return this;
        }

        public FieldMappingBuilder transformationRequired(boolean transformationRequired) {
            this.transformationRequired = transformationRequired;
            return this;
        }

        public FieldMappingBuilder transformationHint(String transformationHint) {
            this.transformationHint = transformationHint;
            return this;
        }

        public FieldMapping build() {
            FieldMapping mapping = new FieldMapping();
            mapping.setSourcePath(this.sourcePath);
            mapping.setTargetPath(this.targetPath);
            mapping.setSourceType(this.sourceType);
            mapping.setTargetType(this.targetType);
            mapping.setCompatible(this.compatible);
            mapping.setTransformationRequired(this.transformationRequired);
            mapping.setTransformationHint(this.transformationHint);
            return mapping;
        }
    }
}
