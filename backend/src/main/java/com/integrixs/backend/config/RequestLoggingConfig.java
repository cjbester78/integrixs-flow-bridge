package com.integrixs.backend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.Collections;

@Configuration
public class RequestLoggingConfig {

    @Bean
    public RequestLoggingFilter requestLoggingFilter() {
        return new RequestLoggingFilter();
    }

    public static class RequestLoggingFilter extends OncePerRequestFilter {

        private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                      FilterChain filterChain) throws ServletException, IOException {

            // Skip logging for static resources
            String path = request.getRequestURI();
            if(path.contains("/static/") || path.contains("/assets/") ||
                path.endsWith(".js") || path.endsWith(".css") || path.endsWith(".png") ||
                path.endsWith(".ico") || path.endsWith(".svg")) {
                filterChain.doFilter(request, response);
                return;
            }

            // Check if this is a multipart request
            boolean isMultipart = request.getContentType() != null &&
                                request.getContentType().toLowerCase().startsWith("multipart/");

            HttpServletRequest requestToUse = request;
            HttpServletResponse responseToUse = response;

            // Only wrap non - multipart requests
            if(!isMultipart) {
                requestToUse = new ContentCachingRequestWrapper(request);
                responseToUse = new ContentCachingResponseWrapper(response);
            }

            logger.info("🔵 === INCOMING REQUEST === {} {}", request.getMethod(), request.getRequestURI());
            logger.info("🔵 Headers: {}", Collections.list(request.getHeaderNames()));
            logger.info("🔵 Content - Type: {}", request.getContentType());
            logger.info("🔵 User - Agent: {}", request.getHeader("User - Agent"));
            logger.info("🔵 Origin: {}", request.getHeader("Origin"));

            // Special logging for multipart requests
            if(isMultipart) {
                logger.info("🔵 === MULTIPART REQUEST DETECTED ===");
                logger.info("🔵 Content - Length: {}", request.getContentLength());
                logger.info("🔵 Character Encoding: {}", request.getCharacterEncoding());
                logger.info("🔵 Authorization Header Present: {}", request.getHeader("Authorization") != null);

                // Log all headers
                Collections.list(request.getHeaderNames()).forEach(headerName -> {
                    logger.info("🔵 Header {}: {}", headerName, request.getHeader(headerName));
                });
            }

            long startTime = System.currentTimeMillis();

            try {
                filterChain.doFilter(requestToUse, responseToUse);

                long duration = System.currentTimeMillis() - startTime;

                logger.info("🟢 === RESPONSE === {} {} -> Status: {}, Duration: {}ms",
                           request.getMethod(), request.getRequestURI(), response.getStatus(), duration);

                // Log request body for POST requests(only for non - multipart)
                if(!isMultipart && "POST".equals(request.getMethod()) && requestToUse instanceof ContentCachingRequestWrapper) {
                    ContentCachingRequestWrapper wrapper = (ContentCachingRequestWrapper) requestToUse;
                    if(wrapper.getContentAsByteArray().length > 0) {
                        String requestBody = new String(wrapper.getContentAsByteArray());
                        logger.info("🔵 Request Body: {}", requestBody);
                    }
                }

            } catch(Exception e) {
                long duration = System.currentTimeMillis() - startTime;
                logger.error("🔴 === ERROR === {} {} -> Error: {}, Duration: {}ms",
                            request.getMethod(), request.getRequestURI(), e.getMessage(), duration);
                throw e;
            } finally {
                if(responseToUse instanceof ContentCachingResponseWrapper) {
                    ((ContentCachingResponseWrapper) responseToUse).copyBodyToResponse();
                }
            }
        }
    }
}
