package com.integrixs.data.sql.repository;

import com.integrixs.data.model.AlertRule;
import com.integrixs.data.model.AlertRule.*;
import com.integrixs.data.sql.core.BaseSqlRepository;
import com.integrixs.data.sql.core.ResultSetMapper;
import com.integrixs.data.sql.core.SqlQueryExecutor;
import com.integrixs.data.sql.core.SqlPaginationHelper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

/**
 * SQL implementation of AlertRuleRepository using native queries.
 * Handles multiple @ElementCollection mappings as separate tables.
 */
@Repository("alertRuleSqlRepository")
public class AlertRuleSqlRepository extends BaseSqlRepository<AlertRule, UUID> {

    private static final String TABLE_NAME = "alert_rules";
    private static final String ID_COLUMN = "id";
    private static final String CHANNELS_TABLE = "alert_rule_channels";
    private static final String ESCALATION_CHANNELS_TABLE = "alert_rule_escalation_channels";
    private static final String TAGS_TABLE = "alert_rule_tags";

    /**
     * Row mapper for AlertRule entity
     */
    private static final RowMapper<AlertRule> ALERT_RULE_ROW_MAPPER = new RowMapper<AlertRule>() {
        @Override
        public AlertRule mapRow(ResultSet rs, int rowNum) throws SQLException {
            AlertRule rule = new AlertRule();
            rule.setId(ResultSetMapper.getUUID(rs, "id"));
            rule.setRuleName(ResultSetMapper.getString(rs, "rule_name"));
            rule.setDescription(ResultSetMapper.getString(rs, "description"));

            String alertTypeStr = ResultSetMapper.getString(rs, "alert_type");
            if (alertTypeStr != null) {
                rule.setAlertType(AlertType.valueOf(alertTypeStr));
            }

            String severityStr = ResultSetMapper.getString(rs, "severity");
            if (severityStr != null) {
                rule.setSeverity(AlertSeverity.valueOf(severityStr));
            }

            rule.setEnabled(rs.getBoolean("is_enabled"));

            String conditionTypeStr = ResultSetMapper.getString(rs, "condition_type");
            if (conditionTypeStr != null) {
                rule.setConditionType(ConditionType.valueOf(conditionTypeStr));
            }

            rule.setConditionExpression(ResultSetMapper.getString(rs, "condition_expression"));
            rule.setThresholdValue(ResultSetMapper.getDouble(rs, "threshold_value"));

            String thresholdOperatorStr = ResultSetMapper.getString(rs, "threshold_operator");
            if (thresholdOperatorStr != null) {
                rule.setThresholdOperator(ThresholdOperator.valueOf(thresholdOperatorStr));
            }

            rule.setTimeWindowMinutes(ResultSetMapper.getInteger(rs, "time_window_minutes"));
            rule.setOccurrenceCount(ResultSetMapper.getInteger(rs, "occurrence_count"));

            String targetTypeStr = ResultSetMapper.getString(rs, "target_type");
            if (targetTypeStr != null) {
                rule.setTargetType(TargetType.valueOf(targetTypeStr));
            }

            rule.setTargetId(ResultSetMapper.getString(rs, "target_id"));
            rule.setSuppressionDurationMinutes(ResultSetMapper.getInteger(rs, "suppression_duration_minutes"));
            rule.setLastTriggeredAt(ResultSetMapper.getLocalDateTime(rs, "last_triggered_at"));
            rule.setTriggerCount(ResultSetMapper.getInteger(rs, "trigger_count"));
            rule.setEscalationEnabled(rs.getBoolean("escalation_enabled"));
            rule.setEscalationAfterMinutes(ResultSetMapper.getInteger(rs, "escalation_after_minutes"));
            // BaseEntity fields are set separately
            rule.setCreatedAt(ResultSetMapper.getLocalDateTime(rs, "created_at"));
            rule.setUpdatedAt(ResultSetMapper.getLocalDateTime(rs, "updated_at"));

            return rule;
        }
    };

    public AlertRuleSqlRepository(SqlQueryExecutor sqlQueryExecutor) {
        super(sqlQueryExecutor, TABLE_NAME, ID_COLUMN, ALERT_RULE_ROW_MAPPER);
    }

    @Override
    public Optional<AlertRule> findById(UUID id) {
        Optional<AlertRule> ruleOpt = super.findById(id);
        if (ruleOpt.isPresent()) {
            loadCollections(ruleOpt.get());
        }
        return ruleOpt;
    }

