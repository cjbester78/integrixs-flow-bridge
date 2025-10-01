package com.integrixs.data.sql.repository;

import com.integrixs.data.model.NotificationChannel;
import com.integrixs.data.model.NotificationChannel.ChannelType;
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
 * SQL implementation of NotificationChannelRepository using native queries.
 * Handles @ElementCollection mapping for configuration as a separate table.
 */
@Repository("notificationChannelSqlRepository")
public class NotificationChannelSqlRepository extends BaseSqlRepository<NotificationChannel, UUID> {

    private static final String TABLE_NAME = "notification_channels";
    private static final String ID_COLUMN = "id";
    private static final String CONFIG_TABLE_NAME = "notification_channel_config";

    /**
     * Row mapper for NotificationChannel entity
     */
    private static final RowMapper<NotificationChannel> NOTIFICATION_CHANNEL_ROW_MAPPER = new RowMapper<NotificationChannel>() {
        @Override
        public NotificationChannel mapRow(ResultSet rs, int rowNum) throws SQLException {
            NotificationChannel channel = new NotificationChannel();
            channel.setId(ResultSetMapper.getUUID(rs, "id"));
            channel.setChannelName(ResultSetMapper.getString(rs, "channel_name"));
            channel.setDescription(ResultSetMapper.getString(rs, "description"));

            String channelTypeStr = ResultSetMapper.getString(rs, "channel_type");
            if (channelTypeStr != null) {
                channel.setChannelType(ChannelType.valueOf(channelTypeStr));
            }

            channel.setEnabled(rs.getBoolean("is_enabled"));
            channel.setRateLimitPerHour(ResultSetMapper.getInteger(rs, "rate_limit_per_hour"));
            channel.setLastNotificationAt(ResultSetMapper.getLocalDateTime(rs, "last_notification_at"));
            channel.setNotificationCountCurrentHour(ResultSetMapper.getInteger(rs, "notification_count_current_hour"));
            channel.setLastTestAt(ResultSetMapper.getLocalDateTime(rs, "last_test_at"));
            channel.setLastTestSuccess(ResultSetMapper.getBoolean(rs, "last_test_success"));
            channel.setLastTestMessage(ResultSetMapper.getString(rs, "last_test_message"));
            channel.setCreatedAt(ResultSetMapper.getLocalDateTime(rs, "created_at"));
            channel.setUpdatedAt(ResultSetMapper.getLocalDateTime(rs, "updated_at"));

            return channel;
        }
    };

    public NotificationChannelSqlRepository(SqlQueryExecutor sqlQueryExecutor) {
        super(sqlQueryExecutor, TABLE_NAME, ID_COLUMN, NOTIFICATION_CHANNEL_ROW_MAPPER);
    }

    @Override
    public Optional<NotificationChannel> findById(UUID id) {
        Optional<NotificationChannel> channelOpt = super.findById(id);
        if (channelOpt.isPresent()) {
            loadConfiguration(channelOpt.get());
        }
        return channelOpt;
    }

    @Override
    public List<NotificationChannel> findAll() {
        List<NotificationChannel> channels = super.findAll();
        for (NotificationChannel channel : channels) {
            loadConfiguration(channel);
        }
        return channels;
    }

    public Optional<NotificationChannel> findByChannelName(String channelName) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE channel_name = ?";
        List<NotificationChannel> results = sqlQueryExecutor.queryForList(sql, NOTIFICATION_CHANNEL_ROW_MAPPER, channelName);

