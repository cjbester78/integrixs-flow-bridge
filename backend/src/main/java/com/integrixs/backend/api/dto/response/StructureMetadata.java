package com.integrixs.backend.api.dto.response;

import java.util.List;
import java.util.Map;

public class StructureMetadata {

    private List<Field> fields;
    private Map<String, String> namespaces;

    // Default constructor
    public StructureMetadata() {
    }

    public List<Field> getFields() {
        return fields;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }

    public Map<String, String> getNamespaces() {
        return namespaces;
    }

    public void setNamespaces(Map<String, String> namespaces) {
        this.namespaces = namespaces;
    }

    // Builder
    public static StructureMetadataBuilder builder() {
        return new StructureMetadataBuilder();
    }

    public static class StructureMetadataBuilder {
        private List<Field> fields;
        private Map<String, String> namespaces;

        public StructureMetadataBuilder fields(List<Field> fields) {
            this.fields = fields;
            return this;
        }

        public StructureMetadataBuilder namespaces(Map<String, String> namespaces) {
            this.namespaces = namespaces;
            return this;
        }

        public StructureMetadata build() {
            StructureMetadata metadata = new StructureMetadata();
            metadata.setFields(this.fields);
            metadata.setNamespaces(this.namespaces);
            return metadata;
        }
    }

    public static class Field {
        private String path;
        private String type;
        private boolean required;

        // Default constructor
        public Field() {
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public boolean isRequired() {
            return required;
        }

        public void setRequired(boolean required) {
            this.required = required;
        }

        // Builder
        public static FieldBuilder builder() {
            return new FieldBuilder();
        }

        public static class FieldBuilder {
            private String path;
            private String type;
            private boolean required;

            public FieldBuilder path(String path) {
                this.path = path;
                return this;
            }

            public FieldBuilder type(String type) {
                this.type = type;
                return this;
            }

            public FieldBuilder required(boolean required) {
                this.required = required;
                return this;
            }

            public Field build() {
                Field field = new Field();
                field.setPath(this.path);
                field.setType(this.type);
                field.setRequired(this.required);
                return field;
            }
        }
    }
}
