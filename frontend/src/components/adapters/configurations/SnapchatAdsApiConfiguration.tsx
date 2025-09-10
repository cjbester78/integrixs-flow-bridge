import React, { useState, useMemo } from 'react';
import {
  Box,
  TextField,
  FormControl,
  FormControlLabel,
  InputLabel,
  Select,
  MenuItem,
  Checkbox,
  Switch,
  Typography,
  Paper,
  Tabs,
  Tab,
  Grid,
  Chip,
  FormHelperText,
  Accordion,
  AccordionSummary,
  AccordionDetails,
  Button,
  IconButton,
  List,
  ListItem,
  ListItemText,
  ListItemSecondaryAction,
  Autocomplete,
  Alert,
  Divider,
} from '@mui/material';
import {
  ExpandMore,
  Delete,
  Add,
  Info,
  CameraAlt,
  VideoCall,
  People,
  Analytics,
  AttachMoney,
  Palette,
  ViewInAr,
  FilterVintage,
  Celebration,
  AutoAwesome,
} from '@mui/icons-material';

interface SnapchatAdsApiConfiguration {
  clientId: string;
  clientSecret: string;
  adAccountId: string;
  organizationId: string;
  pixelId: string;
  features: {
    enableCampaignManagement: boolean;
    enableAdManagement: boolean;
    enableCreativeManagement: boolean;
    enableAudienceManagement: boolean;
    enablePixelTracking: boolean;
    enableReporting: boolean;
    enableBulkOperations: boolean;
    enableDynamicAds: boolean;
    enableCatalogManagement: boolean;
    enableAppInstallAds: boolean;
    enableWebConversions: boolean;
    enableStoryAds: boolean;
    enableCollectionAds: boolean;
    enableARLenses: boolean;
    enableFilters: boolean;
    enableBrandedMoments: boolean;
    enableCommercializedLenses: boolean;
    enableMeasurement: boolean;
    enableAutoOptimization: boolean;
    enableCreativeLibrary: boolean;
  };
  campaignObjectives: string[];
  adSquadTypes: string[];
  creativeTypes: string[];
  targetingTypes: string[];
  bidStrategies: string[];
  optimizationGoals: string[];
  reportMetrics: string[];
  reportDimensions: string[];
  pixelEventTypes: string[];
  customAudiences: string[];
  catalogIds: string[];
  arLensIds: string[];
  filterIds: string[];
}

interface SnapchatAdsApiConfigurationProps {
  configuration: SnapchatAdsApiConfiguration;
  onChange: (config: SnapchatAdsApiConfiguration) => void;
}

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

const TabPanel: React.FC<TabPanelProps> = ({ children, value, index, ...other }) => {
  return (
    <div
      role="tabpanel"
      hidden={value !== index}
      id={`snapchat-ads-tabpanel-${index}`}
      aria-labelledby={`snapchat-ads-tab-${index}`}
      {...other}
    >
      {value === index && <Box sx={{ p: 3 }}>{children}</Box>}
    </div>
  );
};

