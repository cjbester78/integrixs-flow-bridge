package com.integrixs.backend.config;

import com.integrixs.backend.filter.MDCFilter;
import com.integrixs.backend.logging.EnhancedAuthenticationLogger;
import com.integrixs.backend.logging.EnhancedFlowExecutionLogger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@Order(Ordered.LOWEST_PRECEDENCE)
public class WebConfig implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(WebConfig.class);


    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Don't handle WebSocket paths
        registry.setOrder(Ordered.LOWEST_PRECEDENCE);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Explicitly handle static resources first
        registry.addResourceHandler("/assets/**")
                .addResourceLocations("classpath:/public/assets/")
                .setCachePeriod(3600);

        registry.addResourceHandler("/*.js", "/*.css", "/*.ico", "/*.png", "/*.jpg", "/*.json")
                .addResourceLocations("classpath:/public/")
                .setCachePeriod(3600);

        // Handle React routes - but NOT WebSocket paths
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/public/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        // NEVER handle WebSocket paths
                        if(resourcePath.startsWith("ws/") ||
                            resourcePath.equals("wstest") ||
                            resourcePath.equals("echo") ||
                            resourcePath.equals("test - ws") ||
                            resourcePath.equals("direct - ws") ||
                            resourcePath.equals("minimal - echo") ||
                            resourcePath.equals("basic")) {
                            return null;
                        }

                        Resource requestedResource = location.createRelative(resourcePath);

                        // If the resource exists and is readable, serve it
                        if(requestedResource.exists() && requestedResource.isReadable()) {
                            return requestedResource;
                        }

                        // Skip API routes, actuator, and other backend routes
                        if(resourcePath.startsWith("api/") ||
                            resourcePath.startsWith("actuator/") ||
                            resourcePath.startsWith("swagger - ui/") ||
                            resourcePath.startsWith("v3/api - docs") ||
                            resourcePath.startsWith("webjars/")) {
                            return null;
                        }

                        // For all other routes, return index.html(React app)
                        return new ClassPathResource("/public/index.html");
                    }
                });
    }

    /**
     * Register MDC filter with highest priority
     */
    @Bean
    public FilterRegistrationBean<MDCFilter> mdcFilterRegistration(MDCFilter mdcFilter) {
        FilterRegistrationBean<MDCFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(mdcFilter);
        registration.addUrlPatterns("/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registration.setName("mdcFilter");
        log.info("Registered MDC filter with highest priority");
        return registration;
    }

    /**
     * Authentication event publisher for Spring Security events
     */
    @Bean
    @ConditionalOnMissingBean
    public AuthenticationEventPublisher authenticationEventPublisher() {
        log.info("Configured authentication event publisher");
        return new DefaultAuthenticationEventPublisher();
    }

    /**
     * Request logging filter for debugging(optional)
     */
    @Bean
    @ConditionalOnMissingBean
    public CommonsRequestLoggingFilter requestLoggingFilter() {
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
        filter.setIncludeClientInfo(true);
        filter.setIncludeQueryString(true);
        filter.setIncludePayload(false); // Set to true for debugging
        filter.setMaxPayloadLength(1000);
        filter.setIncludeHeaders(false); // Set to true to log headers
        filter.setBeforeMessagePrefix("REQUEST: ");
        filter.setAfterMessagePrefix("REQUEST COMPLETE: ");
        return filter;
    }

    /**
     * Ensure enhanced authentication logger is available
     */
    @Bean
    @ConditionalOnMissingBean
    public EnhancedAuthenticationLogger enhancedAuthenticationLogger() {
        log.info("Created enhanced authentication logger");
        return new EnhancedAuthenticationLogger();
    }

    /**
     * Ensure enhanced flow execution logger is available
     */
    @Bean
    @ConditionalOnMissingBean
    public EnhancedFlowExecutionLogger enhancedFlowExecutionLogger() {
        log.info("Created enhanced flow execution logger");
        return new EnhancedFlowExecutionLogger();
    }
}
