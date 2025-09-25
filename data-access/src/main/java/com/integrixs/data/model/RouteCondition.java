package com.integrixs.data.model;

/**
 * Entity representing a condition for a flow route
 */
public class RouteCondition extends BaseEntity {

    private FlowRoute flowRoute;

    private ConditionType conditionType;

    private String fieldPath;

    private Operator operator;

    private String expectedValue;

    private LogicalOperator logicalOperator = LogicalOperator.AND;

    private Integer order = 0;

    private boolean active = true;

    private SourceType sourceType = SourceType.VARIABLE;

    private String sourcePath;

    public enum ConditionType {
        EQUALS,
        NOT_EQUALS,
        CONTAINS,
        NOT_CONTAINS,
        MATCHES_REGEX,
        GREATER_THAN,
        LESS_THAN,
        IS_NULL,
        IS_NOT_NULL,
        IN_LIST,
        NOT_IN_LIST
    }

    public enum Operator {
        EQUALS,
        NOT_EQUALS,
        CONTAINS,
        NOT_CONTAINS,
        STARTS_WITH,
        ENDS_WITH,
        GREATER_THAN,
        LESS_THAN,
        GREATER_THAN_OR_EQUALS,
        LESS_THAN_OR_EQUALS,
        IS_NULL,
        IS_NOT_NULL,
        MATCHES_REGEX,
        IN_LIST,
        NOT_IN_LIST
    }

    public enum SourceType {
        HEADER,
        VARIABLE,
        XPATH,
        JSONPATH,
        CONSTANT
    }

    public enum LogicalOperator {
        AND,
        OR
    }

    // Getters and setters

    public FlowRoute getFlowRoute() {
        return flowRoute;
    }

    public void setFlowRoute(FlowRoute flowRoute) {
        this.flowRoute = flowRoute;
    }

    public ConditionType getConditionType() {
        return conditionType;
    }

    public void setConditionType(ConditionType conditionType) {
        this.conditionType = conditionType;
    }

    public String getFieldPath() {
        return fieldPath;
    }

    public void setFieldPath(String fieldPath) {
        this.fieldPath = fieldPath;
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    public String getExpectedValue() {
        return expectedValue;
    }

    public void setExpectedValue(String expectedValue) {
        this.expectedValue = expectedValue;
    }

    public LogicalOperator getLogicalOperator() {
        return logicalOperator;
    }

    public void setLogicalOperator(LogicalOperator logicalOperator) {
        this.logicalOperator = logicalOperator;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(SourceType sourceType) {
        this.sourceType = sourceType;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }
}
