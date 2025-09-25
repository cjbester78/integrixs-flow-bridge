package com.integrixs.data.model;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Entity representing a flow router configuration
 */
public class FlowRouter extends BaseEntity {

    private String routerName;

    private IntegrationFlow flow;

    private RouterType routerType;

    private String configuration;

    private boolean active = true;

    private String description;

    private String inputChannel;

    private String defaultOutputChannel;

    private Map<String, String> channelMappings = new HashMap<>();

    private Integer evaluationOrder = 0;

    public enum RouterType {
        CONTENT_BASED,
        HEADER_VALUE,
        PAYLOAD_TYPE,
        RECIPIENT_LIST,
        MULTICAST,
        SPLITTER,
        AGGREGATOR,
        CHOICE,
        FILTER,
        DYNAMIC,
        XPATH,
        JSONPATH,
        CUSTOM
    }

    // Getters and setters

    public String getRouterName() {
        return routerName;
    }

    public void setRouterName(String routerName) {
        this.routerName = routerName;
    }

    public IntegrationFlow getFlow() {
        return flow;
    }

    public void setFlow(IntegrationFlow flow) {
        this.flow = flow;
    }

    public RouterType getRouterType() {
        return routerType;
    }

    public void setRouterType(RouterType routerType) {
        this.routerType = routerType;
    }

    public String getConfiguration() {
        return configuration;
    }

    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInputChannel() {
        return inputChannel;
    }

    public void setInputChannel(String inputChannel) {
        this.inputChannel = inputChannel;
    }

    public String getDefaultOutputChannel() {
        return defaultOutputChannel;
    }

    public void setDefaultOutputChannel(String defaultOutputChannel) {
        this.defaultOutputChannel = defaultOutputChannel;
    }

    public Map<String, String> getChannelMappings() {
        return channelMappings;
    }

    public void setChannelMappings(Map<String, String> channelMappings) {
        this.channelMappings = channelMappings;
    }

    public Integer getEvaluationOrder() {
        return evaluationOrder;
    }

    public void setEvaluationOrder(Integer evaluationOrder) {
        this.evaluationOrder = evaluationOrder;
    }

    // Additional helper methods
    public void setFlowId(UUID flowId) {
        if(this.flow == null) {
            this.flow = new IntegrationFlow();
        }
        this.flow.setId(flowId);
    }

    public void setName(String name) {
        this.routerName = name;
    }
}
