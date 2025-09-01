package com.integrixs.backend.dto.dashboard.trend;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Regression analysis results.
 */
@Data
public class RegressionAnalysis {
    private String dependentVariable;
    private List<String> independentVariables;
    private Map<String, Double> coefficients;
    private double rSquared;
    private Map<String, Double> pValues;
    private List<Double> predictedValues;
}