export const SnapchatAdsApiConfiguration: React.FC<SnapchatAdsApiConfigurationProps> = ({
  configuration,
  onChange,
}) => {
  const [tabValue, setTabValue] = useState(0);

  const handleTabChange = (event: React.SyntheticEvent, newValue: number) => {
    setTabValue(newValue);
  };

  const updateConfig = (updates: Partial<SnapchatAdsApiConfiguration>) => {
    onChange({ ...configuration, ...updates });
  };

  const updateFeature = (feature: keyof SnapchatAdsApiConfiguration['features'], value: boolean) => {
    updateConfig({
      features: { ...configuration.features, [feature]: value },
    });
  };

  const updateStringArray = (field: keyof SnapchatAdsApiConfiguration, values: string[]) => {
    updateConfig({ [field]: values });
  };

  const campaignObjectiveOptions = [
    'AWARENESS',
    'TRAFFIC',
    'APP_INSTALLS',
    'APP_VISITS',
    'ENGAGEMENT',
    'VIDEO_VIEWS',
    'LEAD_GENERATION',
    'WEBSITE_CONVERSIONS',
    'CATALOG_SALES',
    'STORE_VISITS',
    'BRAND_AWARENESS',
    'LOCAL_AWARENESS',
  ];

  const adSquadTypeOptions = [
    'SNAP_ADS',
    'STORY_ADS',
    'COLLECTION_ADS',
    'DYNAMIC_ADS',
    'COMMERCIALS',
    'AR_EXPERIENCE',
  ];

  const creativeTypeOptions = [
    'SINGLE_IMAGE',
    'SINGLE_VIDEO',
    'TOP_SNAP_ONLY',
    'WEB_VIEW',
    'APP_INSTALL',
    'DEEP_LINK',
    'LONGFORM_VIDEO',
    'COLLECTION',
    'DYNAMIC_COLLECTION',
    'LENS',
    'FILTER',
    'BRANDED_MOMENT',
  ];

  const targetingTypeOptions = [
    'DEMOGRAPHICS',
    'INTERESTS',
    'BEHAVIORS',
    'LOCATION',
    'DEVICE',
    'CARRIER',
    'OS_VERSION',
    'CONNECTION_TYPE',
    'CUSTOM_AUDIENCE',
    'LOOKALIKE_AUDIENCE',
    'ENGAGEMENT_AUDIENCE',
    'PIXEL_AUDIENCE',
    'APP_ACTIVITY',
    'LANGUAGE',
    'ADVANCED_DEMOGRAPHICS',
    'LIFESTYLE',
    'SHOPPERS',
    'CONTENT_KEYWORDS',
  ];

  const bidStrategyOptions = [
    'LOWEST_COST',
    'COST_CAP',
    'BID_CAP',
    'MIN_ROAS',
    'TARGET_COST',
  ];

  const optimizationGoalOptions = [
    'IMPRESSIONS',
    'SWIPES',
    'APP_INSTALLS',
    'APP_OPENS',
    'USES',
    'PURCHASES',
    'SIGN_UPS',
    'PAGE_VIEWS',
    'ADD_TO_CART',
    'PURCHASE_WEB',
    'LEAD',
    'COMPLETE_TUTORIAL',
    'PIXEL_PAGE_VIEW',
    'PIXEL_ADD_CART',
    'PIXEL_PURCHASE',
    'PIXEL_SIGNUP',
    'PIXEL_CUSTOM',
    'STORY_OPENS',
    'STORY_COMPLETES',
  ];

  const reportMetricOptions = [
    'IMPRESSIONS',
    'SWIPE_UPS',
    'SPEND',
    'REACH',
    'FREQUENCY',
    'UNIQUES',
    'TOTAL_REACH',
    'CPM',
    'CPC',
    'CTR',
    'VIDEO_VIEWS',
    'VIEW_TIME',
    'QUARTILE_1',
    'QUARTILE_2',
    'QUARTILE_3',
    'QUARTILE_4',
    'SCREEN_TIME',
    'PLAY_TIME',
    'AVG_SCREEN_TIME',
    'AVG_VIEW_TIME',
    'CONVERSION_PURCHASES',
    'CONVERSION_SAVE',
    'CONVERSION_START_CHECKOUT',
    'CONVERSION_ADD_CART',
    'CONVERSION_VIEW_CONTENT',
    'CONVERSION_SIGN_UPS',
    'CONVERSION_PAGE_VIEWS',
    'SHARES',
    'SAVES',
    'STORY_COMPLETES',
    'STORY_OPENS',
  ];

  const reportDimensionOptions = [
    'COUNTRY',
    'REGION',
    'DMA',
    'GENDER',
    'AGE_BUCKET',
    'INTEREST_CATEGORY',
    'OS',
    'MAKE',
    'CAMPAIGN',
    'AD_SQUAD',
    'AD',
    'CREATIVE',
    'DAY',
    'HOUR',
    'DAY_HOUR',
    'PLACEMENT',
    'PRODUCT_ID',
    'SKU',
  ];

  const pixelEventTypeOptions = [
    'PAGE_VIEW',
    'VIEW_CONTENT',
    'SEARCH',
    'ADD_TO_CART',
    'ADD_TO_WISHLIST',
    'START_CHECKOUT',
    'ADD_BILLING',
    'PURCHASE',
    'SIGN_UP',
    'CUSTOM_EVENT_1',
    'CUSTOM_EVENT_2',
    'CUSTOM_EVENT_3',
    'CUSTOM_EVENT_4',
    'CUSTOM_EVENT_5',
    'AD_CLICK',
    'AD_VIEW',
    'COMPLETE_TUTORIAL',
    'APP_OPEN',
    'APP_INSTALL',
    'LEVEL_COMPLETE',
    'ACHIEVEMENT_UNLOCKED',
  ];

  return (
    <Box>
      <Paper sx={{ width: '100%' }}>
        <Tabs
          value={tabValue}
          onChange={handleTabChange}
          aria-label="snapchat ads configuration tabs"
          variant="scrollable"
          scrollButtons="auto"
        >
          <Tab label="Basic Settings" />
          <Tab label="Features" />
          <Tab label="Campaign Settings" />
          <Tab label="Creative Settings" />
          <Tab label="Audience & Targeting" />
          <Tab label="Pixel & Conversion" />
          <Tab label="Reporting & Analytics" />
          <Tab label="Advanced Features" />
        </Tabs>

        <TabPanel value={tabValue} index={0}>
          <Grid container spacing={3}>
            <Grid item xs={12}>
              <Alert severity="info">
                Configure your Snapchat Ads API credentials and account information.
              </Alert>
            </Grid>

            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label="Client ID"
                value={configuration.clientId || ''}
                onChange={(e) => updateConfig({ clientId: e.target.value })}
                helperText="Your Snapchat Ads API client ID"
                required
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label="Client Secret"
                type="password"
                value={configuration.clientSecret || ''}
                onChange={(e) => updateConfig({ clientSecret: e.target.value })}
                helperText="Your Snapchat Ads API client secret"
                required
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label="Ad Account ID"
                value={configuration.adAccountId || ''}
                onChange={(e) => updateConfig({ adAccountId: e.target.value })}
                helperText="Your Snapchat Ad Account ID"
                required
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label="Organization ID"
                value={configuration.organizationId || ''}
                onChange={(e) => updateConfig({ organizationId: e.target.value })}
                helperText="Your Snapchat Organization ID (optional)"
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label="Pixel ID"
                value={configuration.pixelId || ''}
                onChange={(e) => updateConfig({ pixelId: e.target.value })}
                helperText="Your Snapchat Pixel ID for conversion tracking"
              />
            </Grid>
          </Grid>
        </TabPanel>

        <TabPanel value={tabValue} index={1}>
          <Grid container spacing={3}>
            <Grid item xs={12}>
              <Typography variant="h6" gutterBottom>
                Enable/Disable Features
              </Typography>
              <Typography variant="body2" color="text.secondary" gutterBottom>
                Control which Snapchat Ads features are available in your integration.
              </Typography>
            </Grid>

            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableCampaignManagement || false}
                    onChange={(e) => updateFeature('enableCampaignManagement', e.target.checked)}
                  />
                }
                label="Campaign Management"
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableAdManagement || false}
                    onChange={(e) => updateFeature('enableAdManagement', e.target.checked)}
                  />
                }
                label="Ad Squad Management"
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableCreativeManagement || false}
                    onChange={(e) => updateFeature('enableCreativeManagement', e.target.checked)}
                  />
                }
                label="Creative Management"
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableAudienceManagement || false}
                    onChange={(e) => updateFeature('enableAudienceManagement', e.target.checked)}
                  />
                }
                label="Audience Management"
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enablePixelTracking || false}
                    onChange={(e) => updateFeature('enablePixelTracking', e.target.checked)}
                  />
                }
                label="Pixel Tracking"
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableReporting || false}
                    onChange={(e) => updateFeature('enableReporting', e.target.checked)}
                  />
                }
                label="Reporting & Analytics"
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableBulkOperations || false}
                    onChange={(e) => updateFeature('enableBulkOperations', e.target.checked)}
                  />
                }
                label="Bulk Operations"
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableDynamicAds || false}
                    onChange={(e) => updateFeature('enableDynamicAds', e.target.checked)}
                  />
                }
                label="Dynamic Ads"
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableCatalogManagement || false}
                    onChange={(e) => updateFeature('enableCatalogManagement', e.target.checked)}
                  />
                }
                label="Catalog Management"
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableStoryAds || false}
                    onChange={(e) => updateFeature('enableStoryAds', e.target.checked)}
                  />
                }
                label="Story Ads"
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableCollectionAds || false}
                    onChange={(e) => updateFeature('enableCollectionAds', e.target.checked)}
                  />
                }
                label="Collection Ads"
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableAutoOptimization || false}
                    onChange={(e) => updateFeature('enableAutoOptimization', e.target.checked)}
                  />
                }
                label="Auto-optimization"
              />
            </Grid>
          </Grid>
        </TabPanel>

        <TabPanel value={tabValue} index={2}>
          <Grid container spacing={3}>
            <Grid item xs={12}>
              <Typography variant="h6" gutterBottom>
                Campaign Configuration
              </Typography>
            </Grid>

            <Grid item xs={12}>
              <Autocomplete
                multiple
                options={campaignObjectiveOptions}
                value={configuration.campaignObjectives || []}
                onChange={(_, values) => updateStringArray('campaignObjectives', values)}
                renderTags={(value: string[], getTagProps) =>
                  value.map((option: string, index: number) => (
                    <Chip
                      variant="outlined"
                      label={option}
                      {...getTagProps({ index })}
                    />
                  ))
                }
                renderInput={(params) => (
                  <TextField
                    {...params}
                    label="Campaign Objectives"
                    helperText="Select campaign objectives to support"
                  />
                )}
              />
            </Grid>

            <Grid item xs={12}>
              <Autocomplete
                multiple
                options={adSquadTypeOptions}
                value={configuration.adSquadTypes || []}
                onChange={(_, values) => updateStringArray('adSquadTypes', values)}
                renderTags={(value: string[], getTagProps) =>
                  value.map((option: string, index: number) => (
                    <Chip
                      variant="outlined"
                      label={option}
                      {...getTagProps({ index })}
                    />
                  ))
                }
                renderInput={(params) => (
                  <TextField
                    {...params}
                    label="Ad Squad Types"
                    helperText="Select ad squad types to enable"
                  />
                )}
              />
            </Grid>

            <Grid item xs={12}>
              <Autocomplete
                multiple
                options={bidStrategyOptions}
                value={configuration.bidStrategies || []}
                onChange={(_, values) => updateStringArray('bidStrategies', values)}
                renderTags={(value: string[], getTagProps) =>
                  value.map((option: string, index: number) => (
                    <Chip
                      variant="outlined"
                      label={option}
                      icon={<AttachMoney />}
                      {...getTagProps({ index })}
                    />
                  ))
                }
                renderInput={(params) => (
                  <TextField
                    {...params}
                    label="Bid Strategies"
                    helperText="Available bidding strategies"
                  />
                )}
              />
            </Grid>

            <Grid item xs={12}>
              <Autocomplete
                multiple
                options={optimizationGoalOptions}
                value={configuration.optimizationGoals || []}
                onChange={(_, values) => updateStringArray('optimizationGoals', values)}
                renderTags={(value: string[], getTagProps) =>
                  value.map((option: string, index: number) => (
                    <Chip
                      variant="outlined"
                      label={option}
                      {...getTagProps({ index })}
                    />
                  ))
                }
                renderInput={(params) => (
                  <TextField
                    {...params}
                    label="Optimization Goals"
                    helperText="Select optimization goals to support"
                  />
                )}
              />
            </Grid>
          </Grid>
        </TabPanel>

        <TabPanel value={tabValue} index={3}>
          <Grid container spacing={3}>
            <Grid item xs={12}>
              <Typography variant="h6" gutterBottom>
                Creative Settings
              </Typography>
            </Grid>

            <Grid item xs={12}>
              <Autocomplete
                multiple
                options={creativeTypeOptions}
                value={configuration.creativeTypes || []}
                onChange={(_, values) => updateStringArray('creativeTypes', values)}
                renderTags={(value: string[], getTagProps) =>
                  value.map((option: string, index: number) => {
                    const icon = option.includes('VIDEO') ? <VideoCall /> : 
                                option.includes('IMAGE') ? <CameraAlt /> :
                                option.includes('LENS') ? <ViewInAr /> :
                                option.includes('FILTER') ? <FilterVintage /> :
                                <Palette />;
                    return (
                      <Chip
                        variant="outlined"
                        label={option}
                        icon={icon}
                        {...getTagProps({ index })}
                      />
                    );
                  })
                }
                renderInput={(params) => (
                  <TextField
                    {...params}
                    label="Creative Types"
                    helperText="Select creative types to support"
                  />
                )}
              />
            </Grid>

            <Grid item xs={12}>
              <Typography variant="subtitle1" gutterBottom>
                Advanced Creative Features
              </Typography>
            </Grid>

            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableARLenses || false}
                    onChange={(e) => updateFeature('enableARLenses', e.target.checked)}
                  />
                }
                label="AR Lenses"
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableFilters || false}
                    onChange={(e) => updateFeature('enableFilters', e.target.checked)}
                  />
                }
                label="Filters"
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableBrandedMoments || false}
                    onChange={(e) => updateFeature('enableBrandedMoments', e.target.checked)}
                  />
                }
                label="Branded Moments"
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableCommercializedLenses || false}
                    onChange={(e) => updateFeature('enableCommercializedLenses', e.target.checked)}
                  />
                }
                label="Commercialized Lenses"
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableCreativeLibrary || false}
                    onChange={(e) => updateFeature('enableCreativeLibrary', e.target.checked)}
                  />
                }
                label="Creative Library"
              />
            </Grid>
          </Grid>
        </TabPanel>

        <TabPanel value={tabValue} index={4}>
          <Grid container spacing={3}>
            <Grid item xs={12}>
              <Typography variant="h6" gutterBottom>
                Audience & Targeting
              </Typography>
            </Grid>

            <Grid item xs={12}>
              <Autocomplete
                multiple
                options={targetingTypeOptions}
                value={configuration.targetingTypes || []}
                onChange={(_, values) => updateStringArray('targetingTypes', values)}
                renderTags={(value: string[], getTagProps) =>
                  value.map((option: string, index: number) => (
                    <Chip
                      variant="outlined"
                      label={option}
                      icon={<People />}
                      {...getTagProps({ index })}
                    />
                  ))
                }
                renderInput={(params) => (
                  <TextField
                    {...params}
                    label="Targeting Types"
                    helperText="Available targeting options"
                  />
                )}
              />
            </Grid>

            <Grid item xs={12}>
              <Typography variant="subtitle1" gutterBottom>
                Custom Audiences
              </Typography>
              <TextField
                fullWidth
                multiline
                rows={3}
                placeholder="Enter custom audience IDs, one per line"
                value={(configuration.customAudiences || []).join('\n')}
                onChange={(e) => {
                  const values = e.target.value.split('\n').filter(v => v.trim());
                  updateStringArray('customAudiences', values);
                }}
                helperText="Add custom audience IDs for targeting"
              />
            </Grid>

            <Grid item xs={12}>
              <Alert severity="info">
                <Typography variant="body2">
                  Custom audiences can include customer lists, pixel-based audiences, 
                  engagement audiences, and lookalike audiences.
                </Typography>
              </Alert>
            </Grid>
          </Grid>
        </TabPanel>

        <TabPanel value={tabValue} index={5}>
          <Grid container spacing={3}>
            <Grid item xs={12}>
              <Typography variant="h6" gutterBottom>
                Pixel & Conversion Tracking
              </Typography>
            </Grid>

            <Grid item xs={12}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enablePixelTracking || false}
                    onChange={(e) => updateFeature('enablePixelTracking', e.target.checked)}
                  />
                }
                label="Enable Pixel Tracking"
              />
            </Grid>

            <Grid item xs={12}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableWebConversions || false}
                    onChange={(e) => updateFeature('enableWebConversions', e.target.checked)}
                  />
                }
                label="Web Conversions"
              />
            </Grid>

            <Grid item xs={12}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableAppInstallAds || false}
                    onChange={(e) => updateFeature('enableAppInstallAds', e.target.checked)}
                  />
                }
                label="App Install Tracking"
              />
            </Grid>

            <Grid item xs={12}>
              <Autocomplete
                multiple
                options={pixelEventTypeOptions}
                value={configuration.pixelEventTypes || []}
                onChange={(_, values) => updateStringArray('pixelEventTypes', values)}
                renderTags={(value: string[], getTagProps) =>
                  value.map((option: string, index: number) => (
                    <Chip
                      variant="outlined"
                      label={option}
                      size="small"
                      {...getTagProps({ index })}
                    />
                  ))
                }
                renderInput={(params) => (
                  <TextField
                    {...params}
                    label="Pixel Event Types"
                    helperText="Select pixel events to track"
                  />
                )}
              />
            </Grid>
          </Grid>
        </TabPanel>

        <TabPanel value={tabValue} index={6}>
          <Grid container spacing={3}>
            <Grid item xs={12}>
              <Typography variant="h6" gutterBottom>
                Reporting & Analytics
              </Typography>
            </Grid>

            <Grid item xs={12}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableReporting || false}
                    onChange={(e) => updateFeature('enableReporting', e.target.checked)}
                  />
                }
                label="Enable Reporting"
              />
            </Grid>

            <Grid item xs={12}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableMeasurement || false}
                    onChange={(e) => updateFeature('enableMeasurement', e.target.checked)}
                  />
                }
                label="Advanced Measurement"
              />
            </Grid>

            <Grid item xs={12}>
              <Autocomplete
                multiple
                options={reportMetricOptions}
                value={configuration.reportMetrics || []}
                onChange={(_, values) => updateStringArray('reportMetrics', values)}
                renderTags={(value: string[], getTagProps) =>
                  value.map((option: string, index: number) => (
                    <Chip
                      variant="outlined"
                      label={option}
                      icon={<Analytics />}
                      size="small"
                      {...getTagProps({ index })}
                    />
                  ))
                }
                renderInput={(params) => (
                  <TextField
                    {...params}
                    label="Report Metrics"
                    helperText="Select metrics to include in reports"
                  />
                )}
              />
            </Grid>

            <Grid item xs={12}>
              <Autocomplete
                multiple
                options={reportDimensionOptions}
                value={configuration.reportDimensions || []}
                onChange={(_, values) => updateStringArray('reportDimensions', values)}
                renderTags={(value: string[], getTagProps) =>
                  value.map((option: string, index: number) => (
                    <Chip
                      variant="outlined"
                      label={option}
                      size="small"
                      {...getTagProps({ index })}
                    />
                  ))
                }
                renderInput={(params) => (
                  <TextField
                    {...params}
                    label="Report Dimensions"
                    helperText="Select dimensions for report breakdowns"
                  />
                )}
              />
            </Grid>
          </Grid>
        </TabPanel>

        <TabPanel value={tabValue} index={7}>
          <Grid container spacing={3}>
            <Grid item xs={12}>
              <Typography variant="h6" gutterBottom>
                Advanced Features
              </Typography>
            </Grid>

            <Grid item xs={12}>
              <Accordion>
                <AccordionSummary expandIcon={<ExpandMore />}>
                  <Typography>
                    <ViewInAr sx={{ mr: 1, verticalAlign: 'middle' }} />
                    AR & Interactive Features
                  </Typography>
                </AccordionSummary>
                <AccordionDetails>
                  <Grid container spacing={2}>
                    <Grid item xs={12}>
                      <TextField
                        fullWidth
                        multiline
                        rows={3}
                        placeholder="Enter AR Lens IDs, one per line"
                        value={(configuration.arLensIds || []).join('\n')}
                        onChange={(e) => {
                          const values = e.target.value.split('\n').filter(v => v.trim());
                          updateStringArray('arLensIds', values);
                        }}
                        helperText="Add AR Lens IDs for campaigns"
                      />
                    </Grid>
                    <Grid item xs={12}>
                      <TextField
                        fullWidth
                        multiline
                        rows={3}
                        placeholder="Enter Filter IDs, one per line"
                        value={(configuration.filterIds || []).join('\n')}
                        onChange={(e) => {
                          const values = e.target.value.split('\n').filter(v => v.trim());
                          updateStringArray('filterIds', values);
                        }}
                        helperText="Add Filter IDs for campaigns"
                      />
                    </Grid>
                  </Grid>
                </AccordionDetails>
              </Accordion>
            </Grid>

            <Grid item xs={12}>
              <Accordion>
                <AccordionSummary expandIcon={<ExpandMore />}>
                  <Typography>
                    <ShoppingCart sx={{ mr: 1, verticalAlign: 'middle' }} />
                    Catalog & Dynamic Ads
                  </Typography>
                </AccordionSummary>
                <AccordionDetails>
                  <Grid container spacing={2}>
                    <Grid item xs={12}>
                      <FormControlLabel
                        control={
                          <Switch
                            checked={configuration.features?.enableCatalogManagement || false}
                            onChange={(e) => updateFeature('enableCatalogManagement', e.target.checked)}
                          />
                        }
                        label="Enable Catalog Management"
                      />
                    </Grid>
                    <Grid item xs={12}>
                      <TextField
                        fullWidth
                        multiline
                        rows={3}
                        placeholder="Enter Catalog IDs, one per line"
                        value={(configuration.catalogIds || []).join('\n')}
                        onChange={(e) => {
                          const values = e.target.value.split('\n').filter(v => v.trim());
                          updateStringArray('catalogIds', values);
                        }}
                        helperText="Add catalog IDs for product feeds"
                      />
                    </Grid>
                  </Grid>
                </AccordionDetails>
              </Accordion>
            </Grid>

            <Grid item xs={12}>
              <Alert severity="info">
                <Typography variant="body2">
                  Advanced features like AR Lenses, Filters, and Branded Moments require 
                  additional approval from Snapchat and may have specific creative requirements.
                </Typography>
              </Alert>
            </Grid>
          </Grid>
        </TabPanel>
      </Paper>
    </Box>
  );
};