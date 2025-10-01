import React, { useState } from 'react';
import {
  Grid,
  TextField,
  FormControlLabel,
  Switch,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  Paper,
  Typography,
  Box,
  Tabs,
  Tab,
  Chip,
  IconButton,
  Button,
  Alert,
  Divider,
  List,
  ListItem,
  ListItemText,
  ListItemSecondaryAction,
  FormHelperText
} from '@mui/material';
import {
  Campaign,
  Analytics,
  People,
  Settings,
  AttachMoney,
  BarChart,
  Add,
  Delete,
  Assessment,
  Groups,
  TrendingUp,
  CreditCard
} from '@mui/icons-material';

interface LinkedInAdsApiConfigurationProps {
  configuration: any;
  onChange: (config: any) => void;
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

const campaignObjectives = [
  { value: 'BRAND_AWARENESS', label: 'Brand Awareness - Increase visibility' },
  { value: 'WEBSITE_VISITS', label: 'Website Visits - Drive traffic' },
  { value: 'ENGAGEMENT', label: 'Engagement - Get likes, comments, shares' },
  { value: 'VIDEO_VIEWS', label: 'Video Views - Promote video content' },
  { value: 'LEAD_GENERATION', label: 'Lead Generation - Collect leads' },
  { value: 'WEBSITE_CONVERSIONS', label: 'Website Conversions - Drive actions' },
  { value: 'JOB_APPLICANTS', label: 'Job Applicants - Find candidates' },
  { value: 'TALENT_LEADS', label: 'Talent Leads - Build talent pipeline' },
];

const campaignTypes = [
  { value: 'SPONSORED_CONTENT', label: 'Sponsored Content - Feed ads' },
  { value: 'SPONSORED_MESSAGING', label: 'Sponsored Messaging - InMail ads' },
  { value: 'TEXT_ADS', label: 'Text Ads - Simple text ads' },
  { value: 'DYNAMIC_ADS', label: 'Dynamic Ads - Personalized ads' },
  { value: 'SPONSORED_JOBS', label: 'Sponsored Jobs - Job postings' },
];

const adFormats = [
  { value: 'SINGLE_IMAGE', label: 'Single Image' },
  { value: 'CAROUSEL', label: 'Carousel - Multiple images' },
  { value: 'VIDEO', label: 'Video' },
  { value: 'TEXT_AD', label: 'Text Ad' },
  { value: 'SPOTLIGHT_AD', label: 'Spotlight Ad' },
  { value: 'MESSAGE_AD', label: 'Message Ad' },
  { value: 'CONVERSATION_AD', label: 'Conversation Ad' },
  { value: 'EVENT_AD', label: 'Event Ad' },
  { value: 'DOCUMENT_AD', label: 'Document Ad' },
  { value: 'SINGLE_JOB_AD', label: 'Single Job Ad' },
  { value: 'FOLLOWER_AD', label: 'Follower Ad' },
];

const bidTypes = [
  { value: 'CPM', label: 'CPM - Cost per 1000 impressions' },
  { value: 'CPC', label: 'CPC - Cost per click' },
  { value: 'CPV', label: 'CPV - Cost per video view' },
  { value: 'CPS', label: 'CPS - Cost per send (message ads)' },
];

const optimizationTargets = [
  { value: 'REACH', label: 'Reach - Maximize unique viewers' },
  { value: 'IMPRESSIONS', label: 'Impressions - Maximize views' },
  { value: 'CLICKS', label: 'Clicks - Get link clicks' },
  { value: 'LANDING_PAGE_CLICKS', label: 'Landing Page Clicks' },
  { value: 'EXTERNAL_WEBSITE_CONVERSIONS', label: 'Website Conversions' },
  { value: 'LEAD_GENERATION_MAIL_CONTACT_INFO', label: 'Lead Gen - Contact Info' },
  { value: 'LEAD_GENERATION_MAIL_INTEREST', label: 'Lead Gen - Interest' },
];

const targetingCriteria = [
  { value: 'LOCATION', label: 'Location' },
  { value: 'COMPANY', label: 'Company' },
  { value: 'COMPANY_SIZE', label: 'Company Size' },
  { value: 'COMPANY_INDUSTRY', label: 'Industry' },
  { value: 'JOB_TITLE', label: 'Job Title' },
  { value: 'JOB_FUNCTION', label: 'Job Function' },
  { value: 'JOB_SENIORITY', label: 'Seniority Level' },
  { value: 'SKILLS', label: 'Skills' },
  { value: 'DEGREES', label: 'Degrees' },
  { value: 'FIELDS_OF_STUDY', label: 'Fields of Study' },
  { value: 'SCHOOLS', label: 'Schools' },
  { value: 'AGE', label: 'Age Range' },
  { value: 'GENDER', label: 'Gender' },
  { value: 'MEMBER_INTERESTS', label: 'Member Interests' },
  { value: 'MEMBER_GROUPS', label: 'LinkedIn Groups' },
  { value: 'MATCHED_AUDIENCES', label: 'Matched Audiences' },
  { value: 'LOOKALIKE_AUDIENCES', label: 'Lookalike Audiences' },
];

const conversionTypes = [
  { value: 'ADD_TO_CART', label: 'Add to Cart' },
  { value: 'DOWNLOAD', label: 'Download' },
  { value: 'INSTALL', label: 'Install' },
  { value: 'KEY_PAGE_VIEW', label: 'Key Page View' },
  { value: 'LEAD', label: 'Lead' },
  { value: 'PURCHASE', label: 'Purchase' },
  { value: 'SIGN_UP', label: 'Sign Up' },
  { value: 'OTHER', label: 'Other' },
];

const metricTypes = [
  { value: 'IMPRESSIONS', label: 'Impressions' },
  { value: 'CLICKS', label: 'Clicks' },
  { value: 'CTR', label: 'Click-through Rate' },
  { value: 'AVERAGE_CPC', label: 'Average CPC' },
  { value: 'AVERAGE_CPM', label: 'Average CPM' },
  { value: 'SPEND', label: 'Total Spend' },
  { value: 'REACH', label: 'Reach' },
  { value: 'FREQUENCY', label: 'Frequency' },
  { value: 'VIDEO_VIEWS', label: 'Video Views' },
  { value: 'VIDEO_COMPLETION_RATE', label: 'Video Completion Rate' },
  { value: 'CONVERSIONS', label: 'Conversions' },
  { value: 'CONVERSION_RATE', label: 'Conversion Rate' },
  { value: 'COST_PER_CONVERSION', label: 'Cost per Conversion' },
  { value: 'LEADS', label: 'Leads Generated' },
  { value: 'COST_PER_LEAD', label: 'Cost per Lead' },
  { value: 'ENGAGEMENT_RATE', label: 'Engagement Rate' },
];

const timeGranularities = [
  { value: 'DAILY', label: 'Daily' },
  { value: 'MONTHLY', label: 'Monthly' },
  { value: 'ALL', label: 'All Time' },
];

export const LinkedInAdsApiConfiguration: React.FC<LinkedInAdsApiConfigurationProps> = ({
  configuration,
  onChange
}) => {
  const [activeTab, setActiveTab] = useState(0);
  const [errors, setErrors] = useState<Record<string, string>>({});

  const handleTabChange = (event: React.SyntheticEvent, newValue: number) => {
    setActiveTab(newValue);
  };

  const updateConfig = (key: string, value: any) => {
    onChange({
      ...configuration,
      [key]: value
    });
  };

  const updateFeatures = (feature: string, value: boolean) => {
    onChange({
      ...configuration,
      features: {
        ...configuration.features,
        [feature]: value
      }
    });
  };

  const updateLimits = (limit: string, value: any) => {
    onChange({
      ...configuration,
      limits: {
        ...configuration.limits,
        [limit]: value
      }
    });
  };

  const validateConfig = () => {
    const newErrors: Record<string, string> = {};
    
    if (!configuration.clientId) {
      newErrors.clientId = 'Client ID is required';
    }
    if (!configuration.clientSecret) {
      newErrors.clientSecret = 'Client Secret is required';
    }
    if (!configuration.adAccountId) {
      newErrors.adAccountId = 'Ad Account ID is required';
    }
    
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  return (
      <Box sx={{ width: '100%' }}>
        <Paper sx={{ mb: 2 }}>
          <Tabs
            value={activeTab}
            onChange={handleTabChange}
            variant="scrollable"
            scrollButtons="auto"
          >
            <Tab label="Basic Setup" icon={<Settings />} iconPosition="start" />
            <Tab label="Campaign Settings" icon={<Campaign />} iconPosition="start" />
            <Tab label="Ad Formats" icon={<AttachMoney />} iconPosition="start" />
            <Tab label="Targeting" icon={<People />} iconPosition="start" />
            <Tab label="Budget & Bidding" icon={<CreditCard />} iconPosition="start" />
            <Tab label="Analytics" icon={<Analytics />} iconPosition="start" />
            <Tab label="Advanced" icon={<TrendingUp />} iconPosition="start" />
          </Tabs>
        </Paper>

        <TabPanel value={activeTab} index={0}>
          <Grid container spacing={3}>
            <Grid item xs={12}>
              <Alert severity="info">
                Configure your LinkedIn Ads API credentials to manage campaigns, create ads, and track performance.
                You'll need a LinkedIn Ads account with API access.
              </Alert>
            </Grid>
            
            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label="Client ID"
                value={configuration.clientId || ''}
                onChange={(e) => updateConfig('clientId', e.target.value)}
                error={!!errors.clientId}
                helperText={errors.clientId || 'LinkedIn App Client ID'}
                required
              />
            </Grid>
            
            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label="Client Secret"
                type="password"
                value={configuration.clientSecret || ''}
                onChange={(e) => updateConfig('clientSecret', e.target.value)}
                error={!!errors.clientSecret}
                helperText={errors.clientSecret || 'LinkedIn App Client Secret'}
                required
              />
            </Grid>
            
            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label="Ad Account ID"
                value={configuration.adAccountId || ''}
                onChange={(e) => updateConfig('adAccountId', e.target.value)}
                error={!!errors.adAccountId}
                helperText={errors.adAccountId || 'Your LinkedIn Ads Account ID'}
                required
              />
            </Grid>
            
            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label="Organization URN"
                value={configuration.organizationUrn || ''}
                onChange={(e) => updateConfig('organizationUrn', e.target.value)}
                helperText="Organization URN for company ads (optional)"
              />
            </Grid>
            
            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label="Access Token"
                type="password"
                value={configuration.accessToken || ''}
                onChange={(e) => updateConfig('accessToken', e.target.value)}
                helperText="OAuth2 Access Token"
              />
            </Grid>
            
            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label="Refresh Token"
                type="password"
                value={configuration.refreshToken || ''}
                onChange={(e) => updateConfig('refreshToken', e.target.value)}
                helperText="OAuth2 Refresh Token for automatic renewal"
              />
            </Grid>
            
            <Grid item xs={12}>
              <Typography variant="h6" gutterBottom>
                API Features
              </Typography>
              <Divider sx={{ mb: 2 }} />
              <Grid container spacing={2}>
                <Grid item xs={12} md={6}>
                  <FormControlLabel
                    control={
                      <Switch
                        checked={configuration.features?.enableCampaignManagement ?? true}
                        onChange={(e) => updateFeatures('enableCampaignManagement', e.target.checked)}
                      />
                    }
                    label="Campaign Management"
                  />
                  <FormHelperText>Create and manage ad campaigns</FormHelperText>
                </Grid>
                
                <Grid item xs={12} md={6}>
                  <FormControlLabel
                    control={
                      <Switch
                        checked={configuration.features?.enableAdCreativeManagement ?? true}
                        onChange={(e) => updateFeatures('enableAdCreativeManagement', e.target.checked)}
                      />
                    }
                    label="Ad Creative Management"
                  />
                  <FormHelperText>Create and manage ad creatives</FormHelperText>
                </Grid>
                
                <Grid item xs={12} md={6}>
                  <FormControlLabel
                    control={
                      <Switch
                        checked={configuration.features?.enableAudienceTargeting ?? true}
                        onChange={(e) => updateFeatures('enableAudienceTargeting', e.target.checked)}
                      />
                    }
                    label="Audience Targeting"
                  />
                  <FormHelperText>Advanced audience targeting options</FormHelperText>
                </Grid>
                
                <Grid item xs={12} md={6}>
                  <FormControlLabel
                    control={
                      <Switch
                        checked={configuration.features?.enableAnalytics ?? true}
                        onChange={(e) => updateFeatures('enableAnalytics', e.target.checked)}
                      />
                    }
                    label="Analytics & Reporting"
                  />
                  <FormHelperText>Track campaign performance</FormHelperText>
                </Grid>
              </Grid>
            </Grid>
          </Grid>
        </TabPanel>

        <TabPanel value={activeTab} index={1}>
          <Grid container spacing={3}>
            <Grid item xs={12}>
              <Typography variant="h6" gutterBottom>
                Campaign Configuration
              </Typography>
              <Divider sx={{ mb: 2 }} />
            </Grid>
            
            <Grid item xs={12} md={6}>
              <FormControl fullWidth>
                <InputLabel>Default Campaign Objective</InputLabel>
                <Select
                  value={configuration.defaultCampaignObjective || ''}
                  onChange={(e) => updateConfig('defaultCampaignObjective', e.target.value)}
                  label="Default Campaign Objective"
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
              <FormControl fullWidth>
                <InputLabel>Default Campaign Type</InputLabel>
                <Select
                  value={configuration.defaultCampaignType || ''}
                  onChange={(e) => updateConfig('defaultCampaignType', e.target.value)}
                  label="Default Campaign Type"
                >
                  {campaignTypes.map((type) => (
                    <MenuItem key={type.value} value={type.value}>
                      {type.label}
                    </MenuItem>
                  ))}
                </Select>
                <FormHelperText>Default type for new campaigns</FormHelperText>
              </FormControl>
            </Grid>
            
            <Grid item xs={12}>
              <Typography variant="subtitle1" gutterBottom sx={{ mt: 2 }}>
                Campaign Features
              </Typography>
              <Grid container spacing={2}>
                <Grid item xs={12} md={6}>
                  <FormControlLabel
                    control={
                      <Switch
                        checked={configuration.features?.enableBoostPosts ?? true}
                        onChange={(e) => updateFeatures('enableBoostPosts', e.target.checked)}
                      />
                    }
                    label="Boost Posts"
                  />
                  <FormHelperText>Promote existing LinkedIn posts</FormHelperText>
                </Grid>
                
                <Grid item xs={12} md={6}>
                  <FormControlLabel
                    control={
                      <Switch
                        checked={configuration.features?.enableABTesting ?? true}
                        onChange={(e) => updateFeatures('enableABTesting', e.target.checked)}
                      />
                    }
                    label="A/B Testing"
                  />
                  <FormHelperText>Test different ad variations</FormHelperText>
                </Grid>
                
                <Grid item xs={12} md={6}>
                  <FormControlLabel
                    control={
                      <Switch
                        checked={configuration.features?.enableDynamicAds ?? true}
                        onChange={(e) => updateFeatures('enableDynamicAds', e.target.checked)}
                      />
                    }
                    label="Dynamic Ads"
                  />
                  <FormHelperText>Personalized ads based on profile data</FormHelperText>
                </Grid>
                
                <Grid item xs={12} md={6}>
                  <FormControlLabel
                    control={
                      <Switch
                        checked={configuration.features?.enableJobAds ?? true}
                        onChange={(e) => updateFeatures('enableJobAds', e.target.checked)}
                      />
                    }
                    label="Job Ads"
                  />
                  <FormHelperText>Promote job postings</FormHelperText>
                </Grid>
              </Grid>
            </Grid>
          </Grid>
        </TabPanel>

        <TabPanel value={activeTab} index={2}>
          <Grid container spacing={3}>
            <Grid item xs={12}>
              <Typography variant="h6" gutterBottom>
                Ad Format Settings
              </Typography>
              <Divider sx={{ mb: 2 }} />
            </Grid>
            
            <Grid item xs={12}>
              <Typography variant="subtitle1" gutterBottom>
                Enabled Ad Formats
              </Typography>
              <Grid container spacing={2}>
                <Grid item xs={12} md={6}>
                  <FormControlLabel
                    control={
                      <Switch
                        checked={configuration.features?.enableSponsoredContent ?? true}
                        onChange={(e) => updateFeatures('enableSponsoredContent', e.target.checked)}
                      />
                    }
                    label="Sponsored Content"
                  />
                  <FormHelperText>Native ads in the LinkedIn feed</FormHelperText>
                </Grid>
                
                <Grid item xs={12} md={6}>
                  <FormControlLabel
                    control={
                      <Switch
                        checked={configuration.features?.enableSponsoredMessaging ?? true}
                        onChange={(e) => updateFeatures('enableSponsoredMessaging', e.target.checked)}
                      />
                    }
                    label="Sponsored Messaging"
                  />
                  <FormHelperText>InMail and Message Ads</FormHelperText>
                </Grid>
                
                <Grid item xs={12} md={6}>
                  <FormControlLabel
                    control={
                      <Switch
                        checked={configuration.features?.enableTextAds ?? true}
                        onChange={(e) => updateFeatures('enableTextAds', e.target.checked)}
                      />
                    }
                    label="Text Ads"
                  />
                  <FormHelperText>Simple text-based ads</FormHelperText>
                </Grid>
                
                <Grid item xs={12} md={6}>
                  <FormControlLabel
                    control={
                      <Switch
                        checked={configuration.features?.enableVideoAds ?? true}
                        onChange={(e) => updateFeatures('enableVideoAds', e.target.checked)}
                      />
                    }
                    label="Video Ads"
                  />
                  <FormHelperText>Video content ads</FormHelperText>
                </Grid>
                
                <Grid item xs={12} md={6}>
                  <FormControlLabel
                    control={
                      <Switch
                        checked={configuration.features?.enableCarouselAds ?? true}
                        onChange={(e) => updateFeatures('enableCarouselAds', e.target.checked)}
                      />
                    }
                    label="Carousel Ads"
                  />
                  <FormHelperText>Multi-image carousel ads</FormHelperText>
                </Grid>
                
                <Grid item xs={12} md={6}>
                  <FormControlLabel
                    control={
                      <Switch
                        checked={configuration.features?.enableEventAds ?? true}
                        onChange={(e) => updateFeatures('enableEventAds', e.target.checked)}
                      />
                    }
                    label="Event Ads"
                  />
                  <FormHelperText>Promote LinkedIn Events</FormHelperText>
                </Grid>
                
                <Grid item xs={12} md={6}>
                  <FormControlLabel
                    control={
                      <Switch
                        checked={configuration.features?.enableFollowerAds ?? true}
                        onChange={(e) => updateFeatures('enableFollowerAds', e.target.checked)}
                      />
                    }
                    label="Follower Ads"
                  />
                  <FormHelperText>Grow page followers</FormHelperText>
                </Grid>
                
                <Grid item xs={12} md={6}>
                  <FormControlLabel
                    control={
                      <Switch
                        checked={configuration.features?.enableSpotlightAds ?? true}
                        onChange={(e) => updateFeatures('enableSpotlightAds', e.target.checked)}
                      />
                    }
                    label="Spotlight Ads"
                  />
                  <FormHelperText>Dynamic personalized ads</FormHelperText>
                </Grid>
              </Grid>
            </Grid>
            
            <Grid item xs={12} md={6}>
              <FormControl fullWidth>
                <InputLabel>Default Ad Format</InputLabel>
                <Select
                  value={configuration.defaultAdFormat || ''}
                  onChange={(e) => updateConfig('defaultAdFormat', e.target.value)}
                  label="Default Ad Format"
                >
                  {adFormats.map((format) => (
                    <MenuItem key={format.value} value={format.value}>
                      {format.label}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>
            
            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label="Max Creatives per Campaign"
                type="number"
                value={configuration.limits?.maxCreativesPerCampaign || 100}
                onChange={(e) => updateLimits('maxCreativesPerCampaign', parseInt(e.target.value))}
                helperText="Maximum number of creatives per campaign"
                InputProps={{
                  inputProps: { min: 1, max: 200 }
                }}
              />
            </Grid>
          </Grid>
        </TabPanel>

        <TabPanel value={activeTab} index={3}>
          <Grid container spacing={3}>
            <Grid item xs={12}>
              <Typography variant="h6" gutterBottom>
                Audience Targeting Options
              </Typography>
              <Divider sx={{ mb: 2 }} />
            </Grid>
            
            <Grid item xs={12}>
              <Typography variant="subtitle1" gutterBottom>
                Targeting Features
              </Typography>
              <Grid container spacing={2}>
                <Grid item xs={12} md={6}>
                  <FormControlLabel
                    control={
                      <Switch
                        checked={configuration.features?.enableMatchedAudiences ?? true}
                        onChange={(e) => updateFeatures('enableMatchedAudiences', e.target.checked)}
                      />
                    }
                    label="Matched Audiences"
                  />
                  <FormHelperText>Target based on your data (emails, etc.)</FormHelperText>
                </Grid>
                
                <Grid item xs={12} md={6}>
                  <FormControlLabel
                    control={
                      <Switch
                        checked={configuration.features?.enableLookalikeAudiences ?? true}
                        onChange={(e) => updateFeatures('enableLookalikeAudiences', e.target.checked)}
                      />
                    }
                    label="Lookalike Audiences"
                  />
                  <FormHelperText>Find similar professionals</FormHelperText>
                </Grid>
              </Grid>
            </Grid>
            
            <Grid item xs={12}>
              <Typography variant="subtitle1" gutterBottom sx={{ mt: 2 }}>
                Available Targeting Criteria
              </Typography>
              <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                {targetingCriteria.map((criteria) => (
                  <Chip
                    key={criteria.value}
                    label={criteria.label}
                    variant="outlined"
                    color="primary"
                  />
                ))}
              </Box>
            </Grid>
            
            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label="Min Audience Size"
                type="number"
                value={configuration.limits?.minAudienceSize || 300}
                onChange={(e) => updateLimits('minAudienceSize', parseInt(e.target.value))}
                helperText="Minimum audience size for campaigns"
                InputProps={{
                  inputProps: { min: 300 }
                }}
              />
            </Grid>
            
            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label="Max Audience Size"
                type="number"
                value={configuration.limits?.maxAudienceSize || 300000000}
                onChange={(e) => updateLimits('maxAudienceSize', parseInt(e.target.value))}
                helperText="Maximum audience size for campaigns"
              />
            </Grid>
            
            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label="Max Custom Audiences"
                type="number"
                value={configuration.limits?.maxCustomAudiences || 500}
                onChange={(e) => updateLimits('maxCustomAudiences', parseInt(e.target.value))}
                helperText="Maximum number of custom audiences"
                InputProps={{
                  inputProps: { min: 1, max: 1000 }
                }}
              />
            </Grid>
            
            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label="Max Lookalike Audiences"
                type="number"
                value={configuration.limits?.maxLookalikeAudiences || 100}
                onChange={(e) => updateLimits('maxLookalikeAudiences', parseInt(e.target.value))}
                helperText="Maximum number of lookalike audiences"
                InputProps={{
                  inputProps: { min: 1, max: 200 }
                }}
              />
            </Grid>
          </Grid>
        </TabPanel>

        <TabPanel value={activeTab} index={4}>
          <Grid container spacing={3}>
            <Grid item xs={12}>
              <Typography variant="h6" gutterBottom>
                Budget & Bidding Configuration
              </Typography>
              <Divider sx={{ mb: 2 }} />
            </Grid>
            
            <Grid item xs={12}>
              <Alert severity="info">
                Set default budget limits and bidding strategies for your campaigns.
                All values are in USD.
              </Alert>
            </Grid>
            
            <Grid item xs={12}>
              <Typography variant="subtitle1" gutterBottom>
                Budget Management
              </Typography>
              <Grid container spacing={2}>
                <Grid item xs={12} md={6}>
                  <FormControlLabel
                    control={
                      <Switch
                        checked={configuration.features?.enableBudgetManagement ?? true}
                        onChange={(e) => updateFeatures('enableBudgetManagement', e.target.checked)}
                      />
                    }
                    label="Budget Management"
                  />
                  <FormHelperText>Control campaign spending</FormHelperText>
                </Grid>
                
                <Grid item xs={12} md={6}>
                  <FormControlLabel
                    control={
                      <Switch
                        checked={configuration.features?.enableBidding ?? true}
                        onChange={(e) => updateFeatures('enableBidding', e.target.checked)}
                      />
                    }
                    label="Bidding Controls"
                  />
                  <FormHelperText>Manage bid strategies</FormHelperText>
                </Grid>
              </Grid>
            </Grid>
            
            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label="Min Daily Budget (USD)"
                type="number"
                value={configuration.limits?.minDailyBudget || 10}
                onChange={(e) => updateLimits('minDailyBudget', parseFloat(e.target.value))}
                helperText="Minimum daily budget for campaigns"
                InputProps={{
                  startAdornment: '$',
                  inputProps: { min: 10, step: 0.01 }
                }}
              />
            </Grid>
            
            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label="Max Daily Budget (USD)"
                type="number"
                value={configuration.limits?.maxDailyBudget || 100000}
                onChange={(e) => updateLimits('maxDailyBudget', parseFloat(e.target.value))}
                helperText="Maximum daily budget for campaigns"
                InputProps={{
                  startAdornment: '$',
                  inputProps: { min: 10, step: 0.01 }
                }}
              />
            </Grid>
            
            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label="Min Lifetime Budget (USD)"
                type="number"
                value={configuration.limits?.minLifetimeBudget || 10}
                onChange={(e) => updateLimits('minLifetimeBudget', parseFloat(e.target.value))}
                helperText="Minimum lifetime budget for campaigns"
                InputProps={{
                  startAdornment: '$',
                  inputProps: { min: 10, step: 0.01 }
                }}
              />
            </Grid>
            
            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label="Max Lifetime Budget (USD)"
                type="number"
                value={configuration.limits?.maxLifetimeBudget || 1000000}
                onChange={(e) => updateLimits('maxLifetimeBudget', parseFloat(e.target.value))}
                helperText="Maximum lifetime budget for campaigns"
                InputProps={{
                  startAdornment: '$',
                  inputProps: { min: 10, step: 0.01 }
                }}
              />
            </Grid>
            
            <Grid item xs={12}>
              <Typography variant="subtitle1" gutterBottom sx={{ mt: 2 }}>
                Bidding Configuration
              </Typography>
            </Grid>
            
            <Grid item xs={12} md={6}>
              <FormControl fullWidth>
                <InputLabel>Default Bid Type</InputLabel>
                <Select
                  value={configuration.defaultBidType || ''}
                  onChange={(e) => updateConfig('defaultBidType', e.target.value)}
                  label="Default Bid Type"
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
              <FormControl fullWidth>
                <InputLabel>Default Optimization Target</InputLabel>
                <Select
                  value={configuration.defaultOptimizationTarget || ''}
                  onChange={(e) => updateConfig('defaultOptimizationTarget', e.target.value)}
                  label="Default Optimization Target"
                >
                  {optimizationTargets.map((target) => (
                    <MenuItem key={target.value} value={target.value}>
                      {target.label}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>
            
            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label="Min Bid Amount (USD)"
                type="number"
                value={configuration.limits?.minBidAmount || 2}
                onChange={(e) => updateLimits('minBidAmount', parseFloat(e.target.value))}
                helperText="Minimum bid amount"
                InputProps={{
                  startAdornment: '$',
                  inputProps: { min: 2, step: 0.01 }
                }}
              />
            </Grid>
            
            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label="Max Bid Amount (USD)"
                type="number"
                value={configuration.limits?.maxBidAmount || 1000}
                onChange={(e) => updateLimits('maxBidAmount', parseFloat(e.target.value))}
                helperText="Maximum bid amount"
                InputProps={{
                  startAdornment: '$',
                  inputProps: { min: 2, step: 0.01 }
                }}
              />
            </Grid>
          </Grid>
        </TabPanel>

        <TabPanel value={activeTab} index={5}>
          <Grid container spacing={3}>
            <Grid item xs={12}>
              <Typography variant="h6" gutterBottom>
                Analytics & Reporting
              </Typography>
              <Divider sx={{ mb: 2 }} />
            </Grid>
            
            <Grid item xs={12}>
              <Typography variant="subtitle1" gutterBottom>
                Reporting Features
              </Typography>
              <Grid container spacing={2}>
                <Grid item xs={12} md={6}>
                  <FormControlLabel
                    control={
                      <Switch
                        checked={configuration.features?.enableReporting ?? true}
                        onChange={(e) => updateFeatures('enableReporting', e.target.checked)}
                      />
                    }
                    label="Campaign Reporting"
                  />
                  <FormHelperText>Generate performance reports</FormHelperText>
                </Grid>
                
                <Grid item xs={12} md={6}>
                  <FormControlLabel
                    control={
                      <Switch
                        checked={configuration.features?.enableConversionTracking ?? true}
                        onChange={(e) => updateFeatures('enableConversionTracking', e.target.checked)}
                      />
                    }
                    label="Conversion Tracking"
                  />
                  <FormHelperText>Track conversions and ROI</FormHelperText>
                </Grid>
              </Grid>
            </Grid>
            
            <Grid item xs={12}>
              <Typography variant="subtitle1" gutterBottom sx={{ mt: 2 }}>
                Available Metrics
              </Typography>
              <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                {metricTypes.map((metric) => (
                  <Chip
                    key={metric.value}
                    label={metric.label}
                    variant="outlined"
                    color="secondary"
                    icon={<BarChart />}
                  />
                ))}
              </Box>
            </Grid>
            
            <Grid item xs={12} md={6}>
              <FormControl fullWidth>
                <InputLabel>Default Report Granularity</InputLabel>
                <Select
                  value={configuration.defaultTimeGranularity || 'DAILY'}
                  onChange={(e) => updateConfig('defaultTimeGranularity', e.target.value)}
                  label="Default Report Granularity"
                >
                  {timeGranularities.map((gran) => (
                    <MenuItem key={gran.value} value={gran.value}>
                      {gran.label}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>
            
            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label="Report Retention Days"
                type="number"
                value={configuration.reportRetentionDays || 90}
                onChange={(e) => updateConfig('reportRetentionDays', parseInt(e.target.value))}
                helperText="How long to keep performance data"
                InputProps={{
                  inputProps: { min: 30, max: 365 }
                }}
              />
            </Grid>
            
            <Grid item xs={12}>
              <Typography variant="subtitle1" gutterBottom sx={{ mt: 2 }}>
                Conversion Types
              </Typography>
              <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                {conversionTypes.map((type) => (
                  <Chip
                    key={type.value}
                    label={type.label}
                    variant="outlined"
                  />
                ))}
              </Box>
            </Grid>
          </Grid>
        </TabPanel>

        <TabPanel value={activeTab} index={6}>
          <Grid container spacing={3}>
            <Grid item xs={12}>
              <Typography variant="h6" gutterBottom>
                Advanced Settings
              </Typography>
              <Divider sx={{ mb: 2 }} />
            </Grid>
            
            <Grid item xs={12}>
              <Typography variant="subtitle1" gutterBottom>
                Lead Generation
              </Typography>
              <Grid container spacing={2}>
                <Grid item xs={12} md={6}>
                  <FormControlLabel
                    control={
                      <Switch
                        checked={configuration.features?.enableLeadGenForms ?? true}
                        onChange={(e) => updateFeatures('enableLeadGenForms', e.target.checked)}
                      />
                    }
                    label="Lead Generation Forms"
                  />
                  <FormHelperText>Create and manage lead forms</FormHelperText>
                </Grid>
                
                <Grid item xs={12} md={6}>
                  <TextField
                    fullWidth
                    label="Lead Export Email"
                    type="email"
                    value={configuration.leadExportEmail || ''}
                    onChange={(e) => updateConfig('leadExportEmail', e.target.value)}
                    helperText="Email for lead notifications"
                  />
                </Grid>
              </Grid>
            </Grid>
            
            <Grid item xs={12}>
              <Typography variant="subtitle1" gutterBottom sx={{ mt: 2 }}>
                API Rate Limits
              </Typography>
            </Grid>
            
            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label="Max Campaigns per Account"
                type="number"
                value={configuration.limits?.maxCampaignsPerAccount || 1000}
                onChange={(e) => updateLimits('maxCampaignsPerAccount', parseInt(e.target.value))}
                helperText="Maximum campaigns per account"
                InputProps={{
                  inputProps: { min: 1, max: 5000 }
                }}
              />
            </Grid>
            
            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label="Max Ad Groups per Campaign"
                type="number"
                value={configuration.limits?.maxAdGroupsPerCampaign || 100}
                onChange={(e) => updateLimits('maxAdGroupsPerCampaign', parseInt(e.target.value))}
                helperText="Maximum ad groups per campaign"
                InputProps={{
                  inputProps: { min: 1, max: 500 }
                }}
              />
            </Grid>
            
            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label="Max Ads per Ad Group"
                type="number"
                value={configuration.limits?.maxAdsPerAdGroup || 50}
                onChange={(e) => updateLimits('maxAdsPerAdGroup', parseInt(e.target.value))}
                helperText="Maximum ads per ad group"
                InputProps={{
                  inputProps: { min: 1, max: 200 }
                }}
              />
            </Grid>
            
            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label="Max Conversion Events per Day"
                type="number"
                value={configuration.limits?.maxConversionEventsPerDay || 1000000}
                onChange={(e) => updateLimits('maxConversionEventsPerDay', parseInt(e.target.value))}
                helperText="Daily conversion event limit"
              />
            </Grid>
            
            <Grid item xs={12}>
              <Typography variant="subtitle1" gutterBottom sx={{ mt: 2 }}>
                Webhook Configuration
              </Typography>
            </Grid>
            
            <Grid item xs={12} md={8}>
              <TextField
                fullWidth
                label="Webhook URL"
                value={configuration.webhookUrl || ''}
                onChange={(e) => updateConfig('webhookUrl', e.target.value)}
                helperText="URL to receive campaign and performance notifications"
              />
            </Grid>
            
            <Grid item xs={12} md={4}>
              <TextField
                fullWidth
                label="Webhook Secret"
                type="password"
                value={configuration.webhookSecret || ''}
                onChange={(e) => updateConfig('webhookSecret', e.target.value)}
                helperText="Secret for webhook validation"
              />
            </Grid>
            
            <Grid item xs={12}>
              <Button
                variant="contained"
                onClick={validateConfig}
                sx={{ mt: 2 }}
              >
                Validate Configuration
              </Button>
            </Grid>
            
            {Object.keys(errors).length > 0 && (
              <Grid item xs={12}>
                <Alert severity="error">
                  Please fix the following errors:
                  <ul>
                    {Object.entries(errors).map(([key, error]) => (
                      <li key={key}>{error}</li>
                    ))}
                  </ul>
                </Alert>
              </Grid>
            )}
          </Grid>
        </TabPanel>
      </Box>
  );
};