import React, { useState } from 'react';
import {
  TextField,
  FormControlLabel,
  Switch,
  Grid,
  Typography,
  Paper,
  Box,
  Tabs,
  Tab,
  Chip,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Slider,
  Alert,
  Divider,
  Button,
  IconButton,
  List,
  ListItem,
  ListItemText,
  ListItemSecondaryAction,
} from '@mui/material';
import { SelectChangeEvent } from '@mui/material/Select';
import AddIcon from '@mui/icons-material/Add';
import DeleteIcon from '@mui/icons-material/Delete';
import InfoIcon from '@mui/icons-material/Info';

interface PinterestApiConfigurationProps {
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
      id={`pinterest-tabpanel-${index}`}
      aria-labelledby={`pinterest-tab-${index}`}
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

export const PinterestApiConfiguration: React.FC<PinterestApiConfigurationProps> = ({
  configuration,
  onChange,
}) => {
  const [tabValue, setTabValue] = useState(0);
  const [newScope, setNewScope] = useState('');

  const handleChange = (field: string) => (event: React.ChangeEvent<HTMLInputElement>) => {
    onChange({
      ...configuration,
      [field]: event.target.value,
    });
  };

  const handleSwitchChange = (section: string, field: string) => (event: React.ChangeEvent<HTMLInputElement>) => {
    onChange({
      ...configuration,
      [section]: {
        ...configuration[section],
        [field]: event.target.checked,
      },
    });
  };

  const handleSelectChange = (field: string) => (event: SelectChangeEvent) => {
    onChange({
      ...configuration,
      [field]: event.target.value,
    });
  };

  const handleNumberChange = (section: string, field: string) => (event: React.ChangeEvent<HTMLInputElement>) => {
    onChange({
      ...configuration,
      [section]: {
        ...configuration[section],
        [field]: parseInt(event.target.value),
      },
    });
  };

  const handleSliderChange = (section: string, field: string) => (_event: Event, value: number | number[]) => {
    onChange({
      ...configuration,
      [section]: {
        ...configuration[section],
        [field]: value as number,
      },
    });
  };

  const handleTabChange = (_event: React.SyntheticEvent, newValue: number) => {
    setTabValue(newValue);
  };

  const handleAddScope = () => {
    if (newScope && !configuration.scopes?.includes(newScope)) {
      onChange({
        ...configuration,
        scopes: [...(configuration.scopes || []), newScope],
      });
      setNewScope('');
    }
  };

  const handleRemoveScope = (scope: string) => {
    onChange({
      ...configuration,
      scopes: configuration.scopes?.filter((s: string) => s !== scope) || [],
    });
  };

  const defaultScopes = [
    'boards:read',
    'boards:write',
    'pins:read',
    'pins:write',
    'user_accounts:read',
    'catalogs:read',
    'catalogs:write',
    'ads:read',
    'ads:write'
  ];

  return (
    <>
      <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
        <Tabs value={tabValue} onChange={handleTabChange} aria-label="Pinterest API configuration tabs">
          <Tab label="Authentication" />
          <Tab label="Features" />
          <Tab label="Content Settings" />
          <Tab label="Shopping & Catalogs" />
          <Tab label="Analytics" />
          <Tab label="Advertising" />
          <Tab label="Rate Limits" />
        </Tabs>
      </Box>

      <TabPanel value={tabValue} index={0}>
        <Grid container spacing={3}>
          <Grid item xs={12}>
            <Alert severity="info">
              Configure your Pinterest API credentials. You'll need an App ID and Secret from the Pinterest Developers portal.
            </Alert>
          </Grid>

          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              label="App ID"
              value={configuration.appId || ''}
              onChange={handleChange('appId')}
              helperText="Your Pinterest App ID"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              label="App Secret"
              type="password"
              value={configuration.appSecret || ''}
              onChange={handleChange('appSecret')}
              helperText="Your Pinterest App Secret"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              label="Advertiser ID"
              value={configuration.advertiserId || ''}
              onChange={handleChange('advertiserId')}
              helperText="Required for advertising features"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              label="Access Token"
              type="password"
              value={configuration.accessToken || ''}
              onChange={handleChange('accessToken')}
              helperText="OAuth 2.0 access token"
            />
          </Grid>

          <Grid item xs={12}>
            <Typography variant="h6" gutterBottom>OAuth Scopes</Typography>
            <Typography variant="body2" color="text.secondary" gutterBottom>
              Select the permissions your application needs
            </Typography>
          </Grid>

          <Grid item xs={12}>
            <Paper variant="outlined" sx={{ p: 2 }}>
              <Grid container spacing={2}>
                <Grid item xs={12}>
                  <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                    {defaultScopes.map((scope) => (
                      <Chip
                        key={scope}
                        label={scope}
                        color={configuration.scopes?.includes(scope) ? 'primary' : 'default'}
                        onClick={() => {
                          if (configuration.scopes?.includes(scope)) {
                            handleRemoveScope(scope);
                          } else {
                            onChange({
                              ...configuration,
                              scopes: [...(configuration.scopes || []), scope],
                            });
                          }
                        }}
                      />
                    ))}
                  </Box>
                </Grid>
                <Grid item xs={8}>
                  <TextField
                    fullWidth
                    label="Custom Scope"
                    value={newScope}
                    onChange={(e) => setNewScope(e.target.value)}
                    placeholder="Enter custom scope"
                    onKeyPress={(e) => e.key === 'Enter' && handleAddScope()}
                  />
                </Grid>
                <Grid item xs={4}>
                  <Button
                    fullWidth
                    variant="outlined"
                    onClick={handleAddScope}
                    disabled={!newScope}
                  >
                    Add Scope
                  </Button>
                </Grid>
              </Grid>
            </Paper>
          </Grid>

          <Grid item xs={12}>
            <TextField
              fullWidth
              label="Webhook URL"
              value={configuration.webhookUrl || ''}
              onChange={handleChange('webhookUrl')}
              helperText="Endpoint to receive Pinterest webhook events"
            />
          </Grid>
        </Grid>
      </TabPanel>

      <TabPanel value={tabValue} index={1}>
        <Grid container spacing={3}>
          <Grid item xs={12}>
            <Typography variant="h6" gutterBottom>Core Features</Typography>
          </Grid>

          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enablePinManagement || false}
                  onChange={handleSwitchChange('features', 'enablePinManagement')}
                />
              }
              label="Enable Pin Management"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableBoardManagement || false}
                  onChange={handleSwitchChange('features', 'enableBoardManagement')}
                />
              }
              label="Enable Board Management"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableAnalytics || false}
                  onChange={handleSwitchChange('features', 'enableAnalytics')}
                />
              }
              label="Enable Analytics"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableShopping || false}
                  onChange={handleSwitchChange('features', 'enableShopping')}
                />
              }
              label="Enable Shopping"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableAds || false}
                  onChange={handleSwitchChange('features', 'enableAds')}
                />
              }
              label="Enable Advertising"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableCatalogs || false}
                  onChange={handleSwitchChange('features', 'enableCatalogs')}
                />
              }
              label="Enable Product Catalogs"
            />
          </Grid>

          <Grid item xs={12}>
            <Divider sx={{ my: 2 }} />
            <Typography variant="h6" gutterBottom>Content Features</Typography>
          </Grid>

          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableBulkOperations || false}
                  onChange={handleSwitchChange('features', 'enableBulkOperations')}
                />
              }
              label="Enable Bulk Operations"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableVideoContent || false}
                  onChange={handleSwitchChange('features', 'enableVideoContent')}
                />
              }
              label="Enable Video Content"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableStoryPins || false}
                  onChange={handleSwitchChange('features', 'enableStoryPins')}
                />
              }
              label="Enable Story Pins"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableIdeaPins || false}
                  onChange={handleSwitchChange('features', 'enableIdeaPins')}
                />
              }
              label="Enable Idea Pins"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableTryOn || false}
                  onChange={handleSwitchChange('features', 'enableTryOn')}
                />
              }
              label="Enable Try On Feature"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableCollections || false}
                  onChange={handleSwitchChange('features', 'enableCollections')}
                />
              }
              label="Enable Collections"
            />
          </Grid>

          <Grid item xs={12}>
            <Divider sx={{ my: 2 }} />
            <Typography variant="h6" gutterBottom>Business Features</Typography>
          </Grid>

          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableMerchant || false}
                  onChange={handleSwitchChange('features', 'enableMerchant')}
                />
              }
              label="Enable Merchant Features"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableCreatorTools || false}
                  onChange={handleSwitchChange('features', 'enableCreatorTools')}
                />
              }
              label="Enable Creator Tools"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableAudiences || false}
                  onChange={handleSwitchChange('features', 'enableAudiences')}
                />
              }
              label="Enable Custom Audiences"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableConversionTracking || false}
                  onChange={handleSwitchChange('features', 'enableConversionTracking')}
                />
              }
              label="Enable Conversion Tracking"
            />
          </Grid>
        </Grid>
      </TabPanel>

      <TabPanel value={tabValue} index={2}>
        <Grid container spacing={3}>
          <Grid item xs={12}>
            <Typography variant="h6" gutterBottom>Content Limits</Typography>
          </Grid>

          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              type="number"
              label="Max Pins per Board"
              value={configuration.limits?.maxPinsPerBoard || 200000}
              onChange={handleNumberChange('limits', 'maxPinsPerBoard')}
              helperText="Maximum number of pins allowed per board"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              type="number"
              label="Max Boards per User"
              value={configuration.limits?.maxBoardsPerUser || 2000}
              onChange={handleNumberChange('limits', 'maxBoardsPerUser')}
              helperText="Maximum number of boards per user account"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              type="number"
              label="Max Sections per Board"
              value={configuration.limits?.maxSectionsPerBoard || 500}
              onChange={handleNumberChange('limits', 'maxSectionsPerBoard')}
              helperText="Maximum board sections allowed"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              type="number"
              label="Max Pin Description Length"
              value={configuration.limits?.maxPinDescriptionLength || 500}
              onChange={handleNumberChange('limits', 'maxPinDescriptionLength')}
              helperText="Maximum characters in pin description"
            />
          </Grid>

          <Grid item xs={12}>
            <Divider sx={{ my: 2 }} />
            <Typography variant="h6" gutterBottom>Media Limits</Typography>
          </Grid>

          <Grid item xs={12} md={6}>
            <Typography gutterBottom>Max Image Size (MB)</Typography>
            <Slider
              value={configuration.limits?.maxImageSizeMB || 32}
              onChange={handleSliderChange('limits', 'maxImageSizeMB')}
              aria-labelledby="image-size-slider"
              valueLabelDisplay="auto"
              min={1}
              max={50}
              step={1}
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <Typography gutterBottom>Max Video Size (MB)</Typography>
            <Slider
              value={configuration.limits?.maxVideoSizeMB || 2048}
              onChange={handleSliderChange('limits', 'maxVideoSizeMB')}
              aria-labelledby="video-size-slider"
              valueLabelDisplay="auto"
              min={100}
              max={4096}
              step={100}
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              type="number"
              label="Max Video Length (seconds)"
              value={configuration.limits?.maxVideoLengthSeconds || 900}
              onChange={handleNumberChange('limits', 'maxVideoLengthSeconds')}
              helperText="Maximum video duration in seconds"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              type="number"
              label="Max Bulk Pins per Request"
              value={configuration.limits?.maxBulkPinsPerRequest || 100}
              onChange={handleNumberChange('limits', 'maxBulkPinsPerRequest')}
              helperText="Maximum pins in a bulk operation"
            />
          </Grid>

          <Grid item xs={12}>
            <Divider sx={{ my: 2 }} />
            <Typography variant="h6" gutterBottom>Polling Settings</Typography>
          </Grid>

          <Grid item xs={12}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.pollingEnabled || false}
                  onChange={(e) => onChange({ ...configuration, pollingEnabled: e.target.checked })}
                />
              }
              label="Enable Polling"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              type="number"
              label="Pin Polling Interval (minutes)"
              value={configuration.pinPollingInterval || 5}
              onChange={(e) => onChange({ ...configuration, pinPollingInterval: parseInt(e.target.value) })}
              helperText="How often to check for new pins"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              type="number"
              label="Board Polling Interval (minutes)"
              value={configuration.boardPollingInterval || 10}
              onChange={(e) => onChange({ ...configuration, boardPollingInterval: parseInt(e.target.value) })}
              helperText="How often to check for board updates"
            />
          </Grid>
        </Grid>
      </TabPanel>

      <TabPanel value={tabValue} index={3}>
        <Grid container spacing={3}>
          <Grid item xs={12}>
            <Typography variant="h6" gutterBottom>Shopping Configuration</Typography>
            <Alert severity="info" sx={{ mb: 2 }}>
              Pinterest Shopping features require a verified merchant account and approved product catalogs.
            </Alert>
          </Grid>

          <Grid item xs={12}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.shoppingEnabled || false}
                  onChange={(e) => onChange({ ...configuration, shoppingEnabled: e.target.checked })}
                />
              }
              label="Enable Shopping Features"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              label="Merchant ID"
              value={configuration.merchantId || ''}
              onChange={handleChange('merchantId')}
              helperText="Your Pinterest Merchant ID"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <FormControl fullWidth>
              <InputLabel>Default Currency</InputLabel>
              <Select
                value={configuration.defaultCurrency || 'USD'}
                onChange={handleSelectChange('defaultCurrency')}
                label="Default Currency"
              >
                <MenuItem value="USD">USD - US Dollar</MenuItem>
                <MenuItem value="EUR">EUR - Euro</MenuItem>
                <MenuItem value="GBP">GBP - British Pound</MenuItem>
                <MenuItem value="CAD">CAD - Canadian Dollar</MenuItem>
                <MenuItem value="AUD">AUD - Australian Dollar</MenuItem>
                <MenuItem value="JPY">JPY - Japanese Yen</MenuItem>
                <MenuItem value="CNY">CNY - Chinese Yuan</MenuItem>
                <MenuItem value="INR">INR - Indian Rupee</MenuItem>
              </Select>
            </FormControl>
          </Grid>

          <Grid item xs={12}>
            <Divider sx={{ my: 2 }} />
            <Typography variant="h6" gutterBottom>Catalog Settings</Typography>
          </Grid>

          <Grid item xs={12} md={6}>
            <FormControl fullWidth>
              <InputLabel>Catalog Format</InputLabel>
              <Select
                value={configuration.catalogFormat || 'CSV'}
                onChange={handleSelectChange('catalogFormat')}
                label="Catalog Format"
              >
                <MenuItem value="CSV">CSV</MenuItem>
                <MenuItem value="TSV">TSV</MenuItem>
                <MenuItem value="XML">XML (RSS/ATOM)</MenuItem>
                <MenuItem value="JSON">JSON</MenuItem>
              </Select>
            </FormControl>
          </Grid>

          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              type="number"
              label="Max Products per Catalog"
              value={configuration.limits?.maxProductsPerCatalog || 20000000}
              onChange={handleNumberChange('limits', 'maxProductsPerCatalog')}
              helperText="Maximum products allowed in a catalog"
            />
          </Grid>

          <Grid item xs={12}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.autoTagProducts || false}
                  onChange={(e) => onChange({ ...configuration, autoTagProducts: e.target.checked })}
                />
              }
              label="Auto-tag Products in Pins"
            />
          </Grid>

          <Grid item xs={12}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.syncInventory || false}
                  onChange={(e) => onChange({ ...configuration, syncInventory: e.target.checked })}
                />
              }
              label="Sync Inventory Availability"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              label="Product Feed URL"
              value={configuration.productFeedUrl || ''}
              onChange={handleChange('productFeedUrl')}
              helperText="URL to your product feed file"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              type="number"
              label="Feed Update Frequency (hours)"
              value={configuration.feedUpdateFrequency || 24}
              onChange={(e) => onChange({ ...configuration, feedUpdateFrequency: parseInt(e.target.value) })}
              helperText="How often to update product feeds"
            />
          </Grid>
        </Grid>
      </TabPanel>

      <TabPanel value={tabValue} index={4}>
        <Grid container spacing={3}>
          <Grid item xs={12}>
            <Typography variant="h6" gutterBottom>Analytics Configuration</Typography>
          </Grid>

          <Grid item xs={12}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.analyticsEnabled || false}
                  onChange={(e) => onChange({ ...configuration, analyticsEnabled: e.target.checked })}
                />
              }
              label="Enable Analytics Collection"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <FormControl fullWidth>
              <InputLabel>Default Report Period</InputLabel>
              <Select
                value={configuration.defaultReportPeriod || 'LAST_30_DAYS'}
                onChange={handleSelectChange('defaultReportPeriod')}
                label="Default Report Period"
              >
                <MenuItem value="LAST_7_DAYS">Last 7 Days</MenuItem>
                <MenuItem value="LAST_14_DAYS">Last 14 Days</MenuItem>
                <MenuItem value="LAST_30_DAYS">Last 30 Days</MenuItem>
                <MenuItem value="LAST_90_DAYS">Last 90 Days</MenuItem>
                <MenuItem value="CUSTOM">Custom Range</MenuItem>
              </Select>
            </FormControl>
          </Grid>

          <Grid item xs={12} md={6}>
            <FormControl fullWidth>
              <InputLabel>Report Granularity</InputLabel>
              <Select
                value={configuration.reportGranularity || 'DAY'}
                onChange={handleSelectChange('reportGranularity')}
                label="Report Granularity"
              >
                <MenuItem value="TOTAL">Total</MenuItem>
                <MenuItem value="HOUR">Hourly</MenuItem>
                <MenuItem value="DAY">Daily</MenuItem>
                <MenuItem value="WEEK">Weekly</MenuItem>
                <MenuItem value="MONTH">Monthly</MenuItem>
              </Select>
            </FormControl>
          </Grid>

          <Grid item xs={12}>
            <Typography variant="subtitle1" gutterBottom>Tracked Metrics</Typography>
            {[
              'Impressions',
              'Saves (Repins)',
              'Pin Clicks',
              'Outbound Clicks',
              'Video Views',
              'Engagement Rate',
              'Save Rate',
              'Comments',
              'Close-ups'
            ].map((metric) => (
              <Chip
                key={metric}
                label={metric}
                sx={{ mr: 1, mb: 1 }}
                color="primary"
                variant="outlined"
              />
            ))}
          </Grid>

          <Grid item xs={12}>
            <Divider sx={{ my: 2 }} />
            <Typography variant="h6" gutterBottom>Conversion Tracking</Typography>
          </Grid>

          <Grid item xs={12}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.conversionTrackingEnabled || false}
                  onChange={(e) => onChange({ ...configuration, conversionTrackingEnabled: e.target.checked })}
                />
              }
              label="Enable Conversion Tracking"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              label="Pinterest Tag ID"
              value={configuration.pinterestTagId || ''}
              onChange={handleChange('pinterestTagId')}
              helperText="Your Pinterest conversion tag ID"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <FormControl fullWidth>
              <InputLabel>Attribution Window</InputLabel>
              <Select
                value={configuration.attributionWindow || '30_DAYS'}
                onChange={handleSelectChange('attributionWindow')}
                label="Attribution Window"
              >
                <MenuItem value="1_DAY">1 Day</MenuItem>
                <MenuItem value="7_DAYS">7 Days</MenuItem>
                <MenuItem value="30_DAYS">30 Days</MenuItem>
                <MenuItem value="60_DAYS">60 Days</MenuItem>
              </Select>
            </FormControl>
          </Grid>

          <Grid item xs={12}>
            <Typography variant="subtitle1" gutterBottom>Tracked Events</Typography>
            {[
              'PageVisit',
              'AddToCart',
              'Checkout',
              'Lead',
              'Signup',
              'Search',
              'ViewCategory',
              'Custom'
            ].map((event) => (
              <Chip
                key={event}
                label={event}
                sx={{ mr: 1, mb: 1 }}
                color="secondary"
                variant="outlined"
              />
            ))}
          </Grid>
        </Grid>
      </TabPanel>

      <TabPanel value={tabValue} index={5}>
        <Grid container spacing={3}>
          <Grid item xs={12}>
            <Typography variant="h6" gutterBottom>Advertising Configuration</Typography>
            <Alert severity="warning" sx={{ mb: 2 }}>
              Pinterest Ads require an approved business account with billing set up.
            </Alert>
          </Grid>

          <Grid item xs={12}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.adsEnabled || false}
                  onChange={(e) => onChange({ ...configuration, adsEnabled: e.target.checked })}
                />
              }
              label="Enable Advertising Features"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <FormControl fullWidth>
              <InputLabel>Default Campaign Objective</InputLabel>
              <Select
                value={configuration.defaultCampaignObjective || 'AWARENESS'}
                onChange={handleSelectChange('defaultCampaignObjective')}
                label="Default Campaign Objective"
              >
                <MenuItem value="AWARENESS">Brand Awareness</MenuItem>
                <MenuItem value="VIDEO_VIEW">Video Views</MenuItem>
                <MenuItem value="CONSIDERATION">Consideration</MenuItem>
                <MenuItem value="TRAFFIC">Traffic</MenuItem>
                <MenuItem value="APP_INSTALL">App Install</MenuItem>
                <MenuItem value="CONVERSIONS">Conversions</MenuItem>
                <MenuItem value="CATALOG_SALES">Catalog Sales</MenuItem>
              </Select>
            </FormControl>
          </Grid>

          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              type="number"
              label="Default Daily Budget ($)"
              value={configuration.defaultDailyBudget || 50}
              onChange={(e) => onChange({ ...configuration, defaultDailyBudget: parseFloat(e.target.value) })}
              helperText="Default daily budget for campaigns"
            />
          </Grid>

          <Grid item xs={12}>
            <Divider sx={{ my: 2 }} />
            <Typography variant="h6" gutterBottom>Audience Targeting</Typography>
          </Grid>

          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              type="number"
              label="Max Audience Size"
              value={configuration.limits?.maxAudienceSize || 10000000}
              onChange={handleNumberChange('limits', 'maxAudienceSize')}
              helperText="Maximum size for custom audiences"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <FormControl fullWidth>
              <InputLabel>Default Targeting Strategy</InputLabel>
              <Select
                value={configuration.defaultTargetingStrategy || 'BALANCED'}
                onChange={handleSelectChange('defaultTargetingStrategy')}
                label="Default Targeting Strategy"
              >
                <MenuItem value="BROAD">Broad</MenuItem>
                <MenuItem value="BALANCED">Balanced</MenuItem>
                <MenuItem value="SPECIFIC">Specific</MenuItem>
                <MenuItem value="CUSTOM">Custom</MenuItem>
              </Select>
            </FormControl>
          </Grid>

          <Grid item xs={12}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.enableAutoBidding || false}
                  onChange={(e) => onChange({ ...configuration, enableAutoBidding: e.target.checked })}
                />
              }
              label="Enable Automatic Bidding"
            />
          </Grid>

          <Grid item xs={12}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.enableCampaignOptimization || false}
                  onChange={(e) => onChange({ ...configuration, enableCampaignOptimization: e.target.checked })}
                />
              }
              label="Enable Campaign Optimization"
            />
          </Grid>

          <Grid item xs={12}>
            <Typography variant="subtitle1" gutterBottom>Available Ad Formats</Typography>
            {[
              'Standard Pin',
              'Video Pin',
              'Shopping Pin',
              'Carousel',
              'Collection',
              'Idea Pin Ad',
              'Quiz Pin'
            ].map((format) => (
              <Chip
                key={format}
                label={format}
                sx={{ mr: 1, mb: 1 }}
                color="primary"
                variant="outlined"
              />
            ))}
          </Grid>
        </Grid>
      </TabPanel>

      <TabPanel value={tabValue} index={6}>
        <Grid container spacing={3}>
          <Grid item xs={12}>
            <Typography variant="h6" gutterBottom>API Rate Limits</Typography>
          </Grid>

          <Grid item xs={12} md={6}>
            <Typography gutterBottom>Requests per Hour</Typography>
            <Slider
              value={configuration.limits?.rateLimitPerHour || 1000}
              onChange={handleSliderChange('limits', 'rateLimitPerHour')}
              aria-labelledby="hourly-rate-limit-slider"
              valueLabelDisplay="auto"
              min={100}
              max={10000}
              step={100}
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <Typography gutterBottom>Requests per Minute</Typography>
            <Slider
              value={configuration.limits?.rateLimitPerMinute || 300}
              onChange={handleSliderChange('limits', 'rateLimitPerMinute')}
              aria-labelledby="minute-rate-limit-slider"
              valueLabelDisplay="auto"
              min={10}
              max={1000}
              step={10}
            />
          </Grid>

          <Grid item xs={12}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.enableRateLimitRetry || false}
                  onChange={(e) => onChange({ ...configuration, enableRateLimitRetry: e.target.checked })}
                />
              }
              label="Auto-retry on Rate Limit"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              type="number"
              label="Max Retry Attempts"
              value={configuration.maxRetryAttempts || 3}
              onChange={(e) => onChange({ ...configuration, maxRetryAttempts: parseInt(e.target.value) })}
              helperText="Maximum retry attempts for failed requests"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              type="number"
              label="Retry Delay (seconds)"
              value={configuration.retryDelay || 5}
              onChange={(e) => onChange({ ...configuration, retryDelay: parseInt(e.target.value) })}
              helperText="Delay between retry attempts"
            />
          </Grid>

          <Grid item xs={12}>
            <Alert severity="info" icon={<InfoIcon />}>
              Pinterest API rate limits vary by endpoint and account type. Configure these settings based on your approved limits.
            </Alert>
          </Grid>

          <Grid item xs={12}>
            <Divider sx={{ my: 2 }} />
            <Typography variant="h6" gutterBottom>Request Optimization</Typography>
          </Grid>

          <Grid item xs={12}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.enableRequestBatching || false}
                  onChange={(e) => onChange({ ...configuration, enableRequestBatching: e.target.checked })}
                />
              }
              label="Enable Request Batching"
            />
          </Grid>

          <Grid item xs={12}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.enableResponseCaching || false}
                  onChange={(e) => onChange({ ...configuration, enableResponseCaching: e.target.checked })}
                />
              }
              label="Enable Response Caching"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              type="number"
              label="Cache TTL (minutes)"
              value={configuration.cacheTTL || 15}
              onChange={(e) => onChange({ ...configuration, cacheTTL: parseInt(e.target.value) })}
              helperText="Cache time-to-live in minutes"
              disabled={!configuration.enableResponseCaching}
            />
          </Grid>
        </Grid>
      </TabPanel>
    </>
  );
};