package com.integrixs.backend;

import com.integrixs.adapters.controller.HttpAdapterController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@Profile("test")
@ComponentScan(basePackages = {"com.integrixs.backend", "com.integrixs.data", "com.integrixs.monitoring", "com.integrixs.shared", "com.integrixs.adapters", "com.integrixs.engine"},
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = HttpAdapterController.class))
@EnableJpaRepositories(basePackages = {"com.integrixs.data.repository", "com.integrixs.monitoring.repository"})
@EntityScan(basePackages = {"com.integrixs.data.model", "com.integrixs.monitoring.model"})
public class TestBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestBackendApplication.class, args);
    }
}