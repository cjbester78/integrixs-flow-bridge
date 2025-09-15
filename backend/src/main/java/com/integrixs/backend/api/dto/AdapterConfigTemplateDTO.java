package com.integrixs.backend.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdapterConfigTemplateDTO {
    private UUID id;
    private UUID adapterTypeId;
    private String adapterTypeName;
    private String name;
    private String description;
    private String direction;
    private Map<String, Object> configuration;
    private boolean isDefault;
    private boolean isPublic;
    private String[] tags;
    private LocalDateTime createdAt;
    private String createdByUsername;
}
