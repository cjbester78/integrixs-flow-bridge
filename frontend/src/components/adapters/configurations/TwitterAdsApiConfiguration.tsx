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
  InputAdornment,
} from '@mui/material';
import {
  Info as InfoIcon,
  ExpandMore as ExpandMoreIcon,
  Security as SecurityIcon,
  Settings as SettingsIcon,
  Functions as FunctionsIcon,
  AttachMoney as MoneyIcon,
  People as PeopleIcon,
  Campaign as CampaignIcon,
  Analytics as AnalyticsIcon,
  Speed as SpeedIcon,
  CreditCard as CreditCardIcon,
  Timeline as TimelineIcon,
} from '@mui/icons-material';

interface TwitterAdsApiConfigurationProps {
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

const TwitterAdsApiConfiguration: React.FC<TwitterAdsApiConfigurationProps> = ({
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
    if (!config.adsAccountId) {
      newErrors.adsAccountId = 'Ads Account ID is required';
    }
    if (!config.apiKey) {
      newErrors.apiKey = 'API Key is required';
    }
    if (!config.apiKeySecret) {
      newErrors.apiKeySecret = 'API Key Secret is required';
    }
    if (!config.accessToken) {
      newErrors.accessToken = 'Access Token is required';
    }
    if (!config.accessTokenSecret) {
      newErrors.accessTokenSecret = 'Access Token Secret is required';
    }

    // Budget limits validation
    if (config.limits) {
      if (config.limits.minDailyBudget < 1) {
        newErrors.minDailyBudget = 'Minimum daily budget must be at least $1';
      }
      if (config.limits.maxDailyBudget > 1000000) {
        newErrors.maxDailyBudget = 'Maximum daily budget cannot exceed $1,000,000';
      }
      if (config.limits.maxCampaignsPerAccount > 200) {
        newErrors.maxCampaignsPerAccount = 'Maximum campaigns per account cannot exceed 200';
      }
    }

    setErrors(newErrors);
    onValidation(Object.keys(newErrors).length === 0);
  };

  const campaignObjectives = [
    { value: 'AWARENESS', label: 'Awareness - Maximize reach' },
    { value: 'TWEET_ENGAGEMENTS', label: 'Tweet Engagements - Likes, retweets, replies' },
    { value: 'VIDEO_VIEWS', label: 'Video Views - Maximize video views' },
    { value: 'PRE_ROLL_VIEWS', label: 'Pre-roll Views - In-stream video views' },
    { value: 'APP_INSTALLS', label: 'App Installs - Drive app downloads' },
    { value: 'WEBSITE_TRAFFIC', label: 'Website Traffic - Drive clicks to website' },
    { value: 'FOLLOWERS', label: 'Followers - Grow your audience' },
    { value: 'APP_ENGAGEMENTS', label: 'App Engagements - Drive app opens' },
    { value: 'WEBSITE_CONVERSIONS', label: 'Website Conversions - Drive actions' },
    { value: 'IN_STREAM_VIDEO_VIEWS', label: 'In-stream Video Views' },
  ];

  const bidTypes = [
    { value: 'AUTO', label: 'Automatic Bidding' },
    { value: 'MAX', label: 'Maximum Bid' },
    { value: 'TARGET', label: 'Target Bid' },
  ];

  const placementTypes = [
    { value: 'ALL_ON_TWITTER', label: 'All on Twitter' },
    { value: 'PUBLISHER_NETWORK', label: 'Publisher Network' },
    { value: 'TWITTER_SEARCH', label: 'Twitter Search Results' },
    { value: 'TWITTER_TIMELINE', label: 'Twitter Timeline' },
    { value: 'TWITTER_PROFILE', label: 'Twitter Profiles' },
  ];

  return (
    <Box sx={{ width: '100%' }}>
      <Paper elevation={0} sx={{ borderBottom: 1, borderColor: 'divider' }}>
        <Tabs value={tabValue} onChange={(e, v) => setTabValue(v)} variant="scrollable">
          <Tab icon={<SecurityIcon />} label="Authentication" />
          <Tab icon={<CampaignIcon />} label="Campaign Settings" />
          <Tab icon={<FunctionsIcon />} label="Features" />
          <Tab icon={<MoneyIcon />} label="Budget & Bidding" />
          <Tab icon={<PeopleIcon />} label="Targeting & Audiences" />
          <Tab icon={<AnalyticsIcon />} label="Analytics & Reporting" />
          <Tab icon={<SpeedIcon />} label="Limits & Advanced" />
        </Tabs>
      </Paper>

      <TabPanel value={tabValue} index={0}>
        <Box sx={{ mb: 3 }}>
          <Alert severity="info" icon={<InfoIcon />}>
            Twitter Ads API uses OAuth 1.0a authentication. You'll need to create a Twitter Ads account 
            and app to get these credentials.
            <Link href="https://business.twitter.com/en/help/campaign-setup/access-twitter-ads.html" target="_blank" sx={{ ml: 1 }}>
              Learn more
            </Link>
          </Alert>
        </Box>

        <Grid container spacing={2}>
          <Grid item xs={12}>
            <TextField
              fullWidth
              label="Ads Account ID"
              value={configuration.adsAccountId || ''}
              onChange={(e) => updateConfiguration('adsAccountId', e.target.value)}
              error={!!errors.adsAccountId}
              helperText={errors.adsAccountId || 'Your Twitter Ads account ID (e.g., 18ce54d4x5t)'}
              margin="normal"
              required
            />
          </Grid>
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
              helperText={errors.accessToken || 'User access token with ads permissions'}
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
        </Grid>

        <Box sx={{ mt: 3 }}>
          <Button 
            variant="outlined" 
            href="https://developer.twitter.com/en/portal/projects-and-apps"
            target="_blank"
          >
            Get Twitter Ads API Access
          </Button>
        </Box>
      </TabPanel>

      <TabPanel value={tabValue} index={1}>
        <Typography variant="h6" gutterBottom>
          Default Campaign Settings
        </Typography>
        
        <Grid container spacing={3}>
          <Grid item xs={12}>
            <FormControl fullWidth margin="normal">
              <InputLabel>Default Campaign Objective</InputLabel>
              <Select
                value={configuration.defaultObjective || 'TWEET_ENGAGEMENTS'}
                onChange={(e) => updateConfiguration('defaultObjective', e.target.value)}
              >
                {campaignObjectives.map((obj) => (
                  <MenuItem key={obj.value} value={obj.value}>
                    {obj.label}
                  </MenuItem>
                ))}
              </Select>
              <FormHelperText>Default objective for new campaigns</FormHelperText>
            </FormControl>
          </Grid>

          <Grid item xs={12} md={6}>
            <FormControl fullWidth margin="normal">
              <InputLabel>Default Bid Type</InputLabel>
              <Select
                value={configuration.defaultBidType || 'AUTO'}
                onChange={(e) => updateConfiguration('defaultBidType', e.target.value)}
              >
                {bidTypes.map((type) => (
                  <MenuItem key={type.value} value={type.value}>
                    {type.label}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          </Grid>

          <Grid item xs={12} md={6}>
            <FormControl fullWidth margin="normal">
              <InputLabel>Default Placements</InputLabel>
              <Select
                multiple
                value={configuration.defaultPlacements || ['ALL_ON_TWITTER']}
                onChange={(e) => updateConfiguration('defaultPlacements', e.target.value)}
                renderValue={(selected) => (
                  <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                    {(selected as string[]).map((value) => (
                      <Chip key={value} label={value} size="small" />
                    ))}
                  </Box>
                )}
              >
                {placementTypes.map((placement) => (
                  <MenuItem key={placement.value} value={placement.value}>
                    {placement.label}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          </Grid>

          <Grid item xs={12}>
            <FormControl fullWidth margin="normal">
              <InputLabel>Funding Instrument Type</InputLabel>
              <Select
                value={configuration.fundingInstrumentType || 'CREDIT_CARD'}
                onChange={(e) => updateConfiguration('fundingInstrumentType', e.target.value)}
              >
                <MenuItem value="CREDIT_CARD">Credit Card</MenuItem>
                <MenuItem value="AGENCY_CREDIT_LINE">Agency Credit Line</MenuItem>
                <MenuItem value="INSERTION_ORDER">Insertion Order</MenuItem>
                <MenuItem value="CREDIT_LINE">Credit Line</MenuItem>
              </Select>
            </FormControl>
          </Grid>
        </Grid>
      </TabPanel>

      <TabPanel value={tabValue} index={2}>
        <Typography variant="h6" gutterBottom>
          Campaign Management Features
        </Typography>
        <FormGroup>
          <Grid container spacing={2}>
            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableCampaignManagement ?? true}
                    onChange={(e) => updateConfiguration('features.enableCampaignManagement', e.target.checked)}
                  />
                }
                label="Campaign Management"
              />
              <FormHelperText>Create, update, and manage campaigns</FormHelperText>
            </Grid>
            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableAdGroupManagement ?? true}
                    onChange={(e) => updateConfiguration('features.enableAdGroupManagement', e.target.checked)}
                  />
                }
                label="Ad Group Management"
              />
              <FormHelperText>Manage line items and ad groups</FormHelperText>
            </Grid>
            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableCreativeManagement ?? true}
                    onChange={(e) => updateConfiguration('features.enableCreativeManagement', e.target.checked)}
                  />
                }
                label="Creative Management"
              />
              <FormHelperText>Create and manage ad creatives</FormHelperText>
            </Grid>
            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableBudgetManagement ?? true}
                    onChange={(e) => updateConfiguration('features.enableBudgetManagement', e.target.checked)}
                  />
                }
                label="Budget Management"
              />
              <FormHelperText>Monitor and control spending</FormHelperText>
            </Grid>
          </Grid>
        </FormGroup>

