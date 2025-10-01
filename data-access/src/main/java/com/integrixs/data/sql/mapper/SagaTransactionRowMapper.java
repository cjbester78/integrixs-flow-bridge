package com.integrixs.data.sql.mapper;

import com.integrixs.data.model.SagaTransaction;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * RowMapper for SagaTransaction entity.
 */
public class SagaTransactionRowMapper implements RowMapper<SagaTransaction> {

    @Override
    public SagaTransaction mapRow(ResultSet rs, int rowNum) throws SQLException {
        SagaTransaction entity = new SagaTransaction();

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
