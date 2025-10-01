package com.integrixs.data.sql.mapper;

import com.integrixs.data.model.Organization;
import com.integrixs.data.sql.core.ResultSetMapper;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Row mapper for Organization entities
 */
public class OrganizationRowMapper implements RowMapper<Organization> {

    @Override
    public Organization mapRow(ResultSet rs, int rowNum) throws SQLException {
        Organization org = new Organization();

        // Map base entity fields
        org.setId(ResultSetMapper.getUUID(rs, "id"));
        org.setCreatedAt(ResultSetMapper.getLocalDateTime(rs, "created_at"));
        org.setUpdatedAt(ResultSetMapper.getLocalDateTime(rs, "updated_at"));
        // Note: createdBy and updatedBy are stored as strings in DB but BaseEntity expects User objects
        // This is handled at the service layer

        // Map organization-specific fields
        org.setName(ResultSetMapper.getString(rs, "name"));
        org.setDescription(ResultSetMapper.getString(rs, "description"));
        org.setWebsite(ResultSetMapper.getString(rs, "website"));
        org.setEmail(ResultSetMapper.getString(rs, "email"));
        org.setContactPerson(ResultSetMapper.getString(rs, "contact_person"));
        org.setLogoUrl(ResultSetMapper.getString(rs, "logo_url"));
        org.setVerified(ResultSetMapper.getBoolean(rs, "verified"));
        org.setVerifiedAt(ResultSetMapper.getLocalDateTime(rs, "verified_at"));
        org.setVerificationDetails(ResultSetMapper.getString(rs, "verification_details"));
        org.setTemplateCount(rs.getInt("template_count"));
        org.setDownloadCount(rs.getInt("download_count"));
        org.setAverageRating(rs.getDouble("average_rating"));

        return org;
    }
}