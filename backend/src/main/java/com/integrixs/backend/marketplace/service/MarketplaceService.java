package com.integrixs.backend.marketplace.service;

import com.integrixs.backend.marketplace.dto.*;
import com.integrixs.backend.marketplace.entity.*;
import com.integrixs.backend.marketplace.repository.*;
import com.integrixs.backend.marketplace.exception.TemplateNotFoundException;
import com.integrixs.backend.marketplace.exception.UnauthorizedAccessException;
import com.integrixs.backend.marketplace.specification.TemplateSpecifications;
import com.integrixs.backend.auth.entity.User;
import com.integrixs.backend.auth.service.AuthService;
import com.integrixs.backend.service.IntegrationFlowService;
import com.integrixs.data.model.IntegrationFlow;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MarketplaceService {

    private final FlowTemplateRepository templateRepository;
    private final TemplateVersionRepository versionRepository;
    private final TemplateRatingRepository ratingRepository;
    private final TemplateCommentRepository commentRepository;
    private final TemplateInstallationRepository installationRepository;
    private final OrganizationRepository organizationRepository;
    private final IntegrationFlowService flowService;
    private final AuthService authService;
    private final FileStorageService fileStorageService;
    private final TemplateValidationService validationService;
    private final ObjectMapper objectMapper;

    /**
     * Search templates with filters
     */
    public Page<TemplateDto> searchTemplates(TemplateSearchRequest request, Pageable pageable) {
        Specification<FlowTemplate> spec = Specification.where(TemplateSpecifications.isActive())
            .and(TemplateSpecifications.isPublic());

        if(request.getQuery() != null && !request.getQuery().isEmpty()) {
            spec = spec.and(TemplateSpecifications.searchByQuery(request.getQuery()));
        }

        if(request.getCategory() != null) {
            spec = spec.and(TemplateSpecifications.hasCategory(request.getCategory()));
        }

        if(request.getType() != null) {
            spec = spec.and(TemplateSpecifications.hasType(request.getType()));
        }

        if(request.getTags() != null && !request.getTags().isEmpty()) {
            spec = spec.and(TemplateSpecifications.hasTags(request.getTags()));
        }

        if(request.getMinRating() != null) {
            spec = spec.and(TemplateSpecifications.hasMinRating(request.getMinRating()));
        }

        if(request.isCertifiedOnly()) {
            spec = spec.and(TemplateSpecifications.isCertified());
        }

        if(request.getAuthorId() != null) {
            spec = spec.and(TemplateSpecifications.hasAuthor(request.getAuthorId()));
        }

        if(request.getOrganizationId() != null) {
            spec = spec.and(TemplateSpecifications.hasOrganization(request.getOrganizationId()));
        }

        Page<FlowTemplate> templates = templateRepository.findAll(spec, pageable);
        return templates.map(this::toDto);
    }

    /**
     * Get template details
     */
    public TemplateDetailDto getTemplateDetails(String slug) {
        FlowTemplate template = templateRepository.findBySlugWithDetails(slug)
            .orElseThrow(() -> new TemplateNotFoundException("Template not found: " + slug));

        // Check visibility
        User currentUser = authService.getCurrentUser();
        if(!isTemplateVisibleToUser(template, currentUser)) {
            throw new UnauthorizedAccessException("Template not accessible");
        }

        // Increment view count
        templateRepository.incrementDownloadCount(template.getId());

        return toDetailDto(template);
    }

    /**
     * Create a new template
     */
    public TemplateDto createTemplate(CreateTemplateRequest request) {
        User author = authService.getCurrentUser();

        // Validate template
        validationService.validateTemplate(request);

        // Create template
        FlowTemplate template = new FlowTemplate();
        template.setName(request.getName());
        template.setDescription(request.getDescription());
        template.setDetailedDescription(request.getDetailedDescription());
        template.setCategory(request.getCategory());
        template.setType(request.getType());
        template.setVisibility(request.getVisibility());
        template.setAuthor(author);
        template.setFlowDefinition(request.getFlowDefinition());
        template.setConfigurationSchema(request.getConfigurationSchema());
        template.setTags(new HashSet<>(request.getTags()));
        template.setRequirements(new HashSet<>(request.getRequirements()));
        template.setMinPlatformVersion(request.getMinPlatformVersion());
        template.setMaxPlatformVersion(request.getMaxPlatformVersion());

        // Set organization if provided
        if(request.getOrganizationId() != null) {
            Organization org = organizationRepository.findById(request.getOrganizationId())
                .orElseThrow(() -> new IllegalArgumentException("Organization not found"));
            if(!org.getMembers().contains(author) && !org.getOwner().equals(author)) {
                throw new UnauthorizedAccessException("Not a member of the organization");
            }
            template.setOrganization(org);
        }

        template = templateRepository.save(template);

        // Create initial version
        TemplateVersion version = new TemplateVersion();
        version.setTemplate(template);
        version.setVersion("1.0.0");
        version.setFlowDefinition(request.getFlowDefinition());
        version.setReleaseNotes("Initial release");
        version.setLatest(true);
        versionRepository.save(version);

        log.info("Created template: {} by user: {}", template.getSlug(), author.getUsername());

        return toDto(template);
    }

    /**
     * Update template
     */
    public TemplateDto updateTemplate(String slug, UpdateTemplateRequest request) {
        FlowTemplate template = templateRepository.findBySlug(slug)
            .orElseThrow(() -> new TemplateNotFoundException("Template not found: " + slug));

        // Check permissions
        User currentUser = authService.getCurrentUser();
        if(!canEditTemplate(template, currentUser)) {
            throw new UnauthorizedAccessException("Cannot edit this template");
        }

        // Update fields
        if(request.getName() != null) {
            template.setName(request.getName());
        }
        if(request.getDescription() != null) {
            template.setDescription(request.getDescription());
        }
        if(request.getDetailedDescription() != null) {
            template.setDetailedDescription(request.getDetailedDescription());
        }
        if(request.getCategory() != null) {
            template.setCategory(request.getCategory());
        }
        if(request.getTags() != null) {
            template.setTags(new HashSet<>(request.getTags()));
        }
        if(request.getRequirements() != null) {
            template.setRequirements(new HashSet<>(request.getRequirements()));
        }
        if(request.getVisibility() != null) {
            template.setVisibility(request.getVisibility());
        }
        if(request.getDocumentationUrl() != null) {
            template.setDocumentationUrl(request.getDocumentationUrl());
        }
        if(request.getSourceRepositoryUrl() != null) {
            template.setSourceRepositoryUrl(request.getSourceRepositoryUrl());
        }

        template = templateRepository.save(template);

        log.info("Updated template: {} by user: {}", template.getSlug(), currentUser.getUsername());

        return toDto(template);
    }

    /**
     * Upload template icon
     */
    public void uploadIcon(String slug, MultipartFile file) throws IOException {
        FlowTemplate template = templateRepository.findBySlug(slug)
            .orElseThrow(() -> new TemplateNotFoundException("Template not found: " + slug));

        // Check permissions
        User currentUser = authService.getCurrentUser();
        if(!canEditTemplate(template, currentUser)) {
            throw new UnauthorizedAccessException("Cannot edit this template");
        }

        // Upload file
        String iconUrl = fileStorageService.uploadFile(file, "template - icons/" + template.getId());
        template.setIconUrl(iconUrl);
        templateRepository.save(template);
    }

    /**
     * Add template screenshot
     */
    public void addScreenshot(String slug, MultipartFile file) throws IOException {
        FlowTemplate template = templateRepository.findBySlug(slug)
            .orElseThrow(() -> new TemplateNotFoundException("Template not found: " + slug));

        // Check permissions
        User currentUser = authService.getCurrentUser();
        if(!canEditTemplate(template, currentUser)) {
            throw new UnauthorizedAccessException("Cannot edit this template");
        }

        // Upload file
        String screenshotUrl = fileStorageService.uploadFile(
            file,
            "template - screenshots/" + template.getId() + "/" + UUID.randomUUID()
       );
        template.getScreenshots().add(screenshotUrl);
        templateRepository.save(template);
    }

    /**
     * Publish new version
     */
    public TemplateVersionDto publishVersion(String slug, PublishVersionRequest request) {
        FlowTemplate template = templateRepository.findBySlug(slug)
            .orElseThrow(() -> new TemplateNotFoundException("Template not found: " + slug));

        // Check permissions
        User currentUser = authService.getCurrentUser();
        if(!canEditTemplate(template, currentUser)) {
            throw new UnauthorizedAccessException("Cannot edit this template");
        }

        // Validate version
        validationService.validateVersion(template, request);

        // Mark current latest as not latest
        versionRepository.unmarkLatestVersions(template.getId());

        // Create new version
        TemplateVersion version = new TemplateVersion();
        version.setTemplate(template);
        version.setVersion(request.getVersion());
        version.setFlowDefinition(request.getFlowDefinition());
        version.setReleaseNotes(request.getReleaseNotes());
        version.setStable(request.isStable());
        version.setLatest(true);
        version.setMinPlatformVersion(request.getMinPlatformVersion());
        version.setMaxPlatformVersion(request.getMaxPlatformVersion());

        version = versionRepository.save(version);

        // Update template flow definition
        template.setFlowDefinition(request.getFlowDefinition());
        template.setVersion(request.getVersion());
        templateRepository.save(template);

        log.info("Published version {} for template: {}", version.getVersion(), template.getSlug());

        return toVersionDto(version);
    }

    /**
     * Install template
     */
    @Transactional
    public InstallationResultDto installTemplate(String slug, InstallTemplateRequest request) {
        FlowTemplate template = templateRepository.findBySlugWithDetails(slug)
            .orElseThrow(() -> new TemplateNotFoundException("Template not found: " + slug));

        User currentUser = authService.getCurrentUser();

        // Check visibility
        if(!isTemplateVisibleToUser(template, currentUser)) {
            throw new UnauthorizedAccessException("Template not accessible");
        }

        // Get version to install
        TemplateVersion version;
        if(request.getVersion() != null) {
            version = versionRepository.findByTemplateIdAndVersion(
                template.getId(),
                request.getVersion()
           ).orElseThrow(() -> new IllegalArgumentException("Version not found"));
        } else {
            version = versionRepository.findLatestByTemplateId(template.getId())
                .orElseThrow(() -> new IllegalArgumentException("No version available"));
        }

        try {
            // Parse flow definition
            IntegrationFlow flowDefinition = objectMapper.readValue(
                version.getFlowDefinition(),
                IntegrationFlow.class
           );

            // Apply configuration
            if(request.getConfiguration() != null) {
                applyConfiguration(flowDefinition, request.getConfiguration());
            }

            // Set metadata
            flowDefinition.setName(request.getName() != null ? request.getName() : template.getName());
            flowDefinition.setDescription("Installed from template: " + template.getName());

            // Create flow
            IntegrationFlow createdFlow = flowService.createIntegrationFlow(flowDefinition);

            // Record installation
            TemplateInstallation installation = new TemplateInstallation();
            installation.setTemplate(template);
            installation.setVersion(version);
            installation.setUser(currentUser);
            installation.setFlowId(createdFlow.getId());
            installation.setConfiguration(request.getConfiguration());
            installation.setAutoUpdateEnabled(request.isEnableAutoUpdate());

            if(request.getOrganizationId() != null) {
                Organization org = organizationRepository.findById(request.getOrganizationId())
                    .orElseThrow(() -> new IllegalArgumentException("Organization not found"));
                installation.setOrganization(org);
            }

            installationRepository.save(installation);

            // Update counters
            templateRepository.incrementInstallCount(template.getId());

            log.info("Installed template {} version {} for user {}",
                template.getSlug(), version.getVersion(), currentUser.getUsername());

            return InstallationResultDto.builder()
                .success(true)
                .flowId(createdFlow.getId())
                .installationId(installation.getId())
                .message("Template installed successfully")
                .build();

        } catch(Exception e) {
            log.error("Failed to install template: " + slug, e);
            return InstallationResultDto.builder()
                .success(false)
                .message("Installation failed: " + e.getMessage())
                .build();
        }
    }

    /**
     * Rate template
     */
    public void rateTemplate(String slug, RateTemplateRequest request) {
        FlowTemplate template = templateRepository.findBySlug(slug)
            .orElseThrow(() -> new TemplateNotFoundException("Template not found: " + slug));

        User currentUser = authService.getCurrentUser();

        // Check if already rated
        Optional<TemplateRating> existingRating = ratingRepository.findByTemplateIdAndUserId(
            template.getId(),
            currentUser.getId()
       );

        TemplateRating rating;
        if(existingRating.isPresent()) {
            // Update existing rating
            rating = existingRating.get();
            rating.setRating(request.getRating());
            rating.setReview(request.getReview());
        } else {
            // Create new rating
            rating = new TemplateRating();
            rating.setTemplate(template);
            rating.setUser(currentUser);
            rating.setRating(request.getRating());
            rating.setReview(request.getReview());

            // Check if user has installed this template
            boolean hasInstalled = installationRepository.existsByTemplateIdAndUserId(
                template.getId(),
                currentUser.getId()
           );
            rating.setVerifiedPurchase(hasInstalled);
        }

        ratingRepository.save(rating);

        // Update template rating stats
        updateTemplateRatingStats(template);

        log.info("User {} rated template {} with {} stars",
            currentUser.getUsername(), template.getSlug(), request.getRating());
    }

    /**
     * Add comment to template
     */
    public CommentDto addComment(String slug, AddCommentRequest request) {
        FlowTemplate template = templateRepository.findBySlug(slug)
            .orElseThrow(() -> new TemplateNotFoundException("Template not found: " + slug));

        User currentUser = authService.getCurrentUser();

        TemplateComment comment = new TemplateComment();
        comment.setTemplate(template);
        comment.setUser(currentUser);
        comment.setContent(request.getContent());

        // Check if this is author response
        if(template.getAuthor().equals(currentUser)) {
            comment.setAuthorResponse(true);
        }

        // Set parent comment if replying
        if(request.getParentCommentId() != null) {
            TemplateComment parent = commentRepository.findById(request.getParentCommentId())
                .orElseThrow(() -> new IllegalArgumentException("Parent comment not found"));
            comment.setParentComment(parent);
        }

        comment = commentRepository.save(comment);

        log.info("User {} added comment to template {}", currentUser.getUsername(), template.getSlug());

        return toCommentDto(comment);
    }

    /**
     * Get featured templates
     */
    public List<TemplateDto> getFeaturedTemplates() {
        List<FlowTemplate> templates = templateRepository.findFeaturedTemplates(LocalDateTime.now());
        return templates.stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    /**
     * Get trending templates
     */
    public List<TemplateDto> getTrendingTemplates(String period) {
        LocalDateTime since = calculatePeriodStart(period);

        Page<FlowTemplate> templates = templateRepository.findRecentlyPublished(
            since,
            Pageable.ofSize(10)
       );

        return templates.stream()
            .map(this::toDto)
            .collect(Collectors.toList());
    }

    /**
     * Get template statistics
     */
    public TemplateStatsDto getTemplateStats(String slug) {
        FlowTemplate template = templateRepository.findBySlug(slug)
            .orElseThrow(() -> new TemplateNotFoundException("Template not found: " + slug));

        return TemplateStatsDto.builder()
            .downloadCount(template.getDownloadCount())
            .installCount(template.getInstallCount())
            .averageRating(template.getAverageRating())
            .ratingCount(template.getRatingCount())
            .versionCount((long) template.getVersions().size())
            .commentCount((long) template.getComments().size())
            .lastUpdated(template.getUpdatedAt())
            .build();
    }

    // Helper methods

    private boolean isTemplateVisibleToUser(FlowTemplate template, User user) {
        if(template.getVisibility() == FlowTemplate.TemplateVisibility.PUBLIC) {
            return true;
        }
        if(user == null) {
            return false;
        }
        if(template.getAuthor().equals(user)) {
            return true;
        }
        if(template.getVisibility() == FlowTemplate.TemplateVisibility.ORGANIZATION &&
            template.getOrganization() != null &&
            template.getOrganization().getMembers().contains(user)) {
            return true;
        }
        return false;
    }

    private boolean canEditTemplate(FlowTemplate template, User user) {
        if(template.getAuthor().equals(user)) {
            return true;
        }
        if(template.getOrganization() != null &&
            (template.getOrganization().getOwner().equals(user) ||
             template.getOrganization().getMembers().contains(user))) {
            return true;
        }
        return authService.isAdmin(user);
    }

    private void updateTemplateRatingStats(FlowTemplate template) {
        List<TemplateRating> ratings = ratingRepository.findByTemplateId(template.getId());

        if(ratings.isEmpty()) {
            templateRepository.updateRatingStats(template.getId(), 0.0, 0L);
            return;
        }

        double averageRating = ratings.stream()
            .mapToInt(TemplateRating::getRating)
            .average()
            .orElse(0.0);

        long ratingCount = ratings.size();

        templateRepository.updateRatingStats(template.getId(), averageRating, ratingCount);
    }

    private void applyConfiguration(IntegrationFlow flow, Map<String, String> configuration) {
        // Apply configuration values to flow
        // This would replace placeholder values in the flow definition
        // Implementation depends on how configuration is structured
    }

    private LocalDateTime calculatePeriodStart(String period) {
        LocalDateTime now = LocalDateTime.now();
        return switch(period.toLowerCase()) {
            case "day" -> now.minusDays(1);
            case "week" -> now.minusWeeks(1);
            case "month" -> now.minusMonths(1);
            case "year" -> now.minusYears(1);
            default -> now.minusWeeks(1);
        };
    }

    // DTO conversion methods

    private TemplateDto toDto(FlowTemplate template) {
        return TemplateDto.builder()
            .id(template.getId())
            .slug(template.getSlug())
            .name(template.getName())
            .description(template.getDescription())
            .category(template.getCategory())
            .type(template.getType())
            .author(toAuthorDto(template.getAuthor()))
            .organization(template.getOrganization() != null ?
                toOrganizationDto(template.getOrganization()) : null)
            .version(template.getVersion())
            .iconUrl(template.getIconUrl())
            .tags(new ArrayList<>(template.getTags()))
            .downloadCount(template.getDownloadCount())
            .installCount(template.getInstallCount())
            .averageRating(template.getAverageRating())
            .ratingCount(template.getRatingCount())
            .certified(template.isCertified())
            .featured(template.isFeatured())
            .publishedAt(template.getPublishedAt())
            .build();
    }

    private TemplateDetailDto toDetailDto(FlowTemplate template) {
        TemplateDetailDto dto = new TemplateDetailDto();
        // Copy basic fields from toDto
        dto.setId(template.getId());
        dto.setSlug(template.getSlug());
        dto.setName(template.getName());
        dto.setDescription(template.getDescription());
        dto.setDetailedDescription(template.getDetailedDescription());
        dto.setCategory(template.getCategory());
        dto.setType(template.getType());
        dto.setAuthor(toAuthorDto(template.getAuthor()));
        dto.setOrganization(template.getOrganization() != null ?
            toOrganizationDto(template.getOrganization()) : null);
        dto.setVersion(template.getVersion());
        dto.setIconUrl(template.getIconUrl());
        dto.setDocumentationUrl(template.getDocumentationUrl());
        dto.setSourceRepositoryUrl(template.getSourceRepositoryUrl());
        dto.setTags(new ArrayList<>(template.getTags()));
        dto.setScreenshots(new ArrayList<>(template.getScreenshots()));
        dto.setRequirements(new ArrayList<>(template.getRequirements()));
        dto.setMinPlatformVersion(template.getMinPlatformVersion());
        dto.setMaxPlatformVersion(template.getMaxPlatformVersion());
        dto.setDownloadCount(template.getDownloadCount());
        dto.setInstallCount(template.getInstallCount());
        dto.setAverageRating(template.getAverageRating());
        dto.setRatingCount(template.getRatingCount());
        dto.setCertified(template.isCertified());
        dto.setFeatured(template.isFeatured());
        dto.setPublishedAt(template.getPublishedAt());
        dto.setUpdatedAt(template.getUpdatedAt());

        // Add versions
        dto.setVersions(template.getVersions().stream()
            .map(this::toVersionDto)
            .collect(Collectors.toList()));

        // Add dependencies
        dto.setDependencies(template.getDependencies().stream()
            .map(this::toDto)
            .collect(Collectors.toList()));

        return dto;
    }

    private AuthorDto toAuthorDto(User user) {
        return AuthorDto.builder()
            .id(user.getId())
            .username(user.getUsername())
            .displayName(user.getDisplayName())
            .avatarUrl(user.getAvatarUrl())
            .build();
    }

    private OrganizationDto toOrganizationDto(Organization org) {
        return OrganizationDto.builder()
            .id(org.getId())
            .slug(org.getSlug())
            .name(org.getName())
            .logoUrl(org.getLogoUrl())
            .verified(org.isVerified())
            .build();
    }

    private TemplateVersionDto toVersionDto(TemplateVersion version) {
        return TemplateVersionDto.builder()
            .id(version.getId())
            .version(version.getVersion())
            .releaseNotes(version.getReleaseNotes())
            .stable(version.isStable())
            .latest(version.isLatest())
            .publishedAt(version.getPublishedAt())
            .deprecated(version.isDeprecated())
            .deprecationMessage(version.getDeprecationMessage())
            .build();
    }

    private CommentDto toCommentDto(TemplateComment comment) {
        return CommentDto.builder()
            .id(comment.getId())
            .author(toAuthorDto(comment.getUser()))
            .content(comment.getContent())
            .authorResponse(comment.isAuthorResponse())
            .pinned(comment.isPinned())
            .postedAt(comment.getPostedAt())
            .editedAt(comment.getEditedAt())
            .likeCount(comment.getLikeCount())
            .replies(comment.getReplies().stream()
                .map(this::toCommentDto)
                .collect(Collectors.toList()))
            .build();
    }
}
