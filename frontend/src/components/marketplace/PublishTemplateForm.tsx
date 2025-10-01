import React, { useState, useCallback } from 'react';
import {
    Container,
    Paper,
    Typography,
    Box,
    TextField,
    Select,
    MenuItem,
    FormControl,
    FormLabel,
    InputLabel,
    Button,
    Chip,
    Grid,
    Alert,
    Stepper,
    Step,
    StepLabel,
    FormHelperText,
    IconButton,
    List,
    ListItem,
    ListItemText,
    ListItemSecondaryAction,
    InputAdornment,
    FormControlLabel,
    RadioGroup,
    Radio,
    Autocomplete,
} from '@mui/material';
import {
    Add as AddIcon,
    Delete as DeleteIcon,
    Upload as UploadIcon,
    Code as CodeIcon,
    Image as ImageIcon,
    Check as CheckIcon,
    Warning as WarningIcon,
} from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { useMutation } from 'react-query';
import { useDropzone } from 'react-dropzone';
import { useFormik } from 'formik';
import * as Yup from 'yup';
import { marketplaceApi } from '../../services/marketplaceService';
import {
    TemplateCategory,
    TemplateType,
    TemplateVisibility,
    CreateTemplateRequest,
} from '../../types/marketplace';
import CodeEditor from '../common/CodeEditor';
import { useAuth } from '../../hooks/useAuth';
import { useNotification } from '../../hooks/useNotification';

const steps = ['Basic Information', 'Flow Definition', 'Configuration', 'Assets & Documentation', 'Review & Publish'];

const validationSchema = Yup.object({
    name: Yup.string()
        .required('Template name is required')
        .min(3, 'Name must be at least 3 characters')
        .max(255, 'Name must not exceed 255 characters'),
    description: Yup.string()
        .required('Description is required')
        .min(10, 'Description must be at least 10 characters')
        .max(1000, 'Description must not exceed 1000 characters'),
    detailedDescription: Yup.string()
        .max(5000, 'Detailed description must not exceed 5000 characters'),
    category: Yup.string().required('Category is required'),
    type: Yup.string().required('Type is required'),
    visibility: Yup.string().required('Visibility is required'),
    flowDefinition: Yup.string().required('Flow definition is required'),
    tags: Yup.array()
        .min(1, 'At least one tag is required')
        .max(10, 'Maximum 10 tags allowed'),
});

interface FileWithPreview extends File {
    preview?: string;
}

