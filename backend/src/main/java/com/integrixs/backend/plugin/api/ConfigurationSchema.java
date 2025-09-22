package com.integrixs.backend.plugin.api;

import java.util.List;
import java.util.Map;

/**
 * Configuration schema for dynamic form generation
 */
public class ConfigurationSchema {

    /**
     * Schema sections
     */
    private List<Section> sections;

    // Getters and Setters
    public List<Section> getSections() {
        return sections;
    }

    public void setSections(List<Section> sections) {
        this.sections = sections;
    }

    // Builder pattern
    public static ConfigurationSchemaBuilder builder() {
        return new ConfigurationSchemaBuilder();
    }

    public static class ConfigurationSchemaBuilder {
        private List<Section> sections;

        public ConfigurationSchemaBuilder sections(List<Section> sections) {
            this.sections = sections;
            return this;
        }

        public ConfigurationSchema build() {
            ConfigurationSchema schema = new ConfigurationSchema();
            schema.sections = this.sections;
            return schema;
        }
    }

    /**
     * Configuration section
     */
            public static class Section {
        private String id;
        private String title;
        private String description;
        private List<Field> fields;
        private Map<String, Object> properties;

        // Getters and Setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public List<Field> getFields() {
            return fields;
        }

        public void setFields(List<Field> fields) {
            this.fields = fields;
        }

        public Map<String, Object> getProperties() {
            return properties;
        }

        public void setProperties(Map<String, Object> properties) {
            this.properties = properties;
        }

        // Builder pattern
        public static SectionBuilder builder() {
            return new SectionBuilder();
        }

        public static class SectionBuilder {
            private String id;
            private String title;
            private String description;
            private List<Field> fields;
            private Map<String, Object> properties;

            public SectionBuilder id(String id) {
                this.id = id;
                return this;
            }

            public SectionBuilder title(String title) {
                this.title = title;
                return this;
            }

            public SectionBuilder description(String description) {
                this.description = description;
                return this;
            }

            public SectionBuilder fields(List<Field> fields) {
                this.fields = fields;
                return this;
            }

            public SectionBuilder properties(Map<String, Object> properties) {
                this.properties = properties;
                return this;
            }

