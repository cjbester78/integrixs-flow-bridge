import React, { useState } from 'react';
import {
  Box,
  TextField,
  FormControl,
  FormControlLabel,
  FormLabel,
  Switch,
  Button,
  Typography,
  Alert,
  Divider,
  Grid,
  InputLabel,
  Select,
  MenuItem,
  Chip,
  Paper,
  Tabs,
  Tab,
  FormHelperText,
  Slider,
  Accordion,
  AccordionSummary,
  AccordionDetails,
  Radio,
  RadioGroup,
  Checkbox,
  FormGroup,
  IconButton,
  Tooltip,
  Link,
} from '@mui/material';
import {
  Info as InfoIcon,
  ExpandMore as ExpandMoreIcon,
  Security as SecurityIcon,
  Settings as SettingsIcon,
  Functions as FunctionsIcon,
  Business as BusinessIcon,
  People as PeopleIcon,
  Article as ArticleIcon,
  Analytics as AnalyticsIcon,
  Speed as SpeedIcon,
  Image as ImageIcon,
  VideoLibrary as VideoIcon,
} from '@mui/icons-material';

interface LinkedInApiConfigurationProps {
  configuration: any;
  onConfigurationChange: (config: any) => void;
  onValidation: (isValid: boolean) => void;
}

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

function TabPanel(props: TabPanelProps) {
  const { children, value, index, ...other } = props;
  return (
    <div role="tabpanel" hidden={value !== index} {...other}>
      {value === index && <Box sx={{ p: 3 }}>{children}</Box>}
    </div>
  );
}