        if (!results.isEmpty()) {
            NotificationChannel channel = results.get(0);
            loadConfiguration(channel);
            return Optional.of(channel);
        }
        return Optional.empty();
    }

    public List<NotificationChannel> findByChannelType(ChannelType channelType) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE channel_type = ?";
        List<NotificationChannel> channels = sqlQueryExecutor.queryForList(sql, NOTIFICATION_CHANNEL_ROW_MAPPER, channelType.toString());
        for (NotificationChannel channel : channels) {
            loadConfiguration(channel);
        }
        return channels;
    }

    public List<NotificationChannel> findByEnabled(boolean enabled) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE is_enabled = ?";
        List<NotificationChannel> channels = sqlQueryExecutor.queryForList(sql, NOTIFICATION_CHANNEL_ROW_MAPPER, enabled);
        for (NotificationChannel channel : channels) {
            loadConfiguration(channel);
        }
        return channels;
    }

    public List<NotificationChannel> findByEnabledTrue() {
        return findByEnabled(true);
    }

    public Page<NotificationChannel> findAll(Pageable pageable) {
        String baseQuery = "SELECT * FROM " + TABLE_NAME;
        String countQuery = "SELECT COUNT(*) FROM " + TABLE_NAME;

        // Get total count
        long total = sqlQueryExecutor.count(countQuery);

        // Apply pagination
        String orderBy = SqlPaginationHelper.buildOrderByClause(pageable.getSort());
        String pagination = SqlPaginationHelper.buildPaginationClause(pageable);
        String paginatedQuery = baseQuery + orderBy + pagination;
        List<NotificationChannel> channels = sqlQueryExecutor.queryForList(paginatedQuery, NOTIFICATION_CHANNEL_ROW_MAPPER);

        // Load configurations
        for (NotificationChannel channel : channels) {
            loadConfiguration(channel);
        }

        return new PageImpl<>(channels, pageable, total);
    }

    public Page<NotificationChannel> findByEnabledTrue(Pageable pageable) {
        String baseQuery = "SELECT * FROM " + TABLE_NAME + " WHERE is_enabled = true";
        String countQuery = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE is_enabled = true";

        // Get total count
        long total = sqlQueryExecutor.count(countQuery);

        // Apply pagination
        String orderBy = SqlPaginationHelper.buildOrderByClause(pageable.getSort());
        String pagination = SqlPaginationHelper.buildPaginationClause(pageable);
        String paginatedQuery = baseQuery + orderBy + pagination;
        List<NotificationChannel> channels = sqlQueryExecutor.queryForList(paginatedQuery, NOTIFICATION_CHANNEL_ROW_MAPPER);

        // Load configurations
        for (NotificationChannel channel : channels) {
            loadConfiguration(channel);
        }

        return new PageImpl<>(channels, pageable, total);
    }

    public List<NotificationChannel> findByChannelTypeAndEnabledTrue(ChannelType channelType) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE channel_type = ? AND is_enabled = true";
        List<NotificationChannel> channels = sqlQueryExecutor.queryForList(sql, NOTIFICATION_CHANNEL_ROW_MAPPER, channelType.toString());
        for (NotificationChannel channel : channels) {
            loadConfiguration(channel);
        }
        return channels;
    }

    public Page<NotificationChannel> findByChannelTypeAndEnabledTrue(ChannelType channelType, Pageable pageable) {
        String baseQuery = "SELECT * FROM " + TABLE_NAME + " WHERE channel_type = ? AND is_enabled = true";
        String countQuery = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE channel_type = ? AND is_enabled = true";

        // Get total count
        long total = sqlQueryExecutor.count(countQuery, channelType.toString());

        // Apply pagination
        String orderBy = SqlPaginationHelper.buildOrderByClause(pageable.getSort());
        String pagination = SqlPaginationHelper.buildPaginationClause(pageable);
        String paginatedQuery = baseQuery + orderBy + pagination;
        List<NotificationChannel> channels = sqlQueryExecutor.queryForList(paginatedQuery, NOTIFICATION_CHANNEL_ROW_MAPPER, channelType.toString());

        // Load configurations
        for (NotificationChannel channel : channels) {
            loadConfiguration(channel);
        }

        return new PageImpl<>(channels, pageable, total);
    }

    public List<NotificationChannel> findEnabledChannelsByIds(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }

        String placeholders = String.join(",", Collections.nCopies(ids.size(), "?"));
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE id IN (" + placeholders + ") AND is_enabled = true";

        List<NotificationChannel> channels = sqlQueryExecutor.queryForList(sql, NOTIFICATION_CHANNEL_ROW_MAPPER, ids.toArray());
        for (NotificationChannel channel : channels) {
            loadConfiguration(channel);
        }
        return channels;
    }

    public boolean existsByChannelName(String channelName) {
        String sql = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE channel_name = ?";
        long count = sqlQueryExecutor.count(sql, channelName);
        return count > 0;
    }

    public long countByChannelTypeAndEnabledTrue(ChannelType channelType) {
        String sql = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE channel_type = ? AND is_enabled = true";
        return sqlQueryExecutor.count(sql, channelType.toString());
    }

    public List<NotificationChannel> findChannelsNeedingRateLimitReset(LocalDateTime oneHourAgo) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE is_enabled = true AND " +
                     "rate_limit_per_hour IS NOT NULL AND notification_count_current_hour > 0 AND " +
                     "last_notification_at < ?";

        List<NotificationChannel> channels = sqlQueryExecutor.queryForList(sql, NOTIFICATION_CHANNEL_ROW_MAPPER,
                                                                           ResultSetMapper.toTimestamp(oneHourAgo));
        for (NotificationChannel channel : channels) {
            loadConfiguration(channel);
        }
        return channels;
    }

    @Override
    public NotificationChannel save(NotificationChannel channel) {
        if (channel.getId() == null) {
            // Insert new channel
            channel = insert(channel);
        } else {
            // Update existing channel
            channel = update(channel);
        }

        // Save configuration map
        saveConfiguration(channel);

        return channel;
    }

    private NotificationChannel insert(NotificationChannel channel) {
        String sql = "INSERT INTO " + TABLE_NAME + " (" +
            "channel_name, description, channel_type, is_enabled, " +
            "rate_limit_per_hour, last_notification_at, notification_count_current_hour, " +
            "last_test_at, last_test_success, last_test_message, created_at, modified_at" +
            ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        LocalDateTime now = LocalDateTime.now();
        channel.setCreatedAt(now);
        channel.setUpdatedAt(now);

        UUID id = UUID.randomUUID();
        channel.setId(id);

        sql = "INSERT INTO " + TABLE_NAME + " (" +
            "id, channel_name, description, channel_type, is_enabled, " +
            "rate_limit_per_hour, last_notification_at, notification_count_current_hour, " +
            "last_test_at, last_test_success, last_test_message, created_at, modified_at" +
            ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        sqlQueryExecutor.update(sql,
            id,
            channel.getChannelName(),
            channel.getDescription(),
            channel.getChannelType() != null ? channel.getChannelType().toString() : null,
            channel.isEnabled(),
            channel.getRateLimitPerHour(),
            ResultSetMapper.toTimestamp(channel.getLastNotificationAt()),
            channel.getNotificationCountCurrentHour(),
            ResultSetMapper.toTimestamp(channel.getLastTestAt()),
            channel.getLastTestSuccess(),
            channel.getLastTestMessage(),
            ResultSetMapper.toTimestamp(channel.getCreatedAt()),
            ResultSetMapper.toTimestamp(channel.getUpdatedAt())
        );

        return channel;
    }

    @Override
    public NotificationChannel update(NotificationChannel channel) {
        String sql = "UPDATE " + TABLE_NAME + " SET " +
            "channel_name = ?, description = ?, channel_type = ?, is_enabled = ?, " +
            "rate_limit_per_hour = ?, last_notification_at = ?, notification_count_current_hour = ?, " +
            "last_test_at = ?, last_test_success = ?, last_test_message = ?, updated_at = ? " +
            "WHERE " + ID_COLUMN + " = ?";

        channel.setUpdatedAt(LocalDateTime.now());

        sqlQueryExecutor.update(sql,
            channel.getChannelName(),
            channel.getDescription(),
            channel.getChannelType() != null ? channel.getChannelType().toString() : null,
            channel.isEnabled(),
            channel.getRateLimitPerHour(),
            ResultSetMapper.toTimestamp(channel.getLastNotificationAt()),
            channel.getNotificationCountCurrentHour(),
            ResultSetMapper.toTimestamp(channel.getLastTestAt()),
            channel.getLastTestSuccess(),
            channel.getLastTestMessage(),
            ResultSetMapper.toTimestamp(channel.getUpdatedAt()),
            channel.getId()
        );

        return channel;
    }

    @Override
    public void deleteById(UUID id) {
        // Delete configuration first (foreign key constraint)
        deleteConfiguration(id);

        // Then delete the channel
        super.deleteById(id);
    }

    /**
     * Load configuration map from notification_channel_config table
     */
    private void loadConfiguration(NotificationChannel channel) {
        String sql = "SELECT config_key, config_value FROM " + CONFIG_TABLE_NAME + " WHERE channel_id = ?";

        Map<String, String> configuration = new HashMap<>();
        List<Map<String, Object>> results = sqlQueryExecutor.queryForList(sql,
            (rs, rowNum) -> {
                Map<String, Object> row = new HashMap<>();
                row.put("config_key", rs.getString("config_key"));
                row.put("config_value", rs.getString("config_value"));
                return row;
            }, channel.getId());

        for (Map<String, Object> row : results) {
            configuration.put((String) row.get("config_key"), (String) row.get("config_value"));
        }

        channel.setConfiguration(configuration);
    }

    /**
     * Save configuration map to notification_channel_config table
     */
    private void saveConfiguration(NotificationChannel channel) {
        // Delete existing configuration
        deleteConfiguration(channel.getId());

        // Insert new configuration
        if (channel.getConfiguration() != null && !channel.getConfiguration().isEmpty()) {
            String sql = "INSERT INTO " + CONFIG_TABLE_NAME + " (channel_id, config_key, config_value) VALUES (?, ?, ?)";

            for (Map.Entry<String, String> entry : channel.getConfiguration().entrySet()) {
                sqlQueryExecutor.update(sql, channel.getId(), entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Delete configuration for a channel
     */
    private void deleteConfiguration(UUID channelId) {
        String sql = "DELETE FROM " + CONFIG_TABLE_NAME + " WHERE channel_id = ?";
        sqlQueryExecutor.update(sql, channelId);
    }

    /**
     * Update notification count and last notification timestamp
     */
    public void updateNotificationStats(UUID channelId) {
        String sql = "UPDATE " + TABLE_NAME + " SET " +
            "last_notification_at = ?, notification_count_current_hour = notification_count_current_hour + 1 " +
            "WHERE id = ?";

        sqlQueryExecutor.update(sql, ResultSetMapper.toTimestamp(LocalDateTime.now()), channelId);
    }

    /**
     * Reset hourly notification count for all channels
     */
    public void resetHourlyNotificationCounts() {
        String sql = "UPDATE " + TABLE_NAME + " SET notification_count_current_hour = 0";
        sqlQueryExecutor.update(sql);
    }

    /**
     * Update test results
     */
    public void updateTestResults(UUID channelId, boolean success, String message) {
        String sql = "UPDATE " + TABLE_NAME + " SET " +
            "last_test_at = ?, last_test_success = ?, last_test_message = ? " +
            "WHERE id = ?";

        sqlQueryExecutor.update(sql,
            ResultSetMapper.toTimestamp(LocalDateTime.now()),
            success,
            message,
            channelId);
    }
}