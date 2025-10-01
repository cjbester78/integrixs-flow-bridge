package com.integrixs.backend;

import com.integrixs.backend.config.BusinessLoggingConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.FilterType;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@ComponentScan(basePackages = {"com.integrixs.backend", "com.integrixs.data", "com.integrixs.monitoring", "com.integrixs.shared", "com.integrixs.adapters", "com.integrixs.engine"})
@EnableAspectJAutoProxy
@EnableTransactionManagement
@EnableConfigurationProperties(BusinessLoggingConfig.class)
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
}
