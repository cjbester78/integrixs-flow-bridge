package com.integrixs.backend.domain.service;

import com.integrixs.data.model.CommunicationAdapter;
import com.integrixs.data.model.FlowStatus;
import com.integrixs.data.model.IntegrationFlow;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Domain service for validating deployment operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeploymentValidator {
    
    /**
     * Validate flow is ready for deployment
     */
    public void validateFlowForDeployment(IntegrationFlow flow, CommunicationAdapter sourceAdapter, CommunicationAdapter targetAdapter) {
        // Check deployment status
        if (flow.getStatus() == FlowStatus.DEPLOYED_ACTIVE) {
            throw new IllegalStateException("Flow is already deployed");
        }
        
        // Check adapters are configured
        if (flow.getSourceAdapterId() == null || flow.getTargetAdapterId() == null) {
            throw new IllegalStateException("Flow must have source and target adapters configured");
        }
        
        // Validate source adapter
        if (!sourceAdapter.isActive()) {
            throw new IllegalStateException(
                String.format("Cannot deploy flow: Source adapter '%s' is in a stopped status. " +
                    "Please activate the adapter before deploying the flow.", 
                    sourceAdapter.getName())
            );
        }
        
        // Validate target adapter
        if (!targetAdapter.isActive()) {
            throw new IllegalStateException(
                String.format("Cannot deploy flow: Target adapter '%s' is in a stopped status. " +
                    "Please activate the adapter before deploying the flow.", 
                    targetAdapter.getName())
            );
        }
        
        log.info("Flow {} validated for deployment", flow.getId());
    }
    
    /**
     * Validate flow can be undeployed
     */
    public void validateFlowForUndeployment(IntegrationFlow flow) {
        // If already inactive, nothing to do
        if (flow.getStatus() == FlowStatus.DEVELOPED_INACTIVE) {
            log.info("Flow {} is already undeployed", flow.getId());
            return;
        }
        
        // Check if flow is deployed
        if (flow.getStatus() != FlowStatus.DEPLOYED_ACTIVE) {
            throw new IllegalStateException(
                String.format("Flow is not deployed. Current status: %s", flow.getStatus())
            );
        }
    }
    
    /**
     * Check if flow is deployed
     */
    public boolean isFlowDeployed(IntegrationFlow flow) {
        return flow.getStatus() == FlowStatus.DEPLOYED_ACTIVE;
    }
}