        <Divider sx={{ my: 3 }} />

        <Typography variant="h6" gutterBottom>
          Ad Formats
        </Typography>
        <FormGroup>
          <Grid container spacing={2}>
            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enablePromotedTweets ?? true}
                    onChange={(e) => updateConfiguration('features.enablePromotedTweets', e.target.checked)}
                  />
                }
                label="Promoted Tweets"
              />
            </Grid>
            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enablePromotedAccounts ?? true}
                    onChange={(e) => updateConfiguration('features.enablePromotedAccounts', e.target.checked)}
                  />
                }
                label="Promoted Accounts"
              />
            </Grid>
            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enablePromotedTrends ?? false}
                    onChange={(e) => updateConfiguration('features.enablePromotedTrends', e.target.checked)}
                  />
                }
                label="Promoted Trends"
              />
            </Grid>
            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableVideoAds ?? true}
                    onChange={(e) => updateConfiguration('features.enableVideoAds', e.target.checked)}
                  />
                }
                label="Video Ads"
              />
            </Grid>
            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableCarouselAds ?? true}
                    onChange={(e) => updateConfiguration('features.enableCarouselAds', e.target.checked)}
                  />
                }
                label="Carousel Ads"
              />
            </Grid>
            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableMomentAds ?? false}
                    onChange={(e) => updateConfiguration('features.enableMomentAds', e.target.checked)}
                  />
                }
                label="Moment Ads"
              />
            </Grid>
          </Grid>
        </FormGroup>

        <Divider sx={{ my: 3 }} />

        <Typography variant="h6" gutterBottom>
          Card Types
        </Typography>
        <FormGroup>
          <Grid container spacing={2}>
            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableWebsiteCards ?? true}
                    onChange={(e) => updateConfiguration('features.enableWebsiteCards', e.target.checked)}
                  />
                }
                label="Website Cards"
              />
            </Grid>
            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableAppCards ?? true}
                    onChange={(e) => updateConfiguration('features.enableAppCards', e.target.checked)}
                  />
                }
                label="App Cards"
              />
            </Grid>
            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableLeadGenCards ?? true}
                    onChange={(e) => updateConfiguration('features.enableLeadGenCards', e.target.checked)}
                  />
                }
                label="Lead Generation Cards"
              />
            </Grid>
          </Grid>
        </FormGroup>
      </TabPanel>

      <TabPanel value={tabValue} index={3}>
        <Typography variant="h6" gutterBottom>
          Budget Limits
        </Typography>
        
        <Grid container spacing={3}>
          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              label="Minimum Daily Budget"
              type="number"
              value={configuration.limits?.minDailyBudget || 1}
              onChange={(e) => updateConfiguration('limits.minDailyBudget', parseFloat(e.target.value))}
              error={!!errors.minDailyBudget}
              helperText={errors.minDailyBudget || 'Minimum daily budget in USD'}
              margin="normal"
              InputProps={{
                startAdornment: <InputAdornment position="start">$</InputAdornment>,
              }}
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              label="Maximum Daily Budget"
              type="number"
              value={configuration.limits?.maxDailyBudget || 1000000}
              onChange={(e) => updateConfiguration('limits.maxDailyBudget', parseFloat(e.target.value))}
              error={!!errors.maxDailyBudget}
              helperText={errors.maxDailyBudget || 'Maximum daily budget in USD'}
              margin="normal"
              InputProps={{
                startAdornment: <InputAdornment position="start">$</InputAdornment>,
              }}
            />
          </Grid>
          <Grid item xs={12}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableBidding ?? true}
                  onChange={(e) => updateConfiguration('features.enableBidding', e.target.checked)}
                />
              }
              label="Enable Bidding Management"
            />
            <FormHelperText>
              Control bid strategies and amounts for campaigns
            </FormHelperText>
          </Grid>
          <Grid item xs={12}>
            <Alert severity="warning">
              Budget alerts will be triggered when campaigns reach 75%, 90%, and 100% of their daily budget.
            </Alert>
          </Grid>
        </Grid>

        <Divider sx={{ my: 3 }} />

        <Typography variant="h6" gutterBottom>
          Budget Alert Thresholds
        </Typography>
        
        <Grid container spacing={2}>
          {[50, 75, 90, 95].map((threshold) => (
            <Grid item xs={6} md={3} key={threshold}>
              <FormControlLabel
                control={
                  <Checkbox
                    checked={configuration.budgetAlertThresholds?.includes(threshold) ?? (threshold >= 75)}
                    onChange={(e) => {
                      const current = configuration.budgetAlertThresholds || [75, 90];
                      if (e.target.checked) {
                        updateConfiguration('budgetAlertThresholds', [...current, threshold].sort());
                      } else {
                        updateConfiguration('budgetAlertThresholds', current.filter((t: number) => t !== threshold));
                      }
                    }}
                  />
                }
                label={`${threshold}%`}
              />
            </Grid>
          ))}
        </Grid>
      </TabPanel>

      <TabPanel value={tabValue} index={4}>
        <Typography variant="h6" gutterBottom>
          Audience Features
        </Typography>
        <FormGroup>
          <Grid container spacing={2}>
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
            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableCustomAudiences ?? true}
                    onChange={(e) => updateConfiguration('features.enableCustomAudiences', e.target.checked)}
                  />
                }
                label="Custom Audiences"
              />
              <FormHelperText>Upload and manage custom audience lists</FormHelperText>
            </Grid>
            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableTailoredAudiences ?? true}
                    onChange={(e) => updateConfiguration('features.enableTailoredAudiences', e.target.checked)}
                  />
                }
                label="Tailored Audiences"
              />
              <FormHelperText>Create audiences from web activity</FormHelperText>
            </Grid>
          </Grid>
        </FormGroup>

        <Divider sx={{ my: 3 }} />

        <Typography variant="h6" gutterBottom>
          Audience Limits
        </Typography>
        
        <Grid container spacing={3}>
          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              label="Minimum Custom Audience Size"
              type="number"
              value={configuration.limits?.minCustomAudienceSize || 100}
              onChange={(e) => updateConfiguration('limits.minCustomAudienceSize', parseInt(e.target.value))}
              helperText="Minimum number of users for custom audience"
              margin="normal"
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              label="Maximum Custom Audience Size"
              type="number"
              value={configuration.limits?.maxCustomAudienceSize || 500000}
              onChange={(e) => updateConfiguration('limits.maxCustomAudienceSize', parseInt(e.target.value))}
              helperText="Maximum number of users per custom audience"
              margin="normal"
            />
          </Grid>
        </Grid>

        <Divider sx={{ my: 3 }} />

        <Typography variant="h6" gutterBottom>
          Targeting Options
        </Typography>
        
        <FormControl fullWidth margin="normal">
          <InputLabel>Enabled Targeting Types</InputLabel>
          <Select
            multiple
            value={configuration.enabledTargetingTypes || ['LOCATION', 'GENDER', 'AGE', 'LANGUAGE', 'INTEREST', 'KEYWORD']}
            onChange={(e) => updateConfiguration('enabledTargetingTypes', e.target.value)}
            renderValue={(selected) => (
              <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                {(selected as string[]).map((value) => (
                  <Chip key={value} label={value} size="small" />
                ))}
              </Box>
            )}
          >
            {['LOCATION', 'GENDER', 'AGE', 'LANGUAGE', 'INTEREST', 'KEYWORD', 
              'FOLLOWER', 'DEVICE', 'BEHAVIOR', 'PLATFORM', 'CARRIER', 
              'TV_SHOW', 'TV_GENRE', 'EVENT'].map((type) => (
              <MenuItem key={type} value={type}>
                {type.replace(/_/g, ' ')}
              </MenuItem>
            ))}
          </Select>
          <FormHelperText>Select which targeting options to enable</FormHelperText>
        </FormControl>
      </TabPanel>

      <TabPanel value={tabValue} index={5}>
        <Typography variant="h6" gutterBottom>
          Analytics & Reporting
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
                label="Analytics"
              />
              <FormHelperText>Track campaign performance metrics</FormHelperText>
            </Grid>
            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableReporting ?? true}
                    onChange={(e) => updateConfiguration('features.enableReporting', e.target.checked)}
                  />
                }
                label="Custom Reporting"
              />
              <FormHelperText>Generate custom reports</FormHelperText>
            </Grid>
            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableConversionTracking ?? true}
                    onChange={(e) => updateConfiguration('features.enableConversionTracking', e.target.checked)}
                  />
                }
                label="Conversion Tracking"
              />
              <FormHelperText>Track conversions and ROI</FormHelperText>
            </Grid>
          </Grid>
        </FormGroup>

        <Divider sx={{ my: 3 }} />

        <Typography variant="h6" gutterBottom>
          Metrics Configuration
        </Typography>
        
        <FormControl fullWidth margin="normal">
          <InputLabel>Default Metrics</InputLabel>
          <Select
            multiple
            value={configuration.defaultMetrics || ['IMPRESSIONS', 'ENGAGEMENTS', 'CLICKS', 'SPEND']}
            onChange={(e) => updateConfiguration('defaultMetrics', e.target.value)}
            renderValue={(selected) => (
              <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                {(selected as string[]).map((value) => (
                  <Chip key={value} label={value} size="small" />
                ))}
              </Box>
            )}
          >
            {['IMPRESSIONS', 'ENGAGEMENTS', 'ENGAGEMENT_RATE', 'RETWEETS', 
              'REPLIES', 'LIKES', 'FOLLOWS', 'CLICKS', 'LINK_CLICKS', 
              'APP_CLICKS', 'APP_INSTALLS', 'VIDEO_VIEWS', 'SPEND', 
              'COST_PER_ENGAGEMENT', 'COST_PER_FOLLOWER', 'CONVERSION_PURCHASES',
              'CONVERSION_SIGN_UPS', 'CONVERSION_DOWNLOADS'].map((metric) => (
              <MenuItem key={metric} value={metric}>
                {metric.replace(/_/g, ' ').toLowerCase()}
              </MenuItem>
            ))}
          </Select>
          <FormHelperText>Select default metrics to track</FormHelperText>
        </FormControl>

        <FormControl fullWidth margin="normal">
          <InputLabel>Default Report Granularity</InputLabel>
          <Select
            value={configuration.defaultGranularity || 'DAY'}
            onChange={(e) => updateConfiguration('defaultGranularity', e.target.value)}
          >
            <MenuItem value="HOUR">Hourly</MenuItem>
            <MenuItem value="DAY">Daily</MenuItem>
            <MenuItem value="TOTAL">Total</MenuItem>
          </Select>
        </FormControl>

        <Divider sx={{ my: 3 }} />

        <Typography variant="h6" gutterBottom>
          Polling Intervals
        </Typography>
        
        <Grid container spacing={3}>
          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              label="Campaign Performance Polling (minutes)"
              type="number"
              value={(configuration.polling?.campaignPollingInterval || 300000) / 60000}
              onChange={(e) => updateConfiguration('polling.campaignPollingInterval', parseInt(e.target.value) * 60000)}
              helperText="How often to check campaign performance"
              margin="normal"
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              label="Conversion Events Polling (minutes)"
              type="number"
              value={(configuration.polling?.conversionPollingInterval || 1800000) / 60000}
              onChange={(e) => updateConfiguration('polling.conversionPollingInterval', parseInt(e.target.value) * 60000)}
              helperText="How often to check for conversion events"
              margin="normal"
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              label="Budget Check Interval (minutes)"
              type="number"
              value={(configuration.polling?.budgetCheckInterval || 600000) / 60000}
              onChange={(e) => updateConfiguration('polling.budgetCheckInterval', parseInt(e.target.value) * 60000)}
              helperText="How often to check budget alerts"
              margin="normal"
            />
          </Grid>
        </Grid>
      </TabPanel>

      <TabPanel value={tabValue} index={6}>
        <Accordion defaultExpanded>
          <AccordionSummary expandIcon={<ExpandMoreIcon />}>
            <Typography variant="h6">Campaign & Ad Group Limits</Typography>
          </AccordionSummary>
          <AccordionDetails>
            <Grid container spacing={3}>
              <Grid item xs={12} md={6}>
                <TextField
                  fullWidth
                  label="Max Campaigns Per Account"
                  type="number"
                  value={configuration.limits?.maxCampaignsPerAccount || 200}
                  onChange={(e) => updateConfiguration('limits.maxCampaignsPerAccount', parseInt(e.target.value))}
                  error={!!errors.maxCampaignsPerAccount}
                  helperText={errors.maxCampaignsPerAccount || 'Maximum: 200'}
                  margin="normal"
                />
              </Grid>
              <Grid item xs={12} md={6}>
                <TextField
                  fullWidth
                  label="Max Ad Groups Per Campaign"
                  type="number"
                  value={configuration.limits?.maxAdGroupsPerCampaign || 100}
                  onChange={(e) => updateConfiguration('limits.maxAdGroupsPerCampaign', parseInt(e.target.value))}
                  margin="normal"
                />
              </Grid>
              <Grid item xs={12} md={6}>
                <TextField
                  fullWidth
                  label="Max Ads Per Ad Group"
                  type="number"
                  value={configuration.limits?.maxAdsPerAdGroup || 50}
                  onChange={(e) => updateConfiguration('limits.maxAdsPerAdGroup', parseInt(e.target.value))}
                  margin="normal"
                />
              </Grid>
              <Grid item xs={12} md={6}>
                <TextField
                  fullWidth
                  label="Max Keywords Per Ad Group"
                  type="number"
                  value={configuration.limits?.maxKeywordsPerAdGroup || 1000}
                  onChange={(e) => updateConfiguration('limits.maxKeywordsPerAdGroup', parseInt(e.target.value))}
                  margin="normal"
                />
              </Grid>
              <Grid item xs={12} md={6}>
                <TextField
                  fullWidth
                  label="Max Targeting Criteria Per Ad Group"
                  type="number"
                  value={configuration.limits?.maxTargetingCriteriaPerAdGroup || 50}
                  onChange={(e) => updateConfiguration('limits.maxTargetingCriteriaPerAdGroup', parseInt(e.target.value))}
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
              Twitter Ads API has specific rate limits per endpoint. Configure these settings to stay within limits.
            </Alert>
            <Grid container spacing={3}>
              <Grid item xs={12} md={6}>
                <TextField
                  fullWidth
                  label="Max API Calls Per Window"
                  type="number"
                  value={configuration.rateLimiting?.maxCallsPerWindow || 15}
                  onChange={(e) => updateConfiguration('rateLimiting.maxCallsPerWindow', parseInt(e.target.value))}
                  helperText="Maximum calls per 15-minute window"
                  margin="normal"
                />
              </Grid>
              <Grid item xs={12} md={6}>
                <TextField
                  fullWidth
                  label="Rate Limit Window (minutes)"
                  type="number"
                  value={configuration.rateLimiting?.windowMinutes || 15}
                  onChange={(e) => updateConfiguration('rateLimiting.windowMinutes', parseInt(e.target.value))}
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
                  value={configuration.apiVersion || '12'}
                  onChange={(e) => updateConfiguration('apiVersion', e.target.value)}
                  helperText="Twitter Ads API version (default: 12)"
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
              <li>Always test campaigns with small budgets first</li>
              <li>Use Custom Audiences for better targeting</li>
              <li>Monitor spend closely with budget alerts</li>
              <li>Leverage A/B testing for creative optimization</li>
              <li>Use conversion tracking to measure ROI</li>
              <li>Review performance metrics regularly</li>
            </ul>
          </Alert>
        </Box>
      </TabPanel>
    </Box>
  );
};

export default TwitterAdsApiConfiguration;