            public Section build() {
                Section section = new Section();
                section.id = this.id;
                section.title = this.title;
                section.description = this.description;
                section.fields = this.fields;
                section.properties = this.properties;
                return section;
            }
        }
    }

    /**
     * Configuration field
     */
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

        // Getters and Setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getPlaceholder() {
            return placeholder;
        }

        public void setPlaceholder(String placeholder) {
            this.placeholder = placeholder;
        }

        public String getHelp() {
            return help;
        }

        public void setHelp(String help) {
            this.help = help;
        }

        public boolean isRequired() {
            return required;
        }

        public void setRequired(boolean required) {
            this.required = required;
        }

        public Object getDefaultValue() {
            return defaultValue;
        }

        public void setDefaultValue(Object defaultValue) {
            this.defaultValue = defaultValue;
        }

        public List<Option> getOptions() {
            return options;
        }

        public void setOptions(List<Option> options) {
            this.options = options;
        }

        public Validation getValidation() {
            return validation;
        }

        public void setValidation(Validation validation) {
            this.validation = validation;
        }

        public Condition getCondition() {
            return condition;
        }

        public void setCondition(Condition condition) {
            this.condition = condition;
        }

        public Map<String, Object> getProperties() {
            return properties;
        }

        public void setProperties(Map<String, Object> properties) {
            this.properties = properties;
        }

        // Builder pattern
        public static FieldBuilder builder() {
            return new FieldBuilder();
        }

        public static class FieldBuilder {
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

            public FieldBuilder name(String name) {
                this.name = name;
                return this;
            }

            public FieldBuilder type(String type) {
                this.type = type;
                return this;
            }

            public FieldBuilder label(String label) {
                this.label = label;
                return this;
            }

            public FieldBuilder placeholder(String placeholder) {
                this.placeholder = placeholder;
                return this;
            }

            public FieldBuilder help(String help) {
                this.help = help;
                return this;
            }

            public FieldBuilder required(boolean required) {
                this.required = required;
                return this;
            }

            public FieldBuilder defaultValue(Object defaultValue) {
                this.defaultValue = defaultValue;
                return this;
            }

            public FieldBuilder options(List<Option> options) {
                this.options = options;
                return this;
            }

            public FieldBuilder validation(Validation validation) {
                this.validation = validation;
                return this;
            }

            public FieldBuilder condition(Condition condition) {
                this.condition = condition;
                return this;
            }

            public FieldBuilder properties(Map<String, Object> properties) {
                this.properties = properties;
                return this;
            }

            public Field build() {
                Field field = new Field();
                field.name = this.name;
                field.type = this.type;
                field.label = this.label;
                field.placeholder = this.placeholder;
                field.help = this.help;
                field.required = this.required;
                field.defaultValue = this.defaultValue;
                field.options = this.options;
                field.validation = this.validation;
                field.condition = this.condition;
                field.properties = this.properties;
                return field;
            }
        }
    }

    /**
     * Field option for select/multiselect
     */
    public static class Option {
        private String value;
        private String label;
        private String description;
        private boolean disabled;

        // Getters and Setters
        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public boolean isDisabled() {
            return disabled;
        }

        public void setDisabled(boolean disabled) {
            this.disabled = disabled;
        }

        // Builder pattern
        public static OptionBuilder builder() {
            return new OptionBuilder();
        }

        public static class OptionBuilder {
            private String value;
            private String label;
            private String description;
            private boolean disabled;

            public OptionBuilder value(String value) {
                this.value = value;
                return this;
            }

            public OptionBuilder label(String label) {
                this.label = label;
                return this;
            }

            public OptionBuilder description(String description) {
                this.description = description;
                return this;
            }

            public OptionBuilder disabled(boolean disabled) {
                this.disabled = disabled;
                return this;
            }

            public Option build() {
                Option option = new Option();
                option.value = this.value;
                option.label = this.label;
                option.description = this.description;
                option.disabled = this.disabled;
                return option;
            }
        }
    }

    /**
     * Field validation rules
     */
            public static class Validation {
        private String pattern;
        private Integer minLength;
        private Integer maxLength;
        private Number min;
        private Number max;
        private String message;
        private Map<String, Object> custom;

        // Getters and Setters
        public String getPattern() {
            return pattern;
        }

        public void setPattern(String pattern) {
            this.pattern = pattern;
        }

        public Integer getMinLength() {
            return minLength;
        }

        public void setMinLength(Integer minLength) {
            this.minLength = minLength;
        }

        public Integer getMaxLength() {
            return maxLength;
        }

        public void setMaxLength(Integer maxLength) {
            this.maxLength = maxLength;
        }

        public Number getMin() {
            return min;
        }

        public void setMin(Number min) {
            this.min = min;
        }

        public Number getMax() {
            return max;
        }

        public void setMax(Number max) {
            this.max = max;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Map<String, Object> getCustom() {
            return custom;
        }

        public void setCustom(Map<String, Object> custom) {
            this.custom = custom;
        }

        // Builder pattern
        public static ValidationBuilder builder() {
            return new ValidationBuilder();
        }

        public static class ValidationBuilder {
            private String pattern;
            private Integer minLength;
            private Integer maxLength;
            private Number min;
            private Number max;
            private String message;
            private Map<String, Object> custom;

            public ValidationBuilder pattern(String pattern) {
                this.pattern = pattern;
                return this;
            }

            public ValidationBuilder minLength(Integer minLength) {
                this.minLength = minLength;
                return this;
            }

            public ValidationBuilder maxLength(Integer maxLength) {
                this.maxLength = maxLength;
                return this;
            }

            public ValidationBuilder min(Number min) {
                this.min = min;
                return this;
            }

            public ValidationBuilder max(Number max) {
                this.max = max;
                return this;
            }

            public ValidationBuilder message(String message) {
                this.message = message;
                return this;
            }

            public ValidationBuilder custom(Map<String, Object> custom) {
                this.custom = custom;
                return this;
            }

            public Validation build() {
                Validation validation = new Validation();
                validation.pattern = this.pattern;
                validation.minLength = this.minLength;
                validation.maxLength = this.maxLength;
                validation.min = this.min;
                validation.max = this.max;
                validation.message = this.message;
                validation.custom = this.custom;
                return validation;
            }
        }
    }

    /**
     * Field visibility condition
     */
    public static class Condition {
        private String field;
        private String operator;
        private Object value;
        private List<Condition> and;
        private List<Condition> or;

        // Getters and Setters
        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public String getOperator() {
            return operator;
        }

        public void setOperator(String operator) {
            this.operator = operator;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        public List<Condition> getAnd() {
            return and;
        }

        public void setAnd(List<Condition> and) {
            this.and = and;
        }

        public List<Condition> getOr() {
            return or;
        }

        public void setOr(List<Condition> or) {
            this.or = or;
        }
    }
}
