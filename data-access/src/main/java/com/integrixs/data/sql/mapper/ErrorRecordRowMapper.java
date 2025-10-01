package com.integrixs.data.sql.mapper;

import com.integrixs.data.model.ErrorRecord;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * RowMapper for ErrorRecord entity.
 */
public class ErrorRecordRowMapper implements RowMapper<ErrorRecord> {

    @Override
    public ErrorRecord mapRow(ResultSet rs, int rowNum) throws SQLException {
        ErrorRecord entity = new ErrorRecord();

        // Basic fields
        entity.setId((UUID) rs.getObject("id"));

        // Timestamps
        if (rs.getTimestamp("created_at") != null) {
            entity.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            entity.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }

        return entity;
    }
}
