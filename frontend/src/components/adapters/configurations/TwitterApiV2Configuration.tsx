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
  Timeline as TimelineIcon,
  People as PeopleIcon,
  Message as MessageIcon,
  Analytics as AnalyticsIcon,
  Schedule as ScheduleIcon,
  Speed as SpeedIcon,
} from '@mui/icons-material';

interface TwitterApiV2ConfigurationProps {
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

const TwitterApiV2Configuration: React.FC<TwitterApiV2ConfigurationProps> = ({
  configuration,
  onConfigurationChange,
  onValidation,
}) => {
  const [tabValue, setTabValue] = useState(0);
  const [authType, setAuthType] = useState(configuration.authType || 'oauth2');
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

    // API credentials validation
    if (authType === 'bearer' && !config.bearerToken) {
      newErrors.bearerToken = 'Bearer token is required';
    }
    if (authType === 'oauth2') {
      if (!config.apiKey) newErrors.apiKey = 'API Key is required';
      if (!config.apiKeySecret) newErrors.apiKeySecret = 'API Key Secret is required';
      if (!config.accessToken) newErrors.accessToken = 'Access Token is required';
      if (!config.accessTokenSecret) newErrors.accessTokenSecret = 'Access Token Secret is required';
    }

    // Rate limit validation
    if (config.limits) {
      if (config.limits.maxTweetLength > 280) {
        newErrors.maxTweetLength = 'Maximum tweet length cannot exceed 280 characters';
      }
      if (config.limits.maxThreadLength > 25) {
        newErrors.maxThreadLength = 'Maximum thread length cannot exceed 25 tweets';
      }
      if (config.limits.maxImagesPerTweet > 4) {
        newErrors.maxImagesPerTweet = 'Maximum 4 images allowed per tweet';
      }
    }

    setErrors(newErrors);
    onValidation(Object.keys(newErrors).length === 0);
  };

