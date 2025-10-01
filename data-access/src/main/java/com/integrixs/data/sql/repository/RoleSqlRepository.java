package com.integrixs.data.sql.repository;

import com.integrixs.data.model.Role;
import com.integrixs.data.sql.core.BaseSqlRepository;
import com.integrixs.data.sql.core.ResultSetMapper;
import com.integrixs.data.sql.core.SqlQueryExecutor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * SQL implementation of RoleRepository using native queries.
 */
@Repository("roleSqlRepository")
public class RoleSqlRepository extends BaseSqlRepository<Role, UUID> {

    private static final String TABLE_NAME = "roles";
    private static final String ID_COLUMN = "id";

    /**
     * Row mapper for Role entity
     */
    private static final RowMapper<Role> ROLE_ROW_MAPPER = new RowMapper<Role>() {
        @Override
        public Role mapRow(ResultSet rs, int rowNum) throws SQLException {
            Role role = new Role();
            role.setId(ResultSetMapper.getUUID(rs, "id"));
            role.setName(ResultSetMapper.getString(rs, "name"));
            role.setPermissions(ResultSetMapper.getString(rs, "permissions"));
            return role;
        }
    };

    public RoleSqlRepository(SqlQueryExecutor sqlQueryExecutor) {
        super(sqlQueryExecutor, TABLE_NAME, ID_COLUMN, ROLE_ROW_MAPPER);
    }

    /**
     * Find role by name
     */
    public Optional<Role> findByName(String name) {
        String sql = "SELECT * FROM roles WHERE name = ?";
        return sqlQueryExecutor.queryForObject(sql, ROLE_ROW_MAPPER, name);
    }

    /**
     * Check if role exists by name
     */
    public boolean existsByName(String name) {
        String sql = "SELECT COUNT(*) FROM roles WHERE name = ?";
        return sqlQueryExecutor.exists(sql, name);
    }

    /**
     * Find all roles with pagination
     */
    public Page<Role> findAll(Pageable pageable) {
        String countSql = "SELECT COUNT(*) FROM " + TABLE_NAME;
        long total = sqlQueryExecutor.count(countSql);

        String sql = "SELECT * FROM " + TABLE_NAME + " ORDER BY name";
        sql += " LIMIT " + pageable.getPageSize() + " OFFSET " + pageable.getOffset();

        List<Role> content = sqlQueryExecutor.queryForList(sql, ROLE_ROW_MAPPER);
        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Role save(Role role) {
        if (role.getId() == null) {
            role.setId(generateId());
        }

        String sql = buildInsertSql("id", "name", "permissions");

        sqlQueryExecutor.update(sql,
            role.getId(),
            role.getName(),
            role.getPermissions()
        );

        return role;
    }

    @Override
    public Role update(Role role) {
        String sql = buildUpdateSql("name", "permissions");

        sqlQueryExecutor.update(sql,
            role.getName(),
            role.getPermissions(),
            role.getId()
        );

        return role;
    }
}