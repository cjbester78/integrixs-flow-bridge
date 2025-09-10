package com.integrixs.testing.adapters;

/**
 * Base interface for all mock adapters
 */
public interface MockAdapter {
    
    /**
     * Reset the mock adapter to initial state
     */
    void reset();
    
    /**
     * Get the number of times this adapter was called
     */
    long getCallCount();
    
    /**
     * Get the number of errors encountered
     */
    long getErrorCount();
    
    /**
     * Get average response time in milliseconds
     */
    double getAverageResponseTime();
    
    /**
     * Verify the mock was called as expected
     */
    void verify();
}