package com.integrixs.data.sql.core;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.StringJoiner;

/**
 * Helper class for handling pagination and sorting in native SQL queries.
 */
public class SqlPaginationHelper {

    /**
     * Build ORDER BY clause from Spring Data Sort
     */
    public static String buildOrderByClause(Sort sort) {
        if (sort == null || sort.isUnsorted()) {
            return "";
        }

        StringJoiner orderBy = new StringJoiner(", ", " ORDER BY ", "");
        sort.forEach(order -> {
            String property = camelCaseToSnakeCase(order.getProperty());
            String direction = order.getDirection().name();
            orderBy.add(property + " " + direction);
        });

        return orderBy.toString();
    }

    /**
     * Build pagination clause (LIMIT and OFFSET)
     */
    public static String buildPaginationClause(Pageable pageable) {
        if (pageable == null || pageable.isUnpaged()) {
            return "";
        }

        long offset = pageable.getOffset();
        int pageSize = pageable.getPageSize();

        return String.format(" LIMIT %d OFFSET %d", pageSize, offset);
    }

    /**
     * Build complete paginated query
     */
    public static String buildPaginatedQuery(String baseQuery, Pageable pageable) {
        StringBuilder query = new StringBuilder(baseQuery);

        if (pageable != null && !pageable.isUnpaged()) {
            query.append(buildOrderByClause(pageable.getSort()));
            query.append(buildPaginationClause(pageable));
        }

        return query.toString();
    }

    /**
     * Build count query from base query
     */
    public static String buildCountQuery(String baseQuery) {
        // Remove ORDER BY clause if present
        String countQuery = baseQuery.toUpperCase().contains("ORDER BY")
            ? baseQuery.substring(0, baseQuery.toUpperCase().lastIndexOf("ORDER BY"))
            : baseQuery;

        // Replace SELECT ... FROM with SELECT COUNT(*) FROM
        int fromIndex = countQuery.toUpperCase().indexOf("FROM");
        if (fromIndex > 0) {
            return "SELECT COUNT(*) " + countQuery.substring(fromIndex);
        }

        // If no FROM found, wrap the entire query
        return "SELECT COUNT(*) FROM (" + baseQuery + ") AS count_query";
    }

    /**
     * Create Page object from results and total count
     */
    public static <T> Page<T> createPage(List<T> content, Pageable pageable, long total) {
        return new PageImpl<>(content, pageable, total);
    }

    /**
     * Convert camelCase to snake_case for database column names
     */
    public static String camelCaseToSnakeCase(String camelCase) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < camelCase.length(); i++) {
            char ch = camelCase.charAt(i);
            if (Character.isUpperCase(ch) && i > 0) {
                result.append('_');
            }
            result.append(Character.toLowerCase(ch));
        }

        return result.toString();
    }

    /**
     * Calculate offset from page number and size
     */
    public static long calculateOffset(int page, int size) {
        return (long) page * size;
    }

    /**
     * Build a dynamic WHERE clause from parameters
     */
    public static String buildWhereClause(String... conditions) {
        if (conditions == null || conditions.length == 0) {
            return "";
        }

        StringJoiner where = new StringJoiner(" AND ", " WHERE ", "");
        for (String condition : conditions) {
            if (condition != null && !condition.trim().isEmpty()) {
                where.add(condition);
            }
        }

        String result = where.toString();
        return result.equals(" WHERE ") ? "" : result;
    }
}