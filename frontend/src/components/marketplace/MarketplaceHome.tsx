import React, { useState } from 'react';
import {
    Container,
    Grid,
    Typography,
    Box,
    TextField,
    InputAdornment,
    Tabs,
    Tab,
    Card,
    CardContent,
    Chip,
    Button,
    IconButton,
    Menu,
    MenuItem,
    Paper,
    Skeleton,
    Alert,
    LinearProgress,
} from '@mui/material';
import {
    Search as SearchIcon,
    TrendingUp as TrendingIcon,
    Star as StarIcon,
    Download as DownloadIcon,
    FilterList as FilterIcon,
    Add as AddIcon,
    Category as CategoryIcon,
    LocalOffer as TagIcon,
    Business as BusinessIcon,
} from '@mui/icons-material';
import { useQuery } from 'react-query';
import { useNavigate } from 'react-router-dom';
import { marketplaceApi } from '../../services/marketplaceService';
import TemplateCard from './TemplateCard';
import CategoryFilter from './CategoryFilter';
import TemplateFilters from './TemplateFilters';
import FeaturedCarousel from './FeaturedCarousel';

interface TabPanelProps {
    children?: React.ReactNode;
    index: number;
    value: number;
}

function TabPanel(props: TabPanelProps) {
    const { children, value, index, ...other } = props;
    return (
        <div
            role="tabpanel"
            hidden={value !== index}
            id={`marketplace-tabpanel-${index}`}
            aria-labelledby={`marketplace-tab-${index}`}
            {...other}
        >
            {value === index && <Box sx={{ py: 3 }}>{children}</Box>}
        </div>
    );
}

