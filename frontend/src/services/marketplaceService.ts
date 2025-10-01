import apiClient from './apiClient';
import {
    TemplateDto,
    TemplateDetailDto,
    CreateTemplateRequest,
    UpdateTemplateRequest,
    InstallTemplateRequest,
    InstallationResultDto,
    RateTemplateRequest,
    AddCommentRequest,
    CommentDto,
    TemplateSearchRequest,
    TemplateStatsDto,
    CategoryDto,
    TagDto,
    OrganizationDto,
    OrganizationDetailDto,
    Page,
} from '../types/marketplace';

class MarketplaceService {
    private basePath = '/marketplace';

    // Template endpoints
    async searchTemplates(params: TemplateSearchRequest): Promise<Page<TemplateDto>> {
        const response = await apiClient.get(`${this.basePath}/templates`, { params });
        return response.data;
    }

    async getTemplateDetails(slug: string): Promise<TemplateDetailDto> {
        const response = await apiClient.get(`${this.basePath}/templates/${slug}`);
        return response.data;
    }

    async createTemplate(request: CreateTemplateRequest): Promise<TemplateDto> {
        const response = await apiClient.post(`${this.basePath}/templates`, request);
        return response.data;
    }

    async updateTemplate(slug: string, request: UpdateTemplateRequest): Promise<TemplateDto> {
        const response = await apiClient.put(`${this.basePath}/templates/${slug}`, request);
        return response.data;
    }

    async uploadIcon(slug: string, file: File): Promise<void> {
        const formData = new FormData();
        formData.append('file', file);
        await apiClient.post(`${this.basePath}/templates/${slug}/icon`, formData, {
            headers: { 'Content-Type': 'multipart/form-data' },
        });
    }

    async addScreenshot(slug: string, file: File): Promise<void> {
        const formData = new FormData();
        formData.append('file', file);
        await apiClient.post(`${this.basePath}/templates/${slug}/screenshots`, formData, {
            headers: { 'Content-Type': 'multipart/form-data' },
        });
    }

    async installTemplate(slug: string, request: InstallTemplateRequest): Promise<InstallationResultDto> {
        const response = await apiClient.post(`${this.basePath}/templates/${slug}/install`, request);
        return response.data;
    }

    async rateTemplate(slug: string, request: RateTemplateRequest): Promise<void> {
        await apiClient.post(`${this.basePath}/templates/${slug}/rate`, request);
    }

    async addComment(slug: string, request: AddCommentRequest): Promise<CommentDto> {
        const response = await apiClient.post(`${this.basePath}/templates/${slug}/comments`, request);
        return response.data;
    }

    async getTemplateStats(slug: string): Promise<TemplateStatsDto> {
        const response = await apiClient.get(`${this.basePath}/templates/${slug}/stats`);
        return response.data;
    }

    async getFeaturedTemplates(): Promise<TemplateDto[]> {
        const response = await apiClient.get(`${this.basePath}/templates/featured`);
        return response.data;
    }

    async getTrendingTemplates(period: string = 'week'): Promise<TemplateDto[]> {
        const response = await apiClient.get(`${this.basePath}/templates/trending`, {
            params: { period },
        });
        return response.data;
    }

    async getCategories(): Promise<CategoryDto[]> {
        const response = await apiClient.get(`${this.basePath}/categories`);
        return response.data;
    }

    async getPopularTags(limit: number = 50): Promise<TagDto[]> {
        const response = await apiClient.get(`${this.basePath}/tags`, {
            params: { limit },
        });
        return response.data;
    }

    // Organization endpoints
    async createOrganization(request: any): Promise<OrganizationDto> {
        const response = await apiClient.post(`${this.basePath}/organizations`, request);
        return response.data;
    }

    async getOrganization(slug: string): Promise<OrganizationDetailDto> {
        const response = await apiClient.get(`${this.basePath}/organizations/${slug}`);
        return response.data;
    }

    async getOrganizationTemplates(slug: string, page: number = 0, size: number = 20): Promise<Page<TemplateDto>> {
        const response = await apiClient.get(`${this.basePath}/organizations/${slug}/templates`, {
            params: { page, size },
        });
        return response.data;
    }

    // User endpoints
    async getUserTemplates(page: number = 0, size: number = 20): Promise<Page<TemplateDto>> {
        const response = await apiClient.get(`${this.basePath}/my/templates`, {
            params: { page, size },
        });
        return response.data;
    }

    async getUserInstallations(page: number = 0, size: number = 20): Promise<Page<any>> {
        const response = await apiClient.get(`${this.basePath}/my/installations`, {
            params: { page, size },
        });
        return response.data;
    }

    async uninstallTemplate(installationId: string): Promise<void> {
        await apiClient.delete(`${this.basePath}/my/installations/${installationId}`);
    }

    // Admin endpoints
    async certifyTemplate(slug: string): Promise<void> {
        await apiClient.post(`${this.basePath}/admin/templates/${slug}/certify`);
    }

    async featureTemplate(slug: string, duration: number): Promise<void> {
        await apiClient.post(`${this.basePath}/admin/templates/${slug}/feature`, { duration });
    }

    async verifyOrganization(slug: string): Promise<void> {
        await apiClient.post(`${this.basePath}/admin/organizations/${slug}/verify`);
    }
}

export const marketplaceApi = new MarketplaceService();