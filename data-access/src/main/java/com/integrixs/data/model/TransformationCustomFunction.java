package com.integrixs.data.model;

import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Entity representing custom transformation functions that can be used in visual flow and orchestration editors
 */
@Entity
@Table(name = "transformation_custom_functions")
public class TransformationCustomFunction {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "function_id", columnDefinition = "UUID")
    private UUID functionId;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "category", length = 50)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(name = "language", nullable = false, length = 20)
    private FunctionLanguage language;

    @Column(name = "function_signature", nullable = false, length = 500)
    private String functionSignature;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "parameters", columnDefinition = "json")
    private String parameters; // JSON representation of function parameters

    @Column(name = "function_body", nullable = false, columnDefinition = "TEXT")
    private String functionBody;

    @ElementCollection
    @CollectionTable(name = "function_dependencies", joinColumns = @JoinColumn(name = "function_id"))
    @Column(name = "dependency")
    private List<String> dependencies;

    @ElementCollection
    @CollectionTable(name = "function_test_cases", joinColumns = @JoinColumn(name = "function_id"))
    private List<TestCase> testCases;

    @Column(name = "is_safe", nullable = false)
    private boolean isSafe = false;

    @Column(name = "is_public", nullable = false)
    private boolean isPublic = true;

    @Column(name = "is_built_in", nullable = false)
    private boolean builtIn = false;

    public boolean isBuiltIn() {
        return builtIn;
    }

    public void setBuiltIn(boolean builtIn) {
        this.builtIn = builtIn;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "performance_class", nullable = false, length = 20)
    private PerformanceClass performanceClass = PerformanceClass.NORMAL;

    @Column(name = "version", nullable = false)
    private int version = 1;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum FunctionLanguage {
        JAVA,
        JAVASCRIPT,
        GROOVY,
        PYTHON
    }

    public enum PerformanceClass {
        FAST,
        NORMAL,
        SLOW
    }

    @Embeddable
    public static class TestCase {
        @Column(name = "test_name", length = 100)
        private String testName;

        @Column(name = "input_data", columnDefinition = "TEXT")
        private String inputData;

        @Column(name = "expected_output", columnDefinition = "TEXT")
        private String expectedOutput;

        @Column(name = "test_description", length = 500)
        private String testDescription;

        public String getTestName() {
            return testName;
        }

        public void setTestName(String testName) {
            this.testName = testName;
        }

        public String getInputData() {
            return inputData;
        }

        public void setInputData(String inputData) {
            this.inputData = inputData;
        }

        public String getExpectedOutput() {
            return expectedOutput;
        }

        public void setExpectedOutput(String expectedOutput) {
            this.expectedOutput = expectedOutput;
        }

        public String getTestDescription() {
            return testDescription;
        }

        public void setTestDescription(String testDescription) {
            this.testDescription = testDescription;
        }
    }

    // Default constructor
    public TransformationCustomFunction() {
    }

    public UUID getFunctionId() {
        return functionId;
    }

    public void setFunctionId(UUID functionId) {
        this.functionId = functionId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public FunctionLanguage getLanguage() {
        return language;
    }

    public void setLanguage(FunctionLanguage language) {
        this.language = language;
    }

    public String getFunctionSignature() {
        return functionSignature;
    }

    public void setFunctionSignature(String functionSignature) {
        this.functionSignature = functionSignature;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public String getFunctionBody() {
        return functionBody;
    }

    public void setFunctionBody(String functionBody) {
        this.functionBody = functionBody;
    }

    public List<String> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<String> dependencies) {
        this.dependencies = dependencies;
    }

    public List<TestCase> getTestCases() {
        return testCases;
    }

    public void setTestCases(List<TestCase> testCases) {
        this.testCases = testCases;
    }

    public boolean isIsSafe() {
        return isSafe;
    }

    public void setIsSafe(boolean isSafe) {
        this.isSafe = isSafe;
    }

    public boolean isIsPublic() {
        return isPublic;
    }

    public void setIsPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public PerformanceClass getPerformanceClass() {
        return performanceClass;
    }

    public void setPerformanceClass(PerformanceClass performanceClass) {
        this.performanceClass = performanceClass;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Builder
    public static TransformationCustomFunctionBuilder builder() {
        return new TransformationCustomFunctionBuilder();
    }

    public static class TransformationCustomFunctionBuilder {
        private UUID functionId;
        private String name;
        private String description;
        private String category;
        private FunctionLanguage language;
        private String functionSignature;
        private String parameters;
        private String functionBody;
        private List<String> dependencies;
        private List<TestCase> testCases;
        private boolean isSafe;
        private boolean isPublic;
        private boolean builtIn;
        private PerformanceClass performanceClass;
        private int version;
        private String createdBy;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private String testName;
        private String inputData;
        private String expectedOutput;
        private String testDescription;

        public TransformationCustomFunctionBuilder functionId(UUID functionId) {
            this.functionId = functionId;
            return this;
        }

        public TransformationCustomFunctionBuilder name(String name) {
            this.name = name;
            return this;
        }

        public TransformationCustomFunctionBuilder description(String description) {
            this.description = description;
            return this;
        }

        public TransformationCustomFunctionBuilder category(String category) {
            this.category = category;
            return this;
        }

        public TransformationCustomFunctionBuilder language(FunctionLanguage language) {
            this.language = language;
            return this;
        }

        public TransformationCustomFunctionBuilder functionSignature(String functionSignature) {
            this.functionSignature = functionSignature;
            return this;
        }

        public TransformationCustomFunctionBuilder parameters(String parameters) {
            this.parameters = parameters;
            return this;
        }

        public TransformationCustomFunctionBuilder functionBody(String functionBody) {
            this.functionBody = functionBody;
            return this;
        }

        public TransformationCustomFunctionBuilder dependencies(List<String> dependencies) {
            this.dependencies = dependencies;
            return this;
        }

        public TransformationCustomFunctionBuilder testCases(List<TestCase> testCases) {
            this.testCases = testCases;
            return this;
        }

        public TransformationCustomFunctionBuilder isSafe(boolean isSafe) {
            this.isSafe = isSafe;
            return this;
        }

        public TransformationCustomFunctionBuilder isPublic(boolean isPublic) {
            this.isPublic = isPublic;
            return this;
        }

        public TransformationCustomFunctionBuilder builtIn(boolean builtIn) {
            this.builtIn = builtIn;
            return this;
        }

        public TransformationCustomFunctionBuilder performanceClass(PerformanceClass performanceClass) {
            this.performanceClass = performanceClass;
            return this;
        }

        public TransformationCustomFunctionBuilder version(int version) {
            this.version = version;
            return this;
        }

        public TransformationCustomFunctionBuilder createdBy(String createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public TransformationCustomFunctionBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public TransformationCustomFunctionBuilder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public TransformationCustomFunctionBuilder testName(String testName) {
            this.testName = testName;
            return this;
        }

        public TransformationCustomFunctionBuilder inputData(String inputData) {
            this.inputData = inputData;
            return this;
        }

        public TransformationCustomFunctionBuilder expectedOutput(String expectedOutput) {
            this.expectedOutput = expectedOutput;
            return this;
        }

        public TransformationCustomFunctionBuilder testDescription(String testDescription) {
            this.testDescription = testDescription;
            return this;
        }

        public TransformationCustomFunction build() {
            TransformationCustomFunction instance = new TransformationCustomFunction();
            instance.setFunctionId(this.functionId);
            instance.setName(this.name);
            instance.setDescription(this.description);
            instance.setCategory(this.category);
            instance.setLanguage(this.language);
            instance.setFunctionSignature(this.functionSignature);
            instance.setParameters(this.parameters);
            instance.setFunctionBody(this.functionBody);
            instance.setDependencies(this.dependencies);
            instance.setTestCases(this.testCases);
            instance.setIsSafe(this.isSafe);
            instance.setIsPublic(this.isPublic);
            instance.setBuiltIn(this.builtIn);
            instance.setPerformanceClass(this.performanceClass);
            instance.setVersion(this.version);
            instance.setCreatedBy(this.createdBy);
            instance.setCreatedAt(this.createdAt);
            instance.setUpdatedAt(this.updatedAt);
            // Test fields are part of TestCase inner class, not TransformationCustomFunction
            return instance;
        }
    }
}
