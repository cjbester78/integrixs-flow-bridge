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
} from '@mui/material';
import {
  Info as InfoIcon,
  ExpandMore as ExpandMoreIcon,
  Analytics as AnalyticsIcon,
  VideoLibrary as VideoLibraryIcon,
  MonetizationOn as MonetizationOnIcon,
  People as PeopleIcon,
  DeviceHub as DeviceHubIcon,
  Language as LanguageIcon,
  TrendingUp as TrendingUpIcon,
} from '@mui/icons-material';

interface YouTubeAnalyticsApiConfigurationProps {
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
      id={`youtube-analytics-tabpanel-${index}`}
      aria-labelledby={`youtube-analytics-tab-${index}`}
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

export const YouTubeAnalyticsApiConfiguration: React.FC<YouTubeAnalyticsApiConfigurationProps> = ({
  configuration,
  onChange,
}) => {
  const [tabValue, setTabValue] = useState(0);
  const [selectedMetrics, setSelectedMetrics] = useState<string[]>(configuration.defaultMetrics || []);
  const [selectedDimensions, setSelectedDimensions] = useState<string[]>(configuration.defaultDimensions || []);

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

  // YouTube Analytics metrics
  const availableMetrics = [
    { value: 'views', label: 'Views' },
    { value: 'estimatedMinutesWatched', label: 'Watch Time (minutes)' },
    { value: 'averageViewDuration', label: 'Average View Duration' },
    { value: 'likes', label: 'Likes' },
    { value: 'comments', label: 'Comments' },
    { value: 'shares', label: 'Shares' },
    { value: 'subscribersGained', label: 'Subscribers Gained' },
    { value: 'subscribersLost', label: 'Subscribers Lost' },
    { value: 'estimatedRevenue', label: 'Estimated Revenue' },
    { value: 'cpm', label: 'CPM' },
    { value: 'adImpressions', label: 'Ad Impressions' },
  ];

  // YouTube Analytics dimensions
  const availableDimensions = [
    { value: 'day', label: 'Day' },
    { value: 'month', label: 'Month' },
    { value: 'video', label: 'Video' },
    { value: 'country', label: 'Country' },
    { value: 'ageGroup', label: 'Age Group' },
    { value: 'gender', label: 'Gender' },
    { value: 'deviceType', label: 'Device Type' },
    { value: 'trafficSourceType', label: 'Traffic Source' },
  ];

  return (
    <Box sx={{ width: '100%' }}>
      <Paper sx={{ p: 3, mb: 3 }}>
        <Typography variant="h6" gutterBottom>
          YouTube Analytics API Configuration
        </Typography>
        <Typography variant="body2" color="text.secondary">
          Configure your YouTube Analytics API integration for comprehensive channel and video analytics.
        </Typography>
      </Paper>

      <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
        <Tabs value={tabValue} onChange={handleTabChange} aria-label="YouTube Analytics configuration tabs" scrollButtons="auto" allowScrollButtonsMobile>
          <Tab icon={<AnalyticsIcon />} label="Basic Settings" />
          <Tab icon={<TrendingUpIcon />} label="Report Types" />
          <Tab icon={<VideoLibraryIcon />} label="Metrics & Dimensions" />
          <Tab icon={<MonetizationOnIcon />} label="Revenue & Ads" />
          <Tab icon={<PeopleIcon />} label="Audience" />
          <Tab icon={<DeviceHubIcon />} label="Traffic & Devices" />
          <Tab icon={<LanguageIcon />} label="Advanced" />
        </Tabs>
      </Box>

      <TabPanel value={tabValue} index={0}>
        {/* Basic Settings Tab */}
        <Grid container spacing={3}>
          <Grid item xs={12}>
            <TextField
              fullWidth
              label="Client ID"
              value={configuration.clientId || ''}
              onChange={(e) => handleConfigChange('clientId', e.target.value)}
              helperText="Your YouTube API OAuth2 Client ID"
            />
          </Grid>
          <Grid item xs={12}>
            <TextField
              fullWidth
              label="Client Secret"
              type="password"
              value={configuration.clientSecret || ''}
              onChange={(e) => handleConfigChange('clientSecret', e.target.value)}
              helperText="Your YouTube API OAuth2 Client Secret"
            />
          </Grid>
          <Grid item xs={12}>
            <TextField
              fullWidth
              label="Channel ID"
              value={configuration.channelId || ''}
              onChange={(e) => handleConfigChange('channelId', e.target.value)}
              helperText="The YouTube channel ID to analyze"
            />
          </Grid>
          <Grid item xs={12}>
            <Divider />
          </Grid>
          <Grid item xs={12}>
            <Typography variant="subtitle1" gutterBottom>
              API Limits
            </Typography>
          </Grid>
          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              type="number"
              label="Max Report Rows"
              value={configuration.limits?.maxReportRows || 10000}
              onChange={(e) => handleLimitChange('maxReportRows', parseInt(e.target.value))}
              inputProps={{ min: 1, max: 50000 }}
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              type="number"
              label="Max Date Range (days)"
              value={configuration.limits?.maxDateRange || 365}
              onChange={(e) => handleLimitChange('maxDateRange', parseInt(e.target.value))}
              inputProps={{ min: 1, max: 365 }}
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              type="number"
              label="Data Availability Delay (hours)"
              value={configuration.limits?.dataAvailabilityDelayHours || 48}
              onChange={(e) => handleLimitChange('dataAvailabilityDelayHours', parseInt(e.target.value))}
              helperText="YouTube Analytics data typically has a 48-hour delay"
              inputProps={{ min: 24, max: 72 }}
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              type="number"
              label="Realtime Delay (minutes)"
              value={configuration.limits?.realtimeDelayMinutes || 5}
              onChange={(e) => handleLimitChange('realtimeDelayMinutes', parseInt(e.target.value))}
              inputProps={{ min: 1, max: 60 }}
            />
          </Grid>
        </Grid>
      </TabPanel>

      <TabPanel value={tabValue} index={1}>
        {/* Report Types Tab */}
        <Grid container spacing={3}>
          <Grid item xs={12}>
            <Typography variant="subtitle1" gutterBottom>
              Enable Report Types
            </Typography>
            <Typography variant="body2" color="text.secondary" gutterBottom>
              Select which types of analytics reports you want to enable
            </Typography>
          </Grid>
          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableChannelReports ?? true}
                  onChange={() => handleFeatureToggle('enableChannelReports')}
                />
              }
              label="Channel Reports"
            />
            <Typography variant="caption" display="block" sx={{ ml: 4 }}>
              Overall channel performance metrics
            </Typography>
          </Grid>
          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableVideoReports ?? true}
                  onChange={() => handleFeatureToggle('enableVideoReports')}
                />
              }
              label="Video Reports"
            />
            <Typography variant="caption" display="block" sx={{ ml: 4 }}>
              Individual video performance analytics
            </Typography>
          </Grid>
          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enablePlaylistReports ?? true}
                  onChange={() => handleFeatureToggle('enablePlaylistReports')}
                />
              }
              label="Playlist Reports"
            />
            <Typography variant="caption" display="block" sx={{ ml: 4 }}>
              Playlist performance and engagement
            </Typography>
          </Grid>
          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableRevenueReports ?? true}
                  onChange={() => handleFeatureToggle('enableRevenueReports')}
                />
              }
              label="Revenue Reports"
            />
            <Typography variant="caption" display="block" sx={{ ml: 4 }}>
              Monetization and earnings data
            </Typography>
          </Grid>
          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableEngagementReports ?? true}
                  onChange={() => handleFeatureToggle('enableEngagementReports')}
                />
              }
              label="Engagement Reports"
            />
            <Typography variant="caption" display="block" sx={{ ml: 4 }}>
              Likes, comments, shares, and subscribers
            </Typography>
          </Grid>
          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableAudienceReports ?? true}
                  onChange={() => handleFeatureToggle('enableAudienceReports')}
                />
              }
              label="Audience Reports"
            />
            <Typography variant="caption" display="block" sx={{ ml: 4 }}>
              Demographics and viewer insights
            </Typography>
          </Grid>
          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableTrafficSourceReports ?? true}
                  onChange={() => handleFeatureToggle('enableTrafficSourceReports')}
                />
              }
              label="Traffic Source Reports"
            />
            <Typography variant="caption" display="block" sx={{ ml: 4 }}>
              Where your viewers come from
            </Typography>
          </Grid>
          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableRealtimeReports ?? true}
                  onChange={() => handleFeatureToggle('enableRealtimeReports')}
                />
              }
              label="Realtime Reports"
            />
            <Typography variant="caption" display="block" sx={{ ml: 4 }}>
              Near real-time analytics data
            </Typography>
          </Grid>
          <Grid item xs={12}>
            <Divider sx={{ my: 2 }} />
          </Grid>
          <Grid item xs={12}>
            <Typography variant="subtitle2" gutterBottom>
              Advanced Report Types
            </Typography>
          </Grid>
          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableDeviceReports ?? true}
                  onChange={() => handleFeatureToggle('enableDeviceReports')}
                />
              }
              label="Device Reports"
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableGeographyReports ?? true}
                  onChange={() => handleFeatureToggle('enableGeographyReports')}
                />
              }
              label="Geography Reports"
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableSearchTermReports ?? true}
                  onChange={() => handleFeatureToggle('enableSearchTermReports')}
                />
              }
              label="Search Term Reports"
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableSharingServiceReports ?? true}
                  onChange={() => handleFeatureToggle('enableSharingServiceReports')}
                />
              }
              label="Sharing Service Reports"
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableAnnotationsReports ?? true}
                  onChange={() => handleFeatureToggle('enableAnnotationsReports')}
                />
              }
              label="Annotations Reports"
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableCardsReports ?? true}
                  onChange={() => handleFeatureToggle('enableCardsReports')}
                />
              }
              label="Cards Reports"
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableEndScreenReports ?? true}
                  onChange={() => handleFeatureToggle('enableEndScreenReports')}
                />
              }
              label="End Screen Reports"
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enablePlaybackLocationReports ?? true}
                  onChange={() => handleFeatureToggle('enablePlaybackLocationReports')}
                />
              }
              label="Playback Location Reports"
            />
          </Grid>
        </Grid>
      </TabPanel>

      <TabPanel value={tabValue} index={2}>
        {/* Metrics & Dimensions Tab */}
        <Grid container spacing={3}>
          <Grid item xs={12}>
            <Typography variant="subtitle1" gutterBottom>
              Default Metrics
            </Typography>
            <Typography variant="body2" color="text.secondary" gutterBottom>
              Select the default metrics to include in reports
            </Typography>
            <FormControl fullWidth sx={{ mt: 2 }}>
              <InputLabel>Default Metrics</InputLabel>
              <Select
                multiple
                value={selectedMetrics}
                onChange={(e) => {
                  const value = e.target.value as string[];
                  setSelectedMetrics(value);
                  handleConfigChange('defaultMetrics', value);
                }}
                input={<OutlinedInput label="Default Metrics" />}
                renderValue={(selected) => (
                  <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                    {selected.map((value) => (
                      <Chip key={value} label={availableMetrics.find(m => m.value === value)?.label || value} size="small" />
                    ))}
                  </Box>
                )}
              >
                {availableMetrics.map((metric) => (
                  <MenuItem key={metric.value} value={metric.value}>
                    {metric.label}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          </Grid>
          <Grid item xs={12}>
            <Typography variant="subtitle1" gutterBottom>
              Default Dimensions
            </Typography>
            <Typography variant="body2" color="text.secondary" gutterBottom>
              Select the default dimensions for data grouping
            </Typography>
            <FormControl fullWidth sx={{ mt: 2 }}>
              <InputLabel>Default Dimensions</InputLabel>
              <Select
                multiple
                value={selectedDimensions}
                onChange={(e) => {
                  const value = e.target.value as string[];
                  setSelectedDimensions(value);
                  handleConfigChange('defaultDimensions', value);
                }}
                input={<OutlinedInput label="Default Dimensions" />}
                renderValue={(selected) => (
                  <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                    {selected.map((value) => (
                      <Chip key={value} label={availableDimensions.find(d => d.value === value)?.label || value} size="small" />
                    ))}
                  </Box>
                )}
              >
                {availableDimensions.map((dimension) => (
                  <MenuItem key={dimension.value} value={dimension.value}>
                    {dimension.label}
                  </MenuItem>
                ))}
              </Select>
              <FormHelperText>Maximum {configuration.limits?.maxReportDimensions || 3} dimensions allowed</FormHelperText>
            </FormControl>
          </Grid>
          <Grid item xs={12}>
            <Divider sx={{ my: 2 }} />
          </Grid>
          <Grid item xs={12}>
            <Typography variant="subtitle1" gutterBottom>
              Report Limits
            </Typography>
          </Grid>
          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              type="number"
              label="Max Report Dimensions"
              value={configuration.limits?.maxReportDimensions || 3}
              onChange={(e) => handleLimitChange('maxReportDimensions', parseInt(e.target.value))}
              inputProps={{ min: 1, max: 5 }}
              helperText="Maximum dimensions per report"
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              type="number"
              label="Max Report Metrics"
              value={configuration.limits?.maxReportMetrics || 10}
              onChange={(e) => handleLimitChange('maxReportMetrics', parseInt(e.target.value))}
              inputProps={{ min: 1, max: 20 }}
              helperText="Maximum metrics per report"
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              type="number"
              label="Max Filters"
              value={configuration.limits?.maxFilters || 5}
              onChange={(e) => handleLimitChange('maxFilters', parseInt(e.target.value))}
              inputProps={{ min: 1, max: 10 }}
              helperText="Maximum filters per query"
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              type="number"
              label="Max Group Items"
              value={configuration.limits?.maxGroupItems || 500}
              onChange={(e) => handleLimitChange('maxGroupItems', parseInt(e.target.value))}
              inputProps={{ min: 50, max: 1000 }}
              helperText="Maximum items in analytics groups"
            />
          </Grid>
        </Grid>
      </TabPanel>

      <TabPanel value={tabValue} index={3}>
        {/* Revenue & Ads Tab */}
        <Grid container spacing={3}>
          <Grid item xs={12}>
            <Alert severity="info">
              Revenue reporting requires YouTube Partner Program membership and monetization enabled.
            </Alert>
          </Grid>
          <Grid item xs={12}>
            <Typography variant="subtitle1" gutterBottom>
              Revenue Features
            </Typography>
          </Grid>
          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableRevenueReports ?? true}
                  onChange={() => handleFeatureToggle('enableRevenueReports')}
                />
              }
              label="Revenue Tracking"
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableAdPerformanceReports ?? true}
                  onChange={() => handleFeatureToggle('enableAdPerformanceReports')}
                />
              }
              label="Ad Performance Reports"
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableContentOwnerReports ?? false}
                  onChange={() => handleFeatureToggle('enableContentOwnerReports')}
                />
              }
              label="Content Owner Reports"
            />
            <Typography variant="caption" display="block" sx={{ ml: 4 }}>
              Requires Content Manager access
            </Typography>
          </Grid>
          <Grid item xs={12}>
            <FormControl fullWidth>
              <InputLabel>Default Currency</InputLabel>
              <Select
                value={configuration.defaultCurrency || 'USD'}
                onChange={(e) => handleConfigChange('defaultCurrency', e.target.value)}
                label="Default Currency"
              >
                <MenuItem value="USD">USD - US Dollar</MenuItem>
                <MenuItem value="EUR">EUR - Euro</MenuItem>
                <MenuItem value="GBP">GBP - British Pound</MenuItem>
                <MenuItem value="JPY">JPY - Japanese Yen</MenuItem>
                <MenuItem value="CAD">CAD - Canadian Dollar</MenuItem>
                <MenuItem value="AUD">AUD - Australian Dollar</MenuItem>
                <MenuItem value="INR">INR - Indian Rupee</MenuItem>
                <MenuItem value="BRL">BRL - Brazilian Real</MenuItem>
              </Select>
            </FormControl>
          </Grid>
          <Grid item xs={12}>
            <Divider sx={{ my: 2 }} />
          </Grid>
          <Grid item xs={12}>
            <Typography variant="subtitle1" gutterBottom>
              Revenue Metrics
            </Typography>
            <FormGroup row>
              <FormControlLabel
                control={<Switch defaultChecked />}
                label="Estimated Revenue"
              />
              <FormControlLabel
                control={<Switch defaultChecked />}
                label="Ad Revenue"
              />
              <FormControlLabel
                control={<Switch defaultChecked />}
                label="YouTube Premium Revenue"
              />
              <FormControlLabel
                control={<Switch defaultChecked />}
                label="CPM"
              />
              <FormControlLabel
                control={<Switch defaultChecked />}
                label="Playback-based CPM"
              />
            </FormGroup>
          </Grid>
        </Grid>
      </TabPanel>

      <TabPanel value={tabValue} index={4}>
        {/* Audience Tab */}
        <Grid container spacing={3}>
          <Grid item xs={12}>
            <Typography variant="subtitle1" gutterBottom>
              Audience Analytics
            </Typography>
          </Grid>
          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableDemographicsReports ?? true}
                  onChange={() => handleFeatureToggle('enableDemographicsReports')}
                />
              }
              label="Demographics Reports"
            />
            <Typography variant="caption" display="block" sx={{ ml: 4 }}>
              Age and gender distribution
            </Typography>
          </Grid>
          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableGeographyReports ?? true}
                  onChange={() => handleFeatureToggle('enableGeographyReports')}
                />
              }
              label="Geography Reports"
            />
            <Typography variant="caption" display="block" sx={{ ml: 4 }}>
              Viewer location data
            </Typography>
          </Grid>
          <Grid item xs={12}>
            <Typography variant="subtitle2" gutterBottom>
              Age Group Tracking
            </Typography>
            <FormGroup row>
              <FormControlLabel control={<Switch defaultChecked />} label="13-17" />
              <FormControlLabel control={<Switch defaultChecked />} label="18-24" />
              <FormControlLabel control={<Switch defaultChecked />} label="25-34" />
              <FormControlLabel control={<Switch defaultChecked />} label="35-44" />
              <FormControlLabel control={<Switch defaultChecked />} label="45-54" />
              <FormControlLabel control={<Switch defaultChecked />} label="55-64" />
              <FormControlLabel control={<Switch defaultChecked />} label="65+" />
            </FormGroup>
          </Grid>
          <Grid item xs={12}>
            <Typography variant="subtitle2" gutterBottom>
              Geography Detail Level
            </Typography>
            <FormControl fullWidth>
              <InputLabel>Geography Detail</InputLabel>
              <Select
                value={configuration.geographyDetail || 'country'}
                onChange={(e) => handleConfigChange('geographyDetail', e.target.value)}
                label="Geography Detail"
              >
                <MenuItem value="country">Country</MenuItem>
                <MenuItem value="province">Province/State</MenuItem>
                <MenuItem value="city">City</MenuItem>
              </Select>
            </FormControl>
          </Grid>
        </Grid>
      </TabPanel>

      <TabPanel value={tabValue} index={5}>
        {/* Traffic & Devices Tab */}
        <Grid container spacing={3}>
          <Grid item xs={12}>
            <Typography variant="subtitle1" gutterBottom>
              Traffic Source Analysis
            </Typography>
          </Grid>
          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableTrafficSourceReports ?? true}
                  onChange={() => handleFeatureToggle('enableTrafficSourceReports')}
                />
              }
              label="Traffic Source Reports"
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableSearchTermReports ?? true}
                  onChange={() => handleFeatureToggle('enableSearchTermReports')}
                />
              }
              label="Search Term Reports"
            />
          </Grid>
          <Grid item xs={12}>
            <Typography variant="subtitle2" gutterBottom>
              Track Traffic Sources
            </Typography>
            <FormGroup row>
              <FormControlLabel control={<Switch defaultChecked />} label="YouTube Search" />
              <FormControlLabel control={<Switch defaultChecked />} label="Suggested Videos" />
              <FormControlLabel control={<Switch defaultChecked />} label="External" />
              <FormControlLabel control={<Switch defaultChecked />} label="Browse Features" />
              <FormControlLabel control={<Switch defaultChecked />} label="Channel Pages" />
              <FormControlLabel control={<Switch defaultChecked />} label="Notifications" />
              <FormControlLabel control={<Switch defaultChecked />} label="Playlists" />
            </FormGroup>
          </Grid>
          <Grid item xs={12}>
            <Divider sx={{ my: 2 }} />
          </Grid>
          <Grid item xs={12}>
            <Typography variant="subtitle1" gutterBottom>
              Device Analytics
            </Typography>
          </Grid>
          <Grid item xs={12}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableDeviceReports ?? true}
                  onChange={() => handleFeatureToggle('enableDeviceReports')}
                />
              }
              label="Device Type Reports"
            />
          </Grid>
          <Grid item xs={12}>
            <Typography variant="subtitle2" gutterBottom>
              Track Device Types
            </Typography>
            <FormGroup row>
              <FormControlLabel control={<Switch defaultChecked />} label="Desktop" />
              <FormControlLabel control={<Switch defaultChecked />} label="Mobile" />
              <FormControlLabel control={<Switch defaultChecked />} label="Tablet" />
              <FormControlLabel control={<Switch defaultChecked />} label="TV" />
              <FormControlLabel control={<Switch defaultChecked />} label="Game Console" />
            </FormGroup>
          </Grid>
          <Grid item xs={12}>
            <Typography variant="subtitle2" gutterBottom>
              Operating Systems
            </Typography>
            <FormGroup row>
              <FormControlLabel control={<Switch defaultChecked />} label="Android" />
              <FormControlLabel control={<Switch defaultChecked />} label="iOS" />
              <FormControlLabel control={<Switch defaultChecked />} label="Windows" />
              <FormControlLabel control={<Switch defaultChecked />} label="macOS" />
              <FormControlLabel control={<Switch defaultChecked />} label="Linux" />
            </FormGroup>
          </Grid>
        </Grid>
      </TabPanel>

      <TabPanel value={tabValue} index={6}>
        {/* Advanced Tab */}
        <Grid container spacing={3}>
          <Grid item xs={12}>
            <Typography variant="subtitle1" gutterBottom>
              Advanced Features
            </Typography>
          </Grid>
          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableSubtitlesReports ?? true}
                  onChange={() => handleFeatureToggle('enableSubtitlesReports')}
                />
              }
              label="Subtitles/CC Reports"
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enablePlaybackLocationReports ?? true}
                  onChange={() => handleFeatureToggle('enablePlaybackLocationReports')}
                />
              }
              label="Playback Location Reports"
            />
          </Grid>
          <Grid item xs={12}>
            <Typography variant="subtitle2" gutterBottom>
              Export Settings
            </Typography>
          </Grid>
          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.enableCsvExport ?? true}
                  onChange={(e) => handleConfigChange('enableCsvExport', e.target.checked)}
                />
              }
              label="Enable CSV Export"
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.enableScheduledExports ?? false}
                  onChange={(e) => handleConfigChange('enableScheduledExports', e.target.checked)}
                />
              }
              label="Enable Scheduled Exports"
            />
          </Grid>
          <Grid item xs={12}>
            <Typography variant="subtitle2" gutterBottom>
              API Configuration
            </Typography>
          </Grid>
          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              label="API Version"
              value={configuration.apiVersion || 'v2'}
              onChange={(e) => handleConfigChange('apiVersion', e.target.value)}
              disabled
              helperText="YouTube Analytics API version"
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              type="number"
              label="Request Timeout (seconds)"
              value={configuration.requestTimeout || 30}
              onChange={(e) => handleConfigChange('requestTimeout', parseInt(e.target.value))}
              inputProps={{ min: 10, max: 120 }}
            />
          </Grid>
          <Grid item xs={12}>
            <Typography variant="subtitle2" gutterBottom>
              Polling Intervals
            </Typography>
          </Grid>
          <Grid item xs={12} md={4}>
            <TextField
              fullWidth
              type="number"
              label="Channel Report Interval (min)"
              value={configuration.channelReportInterval || 60}
              onChange={(e) => handleConfigChange('channelReportInterval', parseInt(e.target.value))}
              inputProps={{ min: 15, max: 1440 }}
            />
          </Grid>
          <Grid item xs={12} md={4}>
            <TextField
              fullWidth
              type="number"
              label="Video Report Interval (min)"
              value={configuration.videoReportInterval || 60}
              onChange={(e) => handleConfigChange('videoReportInterval', parseInt(e.target.value))}
              inputProps={{ min: 15, max: 1440 }}
            />
          </Grid>
          <Grid item xs={12} md={4}>
            <TextField
              fullWidth
              type="number"
              label="Revenue Report Interval (min)"
              value={configuration.revenueReportInterval || 1440}
              onChange={(e) => handleConfigChange('revenueReportInterval', parseInt(e.target.value))}
              inputProps={{ min: 60, max: 10080 }}
            />
          </Grid>
        </Grid>
      </TabPanel>
    </Box>
  );
};

export default YouTubeAnalyticsApiConfiguration;