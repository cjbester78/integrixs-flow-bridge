package com.integrixs.data.sql.repository;

import com.integrixs.data.model.FlowStructureMessage;
import com.integrixs.data.model.FlowStructure;
import com.integrixs.data.model.MessageStructure;
import com.integrixs.data.sql.core.ResultSetMapper;
import com.integrixs.data.sql.core.SqlQueryExecutor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * SQL implementation of FlowStructureMessageRepository using native queries.
 */
@Repository
public class FlowStructureMessageSqlRepository {

    private final SqlQueryExecutor sqlQueryExecutor;
    private static final String TABLE_NAME = "flow_structure_messages";

    /**
     * Row mapper for FlowStructureMessage entity
     */
    private static final RowMapper<FlowStructureMessage> FLOW_STRUCTURE_MESSAGE_ROW_MAPPER = new RowMapper<FlowStructureMessage>() {
        @Override
        public FlowStructureMessage mapRow(ResultSet rs, int rowNum) throws SQLException {
            FlowStructureMessage message = new FlowStructureMessage();

            // Set relationships
            UUID flowStructureId = ResultSetMapper.getUUID(rs, "flow_structure_id");
            if (flowStructureId != null) {
                FlowStructure flowStructure = new FlowStructure();
                flowStructure.setId(flowStructureId);
                message.setFlowStructure(flowStructure);
            }

            UUID messageStructureId = ResultSetMapper.getUUID(rs, "message_structure_id");
            if (messageStructureId != null) {
                MessageStructure messageStructure = new MessageStructure();
                messageStructure.setId(messageStructureId);
                message.setMessageStructure(messageStructure);
            }

            // Set message type
            String messageTypeStr = ResultSetMapper.getString(rs, "message_type");
            if (messageTypeStr != null) {
                message.setMessageType(FlowStructureMessage.MessageType.valueOf(messageTypeStr));
            }

            return message;
        }
    };

    public FlowStructureMessageSqlRepository(SqlQueryExecutor sqlQueryExecutor) {
        this.sqlQueryExecutor = sqlQueryExecutor;
    }

    public List<FlowStructureMessage> findByFlowStructureId(UUID flowStructureId) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE flow_structure_id = ?";
        return sqlQueryExecutor.queryForList(sql, FLOW_STRUCTURE_MESSAGE_ROW_MAPPER, flowStructureId);
    }

    public List<FlowStructureMessage> findByMessageStructureId(UUID messageStructureId) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE message_structure_id = ?";
        return sqlQueryExecutor.queryForList(sql, FLOW_STRUCTURE_MESSAGE_ROW_MAPPER, messageStructureId);
    }

    public List<FlowStructureMessage> findByFlowStructureIdAndMessageType(UUID flowStructureId, FlowStructureMessage.MessageType messageType) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE flow_structure_id = ? AND message_type = ?";
        return sqlQueryExecutor.queryForList(sql, FLOW_STRUCTURE_MESSAGE_ROW_MAPPER, flowStructureId, messageType.name());
    }

    public FlowStructureMessage save(FlowStructureMessage message) {
        // Check if exists
        String checkSql = "SELECT COUNT(*) FROM " + TABLE_NAME +
                         " WHERE flow_structure_id = ? AND message_structure_id = ? AND message_type = ?";
        long count = sqlQueryExecutor.count(checkSql,
            message.getFlowStructure().getId(),
            message.getMessageStructure().getId(),
            message.getMessageType().name()
        );

        if (count == 0) {
            return insert(message);
        } else {
            return update(message);
        }
    }

    private FlowStructureMessage insert(FlowStructureMessage message) {
        String sql = "INSERT INTO " + TABLE_NAME + " (" +
                     "flow_structure_id, message_structure_id, message_type" +
                     ") VALUES (?, ?, ?)";

        sqlQueryExecutor.update(sql,
            message.getFlowStructure().getId(),
            message.getMessageStructure().getId(),
            message.getMessageType().name()
        );

        return message;
    }

    private FlowStructureMessage update(FlowStructureMessage message) {
        // Since there are no other fields to update, just return the message
        return message;
    }

    public void deleteByFlowStructureId(UUID flowStructureId) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE flow_structure_id = ?";
        sqlQueryExecutor.update(sql, flowStructureId);
    }

    public void delete(UUID flowStructureId, UUID messageStructureId, FlowStructureMessage.MessageType messageType) {
        String sql = "DELETE FROM " + TABLE_NAME +
                    " WHERE flow_structure_id = ? AND message_structure_id = ? AND message_type = ?";
        sqlQueryExecutor.update(sql, flowStructureId, messageStructureId, messageType.name());
    }

    public boolean exists(UUID flowStructureId, UUID messageStructureId, FlowStructureMessage.MessageType messageType) {
        String sql = "SELECT COUNT(*) FROM " + TABLE_NAME +
                    " WHERE flow_structure_id = ? AND message_structure_id = ? AND message_type = ?";
        return sqlQueryExecutor.count(sql, flowStructureId, messageStructureId, messageType.name()) > 0;
    }

    public List<FlowStructure> findFlowStructuresByMessageStructureId(UUID messageStructureId) {
        String sql = "SELECT DISTINCT fs.* FROM flow_structures fs " +
                    "INNER JOIN " + TABLE_NAME + " fsm ON fs.id = fsm.flow_structure_id " +
                    "WHERE fsm.message_structure_id = ?";

        // Using a custom row mapper for FlowStructure
        return sqlQueryExecutor.queryForList(sql, (rs, rowNum) -> {
            FlowStructure fs = new FlowStructure();
            fs.setId(ResultSetMapper.getUUID(rs, "id"));
            fs.setName(ResultSetMapper.getString(rs, "name"));
            fs.setDescription(ResultSetMapper.getString(rs, "description"));
            fs.setCreatedAt(ResultSetMapper.getLocalDateTime(rs, "created_at"));
            fs.setUpdatedAt(ResultSetMapper.getLocalDateTime(rs, "updated_at"));
            return fs;
        }, messageStructureId);
    }
}