package com.integrixs.backend.service;

import org.springframework.stereotype.Service;

/**
 * Default implementation of ProcessEngineService
 * This replaces the Camunda-based implementation with a simple Spring-native approach
 */
@Service
public class DefaultProcessEngineService extends ProcessEngineService {
    
    // All functionality is inherited from the abstract ProcessEngineService
    // This provides a custom process engine implementation without external dependencies
    
}