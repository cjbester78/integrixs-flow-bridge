import React, { useState } from 'react';
import {
    Container,
    Grid,
    Typography,
    Box,
    Paper,
    Button,
    Tabs,
    Tab,
    Chip,
    Rating,
    Avatar,
    Divider,
    IconButton,
    List,
    ListItem,
    ListItemIcon,
    ListItemText,
    TextField,
    Dialog,
    DialogTitle,
    DialogContent,
    DialogActions,
    Alert,
    Breadcrumbs,
    Link,
    Skeleton,
    Tooltip,
    Menu,
    MenuItem,
} from '@mui/material';
import {
    Download as DownloadIcon,
    Star as StarIcon,
    Share as ShareIcon,
    Report as ReportIcon,
    Verified as VerifiedIcon,
    Code as CodeIcon,
    Description as DocumentationIcon,
    GitHub as GitHubIcon,
    Check as CheckIcon,
    Close as CloseIcon,
    Comment as CommentIcon,
    ThumbUp as ThumbUpIcon,
    MoreVert as MoreVertIcon,
    ContentCopy as CopyIcon,
    Edit as EditIcon,
    Delete as DeleteIcon,
} from '@mui/icons-material';
import { useParams, useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from 'react-query';
import { formatDistanceToNow } from 'date-fns';
import SyntaxHighlighter from 'react-syntax-highlighter';
import { docco } from 'react-syntax-highlighter/dist/esm/styles/hljs';
import { marketplaceApi } from '../../services/marketplaceService';
import { useAuth } from '../../hooks/useAuth';
import ImageGallery from './ImageGallery';
import VersionSelector from './VersionSelector';
import CommentSection from './CommentSection';
import RatingDistribution from './RatingDistribution';
import InstallDialog from './InstallDialog';

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
            id={`template-tabpanel-${index}`}
            aria-labelledby={`template-tab-${index}`}
            {...other}
        >
            {value === index && <Box sx={{ py: 3 }}>{children}</Box>}
        </div>
    );
}