    @Override
    public List<AlertRule> findAll() {
        List<AlertRule> rules = super.findAll();
        for (AlertRule rule : rules) {
            loadCollections(rule);
        }
        return rules;
    }

    public Page<AlertRule> findAll(Pageable pageable) {
        String countSql = "SELECT COUNT(*) FROM " + TABLE_NAME;
        long total = sqlQueryExecutor.count(countSql);

        String sql = "SELECT * FROM " + TABLE_NAME;
        sql += SqlPaginationHelper.buildOrderByClause(pageable.getSort());
        sql += SqlPaginationHelper.buildPaginationClause(pageable);

        List<AlertRule> rules = sqlQueryExecutor.queryForList(sql, ALERT_RULE_ROW_MAPPER);
        for (AlertRule rule : rules) {
            loadCollections(rule);
        }

        return new PageImpl<>(rules, pageable, total);
    }

    public Optional<AlertRule> findByRuleName(String ruleName) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE rule_name = ?";
        List<AlertRule> results = sqlQueryExecutor.queryForList(sql, ALERT_RULE_ROW_MAPPER, ruleName);

        if (!results.isEmpty()) {
            AlertRule rule = results.get(0);
            loadCollections(rule);
            return Optional.of(rule);
        }
        return Optional.empty();
    }

    public List<AlertRule> findByAlertType(AlertType alertType) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE alert_type = ?";
        List<AlertRule> rules = sqlQueryExecutor.queryForList(sql, ALERT_RULE_ROW_MAPPER, alertType.toString());
        for (AlertRule rule : rules) {
            loadCollections(rule);
        }
        return rules;
    }

    public List<AlertRule> findByEnabledTrue() {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE is_enabled = true";
        List<AlertRule> rules = sqlQueryExecutor.queryForList(sql, ALERT_RULE_ROW_MAPPER);
        for (AlertRule rule : rules) {
            loadCollections(rule);
        }
        return rules;
    }

    public Page<AlertRule> findByEnabledTrue(Pageable pageable) {
        String baseQuery = "SELECT * FROM " + TABLE_NAME + " WHERE is_enabled = true";
        String countQuery = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE is_enabled = true";

        long total = sqlQueryExecutor.count(countQuery);
        String paginatedQuery = baseQuery + SqlPaginationHelper.buildOrderByClause(pageable.getSort()) + SqlPaginationHelper.buildPaginationClause(pageable);
        List<AlertRule> rules = sqlQueryExecutor.queryForList(paginatedQuery, ALERT_RULE_ROW_MAPPER);

        for (AlertRule rule : rules) {
            loadCollections(rule);
        }

        return new PageImpl<>(rules, pageable, total);
    }

    public List<AlertRule> findByTargetTypeAndTargetId(TargetType targetType, String targetId) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE target_type = ? AND target_id = ?";
        List<AlertRule> rules = sqlQueryExecutor.queryForList(sql, ALERT_RULE_ROW_MAPPER,
                                                              targetType.toString(), targetId);
        for (AlertRule rule : rules) {
            loadCollections(rule);
        }
        return rules;
    }

    public List<AlertRule> findBySeverityAndEnabledTrue(AlertSeverity severity) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE severity = ? AND is_enabled = true";
        List<AlertRule> rules = sqlQueryExecutor.queryForList(sql, ALERT_RULE_ROW_MAPPER, severity.toString());
        for (AlertRule rule : rules) {
            loadCollections(rule);
        }
        return rules;
    }

    @Override
    public AlertRule save(AlertRule rule) {
        if (rule.getId() == null) {
            rule = insert(rule);
        } else {
            rule = update(rule);
        }

        // Save collections
        saveCollections(rule);

        return rule;
    }

    private AlertRule insert(AlertRule rule) {
        String sql = "INSERT INTO " + TABLE_NAME + " (" +
            "rule_name, description, alert_type, severity, is_enabled, " +
            "condition_type, condition_expression, threshold_value, threshold_operator, " +
            "time_window_minutes, occurrence_count, target_type, target_id, " +
            "suppression_duration_minutes, last_triggered_at, trigger_count, " +
            "escalation_enabled, escalation_after_minutes, created_by, modified_by, " +
            "created_at, modified_at" +
            ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        LocalDateTime now = LocalDateTime.now();
        rule.setCreatedAt(now);
        rule.setUpdatedAt(now);

        UUID id = UUID.randomUUID();
        rule.setId(id);

        sql = "INSERT INTO " + TABLE_NAME + " (" +
            "id, rule_name, description, alert_type, severity, is_enabled, " +
            "condition_type, condition_expression, threshold_value, threshold_operator, " +
            "time_window_minutes, occurrence_count, target_type, target_id, " +
            "suppression_duration_minutes, last_triggered_at, trigger_count, " +
            "escalation_enabled, escalation_after_minutes, created_by, modified_by, " +
            "created_at, modified_at" +
            ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        sqlQueryExecutor.update(sql,
            id,
            rule.getRuleName(),
            rule.getDescription(),
            rule.getAlertType() != null ? rule.getAlertType().toString() : null,
            rule.getSeverity() != null ? rule.getSeverity().toString() : null,
            rule.isEnabled(),
            rule.getConditionType() != null ? rule.getConditionType().toString() : null,
            rule.getConditionExpression(),
            rule.getThresholdValue(),
            rule.getThresholdOperator() != null ? rule.getThresholdOperator().toString() : null,
            rule.getTimeWindowMinutes(),
            rule.getOccurrenceCount(),
            rule.getTargetType() != null ? rule.getTargetType().toString() : null,
            rule.getTargetId(),
            rule.getSuppressionDurationMinutes(),
            ResultSetMapper.toTimestamp(rule.getLastTriggeredAt()),
            rule.getTriggerCount(),
            rule.isEscalationEnabled(),
            rule.getEscalationAfterMinutes(),
            null,
            null,
            ResultSetMapper.toTimestamp(rule.getCreatedAt()),
            ResultSetMapper.toTimestamp(rule.getUpdatedAt())
        );
        return rule;
    }

    @Override
    public AlertRule update(AlertRule rule) {
        String sql = "UPDATE " + TABLE_NAME + " SET " +
            "rule_name = ?, description = ?, alert_type = ?, severity = ?, is_enabled = ?, " +
            "condition_type = ?, condition_expression = ?, threshold_value = ?, threshold_operator = ?, " +
            "time_window_minutes = ?, occurrence_count = ?, target_type = ?, target_id = ?, " +
            "suppression_duration_minutes = ?, last_triggered_at = ?, trigger_count = ?, " +
            "escalation_enabled = ?, escalation_after_minutes = ?, modified_by = ?, updated_at = ? " +
            "WHERE " + ID_COLUMN + " = ?";

        rule.setUpdatedAt(LocalDateTime.now());

        sqlQueryExecutor.update(sql,
            rule.getRuleName(),
            rule.getDescription(),
            rule.getAlertType() != null ? rule.getAlertType().toString() : null,
            rule.getSeverity() != null ? rule.getSeverity().toString() : null,
            rule.isEnabled(),
            rule.getConditionType() != null ? rule.getConditionType().toString() : null,
            rule.getConditionExpression(),
            rule.getThresholdValue(),
            rule.getThresholdOperator() != null ? rule.getThresholdOperator().toString() : null,
            rule.getTimeWindowMinutes(),
            rule.getOccurrenceCount(),
            rule.getTargetType() != null ? rule.getTargetType().toString() : null,
            rule.getTargetId(),
            rule.getSuppressionDurationMinutes(),
            ResultSetMapper.toTimestamp(rule.getLastTriggeredAt()),
            rule.getTriggerCount(),
            rule.isEscalationEnabled(),
            rule.getEscalationAfterMinutes(),
            null,
            ResultSetMapper.toTimestamp(rule.getUpdatedAt()),
            rule.getId()
        );

        return rule;
    }

    @Override
    public void deleteById(UUID id) {
        // Delete collections first (foreign key constraints)
        deleteCollections(id);

        // Then delete the rule
        super.deleteById(id);
    }

    /**
     * Load all collections for an alert rule
     */
    private void loadCollections(AlertRule rule) {
        loadNotificationChannelIds(rule);
        loadEscalationChannelIds(rule);
        loadTags(rule);
    }

    /**
     * Load notification channel IDs
     */
    private void loadNotificationChannelIds(AlertRule rule) {
        String sql = "SELECT channel_id FROM " + CHANNELS_TABLE + " WHERE alert_rule_id = ?";
        Set<String> channelIds = new HashSet<>(sqlQueryExecutor.queryForList(sql,
            (rs, rowNum) -> rs.getString("channel_id"), rule.getId()));
        rule.setNotificationChannelIds(channelIds);
    }

    /**
     * Load escalation channel IDs
     */
    private void loadEscalationChannelIds(AlertRule rule) {
        String sql = "SELECT channel_id FROM " + ESCALATION_CHANNELS_TABLE + " WHERE alert_rule_id = ?";
        Set<String> channelIds = new HashSet<>(sqlQueryExecutor.queryForList(sql,
            (rs, rowNum) -> rs.getString("channel_id"), rule.getId()));
        rule.setEscalationChannelIds(channelIds);
    }

    /**
     * Load tags
     */
    private void loadTags(AlertRule rule) {
        String sql = "SELECT tag FROM " + TAGS_TABLE + " WHERE alert_rule_id = ?";
        Set<String> tags = new HashSet<>(sqlQueryExecutor.queryForList(sql,
            (rs, rowNum) -> rs.getString("tag"), rule.getId()));
        rule.setTags(tags);
    }

    /**
     * Save all collections for an alert rule
     */
    private void saveCollections(AlertRule rule) {
        deleteCollections(rule.getId());
        saveNotificationChannelIds(rule);
        saveEscalationChannelIds(rule);
        saveTags(rule);
    }

    /**
     * Save notification channel IDs
     */
    private void saveNotificationChannelIds(AlertRule rule) {
        if (rule.getNotificationChannelIds() != null && !rule.getNotificationChannelIds().isEmpty()) {
            String sql = "INSERT INTO " + CHANNELS_TABLE + " (alert_rule_id, channel_id) VALUES (?, ?)";
            for (String channelId : rule.getNotificationChannelIds()) {
                sqlQueryExecutor.update(sql, rule.getId(), channelId);
            }
        }
    }

    /**
     * Save escalation channel IDs
     */
    private void saveEscalationChannelIds(AlertRule rule) {
        if (rule.getEscalationChannelIds() != null && !rule.getEscalationChannelIds().isEmpty()) {
            String sql = "INSERT INTO " + ESCALATION_CHANNELS_TABLE + " (alert_rule_id, channel_id) VALUES (?, ?)";
            for (String channelId : rule.getEscalationChannelIds()) {
                sqlQueryExecutor.update(sql, rule.getId(), channelId);
            }
        }
    }

    /**
     * Save tags
     */
    private void saveTags(AlertRule rule) {
        if (rule.getTags() != null && !rule.getTags().isEmpty()) {
            String sql = "INSERT INTO " + TAGS_TABLE + " (alert_rule_id, tag) VALUES (?, ?)";
            for (String tag : rule.getTags()) {
                sqlQueryExecutor.update(sql, rule.getId(), tag);
            }
        }
    }

    /**
     * Delete all collections for a rule
     */
    private void deleteCollections(UUID ruleId) {
        sqlQueryExecutor.update("DELETE FROM " + CHANNELS_TABLE + " WHERE alert_rule_id = ?", ruleId);
        sqlQueryExecutor.update("DELETE FROM " + ESCALATION_CHANNELS_TABLE + " WHERE alert_rule_id = ?", ruleId);
        sqlQueryExecutor.update("DELETE FROM " + TAGS_TABLE + " WHERE alert_rule_id = ?", ruleId);
    }

    public void updateTriggerInfo(UUID ruleId, LocalDateTime triggeredAt) {
        String sql = "UPDATE " + TABLE_NAME + " SET last_triggered_at = ?, trigger_count = trigger_count + 1 WHERE id = ?";
        sqlQueryExecutor.update(sql, ResultSetMapper.toTimestamp(triggeredAt), ruleId);
    }

    public boolean existsByRuleName(String ruleName) {
        String sql = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE rule_name = ?";
        long count = sqlQueryExecutor.count(sql, ruleName);
        return count > 0;
    }

    public List<AlertRule> findByAlertTypeAndEnabledTrue(AlertType alertType) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE alert_type = ? AND is_enabled = true";
        List<AlertRule> rules = sqlQueryExecutor.queryForList(sql, ALERT_RULE_ROW_MAPPER, alertType.toString());
        for (AlertRule rule : rules) {
            loadCollections(rule);
        }
        return rules;
    }

    public Page<AlertRule> findByAlertTypeAndEnabledTrue(AlertType alertType, Pageable pageable) {
        String baseQuery = "SELECT * FROM " + TABLE_NAME + " WHERE alert_type = ? AND is_enabled = true";
        String countQuery = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE alert_type = ? AND is_enabled = true";

        long total = sqlQueryExecutor.count(countQuery, alertType.toString());
        String paginatedQuery = baseQuery + SqlPaginationHelper.buildOrderByClause(pageable.getSort()) + SqlPaginationHelper.buildPaginationClause(pageable);
        List<AlertRule> rules = sqlQueryExecutor.queryForList(paginatedQuery, ALERT_RULE_ROW_MAPPER, alertType.toString());

        for (AlertRule rule : rules) {
            loadCollections(rule);
        }

        return new PageImpl<>(rules, pageable, total);
    }

    public List<AlertRule> findByTargetTypeAndTargetIdAndEnabledTrue(TargetType targetType, String targetId) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE target_type = ? AND target_id = ? AND is_enabled = true";
        List<AlertRule> rules = sqlQueryExecutor.queryForList(sql, ALERT_RULE_ROW_MAPPER,
                                                              targetType.toString(), targetId);
        for (AlertRule rule : rules) {
            loadCollections(rule);
        }
        return rules;
    }

    public Page<AlertRule> findBySeverityAndEnabledTrue(AlertSeverity severity, Pageable pageable) {
        String baseQuery = "SELECT * FROM " + TABLE_NAME + " WHERE severity = ? AND is_enabled = true";
        String countQuery = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE severity = ? AND is_enabled = true";

        long total = sqlQueryExecutor.count(countQuery, severity.toString());
        String paginatedQuery = baseQuery + SqlPaginationHelper.buildOrderByClause(pageable.getSort()) + SqlPaginationHelper.buildPaginationClause(pageable);
        List<AlertRule> rules = sqlQueryExecutor.queryForList(paginatedQuery, ALERT_RULE_ROW_MAPPER, severity.toString());

        for (AlertRule rule : rules) {
            loadCollections(rule);
        }

        return new PageImpl<>(rules, pageable, total);
    }

    public List<AlertRule> findByTagsAndEnabledTrue(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return new ArrayList<>();
        }

        String placeholders = String.join(",", Collections.nCopies(tags.size(), "?"));
        String sql = "SELECT DISTINCT ar.* FROM " + TABLE_NAME + " ar " +
                     "JOIN " + TAGS_TABLE + " t ON ar.id = t.alert_rule_id " +
                     "WHERE t.tag IN (" + placeholders + ") AND ar.is_enabled = true";

        List<AlertRule> rules = sqlQueryExecutor.queryForList(sql, ALERT_RULE_ROW_MAPPER, tags.toArray());
        for (AlertRule rule : rules) {
            loadCollections(rule);
        }
        return rules;
    }

    public List<AlertRule> findActiveRulesForFlow(String flowId) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE is_enabled = true AND " +
                     "(target_type = 'ALL' OR (target_type = 'FLOW' AND target_id = ?))";
        List<AlertRule> rules = sqlQueryExecutor.queryForList(sql, ALERT_RULE_ROW_MAPPER, flowId);
        for (AlertRule rule : rules) {
            loadCollections(rule);
        }
        return rules;
    }

    public List<AlertRule> findActiveRulesForAdapter(String adapterId) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE is_enabled = true AND " +
                     "(target_type = 'ALL' OR (target_type = 'ADAPTER' AND target_id = ?))";
        List<AlertRule> rules = sqlQueryExecutor.queryForList(sql, ALERT_RULE_ROW_MAPPER, adapterId);
        for (AlertRule rule : rules) {
            loadCollections(rule);
        }
        return rules;
    }

    public List<AlertRule> findSystemWideRules() {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE is_enabled = true AND " +
                     "target_type IN ('ALL', 'SYSTEM')";
        List<AlertRule> rules = sqlQueryExecutor.queryForList(sql, ALERT_RULE_ROW_MAPPER);
        for (AlertRule rule : rules) {
            loadCollections(rule);
        }
        return rules;
    }

    public long countByAlertTypeAndEnabledTrue(AlertType alertType) {
        String sql = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE alert_type = ? AND is_enabled = true";
        return sqlQueryExecutor.count(sql, alertType.toString());
    }
}