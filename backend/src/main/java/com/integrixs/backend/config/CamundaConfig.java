package com.integrixs.backend.config;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.spring.ProcessEngineFactoryBean;
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

@Configuration
public class CamundaConfig {

    @Autowired
    private DataSource dataSource;

    @Bean
    public SpringProcessEngineConfiguration processEngineConfiguration() {
        SpringProcessEngineConfiguration config = new SpringProcessEngineConfiguration();

        // Database configuration
        config.setDataSource(dataSource);
        config.setDatabaseSchemaUpdate("true");
        config.setDatabaseType("postgres");

        // Transaction manager
        config.setTransactionManager(transactionManager());

        // Job execution
        config.setJobExecutorActivate(true);

        // History configuration
        config.setHistory("full");
        config.setHistoryCleanupBatchWindowStartTime("00:00");
        config.setHistoryCleanupBatchWindowEndTime("06:00");

        // Deployment
        try {
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath*:bpmn/*.bpmn");
            config.setDeploymentResources(resources);
        } catch (Exception e) {
            // If no BPMN files found, continue without deployment resources
            config.setDeploymentResources(new Resource[0]);
        }

        // Metrics
        config.setMetricsEnabled(true);
        config.setDbMetricsReporterActivate(true);

        // Authorization
        config.setAuthorizationEnabled(true);

        return config;
    }

    @Bean
    @Primary
    public ProcessEngine processEngine() throws Exception {
        ProcessEngineFactoryBean factoryBean = new ProcessEngineFactoryBean();
        factoryBean.setProcessEngineConfiguration(processEngineConfiguration());
        return factoryBean.getObject();
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public RepositoryService repositoryService(ProcessEngine processEngine) {
        return processEngine.getRepositoryService();
    }

    @Bean
    public RuntimeService runtimeService(ProcessEngine processEngine) {
        return processEngine.getRuntimeService();
    }

    @Bean
    public TaskService taskService(ProcessEngine processEngine) {
        return processEngine.getTaskService();
    }

    @Bean
    public HistoryService historyService(ProcessEngine processEngine) {
        return processEngine.getHistoryService();
    }

    @Bean
    public ManagementService managementService(ProcessEngine processEngine) {
        return processEngine.getManagementService();
    }
}