export default function PublishTemplateForm() {
    const navigate = useNavigate();
    const { user } = useAuth();
    const { showSuccess, showError } = useNotification();
    const [activeStep, setActiveStep] = useState(0);
    const [iconFile, setIconFile] = useState<FileWithPreview | null>(null);
    const [screenshots, setScreenshots] = useState<FileWithPreview[]>([]);
    const [newTag, setNewTag] = useState('');
    const [newRequirement, setNewRequirement] = useState('');

    const createTemplateMutation = useMutation(
        (request: CreateTemplateRequest) => marketplaceApi.createTemplate(request),
        {
            onSuccess: async (template) => {
                // Upload icon if provided
                if (iconFile) {
                    await marketplaceApi.uploadIcon(template.slug, iconFile);
                }

                // Upload screenshots
                for (const screenshot of screenshots) {
                    await marketplaceApi.addScreenshot(template.slug, screenshot);
                }

                showSuccess('Template published successfully!');
                navigate(`/marketplace/templates/${template.slug}`);
            },
            onError: (error: any) => {
                showError(error.response?.data?.message || 'Failed to publish template');
            },
        }
    );

    const formik = useFormik<CreateTemplateRequest>({
        initialValues: {
            name: '',
            description: '',
            detailedDescription: '',
            category: TemplateCategory.OTHER,
            type: TemplateType.FLOW,
            visibility: TemplateVisibility.PUBLIC,
            organizationId: undefined,
            flowDefinition: '',
            configurationSchema: '',
            tags: [],
            requirements: [],
            minPlatformVersion: '',
            maxPlatformVersion: '',
            documentationUrl: '',
            sourceRepositoryUrl: '',
        },
        validationSchema,
        onSubmit: (values) => {
            createTemplateMutation.mutate(values);
        },
    });

    const onDropIcon = useCallback((acceptedFiles: File[]) => {
        if (acceptedFiles.length > 0) {
            const file = Object.assign(acceptedFiles[0], {
                preview: URL.createObjectURL(acceptedFiles[0]),
            }) as FileWithPreview;
            setIconFile(file);
        }
    }, []);

    const onDropScreenshots = useCallback((acceptedFiles: File[]) => {
        const newFiles = acceptedFiles.map((file) =>
            Object.assign(file, {
                preview: URL.createObjectURL(file),
            })
        ) as FileWithPreview[];
        setScreenshots((prev) => [...prev, ...newFiles].slice(0, 5)); // Max 5 screenshots
    }, []);

    const { getRootProps: getIconRootProps, getInputProps: getIconInputProps } = useDropzone({
        onDrop: onDropIcon,
        accept: {
            'image/*': ['.png', '.jpg', '.jpeg', '.gif', '.svg'],
        },
        maxFiles: 1,
    });

    const { getRootProps: getScreenshotRootProps, getInputProps: getScreenshotInputProps } = useDropzone({
        onDrop: onDropScreenshots,
        accept: {
            'image/*': ['.png', '.jpg', '.jpeg', '.gif'],
        },
        maxFiles: 5,
    });

    const handleNext = () => {
        const fieldsToValidate = getFieldsForStep(activeStep);
        const errors = Object.keys(formik.errors).filter((key) => fieldsToValidate.includes(key));
        
        if (errors.length === 0) {
            setActiveStep((prev) => prev + 1);
        } else {
            formik.setTouched(
                fieldsToValidate.reduce((acc, field) => ({ ...acc, [field]: true }), {})
            );
        }
    };

    const handleBack = () => {
        setActiveStep((prev) => prev - 1);
    };

    const getFieldsForStep = (step: number): string[] => {
        switch (step) {
            case 0:
                return ['name', 'description', 'category', 'type', 'visibility'];
            case 1:
                return ['flowDefinition'];
            case 2:
                return ['tags'];
            case 3:
                return [];
            default:
                return [];
        }
    };

    const handleAddTag = () => {
        if (newTag && !formik.values.tags.includes(newTag)) {
            formik.setFieldValue('tags', [...formik.values.tags, newTag]);
            setNewTag('');
        }
    };

    const handleRemoveTag = (tag: string) => {
        formik.setFieldValue(
            'tags',
            formik.values.tags.filter((t) => t !== tag)
        );
    };

    const handleAddRequirement = () => {
        if (newRequirement) {
            formik.setFieldValue('requirements', [...(formik.values.requirements || []), newRequirement]);
            setNewRequirement('');
        }
    };

    const handleRemoveRequirement = (index: number) => {
        const updated = [...(formik.values.requirements || [])];
        updated.splice(index, 1);
        formik.setFieldValue('requirements', updated);
    };

    const renderStepContent = (step: number) => {
        switch (step) {
            case 0:
                return (
                    <Grid container spacing={3}>
                        <Grid item xs={12}>
                            <TextField
                                fullWidth
                                label="Template Name"
                                name="name"
                                value={formik.values.name}
                                onChange={formik.handleChange}
                                onBlur={formik.handleBlur}
                                error={formik.touched.name && Boolean(formik.errors.name)}
                                helperText={formik.touched.name && formik.errors.name}
                            />
                        </Grid>
                        <Grid item xs={12}>
                            <TextField
                                fullWidth
                                multiline
                                rows={3}
                                label="Short Description"
                                name="description"
                                value={formik.values.description}
                                onChange={formik.handleChange}
                                onBlur={formik.handleBlur}
                                error={formik.touched.description && Boolean(formik.errors.description)}
                                helperText={formik.touched.description && formik.errors.description}
                            />
                        </Grid>
                        <Grid item xs={12}>
                            <TextField
                                fullWidth
                                multiline
                                rows={6}
                                label="Detailed Description (optional)"
                                name="detailedDescription"
                                value={formik.values.detailedDescription}
                                onChange={formik.handleChange}
                                onBlur={formik.handleBlur}
                                error={formik.touched.detailedDescription && Boolean(formik.errors.detailedDescription)}
                                helperText={formik.touched.detailedDescription && formik.errors.detailedDescription}
                            />
                        </Grid>
                        <Grid item xs={12} md={4}>
                            <FormControl fullWidth>
                                <InputLabel>Category</InputLabel>
                                <Select
                                    name="category"
                                    value={formik.values.category}
                                    onChange={formik.handleChange}
                                    error={formik.touched.category && Boolean(formik.errors.category)}
                                >
                                    {Object.values(TemplateCategory).map((cat) => (
                                        <MenuItem key={cat} value={cat}>
                                            {cat.split('_').map(w => w.charAt(0) + w.slice(1).toLowerCase()).join(' ')}
                                        </MenuItem>
                                    ))}
                                </Select>
                                {formik.touched.category && formik.errors.category && (
                                    <FormHelperText error>{formik.errors.category}</FormHelperText>
                                )}
                            </FormControl>
                        </Grid>
                        <Grid item xs={12} md={4}>
                            <FormControl fullWidth>
                                <InputLabel>Type</InputLabel>
                                <Select
                                    name="type"
                                    value={formik.values.type}
                                    onChange={formik.handleChange}
                                    error={formik.touched.type && Boolean(formik.errors.type)}
                                >
                                    {Object.values(TemplateType).map((type) => (
                                        <MenuItem key={type} value={type}>
                                            {type}
                                        </MenuItem>
                                    ))}
                                </Select>
                                {formik.touched.type && formik.errors.type && (
                                    <FormHelperText error>{formik.errors.type}</FormHelperText>
                                )}
                            </FormControl>
                        </Grid>
                        <Grid item xs={12} md={4}>
                            <FormControl fullWidth>
                                <InputLabel>Visibility</InputLabel>
                                <Select
                                    name="visibility"
                                    value={formik.values.visibility}
                                    onChange={formik.handleChange}
                                    error={formik.touched.visibility && Boolean(formik.errors.visibility)}
                                >
                                    <MenuItem value={TemplateVisibility.PUBLIC}>Public</MenuItem>
                                    <MenuItem value={TemplateVisibility.PRIVATE}>Private</MenuItem>
                                    <MenuItem value={TemplateVisibility.ORGANIZATION}>Organization Only</MenuItem>
                                    <MenuItem value={TemplateVisibility.UNLISTED}>Unlisted</MenuItem>
                                </Select>
                                {formik.touched.visibility && formik.errors.visibility && (
                                    <FormHelperText error>{formik.errors.visibility}</FormHelperText>
                                )}
                            </FormControl>
                        </Grid>
                    </Grid>
                );

            case 1:
                return (
                    <Box>
                        <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                            Define your integration flow using YAML or JSON format. This will be the template that users install.
                        </Typography>
                        <CodeEditor
                            value={formik.values.flowDefinition}
                            onChange={(value) => formik.setFieldValue('flowDefinition', value)}
                            language="yaml"
                            height="500px"
                        />
                        {formik.touched.flowDefinition && formik.errors.flowDefinition && (
                            <FormHelperText error sx={{ mt: 1 }}>
                                {formik.errors.flowDefinition}
                            </FormHelperText>
                        )}
                    </Box>
                );

            case 2:
                return (
                    <Grid container spacing={3}>
                        <Grid item xs={12}>
                            <Box>
                                <Typography variant="subtitle1" gutterBottom>
                                    Tags
                                </Typography>
                                <Box sx={{ display: 'flex', gap: 1, mb: 2 }}>
                                    <TextField
                                        size="small"
                                        placeholder="Add a tag"
                                        value={newTag}
                                        onChange={(e) => setNewTag(e.target.value)}
                                        onKeyPress={(e) => {
                                            if (e.key === 'Enter') {
                                                e.preventDefault();
                                                handleAddTag();
                                            }
                                        }}
                                    />
                                    <Button
                                        variant="outlined"
                                        onClick={handleAddTag}
                                        disabled={!newTag || formik.values.tags.length >= 10}
                                    >
                                        Add
                                    </Button>
                                </Box>
                                <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
                                    {formik.values.tags.map((tag) => (
                                        <Chip
                                            key={tag}
                                            label={tag}
                                            onDelete={() => handleRemoveTag(tag)}
                                        />
                                    ))}
                                </Box>
                                {formik.touched.tags && formik.errors.tags && (
                                    <FormHelperText error sx={{ mt: 1 }}>
                                        {formik.errors.tags}
                                    </FormHelperText>
                                )}
                            </Box>
                        </Grid>
                        <Grid item xs={12}>
                            <Box>
                                <Typography variant="subtitle1" gutterBottom>
                                    Requirements (optional)
                                </Typography>
                                <Box sx={{ display: 'flex', gap: 1, mb: 2 }}>
                                    <TextField
                                        size="small"
                                        fullWidth
                                        placeholder="Add a requirement (e.g., 'RabbitMQ 3.9 or higher')"
                                        value={newRequirement}
                                        onChange={(e) => setNewRequirement(e.target.value)}
                                        onKeyPress={(e) => {
                                            if (e.key === 'Enter') {
                                                e.preventDefault();
                                                handleAddRequirement();
                                            }
                                        }}
                                    />
                                    <Button variant="outlined" onClick={handleAddRequirement}>
                                        Add
                                    </Button>
                                </Box>
                                <List dense>
                                    {formik.values.requirements?.map((req, index) => (
                                        <ListItem key={index}>
                                            <ListItemText primary={req} />
                                            <ListItemSecondaryAction>
                                                <IconButton
                                                    edge="end"
                                                    size="small"
                                                    onClick={() => handleRemoveRequirement(index)}
                                                >
                                                    <DeleteIcon />
                                                </IconButton>
                                            </ListItemSecondaryAction>
                                        </ListItem>
                                    ))}
                                </List>
                            </Box>
                        </Grid>
                        <Grid item xs={12} md={6}>
                            <TextField
                                fullWidth
                                label="Minimum Platform Version (optional)"
                                name="minPlatformVersion"
                                value={formik.values.minPlatformVersion}
                                onChange={formik.handleChange}
                                placeholder="e.g., 1.0.0"
                            />
                        </Grid>
                        <Grid item xs={12} md={6}>
                            <TextField
                                fullWidth
                                label="Maximum Platform Version (optional)"
                                name="maxPlatformVersion"
                                value={formik.values.maxPlatformVersion}
                                onChange={formik.handleChange}
                                placeholder="e.g., 2.0.0"
                            />
                        </Grid>
                        <Grid item xs={12}>
                            <Typography variant="subtitle1" gutterBottom>
                                Configuration Schema (optional)
                            </Typography>
                            <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                                Define a JSON schema for template configuration options
                            </Typography>
                            <CodeEditor
                                value={formik.values.configurationSchema || ''}
                                onChange={(value) => formik.setFieldValue('configurationSchema', value)}
                                language="json"
                                height="300px"
                            />
                        </Grid>
                    </Grid>
                );

            case 3:
                return (
                    <Grid container spacing={3}>
                        <Grid item xs={12} md={6}>
                            <Typography variant="subtitle1" gutterBottom>
                                Template Icon
                            </Typography>
                            <Box
                                {...getIconRootProps()}
                                sx={{
                                    border: '2px dashed',
                                    borderColor: 'divider',
                                    borderRadius: 2,
                                    p: 3,
                                    textAlign: 'center',
                                    cursor: 'pointer',
                                    '&:hover': {
                                        borderColor: 'primary.main',
                                        bgcolor: 'action.hover',
                                    },
                                }}
                            >
                                <input {...getIconInputProps()} />
                                {iconFile ? (
                                    <Box>
                                        <img
                                            src={iconFile.preview}
                                            alt="Icon preview"
                                            style={{ maxWidth: '100%', maxHeight: 150 }}
                                        />
                                        <Typography variant="body2" sx={{ mt: 1 }}>
                                            {iconFile.name}
                                        </Typography>
                                    </Box>
                                ) : (
                                    <Box>
                                        <UploadIcon sx={{ fontSize: 48, color: 'text.secondary' }} />
                                        <Typography>Drop icon here or click to upload</Typography>
                                        <Typography variant="caption" color="text.secondary">
                                            PNG, JPG, SVG up to 1MB
                                        </Typography>
                                    </Box>
                                )}
                            </Box>
                        </Grid>
                        <Grid item xs={12} md={6}>
                            <Typography variant="subtitle1" gutterBottom>
                                Screenshots (optional)
                            </Typography>
                            <Box
                                {...getScreenshotRootProps()}
                                sx={{
                                    border: '2px dashed',
                                    borderColor: 'divider',
                                    borderRadius: 2,
                                    p: 3,
                                    textAlign: 'center',
                                    cursor: 'pointer',
                                    minHeight: 150,
                                    '&:hover': {
                                        borderColor: 'primary.main',
                                        bgcolor: 'action.hover',
                                    },
                                }}
                            >
                                <input {...getScreenshotInputProps()} />
                                <ImageIcon sx={{ fontSize: 48, color: 'text.secondary' }} />
                                <Typography>Drop screenshots here or click to upload</Typography>
                                <Typography variant="caption" color="text.secondary">
                                    PNG, JPG up to 5MB each (max 5 screenshots)
                                </Typography>
                            </Box>
                            {screenshots.length > 0 && (
                                <Box sx={{ mt: 2, display: 'flex', gap: 1, flexWrap: 'wrap' }}>
                                    {screenshots.map((file, index) => (
                                        <Box
                                            key={index}
                                            sx={{
                                                position: 'relative',
                                                width: 100,
                                                height: 100,
                                                borderRadius: 1,
                                                overflow: 'hidden',
                                            }}
                                        >
                                            <img
                                                src={file.preview}
                                                alt={`Screenshot ${index + 1}`}
                                                style={{
                                                    width: '100%',
                                                    height: '100%',
                                                    objectFit: 'cover',
                                                }}
                                            />
                                            <IconButton
                                                size="small"
                                                sx={{
                                                    position: 'absolute',
                                                    top: 4,
                                                    right: 4,
                                                    bgcolor: 'background.paper',
                                                }}
                                                onClick={() => {
                                                    setScreenshots(screenshots.filter((_, i) => i !== index));
                                                }}
                                            >
                                                <DeleteIcon fontSize="small" />
                                            </IconButton>
                                        </Box>
                                    ))}
                                </Box>
                            )}
                        </Grid>
                        <Grid item xs={12}>
                            <TextField
                                fullWidth
                                label="Documentation URL (optional)"
                                name="documentationUrl"
                                value={formik.values.documentationUrl}
                                onChange={formik.handleChange}
                                placeholder="https://docs.example.com/my-template"
                                type="url"
                            />
                        </Grid>
                        <Grid item xs={12}>
                            <TextField
                                fullWidth
                                label="Source Repository URL (optional)"
                                name="sourceRepositoryUrl"
                                value={formik.values.sourceRepositoryUrl}
                                onChange={formik.handleChange}
                                placeholder="https://github.com/myorg/my-template"
                                type="url"
                            />
                        </Grid>
                    </Grid>
                );

            case 4:
                return (
                    <Box>
                        <Alert severity="info" sx={{ mb: 3 }}>
                            Please review your template information before publishing.
                        </Alert>
                        <Grid container spacing={2}>
                            <Grid item xs={12}>
                                <Typography variant="h6" gutterBottom>
                                    {formik.values.name}
                                </Typography>
                                <Typography variant="body2" color="text.secondary" paragraph>
                                    {formik.values.description}
                                </Typography>
                            </Grid>
                            <Grid item xs={12} sm={4}>
                                <Typography variant="caption" color="text.secondary">
                                    Category
                                </Typography>
                                <Typography>{formik.values.category}</Typography>
                            </Grid>
                            <Grid item xs={12} sm={4}>
                                <Typography variant="caption" color="text.secondary">
                                    Type
                                </Typography>
                                <Typography>{formik.values.type}</Typography>
                            </Grid>
                            <Grid item xs={12} sm={4}>
                                <Typography variant="caption" color="text.secondary">
                                    Visibility
                                </Typography>
                                <Typography>{formik.values.visibility}</Typography>
                            </Grid>
                            <Grid item xs={12}>
                                <Typography variant="caption" color="text.secondary">
                                    Tags
                                </Typography>
                                <Box sx={{ display: 'flex', gap: 0.5, flexWrap: 'wrap', mt: 1 }}>
                                    {formik.values.tags.map((tag) => (
                                        <Chip key={tag} label={tag} size="small" />
                                    ))}
                                </Box>
                            </Grid>
                            {formik.values.requirements && formik.values.requirements.length > 0 && (
                                <Grid item xs={12}>
                                    <Typography variant="caption" color="text.secondary">
                                        Requirements
                                    </Typography>
                                    <List dense>
                                        {formik.values.requirements.map((req, index) => (
                                            <ListItem key={index}>
                                                <ListItemText primary={req} />
                                            </ListItem>
                                        ))}
                                    </List>
                                </Grid>
                            )}
                            <Grid item xs={12}>
                                <Typography variant="caption" color="text.secondary">
                                    Flow Definition
                                </Typography>
                                <Paper variant="outlined" sx={{ mt: 1, p: 2, maxHeight: 200, overflow: 'auto' }}>
                                    <pre style={{ margin: 0, fontSize: '0.875rem' }}>
                                        {formik.values.flowDefinition.substring(0, 500)}
                                        {formik.values.flowDefinition.length > 500 && '...'}
                                    </pre>
                                </Paper>
                            </Grid>
                            <Grid item xs={12}>
                                <Alert severity="warning">
                                    <Typography variant="body2">
                                        By publishing this template, you agree to make it available according to
                                        the selected visibility settings. Public templates can be discovered and
                                        installed by all users.
                                    </Typography>
                                </Alert>
                            </Grid>
                        </Grid>
                    </Box>
                );

            default:
                return null;
        }
    };

    return (
        <Container maxWidth="lg" sx={{ py: 4 }}>
            <Paper sx={{ p: 3 }}>
                <Typography variant="h4" gutterBottom>
                    Publish Template
                </Typography>
                <Typography variant="body1" color="text.secondary" paragraph>
                    Share your integration flow template with the community
                </Typography>

                <Stepper activeStep={activeStep} sx={{ my: 4 }}>
                    {steps.map((label) => (
                        <Step key={label}>
                            <StepLabel>{label}</StepLabel>
                        </Step>
                    ))}
                </Stepper>

                <form onSubmit={formik.handleSubmit}>
                    {renderStepContent(activeStep)}

                    <Box sx={{ display: 'flex', justifyContent: 'space-between', mt: 4 }}>
                        <Button
                            disabled={activeStep === 0}
                            onClick={handleBack}
                            sx={{ mr: 1 }}
                        >
                            Back
                        </Button>
                        <Box>
                            <Button onClick={() => navigate('/marketplace')} sx={{ mr: 1 }}>
                                Cancel
                            </Button>
                            {activeStep === steps.length - 1 ? (
                                <Button
                                    type="submit"
                                    variant="contained"
                                    disabled={createTemplateMutation.isLoading}
                                    startIcon={createTemplateMutation.isLoading ? null : <CheckIcon />}
                                >
                                    {createTemplateMutation.isLoading ? 'Publishing...' : 'Publish Template'}
                                </Button>
                            ) : (
                                <Button variant="contained" onClick={handleNext}>
                                    Next
                                </Button>
                            )}
                        </Box>
                    </Box>
                </form>
            </Paper>
        </Container>
    );
}