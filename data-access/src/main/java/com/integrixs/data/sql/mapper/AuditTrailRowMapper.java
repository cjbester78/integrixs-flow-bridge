package com.integrixs.data.sql.mapper;

import com.integrixs.data.model.AuditTrail;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * RowMapper for AuditTrail entity.
 */
public class AuditTrailRowMapper implements RowMapper<AuditTrail> {

    @Override
    public AuditTrail mapRow(ResultSet rs, int rowNum) throws SQLException {
        AuditTrail entity = new AuditTrail();

        // Basic fields
        entity.setId((UUID) rs.getObject("id"));

        // Timestamps
        if (rs.getTimestamp("created_at") != null) {
            entity.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }

        return entity;
    }
}
