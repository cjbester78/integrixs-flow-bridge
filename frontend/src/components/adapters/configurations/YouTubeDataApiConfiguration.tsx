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
  FormHelperText,
  Slider
} from '@mui/material';
import {
  YouTube,
  VideoLibrary,
  PlaylistPlay,
  Comment,
  LiveTv,
  Analytics,
  Settings,
  Subscriptions,
  Add,
  Delete,
  CloudUpload,
  AttachMoney,
  Groups,
  Subtitles,
  PhotoCamera,
  ViewStream
} from '@mui/icons-material';

interface YouTubeDataApiConfigurationProps {
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

const videoCategories = [
  { value: '1', label: 'Film & Animation' },
  { value: '2', label: 'Autos & Vehicles' },
  { value: '10', label: 'Music' },
  { value: '15', label: 'Pets & Animals' },
  { value: '17', label: 'Sports' },
  { value: '19', label: 'Travel & Events' },
  { value: '20', label: 'Gaming' },
  { value: '22', label: 'People & Blogs' },
  { value: '23', label: 'Comedy' },
  { value: '24', label: 'Entertainment' },
  { value: '25', label: 'News & Politics' },
  { value: '26', label: 'Howto & Style' },
  { value: '27', label: 'Education' },
  { value: '28', label: 'Science & Technology' },
  { value: '29', label: 'Nonprofits & Activism' },
];

const privacyStatuses = [
  { value: 'private', label: 'Private - Only you can view' },
  { value: 'unlisted', label: 'Unlisted - Anyone with link' },
  { value: 'public', label: 'Public - Everyone can view' },
];

const licenses = [
  { value: 'youtube', label: 'Standard YouTube License' },
  { value: 'creativeCommon', label: 'Creative Commons - Attribution' },
];

const videoDefinitions = [
  { value: 'hd', label: 'High Definition (HD)' },
  { value: 'sd', label: 'Standard Definition (SD)' },
];

const liveStreamQualities = [
  { value: '240p', label: '240p - Low quality' },
  { value: '360p', label: '360p - Medium quality' },
  { value: '480p', label: '480p - Standard quality' },
  { value: '720p', label: '720p - HD quality' },
  { value: '1080p', label: '1080p - Full HD quality' },
  { value: '1440p', label: '1440p - 2K quality' },
  { value: '2160p', label: '2160p - 4K quality' },
];

const frameRates = [
  { value: '24fps', label: '24 FPS - Cinema' },
  { value: '25fps', label: '25 FPS - PAL' },
  { value: '30fps', label: '30 FPS - Standard' },
  { value: '50fps', label: '50 FPS - Smooth' },
  { value: '60fps', label: '60 FPS - Gaming' },
];

const sortOrders = [
  { value: 'date', label: 'Date - Newest first' },
  { value: 'rating', label: 'Rating - Highest rated' },
  { value: 'relevance', label: 'Relevance - Most relevant' },
  { value: 'title', label: 'Title - Alphabetical' },
  { value: 'viewCount', label: 'View Count - Most viewed' },
];

export const YouTubeDataApiConfiguration: React.FC<YouTubeDataApiConfigurationProps> = ({
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
            <Tab label="Video Settings" icon={<VideoLibrary />} iconPosition="start" />
            <Tab label="Channel Features" icon={<ViewStream />} iconPosition="start" />
            <Tab label="Live Streaming" icon={<LiveTv />} iconPosition="start" />
            <Tab label="Community" icon={<Groups />} iconPosition="start" />
            <Tab label="Monetization" icon={<AttachMoney />} iconPosition="start" />
            <Tab label="Advanced" icon={<Analytics />} iconPosition="start" />
          </Tabs>
        </Paper>

        <TabPanel value={activeTab} index={0}>
          <Grid container spacing={3}>
            <Grid item xs={12}>
              <Alert severity="info">
                Configure your YouTube Data API credentials to manage videos, playlists, comments, and more.
                You'll need to enable the YouTube Data API v3 in Google Cloud Console.
              </Alert>
            </Grid>
            
            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label="Client ID"
                value={configuration.clientId || ''}
                onChange={(e) => updateConfig('clientId', e.target.value)}
                error={!!errors.clientId}
                helperText={errors.clientId || 'Google OAuth2 Client ID'}
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
                helperText={errors.clientSecret || 'Google OAuth2 Client Secret'}
                required
              />
            </Grid>
            
            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label="Channel ID"
                value={configuration.channelId || ''}
                onChange={(e) => updateConfig('channelId', e.target.value)}
                helperText="Your YouTube channel ID (optional)"
              />
            </Grid>
            
            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label="Upload Playlist ID"
                value={configuration.uploadPlaylistId || ''}
                onChange={(e) => updateConfig('uploadPlaylistId', e.target.value)}
                helperText="Usually 'uploads' playlist ID"
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
                Core Features
              </Typography>
              <Divider sx={{ mb: 2 }} />
              <Grid container spacing={2}>
                <Grid item xs={12} md={6}>
                  <FormControlLabel
                    control={
                      <Switch
                        checked={configuration.features?.enableVideoUpload ?? true}
                        onChange={(e) => updateFeatures('enableVideoUpload', e.target.checked)}
                      />
                    }
                    label="Video Upload"
                  />
                  <FormHelperText>Upload videos to YouTube</FormHelperText>
                </Grid>
                
                <Grid item xs={12} md={6}>
                  <FormControlLabel
                    control={
                      <Switch
                        checked={configuration.features?.enableVideoManagement ?? true}
                        onChange={(e) => updateFeatures('enableVideoManagement', e.target.checked)}
                      />
                    }
                    label="Video Management"
                  />
                  <FormHelperText>Edit, delete, and manage videos</FormHelperText>
                </Grid>
                
                <Grid item xs={12} md={6}>
                  <FormControlLabel
                    control={
                      <Switch
                        checked={configuration.features?.enablePlaylistManagement ?? true}
                        onChange={(e) => updateFeatures('enablePlaylistManagement', e.target.checked)}
                      />
                    }
                    label="Playlist Management"
                  />
                  <FormHelperText>Create and manage playlists</FormHelperText>
                </Grid>
                
                <Grid item xs={12} md={6}>
                  <FormControlLabel
                    control={
                      <Switch
                        checked={configuration.features?.enableCommentManagement ?? true}
                        onChange={(e) => updateFeatures('enableCommentManagement', e.target.checked)}
                      />
                    }
                    label="Comment Management"
                  />
                  <FormHelperText>Moderate and respond to comments</FormHelperText>
                </Grid>
              </Grid>
            </Grid>
          </Grid>
        </TabPanel>

        <TabPanel value={activeTab} index={1}>
          <Grid container spacing={3}>
            <Grid item xs={12}>
              <Typography variant="h6" gutterBottom>
                Video Upload Settings
              </Typography>
              <Divider sx={{ mb: 2 }} />
            </Grid>
            
            <Grid item xs={12} md={6}>
              <FormControl fullWidth>
                <InputLabel>Default Privacy Status</InputLabel>
                <Select
                  value={configuration.defaultPrivacyStatus || 'private'}
                  onChange={(e) => updateConfig('defaultPrivacyStatus', e.target.value)}
                  label="Default Privacy Status"
                >
                  {privacyStatuses.map((status) => (
                    <MenuItem key={status.value} value={status.value}>
                      {status.label}
                    </MenuItem>
                  ))}
                </Select>
                <FormHelperText>Default privacy for new videos</FormHelperText>
              </FormControl>
            </Grid>
            
            <Grid item xs={12} md={6}>
              <FormControl fullWidth>
                <InputLabel>Default Category</InputLabel>
                <Select
                  value={configuration.defaultCategoryId || '22'}
                  onChange={(e) => updateConfig('defaultCategoryId', e.target.value)}
                  label="Default Category"
                >
                  {videoCategories.map((category) => (
                    <MenuItem key={category.value} value={category.value}>
                      {category.label}
                    </MenuItem>
                  ))}
                </Select>
                <FormHelperText>Default category for new videos</FormHelperText>
              </FormControl>
            </Grid>
            
            <Grid item xs={12} md={6}>
              <FormControl fullWidth>
                <InputLabel>Default License</InputLabel>
                <Select
                  value={configuration.defaultLicense || 'youtube'}
                  onChange={(e) => updateConfig('defaultLicense', e.target.value)}
                  label="Default License"
                >
                  {licenses.map((license) => (
                    <MenuItem key={license.value} value={license.value}>
                      {license.label}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>
            
            <Grid item xs={12} md={6}>
              <FormControl fullWidth>
                <InputLabel>Default Video Definition</InputLabel>
                <Select
                  value={configuration.defaultVideoDefinition || 'hd'}
                  onChange={(e) => updateConfig('defaultVideoDefinition', e.target.value)}
                  label="Default Video Definition"
                >
                  {videoDefinitions.map((def) => (
                    <MenuItem key={def.value} value={def.value}>
                      {def.label}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>
            
            <Grid item xs={12}>
              <Typography variant="subtitle1" gutterBottom sx={{ mt: 2 }}>
                Upload Features
              </Typography>
              <Grid container spacing={2}>
                <Grid item xs={12} md={6}>
                  <FormControlLabel
                    control={
                      <Switch
                        checked={configuration.features?.enableThumbnailUpload ?? true}
                        onChange={(e) => updateFeatures('enableThumbnailUpload', e.target.checked)}
                      />
                    }
                    label="Custom Thumbnails"
                  />
                  <FormHelperText>Upload custom video thumbnails</FormHelperText>
                </Grid>
                
                <Grid item xs={12} md={6}>
                  <FormControlLabel
                    control={
                      <Switch
                        checked={configuration.features?.enableCaptions ?? true}
                        onChange={(e) => updateFeatures('enableCaptions', e.target.checked)}
                      />
                    }
                    label="Captions/Subtitles"
                  />
                  <FormHelperText>Upload captions and subtitles</FormHelperText>
                </Grid>
                
                <Grid item xs={12} md={6}>
                  <FormControlLabel
                    control={
                      <Switch
                        checked={configuration.features?.enableVideoEditing ?? true}
                        onChange={(e) => updateFeatures('enableVideoEditing', e.target.checked)}
                      />
                    }
                    label="Video Editing"
                  />
                  <FormHelperText>Edit videos after upload</FormHelperText>
                </Grid>
                
                <Grid item xs={12} md={6}>
                  <FormControlLabel
                    control={
                      <Switch
                        checked={configuration.features?.enablePremiere ?? true}
                        onChange={(e) => updateFeatures('enablePremiere', e.target.checked)}
                      />
                    }
                    label="Premieres"
                  />
                  <FormHelperText>Schedule video premieres</FormHelperText>
                </Grid>
              </Grid>
            </Grid>
            
            <Grid item xs={12}>
              <Typography variant="subtitle1" gutterBottom sx={{ mt: 2 }}>
                Upload Limits
              </Typography>
            </Grid>
            
            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label="Max Video Size (GB)"
                type="number"
                value={configuration.limits?.maxVideoSize ? configuration.limits.maxVideoSize / (1024 * 1024 * 1024) : 128}
                onChange={(e) => updateLimits('maxVideoSize', parseInt(e.target.value) * 1024 * 1024 * 1024)}
                helperText="Maximum video file size in GB"
                InputProps={{
                  inputProps: { min: 1, max: 128 }
                }}
              />
            </Grid>
            
            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label="Daily Upload Limit"
                type="number"
                value={configuration.limits?.dailyUploadLimit || 50}
                onChange={(e) => updateLimits('dailyUploadLimit', parseInt(e.target.value))}
                helperText="Maximum videos per day"
                InputProps={{
                  inputProps: { min: 1, max: 100 }
                }}
              />
            </Grid>
            
            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label="Max Title Length"
                type="number"
                value={configuration.limits?.maxTitleLength || 100}
                onChange={(e) => updateLimits('maxTitleLength', parseInt(e.target.value))}
                helperText="Maximum characters in title"
                InputProps={{
                  inputProps: { min: 1, max: 100 }
                }}
              />
            </Grid>
            
            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label="Max Description Length"
                type="number"
                value={configuration.limits?.maxDescriptionLength || 5000}
                onChange={(e) => updateLimits('maxDescriptionLength', parseInt(e.target.value))}
                helperText="Maximum characters in description"
                InputProps={{
                  inputProps: { min: 1, max: 5000 }
                }}
              />
            </Grid>
          </Grid>
        </TabPanel>

        <TabPanel value={activeTab} index={2}>
          <Grid container spacing={3}>
            <Grid item xs={12}>
              <Typography variant="h6" gutterBottom>
                Channel Management Features
              </Typography>
              <Divider sx={{ mb: 2 }} />
            </Grid>
            
            <Grid item xs={12}>
              <Grid container spacing={2}>
                <Grid item xs={12} md={6}>
                  <FormControlLabel
                    control={
                      <Switch
                        checked={configuration.features?.enableChannelManagement ?? true}
                        onChange={(e) => updateFeatures('enableChannelManagement', e.target.checked)}
                      />
                    }
                    label="Channel Management"
                  />
                  <FormHelperText>Update channel info and settings</FormHelperText>
                </Grid>
                
                <Grid item xs={12} md={6}>
                  <FormControlLabel
                    control={
                      <Switch
                        checked={configuration.features?.enableSubscriberManagement ?? true}
                        onChange={(e) => updateFeatures('enableSubscriberManagement', e.target.checked)}
                      />
                    }
                    label="Subscriber Management"
                  />
                  <FormHelperText>Track and manage subscribers</FormHelperText>
                </Grid>
                
                <Grid item xs={12} md={6}>
                  <FormControlLabel
                    control={
                      <Switch
                        checked={configuration.features?.enableAnalytics ?? true}
                        onChange={(e) => updateFeatures('enableAnalytics', e.target.checked)}
                      />
                    }
                    label="Analytics"
                  />
                  <FormHelperText>View channel and video analytics</FormHelperText>
                </Grid>
                
                <Grid item xs={12} md={6}>
                  <FormControlLabel
                    control={
                      <Switch
                        checked={configuration.features?.enableContentId ?? false}
                        onChange={(e) => updateFeatures('enableContentId', e.target.checked)}
                      />
                    }
                    label="Content ID"
                  />
                  <FormHelperText>Copyright management system</FormHelperText>
                </Grid>
              </Grid>
            </Grid>
            
            <Grid item xs={12}>
              <Typography variant="subtitle1" gutterBottom sx={{ mt: 2 }}>
                Playlist Settings
              </Typography>
            </Grid>
            
            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label="Max Playlists per Channel"
                type="number"
                value={configuration.limits?.maxPlaylistsPerChannel || 200}
                onChange={(e) => updateLimits('maxPlaylistsPerChannel', parseInt(e.target.value))}
                helperText="Maximum number of playlists"
                InputProps={{
                  inputProps: { min: 1, max: 500 }
                }}
              />
            </Grid>
            
            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label="Max Items per Playlist"
                type="number"
                value={configuration.limits?.maxPlaylistItems || 5000}
                onChange={(e) => updateLimits('maxPlaylistItems', parseInt(e.target.value))}
                helperText="Maximum videos in a playlist"
                InputProps={{
                  inputProps: { min: 1, max: 5000 }
                }}
              />
            </Grid>
            
            <Grid item xs={12}>
              <Typography variant="subtitle1" gutterBottom sx={{ mt: 2 }}>
                Comment Settings
              </Typography>
            </Grid>
            
            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label="Max Comment Length"
                type="number"
                value={configuration.limits?.maxCommentLength || 10000}
                onChange={(e) => updateLimits('maxCommentLength', parseInt(e.target.value))}
                helperText="Maximum characters in comments"
                InputProps={{
                  inputProps: { min: 1, max: 10000 }
                }}
              />
            </Grid>
            
            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.autoModerateComments || false}
                    onChange={(e) => updateConfig('autoModerateComments', e.target.checked)}
                  />
                }
                label="Auto-moderate Comments"
              />
              <FormHelperText>Automatically hold potentially inappropriate comments</FormHelperText>
            </Grid>
          </Grid>
        </TabPanel>

        <TabPanel value={activeTab} index={3}>
          <Grid container spacing={3}>
            <Grid item xs={12}>
              <Typography variant="h6" gutterBottom>
                Live Streaming Configuration
              </Typography>
              <Divider sx={{ mb: 2 }} />
            </Grid>
            
            <Grid item xs={12}>
              <Alert severity="info">
                Live streaming requires your channel to be verified and have no active Community Guidelines strikes.
              </Alert>
            </Grid>
            
            <Grid item xs={12}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableLiveStreaming ?? true}
                    onChange={(e) => updateFeatures('enableLiveStreaming', e.target.checked)}
                  />
                }
                label="Enable Live Streaming"
              />
              <FormHelperText>Create and manage live broadcasts</FormHelperText>
            </Grid>
            
            <Grid item xs={12} md={6}>
              <FormControl fullWidth>
                <InputLabel>Default Stream Quality</InputLabel>
                <Select
                  value={configuration.defaultStreamQuality || '1080p'}
                  onChange={(e) => updateConfig('defaultStreamQuality', e.target.value)}
                  label="Default Stream Quality"
                >
                  {liveStreamQualities.map((quality) => (
                    <MenuItem key={quality.value} value={quality.value}>
                      {quality.label}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>
            
            <Grid item xs={12} md={6}>
              <FormControl fullWidth>
                <InputLabel>Default Frame Rate</InputLabel>
                <Select
                  value={configuration.defaultFrameRate || '30fps'}
                  onChange={(e) => updateConfig('defaultFrameRate', e.target.value)}
                  label="Default Frame Rate"
                >
                  {frameRates.map((rate) => (
                    <MenuItem key={rate.value} value={rate.value}>
                      {rate.label}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>
            
            <Grid item xs={12}>
              <Typography variant="subtitle1" gutterBottom sx={{ mt: 2 }}>
                Live Stream Features
              </Typography>
              <Grid container spacing={2}>
                <Grid item xs={12} md={6}>
                  <FormControlLabel
                    control={
                      <Switch
                        checked={configuration.enableDvr || true}
                        onChange={(e) => updateConfig('enableDvr', e.target.checked)}
                      />
                    }
                    label="DVR (Pause/Rewind)"
                  />
                  <FormHelperText>Allow viewers to pause and rewind</FormHelperText>
                </Grid>
                
                <Grid item xs={12} md={6}>
                  <FormControlLabel
                    control={
                      <Switch
                        checked={configuration.recordFromStart || true}
                        onChange={(e) => updateConfig('recordFromStart', e.target.checked)}
                      />
                    }
                    label="Record from Start"
                  />
                  <FormHelperText>Save live stream as video</FormHelperText>
                </Grid>
                
                <Grid item xs={12} md={6}>
                  <FormControlLabel
                    control={
                      <Switch
                        checked={configuration.enableEmbed || true}
                        onChange={(e) => updateConfig('enableEmbed', e.target.checked)}
                      />
                    }
                    label="Allow Embedding"
                  />
                  <FormHelperText>Allow stream embedding on other sites</FormHelperText>
                </Grid>
                
                <Grid item xs={12} md={6}>
                  <FormControlLabel
                    control={
                      <Switch
                        checked={configuration.enableAutoStart || false}
                        onChange={(e) => updateConfig('enableAutoStart', e.target.checked)}
                      />
                    }
                    label="Auto-start Streams"
                  />
                  <FormHelperText>Automatically go live when stream detected</FormHelperText>
                </Grid>
              </Grid>
            </Grid>
            
            <Grid item xs={12}>
              <Typography variant="subtitle1" gutterBottom sx={{ mt: 2 }}>
                Stream Settings
              </Typography>
            </Grid>
            
            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label="Stream Key Prefix"
                value={configuration.streamKeyPrefix || ''}
                onChange={(e) => updateConfig('streamKeyPrefix', e.target.value)}
                helperText="Optional prefix for stream keys"
              />
            </Grid>
            
            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label="Backup Stream URL"
                value={configuration.backupStreamUrl || ''}
                onChange={(e) => updateConfig('backupStreamUrl', e.target.value)}
                helperText="Backup RTMP URL for redundancy"
              />
            </Grid>
          </Grid>
        </TabPanel>

        <TabPanel value={activeTab} index={4}>
          <Grid container spacing={3}>
            <Grid item xs={12}>
              <Typography variant="h6" gutterBottom>
                Community Features
              </Typography>
              <Divider sx={{ mb: 2 }} />
            </Grid>
            
            <Grid item xs={12}>
              <Grid container spacing={2}>
                <Grid item xs={12} md={6}>
                  <FormControlLabel
                    control={
                      <Switch
                        checked={configuration.features?.enableCommunityPosts ?? true}
                        onChange={(e) => updateFeatures('enableCommunityPosts', e.target.checked)}
                      />
                    }
                    label="Community Posts"
                  />
                  <FormHelperText>Create community tab posts</FormHelperText>
                </Grid>
                
                <Grid item xs={12} md={6}>
                  <FormControlLabel
                    control={
                      <Switch
                        checked={configuration.features?.enableStories ?? true}
                        onChange={(e) => updateFeatures('enableStories', e.target.checked)}
                      />
                    }
                    label="YouTube Stories"
                  />
                  <FormHelperText>Create short-form stories</FormHelperText>
                </Grid>
                
                <Grid item xs={12} md={6}>
                  <FormControlLabel
                    control={
                      <Switch
                        checked={configuration.features?.enableShorts ?? true}
                        onChange={(e) => updateFeatures('enableShorts', e.target.checked)}
                      />
                    }
                    label="YouTube Shorts"
                  />
                  <FormHelperText>Upload vertical short videos</FormHelperText>
                </Grid>
                
                <Grid item xs={12} md={6}>
                  <FormControlLabel
                    control={
                      <Switch
                        checked={configuration.features?.enableMemberships ?? true}
                        onChange={(e) => updateFeatures('enableMemberships', e.target.checked)}
                      />
                    }
                    label="Channel Memberships"
                  />
                  <FormHelperText>Manage channel membership perks</FormHelperText>
                </Grid>
              </Grid>
            </Grid>
            
            <Grid item xs={12}>
              <Typography variant="subtitle1" gutterBottom sx={{ mt: 2 }}>
                Interaction Settings
              </Typography>
            </Grid>
            
            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.enableHeartComments || true}
                    onChange={(e) => updateConfig('enableHeartComments', e.target.checked)}
                  />
                }
                label="Heart Comments"
              />
              <FormHelperText>Show creator appreciation on comments</FormHelperText>
            </Grid>
            
            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.enablePinnedComments || true}
                    onChange={(e) => updateConfig('enablePinnedComments', e.target.checked)}
                  />
                }
                label="Pin Comments"
              />
              <FormHelperText>Pin important comments to top</FormHelperText>
            </Grid>
            
            <Grid item xs={12}>
              <Typography variant="subtitle1" gutterBottom sx={{ mt: 2 }}>
                Community Guidelines
              </Typography>
            </Grid>
            
            <Grid item xs={12}>
              <TextField
                fullWidth
                multiline
                rows={4}
                label="Community Guidelines Message"
                value={configuration.communityGuidelines || ''}
                onChange={(e) => updateConfig('communityGuidelines', e.target.value)}
                helperText="Message shown to users about community standards"
              />
            </Grid>
            
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Blocked Words"
                value={configuration.blockedWords || ''}
                onChange={(e) => updateConfig('blockedWords', e.target.value)}
                helperText="Comma-separated list of words to auto-block in comments"
              />
            </Grid>
          </Grid>
        </TabPanel>

        <TabPanel value={activeTab} index={5}>
          <Grid container spacing={3}>
            <Grid item xs={12}>
              <Typography variant="h6" gutterBottom>
                Monetization Settings
              </Typography>
              <Divider sx={{ mb: 2 }} />
            </Grid>
            
            <Grid item xs={12}>
              <Alert severity="warning">
                Monetization features require YouTube Partner Program membership and meeting eligibility requirements.
              </Alert>
            </Grid>
            
            <Grid item xs={12}>
              <Grid container spacing={2}>
                <Grid item xs={12} md={6}>
                  <FormControlLabel
                    control={
                      <Switch
                        checked={configuration.features?.enableMonetization ?? true}
                        onChange={(e) => updateFeatures('enableMonetization', e.target.checked)}
                      />
                    }
                    label="Ad Monetization"
                  />
                  <FormHelperText>Enable ads on videos</FormHelperText>
                </Grid>
                
                <Grid item xs={12} md={6}>
                  <FormControlLabel
                    control={
                      <Switch
                        checked={configuration.features?.enableSuperChat ?? true}
                        onChange={(e) => updateFeatures('enableSuperChat', e.target.checked)}
                      />
                    }
                    label="Super Chat & Super Stickers"
                  />
                  <FormHelperText>Live stream monetization features</FormHelperText>
                </Grid>
                
                <Grid item xs={12} md={6}>
                  <FormControlLabel
                    control={
                      <Switch
                        checked={configuration.features?.enableMerchandise ?? false}
                        onChange={(e) => updateFeatures('enableMerchandise', e.target.checked)}
                      />
                    }
                    label="Merchandise Shelf"
                  />
                  <FormHelperText>Display merchandise below videos</FormHelperText>
                </Grid>
                
                <Grid item xs={12} md={6}>
                  <FormControlLabel
                    control={
                      <Switch
                        checked={configuration.enableMidrollAds || false}
                        onChange={(e) => updateConfig('enableMidrollAds', e.target.checked)}
                      />
                    }
                    label="Mid-roll Ads"
                  />
                  <FormHelperText>Allow ads during video playback</FormHelperText>
                </Grid>
              </Grid>
            </Grid>
            
            <Grid item xs={12}>
              <Typography variant="subtitle1" gutterBottom sx={{ mt: 2 }}>
                Membership Settings
              </Typography>
            </Grid>
            
            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label="Membership Price Tiers"
                type="number"
                value={configuration.membershipTiers || 3}
                onChange={(e) => updateConfig('membershipTiers', parseInt(e.target.value))}
                helperText="Number of membership price tiers"
                InputProps={{
                  inputProps: { min: 1, max: 5 }
                }}
              />
            </Grid>
            
            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label="Membership Welcome Message"
                value={configuration.membershipWelcome || ''}
                onChange={(e) => updateConfig('membershipWelcome', e.target.value)}
                helperText="Message for new members"
              />
            </Grid>
            
            <Grid item xs={12}>
              <Typography variant="subtitle1" gutterBottom sx={{ mt: 2 }}>
                Revenue Tracking
              </Typography>
            </Grid>
            
            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.trackRevenue || false}
                    onChange={(e) => updateConfig('trackRevenue', e.target.checked)}
                  />
                }
                label="Track Revenue"
              />
              <FormHelperText>Monitor monetization performance</FormHelperText>
            </Grid>
            
            <Grid item xs={12} md={6}>
              <FormControl fullWidth>
                <InputLabel>Revenue Currency</InputLabel>
                <Select
                  value={configuration.revenueCurrency || 'USD'}
                  onChange={(e) => updateConfig('revenueCurrency', e.target.value)}
                  label="Revenue Currency"
                >
                  <MenuItem value="USD">USD - US Dollar</MenuItem>
                  <MenuItem value="EUR">EUR - Euro</MenuItem>
                  <MenuItem value="GBP">GBP - British Pound</MenuItem>
                  <MenuItem value="JPY">JPY - Japanese Yen</MenuItem>
                  <MenuItem value="CAD">CAD - Canadian Dollar</MenuItem>
                  <MenuItem value="AUD">AUD - Australian Dollar</MenuItem>
                </Select>
              </FormControl>
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
                API Rate Limits
              </Typography>
            </Grid>
            
            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label="Daily Quota Units"
                type="number"
                value={configuration.limits?.rateLimit || 10000}
                onChange={(e) => updateLimits('rateLimit', parseInt(e.target.value))}
                helperText="YouTube API quota units per day"
                InputProps={{
                  inputProps: { min: 1000, max: 1000000 }
                }}
              />
            </Grid>
            
            <Grid item xs={12} md={6}>
              <FormControl fullWidth>
                <InputLabel>Default Sort Order</InputLabel>
                <Select
                  value={configuration.defaultSortOrder || 'date'}
                  onChange={(e) => updateConfig('defaultSortOrder', e.target.value)}
                  label="Default Sort Order"
                >
                  {sortOrders.map((order) => (
                    <MenuItem key={order.value} value={order.value}>
                      {order.label}
                    </MenuItem>
                  ))}
                </Select>
                <FormHelperText>Default sort order for queries</FormHelperText>
              </FormControl>
            </Grid>
            
            <Grid item xs={12}>
              <Typography variant="subtitle1" gutterBottom sx={{ mt: 2 }}>
                Webhook Configuration
              </Typography>
            </Grid>
            
            <Grid item xs={12} md={8}>
              <TextField
                fullWidth
                label="Push Notification URL"
                value={configuration.pushNotificationUrl || ''}
                onChange={(e) => updateConfig('pushNotificationUrl', e.target.value)}
                helperText="URL to receive YouTube push notifications (PubSubHubbub)"
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
              <Typography variant="subtitle1" gutterBottom sx={{ mt: 2 }}>
                Content Restrictions
              </Typography>
            </Grid>
            
            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.madeForKids || false}
                    onChange={(e) => updateConfig('madeForKids', e.target.checked)}
                  />
                }
                label="Made for Kids"
              />
              <FormHelperText>Mark all content as made for kids (COPPA)</FormHelperText>
            </Grid>
            
            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.ageRestricted || false}
                    onChange={(e) => updateConfig('ageRestricted', e.target.checked)}
                  />
                }
                label="Age Restricted"
              />
              <FormHelperText>Default videos to age-restricted</FormHelperText>
            </Grid>
            
            <Grid item xs={12}>
              <Typography variant="subtitle1" gutterBottom sx={{ mt: 2 }}>
                Search Settings
              </Typography>
            </Grid>
            
            <Grid item xs={12} md={6}>
              <FormControl fullWidth>
                <InputLabel>Safe Search</InputLabel>
                <Select
                  value={configuration.safeSearch || 'moderate'}
                  onChange={(e) => updateConfig('safeSearch', e.target.value)}
                  label="Safe Search"
                >
                  <MenuItem value="none">None - No filtering</MenuItem>
                  <MenuItem value="moderate">Moderate - Some filtering</MenuItem>
                  <MenuItem value="strict">Strict - Maximum filtering</MenuItem>
                </Select>
                <FormHelperText>Content filtering level for searches</FormHelperText>
              </FormControl>
            </Grid>
            
            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label="Default Region Code"
                value={configuration.regionCode || 'US'}
                onChange={(e) => updateConfig('regionCode', e.target.value)}
                helperText="ISO 3166-1 alpha-2 country code"
                inputProps={{ maxLength: 2 }}
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