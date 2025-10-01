package com.integrixs.backend.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.core.instrument.binder.system.UptimeMetrics;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Metrics configuration for comprehensive application monitoring.
 * Configures Micrometer with Prometheus registry and custom metrics.
 */
@Configuration
public class MetricsConfig implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(MetricsConfig.class);


    @Value("${spring.application.name:integrixs-flow-bridge}")
    private String applicationName;

    @Value("${integrix.metrics.tags.environment:development}")
    private String environment;

    @Value("${integrix.metrics.tags.region:default}")
    private String region;

    /**
     * Customize the meter registry with common tags
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config()
            .commonTags(
                "application", applicationName,
                "environment", environment,
                "region", region
           );
    }

    /**
     * Enable @Timed annotation support
     */
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }

    /**
     * JVM metrics
     */
    @Bean
    public JvmMemoryMetrics jvmMemoryMetrics() {
        return new JvmMemoryMetrics();
    }

    @Bean
    public JvmGcMetrics jvmGcMetrics() {
        return new JvmGcMetrics();
    }

    @Bean
    public JvmThreadMetrics jvmThreadMetrics() {
        return new JvmThreadMetrics();
    }

    @Bean
    public ClassLoaderMetrics classLoaderMetrics() {
        return new ClassLoaderMetrics();
    }

    /**
     * System metrics
     */
    @Bean
    public ProcessorMetrics processorMetrics() {
        return new ProcessorMetrics();
    }

    @Bean
    public UptimeMetrics uptimeMetrics() {
        return new UptimeMetrics();
    }

    /**
     * Custom business metrics helper
     */
    @Bean
    public BusinessMetrics businessMetrics(MeterRegistry meterRegistry) {
        return new BusinessMetrics(meterRegistry);
    }

    /**
     * HTTP request metrics interceptor
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HttpMetricsInterceptor());
    }

    /**
     * Business metrics helper class
     */
    public static class BusinessMetrics {
        private final MeterRegistry meterRegistry;

        public BusinessMetrics(MeterRegistry meterRegistry) {
            this.meterRegistry = meterRegistry;
            log.info("Initialized business metrics collector");
        }

        public void recordFlowExecution(String flowName, String status, long duration) {
            meterRegistry.counter("integrix.flow.executions",
                "flow", flowName,
                "status", status
           ).increment();

            meterRegistry.timer("integrix.flow.duration",
                "flow", flowName,
                "status", status
           ).record(duration, java.util.concurrent.TimeUnit.MILLISECONDS);
        }

        public void recordAdapterCall(String adapterType, String operation, boolean success, long duration) {
            meterRegistry.counter("integrix.adapter.calls",
                "type", adapterType,
                "operation", operation,
                "success", String.valueOf(success)
           ).increment();

            meterRegistry.timer("integrix.adapter.duration",
                "type", adapterType,
                "operation", operation
           ).record(duration, java.util.concurrent.TimeUnit.MILLISECONDS);
        }

        public void recordTransformation(String type, boolean success, long processingTime) {
            meterRegistry.counter("integrix.transformation.count",
                "type", type,
                "success", String.valueOf(success)
           ).increment();

            meterRegistry.timer("integrix.transformation.duration",
                "type", type
           ).record(processingTime, java.util.concurrent.TimeUnit.MILLISECONDS);
        }

        public void recordAuthenticationAttempt(String method, boolean success) {
            meterRegistry.counter("integrix.auth.attempts",
                "method", method,
                "success", String.valueOf(success)
           ).increment();
        }

        public void recordMessageSize(String direction, String protocol, long sizeBytes) {
            meterRegistry.summary("integrix.message.size",
                "direction", direction,
                "protocol", protocol
           ).record(sizeBytes);
        }

        public void updateActiveFlows(int count) {
            meterRegistry.gauge("integrix.flows.active", Tags.empty(), count);
        }

        public void updateQueueSize(String queueName, int size) {
            meterRegistry.gauge("integrix.queue.size",
                Tags.of("queue", queueName),
                size
           );
        }
    }

    /**
     * HTTP metrics interceptor
     */
    private static class HttpMetricsInterceptor implements HandlerInterceptor {

        private static final String START_TIME_ATTR = "http-request-start-time";

        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
            request.setAttribute(START_TIME_ATTR, System.currentTimeMillis());
            return true;
        }

        @Override
        public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                   Object handler, Exception ex) {
            Long startTime = (Long) request.getAttribute(START_TIME_ATTR);
            if(startTime != null) {
                long duration = System.currentTimeMillis()-startTime;
                log.trace("HTTP {} {}-{} in {}ms",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    duration
               );
            }
        }
    }
}
