import React, { useState } from 'react';
import {
  Box,
  TextField,
  FormControl,
  FormLabel,
  FormControlLabel,
  Switch,
  Button,
  Grid,
  Typography,
  Paper,
  Divider,
  IconButton,
  Tooltip,
  Select,
  MenuItem,
  InputLabel,
  Chip,
  Slider,
  Alert,
  Tabs,
  Tab,
  Accordion,
  AccordionSummary,
  AccordionDetails,
  FormHelperText,
  InputAdornment,
  OutlinedInput,
  FormGroup,
  Link,
} from '@mui/material';
import {
  Info as InfoIcon,
  ExpandMore as ExpandMoreIcon,
  Business as BusinessIcon,
  Campaign as CampaignIcon,
  Groups as GroupsIcon,
  VideoLibrary as VideoLibraryIcon,
  Analytics as AnalyticsIcon,
  Code as CodeIcon,
  ShoppingCart as ShoppingCartIcon,
} from '@mui/icons-material';

interface TikTokBusinessApiConfigurationProps {
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
    <div
      role="tabpanel"
      hidden={value !== index}
      id={`tiktok-business-tabpanel-${index}`}
      aria-labelledby={`tiktok-business-tab-${index}`}
      {...other}
    >
      {value === index && (
        <Box sx={{ p: 3 }}>
          {children}
        </Box>
      )}
    </div>
  );
}

