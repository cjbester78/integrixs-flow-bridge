export interface TemplateDto {
    id: string;
    slug: string;
    name: string;
    description: string;
    category: TemplateCategory;
    type: TemplateType;
    author: AuthorDto;
    organization?: OrganizationDto;
    version: string;
    iconUrl?: string;
    tags: string[];
    downloadCount: number;
    installCount: number;
    averageRating: number;
    ratingCount: number;
    certified: boolean;
    featured: boolean;
    publishedAt: string;
}

export interface TemplateDetailDto extends TemplateDto {
    detailedDescription?: string;
    documentationUrl?: string;
    sourceRepositoryUrl?: string;
    screenshots: string[];
    requirements: string[];
    minPlatformVersion?: string;
    maxPlatformVersion?: string;
    versions: TemplateVersionDto[];
    dependencies: TemplateDto[];
    flowDefinition: string;
    configurationSchema?: string;
    updatedAt: string;
}

export interface TemplateVersionDto {
    id: string;
    version: string;
    releaseNotes?: string;
    stable: boolean;
    latest: boolean;
    publishedAt: string;
    deprecated: boolean;
    deprecationMessage?: string;
}

export interface AuthorDto {
    id: string;
    username: string;
    displayName?: string;
    avatarUrl?: string;
}

export interface OrganizationDto {
    id: string;
    slug: string;
    name: string;
    logoUrl?: string;
    verified: boolean;
}

export interface OrganizationDetailDto extends OrganizationDto {
    description?: string;
    websiteUrl?: string;
    email: string;
    owner: AuthorDto;
    memberCount: number;
    templateCount: number;
    createdAt: string;
}

export interface CommentDto {
    id: string;
    author: AuthorDto;
    content: string;
    authorResponse: boolean;
    pinned: boolean;
    postedAt: string;
    editedAt?: string;
    likeCount: number;
    replies: CommentDto[];
    liked?: boolean;
}

export interface TemplateStatsDto {
    downloadCount: number;
    installCount: number;
    averageRating: number;
    ratingCount: number;
    versionCount: number;
    commentCount: number;
    lastUpdated: string;
}

export interface CategoryDto {
    value: string;
    label: string;
    count: number;
    icon?: string;
}

export interface TagDto {
    name: string;
    count: number;
}

export interface Page<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
    first: boolean;
    last: boolean;
    empty: boolean;
}

// Request types

export interface TemplateSearchRequest {
    query?: string;
    category?: TemplateCategory;
    type?: TemplateType;
    tags?: string[];
    minRating?: number;
    certifiedOnly?: boolean;
    authorId?: string;
    organizationId?: string;
    sortBy?: 'popular' | 'recent' | 'rating' | 'downloads';
    page?: number;
    size?: number;
}

export interface CreateTemplateRequest {
    name: string;
    description: string;
    detailedDescription?: string;
    category: TemplateCategory;
    type: TemplateType;
    visibility: TemplateVisibility;
    organizationId?: string;
    flowDefinition: string;
    configurationSchema?: string;
    tags: string[];
    requirements?: string[];
    minPlatformVersion?: string;
    maxPlatformVersion?: string;
    documentationUrl?: string;
    sourceRepositoryUrl?: string;
}

export interface UpdateTemplateRequest {
    name?: string;
    description?: string;
    detailedDescription?: string;
    category?: TemplateCategory;
    tags?: string[];
    requirements?: string[];
    visibility?: TemplateVisibility;
    documentationUrl?: string;
    sourceRepositoryUrl?: string;
}

export interface PublishVersionRequest {
    version: string;
    flowDefinition: string;
    releaseNotes?: string;
    stable?: boolean;
    minPlatformVersion?: string;
    maxPlatformVersion?: string;
}

export interface InstallTemplateRequest {
    name?: string;
    version?: string;
    configuration?: Record<string, string>;
    organizationId?: string;
    enableAutoUpdate?: boolean;
}

export interface InstallationResultDto {
    success: boolean;
    flowId?: string;
    installationId?: string;
    message: string;
    errors?: string[];
}

export interface RateTemplateRequest {
    rating: number;
    review?: string;
}

export interface AddCommentRequest {
    content: string;
    parentCommentId?: string;
}

// Enums

export enum TemplateCategory {
    DATA_INTEGRATION = 'DATA_INTEGRATION',
    API_INTEGRATION = 'API_INTEGRATION',
    FILE_PROCESSING = 'FILE_PROCESSING',
    MESSAGE_PROCESSING = 'MESSAGE_PROCESSING',
    DATABASE_SYNC = 'DATABASE_SYNC',
    CLOUD_INTEGRATION = 'CLOUD_INTEGRATION',
    IOT_INTEGRATION = 'IOT_INTEGRATION',
    SECURITY = 'SECURITY',
    MONITORING = 'MONITORING',
    TRANSFORMATION = 'TRANSFORMATION',
    ORCHESTRATION = 'ORCHESTRATION',
    UTILITY = 'UTILITY',
    OTHER = 'OTHER',
}

export enum TemplateType {
    FLOW = 'FLOW',
    PATTERN = 'PATTERN',
    CONNECTOR = 'CONNECTOR',
    TRANSFORMATION = 'TRANSFORMATION',
    ORCHESTRATION = 'ORCHESTRATION',
    SNIPPET = 'SNIPPET',
}

export enum TemplateVisibility {
    PUBLIC = 'PUBLIC',
    PRIVATE = 'PRIVATE',
    ORGANIZATION = 'ORGANIZATION',
    UNLISTED = 'UNLISTED',
}