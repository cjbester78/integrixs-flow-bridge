package com.integrixs.backend.marketplace.dto;

import com.integrixs.backend.marketplace.entity.FlowTemplate.TemplateCategory;
import com.integrixs.backend.marketplace.entity.FlowTemplate.TemplateType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateDto {
    private UUID id;
    private String slug;
    private String name;
    private String description;
    private TemplateCategory category;
    private TemplateType type;
    private AuthorDto author;
    private OrganizationDto organization;
    private String version;
    private String iconUrl;
    private List<String> tags;
    private Long downloadCount;
    private Long installCount;
    private Double averageRating;
    private Long ratingCount;
    private boolean certified;
    private boolean featured;
    private LocalDateTime publishedAt;
}