export default function TemplateDetail() {
    const { slug } = useParams<{ slug: string }>();
    const navigate = useNavigate();
    const queryClient = useQueryClient();
    const { user, isAuthenticated } = useAuth();
    const [activeTab, setActiveTab] = useState(0);
    const [installDialogOpen, setInstallDialogOpen] = useState(false);
    const [ratingDialogOpen, setRatingDialogOpen] = useState(false);
    const [shareMenuAnchor, setShareMenuAnchor] = useState<null | HTMLElement>(null);
    const [moreMenuAnchor, setMoreMenuAnchor] = useState<null | HTMLElement>(null);
    const [rating, setRating] = useState(0);
    const [review, setReview] = useState('');

    // Fetch template details
    const { data: template, isLoading, error } = useQuery(
        ['template', slug],
        () => marketplaceApi.getTemplateDetails(slug!),
        {
            enabled: !!slug,
        }
    );

    // Fetch template stats
    const { data: stats } = useQuery(
        ['template-stats', slug],
        () => marketplaceApi.getTemplateStats(slug!),
        {
            enabled: !!slug,
        }
    );

    // Install template mutation
    const installMutation = useMutation(
        (config: any) => marketplaceApi.installTemplate(slug!, config),
        {
            onSuccess: (result) => {
                if (result.success) {
                    queryClient.invalidateQueries(['template-stats', slug]);
                    navigate(`/flows/${result.flowId}`);
                }
            },
        }
    );

    // Rate template mutation
    const rateMutation = useMutation(
        ({ rating, review }: { rating: number; review?: string }) =>
            marketplaceApi.rateTemplate(slug!, { rating, review }),
        {
            onSuccess: () => {
                queryClient.invalidateQueries(['template', slug]);
                queryClient.invalidateQueries(['template-stats', slug]);
                setRatingDialogOpen(false);
                setRating(0);
                setReview('');
            },
        }
    );

    const handleTabChange = (event: React.SyntheticEvent, newValue: number) => {
        setActiveTab(newValue);
    };

    const handleInstall = () => {
        if (!isAuthenticated) {
            navigate('/login?redirect=' + encodeURIComponent(window.location.pathname));
            return;
        }
        setInstallDialogOpen(true);
    };

    const handleRate = () => {
        if (!isAuthenticated) {
            navigate('/login?redirect=' + encodeURIComponent(window.location.pathname));
            return;
        }
        setRatingDialogOpen(true);
    };

    const handleShare = (type: 'link' | 'twitter' | 'linkedin') => {
        const url = window.location.href;
        const title = `Check out ${template?.name} on Integrixs Marketplace`;

        switch (type) {
            case 'link':
                navigator.clipboard.writeText(url);
                // Show success toast
                break;
            case 'twitter':
                window.open(
                    `https://twitter.com/intent/tweet?url=${encodeURIComponent(url)}&text=${encodeURIComponent(title)}`,
                    '_blank'
                );
                break;
            case 'linkedin':
                window.open(
                    `https://www.linkedin.com/sharing/share-offsite/?url=${encodeURIComponent(url)}`,
                    '_blank'
                );
                break;
        }
        setShareMenuAnchor(null);
    };

    const handleEdit = () => {
        navigate(`/marketplace/templates/${slug}/edit`);
    };

    const canEdit = user && template && (user.id === template.author.id || user.role === 'ADMIN');

    if (isLoading) {
        return (
            <Container maxWidth="xl" sx={{ py: 4 }}>
                <Grid container spacing={3}>
                    <Grid item xs={12} md={8}>
                        <Skeleton variant="rectangular" height={400} />
                    </Grid>
                    <Grid item xs={12} md={4}>
                        <Skeleton variant="rectangular" height={300} />
                    </Grid>
                </Grid>
            </Container>
        );
    }

    if (error || !template) {
        return (
            <Container maxWidth="xl" sx={{ py: 4 }}>
                <Alert severity="error">
                    Template not found or you don't have permission to view it.
                </Alert>
            </Container>
        );
    }

    return (
        <Container maxWidth="xl" sx={{ py: 4 }}>
            {/* Breadcrumbs */}
            <Breadcrumbs sx={{ mb: 2 }}>
                <Link
                    component="button"
                    variant="body2"
                    onClick={() => navigate('/marketplace')}
                    underline="hover"
                    color="inherit"
                >
                    Marketplace
                </Link>
                <Link
                    component="button"
                    variant="body2"
                    onClick={() => navigate(`/marketplace?category=${template.category}`)}
                    underline="hover"
                    color="inherit"
                >
                    {template.category.split('_').map(w => w.charAt(0) + w.slice(1).toLowerCase()).join(' ')}
                </Link>
                <Typography color="text.primary">{template.name}</Typography>
            </Breadcrumbs>

            <Grid container spacing={3}>
                {/* Main Content */}
                <Grid item xs={12} md={8}>
                    <Paper sx={{ p: 3, mb: 3 }}>
                        {/* Header */}
                        <Box sx={{ display: 'flex', alignItems: 'flex-start', mb: 3 }}>
                            {template.iconUrl ? (
                                <Avatar
                                    src={template.iconUrl}
                                    sx={{ width: 80, height: 80, mr: 2 }}
                                    variant="rounded"
                                />
                            ) : (
                                <Avatar sx={{ width: 80, height: 80, mr: 2 }} variant="rounded">
                                    <Typography variant="h3">
                                        {template.name.charAt(0).toUpperCase()}
                                    </Typography>
                                </Avatar>
                            )}
                            <Box sx={{ flexGrow: 1 }}>
                                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                                    <Typography variant="h4" component="h1">
                                        {template.name}
                                    </Typography>
                                    {template.certified && (
                                        <Tooltip title="Certified Template">
                                            <VerifiedIcon color="primary" />
                                        </Tooltip>
                                    )}
                                </Box>
                                <Typography variant="body1" color="text.secondary" paragraph>
                                    {template.description}
                                </Typography>
                                <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
                                    <Chip
                                        label={template.category.split('_').map(w => w.charAt(0) + w.slice(1).toLowerCase()).join(' ')}
                                        color="primary"
                                    />
                                    <Chip label={template.type} variant="outlined" />
                                    <Chip label={`v${template.version}`} size="small" />
                                    {template.tags.map((tag) => (
                                        <Chip key={tag} label={tag} size="small" variant="outlined" />
                                    ))}
                                </Box>
                            </Box>
                            <Box sx={{ display: 'flex', gap: 1 }}>
                                <IconButton onClick={(e) => setShareMenuAnchor(e.currentTarget)}>
                                    <ShareIcon />
                                </IconButton>
                                {canEdit && (
                                    <IconButton onClick={(e) => setMoreMenuAnchor(e.currentTarget)}>
                                        <MoreVertIcon />
                                    </IconButton>
                                )}
                            </Box>
                        </Box>

                        {/* Stats */}
                        <Grid container spacing={2} sx={{ mb: 3 }}>
                            <Grid item xs={6} sm={3}>
                                <Box sx={{ textAlign: 'center' }}>
                                    <Typography variant="h5">{stats?.downloadCount || 0}</Typography>
                                    <Typography variant="body2" color="text.secondary">
                                        Downloads
                                    </Typography>
                                </Box>
                            </Grid>
                            <Grid item xs={6} sm={3}>
                                <Box sx={{ textAlign: 'center' }}>
                                    <Typography variant="h5">{stats?.installCount || 0}</Typography>
                                    <Typography variant="body2" color="text.secondary">
                                        Installations
                                    </Typography>
                                </Box>
                            </Grid>
                            <Grid item xs={6} sm={3}>
                                <Box sx={{ textAlign: 'center' }}>
                                    <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                                        <Rating value={template.averageRating} readOnly size="small" />
                                    </Box>
                                    <Typography variant="body2" color="text.secondary">
                                        {template.ratingCount} ratings
                                    </Typography>
                                </Box>
                            </Grid>
                            <Grid item xs={6} sm={3}>
                                <Box sx={{ textAlign: 'center' }}>
                                    <Typography variant="h5">{stats?.versionCount || 1}</Typography>
                                    <Typography variant="body2" color="text.secondary">
                                        Versions
                                    </Typography>
                                </Box>
                            </Grid>
                        </Grid>

                        {/* Screenshots */}
                        {template.screenshots && template.screenshots.length > 0 && (
                            <Box sx={{ mb: 3 }}>
                                <ImageGallery images={template.screenshots} />
                            </Box>
                        )}

                        {/* Tabs */}
                        <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
                            <Tabs value={activeTab} onChange={handleTabChange}>
                                <Tab label="Overview" />
                                <Tab label="Versions" />
                                <Tab label="Reviews" />
                                <Tab label="Comments" />
                            </Tabs>
                        </Box>

                        {/* Tab Content */}
                        <TabPanel value={activeTab} index={0}>
                            {/* Detailed Description */}
                            {template.detailedDescription && (
                                <Box sx={{ mb: 4 }}>
                                    <Typography variant="h6" gutterBottom>
                                        Description
                                    </Typography>
                                    <Typography variant="body1" sx={{ whiteSpace: 'pre-wrap' }}>
                                        {template.detailedDescription}
                                    </Typography>
                                </Box>
                            )}

                            {/* Requirements */}
                            {template.requirements && template.requirements.length > 0 && (
                                <Box sx={{ mb: 4 }}>
                                    <Typography variant="h6" gutterBottom>
                                        Requirements
                                    </Typography>
                                    <List dense>
                                        {template.requirements.map((req, index) => (
                                            <ListItem key={index}>
                                                <ListItemIcon>
                                                    <CheckIcon color="success" />
                                                </ListItemIcon>
                                                <ListItemText primary={req} />
                                            </ListItem>
                                        ))}
                                    </List>
                                </Box>
                            )}

                            {/* Dependencies */}
                            {template.dependencies && template.dependencies.length > 0 && (
                                <Box sx={{ mb: 4 }}>
                                    <Typography variant="h6" gutterBottom>
                                        Dependencies
                                    </Typography>
                                    <Grid container spacing={2}>
                                        {template.dependencies.map((dep) => (
                                            <Grid item xs={12} sm={6} key={dep.id}>
                                                <Paper
                                                    variant="outlined"
                                                    sx={{ p: 2, cursor: 'pointer' }}
                                                    onClick={() => navigate(`/marketplace/templates/${dep.slug}`)}
                                                >
                                                    <Typography variant="subtitle2">{dep.name}</Typography>
                                                    <Typography variant="caption" color="text.secondary">
                                                        v{dep.version}
                                                    </Typography>
                                                </Paper>
                                            </Grid>
                                        ))}
                                    </Grid>
                                </Box>
                            )}

                            {/* Flow Preview */}
                            <Box sx={{ mb: 4 }}>
                                <Typography variant="h6" gutterBottom>
                                    Flow Definition Preview
                                </Typography>
                                <Paper variant="outlined" sx={{ overflow: 'auto', maxHeight: 400 }}>
                                    <SyntaxHighlighter language="yaml" style={docco}>
                                        {template.flowDefinition || 'No preview available'}
                                    </SyntaxHighlighter>
                                </Paper>
                            </Box>
                        </TabPanel>

                        <TabPanel value={activeTab} index={1}>
                            <VersionSelector versions={template.versions} currentVersion={template.version} />
                        </TabPanel>

                        <TabPanel value={activeTab} index={2}>
                            <RatingDistribution
                                averageRating={template.averageRating}
                                ratingCount={template.ratingCount}
                                onAddReview={handleRate}
                            />
                        </TabPanel>

                        <TabPanel value={activeTab} index={3}>
                            <CommentSection templateSlug={slug!} />
                        </TabPanel>
                    </Paper>
                </Grid>

                {/* Sidebar */}
                <Grid item xs={12} md={4}>
                    {/* Actions */}
                    <Paper sx={{ p: 3, mb: 3 }}>
                        <Button
                            variant="contained"
                            fullWidth
                            size="large"
                            startIcon={<DownloadIcon />}
                            onClick={handleInstall}
                            sx={{ mb: 2 }}
                        >
                            Install Template
                        </Button>
                        <Button
                            variant="outlined"
                            fullWidth
                            startIcon={<StarIcon />}
                            onClick={handleRate}
                        >
                            Rate & Review
                        </Button>
                    </Paper>

                    {/* Author Info */}
                    <Paper sx={{ p: 3, mb: 3 }}>
                        <Typography variant="h6" gutterBottom>
                            Publisher
                        </Typography>
                        {template.organization ? (
                            <Box
                                sx={{ display: 'flex', alignItems: 'center', cursor: 'pointer' }}
                                onClick={() => navigate(`/marketplace/organizations/${template.organization!.slug}`)}
                            >
                                <Avatar src={template.organization.logoUrl} sx={{ mr: 2 }}>
                                    {template.organization.name.charAt(0)}
                                </Avatar>
                                <Box>
                                    <Typography variant="subtitle1">
                                        {template.organization.name}
                                        {template.organization.verified && (
                                            <VerifiedIcon sx={{ fontSize: 16, ml: 0.5, verticalAlign: 'middle' }} color="primary" />
                                        )}
                                    </Typography>
                                    <Typography variant="caption" color="text.secondary">
                                        Organization
                                    </Typography>
                                </Box>
                            </Box>
                        ) : (
                            <Box
                                sx={{ display: 'flex', alignItems: 'center', cursor: 'pointer' }}
                                onClick={() => navigate(`/marketplace/authors/${template.author.username}`)}
                            >
                                <Avatar src={template.author.avatarUrl} sx={{ mr: 2 }}>
                                    {template.author.displayName?.charAt(0) || 'U'}
                                </Avatar>
                                <Box>
                                    <Typography variant="subtitle1">
                                        {template.author.displayName || template.author.username}
                                    </Typography>
                                    <Typography variant="caption" color="text.secondary">
                                        Individual Developer
                                    </Typography>
                                </Box>
                            </Box>
                        )}
                    </Paper>

                    {/* Resources */}
                    <Paper sx={{ p: 3, mb: 3 }}>
                        <Typography variant="h6" gutterBottom>
                            Resources
                        </Typography>
                        <List dense>
                            {template.documentationUrl && (
                                <ListItem
                                    component="a"
                                    href={template.documentationUrl}
                                    target="_blank"
                                    rel="noopener noreferrer"
                                >
                                    <ListItemIcon>
                                        <DocumentationIcon />
                                    </ListItemIcon>
                                    <ListItemText primary="Documentation" />
                                </ListItem>
                            )}
                            {template.sourceRepositoryUrl && (
                                <ListItem
                                    component="a"
                                    href={template.sourceRepositoryUrl}
                                    target="_blank"
                                    rel="noopener noreferrer"
                                >
                                    <ListItemIcon>
                                        <GitHubIcon />
                                    </ListItemIcon>
                                    <ListItemText primary="Source Code" />
                                </ListItem>
                            )}
                            <ListItem button onClick={() => setActiveTab(3)}>
                                <ListItemIcon>
                                    <CommentIcon />
                                </ListItemIcon>
                                <ListItemText primary={`Community (${stats?.commentCount || 0})`} />
                            </ListItem>
                        </List>
                    </Paper>

                    {/* Platform Compatibility */}
                    <Paper sx={{ p: 3 }}>
                        <Typography variant="h6" gutterBottom>
                            Platform Compatibility
                        </Typography>
                        <List dense>
                            <ListItem>
                                <ListItemIcon>
                                    <CheckIcon color="success" />
                                </ListItemIcon>
                                <ListItemText
                                    primary="Minimum Version"
                                    secondary={template.minPlatformVersion || 'Any'}
                                />
                            </ListItem>
                            {template.maxPlatformVersion && (
                                <ListItem>
                                    <ListItemIcon>
                                        <CloseIcon color="error" />
                                    </ListItemIcon>
                                    <ListItemText
                                        primary="Maximum Version"
                                        secondary={template.maxPlatformVersion}
                                    />
                                </ListItem>
                            )}
                        </List>
                        <Typography variant="caption" color="text.secondary" sx={{ mt: 2, display: 'block' }}>
                            Last updated {formatDistanceToNow(new Date(template.updatedAt), { addSuffix: true })}
                        </Typography>
                    </Paper>
                </Grid>
            </Grid>

            {/* Share Menu */}
            <Menu
                anchorEl={shareMenuAnchor}
                open={Boolean(shareMenuAnchor)}
                onClose={() => setShareMenuAnchor(null)}
            >
                <MenuItem onClick={() => handleShare('link')}>
                    <ListItemIcon>
                        <CopyIcon fontSize="small" />
                    </ListItemIcon>
                    Copy Link
                </MenuItem>
                <MenuItem onClick={() => handleShare('twitter')}>Share on Twitter</MenuItem>
                <MenuItem onClick={() => handleShare('linkedin')}>Share on LinkedIn</MenuItem>
            </Menu>

            {/* More Menu */}
            <Menu
                anchorEl={moreMenuAnchor}
                open={Boolean(moreMenuAnchor)}
                onClose={() => setMoreMenuAnchor(null)}
            >
                {canEdit && (
                    <MenuItem onClick={handleEdit}>
                        <ListItemIcon>
                            <EditIcon fontSize="small" />
                        </ListItemIcon>
                        Edit Template
                    </MenuItem>
                )}
                <MenuItem>
                    <ListItemIcon>
                        <ReportIcon fontSize="small" />
                    </ListItemIcon>
                    Report Template
                </MenuItem>
            </Menu>

            {/* Install Dialog */}
            <InstallDialog
                open={installDialogOpen}
                onClose={() => setInstallDialogOpen(false)}
                template={template}
                onInstall={installMutation.mutate}
                isInstalling={installMutation.isLoading}
            />

            {/* Rating Dialog */}
            <Dialog
                open={ratingDialogOpen}
                onClose={() => setRatingDialogOpen(false)}
                maxWidth="sm"
                fullWidth
            >
                <DialogTitle>Rate {template.name}</DialogTitle>
                <DialogContent>
                    <Box sx={{ py: 2 }}>
                        <Typography gutterBottom>How would you rate this template?</Typography>
                        <Rating
                            value={rating}
                            onChange={(event, newValue) => setRating(newValue || 0)}
                            size="large"
                            sx={{ mb: 2 }}
                        />
                        <TextField
                            fullWidth
                            multiline
                            rows={4}
                            label="Review (optional)"
                            value={review}
                            onChange={(e) => setReview(e.target.value)}
                            placeholder="Share your experience with this template..."
                        />
                    </Box>
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setRatingDialogOpen(false)}>Cancel</Button>
                    <Button
                        variant="contained"
                        onClick={() => rateMutation.mutate({ rating, review })}
                        disabled={rating === 0 || rateMutation.isLoading}
                    >
                        Submit Rating
                    </Button>
                </DialogActions>
            </Dialog>
        </Container>
    );
}