const LinkedInApiConfiguration: React.FC<LinkedInApiConfigurationProps> = ({
  configuration,
  onConfigurationChange,
  onValidation,
}) => {
  const [tabValue, setTabValue] = useState(0);
  const [errors, setErrors] = useState<any>({});

  const updateConfiguration = (path: string, value: any) => {
    const newConfig = { ...configuration };
    const keys = path.split('.');
    let current = newConfig;
    
    for (let i = 0; i < keys.length - 1; i++) {
      if (!current[keys[i]]) {
        current[keys[i]] = {};
      }
      current = current[keys[i]];
    }
    
    current[keys[keys.length - 1]] = value;
    onConfigurationChange(newConfig);
    validateConfiguration(newConfig);
  };

  const validateConfiguration = (config: any) => {
    const newErrors: any = {};

    // Required fields validation
    if (!config.clientId) {
      newErrors.clientId = 'Client ID is required';
    }
    if (!config.clientSecret) {
      newErrors.clientSecret = 'Client Secret is required';
    }
    if (!config.accessToken && !config.refreshToken) {
      newErrors.accessToken = 'Access Token or Refresh Token is required';
    }
    if (!config.memberUrn && !config.organizationId) {
      newErrors.memberUrn = 'Either Member URN or Organization ID is required';
    }

    // Limits validation
    if (config.limits) {
      if (config.limits.maxPostLength > 3000) {
        newErrors.maxPostLength = 'Maximum post length is 3000 characters';
      }
      if (config.limits.maxArticleLength > 125000) {
        newErrors.maxArticleLength = 'Maximum article length is 125000 characters';
      }
      if (config.limits.maxHashtagsPerPost > 30) {
        newErrors.maxHashtagsPerPost = 'Maximum 30 hashtags allowed per post';
      }
    }

    setErrors(newErrors);
    onValidation(Object.keys(newErrors).length === 0);
  };

  const visibilityOptions = [
    { value: 'CONNECTIONS', label: 'Connections only (1st degree)' },
    { value: 'PUBLIC', label: 'Anyone on LinkedIn' },
    { value: 'LOGGED_IN', label: 'LinkedIn members' },
    { value: 'CONTAINER', label: 'Container members only' },
  ];

  const organizationRoles = [
    'ADMIN',
    'ANALYST',
    'ASSOCIATE',
    'CONTENT_ADMIN',
    'CURATOR',
    'LEAD_GEN_FORMS_MANAGER',
    'MANAGER',
    'MESSAGING_AGENT',
    'PAID_MEDIA_ADMIN',
    'RECRUITER',
  ];

  return (
    <Box sx={{ width: '100%' }}>
      <Paper elevation={0} sx={{ borderBottom: 1, borderColor: 'divider' }}>
        <Tabs value={tabValue} onChange={(e, v) => setTabValue(v)} variant="scrollable">
          <Tab icon={<SecurityIcon />} label="Authentication" />
          <Tab icon={<BusinessIcon />} label="Account Settings" />
          <Tab icon={<FunctionsIcon />} label="Features" />
          <Tab icon={<ArticleIcon />} label="Content Settings" />
          <Tab icon={<PeopleIcon />} label="Social Features" />
          <Tab icon={<AnalyticsIcon />} label="Analytics & Insights" />
          <Tab icon={<SpeedIcon />} label="Limits & Advanced" />
        </Tabs>
      </Paper>

      <TabPanel value={tabValue} index={0}>
        <Box sx={{ mb: 3 }}>
          <Alert severity="info" icon={<InfoIcon />}>
            LinkedIn uses OAuth 2.0 for authentication. You'll need to create a LinkedIn app to get these credentials.
            Access tokens expire after 60 days and need to be refreshed.
            <Link href="https://www.linkedin.com/developers/apps" target="_blank" sx={{ ml: 1 }}>
              Create LinkedIn App
            </Link>
          </Alert>
        </Box>

        <Grid container spacing={2}>
          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              label="Client ID"
              value={configuration.clientId || ''}
              onChange={(e) => updateConfiguration('clientId', e.target.value)}
              error={!!errors.clientId}
              helperText={errors.clientId || 'Your LinkedIn app Client ID'}
              margin="normal"
              required
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              label="Client Secret"
              type="password"
              value={configuration.clientSecret || ''}
              onChange={(e) => updateConfiguration('clientSecret', e.target.value)}
              error={!!errors.clientSecret}
              helperText={errors.clientSecret}
              margin="normal"
              required
            />
          </Grid>
          <Grid item xs={12}>
            <TextField
              fullWidth
              label="Access Token"
              type="password"
              value={configuration.accessToken || ''}
              onChange={(e) => updateConfiguration('accessToken', e.target.value)}
              error={!!errors.accessToken}
              helperText={errors.accessToken || 'OAuth 2.0 access token (expires after 60 days)'}
              margin="normal"
            />
          </Grid>
          <Grid item xs={12}>
            <TextField
              fullWidth
              label="Refresh Token"
              type="password"
              value={configuration.refreshToken || ''}
              onChange={(e) => updateConfiguration('refreshToken', e.target.value)}
              helperText="Optional: Used to automatically refresh access tokens"
              margin="normal"
            />
          </Grid>
        </Grid>

        <Box sx={{ mt: 3 }}>
          <Alert severity="warning">
            <Typography variant="subtitle2" gutterBottom>
              Required OAuth 2.0 Scopes:
            </Typography>
            <ul style={{ marginTop: 8, marginBottom: 0 }}>
              <li>r_liteprofile - Read basic profile data</li>
              <li>r_emailaddress - Read email address</li>
              <li>w_member_social - Create, modify, and delete posts</li>
              <li>r_organization_social - Read organization data</li>
              <li>w_organization_social - Manage organization posts</li>
              <li>rw_organization_admin - Full organization management (if applicable)</li>
            </ul>
          </Alert>
        </Box>
      </TabPanel>

      <TabPanel value={tabValue} index={1}>
        <Typography variant="h6" gutterBottom>
          LinkedIn Account Configuration
        </Typography>
        
        <Grid container spacing={3}>
          <Grid item xs={12}>
            <TextField
              fullWidth
              label="Member URN"
              value={configuration.memberUrn || ''}
              onChange={(e) => updateConfiguration('memberUrn', e.target.value)}
              error={!!errors.memberUrn}
              helperText={errors.memberUrn || 'Your LinkedIn member URN (e.g., urn:li:person:ABC123)'}
              margin="normal"
            />
          </Grid>
          <Grid item xs={12}>
            <TextField
              fullWidth
              label="Organization ID"
              value={configuration.organizationId || ''}
              onChange={(e) => updateConfiguration('organizationId', e.target.value)}
              helperText="Organization ID for company page management (optional)"
              margin="normal"
            />
          </Grid>
          <Grid item xs={12}>
            <FormControl fullWidth margin="normal">
              <InputLabel>Default Post Visibility</InputLabel>
              <Select
                value={configuration.defaultVisibility || 'CONNECTIONS'}
                onChange={(e) => updateConfiguration('defaultVisibility', e.target.value)}
              >
                {visibilityOptions.map((option) => (
                  <MenuItem key={option.value} value={option.value}>
                    {option.label}
                  </MenuItem>
                ))}
              </Select>
              <FormHelperText>Default visibility for new posts</FormHelperText>
            </FormControl>
          </Grid>
          {configuration.organizationId && (
            <Grid item xs={12}>
              <FormControl fullWidth margin="normal">
                <InputLabel>Organization Role</InputLabel>
                <Select
                  value={configuration.organizationRole || 'CONTENT_ADMIN'}
                  onChange={(e) => updateConfiguration('organizationRole', e.target.value)}
                >
                  {organizationRoles.map((role) => (
                    <MenuItem key={role} value={role}>
                      {role.replace(/_/g, ' ')}
                    </MenuItem>
                  ))}
                </Select>
                <FormHelperText>Your role in the organization</FormHelperText>
              </FormControl>
            </Grid>
          )}
        </Grid>
      </TabPanel>

      <TabPanel value={tabValue} index={2}>
        <Typography variant="h6" gutterBottom>
          Core Features
        </Typography>
        <FormGroup>
          <Grid container spacing={2}>
            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableProfileAccess ?? true}
                    onChange={(e) => updateConfiguration('features.enableProfileAccess', e.target.checked)}
                  />
                }
                label="Profile Access"
              />
              <FormHelperText>Read and update profile information</FormHelperText>
            </Grid>
            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enablePostSharing ?? true}
                    onChange={(e) => updateConfiguration('features.enablePostSharing', e.target.checked)}
                  />
                }
                label="Post Sharing"
              />
              <FormHelperText>Create and share posts</FormHelperText>
            </Grid>
            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableArticlePublishing ?? true}
                    onChange={(e) => updateConfiguration('features.enableArticlePublishing', e.target.checked)}
                  />
                }
                label="Article Publishing"
              />
              <FormHelperText>Publish long-form articles</FormHelperText>
            </Grid>
            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableCommentManagement ?? true}
                    onChange={(e) => updateConfiguration('features.enableCommentManagement', e.target.checked)}
                  />
                }
                label="Comment Management"
              />
              <FormHelperText>Create and manage comments</FormHelperText>
            </Grid>
            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableMessaging ?? false}
                    onChange={(e) => updateConfiguration('features.enableMessaging', e.target.checked)}
                  />
                }
                label="Direct Messaging"
              />
              <FormHelperText>Send and receive messages</FormHelperText>
            </Grid>
            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableConnectionManagement ?? true}
                    onChange={(e) => updateConfiguration('features.enableConnectionManagement', e.target.checked)}
                  />
                }
                label="Connection Management"
              />
              <FormHelperText>Manage connections and invitations</FormHelperText>
            </Grid>
          </Grid>
        </FormGroup>

        <Divider sx={{ my: 3 }} />

        <Typography variant="h6" gutterBottom>
          Company Features
        </Typography>
        <FormGroup>
          <Grid container spacing={2}>
            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableCompanyPageManagement ?? true}
                    onChange={(e) => updateConfiguration('features.enableCompanyPageManagement', e.target.checked)}
                    disabled={!configuration.organizationId}
                  />
                }
                label="Company Page Management"
              />
              <FormHelperText>Manage company page content</FormHelperText>
            </Grid>
            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableEmployeeAdvocacy ?? true}
                    onChange={(e) => updateConfiguration('features.enableEmployeeAdvocacy', e.target.checked)}
                  />
                }
                label="Employee Advocacy"
              />
              <FormHelperText>Employee content sharing</FormHelperText>
            </Grid>
            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableEventCreation ?? true}
                    onChange={(e) => updateConfiguration('features.enableEventCreation', e.target.checked)}
                  />
                }
                label="Event Creation"
              />
              <FormHelperText>Create and manage events</FormHelperText>
            </Grid>
            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableNewsletters ?? true}
                    onChange={(e) => updateConfiguration('features.enableNewsletters', e.target.checked)}
                  />
                }
                label="Newsletters"
              />
              <FormHelperText>Newsletter publishing</FormHelperText>
            </Grid>
          </Grid>
        </FormGroup>

        <Divider sx={{ my: 3 }} />

        <Typography variant="h6" gutterBottom>
          Advanced Features
        </Typography>
        <FormGroup>
          <Grid container spacing={2}>
            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableScheduledPosts ?? true}
                    onChange={(e) => updateConfiguration('features.enableScheduledPosts', e.target.checked)}
                  />
                }
                label="Scheduled Posts"
              />
              <FormHelperText>Schedule posts for later</FormHelperText>
            </Grid>
            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableHashtagTracking ?? true}
                    onChange={(e) => updateConfiguration('features.enableHashtagTracking', e.target.checked)}
                  />
                }
                label="Hashtag Tracking"
              />
              <FormHelperText>Follow and track hashtags</FormHelperText>
            </Grid>
            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableContentSuggestions ?? true}
                    onChange={(e) => updateConfiguration('features.enableContentSuggestions', e.target.checked)}
                  />
                }
                label="Content Suggestions"
              />
              <FormHelperText>AI-powered content ideas</FormHelperText>
            </Grid>
            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableAudienceTargeting ?? true}
                    onChange={(e) => updateConfiguration('features.enableAudienceTargeting', e.target.checked)}
                  />
                }
                label="Audience Targeting"
              />
              <FormHelperText>Target specific audiences</FormHelperText>
            </Grid>
          </Grid>
        </FormGroup>
      </TabPanel>

      <TabPanel value={tabValue} index={3}>
        <Typography variant="h6" gutterBottom>
          Media Support
        </Typography>
        <FormGroup>
          <Grid container spacing={2}>
            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableImageSharing ?? true}
                    onChange={(e) => updateConfiguration('features.enableImageSharing', e.target.checked)}
                  />
                }
                label="Image Sharing"
              />
              <FormHelperText>Share images with posts</FormHelperText>
            </Grid>
            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableVideoSharing ?? true}
                    onChange={(e) => updateConfiguration('features.enableVideoSharing', e.target.checked)}
                  />
                }
                label="Video Sharing"
              />
              <FormHelperText>Share video content</FormHelperText>
            </Grid>
            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableDocumentSharing ?? true}
                    onChange={(e) => updateConfiguration('features.enableDocumentSharing', e.target.checked)}
                  />
                }
                label="Document Sharing"
              />
              <FormHelperText>Share PDFs and documents</FormHelperText>
            </Grid>
            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableLiveVideo ?? false}
                    onChange={(e) => updateConfiguration('features.enableLiveVideo', e.target.checked)}
                  />
                }
                label="Live Video"
              />
              <FormHelperText>LinkedIn Live broadcasting</FormHelperText>
            </Grid>
          </Grid>
        </FormGroup>

        <Divider sx={{ my: 3 }} />

        <Typography variant="h6" gutterBottom>
          Content Limits
        </Typography>
        
        <Grid container spacing={3}>
          <Grid item xs={12} md={6}>
            <Typography gutterBottom>Max Post Length (characters)</Typography>
            <Slider
              value={configuration.limits?.maxPostLength || 3000}
              onChange={(e, value) => updateConfiguration('limits.maxPostLength', value)}
              min={100}
              max={3000}
              marks={[
                { value: 700, label: '700' },
                { value: 1300, label: '1300' },
                { value: 3000, label: '3000' }
              ]}
              valueLabelDisplay="on"
            />
            {errors.maxPostLength && (
              <FormHelperText error>{errors.maxPostLength}</FormHelperText>
            )}
          </Grid>
          <Grid item xs={12} md={6}>
            <Typography gutterBottom>Max Hashtags Per Post</Typography>
            <Slider
              value={configuration.limits?.maxHashtagsPerPost || 30}
              onChange={(e, value) => updateConfiguration('limits.maxHashtagsPerPost', value)}
              min={1}
              max={30}
              marks={[
                { value: 5, label: '5' },
                { value: 15, label: '15' },
                { value: 30, label: '30' }
              ]}
              valueLabelDisplay="on"
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <Typography gutterBottom>Max Images Per Post</Typography>
            <Slider
              value={configuration.limits?.maxImagesPerPost || 20}
              onChange={(e, value) => updateConfiguration('limits.maxImagesPerPost', value)}
              min={1}
              max={20}
              marks
              valueLabelDisplay="on"
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <Typography gutterBottom>Max Video Length (minutes)</Typography>
            <Slider
              value={(configuration.limits?.maxVideoLength || 600) / 60}
              onChange={(e, value) => updateConfiguration('limits.maxVideoLength', value * 60)}
              min={1}
              max={10}
              marks={[
                { value: 1, label: '1m' },
                { value: 5, label: '5m' },
                { value: 10, label: '10m' }
              ]}
              valueLabelDisplay="on"
            />
          </Grid>
        </Grid>

        <Grid container spacing={3} sx={{ mt: 1 }}>
          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              label="Max Image Size (MB)"
              type="number"
              value={configuration.limits?.maxImageSizeMB || 10}
              onChange={(e) => updateConfiguration('limits.maxImageSizeMB', parseInt(e.target.value))}
              margin="normal"
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              label="Max Video Size (MB)"
              type="number"
              value={configuration.limits?.maxVideoSizeMB || 5120}
              onChange={(e) => updateConfiguration('limits.maxVideoSizeMB', parseInt(e.target.value))}
              helperText="Maximum: 5GB (5120 MB)"
              margin="normal"
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              label="Max Document Size (MB)"
              type="number"
              value={configuration.limits?.maxDocumentSizeMB || 100}
              onChange={(e) => updateConfiguration('limits.maxDocumentSizeMB', parseInt(e.target.value))}
              margin="normal"
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              label="Max Article Length (characters)"
              type="number"
              value={configuration.limits?.maxArticleLength || 125000}
              onChange={(e) => updateConfiguration('limits.maxArticleLength', parseInt(e.target.value))}
              error={!!errors.maxArticleLength}
              helperText={errors.maxArticleLength || 'Maximum: 125,000 characters'}
              margin="normal"
            />
          </Grid>
        </Grid>
      </TabPanel>

      <TabPanel value={tabValue} index={4}>
        <Typography variant="h6" gutterBottom>
          Social Limits
        </Typography>
        
        <Grid container spacing={3}>
          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              label="Max Connections Per Day"
              type="number"
              value={configuration.limits?.maxConnectionsPerDay || 100}
              onChange={(e) => updateConfiguration('limits.maxConnectionsPerDay', parseInt(e.target.value))}
              helperText="LinkedIn's daily connection request limit"
              margin="normal"
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              label="Max Messages Per Day"
              type="number"
              value={configuration.limits?.maxMessagesPerDay || 250}
              onChange={(e) => updateConfiguration('limits.maxMessagesPerDay', parseInt(e.target.value))}
              helperText="Daily messaging limit"
              margin="normal"
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              label="Max Posts Per Day"
              type="number"
              value={configuration.limits?.maxPostsPerDay || 100}
              onChange={(e) => updateConfiguration('limits.maxPostsPerDay', parseInt(e.target.value))}
              helperText="Daily posting limit"
              margin="normal"
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              label="Max Mentions Per Post"
              type="number"
              value={configuration.limits?.maxMentionsPerPost || 50}
              onChange={(e) => updateConfiguration('limits.maxMentionsPerPost', parseInt(e.target.value))}
              margin="normal"
            />
          </Grid>
        </Grid>

        <Divider sx={{ my: 3 }} />

        <Typography variant="h6" gutterBottom>
          Reaction Types
        </Typography>
        
        <FormControl fullWidth margin="normal">
          <InputLabel>Enabled Reaction Types</InputLabel>
          <Select
            multiple
            value={configuration.enabledReactions || ['LIKE', 'PRAISE', 'APPRECIATION', 'EMPATHY', 'INTEREST']}
            onChange={(e) => updateConfiguration('enabledReactions', e.target.value)}
            renderValue={(selected) => (
              <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                {(selected as string[]).map((value) => (
                  <Chip key={value} label={value} size="small" />
                ))}
              </Box>
            )}
          >
            {['LIKE', 'PRAISE', 'APPRECIATION', 'EMPATHY', 'INTEREST', 
              'CELEBRATION', 'ENTERTAINMENT', 'MAYBE'].map((reaction) => (
              <MenuItem key={reaction} value={reaction}>
                {reaction}
              </MenuItem>
            ))}
          </Select>
        </FormControl>
      </TabPanel>

      <TabPanel value={tabValue} index={5}>
        <Typography variant="h6" gutterBottom>
          Analytics & Insights
        </Typography>
        <FormGroup>
          <Grid container spacing={2}>
            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableAnalytics ?? true}
                    onChange={(e) => updateConfiguration('features.enableAnalytics', e.target.checked)}
                  />
                }
                label="Enable Analytics"
              />
              <FormHelperText>Track post and page performance</FormHelperText>
            </Grid>
          </Grid>
        </FormGroup>

        <Divider sx={{ my: 3 }} />

        <Typography variant="h6" gutterBottom>
          Metrics to Track
        </Typography>
        
        <FormControl fullWidth margin="normal">
          <InputLabel>Default Metrics</InputLabel>
          <Select
            multiple
            value={configuration.defaultMetrics || ['IMPRESSIONS', 'CLICKS', 'ENGAGEMENT', 'LIKES']}
            onChange={(e) => updateConfiguration('defaultMetrics', e.target.value)}
            renderValue={(selected) => (
              <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                {(selected as string[]).map((value) => (
                  <Chip key={value} label={value} size="small" />
                ))}
              </Box>
            )}
          >
            {['IMPRESSIONS', 'CLICKS', 'ENGAGEMENT', 'ENGAGEMENT_RATE', 
              'LIKES', 'COMMENTS', 'SHARES', 'FOLLOWS', 'VIDEO_VIEWS', 
              'VIDEO_COMPLETION_RATE', 'CLICK_THROUGH_RATE', 'SOCIAL_ACTIONS',
              'UNIQUE_IMPRESSIONS'].map((metric) => (
              <MenuItem key={metric} value={metric}>
                {metric.replace(/_/g, ' ').toLowerCase()}
              </MenuItem>
            ))}
          </Select>
          <FormHelperText>Select which metrics to track by default</FormHelperText>
        </FormControl>

        <Divider sx={{ my: 3 }} />

        <Typography variant="h6" gutterBottom>
          Polling Intervals
        </Typography>
        
        <Grid container spacing={3}>
          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              label="Feed Polling Interval (minutes)"
              type="number"
              value={(configuration.polling?.feedPollingInterval || 300000) / 60000}
              onChange={(e) => updateConfiguration('polling.feedPollingInterval', parseInt(e.target.value) * 60000)}
              helperText="How often to check for new posts"
              margin="normal"
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              label="Comment Polling Interval (minutes)"
              type="number"
              value={(configuration.polling?.commentPollingInterval || 600000) / 60000}
              onChange={(e) => updateConfiguration('polling.commentPollingInterval', parseInt(e.target.value) * 60000)}
              helperText="How often to check for new comments"
              margin="normal"
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              label="Message Polling Interval (minutes)"
              type="number"
              value={(configuration.polling?.messagePollingInterval || 120000) / 60000}
              onChange={(e) => updateConfiguration('polling.messagePollingInterval', parseInt(e.target.value) * 60000)}
              helperText="How often to check for new messages"
              margin="normal"
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              label="Analytics Polling Interval (minutes)"
              type="number"
              value={(configuration.polling?.analyticsPollingInterval || 1800000) / 60000}
              onChange={(e) => updateConfiguration('polling.analyticsPollingInterval', parseInt(e.target.value) * 60000)}
              helperText="How often to update analytics"
              margin="normal"
            />
          </Grid>
        </Grid>
      </TabPanel>

      <TabPanel value={tabValue} index={6}>
        <Accordion defaultExpanded>
          <AccordionSummary expandIcon={<ExpandMoreIcon />}>
            <Typography variant="h6">API Limits</Typography>
          </AccordionSummary>
          <AccordionDetails>
            <Alert severity="info" sx={{ mb: 2 }}>
              LinkedIn API has strict rate limits. Stay within these limits to avoid throttling.
            </Alert>
            <Grid container spacing={3}>
              <Grid item xs={12} md={6}>
                <TextField
                  fullWidth
                  label="Max Batch Size"
                  type="number"
                  value={configuration.limits?.maxBatchSize || 50}
                  onChange={(e) => updateConfiguration('limits.maxBatchSize', parseInt(e.target.value))}
                  helperText="Maximum items per batch operation"
                  margin="normal"
                />
              </Grid>
              <Grid item xs={12} md={6}>
                <TextField
                  fullWidth
                  label="API Call Limit (per day)"
                  type="number"
                  value={configuration.apiCallLimit || 1000}
                  onChange={(e) => updateConfiguration('apiCallLimit', parseInt(e.target.value))}
                  helperText="Daily API call limit"
                  margin="normal"
                />
              </Grid>
            </Grid>
          </AccordionDetails>
        </Accordion>

        <Accordion>
          <AccordionSummary expandIcon={<ExpandMoreIcon />}>
            <Typography variant="h6">Rate Limiting</Typography>
          </AccordionSummary>
          <AccordionDetails>
            <Grid container spacing={3}>
              <Grid item xs={12} md={6}>
                <TextField
                  fullWidth
                  label="Max Requests Per Second"
                  type="number"
                  value={configuration.rateLimiting?.maxRequestsPerSecond || 10}
                  onChange={(e) => updateConfiguration('rateLimiting.maxRequestsPerSecond', parseInt(e.target.value))}
                  margin="normal"
                />
              </Grid>
              <Grid item xs={12} md={6}>
                <TextField
                  fullWidth
                  label="Max Concurrent Requests"
                  type="number"
                  value={configuration.rateLimiting?.maxConcurrentRequests || 5}
                  onChange={(e) => updateConfiguration('rateLimiting.maxConcurrentRequests', parseInt(e.target.value))}
                  margin="normal"
                />
              </Grid>
              <Grid item xs={12}>
                <FormControlLabel
                  control={
                    <Switch
                      checked={configuration.rateLimiting?.enableAdaptiveRateLimiting ?? true}
                      onChange={(e) => updateConfiguration('rateLimiting.enableAdaptiveRateLimiting', e.target.checked)}
                    />
                  }
                  label="Enable Adaptive Rate Limiting"
                />
                <FormHelperText>
                  Automatically adjust rate based on API response headers
                </FormHelperText>
              </Grid>
            </Grid>
          </AccordionDetails>
        </Accordion>

        <Accordion>
          <AccordionSummary expandIcon={<ExpandMoreIcon />}>
            <Typography variant="h6">Error Handling</Typography>
          </AccordionSummary>
          <AccordionDetails>
            <Grid container spacing={3}>
              <Grid item xs={12} md={6}>
                <TextField
                  fullWidth
                  label="Max Retry Attempts"
                  type="number"
                  value={configuration.errorHandling?.maxRetryAttempts || 3}
                  onChange={(e) => updateConfiguration('errorHandling.maxRetryAttempts', parseInt(e.target.value))}
                  margin="normal"
                />
              </Grid>
              <Grid item xs={12} md={6}>
                <TextField
                  fullWidth
                  label="Retry Delay (ms)"
                  type="number"
                  value={configuration.errorHandling?.retryDelay || 1000}
                  onChange={(e) => updateConfiguration('errorHandling.retryDelay', parseInt(e.target.value))}
                  margin="normal"
                />
              </Grid>
              <Grid item xs={12}>
                <FormControlLabel
                  control={
                    <Switch
                      checked={configuration.errorHandling?.enableExponentialBackoff ?? true}
                      onChange={(e) => updateConfiguration('errorHandling.enableExponentialBackoff', e.target.checked)}
                    />
                  }
                  label="Enable Exponential Backoff"
                />
              </Grid>
            </Grid>
          </AccordionDetails>
        </Accordion>

        <Accordion>
          <AccordionSummary expandIcon={<ExpandMoreIcon />}>
            <Typography variant="h6">Advanced Settings</Typography>
          </AccordionSummary>
          <AccordionDetails>
            <Grid container spacing={3}>
              <Grid item xs={12}>
                <TextField
                  fullWidth
                  label="API Version"
                  value={configuration.apiVersion || '202401'}
                  onChange={(e) => updateConfiguration('apiVersion', e.target.value)}
                  helperText="LinkedIn API version (format: YYYYMM)"
                  margin="normal"
                />
              </Grid>
              <Grid item xs={12}>
                <FormControl fullWidth margin="normal">
                  <InputLabel>Log Level</InputLabel>
                  <Select
                    value={configuration.logging?.level || 'INFO'}
                    onChange={(e) => updateConfiguration('logging.level', e.target.value)}
                  >
                    <MenuItem value="DEBUG">DEBUG</MenuItem>
                    <MenuItem value="INFO">INFO</MenuItem>
                    <MenuItem value="WARN">WARN</MenuItem>
                    <MenuItem value="ERROR">ERROR</MenuItem>
                  </Select>
                </FormControl>
              </Grid>
              <Grid item xs={12}>
                <FormGroup>
                  <FormControlLabel
                    control={
                      <Switch
                        checked={configuration.logging?.logApiCalls ?? false}
                        onChange={(e) => updateConfiguration('logging.logApiCalls', e.target.checked)}
                      />
                    }
                    label="Log API Calls"
                  />
                  <FormControlLabel
                    control={
                      <Switch
                        checked={configuration.monitoring?.enableMetrics ?? true}
                        onChange={(e) => updateConfiguration('monitoring.enableMetrics', e.target.checked)}
                      />
                    }
                    label="Enable Metrics Collection"
                  />
                  <FormControlLabel
                    control={
                      <Switch
                        checked={configuration.monitoring?.enableHealthCheck ?? true}
                        onChange={(e) => updateConfiguration('monitoring.enableHealthCheck', e.target.checked)}
                      />
                    }
                    label="Enable Health Check Endpoint"
                  />
                </FormGroup>
              </Grid>
            </Grid>
          </AccordionDetails>
        </Accordion>

        <Box sx={{ mt: 3 }}>
          <Alert severity="info">
            <Typography variant="subtitle2" gutterBottom>
              Best Practices
            </Typography>
            <ul style={{ marginLeft: 20, marginTop: 8 }}>
              <li>Post during peak engagement hours (Tuesday-Thursday, 8-10 AM and 5-6 PM)</li>
              <li>Use 3-5 relevant hashtags per post</li>
              <li>Include visuals to increase engagement by 2x</li>
              <li>Keep posts concise (150-300 characters perform best)</li>
              <li>Engage with comments within the first hour</li>
              <li>Monitor analytics to optimize content strategy</li>
            </ul>
          </Alert>
        </Box>
      </TabPanel>
    </Box>
  );
};

export default LinkedInApiConfiguration;