export const TikTokBusinessApiConfiguration: React.FC<TikTokBusinessApiConfigurationProps> = ({
  configuration,
  onChange,
}) => {
  const [tabValue, setTabValue] = useState(0);

  const handleTabChange = (event: React.SyntheticEvent, newValue: number) => {
    setTabValue(newValue);
  };

  const handleConfigChange = (field: string, value: any) => {
    onChange({
      ...configuration,
      [field]: value,
    });
  };

  const handleFeatureToggle = (feature: string) => {
    onChange({
      ...configuration,
      features: {
        ...configuration.features,
        [feature]: !configuration.features?.[feature],
      },
    });
  };

  const handleLimitChange = (limit: string, value: any) => {
    onChange({
      ...configuration,
      limits: {
        ...configuration.limits,
        [limit]: value,
      },
    });
  };

  // Campaign objectives
  const campaignObjectives = [
    { value: 'REACH', label: 'Reach' },
    { value: 'TRAFFIC', label: 'Traffic' },
    { value: 'APP_INSTALL', label: 'App Install' },
    { value: 'VIDEO_VIEWS', label: 'Video Views' },
    { value: 'LEAD_GENERATION', label: 'Lead Generation' },
    { value: 'CONVERSIONS', label: 'Conversions' },
    { value: 'CATALOG_SALES', label: 'Catalog Sales' },
  ];

  // Ad formats
  const adFormats = [
    { value: 'SINGLE_VIDEO', label: 'Single Video' },
    { value: 'SINGLE_IMAGE', label: 'Single Image' },
    { value: 'CAROUSEL', label: 'Carousel' },
    { value: 'COLLECTION', label: 'Collection' },
    { value: 'SPARK_AD', label: 'Spark Ad' },
    { value: 'PLAYABLE_AD', label: 'Playable Ad' },
  ];

  return (
    <Box sx={{ width: '100%' }}>
      <Paper sx={{ p: 3, mb: 3 }}>
        <Typography variant="h6" gutterBottom>
          TikTok Business API Configuration
        </Typography>
        <Typography variant="body2" color="text.secondary">
          Configure your TikTok Business API integration for advertising campaigns and analytics.
        </Typography>
        <Link href="https://ads.tiktok.com/marketing_api/docs" target="_blank" rel="noopener" sx={{ mt: 1, display: 'block' }}>
          View TikTok Marketing API Documentation
        </Link>
      </Paper>

      <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
        <Tabs value={tabValue} onChange={handleTabChange} aria-label="TikTok Business configuration tabs" scrollButtons="auto" allowScrollButtonsMobile>
          <Tab icon={<BusinessIcon />} label="Basic Settings" />
          <Tab icon={<CampaignIcon />} label="Campaign Management" />
          <Tab icon={<VideoLibraryIcon />} label="Creative & Assets" />
          <Tab icon={<GroupsIcon />} label="Audience Targeting" />
          <Tab icon={<AnalyticsIcon />} label="Analytics & Reports" />
          <Tab icon={<CodeIcon />} label="Pixel & Conversion" />
          <Tab icon={<ShoppingCartIcon />} label="E-commerce" />
        </Tabs>
      </Box>

      <TabPanel value={tabValue} index={0}>
        {/* Basic Settings Tab */}
        <Grid container spacing={3}>
          <Grid item xs={12}>
            <Alert severity="info">
              To use the TikTok Business API, you need to create a TikTok for Business account and register as a developer.
            </Alert>
          </Grid>
          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              label="App ID"
              value={configuration.appId || ''}
              onChange={(e) => handleConfigChange('appId', e.target.value)}
              helperText="Your TikTok app ID from the developer portal"
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              label="App Secret"
              type="password"
              value={configuration.appSecret || ''}
              onChange={(e) => handleConfigChange('appSecret', e.target.value)}
              helperText="Your TikTok app secret"
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              label="Advertiser ID"
              value={configuration.advertiserId || ''}
              onChange={(e) => handleConfigChange('advertiserId', e.target.value)}
              helperText="Your TikTok advertiser account ID"
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              label="Business ID"
              value={configuration.businessId || ''}
              onChange={(e) => handleConfigChange('businessId', e.target.value)}
              helperText="Business Center ID (for catalog management)"
            />
          </Grid>
          <Grid item xs={12}>
            <Divider />
          </Grid>
          <Grid item xs={12}>
            <Typography variant="subtitle1" gutterBottom>
              Account Limits
            </Typography>
          </Grid>
          <Grid item xs={12} md={4}>
            <TextField
              fullWidth
              type="number"
              label="Max Campaigns"
              value={configuration.limits?.maxCampaignsPerAccount || 500}
              onChange={(e) => handleLimitChange('maxCampaignsPerAccount', parseInt(e.target.value))}
              inputProps={{ min: 1, max: 1000 }}
            />
          </Grid>
          <Grid item xs={12} md={4}>
            <TextField
              fullWidth
              type="number"
              label="Max Ad Groups per Campaign"
              value={configuration.limits?.maxAdGroupsPerCampaign || 1000}
              onChange={(e) => handleLimitChange('maxAdGroupsPerCampaign', parseInt(e.target.value))}
              inputProps={{ min: 1, max: 2000 }}
            />
          </Grid>
          <Grid item xs={12} md={4}>
            <TextField
              fullWidth
              type="number"
              label="Max Ads per Ad Group"
              value={configuration.limits?.maxAdsPerAdGroup || 20}
              onChange={(e) => handleLimitChange('maxAdsPerAdGroup', parseInt(e.target.value))}
              inputProps={{ min: 1, max: 50 }}
            />
          </Grid>
        </Grid>
      </TabPanel>

      <TabPanel value={tabValue} index={1}>
        {/* Campaign Management Tab */}
        <Grid container spacing={3}>
          <Grid item xs={12}>
            <Typography variant="subtitle1" gutterBottom>
              Campaign Features
            </Typography>
          </Grid>
          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableCampaignManagement ?? true}
                  onChange={() => handleFeatureToggle('enableCampaignManagement')}
                />
              }
              label="Campaign Management"
            />
            <Typography variant="caption" display="block" sx={{ ml: 4 }}>
              Create, update, and manage advertising campaigns
            </Typography>
          </Grid>
          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableAdGroupManagement ?? true}
                  onChange={() => handleFeatureToggle('enableAdGroupManagement')}
                />
              }
              label="Ad Group Management"
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableAdManagement ?? true}
                  onChange={() => handleFeatureToggle('enableAdManagement')}
                />
              }
              label="Ad Management"
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableBudgetManagement ?? true}
                  onChange={() => handleFeatureToggle('enableBudgetManagement')}
                />
              }
              label="Budget Management"
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableBidStrategy ?? true}
                  onChange={() => handleFeatureToggle('enableBidStrategy')}
                />
              }
              label="Bid Strategy Optimization"
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableAutomatedRules ?? true}
                  onChange={() => handleFeatureToggle('enableAutomatedRules')}
                />
              }
              label="Automated Rules"
            />
          </Grid>
          <Grid item xs={12}>
            <Divider sx={{ my: 2 }} />
          </Grid>
          <Grid item xs={12}>
            <Typography variant="subtitle1" gutterBottom>
              Default Campaign Settings
            </Typography>
          </Grid>
          <Grid item xs={12} md={6}>
            <FormControl fullWidth>
              <InputLabel>Default Campaign Objective</InputLabel>
              <Select
                value={configuration.defaultObjective || ''}
                onChange={(e) => handleConfigChange('defaultObjective', e.target.value)}
                label="Default Campaign Objective"
              >
                {campaignObjectives.map((obj) => (
                  <MenuItem key={obj.value} value={obj.value}>
                    {obj.label}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          </Grid>
          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              type="number"
              label="Minimum Budget (USD)"
              value={configuration.limits?.minBudgetUSD || 20}
              onChange={(e) => handleLimitChange('minBudgetUSD', parseInt(e.target.value))}
              InputProps={{
                startAdornment: <InputAdornment position="start">$</InputAdornment>,
              }}
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              type="number"
              label="Max Bulk Operations"
              value={configuration.limits?.maxBulkOperations || 100}
              onChange={(e) => handleLimitChange('maxBulkOperations', parseInt(e.target.value))}
              helperText="Maximum items in bulk update operations"
            />
          </Grid>
        </Grid>
      </TabPanel>

      <TabPanel value={tabValue} index={2}>
        {/* Creative & Assets Tab */}
        <Grid container spacing={3}>
          <Grid item xs={12}>
            <Typography variant="subtitle1" gutterBottom>
              Creative Management
            </Typography>
          </Grid>
          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableCreativeManagement ?? true}
                  onChange={() => handleFeatureToggle('enableCreativeManagement')}
                />
              }
              label="Creative Management"
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableVideoUpload ?? true}
                  onChange={() => handleFeatureToggle('enableVideoUpload')}
                />
              }
              label="Video Upload"
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableImageUpload ?? true}
                  onChange={() => handleFeatureToggle('enableImageUpload')}
                />
              }
              label="Image Upload"
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableCreativeOptimization ?? true}
                  onChange={() => handleFeatureToggle('enableCreativeOptimization')}
                />
              }
              label="Creative Optimization"
            />
          </Grid>
          <Grid item xs={12}>
            <Divider sx={{ my: 2 }} />
          </Grid>
          <Grid item xs={12}>
            <Typography variant="subtitle1" gutterBottom>
              Asset Limits
            </Typography>
          </Grid>
          <Grid item xs={12} md={4}>
            <TextField
              fullWidth
              type="number"
              label="Max Video Size (MB)"
              value={configuration.limits?.maxVideoSizeMB || 500}
              onChange={(e) => handleLimitChange('maxVideoSizeMB', parseInt(e.target.value))}
              inputProps={{ min: 10, max: 1000 }}
            />
          </Grid>
          <Grid item xs={12} md={4}>
            <TextField
              fullWidth
              type="number"
              label="Max Video Duration (seconds)"
              value={configuration.limits?.maxVideoDurationSeconds || 60}
              onChange={(e) => handleLimitChange('maxVideoDurationSeconds', parseInt(e.target.value))}
              inputProps={{ min: 5, max: 180 }}
            />
          </Grid>
          <Grid item xs={12} md={4}>
            <TextField
              fullWidth
              type="number"
              label="Max Image Size (MB)"
              value={configuration.limits?.maxImageSizeMB || 30}
              onChange={(e) => handleLimitChange('maxImageSizeMB', parseInt(e.target.value))}
              inputProps={{ min: 1, max: 50 }}
            />
          </Grid>
          <Grid item xs={12}>
            <Typography variant="subtitle2" gutterBottom>
              Supported Ad Formats
            </Typography>
            <FormGroup row>
              {adFormats.map((format) => (
                <FormControlLabel
                  key={format.value}
                  control={<Switch defaultChecked />}
                  label={format.label}
                />
              ))}
            </FormGroup>
          </Grid>
          <Grid item xs={12}>
            <Divider sx={{ my: 2 }} />
          </Grid>
          <Grid item xs={12}>
            <Typography variant="subtitle1" gutterBottom>
              Special Ad Types
            </Typography>
          </Grid>
          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableSparkAds ?? true}
                  onChange={() => handleFeatureToggle('enableSparkAds')}
                />
              }
              label="Spark Ads"
            />
            <Typography variant="caption" display="block" sx={{ ml: 4 }}>
              Promote organic TikTok posts as ads
            </Typography>
          </Grid>
          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableBrandedContent ?? true}
                  onChange={() => handleFeatureToggle('enableBrandedContent')}
                />
              }
              label="Branded Content"
            />
            <Typography variant="caption" display="block" sx={{ ml: 4 }}>
              Collaborate with creators for branded content
            </Typography>
          </Grid>
        </Grid>
      </TabPanel>

      <TabPanel value={tabValue} index={3}>
        {/* Audience Targeting Tab */}
        <Grid container spacing={3}>
          <Grid item xs={12}>
            <Typography variant="subtitle1" gutterBottom>
              Audience Features
            </Typography>
          </Grid>
          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableAudienceTargeting ?? true}
                  onChange={() => handleFeatureToggle('enableAudienceTargeting')}
                />
              }
              label="Audience Targeting"
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableCustomAudiences ?? true}
                  onChange={() => handleFeatureToggle('enableCustomAudiences')}
                />
              }
              label="Custom Audiences"
            />
            <Typography variant="caption" display="block" sx={{ ml: 4 }}>
              Upload customer lists and create custom audiences
            </Typography>
          </Grid>
          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              type="number"
              label="Max Custom Audiences"
              value={configuration.limits?.maxCustomAudiences || 500}
              onChange={(e) => handleLimitChange('maxCustomAudiences', parseInt(e.target.value))}
              inputProps={{ min: 10, max: 1000 }}
            />
          </Grid>
          <Grid item xs={12}>
            <Divider sx={{ my: 2 }} />
          </Grid>
          <Grid item xs={12}>
            <Typography variant="subtitle1" gutterBottom>
              Targeting Options
            </Typography>
          </Grid>
          <Grid item xs={12}>
            <Typography variant="subtitle2" gutterBottom>
              Demographics
            </Typography>
            <FormGroup row>
              <FormControlLabel control={<Switch defaultChecked />} label="Age" />
              <FormControlLabel control={<Switch defaultChecked />} label="Gender" />
              <FormControlLabel control={<Switch defaultChecked />} label="Location" />
              <FormControlLabel control={<Switch defaultChecked />} label="Language" />
            </FormGroup>
          </Grid>
          <Grid item xs={12}>
            <Typography variant="subtitle2" gutterBottom>
              Interests & Behaviors
            </Typography>
            <FormGroup row>
              <FormControlLabel control={<Switch defaultChecked />} label="Interest Categories" />
              <FormControlLabel control={<Switch defaultChecked />} label="Behavior Categories" />
              <FormControlLabel control={<Switch defaultChecked />} label="Video Interactions" />
              <FormControlLabel control={<Switch defaultChecked />} label="Creator Interactions" />
              <FormControlLabel control={<Switch defaultChecked />} label="Hashtag Interactions" />
            </FormGroup>
          </Grid>
          <Grid item xs={12}>
            <Typography variant="subtitle2" gutterBottom>
              Device & Technology
            </Typography>
            <FormGroup row>
              <FormControlLabel control={<Switch defaultChecked />} label="Device Type" />
              <FormControlLabel control={<Switch defaultChecked />} label="Operating System" />
              <FormControlLabel control={<Switch defaultChecked />} label="Connection Type" />
              <FormControlLabel control={<Switch defaultChecked />} label="Carrier" />
              <FormControlLabel control={<Switch defaultChecked />} label="Device Price" />
            </FormGroup>
          </Grid>
        </Grid>
      </TabPanel>

      <TabPanel value={tabValue} index={4}>
        {/* Analytics & Reports Tab */}
        <Grid container spacing={3}>
          <Grid item xs={12}>
            <Typography variant="subtitle1" gutterBottom>
              Reporting Features
            </Typography>
          </Grid>
          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableReporting ?? true}
                  onChange={() => handleFeatureToggle('enableReporting')}
                />
              }
              label="Campaign Reporting"
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableAnalyticsExport ?? true}
                  onChange={() => handleFeatureToggle('enableAnalyticsExport')}
                />
              }
              label="Analytics Export"
            />
          </Grid>
          <Grid item xs={12}>
            <TextField
              fullWidth
              type="number"
              label="Reporting Delay (hours)"
              value={configuration.limits?.reportingDelayHours || 3}
              onChange={(e) => handleLimitChange('reportingDelayHours', parseInt(e.target.value))}
              helperText="Data processing delay for reports"
              inputProps={{ min: 1, max: 24 }}
            />
          </Grid>
          <Grid item xs={12}>
            <Divider sx={{ my: 2 }} />
          </Grid>
          <Grid item xs={12}>
            <Typography variant="subtitle1" gutterBottom>
              Report Types
            </Typography>
            <FormGroup row>
              <FormControlLabel control={<Switch defaultChecked />} label="Basic Reports" />
              <FormControlLabel control={<Switch defaultChecked />} label="Audience Reports" />
              <FormControlLabel control={<Switch defaultChecked />} label="Video Performance" />
              <FormControlLabel control={<Switch defaultChecked />} label="Attribution Reports" />
              <FormControlLabel control={<Switch defaultChecked />} label="Conversion Reports" />
              <FormControlLabel control={<Switch defaultChecked />} label="Custom Reports" />
            </FormGroup>
          </Grid>
          <Grid item xs={12}>
            <Typography variant="subtitle1" gutterBottom>
              Metrics to Track
            </Typography>
            <FormGroup row>
              <FormControlLabel control={<Switch defaultChecked />} label="Impressions" />
              <FormControlLabel control={<Switch defaultChecked />} label="Clicks" />
              <FormControlLabel control={<Switch defaultChecked />} label="Video Views" />
              <FormControlLabel control={<Switch defaultChecked />} label="Conversions" />
              <FormControlLabel control={<Switch defaultChecked />} label="Spend" />
              <FormControlLabel control={<Switch defaultChecked />} label="ROAS" />
              <FormControlLabel control={<Switch defaultChecked />} label="Engagement" />
            </FormGroup>
          </Grid>
        </Grid>
      </TabPanel>

      <TabPanel value={tabValue} index={5}>
        {/* Pixel & Conversion Tab */}
        <Grid container spacing={3}>
          <Grid item xs={12}>
            <Alert severity="info">
              TikTok Pixel helps track user actions on your website and measure ad performance.
            </Alert>
          </Grid>
          <Grid item xs={12}>
            <Typography variant="subtitle1" gutterBottom>
              Pixel Features
            </Typography>
          </Grid>
          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enablePixelTracking ?? true}
                  onChange={() => handleFeatureToggle('enablePixelTracking')}
                />
              }
              label="Pixel Tracking"
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableConversionTracking ?? true}
                  onChange={() => handleFeatureToggle('enableConversionTracking')}
                />
              }
              label="Conversion Tracking"
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              type="number"
              label="Max Pixels per Account"
              value={configuration.limits?.maxPixelsPerAccount || 20}
              onChange={(e) => handleLimitChange('maxPixelsPerAccount', parseInt(e.target.value))}
              inputProps={{ min: 1, max: 50 }}
            />
          </Grid>
          <Grid item xs={12}>
            <Divider sx={{ my: 2 }} />
          </Grid>
          <Grid item xs={12}>
            <Typography variant="subtitle1" gutterBottom>
              Conversion Events
            </Typography>
            <FormGroup row>
              <FormControlLabel control={<Switch defaultChecked />} label="Page View" />
              <FormControlLabel control={<Switch defaultChecked />} label="Add to Cart" />
              <FormControlLabel control={<Switch defaultChecked />} label="Purchase" />
              <FormControlLabel control={<Switch defaultChecked />} label="Registration" />
              <FormControlLabel control={<Switch defaultChecked />} label="Lead Generation" />
              <FormControlLabel control={<Switch defaultChecked />} label="App Install" />
              <FormControlLabel control={<Switch defaultChecked />} label="Custom Events" />
            </FormGroup>
          </Grid>
        </Grid>
      </TabPanel>

      <TabPanel value={tabValue} index={6}>
        {/* E-commerce Tab */}
        <Grid container spacing={3}>
          <Grid item xs={12}>
            <Typography variant="subtitle1" gutterBottom>
              E-commerce Features
            </Typography>
          </Grid>
          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableCatalogManagement ?? true}
                  onChange={() => handleFeatureToggle('enableCatalogManagement')}
                />
              }
              label="Catalog Management"
            />
            <Typography variant="caption" display="block" sx={{ ml: 4 }}>
              Manage product catalogs for dynamic ads
            </Typography>
          </Grid>
          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableDynamicAds ?? true}
                  onChange={() => handleFeatureToggle('enableDynamicAds')}
                />
              }
              label="Dynamic Ads"
            />
            <Typography variant="caption" display="block" sx={{ ml: 4 }}>
              Automatically create ads from product catalogs
            </Typography>
          </Grid>
          <Grid item xs={12}>
            <Typography variant="subtitle2" gutterBottom>
              Catalog Features
            </Typography>
            <FormGroup row>
              <FormControlLabel control={<Switch defaultChecked />} label="Product Feed Import" />
              <FormControlLabel control={<Switch defaultChecked />} label="Real-time Inventory" />
              <FormControlLabel control={<Switch defaultChecked />} label="Price Updates" />
              <FormControlLabel control={<Switch defaultChecked />} label="Product Sets" />
              <FormControlLabel control={<Switch defaultChecked />} label="Collection Ads" />
            </FormGroup>
          </Grid>
        </Grid>
      </TabPanel>
    </Box>
  );
};

export default TikTokBusinessApiConfiguration;