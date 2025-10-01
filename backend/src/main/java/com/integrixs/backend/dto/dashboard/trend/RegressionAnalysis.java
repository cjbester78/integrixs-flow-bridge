package com.integrixs.backend.dto.dashboard.trend;

import java.util.List;
import java.util.Map;

/**
 * Regression analysis results.
 */
public class RegressionAnalysis {
    private String dependentVariable;
    private List<String> independentVariables;
    private Map<String, Double> coefficients;
    private double rSquared;
    private Map<String, Double> pValues;
    private List<Double> predictedValues;

    // Default constructor
    public RegressionAnalysis() {
    }

    public String getDependentVariable() {
        return dependentVariable;
    }

    public void setDependentVariable(String dependentVariable) {
        this.dependentVariable = dependentVariable;
    }

    public List<String> getIndependentVariables() {
        return independentVariables;
    }

    public void setIndependentVariables(List<String> independentVariables) {
        this.independentVariables = independentVariables;
    }

    public double getRSquared() {
        return rSquared;
    }

    public void setRSquared(double rSquared) {
        this.rSquared = rSquared;
    }

    public List<Double> getPredictedValues() {
        return predictedValues;
    }

    public void setPredictedValues(List<Double> predictedValues) {
        this.predictedValues = predictedValues;
    }

    public Map<String, Double> getCoefficients() {
        return coefficients;
    }

    public void setCoefficients(Map<String, Double> coefficients) {
        this.coefficients = coefficients;
    }

    public Map<String, Double> getPValues() {
        return pValues;
    }

    public void setPValues(Map<String, Double> pValues) {
        this.pValues = pValues;
    }
}
