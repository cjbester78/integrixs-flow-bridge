package com.integrixs.data.sql.repository;

import com.integrixs.data.model.TransformationCustomFunction;
import com.integrixs.data.model.TransformationCustomFunction.*;
import com.integrixs.data.sql.core.BaseSqlRepository;
import com.integrixs.data.sql.core.ResultSetMapper;
import com.integrixs.data.sql.core.SqlQueryExecutor;
import com.integrixs.data.sql.core.SqlPaginationHelper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

/**
 * SQL implementation of TransformationCustomFunctionRepository using native queries.
 * Handles @ElementCollection for dependencies and test cases, plus JSON column.
 */
@Repository("transformationCustomFunctionSqlRepository")
public class TransformationCustomFunctionSqlRepository extends BaseSqlRepository<TransformationCustomFunction, UUID> {

    private static final String TABLE_NAME = "transformation_custom_functions";
    private static final String ID_COLUMN = "function_id";
    private static final String DEPENDENCIES_TABLE = "function_dependencies";
    private static final String TEST_CASES_TABLE = "function_test_cases";

    /**
     * Row mapper for TransformationCustomFunction entity
     */
    private static final RowMapper<TransformationCustomFunction> FUNCTION_ROW_MAPPER = new RowMapper<TransformationCustomFunction>() {
        @Override
        public TransformationCustomFunction mapRow(ResultSet rs, int rowNum) throws SQLException {
            TransformationCustomFunction function = new TransformationCustomFunction();
            function.setFunctionId(ResultSetMapper.getUUID(rs, "function_id"));
            function.setName(ResultSetMapper.getString(rs, "name"));
            function.setDescription(ResultSetMapper.getString(rs, "description"));
            function.setCategory(ResultSetMapper.getString(rs, "category"));

            String languageStr = ResultSetMapper.getString(rs, "language");
            if (languageStr != null) {
                function.setLanguage(FunctionLanguage.valueOf(languageStr));
            }

            function.setFunctionSignature(ResultSetMapper.getString(rs, "function_signature"));
            function.setParameters(ResultSetMapper.getString(rs, "parameters"));
            function.setFunctionBody(ResultSetMapper.getString(rs, "function_body"));
            function.setIsSafe(rs.getBoolean("is_safe"));
            function.setIsPublic(rs.getBoolean("is_public"));
            function.setBuiltIn(rs.getBoolean("is_built_in"));

            String performanceClassStr = ResultSetMapper.getString(rs, "performance_class");
            if (performanceClassStr != null) {
                function.setPerformanceClass(PerformanceClass.valueOf(performanceClassStr));
            }

            function.setVersion(rs.getInt("version"));
            function.setCreatedBy(ResultSetMapper.getString(rs, "created_by"));
            function.setCreatedAt(ResultSetMapper.getLocalDateTime(rs, "created_at"));
            function.setUpdatedAt(ResultSetMapper.getLocalDateTime(rs, "updated_at"));

            return function;
        }
    };

    /**
     * Row mapper for TestCase
     */
    private static final RowMapper<TestCase> TEST_CASE_ROW_MAPPER = new RowMapper<TestCase>() {
        @Override
        public TestCase mapRow(ResultSet rs, int rowNum) throws SQLException {
            TestCase testCase = new TestCase();
            testCase.setTestName(ResultSetMapper.getString(rs, "test_name"));
            testCase.setInputData(ResultSetMapper.getString(rs, "input_data"));
            testCase.setExpectedOutput(ResultSetMapper.getString(rs, "expected_output"));
            testCase.setTestDescription(ResultSetMapper.getString(rs, "test_description"));
            return testCase;
        }
    };

    public TransformationCustomFunctionSqlRepository(SqlQueryExecutor sqlQueryExecutor) {
        super(sqlQueryExecutor, TABLE_NAME, ID_COLUMN, FUNCTION_ROW_MAPPER);
    }

    @Override
    public Optional<TransformationCustomFunction> findById(UUID id) {
        Optional<TransformationCustomFunction> functionOpt = super.findById(id);
        if (functionOpt.isPresent()) {
            loadCollections(functionOpt.get());
        }
        return functionOpt;
    }

