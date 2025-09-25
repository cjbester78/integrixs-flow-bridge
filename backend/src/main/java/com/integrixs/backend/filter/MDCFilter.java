package com.integrixs.backend.filter;

import com.integrixs.backend.config.BusinessLoggingConfig;
import com.integrixs.backend.security.jwt.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filter to populate MDC(Mapped Diagnostic Context) with request context.
 */
@Component
@Order(1)
public class MDCFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(MDCFilter.class);


    private static final String CORRELATION_ID = "correlationId";
    private static final String USER_ID = "userId";
    private static final String SESSION_ID = "sessionId";
    private static final String REQUEST_ID = "requestId";
    private static final String COMPONENT = "component";
    private static final String MODULE = "module";
    private static final String TENANT_ID = "tenantId";

    private final BusinessLoggingConfig loggingConfig;
    private final JwtTokenProvider jwtTokenProvider;

    public MDCFilter(BusinessLoggingConfig loggingConfig, JwtTokenProvider jwtTokenProvider) {
        this.loggingConfig = loggingConfig;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {

        if(!loggingConfig.getMdc().isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Set correlation ID
            String correlationId = extractCorrelationId(request);
            MDC.put(CORRELATION_ID, correlationId);
            response.setHeader(loggingConfig.getCorrelationIdHeader(), correlationId);

            // Set request ID
            MDC.put(REQUEST_ID, UUID.randomUUID().toString());

            // Set component and module based on URL path
            setComponentAndModule(request.getRequestURI());

            // Extract and set user information
            if(loggingConfig.getMdc().isIncludeUserId()) {
                extractAndSetUserInfo(request);
            }

            // Set tenant ID if available
            if(loggingConfig.getMdc().isIncludeTenantId()) {
                String tenantId = request.getHeader("X - Tenant - ID");
                if(StringUtils.hasText(tenantId)) {
                    MDC.put(TENANT_ID, tenantId);
                }
            }

            // Log request start
            if(log.isDebugEnabled()) {
                log.debug("Request started - Method: {}, Path: {}, CorrelationId: {}",
                    request.getMethod(), request.getRequestURI(), correlationId);
            }

            filterChain.doFilter(request, response);

        } finally {
            // Clear MDC
            MDC.clear();
        }
    }

    private String extractCorrelationId(HttpServletRequest request) {
        String correlationId = request.getHeader(loggingConfig.getCorrelationIdHeader());
        if(!StringUtils.hasText(correlationId)) {
            correlationId = request.getHeader("X - Request - ID");
        }
        if(!StringUtils.hasText(correlationId)) {
            correlationId = UUID.randomUUID().toString();
        }
        return correlationId;
    }

    private void setComponentAndModule(String path) {
        if(path == null) {
            MDC.put(COMPONENT, "System");
            MDC.put(MODULE, "Core");
            return;
        }

        // Determine component based on path
        if(path.startsWith("/api/auth")) {
            MDC.put(COMPONENT, "Security");
            MDC.put(MODULE, "Authentication");
        } else if(path.startsWith("/api/flows")) {
            MDC.put(COMPONENT, "Integration");
            MDC.put(MODULE, "FlowEngine");
        } else if(path.startsWith("/api/adapters")) {
            MDC.put(COMPONENT, "Integration");
            MDC.put(MODULE, "AdapterFramework");
        } else if(path.startsWith("/api/transformations")) {
            MDC.put(COMPONENT, "Integration");
            MDC.put(MODULE, "TransformationEngine");
        } else if(path.startsWith("/api/messages")) {
            MDC.put(COMPONENT, "Integration");
            MDC.put(MODULE, "MessageProcessor");
        } else if(path.startsWith("/api/admin")) {
            MDC.put(COMPONENT, "Administration");
            MDC.put(MODULE, "AdminConsole");
        } else {
            MDC.put(COMPONENT, "Application");
            MDC.put(MODULE, "API");
        }
    }

    private void extractAndSetUserInfo(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if(StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            try {
                String token = authHeader.substring(7);
                if(jwtTokenProvider.validateToken(token)) {
                    String userId = jwtTokenProvider.getUserId(token);
                    MDC.put(USER_ID, userId);

                    if(loggingConfig.getMdc().isIncludeSessionId()) {
                        // Extract session ID from token or generate one
                        String sessionId = jwtTokenProvider.getClaimFromToken(token, "sessionId");
                        if(sessionId == null) {
                            sessionId = "SID-" + userId.hashCode();
                        }
                        MDC.put(SESSION_ID, sessionId);
                    }
                }
            } catch(Exception e) {
                log.debug("Failed to extract user info from token", e);
                MDC.put(USER_ID, "Guest");
            }
        } else {
            MDC.put(USER_ID, "Guest");
        }
    }
}
