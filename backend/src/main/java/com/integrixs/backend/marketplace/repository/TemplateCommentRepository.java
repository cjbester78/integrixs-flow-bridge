package com.integrixs.backend.marketplace.repository;

import com.integrixs.backend.marketplace.entity.TemplateComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository for template comments
 */
@Repository
public interface TemplateCommentRepository extends JpaRepository<TemplateComment, UUID> {
}