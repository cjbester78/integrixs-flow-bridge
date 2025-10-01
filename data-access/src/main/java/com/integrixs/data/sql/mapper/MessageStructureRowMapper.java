package com.integrixs.data.sql.mapper;

import com.integrixs.data.model.MessageStructure;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * RowMapper for MessageStructure entity.
 */
public class MessageStructureRowMapper implements RowMapper<MessageStructure> {

    @Override
    public MessageStructure mapRow(ResultSet rs, int rowNum) throws SQLException {
        MessageStructure entity = new MessageStructure();

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
