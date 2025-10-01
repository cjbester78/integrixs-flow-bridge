package com.integrixs.data.sql.repository;

import com.integrixs.data.model.AdapterCategory;
import com.integrixs.data.sql.core.BaseSqlRepository;
import com.integrixs.data.sql.core.ResultSetMapper;
import com.integrixs.data.sql.core.SqlQueryExecutor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * SQL implementation of AdapterCategoryRepository using native queries.
 */
@Repository
public class AdapterCategorySqlRepository extends BaseSqlRepository<AdapterCategory, UUID> {

    private static final String TABLE_NAME = "adapter_categories";
    private static final String ID_COLUMN = "id";

    /**
     * Row mapper for AdapterCategory entity
     */
    private static final RowMapper<AdapterCategory> ADAPTER_CATEGORY_ROW_MAPPER = new RowMapper<AdapterCategory>() {
        @Override
        public AdapterCategory mapRow(ResultSet rs, int rowNum) throws SQLException {
            AdapterCategory category = new AdapterCategory();
            category.setId(ResultSetMapper.getUUID(rs, "id"));
            category.setCode(ResultSetMapper.getString(rs, "code"));
            category.setName(ResultSetMapper.getString(rs, "name"));
            category.setDescription(ResultSetMapper.getString(rs, "description"));
            category.setIcon(ResultSetMapper.getString(rs, "icon"));
            category.setDisplayOrder(ResultSetMapper.getInteger(rs, "display_order"));
            category.setCreatedAt(ResultSetMapper.getLocalDateTime(rs, "created_at"));

            // Handle parent category if exists
            UUID parentId = ResultSetMapper.getUUID(rs, "parent_category_id");
            if (parentId != null) {
                AdapterCategory parent = new AdapterCategory();
                parent.setId(parentId);
                category.setParentCategory(parent);
            }

            return category;
        }
    };

    public AdapterCategorySqlRepository(SqlQueryExecutor sqlQueryExecutor) {
        super(sqlQueryExecutor, TABLE_NAME, ID_COLUMN, ADAPTER_CATEGORY_ROW_MAPPER);
    }

    public Optional<AdapterCategory> findByName(String name) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE name = ?";
        return sqlQueryExecutor.queryForObject(sql, ADAPTER_CATEGORY_ROW_MAPPER, name);
    }

    public List<AdapterCategory> findAllOrderByDisplayOrder() {
        String sql = "SELECT * FROM " + TABLE_NAME + " ORDER BY display_order";
        return sqlQueryExecutor.queryForList(sql, ADAPTER_CATEGORY_ROW_MAPPER);
    }

    @Override
    public AdapterCategory save(AdapterCategory category) {
        if (category.getId() == null) {
            category.setId(generateId());
        }

        boolean exists = existsById(category.getId());

        if (!exists) {
            return insert(category);
        } else {
            return update(category);
        }
    }

    private AdapterCategory insert(AdapterCategory category) {
        String sql = "INSERT INTO " + TABLE_NAME + " (" +
                     "id, code, name, description, icon, display_order, " +
                     "parent_category_id, created_at" +
                     ") VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        LocalDateTime now = LocalDateTime.now();
        if (category.getCreatedAt() == null) {
            category.setCreatedAt(now);
        }

        UUID parentCategoryId = category.getParentCategory() != null ?
                               category.getParentCategory().getId() : null;

        sqlQueryExecutor.update(sql,
            category.getId(),
            category.getCode(),
            category.getName(),
            category.getDescription(),
            category.getIcon(),
            category.getDisplayOrder(),
            parentCategoryId,
            ResultSetMapper.toTimestamp(category.getCreatedAt())
        );

        return category;
    }

    @Override
    public AdapterCategory update(AdapterCategory category) {
        String sql = "UPDATE " + TABLE_NAME + " SET " +
                     "code = ?, name = ?, description = ?, icon = ?, display_order = ?, " +
                     "parent_category_id = ? WHERE id = ?";

        UUID parentCategoryId = category.getParentCategory() != null ?
                               category.getParentCategory().getId() : null;

        sqlQueryExecutor.update(sql,
            category.getCode(),
            category.getName(),
            category.getDescription(),
            category.getIcon(),
            category.getDisplayOrder(),
            parentCategoryId,
            category.getId()
        );

        return category;
    }

    public java.util.Optional<AdapterCategory> findByCode(String code) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE code = ?";
        java.util.List<AdapterCategory> results = sqlQueryExecutor.queryForList(sql, ADAPTER_CATEGORY_ROW_MAPPER, code);
        return results.isEmpty() ? java.util.Optional.empty() : java.util.Optional.of(results.get(0));
    }
}