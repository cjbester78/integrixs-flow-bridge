# API Gateway

A comprehensive API Gateway built with Spring Cloud Gateway providing routing, authentication, rate limiting, and monitoring capabilities.

## Features

### 1. **Intelligent Routing**
- Dynamic route configuration
- Path-based routing
- Load balancing
- Service discovery integration (optional)

### 2. **Security**
- JWT authentication
- API key validation
- Header sanitization
- CORS configuration

### 3. **Rate Limiting**
- Redis-based distributed rate limiting
- Multiple strategies (user, IP, API key)
- Configurable limits per endpoint
- Standard rate limit headers

### 4. **Resilience**
- Circuit breaker with Resilience4j
- Retry mechanisms
- Fallback handlers
- Timeout configuration

### 5. **Monitoring & Observability**
- Request/response logging
- Distributed tracing with request IDs
- Prometheus metrics
- Health checks

### 6. **Performance**
- Response caching
- Request/response compression
- Connection pooling
- HTTP/2 support

## Architecture

```
┌─────────────────┐
│   Client Apps   │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│   API Gateway   │
│  (Port: 8090)   │
├─────────────────┤
│ • Auth Filter   │
│ • Rate Limiter  │
│ • Circuit Break │
│ • Logging       │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│  Backend APIs   │
│  (Port: 8080)   │
└─────────────────┘
```

## Configuration

### Environment Variables

```bash
# Backend service URL
BACKEND_URL=http://localhost:8080

# Frontend URL (if serving frontend)
FRONTEND_URL=http://localhost:3000

# Redis configuration
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# JWT configuration
JWT_SECRET=your-secret-key
JWT_EXPIRATION=3600000

# Rate limiting defaults
RATE_LIMIT_DEFAULT=100
RATE_LIMIT_BURST=200
```

### Route Configuration

Routes can be configured in `application.yml` or dynamically via the management API.

#### Static Route Example:
```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: backend-api
          uri: http://localhost:8080
          predicates:
            - Path=/api/**
          filters:
            - StripPrefix=0
            - RequestRateLimiter
```

#### Dynamic Route Example:
```bash
curl -X POST http://localhost:8090/gateway/admin/routes \
  -H "Content-Type: application/json" \
  -d '{
    "id": "new-service",
    "path": "/api/new/**",
    "uri": "http://new-service:8080",
    "filters": ["StripPrefix=2", "Authentication=true"]
  }'
```

## Filters

### Global Filters
1. **LoggingGatewayFilter** - Logs all requests/responses
2. **HeaderSanitizationFilter** - Removes/sanitizes headers
3. **MetricsFilter** - Collects metrics

### Route-Specific Filters
1. **AuthenticationGatewayFilter** - JWT validation
2. **RequestRateLimiter** - Rate limiting
3. **CircuitBreaker** - Fault tolerance
4. **Retry** - Automatic retry

## API Endpoints

### Gateway Management
- `GET /gateway/admin/routes` - List all routes
- `POST /gateway/admin/routes` - Add new route
- `PUT /gateway/admin/routes/{id}` - Update route
- `DELETE /gateway/admin/routes/{id}` - Delete route
- `POST /gateway/admin/routes/refresh` - Refresh routes

### Monitoring
- `GET /actuator/health` - Health check
- `GET /actuator/metrics` - Metrics
- `GET /actuator/gateway/routes` - Route details

## Rate Limiting

The gateway implements token bucket algorithm for rate limiting:

```java
// Default: 100 requests per second, burst of 200
@Bean
public RedisRateLimiter defaultRateLimiter() {
    return new RedisRateLimiter(100, 200, 1);
}
```

Rate limit headers in response:
- `X-RateLimit-Limit` - Request limit
- `X-RateLimit-Remaining` - Remaining requests
- `X-RateLimit-Reset` - Reset time

## Circuit Breaker

Circuit breaker configuration:
- Failure rate threshold: 50%
- Slow call threshold: 5 seconds
- Wait duration in open state: 30 seconds
- Sliding window: 10 requests

## Security

### JWT Authentication
- Bearer token validation
- User info extraction
- Role-based access control

### CORS Configuration
- Configurable allowed origins
- Support for credentials
- Exposed headers configuration

## Running the Gateway

### Development
```bash
cd api-gateway
mvn spring-boot:run
```

### Docker
```bash
docker build -t integrixs-gateway .
docker run -p 8090:8090 \
  -e BACKEND_URL=http://backend:8080 \
  -e REDIS_HOST=redis \
  integrixs-gateway
```

### Production Considerations

1. **High Availability**
   - Deploy multiple gateway instances
   - Use load balancer in front
   - Configure session affinity if needed

2. **Performance Tuning**
   - Adjust connection pool sizes
   - Configure appropriate timeouts
   - Enable HTTP/2

3. **Security Hardening**
   - Use proper JWT secrets
   - Enable TLS/SSL
   - Configure firewall rules
   - Regular security updates

4. **Monitoring**
   - Set up Prometheus/Grafana
   - Configure alerting
   - Enable distributed tracing

## Troubleshooting

### Common Issues

1. **Rate Limit Errors**
   - Check Redis connection
   - Verify rate limit configuration
   - Monitor rate limit metrics

2. **Circuit Breaker Open**
   - Check backend service health
   - Review circuit breaker thresholds
   - Monitor error rates

3. **Authentication Failures**
   - Verify JWT secret matches backend
   - Check token expiration
   - Review excluded paths

### Debug Logging

Enable debug logging:
```yaml
logging:
  level:
    com.integrixs.gateway: DEBUG
    org.springframework.cloud.gateway: DEBUG
```

## Integration with Backend

The gateway automatically adds headers for the backend:
- `X-Request-ID` - Unique request identifier
- `X-Auth-Username` - Authenticated username
- `X-Auth-Roles` - User roles
- `X-Auth-Tenant` - Tenant ID

## Load Testing

Example using Apache Bench:
```bash
ab -n 1000 -c 10 -H "Authorization: Bearer <token>" \
   http://localhost:8090/api/flows
```

## Future Enhancements

1. **Service Mesh Integration**
   - Istio compatibility
   - Envoy proxy support

2. **Advanced Features**
   - Request/response transformation
   - Protocol translation
   - GraphQL gateway

3. **Enhanced Security**
   - OAuth2/OIDC support
   - Mutual TLS
   - WAF integration