    @Override
    public List<TransformationCustomFunction> findAll() {
        List<TransformationCustomFunction> functions = super.findAll();
        for (TransformationCustomFunction function : functions) {
            loadCollections(function);
        }
        return functions;
    }

    public Page<TransformationCustomFunction> findAll(Pageable pageable) {
        String baseQuery = "SELECT * FROM " + TABLE_NAME;
        String countQuery = "SELECT COUNT(*) FROM " + TABLE_NAME;

        long total = sqlQueryExecutor.count(countQuery);
        String paginatedQuery = baseQuery + SqlPaginationHelper.buildOrderByClause(pageable.getSort()) + SqlPaginationHelper.buildPaginationClause(pageable);
        List<TransformationCustomFunction> functions = sqlQueryExecutor.queryForList(paginatedQuery, FUNCTION_ROW_MAPPER);

        for (TransformationCustomFunction function : functions) {
            loadCollections(function);
        }

        return new PageImpl<>(functions, pageable, total);
    }

    public Optional<TransformationCustomFunction> findByName(String name) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE name = ?";
        List<TransformationCustomFunction> results = sqlQueryExecutor.queryForList(sql, FUNCTION_ROW_MAPPER, name);

        if (!results.isEmpty()) {
            TransformationCustomFunction function = results.get(0);
            loadCollections(function);
            return Optional.of(function);
        }
        return Optional.empty();
    }

    public List<TransformationCustomFunction> findByCategory(String category) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE category = ?";
        List<TransformationCustomFunction> functions = sqlQueryExecutor.queryForList(sql, FUNCTION_ROW_MAPPER, category);
        for (TransformationCustomFunction function : functions) {
            loadCollections(function);
        }
        return functions;
    }

    public List<TransformationCustomFunction> findByLanguage(FunctionLanguage language) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE language = ?";
        List<TransformationCustomFunction> functions = sqlQueryExecutor.queryForList(sql, FUNCTION_ROW_MAPPER, language.toString());
        for (TransformationCustomFunction function : functions) {
            loadCollections(function);
        }
        return functions;
    }

    public List<TransformationCustomFunction> findByIsPublicTrue() {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE is_public = true";
        List<TransformationCustomFunction> functions = sqlQueryExecutor.queryForList(sql, FUNCTION_ROW_MAPPER);
        for (TransformationCustomFunction function : functions) {
            loadCollections(function);
        }
        return functions;
    }

    public List<TransformationCustomFunction> findByBuiltInTrue() {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE is_built_in = true ORDER BY category, name";
        List<TransformationCustomFunction> functions = sqlQueryExecutor.queryForList(sql, FUNCTION_ROW_MAPPER);
        for (TransformationCustomFunction function : functions) {
            loadCollections(function);
        }
        return functions;
    }

    public List<TransformationCustomFunction> searchByNameOrDescription(String searchTerm) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE " +
                     "LOWER(name) LIKE LOWER(?) OR LOWER(description) LIKE LOWER(?)";
        String pattern = "%" + searchTerm + "%";
        List<TransformationCustomFunction> functions = sqlQueryExecutor.queryForList(sql, FUNCTION_ROW_MAPPER, pattern, pattern);
        for (TransformationCustomFunction function : functions) {
            loadCollections(function);
        }
        return functions;
    }

    @Override
    public TransformationCustomFunction save(TransformationCustomFunction function) {
        if (function.getFunctionId() == null) {
            function.setFunctionId(generateId());
        }

        boolean exists = existsById(function.getFunctionId());

        if (!exists) {
            function = insert(function);
        } else {
            function = update(function);
        }

        // Save collections
        saveCollections(function);

        return function;
    }

    private TransformationCustomFunction insert(TransformationCustomFunction function) {
        String sql = "INSERT INTO " + TABLE_NAME + " (" +
                     "function_id, name, description, category, language, function_signature, " +
                     "parameters, function_body, is_safe, is_public, is_built_in, " +
                     "performance_class, version, created_by, created_at, updated_at" +
                     ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        if (function.getCreatedAt() == null) {
            function.setCreatedAt(LocalDateTime.now());
        }

        sqlQueryExecutor.update(sql,
            function.getFunctionId(),
            function.getName(),
            function.getDescription(),
            function.getCategory(),
            function.getLanguage() != null ? function.getLanguage().toString() : null,
            function.getFunctionSignature(),
            function.getParameters(),
            function.getFunctionBody(),
            function.isIsSafe(),
            function.isIsPublic(),
            function.isBuiltIn(),
            function.getPerformanceClass() != null ? function.getPerformanceClass().toString() : "NORMAL",
            function.getVersion(),
            function.getCreatedBy(),
            ResultSetMapper.toTimestamp(function.getCreatedAt()),
            ResultSetMapper.toTimestamp(function.getUpdatedAt())
        );

        return function;
    }

    @Override
    public TransformationCustomFunction update(TransformationCustomFunction function) {
        String sql = "UPDATE " + TABLE_NAME + " SET " +
                     "name = ?, description = ?, category = ?, language = ?, function_signature = ?, " +
                     "parameters = ?, function_body = ?, is_safe = ?, is_public = ?, is_built_in = ?, " +
                     "performance_class = ?, version = ?, updated_at = ? " +
                     "WHERE " + ID_COLUMN + " = ?";

        function.setUpdatedAt(LocalDateTime.now());

        sqlQueryExecutor.update(sql,
            function.getName(),
            function.getDescription(),
            function.getCategory(),
            function.getLanguage() != null ? function.getLanguage().toString() : null,
            function.getFunctionSignature(),
            function.getParameters(),
            function.getFunctionBody(),
            function.isIsSafe(),
            function.isIsPublic(),
            function.isBuiltIn(),
            function.getPerformanceClass() != null ? function.getPerformanceClass().toString() : "NORMAL",
            function.getVersion(),
            ResultSetMapper.toTimestamp(function.getUpdatedAt()),
            function.getFunctionId()
        );

        return function;
    }

    @Override
    public void deleteById(UUID id) {
        // Delete collections first (foreign key constraints)
        deleteCollections(id);

        // Then delete the function
        super.deleteById(id);
    }

    /**
     * Load all collections for a function
     */
    private void loadCollections(TransformationCustomFunction function) {
        loadDependencies(function);
        loadTestCases(function);
    }

    /**
     * Load dependencies
     */
    private void loadDependencies(TransformationCustomFunction function) {
        String sql = "SELECT dependency FROM " + DEPENDENCIES_TABLE + " WHERE function_id = ?";
        List<String> dependencies = sqlQueryExecutor.queryForList(sql,
            (rs, rowNum) -> rs.getString("dependency"),
            function.getFunctionId());
        function.setDependencies(dependencies);
    }

    /**
     * Load test cases
     */
    private void loadTestCases(TransformationCustomFunction function) {
        String sql = "SELECT test_name, input_data, expected_output, test_description FROM " +
                     TEST_CASES_TABLE + " WHERE function_id = ?";
        List<TestCase> testCases = sqlQueryExecutor.queryForList(sql, TEST_CASE_ROW_MAPPER, function.getFunctionId());
        function.setTestCases(testCases);
    }

    /**
     * Save all collections for a function
     */
    private void saveCollections(TransformationCustomFunction function) {
        deleteCollections(function.getFunctionId());
        saveDependencies(function);
        saveTestCases(function);
    }

    /**
     * Save dependencies
     */
    private void saveDependencies(TransformationCustomFunction function) {
        if (function.getDependencies() != null && !function.getDependencies().isEmpty()) {
            String sql = "INSERT INTO " + DEPENDENCIES_TABLE + " (function_id, dependency) VALUES (?, ?)";
            for (String dependency : function.getDependencies()) {
                sqlQueryExecutor.update(sql, function.getFunctionId(), dependency);
            }
        }
    }

    /**
     * Save test cases
     */
    private void saveTestCases(TransformationCustomFunction function) {
        if (function.getTestCases() != null && !function.getTestCases().isEmpty()) {
            String sql = "INSERT INTO " + TEST_CASES_TABLE + " (" +
                        "function_id, test_name, input_data, expected_output, test_description" +
                        ") VALUES (?, ?, ?, ?, ?)";

            for (TestCase testCase : function.getTestCases()) {
                sqlQueryExecutor.update(sql,
                    function.getFunctionId(),
                    testCase.getTestName(),
                    testCase.getInputData(),
                    testCase.getExpectedOutput(),
                    testCase.getTestDescription()
                );
            }
        }
    }

    /**
     * Delete all collections for a function
     */
    private void deleteCollections(UUID functionId) {
        sqlQueryExecutor.update("DELETE FROM " + DEPENDENCIES_TABLE + " WHERE function_id = ?", functionId);
        sqlQueryExecutor.update("DELETE FROM " + TEST_CASES_TABLE + " WHERE function_id = ?", functionId);
    }

    public boolean existsByName(String name) {
        String sql = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE name = ?";
        long count = sqlQueryExecutor.count(sql, name);
        return count > 0;
    }

    public List<TransformationCustomFunction> findByCategoryAndIsPublicTrue(String category) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE category = ? AND is_public = true";
        List<TransformationCustomFunction> functions = sqlQueryExecutor.queryForList(sql, FUNCTION_ROW_MAPPER, category);
        for (TransformationCustomFunction function : functions) {
            loadCollections(function);
        }
        return functions;
    }

    public int incrementVersion(UUID functionId) {
        String sql = "UPDATE " + TABLE_NAME + " SET version = version + 1, updated_at = ? WHERE function_id = ?";
        return sqlQueryExecutor.update(sql, ResultSetMapper.toTimestamp(LocalDateTime.now()), functionId);
    }

    public boolean existsByNameAndFunctionIdNot(String name, UUID functionId) {
        String sql = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE name = ? AND function_id != ?";
        long count = sqlQueryExecutor.count(sql, name, functionId);
        return count > 0;
    }

    public List<TransformationCustomFunction> findByPerformanceClass(PerformanceClass performanceClass) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE performance_class = ?";
        List<TransformationCustomFunction> functions = sqlQueryExecutor.queryForList(sql, FUNCTION_ROW_MAPPER, performanceClass.toString());
        for (TransformationCustomFunction function : functions) {
            loadCollections(function);
        }
        return functions;
    }

    public List<TransformationCustomFunction> findByCreatedBy(String createdBy) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE created_by = ?";
        List<TransformationCustomFunction> functions = sqlQueryExecutor.queryForList(sql, FUNCTION_ROW_MAPPER, createdBy);
        for (TransformationCustomFunction function : functions) {
            loadCollections(function);
        }
        return functions;
    }

    public List<TransformationCustomFunction> findByBuiltInFalse() {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE is_built_in = false";
        List<TransformationCustomFunction> functions = sqlQueryExecutor.queryForList(sql, FUNCTION_ROW_MAPPER);
        for (TransformationCustomFunction function : functions) {
            loadCollections(function);
        }
        return functions;
    }

    public Page<TransformationCustomFunction> findByBuiltInFalse(Pageable pageable) {
        String baseQuery = "SELECT * FROM " + TABLE_NAME + " WHERE is_built_in = false";
        String countQuery = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE is_built_in = false";

        long total = sqlQueryExecutor.count(countQuery);
        String paginatedQuery = baseQuery + SqlPaginationHelper.buildOrderByClause(pageable.getSort()) + SqlPaginationHelper.buildPaginationClause(pageable);
        List<TransformationCustomFunction> functions = sqlQueryExecutor.queryForList(paginatedQuery, FUNCTION_ROW_MAPPER);

        for (TransformationCustomFunction function : functions) {
            loadCollections(function);
        }

        return new PageImpl<>(functions, pageable, total);
    }

    public List<TransformationCustomFunction> findByBuiltInTrueAndCategory(String category) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE is_built_in = true AND category = ?";
        List<TransformationCustomFunction> functions = sqlQueryExecutor.queryForList(sql, FUNCTION_ROW_MAPPER, category);
        for (TransformationCustomFunction function : functions) {
            loadCollections(function);
        }
        return functions;
    }
}