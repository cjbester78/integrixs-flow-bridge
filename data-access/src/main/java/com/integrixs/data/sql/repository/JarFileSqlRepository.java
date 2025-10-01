package com.integrixs.data.sql.repository;

import com.integrixs.data.model.JarFile;
import com.integrixs.data.sql.core.BaseSqlRepository;
import com.integrixs.data.sql.core.ResultSetMapper;
import com.integrixs.data.sql.core.SqlQueryExecutor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * SQL implementation of JarFileRepository using native queries.
 */
@Repository("jarFileSqlRepository")
public class JarFileSqlRepository {

    private static final String TABLE_NAME = "jar_files";
    private static final String ID_COLUMN = "id";
    private final ObjectMapper objectMapper;
    private final SqlQueryExecutor sqlQueryExecutor;
    private final RowMapper<JarFile> JAR_FILE_ROW_MAPPER;

    public JarFileSqlRepository(SqlQueryExecutor sqlQueryExecutor, ObjectMapper objectMapper) {
        this.sqlQueryExecutor = sqlQueryExecutor;
        this.objectMapper = objectMapper;
        this.JAR_FILE_ROW_MAPPER = createRowMapper();
    }

    private RowMapper<JarFile> createRowMapper() {
        return new RowMapper<JarFile>() {
            @Override
            public JarFile mapRow(ResultSet rs, int rowNum) throws SQLException {
                JarFile jarFile = new JarFile();
                jarFile.setId(ResultSetMapper.getUUID(rs, "id"));
                jarFile.setFileName(ResultSetMapper.getString(rs, "file_name"));
                jarFile.setDisplayName(ResultSetMapper.getString(rs, "display_name"));
                jarFile.setDescription(ResultSetMapper.getString(rs, "description"));
                jarFile.setVersion(ResultSetMapper.getString(rs, "version"));
                jarFile.setFileSize(ResultSetMapper.getLong(rs, "file_size"));
                jarFile.setChecksum(ResultSetMapper.getString(rs, "checksum"));
                jarFile.setFileContent(ResultSetMapper.getBytes(rs, "file_content"));

                // Handle PostgreSQL array type
                Array sqlArray = rs.getArray("adapter_types");
                if (sqlArray != null) {
                    jarFile.setAdapterTypes((String[]) sqlArray.getArray());
                }

                jarFile.setUploadedBy(ResultSetMapper.getString(rs, "uploaded_by"));
                jarFile.setUploadedAt(ResultSetMapper.getLocalDateTime(rs, "uploaded_at"));
                jarFile.setActive(ResultSetMapper.getBoolean(rs, "is_active"));

                // Handle JSON metadata
                String metadataJson = ResultSetMapper.getString(rs, "metadata");
                if (metadataJson != null) {
                    try {
                        Map<String, Object> metadata = objectMapper.readValue(metadataJson,
                            new TypeReference<Map<String, Object>>() {});
                        jarFile.setMetadata(metadata);
                    } catch (Exception e) {
                        // Log error and continue without metadata
                    }
                }

                return jarFile;
            }
        };
    }

    public Optional<JarFile> findById(UUID id) {
        String sql = "SELECT * FROM " + TABLE_NAME + " WHERE " + ID_COLUMN + " = ?";
        return sqlQueryExecutor.queryForObject(sql, JAR_FILE_ROW_MAPPER, id);
    }

    public List<JarFile> findAll() {
        String sql = "SELECT * FROM " + TABLE_NAME + " ORDER BY uploaded_at DESC";
        return sqlQueryExecutor.queryForList(sql, JAR_FILE_ROW_MAPPER);
    }

    public Optional<JarFile> findByFileName(String fileName) {
        String sql = "SELECT * FROM jar_files WHERE file_name = ?";
        return sqlQueryExecutor.queryForObject(sql, JAR_FILE_ROW_MAPPER, fileName);
    }

    public Optional<JarFile> findByChecksum(String checksum) {
        String sql = "SELECT * FROM jar_files WHERE checksum = ?";
        return sqlQueryExecutor.queryForObject(sql, JAR_FILE_ROW_MAPPER, checksum);
    }

    public List<JarFile> findByIsActiveTrue() {
        String sql = "SELECT * FROM jar_files WHERE is_active = true";
        return sqlQueryExecutor.queryForList(sql, JAR_FILE_ROW_MAPPER);
    }

