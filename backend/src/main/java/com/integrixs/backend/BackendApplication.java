package com.integrixs.backend;

import com.integrixs.backend.config.BusinessLoggingConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {"com.integrixs.backend", "com.integrixs.data", "com.integrixs.monitoring", "com.integrixs.shared", "com.integrixs.adapters", "com.integrixs.engine"})
@EnableJpaRepositories(basePackages = {"com.integrixs.data.repository", "com.integrixs.monitoring.repository"})
@EntityScan(basePackages = {"com.integrixs.data.model", "com.integrixs.monitoring.model"})
@EnableAspectJAutoProxy
@EnableConfigurationProperties(BusinessLoggingConfig.class)
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
}
