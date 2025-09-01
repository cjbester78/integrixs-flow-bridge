package com.integrixs.data.model;

import jakarta.persistence.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Entity representing a flow router configuration
 */
@Entity
@Table(name = "flow_routers")
public class FlowRouter extends BaseEntity {
    
    
    @Column(name = "router_name", unique = true, nullable = false)
    private String routerName;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flow_id")
    private IntegrationFlow flow;
    
    @Column(name = "router_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private RouterType routerType;
    
    @Column(name = "configuration", columnDefinition = "TEXT")
    private String configuration;
    
    @Column(name = "is_active")
    private boolean active = true;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "input_channel")
    private String inputChannel;
    
    @Column(name = "default_output_channel")
    private String defaultOutputChannel;
    
    @ElementCollection
    @CollectionTable(name = "router_channel_mappings", 
                     joinColumns = @JoinColumn(name = "router_id"))
    @MapKeyColumn(name = "condition_value")
    @Column(name = "output_channel")
    private Map<String, String> channelMappings = new HashMap<>();
    
    @Column(name = "evaluation_order")
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
        if (this.flow == null) {
            this.flow = new IntegrationFlow();
        }
        this.flow.setId(flowId);
    }
    
    public void setName(String name) {
        this.routerName = name;
    }
}