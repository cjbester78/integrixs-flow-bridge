import React from 'react';
import {
    Card,
    CardContent,
    CardMedia,
    CardActionArea,
    Typography,
    Box,
    Chip,
    Rating,
    Stack,
    Avatar,
    Tooltip,
    IconButton,
} from '@mui/material';
import {
    Download as DownloadIcon,
    Star as StarIcon,
    Verified as VerifiedIcon,
    TrendingUp as TrendingIcon,
    Business as BusinessIcon,
} from '@mui/icons-material';
import { formatDistanceToNow } from 'date-fns';
import { TemplateDto } from '../../types/marketplace';

interface TemplateCardProps {
    template: TemplateDto;
    onClick: () => void;
}

export default function TemplateCard({ template, onClick }: TemplateCardProps) {
    const getCategoryColor = (category: string): 'default' | 'primary' | 'secondary' | 'success' | 'info' | 'warning' => {
        const colors: Record<string, any> = {
            DATA_INTEGRATION: 'primary',
            API_INTEGRATION: 'secondary',
            FILE_PROCESSING: 'info',
            MESSAGE_PROCESSING: 'success',
            DATABASE_SYNC: 'warning',
            CLOUD_INTEGRATION: 'primary',
            IOT_INTEGRATION: 'secondary',
            SECURITY: 'error',
            MONITORING: 'info',
            TRANSFORMATION: 'success',
            ORCHESTRATION: 'warning',
            UTILITY: 'default',
            OTHER: 'default',
        };
        return colors[category] || 'default';
    };

    const formatCategory = (category: string) => {
        return category
            .split('_')
            .map((word) => word.charAt(0) + word.slice(1).toLowerCase())
            .join(' ');
    };

    const formatDownloadCount = (count: number) => {
        if (count >= 1000000) {
            return `${(count / 1000000).toFixed(1)}M`;
        }
        if (count >= 1000) {
            return `${(count / 1000).toFixed(1)}K`;
        }
        return count.toString();
    };

    return (
        <Card
            sx={{
                height: '100%',
                display: 'flex',
                flexDirection: 'column',
                transition: 'all 0.3s',
                '&:hover': {
                    transform: 'translateY(-4px)',
                    boxShadow: 4,
                },
            }}
        >
            <CardActionArea onClick={onClick} sx={{ flexGrow: 1 }}>
                {template.iconUrl ? (
                    <CardMedia
                        component="img"
                        height="160"
                        image={template.iconUrl}
                        alt={template.name}
                        sx={{ objectFit: 'contain', p: 2, bgcolor: 'grey.50' }}
                    />
                ) : (
                    <Box
                        sx={{
                            height: 160,
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            bgcolor: 'grey.50',
                        }}
                    >
                        <Typography variant="h1" color="text.disabled">
                            {template.name.charAt(0).toUpperCase()}
                        </Typography>
                    </Box>
                )}
                <CardContent sx={{ flexGrow: 1 }}>
                    <Box sx={{ display: 'flex', alignItems: 'flex-start', mb: 1 }}>
                        <Typography variant="h6" component="h3" sx={{ flexGrow: 1, mr: 1 }}>
                            {template.name}
                        </Typography>
                        {template.certified && (
                            <Tooltip title="Certified Template">
                                <VerifiedIcon color="primary" fontSize="small" />
                            </Tooltip>
                        )}
                        {template.featured && (
                            <Tooltip title="Featured Template">
                                <TrendingIcon color="warning" fontSize="small" />
                            </Tooltip>
                        )}
                    </Box>

                    <Typography
                        variant="body2"
                        color="text.secondary"
                        sx={{
                            mb: 2,
                            overflow: 'hidden',
                            textOverflow: 'ellipsis',
                            display: '-webkit-box',
                            WebkitLineClamp: 2,
                            WebkitBoxOrient: 'vertical',
                        }}
                    >
                        {template.description}
                    </Typography>

                    <Stack spacing={1}>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                            <Chip
                                label={formatCategory(template.category)}
                                size="small"
                                color={getCategoryColor(template.category)}
                            />
                            <Chip
                                label={template.type}
                                size="small"
                                variant="outlined"
                            />
                        </Box>

                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                            <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                                <Rating
                                    value={template.averageRating}
                                    readOnly
                                    size="small"
                                    precision={0.5}
                                />
                                <Typography variant="caption" color="text.secondary">
                                    ({template.ratingCount})
                                </Typography>
                            </Box>
                            <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                                <DownloadIcon sx={{ fontSize: 16, color: 'text.secondary' }} />
                                <Typography variant="caption" color="text.secondary">
                                    {formatDownloadCount(template.downloadCount)}
                                </Typography>
                            </Box>
                        </Box>

                        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mt: 2 }}>
                            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                                {template.organization ? (
                                    <>
                                        <Avatar
                                            src={template.organization.logoUrl}
                                            sx={{ width: 20, height: 20 }}
                                        >
                                            <BusinessIcon sx={{ fontSize: 16 }} />
                                        </Avatar>
                                        <Typography variant="caption" color="text.secondary">
                                            {template.organization.name}
                                        </Typography>
                                    </>
                                ) : (
                                    <>
                                        <Avatar
                                            src={template.author.avatarUrl}
                                            sx={{ width: 20, height: 20 }}
                                        >
                                            {template.author.displayName?.charAt(0) || 'U'}
                                        </Avatar>
                                        <Typography variant="caption" color="text.secondary">
                                            {template.author.displayName || template.author.username}
                                        </Typography>
                                    </>
                                )}
                            </Box>
                            <Typography variant="caption" color="text.secondary">
                                v{template.version}
                            </Typography>
                        </Box>

                        {template.tags && template.tags.length > 0 && (
                            <Box sx={{ display: 'flex', gap: 0.5, flexWrap: 'wrap' }}>
                                {template.tags.slice(0, 3).map((tag) => (
                                    <Chip
                                        key={tag}
                                        label={tag}
                                        size="small"
                                        variant="outlined"
                                        sx={{ height: 20, fontSize: '0.7rem' }}
                                    />
                                ))}
                                {template.tags.length > 3 && (
                                    <Chip
                                        label={`+${template.tags.length - 3}`}
                                        size="small"
                                        variant="outlined"
                                        sx={{ height: 20, fontSize: '0.7rem' }}
                                    />
                                )}
                            </Box>
                        )}

                        <Typography variant="caption" color="text.secondary" sx={{ mt: 'auto' }}>
                            Published {formatDistanceToNow(new Date(template.publishedAt), { addSuffix: true })}
                        </Typography>
                    </Stack>
                </CardContent>
            </CardActionArea>
        </Card>
    );
}