package com.integrixs.data.config;

import com.integrixs.data.sql.core.SqlQueryExecutor;
import com.integrixs.data.sql.repository.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Configuration for SQL repository implementations.
 * All repositories use native SQL queries.
 */
@Configuration
public class SqlRepositoryConfig {

    @Bean
    public SqlQueryExecutor sqlQueryExecutor(JdbcTemplate jdbcTemplate) {
        return new SqlQueryExecutor(jdbcTemplate);
    }

    @Bean
    public UserSqlRepository userSqlRepository(SqlQueryExecutor sqlQueryExecutor) {
        return new UserSqlRepository(sqlQueryExecutor);
    }

    @Bean
    public RoleSqlRepository roleSqlRepository(SqlQueryExecutor sqlQueryExecutor) {
        return new RoleSqlRepository(sqlQueryExecutor);
    }

    @Bean
    public SystemLogSqlRepository systemLogSqlRepository(SqlQueryExecutor sqlQueryExecutor) {
        return new SystemLogSqlRepository(sqlQueryExecutor);
    }

    @Bean
    public CertificateSqlRepository certificateSqlRepository(SqlQueryExecutor sqlQueryExecutor) {
        return new CertificateSqlRepository(sqlQueryExecutor);
    }

    @Bean
    public SystemSettingSqlRepository systemSettingSqlRepository(SqlQueryExecutor sqlQueryExecutor) {
        return new SystemSettingSqlRepository(sqlQueryExecutor);
    }

    @Bean
    public EventStoreSqlRepository eventStoreSqlRepository(SqlQueryExecutor sqlQueryExecutor) {
        return new EventStoreSqlRepository(sqlQueryExecutor);
    }

    @Bean
    public JarFileSqlRepository jarFileSqlRepository(SqlQueryExecutor sqlQueryExecutor, ObjectMapper objectMapper) {
        return new JarFileSqlRepository(sqlQueryExecutor, objectMapper);
    }

    @Bean
    public AdapterPayloadSqlRepository adapterPayloadSqlRepository(SqlQueryExecutor sqlQueryExecutor) {
        return new AdapterPayloadSqlRepository(sqlQueryExecutor);
    }

    @Bean
    public NotificationChannelSqlRepository notificationChannelSqlRepository(SqlQueryExecutor sqlQueryExecutor) {
        return new NotificationChannelSqlRepository(sqlQueryExecutor);
    }

    @Bean
    public AlertRuleSqlRepository alertRuleSqlRepository(SqlQueryExecutor sqlQueryExecutor) {
        return new AlertRuleSqlRepository(sqlQueryExecutor);
    }

    @Bean
    public SystemConfigurationSqlRepository systemConfigurationSqlRepository(SqlQueryExecutor sqlQueryExecutor,
                                                                             UserSqlRepository userSqlRepository) {
        return new SystemConfigurationSqlRepository(sqlQueryExecutor, userSqlRepository);
    }

    @Bean
    public TransformationCustomFunctionSqlRepository transformationCustomFunctionSqlRepository(SqlQueryExecutor sqlQueryExecutor) {
        return new TransformationCustomFunctionSqlRepository(sqlQueryExecutor);
    }

    @Bean
    public CommunicationAdapterSqlRepository communicationAdapterSqlRepository(SqlQueryExecutor sqlQueryExecutor) {
        return new CommunicationAdapterSqlRepository(sqlQueryExecutor);
    }

    @Bean
    public FlowTransformationSqlRepository flowTransformationSqlRepository(SqlQueryExecutor sqlQueryExecutor) {
        return new FlowTransformationSqlRepository(sqlQueryExecutor);
    }

    @Bean
    public OrchestrationTargetSqlRepository orchestrationTargetSqlRepository(SqlQueryExecutor sqlQueryExecutor) {
        return new OrchestrationTargetSqlRepository(sqlQueryExecutor);
    }

    @Bean
    public IntegrationFlowSqlRepository integrationFlowSqlRepository(SqlQueryExecutor sqlQueryExecutor,
                                                                     FlowTransformationSqlRepository transformationRepository,
                                                                     OrchestrationTargetSqlRepository orchestrationTargetRepository) {
        return new IntegrationFlowSqlRepository(sqlQueryExecutor, transformationRepository, orchestrationTargetRepository);
    }

    @Bean
    public MessageSqlRepository messageSqlRepository(SqlQueryExecutor sqlQueryExecutor) {
        return new MessageSqlRepository(sqlQueryExecutor);
    }

    @Bean
    public BusinessComponentSqlRepository businessComponentSqlRepository(SqlQueryExecutor sqlQueryExecutor) {
        return new BusinessComponentSqlRepository(sqlQueryExecutor);
    }
}