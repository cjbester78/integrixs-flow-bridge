package com.integrixs.backend.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration for event - driven architecture.
 *
 * <p>Configures async event processing and thread pools.
 *
 * @author Integration Team
 * @since 1.0.0
 */
@Configuration
public class EventDrivenConfig {

    /**
     * Creates the event executor for async event processing.
     *
     * @return configured thread pool executor
     */

    private static final Logger log = LoggerFactory.getLogger(EventDrivenConfig.class);

    @Bean(name = "eventExecutor")
    public TaskExecutor eventExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("event - handler-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }

    /**
     * Configures the application event multicaster for async event processing.
     *
     * @param eventExecutor the event executor
     * @return configured event multicaster
     */
    @Bean(name = "applicationEventMulticaster")
    public ApplicationEventMulticaster applicationEventMulticaster(@Qualifier("eventExecutor") TaskExecutor eventExecutor) {
        SimpleApplicationEventMulticaster eventMulticaster = new SimpleApplicationEventMulticaster();
        eventMulticaster.setTaskExecutor(eventExecutor);
        eventMulticaster.setErrorHandler(throwable ->
            log.error("Error in event listener", throwable)
       );
        return eventMulticaster;
    }

}
