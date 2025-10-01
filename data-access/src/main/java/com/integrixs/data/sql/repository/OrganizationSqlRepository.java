package com.integrixs.data.sql.repository;

import com.integrixs.data.model.Organization;
import com.integrixs.data.repository.OrganizationRepository;
import com.integrixs.data.sql.core.BaseSqlRepository;
import com.integrixs.data.sql.core.SqlPaginationHelper;
import com.integrixs.data.sql.core.SqlQueryExecutor;
import com.integrixs.data.sql.mapper.OrganizationRowMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;

/**
 * SQL implementation of OrganizationRepository
 */
@Repository("organizationSqlRepository")
public class OrganizationSqlRepository extends BaseSqlRepository<Organization, UUID> implements OrganizationRepository {

    private static final String TABLE_NAME = "organizations";
    private static final String ID_COLUMN = "id";
    private static final RowMapper<Organization> ROW_MAPPER = new OrganizationRowMapper();

    public OrganizationSqlRepository(SqlQueryExecutor sqlQueryExecutor) {
        super(sqlQueryExecutor, TABLE_NAME, ID_COLUMN, ROW_MAPPER);
    }

    @Override
    public Organization save(Organization organization) {
        if (organization.getId() == null) {
            organization.setId(UUID.randomUUID());
            organization.setCreatedAt(LocalDateTime.now());
            organization.setUpdatedAt(LocalDateTime.now());

            String sql = "INSERT INTO " + TABLE_NAME +
                " (id, name, description, website, email, contact_person, logo_url, " +
                "verified, verified_at, verification_details, template_count, download_count, " +
                "average_rating, created_at, updated_at, created_by, updated_by) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            sqlQueryExecutor.update(sql,
                organization.getId(),
                organization.getName(),
                organization.getDescription(),
                organization.getWebsite(),
                organization.getEmail(),
                organization.getContactPerson(),
                organization.getLogoUrl(),
                organization.isVerified(),
                organization.getVerifiedAt(),
                organization.getVerificationDetails(),
                organization.getTemplateCount(),
                organization.getDownloadCount(),
                organization.getAverageRating(),
                organization.getCreatedAt(),
                organization.getUpdatedAt(),
                null, // createdBy - handled at service layer
                null  // updatedBy - handled at service layer
            );
            return organization;
        } else {
            return update(organization);
        }
    }

    @Override
    public Organization update(Organization organization) {
        organization.setUpdatedAt(LocalDateTime.now());

        String sql = "UPDATE " + TABLE_NAME + " SET " +
            "name = ?, description = ?, website = ?, email = ?, contact_person = ?, " +
            "logo_url = ?, verified = ?, verified_at = ?, verification_details = ?, " +
            "template_count = ?, download_count = ?, average_rating = ?, updated_at = ?, updated_by = ? " +
            "WHERE id = ?";

        sqlQueryExecutor.update(sql,
            organization.getName(),
            organization.getDescription(),
            organization.getWebsite(),
            organization.getEmail(),
            organization.getContactPerson(),
            organization.getLogoUrl(),
            organization.isVerified(),
            organization.getVerifiedAt(),
            organization.getVerificationDetails(),
            organization.getTemplateCount(),
            organization.getDownloadCount(),
            organization.getAverageRating(),
            organization.getUpdatedAt(),
            null, // updatedBy - handled at service layer
            organization.getId()
        );

        return organization;
    }

    @Override
    public Optional<Organization> findById(UUID id) {
        return super.findById(id);
    }

    @Override
    public Page<Organization> findAll(Pageable pageable) {
        String baseQuery = "SELECT * FROM " + TABLE_NAME;
        String countQuery = "SELECT COUNT(*) FROM " + TABLE_NAME;

        long total = sqlQueryExecutor.count(countQuery);
        String paginatedQuery = baseQuery +
            SqlPaginationHelper.buildOrderByClause(pageable.getSort()) +
            SqlPaginationHelper.buildPaginationClause(pageable);

        List<Organization> organizations = sqlQueryExecutor.queryForList(paginatedQuery, rowMapper);

        return new PageImpl<>(organizations, pageable, total);
    }

    @Override
    public Page<Organization> findByVerifiedTrue(Pageable pageable) {
        String baseQuery = "SELECT * FROM " + TABLE_NAME + " WHERE verified = true";
        String countQuery = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE verified = true";

        long total = sqlQueryExecutor.count(countQuery);
        String paginatedQuery = baseQuery +
            SqlPaginationHelper.buildOrderByClause(pageable.getSort()) +
            SqlPaginationHelper.buildPaginationClause(pageable);

        List<Organization> organizations = sqlQueryExecutor.queryForList(paginatedQuery, rowMapper);

        return new PageImpl<>(organizations, pageable, total);
    }

    @Override
    public Optional<Organization> findByName(String name) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE name = ?";
        List<Organization> results = sqlQueryExecutor.queryForList(sql, rowMapper, name);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public Optional<Organization> findByEmail(String email) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE email = ?";
        List<Organization> results = sqlQueryExecutor.queryForList(sql, rowMapper, email);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public List<Organization> findByVerifiedFalse() {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE verified = false ORDER BY created_at DESC";
        return sqlQueryExecutor.queryForList(sql, rowMapper);
    }

    @Override
    public void deleteById(UUID id) {
        super.deleteById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return super.existsById(id);
    }

    @Override
    public long count() {
        return super.count();
    }
}