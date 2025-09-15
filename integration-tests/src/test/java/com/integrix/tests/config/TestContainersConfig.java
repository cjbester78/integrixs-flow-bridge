package com.integrix.tests.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * TestContainers configuration for integration tests
 */
@TestConfiguration
@Profile("test")
public class TestContainersConfig {

    private static PostgreSQLContainer<?> postgres;
    private static KafkaContainer kafka;
    private static GenericContainer<?> redis;

    static {
        postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:14 - alpine"))
                .withDatabaseName("integrixflowbridge")
                .withUsername("integrix")
                .withPassword("integrix");
        postgres.start();

        kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp - kafka:7.5.0"))
                .withKraft();
        kafka.start();

        redis = new GenericContainer<>(DockerImageName.parse("redis:7 - alpine"))
                .withExposedPorts(6379);
        redis.start();
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.kafka.bootstrap - servers", kafka::getBootstrapServers);

        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    /**
     * PostgreSQL container for database testing
     */
    @Bean
    PostgreSQLContainer<?> postgreSQLContainer() {
        return postgres;
    }

    /**
     * Kafka container for message queue testing
     */
    @Bean
    KafkaContainer kafkaContainer() {
        return kafka;
    }

    /**
     * Redis container for caching
     */
    @Bean
    GenericContainer<?> redisContainer() {
        return redis;
    }

    /**
     * FTP server container
     */
    @Bean
    GenericContainer<?> ftpServerContainer() {
        GenericContainer<?> container = new GenericContainer<>(DockerImageName.parse("delfer/alpine - ftp - server"))
                .withEnv("USERS", "testuser|testpass|/ftp")
                .withEnv("ADDRESS", "localhost")
                .withExposedPorts(21, 21000, 21001, 21002, 21003, 21004, 21005, 21006, 21007, 21008, 21009);
        container.start();
        return container;
    }

    /**
     * SMTP server container for email testing
     */
    @Bean
    GenericContainer<?> smtpServerContainer() {
        GenericContainer<?> container = new GenericContainer<>(DockerImageName.parse("mailhog/mailhog:v1.0.1"))
                .withExposedPorts(1025, 8025); // SMTP port and Web UI
        container.start();
        return container;
    }

    /**
     * MinIO container for S3 compatible storage
     */
    @Bean
    GenericContainer<?> minioContainer() {
        GenericContainer<?> container = new GenericContainer<>(DockerImageName.parse("minio/minio:latest"))
                .withCommand("server", "/data", "--console - address", ":9001")
                .withExposedPorts(9000, 9001)
                .withEnv("MINIO_ROOT_USER", "minioadmin")
                .withEnv("MINIO_ROOT_PASSWORD", "minioadmin");
        container.start();
        return container;
    }
}
