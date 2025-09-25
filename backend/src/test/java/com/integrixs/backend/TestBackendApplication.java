package com.integrixs.backend;

import com.integrixs.adapters.controller.HttpAdapterController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Profile;

@SpringBootApplication
@Profile("test")
@ComponentScan(basePackages = {"com.integrixs.backend", "com.integrixs.data", "com.integrixs.monitoring", "com.integrixs.shared", "com.integrixs.adapters", "com.integrixs.engine"},
    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = HttpAdapterController.class))
public class TestBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestBackendApplication.class, args);
    }
}