  return (
    <Box sx={{ width: '100%' }}>
      <Paper elevation={0} sx={{ borderBottom: 1, borderColor: 'divider' }}>
        <Tabs value={tabValue} onChange={(e, v) => setTabValue(v)} variant="scrollable">
          <Tab icon={<SecurityIcon />} label="Authentication" />
          <Tab icon={<FunctionsIcon />} label="Features" />
          <Tab icon={<SpeedIcon />} label="Limits & Rate Limiting" />
          <Tab icon={<TimelineIcon />} label="Polling & Webhooks" />
          <Tab icon={<SettingsIcon />} label="Advanced Settings" />
        </Tabs>
      </Paper>

      <TabPanel value={tabValue} index={0}>
        <Box sx={{ mb: 3 }}>
          <Alert severity="info" icon={<InfoIcon />}>
            Twitter API v2 requires authentication for all operations. Choose between OAuth 2.0 (user context) 
            or Bearer Token (app-only context). User context provides access to more features.
            <Link href="https://developer.twitter.com/en/docs/authentication/overview" target="_blank" sx={{ ml: 1 }}>
              Learn more
            </Link>
          </Alert>
        </Box>

        <FormControl component="fieldset" sx={{ mb: 3 }}>
          <FormLabel component="legend">Authentication Type</FormLabel>
          <RadioGroup
            value={authType}
            onChange={(e) => {
              setAuthType(e.target.value);
              updateConfiguration('authType', e.target.value);
            }}
          >
            <FormControlLabel 
              value="oauth2" 
              control={<Radio />} 
              label="OAuth 2.0 (User Context - Full Access)" 
            />
            <FormControlLabel 
              value="bearer" 
              control={<Radio />} 
              label="Bearer Token (App-only Context - Limited Access)" 
            />
          </RadioGroup>
        </FormControl>

        {authType === 'bearer' ? (
          <TextField
            fullWidth
            label="Bearer Token"
            type="password"
            value={configuration.bearerToken || ''}
            onChange={(e) => updateConfiguration('bearerToken', e.target.value)}
            error={!!errors.bearerToken}
            helperText={errors.bearerToken || 'App-only authentication token'}
            margin="normal"
            required
          />
        ) : (
          <Grid container spacing={2}>
            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label="API Key (Consumer Key)"
                value={configuration.apiKey || ''}
                onChange={(e) => updateConfiguration('apiKey', e.target.value)}
                error={!!errors.apiKey}
                helperText={errors.apiKey || 'Your Twitter app API key'}
                margin="normal"
                required
              />
            </Grid>
            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label="API Key Secret (Consumer Secret)"
                type="password"
                value={configuration.apiKeySecret || ''}
                onChange={(e) => updateConfiguration('apiKeySecret', e.target.value)}
                error={!!errors.apiKeySecret}
                helperText={errors.apiKeySecret}
                margin="normal"
                required
              />
            </Grid>
            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label="Access Token"
                value={configuration.accessToken || ''}
                onChange={(e) => updateConfiguration('accessToken', e.target.value)}
                error={!!errors.accessToken}
                helperText={errors.accessToken || 'User access token'}
                margin="normal"
                required
              />
            </Grid>
            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label="Access Token Secret"
                type="password"
                value={configuration.accessTokenSecret || ''}
                onChange={(e) => updateConfiguration('accessTokenSecret', e.target.value)}
                error={!!errors.accessTokenSecret}
                helperText={errors.accessTokenSecret}
                margin="normal"
                required
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Client ID (OAuth 2.0)"
                value={configuration.clientId || ''}
                onChange={(e) => updateConfiguration('clientId', e.target.value)}
                helperText="Required for OAuth 2.0 flows"
                margin="normal"
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Client Secret (OAuth 2.0)"
                type="password"
                value={configuration.clientSecret || ''}
                onChange={(e) => updateConfiguration('clientSecret', e.target.value)}
                helperText="Required for OAuth 2.0 flows"
                margin="normal"
              />
            </Grid>
          </Grid>
        )}

        <Box sx={{ mt: 3 }}>
          <Button 
            variant="outlined" 
            href="https://developer.twitter.com/en/portal/projects-and-apps"
            target="_blank"
          >
            Get Twitter API Credentials
          </Button>
        </Box>
      </TabPanel>

      <TabPanel value={tabValue} index={1}>
        <Typography variant="h6" gutterBottom>
          Core Features
        </Typography>
        <FormGroup>
          <Grid container spacing={2}>
            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableTweetComposition ?? true}
                    onChange={(e) => updateConfiguration('features.enableTweetComposition', e.target.checked)}
                  />
                }
                label="Tweet Composition"
              />
              <FormHelperText>Create and post tweets</FormHelperText>
            </Grid>
            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableThreading ?? true}
                    onChange={(e) => updateConfiguration('features.enableThreading', e.target.checked)}
                  />
                }
                label="Threading Support"
              />
              <FormHelperText>Create tweet threads</FormHelperText>
            </Grid>
            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableTimelineRetrieval ?? true}
                    onChange={(e) => updateConfiguration('features.enableTimelineRetrieval', e.target.checked)}
                  />
                }
                label="Timeline Retrieval"
              />
              <FormHelperText>Access home and user timelines</FormHelperText>
            </Grid>
            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableMentionMonitoring ?? true}
                    onChange={(e) => updateConfiguration('features.enableMentionMonitoring', e.target.checked)}
                  />
                }
                label="Mention Monitoring"
              />
              <FormHelperText>Track @mentions</FormHelperText>
            </Grid>
            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableFollowerAnalytics ?? true}
                    onChange={(e) => updateConfiguration('features.enableFollowerAnalytics', e.target.checked)}
                  />
                }
                label="Follower Analytics"
              />
              <FormHelperText>Track follower metrics</FormHelperText>
            </Grid>
            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableMediaUpload ?? true}
                    onChange={(e) => updateConfiguration('features.enableMediaUpload', e.target.checked)}
                  />
                }
                label="Media Upload"
              />
              <FormHelperText>Upload images, GIFs, and videos</FormHelperText>
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
                    checked={configuration.features?.enableSpacesIntegration ?? false}
                    onChange={(e) => updateConfiguration('features.enableSpacesIntegration', e.target.checked)}
                  />
                }
                label="Spaces Integration"
              />
              <FormHelperText>Monitor and interact with Twitter Spaces</FormHelperText>
            </Grid>
            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableDirectMessages ?? false}
                    onChange={(e) => updateConfiguration('features.enableDirectMessages', e.target.checked)}
                  />
                }
                label="Direct Messages"
              />
              <FormHelperText>Send and receive DMs</FormHelperText>
            </Grid>
            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableLists ?? false}
                    onChange={(e) => updateConfiguration('features.enableLists', e.target.checked)}
                  />
                }
                label="Lists Management"
              />
              <FormHelperText>Create and manage Twitter lists</FormHelperText>
            </Grid>
            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableBookmarks ?? false}
                    onChange={(e) => updateConfiguration('features.enableBookmarks', e.target.checked)}
                  />
                }
                label="Bookmarks"
              />
              <FormHelperText>Save and manage bookmarks</FormHelperText>
            </Grid>
            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enablePolls ?? false}
                    onChange={(e) => updateConfiguration('features.enablePolls', e.target.checked)}
                  />
                }
                label="Polls"
              />
              <FormHelperText>Create polls in tweets</FormHelperText>
            </Grid>
            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableScheduledTweets ?? false}
                    onChange={(e) => updateConfiguration('features.enableScheduledTweets', e.target.checked)}
                  />
                }
                label="Scheduled Tweets"
              />
              <FormHelperText>Schedule tweets for later</FormHelperText>
            </Grid>
            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableQuoteTweets ?? true}
                    onChange={(e) => updateConfiguration('features.enableQuoteTweets', e.target.checked)}
                  />
                }
                label="Quote Tweets"
              />
              <FormHelperText>Quote other tweets</FormHelperText>
            </Grid>
            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableRetweets ?? true}
                    onChange={(e) => updateConfiguration('features.enableRetweets', e.target.checked)}
                  />
                }
                label="Retweets"
              />
              <FormHelperText>Retweet functionality</FormHelperText>
            </Grid>
            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableLikes ?? true}
                    onChange={(e) => updateConfiguration('features.enableLikes', e.target.checked)}
                  />
                }
                label="Likes"
              />
              <FormHelperText>Like tweets</FormHelperText>
            </Grid>
          </Grid>
        </FormGroup>
      </TabPanel>

      <TabPanel value={tabValue} index={2}>
        <Accordion defaultExpanded>
          <AccordionSummary expandIcon={<ExpandMoreIcon />}>
            <Typography variant="h6">Content Limits</Typography>
          </AccordionSummary>
          <AccordionDetails>
            <Grid container spacing={3}>
              <Grid item xs={12} md={6}>
                <Typography gutterBottom>Max Tweet Length</Typography>
                <Slider
                  value={configuration.limits?.maxTweetLength || 280}
                  onChange={(e, value) => updateConfiguration('limits.maxTweetLength', value)}
                  min={1}
                  max={280}
                  marks={[
                    { value: 140, label: '140' },
                    { value: 280, label: '280' }
                  ]}
                  valueLabelDisplay="on"
                />
                {errors.maxTweetLength && (
                  <FormHelperText error>{errors.maxTweetLength}</FormHelperText>
                )}
              </Grid>
              <Grid item xs={12} md={6}>
                <Typography gutterBottom>Max Thread Length</Typography>
                <Slider
                  value={configuration.limits?.maxThreadLength || 25}
                  onChange={(e, value) => updateConfiguration('limits.maxThreadLength', value)}
                  min={2}
                  max={25}
                  marks={[
                    { value: 5, label: '5' },
                    { value: 15, label: '15' },
                    { value: 25, label: '25' }
                  ]}
                  valueLabelDisplay="on"
                />
              </Grid>
              <Grid item xs={12} md={6}>
                <Typography gutterBottom>Max Images Per Tweet</Typography>
                <Slider
                  value={configuration.limits?.maxImagesPerTweet || 4}
                  onChange={(e, value) => updateConfiguration('limits.maxImagesPerTweet', value)}
                  min={1}
                  max={4}
                  marks
                  valueLabelDisplay="on"
                />
              </Grid>
              <Grid item xs={12} md={6}>
                <Typography gutterBottom>Max Video Length (seconds)</Typography>
                <Slider
                  value={configuration.limits?.maxVideoLength || 140}
                  onChange={(e, value) => updateConfiguration('limits.maxVideoLength', value)}
                  min={1}
                  max={140}
                  marks={[
                    { value: 30, label: '30s' },
                    { value: 60, label: '1m' },
                    { value: 140, label: '2m20s' }
                  ]}
                  valueLabelDisplay="on"
                />
              </Grid>
              <Grid item xs={12} md={6}>
                <TextField
                  fullWidth
                  label="Max Video Size (MB)"
                  type="number"
                  value={configuration.limits?.maxVideoSizeMB || 512}
                  onChange={(e) => updateConfiguration('limits.maxVideoSizeMB', parseInt(e.target.value))}
                  margin="normal"
                />
              </Grid>
              <Grid item xs={12} md={6}>
                <TextField
                  fullWidth
                  label="Max GIF Size (MB)"
                  type="number"
                  value={configuration.limits?.maxGifSizeMB || 15}
                  onChange={(e) => updateConfiguration('limits.maxGifSizeMB', parseInt(e.target.value))}
                  margin="normal"
                />
              </Grid>
            </Grid>
          </AccordionDetails>
        </Accordion>

        <Accordion>
          <AccordionSummary expandIcon={<ExpandMoreIcon />}>
            <Typography variant="h6">Poll Limits</Typography>
          </AccordionSummary>
          <AccordionDetails>
            <Grid container spacing={3}>
              <Grid item xs={12} md={6}>
                <Typography gutterBottom>Max Poll Options</Typography>
                <Slider
                  value={configuration.limits?.maxPollOptions || 4}
                  onChange={(e, value) => updateConfiguration('limits.maxPollOptions', value)}
                  min={2}
                  max={4}
                  marks
                  valueLabelDisplay="on"
                />
              </Grid>
              <Grid item xs={12} md={6}>
                <TextField
                  fullWidth
                  label="Max Poll Duration (hours)"
                  type="number"
                  value={configuration.limits?.maxPollDurationHours || 168}
                  onChange={(e) => updateConfiguration('limits.maxPollDurationHours', parseInt(e.target.value))}
                  helperText="Maximum: 7 days (168 hours)"
                  margin="normal"
                />
              </Grid>
            </Grid>
          </AccordionDetails>
        </Accordion>

        <Accordion>
          <AccordionSummary expandIcon={<ExpandMoreIcon />}>
            <Typography variant="h6">List Limits</Typography>
          </AccordionSummary>
          <AccordionDetails>
            <Grid container spacing={3}>
              <Grid item xs={12} md={6}>
                <TextField
                  fullWidth
                  label="Max Lists Per Account"
                  type="number"
                  value={configuration.limits?.maxListsPerAccount || 1000}
                  onChange={(e) => updateConfiguration('limits.maxListsPerAccount', parseInt(e.target.value))}
                  margin="normal"
                />
              </Grid>
              <Grid item xs={12} md={6}>
                <TextField
                  fullWidth
                  label="Max Accounts Per List"
                  type="number"
                  value={configuration.limits?.maxAccountsPerList || 5000}
                  onChange={(e) => updateConfiguration('limits.maxAccountsPerList', parseInt(e.target.value))}
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
            <Alert severity="info" sx={{ mb: 2 }}>
              Twitter API v2 has strict rate limits. Configure these settings to avoid hitting limits.
            </Alert>
            <Grid container spacing={3}>
              <Grid item xs={12} md={6}>
                <TextField
                  fullWidth
                  label="Max API Calls Per Hour"
                  type="number"
                  value={configuration.rateLimiting?.maxCallsPerHour || 300}
                  onChange={(e) => updateConfiguration('rateLimiting.maxCallsPerHour', parseInt(e.target.value))}
                  helperText="Twitter's default: 300/15min for most endpoints"
                  margin="normal"
                />
              </Grid>
              <Grid item xs={12} md={6}>
                <TextField
                  fullWidth
                  label="Burst Limit"
                  type="number"
                  value={configuration.rateLimiting?.burstLimit || 100}
                  onChange={(e) => updateConfiguration('rateLimiting.burstLimit', parseInt(e.target.value))}
                  helperText="Maximum calls in a short burst"
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
      </TabPanel>

      <TabPanel value={tabValue} index={3}>
        <Typography variant="h6" gutterBottom>
          Polling Configuration
        </Typography>
        <Alert severity="info" sx={{ mb: 2 }}>
          Configure how often to poll Twitter for new data. Lower intervals provide more real-time data but consume more API calls.
        </Alert>
        
        <Grid container spacing={3}>
          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              label="Mention Polling Interval (seconds)"
              type="number"
              value={configuration.polling?.mentionPollingInterval || 60}
              onChange={(e) => updateConfiguration('polling.mentionPollingInterval', parseInt(e.target.value))}
              helperText="How often to check for new mentions"
              margin="normal"
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              label="Timeline Polling Interval (seconds)"
              type="number"
              value={configuration.polling?.timelinePollingInterval || 300}
              onChange={(e) => updateConfiguration('polling.timelinePollingInterval', parseInt(e.target.value))}
              helperText="How often to check timeline updates"
              margin="normal"
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              label="DM Polling Interval (seconds)"
              type="number"
              value={configuration.polling?.dmPollingInterval || 120}
              onChange={(e) => updateConfiguration('polling.dmPollingInterval', parseInt(e.target.value))}
              helperText="How often to check for new direct messages"
              margin="normal"
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              label="Spaces Polling Interval (seconds)"
              type="number"
              value={configuration.polling?.spacesPollingInterval || 300}
              onChange={(e) => updateConfiguration('polling.spacesPollingInterval', parseInt(e.target.value))}
              helperText="How often to check for live Spaces"
              margin="normal"
            />
          </Grid>
        </Grid>

        <Divider sx={{ my: 3 }} />

        <Typography variant="h6" gutterBottom>
          Webhook Configuration
        </Typography>
        <Alert severity="warning" sx={{ mb: 2 }}>
          Twitter webhooks require Account Activity API access and proper webhook URL registration.
        </Alert>
        
        <FormControlLabel
          control={
            <Switch
              checked={configuration.webhook?.enabled ?? false}
              onChange={(e) => updateConfiguration('webhook.enabled', e.target.checked)}
            />
          }
          label="Enable Webhooks"
        />
        
        {configuration.webhook?.enabled && (
          <Box sx={{ mt: 2 }}>
            <TextField
              fullWidth
              label="Webhook Environment"
              value={configuration.webhook?.environment || ''}
              onChange={(e) => updateConfiguration('webhook.environment', e.target.value)}
              helperText="Twitter webhook environment name"
              margin="normal"
            />
            <TextField
              fullWidth
              label="Webhook URL"
              value={configuration.webhook?.url || ''}
              onChange={(e) => updateConfiguration('webhook.url', e.target.value)}
              helperText="Your server's webhook endpoint URL"
              margin="normal"
            />
            <FormGroup sx={{ mt: 2 }}>
              <Typography variant="subtitle2" gutterBottom>
                Webhook Events
              </Typography>
              <FormControlLabel
                control={
                  <Checkbox
                    checked={configuration.webhook?.events?.tweets ?? true}
                    onChange={(e) => updateConfiguration('webhook.events.tweets', e.target.checked)}
                  />
                }
                label="Tweet Events"
              />
              <FormControlLabel
                control={
                  <Checkbox
                    checked={configuration.webhook?.events?.favorites ?? true}
                    onChange={(e) => updateConfiguration('webhook.events.favorites', e.target.checked)}
                  />
                }
                label="Favorite Events"
              />
              <FormControlLabel
                control={
                  <Checkbox
                    checked={configuration.webhook?.events?.follows ?? true}
                    onChange={(e) => updateConfiguration('webhook.events.follows', e.target.checked)}
                  />
                }
                label="Follow Events"
              />
              <FormControlLabel
                control={
                  <Checkbox
                    checked={configuration.webhook?.events?.directMessages ?? true}
                    onChange={(e) => updateConfiguration('webhook.events.directMessages', e.target.checked)}
                  />
                }
                label="Direct Message Events"
              />
            </FormGroup>
          </Box>
        )}
      </TabPanel>

      <TabPanel value={tabValue} index={4}>
        <Typography variant="h6" gutterBottom>
          Data Processing
        </Typography>
        
        <FormControl fullWidth margin="normal">
          <InputLabel>Tweet Fields to Retrieve</InputLabel>
          <Select
            multiple
            value={configuration.dataProcessing?.tweetFields || ['id', 'text', 'created_at', 'author_id']}
            onChange={(e) => updateConfiguration('dataProcessing.tweetFields', e.target.value)}
            renderValue={(selected) => (
              <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                {(selected as string[]).map((value) => (
                  <Chip key={value} label={value} size="small" />
                ))}
              </Box>
            )}
          >
            {['id', 'text', 'created_at', 'author_id', 'conversation_id', 'public_metrics', 
              'entities', 'attachments', 'geo', 'lang', 'possibly_sensitive', 'reply_settings',
              'source', 'withheld'].map((field) => (
              <MenuItem key={field} value={field}>
                {field}
              </MenuItem>
            ))}
          </Select>
        </FormControl>

        <FormControl fullWidth margin="normal">
          <InputLabel>User Fields to Retrieve</InputLabel>
          <Select
            multiple
            value={configuration.dataProcessing?.userFields || ['id', 'name', 'username']}
            onChange={(e) => updateConfiguration('dataProcessing.userFields', e.target.value)}
            renderValue={(selected) => (
              <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                {(selected as string[]).map((value) => (
                  <Chip key={value} label={value} size="small" />
                ))}
              </Box>
            )}
          >
            {['id', 'name', 'username', 'created_at', 'description', 'location',
              'pinned_tweet_id', 'profile_image_url', 'protected', 'public_metrics',
              'url', 'verified', 'verified_type'].map((field) => (
              <MenuItem key={field} value={field}>
                {field}
              </MenuItem>
            ))}
          </Select>
        </FormControl>

        <Divider sx={{ my: 3 }} />

        <Typography variant="h6" gutterBottom>
          Error Handling
        </Typography>
        
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
            <FormHelperText>
              Increase retry delay exponentially on consecutive failures
            </FormHelperText>
          </Grid>
        </Grid>

        <Divider sx={{ my: 3 }} />

        <Typography variant="h6" gutterBottom>
          Logging & Monitoring
        </Typography>
        
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

        <FormGroup sx={{ mt: 2 }}>
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
                checked={configuration.logging?.logRateLimitHeaders ?? true}
                onChange={(e) => updateConfiguration('logging.logRateLimitHeaders', e.target.checked)}
              />
            }
            label="Log Rate Limit Headers"
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

        <Box sx={{ mt: 3 }}>
          <Alert severity="info">
            <Typography variant="subtitle2" gutterBottom>
              Best Practices
            </Typography>
            <ul style={{ marginLeft: 20, marginTop: 8 }}>
              <li>Always handle rate limit responses gracefully</li>
              <li>Use webhook events when possible to reduce polling</li>
              <li>Batch requests when retrieving multiple items</li>
              <li>Cache frequently accessed data locally</li>
              <li>Monitor your API usage to avoid hitting limits</li>
            </ul>
          </Alert>
        </Box>
      </TabPanel>
    </Box>
  );
};

export default TwitterApiV2Configuration;