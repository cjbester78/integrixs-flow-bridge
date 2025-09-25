package com.integrixs.backend.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis configuration for distributed caching
 */
@Configuration
@EnableCaching
@ConditionalOnProperty(name = "spring.redis.enabled", havingValue = "true", matchIfMissing = false)
public class RedisConfig implements CachingConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(RedisConfig.class);

    @Value("${spring.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.redis.port:6379}")
    private int redisPort;

    @Value("${spring.redis.password:}")
    private String redisPassword;

    @Value("${spring.redis.database:0}")
    private int redisDatabase;

    @Value("${spring.redis.timeout:60000}")
    private long redisTimeout;

    @Value("${spring.redis.pool.max-active:8}")
    private int maxActive;

    @Value("${spring.redis.pool.max-idle:8}")
    private int maxIdle;

    @Value("${spring.redis.pool.min-idle:0}")
    private int minIdle;

    @Value("${spring.redis.pool.max-wait:-1}")
    private long maxWait;

    @Value("${spring.redis.cluster.enabled:false}")
    private boolean clusterEnabled;

    /**
     * Redis connection factory with connection pooling
     */
    @Bean
    public LettuceConnectionFactory redisConnectionFactory() {
        logger.info("Configuring Redis connection: {}: {}", redisHost, redisPort);

        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        redisConfig.setHostName(redisHost);
        redisConfig.setPort(redisPort);
        redisConfig.setDatabase(redisDatabase);

        if(!redisPassword.isEmpty()) {
            redisConfig.setPassword(redisPassword);
        }

        // Configure connection pool
        GenericObjectPoolConfig<io.lettuce.core.api.StatefulConnection<?, ?>> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(maxActive);
        poolConfig.setMaxIdle(maxIdle);
        poolConfig.setMinIdle(minIdle);
        poolConfig.setMaxWaitMillis(maxWait);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestWhileIdle(true);

        // Configure client options
        ClientOptions clientOptions;
        if(clusterEnabled) {
            ClusterTopologyRefreshOptions refreshOptions = ClusterTopologyRefreshOptions.builder()
                .enablePeriodicRefresh(Duration.ofMinutes(1))
                .enableAllAdaptiveRefreshTriggers()
                .build();

            clientOptions = ClusterClientOptions.builder()
                .topologyRefreshOptions(refreshOptions)
                .socketOptions(SocketOptions.builder()
                    .connectTimeout(Duration.ofMillis(redisTimeout))
                    .keepAlive(true)
                    .build())
                .build();
        } else {
            clientOptions = ClientOptions.builder()
                .socketOptions(SocketOptions.builder()
                    .connectTimeout(Duration.ofMillis(redisTimeout))
                    .keepAlive(true)
                    .build())
                .autoReconnect(true)
                .pingBeforeActivateConnection(true)
                .build();
        }

        LettucePoolingClientConfiguration lettuceConfig = LettucePoolingClientConfiguration.builder()
            .poolConfig(poolConfig)
            .clientOptions(clientOptions)
            .commandTimeout(Duration.ofMillis(redisTimeout))
            .build();

        return new LettuceConnectionFactory(redisConfig, lettuceConfig);
    }

    /**
     * Redis template for object serialization
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Configure Jackson serializer
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.activateDefaultTyping(
            BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object.class)
                .build(),
            ObjectMapper.DefaultTyping.NON_FINAL,
            JsonTypeInfo.As.PROPERTY
       );

        GenericJackson2JsonRedisSerializer jsonSerializer =
            new GenericJackson2JsonRedisSerializer(objectMapper);

        // Set serializers
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();

        logger.info("Redis template configured with Jackson serialization");
        return template;
    }

    /**
     * Cache manager for distributed caching
     */
    @Bean
    @Primary
    @Override
    public CacheManager cacheManager() {
        logger.info("Configuring Redis cache manager");

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(30))
            .serializeKeysWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer()))
            .disableCachingNullValues();

        // Configure specific caches with different TTLs
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // User permissions cache-15 minutes
        cacheConfigurations.put("userPermissions",
            defaultConfig.entryTtl(Duration.ofMinutes(15)));

        // Flow metadata cache-1 hour
        cacheConfigurations.put("flowMetadata",
            defaultConfig.entryTtl(Duration.ofHours(1)));

        // Adapter configurations-2 hours
        cacheConfigurations.put("adapterConfigs",
            defaultConfig.entryTtl(Duration.ofHours(2)));

        // Message structures-6 hours
        cacheConfigurations.put("messageStructures",
            defaultConfig.entryTtl(Duration.ofHours(6)));

        // Transformation mappings-1 hour
        cacheConfigurations.put("transformationMappings",
            defaultConfig.entryTtl(Duration.ofHours(1)));

        // Tenant configurations-30 minutes
        cacheConfigurations.put("tenantConfigs",
            defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // Job status-5 minutes
        cacheConfigurations.put("jobStatus",
            defaultConfig.entryTtl(Duration.ofMinutes(5)));

        // API rate limits-1 minute
        cacheConfigurations.put("rateLimits",
            defaultConfig.entryTtl(Duration.ofMinutes(1)));

        return RedisCacheManager.builder(redisConnectionFactory())
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(cacheConfigurations)
            .transactionAware()
            .build();
    }

    /**
     * Custom key generator for complex cache keys
     */
    @Bean
    @Override
    public KeyGenerator keyGenerator() {
        return new TenantAwareKeyGenerator();
    }

    /**
     * Key generator that includes tenant context
     */
    public static class TenantAwareKeyGenerator implements KeyGenerator {
        @Override
        public Object generate(Object target, Method method, Object... params) {
            StringBuilder sb = new StringBuilder();

            // Add tenant ID if available
            String tenantId = TenantContext.getCurrentTenant();
            if(tenantId != null) {
                sb.append(tenantId).append(":");
            }

            // Add class and method name
            sb.append(target.getClass().getSimpleName())
              .append(".")
              .append(method.getName())
              .append(":");

            // Add parameters
            if(params.length > 0) {
                for(int i = 0; i < params.length; i++) {
                    if(i > 0) sb.append(",");
                    if(params[i] != null) {
                        sb.append(params[i].toString());
                    } else {
                        sb.append("null");
                    }
                }
            }

            return sb.toString();
        }
    }

    /**
     * Redis health indicator
     */
    @Bean
    public RedisHealthIndicator redisHealthIndicator(RedisConnectionFactory connectionFactory) {
        return new RedisHealthIndicator(connectionFactory);
    }

    /**
     * Custom health indicator for Redis
     */
    public static class RedisHealthIndicator {
        private final RedisConnectionFactory connectionFactory;

        public RedisHealthIndicator(RedisConnectionFactory connectionFactory) {
            this.connectionFactory = connectionFactory;
        }

        public boolean isHealthy() {
            try {
                connectionFactory.getConnection().ping();
                return true;
            } catch(Exception e) {
                logger.error("Redis health check failed", e);
                return false;
            }
        }
    }
}