export default function MarketplaceHome() {
    const navigate = useNavigate();
    const [searchQuery, setSearchQuery] = useState('');
    const [activeTab, setActiveTab] = useState(0);
    const [selectedCategory, setSelectedCategory] = useState<string | null>(null);
    const [selectedTags, setSelectedTags] = useState<string[]>([]);
    const [filterAnchorEl, setFilterAnchorEl] = useState<null | HTMLElement>(null);
    const [filters, setFilters] = useState({
        type: null as string | null,
        minRating: null as number | null,
        certifiedOnly: false,
        sortBy: 'popular' as 'popular' | 'recent' | 'rating' | 'downloads',
    });

    // Fetch featured templates
    const { data: featuredTemplates, isLoading: loadingFeatured } = useQuery(
        'featured-templates',
        marketplaceApi.getFeaturedTemplates
    );

    // Fetch templates based on active tab and filters
    const { data: templatesPage, isLoading: loadingTemplates } = useQuery(
        ['templates', activeTab, searchQuery, selectedCategory, selectedTags, filters],
        () => {
            const params = {
                query: searchQuery || undefined,
                category: selectedCategory || undefined,
                tags: selectedTags.length > 0 ? selectedTags : undefined,
                type: filters.type || undefined,
                minRating: filters.minRating || undefined,
                certifiedOnly: filters.certifiedOnly || undefined,
            };

            switch (activeTab) {
                case 1: // New
                    return marketplaceApi.searchTemplates({ ...params, sortBy: 'recent' });
                case 2: // Top Rated
                    return marketplaceApi.searchTemplates({ ...params, sortBy: 'rating' });
                case 3: // Most Downloaded
                    return marketplaceApi.searchTemplates({ ...params, sortBy: 'downloads' });
                default: // Popular
                    return marketplaceApi.searchTemplates({ ...params, sortBy: 'popular' });
            }
        },
        {
            keepPreviousData: true,
        }
    );

    // Fetch categories
    const { data: categories } = useQuery('template-categories', marketplaceApi.getCategories);

    // Fetch popular tags
    const { data: popularTags } = useQuery('popular-tags', () => marketplaceApi.getPopularTags(20));

    const handleSearch = (event: React.ChangeEvent<HTMLInputElement>) => {
        setSearchQuery(event.target.value);
    };

    const handleTabChange = (event: React.SyntheticEvent, newValue: number) => {
        setActiveTab(newValue);
    };

    const handleCreateTemplate = () => {
        navigate('/marketplace/templates/new');
    };

    const handleTemplateClick = (slug: string) => {
        navigate(`/marketplace/templates/${slug}`);
    };

    const handleFilterMenuOpen = (event: React.MouseEvent<HTMLElement>) => {
        setFilterAnchorEl(event.currentTarget);
    };

    const handleFilterMenuClose = () => {
        setFilterAnchorEl(null);
    };

    return (
        <Container maxWidth="xl" sx={{ py: 4 }}>
            {/* Header */}
            <Box sx={{ mb: 4 }}>
                <Grid container spacing={3} alignItems="center">
                    <Grid item xs={12} md={6}>
                        <Typography variant="h3" gutterBottom fontWeight="bold">
                            Template Marketplace
                        </Typography>
                        <Typography variant="subtitle1" color="text.secondary">
                            Discover and share integration flow templates
                        </Typography>
                    </Grid>
                    <Grid item xs={12} md={6} sx={{ textAlign: { md: 'right' } }}>
                        <Button
                            variant="contained"
                            startIcon={<AddIcon />}
                            onClick={handleCreateTemplate}
                            size="large"
                        >
                            Publish Template
                        </Button>
                    </Grid>
                </Grid>
            </Box>

            {/* Featured Templates */}
            {loadingFeatured ? (
                <Skeleton variant="rectangular" height={300} sx={{ mb: 4, borderRadius: 2 }} />
            ) : featuredTemplates && featuredTemplates.length > 0 ? (
                <Box sx={{ mb: 4 }}>
                    <FeaturedCarousel templates={featuredTemplates} onTemplateClick={handleTemplateClick} />
                </Box>
            ) : null}

            {/* Search and Filters */}
            <Box sx={{ mb: 4 }}>
                <Grid container spacing={2} alignItems="center">
                    <Grid item xs={12} md={6}>
                        <TextField
                            fullWidth
                            placeholder="Search templates..."
                            value={searchQuery}
                            onChange={handleSearch}
                            InputProps={{
                                startAdornment: (
                                    <InputAdornment position="start">
                                        <SearchIcon />
                                    </InputAdornment>
                                ),
                            }}
                        />
                    </Grid>
                    <Grid item xs={12} md={6} sx={{ display: 'flex', gap: 1 }}>
                        <Button
                            startIcon={<CategoryIcon />}
                            onClick={() => setSelectedCategory(null)}
                            variant={selectedCategory === null ? 'contained' : 'outlined'}
                        >
                            All Categories
                        </Button>
                        <IconButton onClick={handleFilterMenuOpen}>
                            <FilterIcon />
                        </IconButton>
                        <Menu
                            anchorEl={filterAnchorEl}
                            open={Boolean(filterAnchorEl)}
                            onClose={handleFilterMenuClose}
                        >
                            <TemplateFilters filters={filters} onFiltersChange={setFilters} />
                        </Menu>
                    </Grid>
                </Grid>

                {/* Category Pills */}
                {categories && (
                    <Box sx={{ mt: 2, display: 'flex', gap: 1, flexWrap: 'wrap' }}>
                        {categories.map((category) => (
                            <Chip
                                key={category.value}
                                label={`${category.label} (${category.count})`}
                                onClick={() => setSelectedCategory(category.value)}
                                color={selectedCategory === category.value ? 'primary' : 'default'}
                                variant={selectedCategory === category.value ? 'filled' : 'outlined'}
                            />
                        ))}
                    </Box>
                )}

                {/* Popular Tags */}
                {popularTags && popularTags.length > 0 && (
                    <Box sx={{ mt: 2 }}>
                        <Typography variant="caption" color="text.secondary" sx={{ mb: 1, display: 'block' }}>
                            Popular tags:
                        </Typography>
                        <Box sx={{ display: 'flex', gap: 0.5, flexWrap: 'wrap' }}>
                            {popularTags.map((tag) => (
                                <Chip
                                    key={tag.name}
                                    label={tag.name}
                                    size="small"
                                    icon={<TagIcon />}
                                    onClick={() => {
                                        setSelectedTags((prev) =>
                                            prev.includes(tag.name)
                                                ? prev.filter((t) => t !== tag.name)
                                                : [...prev, tag.name]
                                        );
                                    }}
                                    color={selectedTags.includes(tag.name) ? 'primary' : 'default'}
                                    variant={selectedTags.includes(tag.name) ? 'filled' : 'outlined'}
                                />
                            ))}
                        </Box>
                    </Box>
                )}
            </Box>

            {/* Tabs */}
            <Box sx={{ borderBottom: 1, borderColor: 'divider', mb: 3 }}>
                <Tabs value={activeTab} onChange={handleTabChange} aria-label="marketplace tabs">
                    <Tab icon={<TrendingIcon />} label="Popular" iconPosition="start" />
                    <Tab icon={<AddIcon />} label="New" iconPosition="start" />
                    <Tab icon={<StarIcon />} label="Top Rated" iconPosition="start" />
                    <Tab icon={<DownloadIcon />} label="Most Downloaded" iconPosition="start" />
                </Tabs>
            </Box>

            {/* Loading State */}
            {loadingTemplates && <LinearProgress sx={{ mb: 2 }} />}

            {/* Template Grid */}
            <TabPanel value={activeTab} index={activeTab}>
                {templatesPage && templatesPage.content.length > 0 ? (
                    <Grid container spacing={3}>
                        {templatesPage.content.map((template) => (
                            <Grid item xs={12} sm={6} md={4} lg={3} key={template.id}>
                                <TemplateCard
                                    template={template}
                                    onClick={() => handleTemplateClick(template.slug)}
                                />
                            </Grid>
                        ))}
                    </Grid>
                ) : (
                    <Paper sx={{ p: 4, textAlign: 'center' }}>
                        <Typography variant="h6" gutterBottom>
                            No templates found
                        </Typography>
                        <Typography color="text.secondary">
                            {searchQuery
                                ? 'Try adjusting your search or filters'
                                : 'Be the first to publish a template!'}
                        </Typography>
                        {!searchQuery && (
                            <Button
                                variant="contained"
                                startIcon={<AddIcon />}
                                onClick={handleCreateTemplate}
                                sx={{ mt: 2 }}
                            >
                                Publish Template
                            </Button>
                        )}
                    </Paper>
                )}
            </TabPanel>

            {/* Stats Summary */}
            <Box sx={{ mt: 6, p: 3, bgcolor: 'background.paper', borderRadius: 2 }}>
                <Grid container spacing={3} textAlign="center">
                    <Grid item xs={12} sm={3}>
                        <Typography variant="h4" color="primary">
                            {templatesPage?.totalElements || 0}
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                            Total Templates
                        </Typography>
                    </Grid>
                    <Grid item xs={12} sm={3}>
                        <Typography variant="h4" color="primary">
                            {categories?.length || 0}
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                            Categories
                        </Typography>
                    </Grid>
                    <Grid item xs={12} sm={3}>
                        <Typography variant="h4" color="primary">
                            {popularTags?.length || 0}+
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                            Active Tags
                        </Typography>
                    </Grid>
                    <Grid item xs={12} sm={3}>
                        <Typography variant="h4" color="primary">
                            <BusinessIcon sx={{ fontSize: 'inherit' }} />
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                            Organizations
                        </Typography>
                    </Grid>
                </Grid>
            </Box>
        </Container>
    );
}