package com.integrixs.data.repository;

import com.integrixs.data.model.JarFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JarFileRepository extends JpaRepository<JarFile, UUID> {
    
    Optional<JarFile> findByFileName(String fileName);
    
    Optional<JarFile> findByChecksum(String checksum);
    
    List<JarFile> findByIsActiveTrue();
    
    List<JarFile> findByUploadedBy(String uploadedBy);
    
    @Query("SELECT j FROM JarFile j WHERE j.isActive = true ORDER BY j.uploadedAt DESC")
    List<JarFile> findAllActive();
    
    @Query("SELECT j FROM JarFile j WHERE LOWER(j.fileName) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(j.displayName) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(j.description) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<JarFile> searchByQuery(@Param("query") String query);
    
    boolean existsByChecksum(String checksum);
    
    @Query("SELECT SUM(j.fileSize) FROM JarFile j WHERE j.isActive = true")
    Long getTotalActiveFileSize();
}