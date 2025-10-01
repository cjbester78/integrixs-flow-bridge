package com.integrixs.data.sql.mapper;

import com.integrixs.data.model.FlowStructure;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * RowMapper for FlowStructure entity.
 */
public class FlowStructureRowMapper implements RowMapper<FlowStructure> {

    @Override
    public FlowStructure mapRow(ResultSet rs, int rowNum) throws SQLException {
        FlowStructure entity = new FlowStructure();

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
