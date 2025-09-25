package com.integrixs.data.sql.core;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Utility class for mapping ResultSet to domain objects.
 * Provides common helper methods for SQL result mapping.
 */
public abstract class ResultSetMapper {

    /**
     * Get UUID from ResultSet, handling null values
     */
    public static UUID getUUID(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return value != null ? UUID.fromString(value) : null;
    }

    /**
     * Get String from ResultSet, handling null values
     */
    public static String getString(ResultSet rs, String columnName) throws SQLException {
        return rs.getString(columnName);
    }

    /**
     * Get Integer from ResultSet, handling null values
     */
    public static Integer getInteger(ResultSet rs, String columnName) throws SQLException {
        int value = rs.getInt(columnName);
        return rs.wasNull() ? null : value;
    }

    /**
     * Get Long from ResultSet, handling null values
     */
    public static Long getLong(ResultSet rs, String columnName) throws SQLException {
        long value = rs.getLong(columnName);
        return rs.wasNull() ? null : value;
    }

    /**
     * Get Boolean from ResultSet, handling null values
     */
    public static Boolean getBoolean(ResultSet rs, String columnName) throws SQLException {
        boolean value = rs.getBoolean(columnName);
        return rs.wasNull() ? null : value;
    }

    /**
     * Get Double from ResultSet, handling null values
     */
    public static Double getDouble(ResultSet rs, String columnName) throws SQLException {
        double value = rs.getDouble(columnName);
        return rs.wasNull() ? null : value;
    }

    /**
     * Get LocalDateTime from ResultSet, handling null values
     */
    public static LocalDateTime getLocalDateTime(ResultSet rs, String columnName) throws SQLException {
        Timestamp timestamp = rs.getTimestamp(columnName);
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }

    /**
     * Get byte array from ResultSet, handling null values
     */
    public static byte[] getBytes(ResultSet rs, String columnName) throws SQLException {
        return rs.getBytes(columnName);
    }

    /**
     * Convert LocalDateTime to Timestamp for SQL operations
     */
    public static Timestamp toTimestamp(LocalDateTime dateTime) {
        return dateTime != null ? Timestamp.valueOf(dateTime) : null;
    }

    /**
     * Create a simple RowMapper using lambda
     */
    public static <T> RowMapper<T> createRowMapper(RowMapperFunction<T> mapperFunction) {
        return (rs, rowNum) -> mapperFunction.map(rs);
    }

    /**
     * Functional interface for row mapping
     */
    @FunctionalInterface
    public interface RowMapperFunction<T> {
        T map(ResultSet rs) throws SQLException;
    }
}