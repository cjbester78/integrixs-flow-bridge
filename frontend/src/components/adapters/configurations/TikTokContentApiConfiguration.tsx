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
  VideoLibrary as VideoLibraryIcon,
  Comment as CommentIcon,
  Analytics as AnalyticsIcon,
  MusicNote as MusicNoteIcon,
  Tag as TagIcon,
  Group as GroupIcon,
  Explore as ExploreIcon,
} from '@mui/icons-material';

interface TikTokContentApiConfigurationProps {
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
      id={`tiktok-content-tabpanel-${index}`}
      aria-labelledby={`tiktok-content-tab-${index}`}
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

export const TikTokContentApiConfiguration: React.FC<TikTokContentApiConfigurationProps> = ({
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

  // Content categories
  const contentCategories = [
    { value: 'DANCE', label: 'Dance' },
    { value: 'MUSIC', label: 'Music' },
    { value: 'COMEDY', label: 'Comedy' },
    { value: 'EDUCATION', label: 'Education' },
    { value: 'LIFESTYLE', label: 'Lifestyle' },
    { value: 'FASHION', label: 'Fashion & Beauty' },
    { value: 'FOOD', label: 'Food' },
    { value: 'GAMING', label: 'Gaming' },
    { value: 'TECHNOLOGY', label: 'Technology' },
  ];

  return (
    <Box sx={{ width: '100%' }}>
      <Paper sx={{ p: 3, mb: 3 }}>
        <Typography variant="h6" gutterBottom>
          TikTok Content API Configuration
        </Typography>
        <Typography variant="body2" color="text.secondary">
          Configure your TikTok Content API integration for video publishing, analytics, and engagement.
        </Typography>
        <Link href="https://developers.tiktok.com/" target="_blank" rel="noopener" sx={{ mt: 1, display: 'block' }}>
          View TikTok for Developers Documentation
        </Link>
      </Paper>

      <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
        <Tabs value={tabValue} onChange={handleTabChange} aria-label="TikTok Content configuration tabs" scrollButtons="auto" allowScrollButtonsMobile>
          <Tab icon={<VideoLibraryIcon />} label="Basic Settings" />
          <Tab icon={<VideoLibraryIcon />} label="Video Management" />
          <Tab icon={<CommentIcon />} label="Engagement" />
          <Tab icon={<AnalyticsIcon />} label="Analytics" />
          <Tab icon={<MusicNoteIcon />} label="Music & Effects" />
          <Tab icon={<TagIcon />} label="Trends & Discovery" />
          <Tab icon={<GroupIcon />} label="Collaboration" />
        </Tabs>
      </Box>

      <TabPanel value={tabValue} index={0}>
        {/* Basic Settings Tab */}
        <Grid container spacing={3}>
          <Grid item xs={12}>
            <Alert severity="info">
              To use the TikTok Content API, you need to register your app on the TikTok for Developers platform.
            </Alert>
          </Grid>
          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              label="Client Key"
              value={configuration.clientKey || ''}
              onChange={(e) => handleConfigChange('clientKey', e.target.value)}
              helperText="Your TikTok app client key"
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              label="Client Secret"
              type="password"
              value={configuration.clientSecret || ''}
              onChange={(e) => handleConfigChange('clientSecret', e.target.value)}
              helperText="Your TikTok app client secret"
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              label="User ID"
              value={configuration.userId || ''}
              onChange={(e) => handleConfigChange('userId', e.target.value)}
              helperText="TikTok user ID for content management"
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              label="Username"
              value={configuration.username || ''}
              onChange={(e) => handleConfigChange('username', e.target.value)}
              helperText="TikTok username (for display)"
            />
          </Grid>
          <Grid item xs={12}>
            <Divider />
          </Grid>
          <Grid item xs={12}>
            <Typography variant="subtitle1" gutterBottom>
              Content Limits
            </Typography>
          </Grid>
          <Grid item xs={12} md={4}>
            <TextField
              fullWidth
              type="number"
              label="Max Video Duration (seconds)"
              value={configuration.limits?.maxVideoDurationSeconds || 180}
              onChange={(e) => handleLimitChange('maxVideoDurationSeconds', parseInt(e.target.value))}
              inputProps={{ min: 3, max: 600 }}
              helperText="Maximum video length"
            />
          </Grid>
          <Grid item xs={12} md={4}>
            <TextField
              fullWidth
              type="number"
              label="Max Video Size (MB)"
              value={configuration.limits?.maxVideoSizeMB || 287}
              onChange={(e) => handleLimitChange('maxVideoSizeMB', parseInt(e.target.value))}
              inputProps={{ min: 10, max: 500 }}
            />
          </Grid>
          <Grid item xs={12} md={4}>
            <TextField
              fullWidth
              type="number"
              label="Max Videos per Day"
              value={configuration.limits?.maxVideosPerDay || 10}
              onChange={(e) => handleLimitChange('maxVideosPerDay', parseInt(e.target.value))}
              inputProps={{ min: 1, max: 50 }}
            />
          </Grid>
        </Grid>
      </TabPanel>

      <TabPanel value={tabValue} index={1}>
        {/* Video Management Tab */}
        <Grid container spacing={3}>
          <Grid item xs={12}>
            <Typography variant="subtitle1" gutterBottom>
              Video Features
            </Typography>
          </Grid>
          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableVideoPublishing ?? true}
                  onChange={() => handleFeatureToggle('enableVideoPublishing')}
                />
              }
              label="Video Publishing"
            />
            <Typography variant="caption" display="block" sx={{ ml: 4 }}>
              Upload and publish videos to TikTok
            </Typography>
          </Grid>
          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableVideoRetrieval ?? true}
                  onChange={() => handleFeatureToggle('enableVideoRetrieval')}
                />
              }
              label="Video Retrieval"
            />
          </Grid>
          <Grid item xs={12}>
            <Divider sx={{ my: 2 }} />
          </Grid>
          <Grid item xs={12}>
            <Typography variant="subtitle1" gutterBottom>
              Video Settings
            </Typography>
          </Grid>
          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              type="number"
              label="Max Description Length"
              value={configuration.limits?.maxDescriptionLength || 2200}
              onChange={(e) => handleLimitChange('maxDescriptionLength', parseInt(e.target.value))}
              inputProps={{ min: 100, max: 5000 }}
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              type="number"
              label="Max Hashtags per Post"
              value={configuration.limits?.maxHashtagsPerPost || 100}
              onChange={(e) => handleLimitChange('maxHashtagsPerPost', parseInt(e.target.value))}
              inputProps={{ min: 1, max: 200 }}
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              type="number"
              label="Max Mentions per Post"
              value={configuration.limits?.maxMentionsPerPost || 30}
              onChange={(e) => handleLimitChange('maxMentionsPerPost', parseInt(e.target.value))}
              inputProps={{ min: 1, max: 50 }}
            />
          </Grid>
          <Grid item xs={12}>
            <FormControl fullWidth>
              <InputLabel>Default Privacy Setting</InputLabel>
              <Select
                value={configuration.defaultPrivacy || 'PUBLIC_TO_EVERYONE'}
                onChange={(e) => handleConfigChange('defaultPrivacy', e.target.value)}
                label="Default Privacy Setting"
              >
                <MenuItem value="PUBLIC_TO_EVERYONE">Public to Everyone</MenuItem>
                <MenuItem value="FRIENDS_ONLY">Friends Only</MenuItem>
                <MenuItem value="PRIVATE_ONLY_ME">Private (Only Me)</MenuItem>
              </Select>
            </FormControl>
          </Grid>
          <Grid item xs={12}>
            <Typography variant="subtitle2" gutterBottom>
              Default Video Permissions
            </Typography>
            <FormGroup row>
              <FormControlLabel control={<Switch defaultChecked />} label="Allow Comments" />
              <FormControlLabel control={<Switch defaultChecked />} label="Allow Duet" />
              <FormControlLabel control={<Switch defaultChecked />} label="Allow Stitch" />
              <FormControlLabel control={<Switch defaultChecked />} label="Allow Download" />
            </FormGroup>
          </Grid>
        </Grid>
      </TabPanel>

      <TabPanel value={tabValue} index={2}>
        {/* Engagement Tab */}
        <Grid container spacing={3}>
          <Grid item xs={12}>
            <Typography variant="subtitle1" gutterBottom>
              Engagement Features
            </Typography>
          </Grid>
          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableCommentManagement ?? true}
                  onChange={() => handleFeatureToggle('enableCommentManagement')}
                />
              }
              label="Comment Management"
            />
            <Typography variant="caption" display="block" sx={{ ml: 4 }}>
              Read, post, and moderate comments
            </Typography>
          </Grid>
          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableEngagementMetrics ?? true}
                  onChange={() => handleFeatureToggle('enableEngagementMetrics')}
                />
              }
              label="Engagement Metrics"
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableFollowerAnalytics ?? true}
                  onChange={() => handleFeatureToggle('enableFollowerAnalytics')}
                />
              }
              label="Follower Analytics"
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableUserProfile ?? true}
                  onChange={() => handleFeatureToggle('enableUserProfile')}
                />
              }
              label="Profile Management"
            />
          </Grid>
          <Grid item xs={12}>
            <TextField
              fullWidth
              type="number"
              label="Max Comments to Retrieve"
              value={configuration.limits?.maxCommentsToRetrieve || 1000}
              onChange={(e) => handleLimitChange('maxCommentsToRetrieve', parseInt(e.target.value))}
              inputProps={{ min: 50, max: 5000 }}
            />
          </Grid>
          <Grid item xs={12}>
            <TextField
              fullWidth
              type="number"
              label="Max Followers to Retrieve"
              value={configuration.limits?.maxFollowersToRetrieve || 10000}
              onChange={(e) => handleLimitChange('maxFollowersToRetrieve', parseInt(e.target.value))}
              inputProps={{ min: 100, max: 50000 }}
            />
          </Grid>
          <Grid item xs={12}>
            <FormControl fullWidth>
              <InputLabel>Default Comment Filter</InputLabel>
              <Select
                value={configuration.defaultCommentFilter || 'ALL'}
                onChange={(e) => handleConfigChange('defaultCommentFilter', e.target.value)}
                label="Default Comment Filter"
              >
                <MenuItem value="ALL">All Comments</MenuItem>
                <MenuItem value="FRIENDS_ONLY">Friends Only</MenuItem>
                <MenuItem value="NO_ONE">No One</MenuItem>
                <MenuItem value="FILTERED_KEYWORDS">Filtered Keywords</MenuItem>
              </Select>
            </FormControl>
          </Grid>
        </Grid>
      </TabPanel>

      <TabPanel value={tabValue} index={3}>
        {/* Analytics Tab */}
        <Grid container spacing={3}>
          <Grid item xs={12}>
            <Typography variant="subtitle1" gutterBottom>
              Analytics Features
            </Typography>
          </Grid>
          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableContentInsights ?? true}
                  onChange={() => handleFeatureToggle('enableContentInsights')}
                />
              }
              label="Content Insights"
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableViralTrends ?? true}
                  onChange={() => handleFeatureToggle('enableViralTrends')}
                />
              }
              label="Viral Trends Analysis"
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
          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              type="number"
              label="Analytics Retention Days"
              value={configuration.limits?.analyticsRetentionDays || 90}
              onChange={(e) => handleLimitChange('analyticsRetentionDays', parseInt(e.target.value))}
              inputProps={{ min: 7, max: 365 }}
            />
          </Grid>
          <Grid item xs={12}>
            <Typography variant="subtitle2" gutterBottom>
              Track Analytics Metrics
            </Typography>
            <FormGroup row>
              <FormControlLabel control={<Switch defaultChecked />} label="Views" />
              <FormControlLabel control={<Switch defaultChecked />} label="Likes" />
              <FormControlLabel control={<Switch defaultChecked />} label="Comments" />
              <FormControlLabel control={<Switch defaultChecked />} label="Shares" />
              <FormControlLabel control={<Switch defaultChecked />} label="Completion Rate" />
              <FormControlLabel control={<Switch defaultChecked />} label="Average Watch Time" />
              <FormControlLabel control={<Switch defaultChecked />} label="Followers Gained" />
              <FormControlLabel control={<Switch defaultChecked />} label="Engagement Rate" />
            </FormGroup>
          </Grid>
        </Grid>
      </TabPanel>

      <TabPanel value={tabValue} index={4}>
        {/* Music & Effects Tab */}
        <Grid container spacing={3}>
          <Grid item xs={12}>
            <Typography variant="subtitle1" gutterBottom>
              Creative Features
            </Typography>
          </Grid>
          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableMusicIntegration ?? true}
                  onChange={() => handleFeatureToggle('enableMusicIntegration')}
                />
              }
              label="Music Integration"
            />
            <Typography variant="caption" display="block" sx={{ ml: 4 }}>
              Search and use TikTok sounds
            </Typography>
          </Grid>
          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableEffectsManagement ?? true}
                  onChange={() => handleFeatureToggle('enableEffectsManagement')}
                />
              }
              label="Effects Management"
            />
            <Typography variant="caption" display="block" sx={{ ml: 4 }}>
              Apply filters and effects to videos
            </Typography>
          </Grid>
          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableSoundLibrary ?? true}
                  onChange={() => handleFeatureToggle('enableSoundLibrary')}
                />
              }
              label="Sound Library Access"
            />
          </Grid>
          <Grid item xs={12}>
            <Typography variant="subtitle2" gutterBottom>
              Sound Categories
            </Typography>
            <FormGroup row>
              <FormControlLabel control={<Switch defaultChecked />} label="Trending" />
              <FormControlLabel control={<Switch defaultChecked />} label="Recommended" />
              <FormControlLabel control={<Switch defaultChecked />} label="Favorites" />
              <FormControlLabel control={<Switch defaultChecked />} label="Original Sounds" />
              <FormControlLabel control={<Switch defaultChecked />} label="Genres" />
              <FormControlLabel control={<Switch defaultChecked />} label="Moods" />
            </FormGroup>
          </Grid>
          <Grid item xs={12}>
            <Typography variant="subtitle2" gutterBottom>
              Effect Types
            </Typography>
            <FormGroup row>
              <FormControlLabel control={<Switch defaultChecked />} label="Beauty" />
              <FormControlLabel control={<Switch defaultChecked />} label="Funny" />
              <FormControlLabel control={<Switch defaultChecked />} label="World" />
              <FormControlLabel control={<Switch defaultChecked />} label="Animal" />
              <FormControlLabel control={<Switch defaultChecked />} label="Interactive" />
              <FormControlLabel control={<Switch defaultChecked />} label="Transition" />
            </FormGroup>
          </Grid>
        </Grid>
      </TabPanel>

      <TabPanel value={tabValue} index={5}>
        {/* Trends & Discovery Tab */}
        <Grid container spacing={3}>
          <Grid item xs={12}>
            <Typography variant="subtitle1" gutterBottom>
              Discovery Features
            </Typography>
          </Grid>
          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableTrendingContent ?? true}
                  onChange={() => handleFeatureToggle('enableTrendingContent')}
                />
              }
              label="Trending Content"
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableHashtagAnalytics ?? true}
                  onChange={() => handleFeatureToggle('enableHashtagAnalytics')}
                />
              }
              label="Hashtag Analytics"
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableContentDiscovery ?? true}
                  onChange={() => handleFeatureToggle('enableContentDiscovery')}
                />
              }
              label="Content Discovery"
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableCreatorSearch ?? true}
                  onChange={() => handleFeatureToggle('enableCreatorSearch')}
                />
              }
              label="Creator Search"
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableChallengeParticipation ?? true}
                  onChange={() => handleFeatureToggle('enableChallengeParticipation')}
                />
              }
              label="Challenge Participation"
            />
          </Grid>
          <Grid item xs={12}>
            <TextField
              fullWidth
              type="number"
              label="Max Trending Videos"
              value={configuration.limits?.maxTrendingVideos || 100}
              onChange={(e) => handleLimitChange('maxTrendingVideos', parseInt(e.target.value))}
              inputProps={{ min: 10, max: 500 }}
            />
          </Grid>
          <Grid item xs={12}>
            <TextField
              fullWidth
              type="number"
              label="Max Search Results"
              value={configuration.limits?.maxSearchResults || 100}
              onChange={(e) => handleLimitChange('maxSearchResults', parseInt(e.target.value))}
              inputProps={{ min: 10, max: 500 }}
            />
          </Grid>
          <Grid item xs={12}>
            <FormControl fullWidth>
              <InputLabel>Default Content Category</InputLabel>
              <Select
                value={configuration.defaultCategory || ''}
                onChange={(e) => handleConfigChange('defaultCategory', e.target.value)}
                label="Default Content Category"
              >
                <MenuItem value="">All Categories</MenuItem>
                {contentCategories.map((cat) => (
                  <MenuItem key={cat.value} value={cat.value}>
                    {cat.label}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          </Grid>
        </Grid>
      </TabPanel>

      <TabPanel value={tabValue} index={6}>
        {/* Collaboration Tab */}
        <Grid container spacing={3}>
          <Grid item xs={12}>
            <Typography variant="subtitle1" gutterBottom>
              Collaboration Features
            </Typography>
          </Grid>
          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableDuetStitch ?? true}
                  onChange={() => handleFeatureToggle('enableDuetStitch')}
                />
              }
              label="Duet & Stitch"
            />
            <Typography variant="caption" display="block" sx={{ ml: 4 }}>
              Create duets and stitches with other videos
            </Typography>
          </Grid>
          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableCollaboration ?? true}
                  onChange={() => handleFeatureToggle('enableCollaboration')}
                />
              }
              label="Collaboration Tools"
            />
          </Grid>
          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableLiveStreaming ?? true}
                  onChange={() => handleFeatureToggle('enableLiveStreaming')}
                />
              }
              label="Live Streaming"
            />
            <Typography variant="caption" display="block" sx={{ ml: 4 }}>
              Go live and interact with viewers
            </Typography>
          </Grid>
          <Grid item xs={12}>
            <Typography variant="subtitle2" gutterBottom>
              Collaboration Types
            </Typography>
            <FormGroup row>
              <FormControlLabel control={<Switch defaultChecked />} label="Duet" />
              <FormControlLabel control={<Switch defaultChecked />} label="Stitch" />
              <FormControlLabel control={<Switch defaultChecked />} label="React" />
              <FormControlLabel control={<Switch defaultChecked />} label="Collab" />
              <FormControlLabel control={<Switch defaultChecked />} label="Response" />
            </FormGroup>
          </Grid>
          <Grid item xs={12}>
            <Typography variant="subtitle2" gutterBottom>
              Live Streaming Settings
            </Typography>
            <FormGroup row>
              <FormControlLabel control={<Switch defaultChecked />} label="Allow Comments" />
              <FormControlLabel control={<Switch defaultChecked />} label="Allow Gifts" />
              <FormControlLabel control={<Switch defaultChecked />} label="Allow Co-hosts" />
              <FormControlLabel control={<Switch defaultChecked />} label="Save Replay" />
            </FormGroup>
          </Grid>
        </Grid>
      </TabPanel>
    </Box>
  );
};

export default TikTokContentApiConfiguration;