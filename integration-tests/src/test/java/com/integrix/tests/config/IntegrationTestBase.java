package com.integrix.tests.config;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Base class for integration tests with TestContainers support
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@Import(TestContainersConfig.class)
public abstract class IntegrationTestBase {

    @Container
    protected static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:14 - alpine"))
            .withDatabaseName("integrixflowbridge")
            .withUsername("integrix")
            .withPassword("integrix")
            .withInitScript("db/test - init.sql");

    @Container
    protected static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp - kafka:7.5.0"))
            .withKraft();

    @Container
    protected static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7 - alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        // PostgreSQL properties
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        // Kafka properties
        registry.add("spring.kafka.bootstrap - servers", kafka::getBootstrapServers);

        // Redis properties
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", () -> redis.getMappedPort(6379));

        // Disable flyway for tests
        registry.add("spring.flyway.enabled", () -> false);
    }

    @BeforeAll
    static void beforeAll() {
        // Wait for containers to be fully started
        postgres.start();
        kafka.start();
        redis.start();
    }

    protected String getPostgresJdbcUrl() {
        return postgres.getJdbcUrl();
    }

    protected String getKafkaBootstrapServers() {
        return kafka.getBootstrapServers();
    }

    protected String getRedisHost() {
        return redis.getHost();
    }

    protected Integer getRedisPort() {
        return redis.getMappedPort(6379);
    }
}
