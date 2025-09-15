package com.integrixs.data.repository;

import com.integrixs.data.model.AdapterCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AdapterCategoryRepository extends JpaRepository<AdapterCategory, UUID> {

    Optional<AdapterCategory> findByCode(String code);

    List<AdapterCategory> findByParentCategoryIsNullOrderByDisplayOrder();

    List<AdapterCategory> findByParentCategoryIdOrderByDisplayOrder(UUID parentId);

    @Query("SELECT ac FROM AdapterCategory ac WHERE ac.parentCategory IS NULL ORDER BY ac.displayOrder")
    List<AdapterCategory> findAllRootCategories();
}
