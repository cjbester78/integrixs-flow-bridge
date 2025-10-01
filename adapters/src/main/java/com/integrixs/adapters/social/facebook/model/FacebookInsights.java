package com.integrixs.adapters.social.facebook.model;

import java.util.List;

/**
 * Facebook insights data
 */
public class FacebookInsights {
    public FacebookInsights() {
    }


    private String name;
    private String period;
    private List<InsightValue> values;
    private String title;
    private String description;

                    public static class InsightValue {
        private Object value;
        private String endTime;

        // Getters and Setters
        public Object getValue() {
            return value;
        }
        public void setValue(Object value) {
            this.value = value;
        }
        public String getEndTime() {
            return endTime;
        }
        public void setEndTime(String endTime) {
            this.endTime = endTime;
        }
    }
    // Getters and Setters
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getPeriod() {
        return period;
    }
    public void setPeriod(String period) {
        this.period = period;
    }
    public List<InsightValue> getValues() {
        return values;
    }
    public void setValues(List<InsightValue> values) {
        this.values = values;
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
    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name;
        private String period;
        private List<InsightValue> values;
        private String title;
        private String description;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder period(String period) {
            this.period = period;
            return this;
        }

        public Builder values(List<InsightValue> values) {
            this.values = values;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }


        public FacebookInsights build() {
            FacebookInsights obj = new FacebookInsights();
            obj.name = this.name;
            obj.period = this.period;
            obj.values = this.values;
            obj.title = this.title;
            obj.description = this.description;
            return obj;
        }
    }
}
