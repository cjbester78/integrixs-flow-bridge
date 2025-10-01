package com.integrixs.data.sql.mapper;

import com.integrixs.data.model.FieldMapping;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * RowMapper for FieldMapping entity.
 */
public class FieldMappingRowMapper implements RowMapper<FieldMapping> {

    @Override
    public FieldMapping mapRow(ResultSet rs, int rowNum) throws SQLException {
        FieldMapping entity = new FieldMapping();

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
