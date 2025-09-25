package com.integrixs.backend.config;

import com.integrixs.backend.interceptor.MDCTaskDecorator;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Scheduling configuration with MDC context propagation.
 * Ensures that scheduled tasks maintain logging context.
 */
@Configuration
@EnableScheduling
public class SchedulingConfig implements SchedulingConfigurer {

    private static final Logger log = LoggerFactory.getLogger(SchedulingConfig.class);


    private final MDCTaskDecorator mdcTaskDecorator;

    public SchedulingConfig(MDCTaskDecorator mdcTaskDecorator) {
        this.mdcTaskDecorator = mdcTaskDecorator;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(taskScheduler());
    }

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler() {
            @Override
            protected ScheduledExecutorService createExecutor(int poolSize,
                    java.util.concurrent.ThreadFactory threadFactory,
                    java.util.concurrent.RejectedExecutionHandler rejectedExecutionHandler) {

                // Create a scheduled executor that preserves MDC context
                return Executors.newScheduledThreadPool(poolSize, runnable -> {
                    // Capture current MDC context
                    Map<String, String> mdcContext = MDC.getCopyOfContextMap();

                    Thread thread = threadFactory.newThread(() -> {
                        // Set MDC context in the new thread
                        if(mdcContext != null) {
                            MDC.setContextMap(mdcContext);
                        }
                        try {
                            runnable.run();
                        } finally {
                            MDC.clear();
                        }
                    });

                    return thread;
                });
            }
        };

        scheduler.setPoolSize(10);
        scheduler.setThreadNamePrefix("IntegrixScheduled-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(30);
        scheduler.setRejectedExecutionHandler(
            (r, executor) -> log.error("Scheduled task rejected: {}", r.toString())
       );
        scheduler.initialize();

        log.info("Configured scheduled task executor with MDC context propagation");
        return scheduler;
    }

    /**
     * Bean for custom scheduled thread pool executor with MDC support
     * This can be used by services that need custom scheduling
     */
    @Bean(name = "mdcScheduledExecutor")
    public ScheduledExecutorService mdcScheduledExecutorService() {
        return Executors.newScheduledThreadPool(5, runnable -> {
            Map<String, String> mdcContext = MDC.getCopyOfContextMap();

            Thread thread = new Thread(() -> {
                if(mdcContext != null) {
                    MDC.setContextMap(mdcContext);
                }
                try {
                    runnable.run();
                } finally {
                    MDC.clear();
                }
            });

            thread.setName("IntegrixCustomScheduled-" + thread.getId());
            return thread;
        });
    }

    /**
     * Bean for CompletableFuture executor with MDC support
     * Used for async operations that return CompletableFuture
     */
    @Bean(name = "mdcForkJoinPool")
    public Executor mdcForkJoinPool() {
        return new java.util.concurrent.ForkJoinPool(
            Runtime.getRuntime().availableProcessors(),
            pool -> {
                Map<String, String> mdcContext = MDC.getCopyOfContextMap();

                java.util.concurrent.ForkJoinWorkerThread thread =
                    java.util.concurrent.ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);

                thread.setName("IntegrixForkJoin-" + thread.getId());

                // Wrap the thread to set MDC context
                return new java.util.concurrent.ForkJoinWorkerThread(pool) {
                    @Override
                    protected void onStart() {
                        super.onStart();
                        if(mdcContext != null) {
                            MDC.setContextMap(mdcContext);
                        }
                    }

                    @Override
                    protected void onTermination(Throwable exception) {
                        MDC.clear();
                        super.onTermination(exception);
                    }
                };
            },
            null,
            false
       );
    }
}
