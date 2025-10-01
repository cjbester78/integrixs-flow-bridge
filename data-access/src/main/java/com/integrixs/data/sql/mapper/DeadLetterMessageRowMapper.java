package com.integrixs.data.sql.mapper;

import com.integrixs.data.model.DeadLetterMessage;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * RowMapper for DeadLetterMessage entity.
 */
public class DeadLetterMessageRowMapper implements RowMapper<DeadLetterMessage> {

    @Override
    public DeadLetterMessage mapRow(ResultSet rs, int rowNum) throws SQLException {
        DeadLetterMessage entity = new DeadLetterMessage();

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
