package com.integrixs.testing.runners;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manager for embedded test servers
 */
public class EmbeddedServers {
    
    private final List<AutoCloseable> servers = new ArrayList<>();
    private final Map<String, Object> serverInstances = new ConcurrentHashMap<>();
    private final Map<String, Map<String, String>> connectionProperties = new ConcurrentHashMap<>();
    
    private EmbeddedServers() {
    }
    
    /**
     * Start all configured servers
     */
    public void start() {
        servers.forEach(server -> {
            try {
                if (server instanceof PostgreSQLContainer) {
                    PostgreSQLContainer<?> postgres = (PostgreSQLContainer<?>) server;
                    postgres.start();
                    
                    Map<String, String> props = new ConcurrentHashMap<>();
                    props.put("url", postgres.getJdbcUrl());
                    props.put("username", postgres.getUsername());
                    props.put("password", postgres.getPassword());
                    connectionProperties.put("postgresql", props);
                    
                } else if (server instanceof RabbitMQContainer) {
                    RabbitMQContainer rabbit = (RabbitMQContainer) server;
                    rabbit.start();
                    
                    Map<String, String> props = new ConcurrentHashMap<>();
                    props.put("host", rabbit.getHost());
                    props.put("port", String.valueOf(rabbit.getAmqpPort()));
                    props.put("username", rabbit.getAdminUsername());
                    props.put("password", rabbit.getAdminPassword());
                    connectionProperties.put("rabbitmq", props);
                    
                } else if (server instanceof GenericContainer) {
                    GenericContainer<?> container = (GenericContainer<?>) server;
                    container.start();
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to start server: " + server.getClass().getSimpleName(), e);
            }
        });
    }
    
    /**
     * Stop all servers
     */
    public void stop() {
        servers.forEach(server -> {
            try {
                server.close();
            } catch (Exception e) {
                // Log but don't throw
                e.printStackTrace();
            }
        });
    }
    
    /**
     * Get connection properties for a server
     */
    public Map<String, String> getConnectionProperties(String serverType) {
        return connectionProperties.get(serverType);
    }
    
    /**
     * Get server instance
     */
    @SuppressWarnings("unchecked")
    public <T> T getServer(String name, Class<T> type) {
        Object server = serverInstances.get(name);
        if (server != null && type.isAssignableFrom(server.getClass())) {
            return (T) server;
        }
        return null;
    }
    
    /**
     * Builder for embedded servers
     */
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final EmbeddedServers servers = new EmbeddedServers();
        
        /**
         * Add PostgreSQL server
         */
        public Builder withPostgreSQL() {
            PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:14"))
                .withDatabaseName("testdb")
                .withUsername("test")
                .withPassword("test");
            
            servers.servers.add(postgres);
            servers.serverInstances.put("postgresql", postgres);
            return this;
        }
        
        /**
         * Add RabbitMQ server
         */
        public Builder withRabbitMQ() {
            RabbitMQContainer rabbit = new RabbitMQContainer(DockerImageName.parse("rabbitmq:3.12-management"))
                .withExposedPorts(5672, 15672);
            
            servers.servers.add(rabbit);
            servers.serverInstances.put("rabbitmq", rabbit);
            return this;
        }
        
        /**
         * Add Redis server
         */
        public Builder withRedis() {
            GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
                .withExposedPorts(6379);
            
            servers.servers.add(redis);
            servers.serverInstances.put("redis", redis);
            return this;
        }
        
        /**
         * Add Kafka server
         */
        public Builder withKafka() {
            // Simplified Kafka setup for testing
            GenericContainer<?> kafka = new GenericContainer<>(DockerImageName.parse("confluentinc/cp-kafka:7.5.0"))
                .withExposedPorts(9092)
                .withEnv("KAFKA_BROKER_ID", "1")
                .withEnv("KAFKA_ZOOKEEPER_CONNECT", "zookeeper:2181")
                .withEnv("KAFKA_ADVERTISED_LISTENERS", "PLAINTEXT://localhost:9092")
                .withEnv("KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR", "1");
            
            servers.servers.add(kafka);
            servers.serverInstances.put("kafka", kafka);
            return this;
        }
        
        /**
         * Add ElasticSearch server
         */
        public Builder withElasticSearch() {
            GenericContainer<?> elastic = new GenericContainer<>(DockerImageName.parse("docker.elastic.co/elasticsearch/elasticsearch:8.11.0"))
                .withExposedPorts(9200, 9300)
                .withEnv("discovery.type", "single-node")
                .withEnv("xpack.security.enabled", "false");
            
            servers.servers.add(elastic);
            servers.serverInstances.put("elasticsearch", elastic);
            return this;
        }
        
        /**
         * Add custom container
         */
        public Builder withCustomContainer(String name, GenericContainer<?> container) {
            servers.servers.add(container);
            servers.serverInstances.put(name, container);
            return this;
        }
        
        /**
         * Build the embedded servers instance
         */
        public EmbeddedServers build() {
            return servers;
        }
    }
}