package com.integrixs.backend.config;

import com.integrixs.adapters.config.HttpInboundAdapterConfig;
import com.integrixs.adapters.controller.HttpAdapterController;
import com.integrixs.backend.service.AdapterHealthMonitor;
import com.integrixs.backend.service.AdapterPoolManager;
import com.integrixs.backend.service.EnhancedAdapterExecutionService;
import com.integrixs.backend.service.ErrorHandlingService;
import com.integrixs.backend.service.MessageQueueService;
import com.integrixs.engine.AdapterExecutor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@TestConfiguration
@Profile("test")
public class TestAdapterConfiguration {

    @MockBean
    private AdapterExecutor adapterExecutor;

    @MockBean
    private AdapterHealthMonitor adapterHealthMonitor;

    @MockBean
    private AdapterPoolManager adapterPoolManager;

    @MockBean
    private EnhancedAdapterExecutionService enhancedAdapterExecutionService;

    @MockBean
    private ErrorHandlingService errorHandlingService;

    @MockBean
    private MessageQueueService messageQueueService;

    @MockBean
    private HttpInboundAdapterConfig httpInboundAdapterConfig;

}
