package com.integrixs.backend.marketplace.controller;

import com.integrixs.backend.marketplace.dto.*;
import com.integrixs.backend.marketplace.service.MarketplaceService;
import com.integrixs.backend.marketplace.service.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/marketplace")
@Tag(name = "Marketplace", description = "Template marketplace endpoints")
public class MarketplaceController {

    private final MarketplaceService marketplaceService;
    private final OrganizationService organizationService;

    public MarketplaceController(MarketplaceService marketplaceService, OrganizationService organizationService) {
        this.marketplaceService = marketplaceService;
        this.organizationService = organizationService;
    }

    @GetMapping("/templates")
    @Operation(summary = "Search templates", description = "Search and filter templates in the marketplace")
    public ResponseEntity<Page<TemplateDto>> searchTemplates(
            @Valid TemplateSearchRequest searchRequest,
            @PageableDefault(size = 20, sort = "publishedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<TemplateDto> templates = marketplaceService.searchTemplates(searchRequest, pageable);
        return ResponseEntity.ok(templates);
    }

    @GetMapping("/templates/ {slug}")
    @Operation(summary = "Get template details", description = "Get detailed information about a template")
    public ResponseEntity<TemplateDetailDto> getTemplateDetails(
            @PathVariable String slug) {
        TemplateDetailDto template = marketplaceService.getTemplateDetails(slug);
        return ResponseEntity.ok(template);
    }

    @PostMapping("/templates")
    @PreAuthorize("hasRole('DEVELOPER')")
    @Operation(summary = "Create template", description = "Create a new template in the marketplace")
    public ResponseEntity<TemplateDto> createTemplate(
            @Valid @RequestBody CreateTemplateRequest request) {
        TemplateDto template = marketplaceService.createTemplate(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(template);
    }

    @PutMapping("/templates/ {slug}")
    @PreAuthorize("hasRole('DEVELOPER')")
    @Operation(summary = "Update template", description = "Update an existing template")
    public ResponseEntity<TemplateDto> updateTemplate(
            @PathVariable String slug,
            @Valid @RequestBody UpdateTemplateRequest request) {
        TemplateDto template = marketplaceService.updateTemplate(slug, request);
        return ResponseEntity.ok(template);
    }

    @PostMapping("/templates/ {slug}/icon")
    @PreAuthorize("hasRole('DEVELOPER')")
    @Operation(summary = "Upload template icon", description = "Upload an icon for the template")
    public ResponseEntity<Void> uploadIcon(
            @PathVariable String slug,
            @RequestParam("file") MultipartFile file) throws IOException {
        marketplaceService.uploadIcon(slug, file);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/templates/ {slug}/screenshots")
    @PreAuthorize("hasRole('DEVELOPER')")
    @Operation(summary = "Add screenshot", description = "Add a screenshot to the template")
    public ResponseEntity<Void> addScreenshot(
            @PathVariable String slug,
            @RequestParam("file") MultipartFile file) throws IOException {
        marketplaceService.addScreenshot(slug, file);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/templates/ {slug}/versions")
    @PreAuthorize("hasRole('DEVELOPER')")
    @Operation(summary = "Publish new version", description = "Publish a new version of the template")
    public ResponseEntity<TemplateVersionDto> publishVersion(
            @PathVariable String slug,
            @Valid @RequestBody PublishVersionRequest request) {
        TemplateVersionDto version = marketplaceService.publishVersion(slug, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(version);
    }

    @PostMapping("/templates/ {slug}/install")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Install template", description = "Install a template and create a new flow")
    public ResponseEntity<InstallationResultDto> installTemplate(
            @PathVariable String slug,
            @Valid @RequestBody InstallTemplateRequest request) {
        InstallationResultDto result = marketplaceService.installTemplate(slug, request);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/templates/ {slug}/rate")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Rate template", description = "Rate and review a template")
    public ResponseEntity<Void> rateTemplate(
            @PathVariable String slug,
            @Valid @RequestBody RateTemplateRequest request) {
        marketplaceService.rateTemplate(slug, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/templates/ {slug}/comments")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Add comment", description = "Add a comment to a template")
    public ResponseEntity<CommentDto> addComment(
            @PathVariable String slug,
            @Valid @RequestBody AddCommentRequest request) {
        CommentDto comment = marketplaceService.addComment(slug, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }

    @GetMapping("/templates/ {slug}/stats")
    @Operation(summary = "Get template statistics", description = "Get usage statistics for a template")
    public ResponseEntity<TemplateStatsDto> getTemplateStats(
            @PathVariable String slug) {
        TemplateStatsDto stats = marketplaceService.getTemplateStats(slug);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/templates/featured")
    @Operation(summary = "Get featured templates", description = "Get currently featured templates")
    public ResponseEntity<List<TemplateDto>> getFeaturedTemplates() {
        List<TemplateDto> templates = marketplaceService.getFeaturedTemplates();
        return ResponseEntity.ok(templates);
    }

    @GetMapping("/templates/trending")
    @Operation(summary = "Get trending templates", description = "Get trending templates for a period")
    public ResponseEntity<List<TemplateDto>> getTrendingTemplates(
            @RequestParam(defaultValue = "week") String period) {
        List<TemplateDto> templates = marketplaceService.getTrendingTemplates(period);
        return ResponseEntity.ok(templates);
    }

    @GetMapping("/categories")
    @Operation(summary = "Get categories", description = "Get all template categories with counts")
    public ResponseEntity<List<CategoryDto>> getCategories() {
        List<CategoryDto> categories = marketplaceService.getCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/tags")
    @Operation(summary = "Get popular tags", description = "Get popular tags used in templates")
    public ResponseEntity<List<TagDto>> getPopularTags(
            @RequestParam(defaultValue = "50") int limit) {
        List<TagDto> tags = marketplaceService.getPopularTags(limit);
        return ResponseEntity.ok(tags);
    }

    // Organization endpoints

    @PostMapping("/organizations")
    @PreAuthorize("hasRole('DEVELOPER')")
    @Operation(summary = "Create organization", description = "Create a new organization")
    public ResponseEntity<OrganizationDto> createOrganization(
            @Valid @RequestBody CreateOrganizationRequest request) {
        OrganizationDto org = organizationService.createOrganization(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(org);
    }

    @GetMapping("/organizations/ {slug}")
    @Operation(summary = "Get organization", description = "Get organization details")
    public ResponseEntity<OrganizationDetailDto> getOrganization(
            @PathVariable String slug) {
        OrganizationDetailDto org = organizationService.getOrganization(slug);
        return ResponseEntity.ok(org);
    }

    @PutMapping("/organizations/ {slug}")
    @PreAuthorize("hasRole('DEVELOPER')")
    @Operation(summary = "Update organization", description = "Update organization details")
    public ResponseEntity<OrganizationDto> updateOrganization(
            @PathVariable String slug,
            @Valid @RequestBody UpdateOrganizationRequest request) {
        OrganizationDto org = organizationService.updateOrganization(slug, request);
        return ResponseEntity.ok(org);
    }

    @PostMapping("/organizations/ {slug}/members")
    @PreAuthorize("hasRole('DEVELOPER')")
    @Operation(summary = "Add member", description = "Add a member to the organization")
    public ResponseEntity<Void> addMember(
            @PathVariable String slug,
            @Valid @RequestBody AddMemberRequest request) {
        organizationService.addMember(slug, request.getEmail());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/organizations/ {slug}/members/ {userId}")
    @PreAuthorize("hasRole('DEVELOPER')")
    @Operation(summary = "Remove member", description = "Remove a member from the organization")
    public ResponseEntity<Void> removeMember(
            @PathVariable String slug,
            @PathVariable UUID userId) {
        organizationService.removeMember(slug, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/organizations/ {slug}/templates")
    @Operation(summary = "Get organization templates", description = "Get all templates by an organization")
    public ResponseEntity<Page<TemplateDto>> getOrganizationTemplates(
            @PathVariable String slug,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<TemplateDto> templates = organizationService.getOrganizationTemplates(slug, pageable);
        return ResponseEntity.ok(templates);
    }

    // User endpoints

    @GetMapping("/my/templates")
    @PreAuthorize("hasRole('DEVELOPER')")
    @Operation(summary = "Get my templates", description = "Get templates created by the current user")
    public ResponseEntity<Page<TemplateDto>> getMyTemplates(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<TemplateDto> templates = marketplaceService.getUserTemplates(pageable);
        return ResponseEntity.ok(templates);
    }

    @GetMapping("/my/installations")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get my installations", description = "Get templates installed by the current user")
    public ResponseEntity<Page<InstallationDto>> getMyInstallations(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<InstallationDto> installations = marketplaceService.getUserInstallations(pageable);
        return ResponseEntity.ok(installations);
    }

    @DeleteMapping("/my/installations/ {installationId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Uninstall template", description = "Uninstall a template")
    public ResponseEntity<Void> uninstallTemplate(
            @PathVariable UUID installationId) {
        marketplaceService.uninstallTemplate(installationId);
        return ResponseEntity.noContent().build();
    }

    // Admin endpoints

    @PostMapping("/admin/templates/ {slug}/certify")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Certify template", description = "Mark a template as certified")
    public ResponseEntity<Void> certifyTemplate(
            @PathVariable String slug) {
        marketplaceService.certifyTemplate(slug);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/admin/templates/ {slug}/feature")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Feature template", description = "Mark a template as featured")
    public ResponseEntity<Void> featureTemplate(
            @PathVariable String slug,
            @RequestBody FeatureTemplateRequest request) {
        marketplaceService.featureTemplate(slug, request.getDuration());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/admin/organizations/ {slug}/verify")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Verify organization", description = "Mark an organization as verified")
    public ResponseEntity<Void> verifyOrganization(
            @PathVariable String slug) {
        organizationService.verifyOrganization(slug);
        return ResponseEntity.noContent().build();
    }
}
