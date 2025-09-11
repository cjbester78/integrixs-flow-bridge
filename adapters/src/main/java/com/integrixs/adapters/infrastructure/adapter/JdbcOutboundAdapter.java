package com.integrixs.adapters.infrastructure.adapter;

import com.integrixs.adapters.core.AbstractOutboundAdapter;
import com.integrixs.adapters.config.JdbcOutboundAdapterConfig;
import com.integrixs.adapters.domain.model.AdapterConfiguration;
import com.integrixs.adapters.core.AdapterResult;
import com.integrixs.shared.exceptions.AdapterException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;

public class JdbcOutboundAdapter extends AbstractOutboundAdapter {
    private static final Logger logger = LoggerFactory.getLogger(JdbcOutboundAdapter.class);
    
    private final JdbcOutboundAdapterConfig config;
    private HikariDataSource dataSource;
    
    public JdbcOutboundAdapter(JdbcOutboundAdapterConfig config) {
        super(AdapterConfiguration.AdapterTypeEnum.JDBC);
        this.config = config;
    }
    
    @Override
    protected void doReceiverInitialize() throws Exception {
        logger.info("Initializing JDBC outbound adapter with URL: {}", maskSensitiveUrl(config.getJdbcUrl()));
        
        validateConfiguration();
        dataSource = createDataSource();
        
        logger.info("JDBC outbound adapter initialized successfully");
    }
    
    @Override
    protected void doReceiverDestroy() throws Exception {
        logger.info("Destroying JDBC outbound adapter");
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            dataSource = null;
        }
    }
    
    @Override
    protected AdapterResult doReceive(Object criteria) throws Exception {
        if (config.getQuery() == null || config.getQuery().isEmpty()) {
            return AdapterResult.success(null, "No query configured");
        }
        
        List<Map<String, Object>> results = new ArrayList<>();
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(config.getQuery());
             ResultSet rs = stmt.executeQuery()) {
            
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    Object value = rs.getObject(i);
                    row.put(columnName, value);
                }
                results.add(row);
                
                // Limit results if configured
                if (config.getMaxRows() > 0 && results.size() >= config.getMaxRows()) {
                    break;
                }
            }
        }
        
        if (results.isEmpty()) {
            return AdapterResult.success(null, "No data found");
        }
        
        return AdapterResult.success(results, String.format("Retrieved %d rows", results.size()));
    }
    
    @Override
    protected long getPollingIntervalMs() {
        return config.getPollingInterval() * 1000L; // Convert seconds to milliseconds
    }
    
    private void validateConfiguration() throws AdapterException {
        if (config.getJdbcUrl() == null || config.getJdbcUrl().isEmpty()) {
            throw new AdapterException("JDBC URL is required");
        }
        if (config.getUsername() == null || config.getUsername().isEmpty()) {
            throw new AdapterException("Username is required");
        }
    }
    
    private HikariDataSource createDataSource() throws Exception {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(config.getJdbcUrl());
        hikariConfig.setUsername(config.getUsername());
        hikariConfig.setPassword(config.getPassword());
        
        // Set pool configuration
        hikariConfig.setMinimumIdle(config.getMinPoolSize());
        hikariConfig.setMaximumPoolSize(config.getMaxPoolSize());
        hikariConfig.setConnectionTimeout(config.getConnectionTimeout() * 1000L);
        hikariConfig.setIdleTimeout(600000); // 10 minutes
        hikariConfig.setMaxLifetime(1800000); // 30 minutes
        
        return new HikariDataSource(hikariConfig);
    }
    
    private String maskSensitiveUrl(String url) {
        if (url == null) return null;
        return url.replaceAll("password=[^&;]*", "password=****");
    }
}