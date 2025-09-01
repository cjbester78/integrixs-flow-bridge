package com.integrixs.data.repository;

import com.integrixs.data.model.AlertRule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for AlertRule entities
 */
@Repository
public interface AlertRuleRepository extends JpaRepository<AlertRule, Long> {
    
    /**
     * Find alert rule by name
     */
    Optional<AlertRule> findByRuleName(String ruleName);
    
    /**
     * Find all enabled alert rules
     */
    List<AlertRule> findByEnabledTrue();
    
    /**
     * Find all enabled alert rules with pagination
     */
    Page<AlertRule> findByEnabledTrue(Pageable pageable);
    
    /**
     * Find enabled alert rules by type
     */
    List<AlertRule> findByAlertTypeAndEnabledTrue(AlertRule.AlertType alertType);
    
    /**
     * Find enabled alert rules by type with pagination
     */
    Page<AlertRule> findByAlertTypeAndEnabledTrue(AlertRule.AlertType alertType, Pageable pageable);
    
    /**
     * Find enabled alert rules by target
     */
    List<AlertRule> findByTargetTypeAndTargetIdAndEnabledTrue(
            AlertRule.TargetType targetType, String targetId);
    
    /**
     * Find enabled alert rules by severity
     */
    List<AlertRule> findBySeverityAndEnabledTrue(AlertRule.AlertSeverity severity);
    
    /**
     * Find enabled alert rules by severity with pagination
     */
    Page<AlertRule> findBySeverityAndEnabledTrue(AlertRule.AlertSeverity severity, Pageable pageable);
    
    /**
     * Find alert rules with specific tags
     */
    @Query("SELECT DISTINCT ar FROM AlertRule ar JOIN ar.tags t WHERE t IN :tags AND ar.enabled = true")
    List<AlertRule> findByTagsAndEnabledTrue(@Param("tags") List<String> tags);
    
    /**
     * Find alert rules for specific flow
     */
    @Query("SELECT ar FROM AlertRule ar WHERE ar.enabled = true AND " +
           "(ar.targetType = 'ALL' OR (ar.targetType = 'FLOW' AND ar.targetId = :flowId))")
    List<AlertRule> findActiveRulesForFlow(@Param("flowId") String flowId);
    
    /**
     * Find alert rules for specific adapter
     */
    @Query("SELECT ar FROM AlertRule ar WHERE ar.enabled = true AND " +
           "(ar.targetType = 'ALL' OR (ar.targetType = 'ADAPTER' AND ar.targetId = :adapterId))")
    List<AlertRule> findActiveRulesForAdapter(@Param("adapterId") String adapterId);
    
    /**
     * Find system-wide alert rules
     */
    @Query("SELECT ar FROM AlertRule ar WHERE ar.enabled = true AND " +
           "ar.targetType IN ('ALL', 'SYSTEM')")
    List<AlertRule> findSystemWideRules();
    
    /**
     * Check if alert rule name exists
     */
    boolean existsByRuleName(String ruleName);
    
    /**
     * Count enabled rules by type
     */
    long countByAlertTypeAndEnabledTrue(AlertRule.AlertType alertType);
}