package com.integrixs.backend.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdapterCategoryDTO {
    private UUID id;
    private String code;
    private String name;
    private String description;
    private String icon;
    private UUID parentCategoryId;
    private Integer displayOrder;
}