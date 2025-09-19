package com.integrixs.backend.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * Filter to extract tenant information from requests
 */
@Component
@Order(1)
public class TenantFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(TenantFilter.class);

    private static final String TENANT_HEADER = "X - Tenant - ID";
    private static final String TENANT_PARAM = "tenantId";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {
        try {
            // Extract tenant ID from header
            String tenantIdStr = request.getHeader(TENANT_HEADER);

            // If not in header, check request parameter
            if(tenantIdStr == null || tenantIdStr.isEmpty()) {
                tenantIdStr = request.getParameter(TENANT_PARAM);
            }

            // If tenant ID found, set in context
            if(tenantIdStr != null && !tenantIdStr.isEmpty()) {
                try {
                    UUID tenantId = UUID.fromString(tenantIdStr);
                    TenantContext.setCurrentTenant(tenantId);
                    logger.debug("Set tenant context: {}", tenantId);
                } catch(IllegalArgumentException e) {
                    logger.warn("Invalid tenant ID format: {}", tenantIdStr);
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid tenant ID format");
                    return;
                }
            } else {
                // For some endpoints, tenant might be optional
                logger.debug("No tenant ID provided in request");
            }

            // Continue filter chain
            filterChain.doFilter(request, response);

        } finally {
            // Clear tenant context
            TenantContext.clear();
        }
    }
}