    public List<JarFile> findByUploadedBy(String uploadedBy) {
        String sql = "SELECT * FROM jar_files WHERE uploaded_by = ?";
        return sqlQueryExecutor.queryForList(sql, JAR_FILE_ROW_MAPPER, uploadedBy);
    }

    public List<JarFile> findAllActive() {
        String sql = "SELECT * FROM jar_files WHERE is_active = true ORDER BY uploaded_at DESC";
        return sqlQueryExecutor.queryForList(sql, JAR_FILE_ROW_MAPPER);
    }

    public List<JarFile> searchByQuery(String query) {
        String sql = "SELECT * FROM jar_files WHERE " +
                     "LOWER(file_name) LIKE LOWER(?) OR " +
                     "LOWER(display_name) LIKE LOWER(?) OR " +
                     "LOWER(description) LIKE LOWER(?)";
        String searchPattern = "%" + query + "%";
        return sqlQueryExecutor.queryForList(sql, JAR_FILE_ROW_MAPPER,
                                           searchPattern, searchPattern, searchPattern);
    }

    public boolean existsByChecksum(String checksum) {
        String sql = "SELECT COUNT(*) FROM jar_files WHERE checksum = ?";
        return sqlQueryExecutor.exists(sql, checksum);
    }

    public Long getTotalActiveFileSize() {
        String sql = "SELECT SUM(file_size) FROM jar_files WHERE is_active = true";
        Long result = sqlQueryExecutor.getJdbcTemplate().queryForObject(sql, Long.class);
        return result != null ? result : 0L;
    }

    public JarFile save(JarFile jarFile) {
        if (jarFile.getId() == null) {
            jarFile.setId(UUID.randomUUID());
        }

        if (jarFile.getUploadedAt() == null) {
            jarFile.setUploadedAt(LocalDateTime.now());
        }

        // Convert metadata to JSON
        String metadataJson = null;
        if (jarFile.getMetadata() != null) {
            try {
                metadataJson = objectMapper.writeValueAsString(jarFile.getMetadata());
            } catch (Exception e) {
                // Handle error
            }
        }

        String sql = "INSERT INTO jar_files (id, file_name, display_name, description, version, " +
                     "file_size, checksum, file_content, adapter_types, uploaded_by, uploaded_at, " +
                     "is_active, metadata) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?::json)";

        sqlQueryExecutor.getJdbcTemplate().update(sql,
            jarFile.getId(),
            jarFile.getFileName(),
            jarFile.getDisplayName(),
            jarFile.getDescription(),
            jarFile.getVersion(),
            jarFile.getFileSize(),
            jarFile.getChecksum(),
            jarFile.getFileContent(),
            jarFile.getAdapterTypes(),
            jarFile.getUploadedBy(),
            ResultSetMapper.toTimestamp(jarFile.getUploadedAt()),
            jarFile.isActive(),
            metadataJson
        );

        return jarFile;
    }

    public JarFile update(JarFile jarFile) {
        // Convert metadata to JSON
        String metadataJson = null;
        if (jarFile.getMetadata() != null) {
            try {
                metadataJson = objectMapper.writeValueAsString(jarFile.getMetadata());
            } catch (Exception e) {
                // Handle error
            }
        }

        String sql = "UPDATE jar_files SET file_name = ?, display_name = ?, description = ?, " +
                     "version = ?, file_size = ?, checksum = ?, file_content = ?, adapter_types = ?, " +
                     "is_active = ?, metadata = ?::json WHERE id = ?";

        sqlQueryExecutor.getJdbcTemplate().update(sql,
            jarFile.getFileName(),
            jarFile.getDisplayName(),
            jarFile.getDescription(),
            jarFile.getVersion(),
            jarFile.getFileSize(),
            jarFile.getChecksum(),
            jarFile.getFileContent(),
            jarFile.getAdapterTypes(),
            jarFile.isActive(),
            metadataJson,
            jarFile.getId()
        );

        return jarFile;
    }

    public void deleteById(UUID id) {
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE " + ID_COLUMN + " = ?";
        sqlQueryExecutor.update(sql, id);
    }

    public long count() {
        String sql = "SELECT COUNT(*) FROM " + TABLE_NAME;
        return sqlQueryExecutor.count(sql);
    }
}