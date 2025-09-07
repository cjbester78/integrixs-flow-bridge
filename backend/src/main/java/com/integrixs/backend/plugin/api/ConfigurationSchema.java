package com.integrixs.backend.plugin.api;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * Configuration schema for dynamic form generation
 */
@Data
@Builder
public class ConfigurationSchema {
    
    /**
     * Schema sections
     */
    private List<Section> sections;
    
    /**
     * Configuration section
     */
    @Data
    @Builder
    public static class Section {
        private String id;
        private String title;
        private String description;
        private List<Field> fields;
        private Map<String, Object> properties;
    }
    
    /**
     * Configuration field
     */
    @Data
    @Builder
    public static class Field {
        private String name;
        private String type;
        private String label;
        private String placeholder;
        private String help;
        private boolean required;
        private Object defaultValue;
        private List<Option> options;
        private Validation validation;
        private Condition condition;
        private Map<String, Object> properties;
    }
    
    /**
     * Field option for select/multiselect
     */
    @Data
    @Builder
    public static class Option {
        private String value;
        private String label;
        private String description;
        private boolean disabled;
    }
    
    /**
     * Field validation rules
     */
    @Data
    @Builder
    public static class Validation {
        private String pattern;
        private Integer minLength;
        private Integer maxLength;
        private Number min;
        private Number max;
        private String message;
        private Map<String, Object> custom;
    }
    
    /**
     * Field visibility condition
     */
    @Data
    @Builder
    public static class Condition {
        private String field;
        private String operator;
        private Object value;
        private List<Condition> and;
        private List<Condition> or;
    }
}