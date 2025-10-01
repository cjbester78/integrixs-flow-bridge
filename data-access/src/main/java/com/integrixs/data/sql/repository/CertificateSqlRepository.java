package com.integrixs.data.sql.repository;

import com.integrixs.data.model.Certificate;
import com.integrixs.data.sql.core.BaseSqlRepository;
import com.integrixs.data.sql.core.ResultSetMapper;
import com.integrixs.data.sql.core.SqlQueryExecutor;
import com.integrixs.data.sql.core.SqlPaginationHelper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * SQL implementation of CertificateRepository using native queries.
 */
@Repository("certificateSqlRepository")
public class CertificateSqlRepository extends BaseSqlRepository<Certificate, UUID> {

    private static final String TABLE_NAME = "certificates";
    private static final String ID_COLUMN = "id";

    /**
     * Row mapper for Certificate entity
     */
    private static final RowMapper<Certificate> CERTIFICATE_ROW_MAPPER = new RowMapper<Certificate>() {
        @Override
        public Certificate mapRow(ResultSet rs, int rowNum) throws SQLException {
            Certificate cert = new Certificate();
            cert.setId(ResultSetMapper.getUUID(rs, "id"));
            cert.setName(ResultSetMapper.getString(rs, "name"));
            cert.setFormat(ResultSetMapper.getString(rs, "format"));
            cert.setType(ResultSetMapper.getString(rs, "type"));
            cert.setFileName(ResultSetMapper.getString(rs, "file_name"));
            cert.setPassword(ResultSetMapper.getString(rs, "password"));
            cert.setUploadedBy(ResultSetMapper.getString(rs, "uploaded_by"));
            cert.setUploadedAt(ResultSetMapper.getLocalDateTime(rs, "uploaded_at"));
            cert.setContent(ResultSetMapper.getBytes(rs, "content"));
            return cert;
        }
    };

    public CertificateSqlRepository(SqlQueryExecutor sqlQueryExecutor) {
        super(sqlQueryExecutor, TABLE_NAME, ID_COLUMN, CERTIFICATE_ROW_MAPPER);
    }

    /**
     * Find certificate by name
     */
    public Optional<Certificate> findByName(String name) {
        String sql = "SELECT * FROM certificates WHERE name = ?";
        return sqlQueryExecutor.queryForObject(sql, CERTIFICATE_ROW_MAPPER, name);
    }

    /**
     * Find certificates by uploaded by
     */
    public List<Certificate> findByUploadedBy(String uploadedBy) {
        String sql = "SELECT * FROM certificates WHERE uploaded_by = ?";
        return sqlQueryExecutor.queryForList(sql, CERTIFICATE_ROW_MAPPER, uploadedBy);
    }

    /**
     * Check if certificate exists by name
     */
    public boolean existsByName(String name) {
        String sql = "SELECT COUNT(*) FROM certificates WHERE name = ?";
        return sqlQueryExecutor.exists(sql, name);
    }

    @Override
    public Certificate save(Certificate certificate) {
        if (certificate.getId() == null) {
            certificate.setId(generateId());
        }

        if (certificate.getUploadedAt() == null) {
            certificate.setUploadedAt(LocalDateTime.now());
        }

        String sql = buildInsertSql(
            "id", "name", "format", "type", "file_name",
            "password", "uploaded_by", "uploaded_at", "content"
        );

        sqlQueryExecutor.update(sql,
            certificate.getId(),
            certificate.getName(),
            certificate.getFormat(),
            certificate.getType(),
            certificate.getFileName(),
            certificate.getPassword(),
            certificate.getUploadedBy(),
            ResultSetMapper.toTimestamp(certificate.getUploadedAt()),
            certificate.getContent()
        );

        return certificate;
    }

    @Override
    public Certificate update(Certificate certificate) {
        String sql = buildUpdateSql(
            "name", "format", "type", "file_name",
            "password", "uploaded_by", "uploaded_at", "content"
        );

        sqlQueryExecutor.update(sql,
            certificate.getName(),
            certificate.getFormat(),
            certificate.getType(),
            certificate.getFileName(),
            certificate.getPassword(),
            certificate.getUploadedBy(),
            ResultSetMapper.toTimestamp(certificate.getUploadedAt()),
            certificate.getContent(),
            certificate.getId()
        );

        return certificate;
    }

    /**
     * Find all certificates with pagination
     */
    public Page<Certificate> findAll(Pageable pageable) {
        // Count query
        String countSql = "SELECT COUNT(*) FROM " + TABLE_NAME;
        long total = sqlQueryExecutor.count(countSql);

        // Data query
        String sql = "SELECT * FROM " + TABLE_NAME;
        sql += SqlPaginationHelper.buildOrderByClause(pageable.getSort());
        sql += SqlPaginationHelper.buildPaginationClause(pageable);

        List<Certificate> certificates = sqlQueryExecutor.queryForList(sql, CERTIFICATE_ROW_MAPPER);

        return new PageImpl<>(certificates, pageable, total);
    }
}