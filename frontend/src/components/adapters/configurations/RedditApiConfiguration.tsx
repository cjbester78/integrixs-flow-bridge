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
  List,
  ListItem,
  ListItemText,
  ListItemSecondaryAction,
  IconButton,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
} from '@mui/material';
import { SelectChangeEvent } from '@mui/material/Select';
import AddIcon from '@mui/icons-material/Add';
import DeleteIcon from '@mui/icons-material/Delete';
import InfoIcon from '@mui/icons-material/Info';

interface RedditApiConfigurationProps {
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
      id={`reddit-tabpanel-${index}`}
      aria-labelledby={`reddit-tab-${index}`}
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

export const RedditApiConfiguration: React.FC<RedditApiConfigurationProps> = ({
  configuration,
  onChange,
}) => {
  const [tabValue, setTabValue] = useState(0);
  const [subredditDialog, setSubredditDialog] = useState(false);
  const [currentSubreddit, setCurrentSubreddit] = useState('');
  const [flairDialog, setFlairDialog] = useState(false);
  const [currentFlair, setCurrentFlair] = useState<any>({});

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

  // Subreddit Management
  const handleAddSubreddit = () => {
    if (currentSubreddit && !configuration.monitoredSubreddits?.includes(currentSubreddit)) {
      onChange({
        ...configuration,
        monitoredSubreddits: [...(configuration.monitoredSubreddits || []), currentSubreddit],
      });
      setCurrentSubreddit('');
      setSubredditDialog(false);
    }
  };

  const handleRemoveSubreddit = (subreddit: string) => {
    onChange({
      ...configuration,
      monitoredSubreddits: configuration.monitoredSubreddits?.filter((s: string) => s !== subreddit) || [],
    });
  };

  // Flair Management
  const handleAddFlair = () => {
    const newFlairs = [...(configuration.flairTemplates || []), currentFlair];
    onChange({
      ...configuration,
      flairTemplates: newFlairs,
    });
    setCurrentFlair({});
    setFlairDialog(false);
  };

  const handleRemoveFlair = (index: number) => {
    const newFlairs = [...configuration.flairTemplates];
    newFlairs.splice(index, 1);
    onChange({
      ...configuration,
      flairTemplates: newFlairs,
    });
  };

  return (
    <>
      <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
        <Tabs value={tabValue} onChange={handleTabChange} aria-label="Reddit API configuration tabs">
          <Tab label="Authentication" />
          <Tab label="Features" />
          <Tab label="Content Settings" />
          <Tab label="Monitoring" />
          <Tab label="Moderation" />
          <Tab label="Rate Limits" />
        </Tabs>
      </Box>

      <TabPanel value={tabValue} index={0}>
        <Grid container spacing={3}>
          <Grid item xs={12}>
            <Alert severity="info">
              Configure your Reddit App credentials. You'll need to create an app at reddit.com/prefs/apps
            </Alert>
          </Grid>

          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              label="Client ID"
              value={configuration.clientId || ''}
              onChange={handleChange('clientId')}
              helperText="Your Reddit App ID"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              label="Client Secret"
              type="password"
              value={configuration.clientSecret || ''}
              onChange={handleChange('clientSecret')}
              helperText="Your Reddit App Secret"
            />
          </Grid>

          <Grid item xs={12}>
            <TextField
              fullWidth
              label="User Agent"
              value={configuration.userAgent || ''}
              onChange={handleChange('userAgent')}
              helperText="Format: platform:app_id:version (by /u/username)"
              placeholder="web:myapp:v1.0.0 (by /u/yourusername)"
            />
          </Grid>

          <Grid item xs={12}>
            <FormControl fullWidth>
              <InputLabel>App Type</InputLabel>
              <Select
                value={configuration.appType || 'script'}
                onChange={handleSelectChange('appType')}
                label="App Type"
              >
                <MenuItem value="script">Script (personal use)</MenuItem>
                <MenuItem value="web">Web App</MenuItem>
                <MenuItem value="installed">Installed App</MenuItem>
              </Select>
            </FormControl>
          </Grid>

          {configuration.appType === 'script' && (
            <>
              <Grid item xs={12} md={6}>
                <TextField
                  fullWidth
                  label="Username"
                  value={configuration.username || ''}
                  onChange={handleChange('username')}
                  helperText="Reddit username (for script apps)"
                />
              </Grid>

              <Grid item xs={12} md={6}>
                <TextField
                  fullWidth
                  label="Password"
                  type="password"
                  value={configuration.password || ''}
                  onChange={handleChange('password')}
                  helperText="Reddit password (for script apps)"
                />
              </Grid>
            </>
          )}

          {configuration.appType !== 'script' && (
            <>
              <Grid item xs={12} md={6}>
                <TextField
                  fullWidth
                  label="Redirect URI"
                  value={configuration.redirectUri || ''}
                  onChange={handleChange('redirectUri')}
                  helperText="OAuth redirect URI"
                />
              </Grid>

              <Grid item xs={12} md={6}>
                <TextField
                  fullWidth
                  label="Refresh Token"
                  type="password"
                  value={configuration.refreshToken || ''}
                  onChange={handleChange('refreshToken')}
                  helperText="OAuth refresh token"
                />
              </Grid>
            </>
          )}
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
                  checked={configuration.features?.enablePostManagement || false}
                  onChange={handleSwitchChange('features', 'enablePostManagement')}
                />
              }
              label="Enable Post Management"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableCommentManagement || false}
                  onChange={handleSwitchChange('features', 'enableCommentManagement')}
                />
              }
              label="Enable Comment Management"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableSubredditMonitoring || false}
                  onChange={handleSwitchChange('features', 'enableSubredditMonitoring')}
                />
              }
              label="Enable Subreddit Monitoring"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableUserTracking || false}
                  onChange={handleSwitchChange('features', 'enableUserTracking')}
                />
              }
              label="Enable User Tracking"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableModeration || false}
                  onChange={handleSwitchChange('features', 'enableModeration')}
                />
              }
              label="Enable Moderation Tools"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableWikiManagement || false}
                  onChange={handleSwitchChange('features', 'enableWikiManagement')}
                />
              }
              label="Enable Wiki Management"
            />
          </Grid>

          <Grid item xs={12}>
            <Divider sx={{ my: 2 }} />
            <Typography variant="h6" gutterBottom>Social Features</Typography>
          </Grid>

          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enablePrivateMessages || false}
                  onChange={handleSwitchChange('features', 'enablePrivateMessages')}
                />
              }
              label="Enable Private Messages"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableVoting || false}
                  onChange={handleSwitchChange('features', 'enableVoting')}
                />
              }
              label="Enable Voting"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableAwards || false}
                  onChange={handleSwitchChange('features', 'enableAwards')}
                />
              }
              label="Enable Awards"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableSearch || false}
                  onChange={handleSwitchChange('features', 'enableSearch')}
                />
              }
              label="Enable Search"
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
                  checked={configuration.features?.enableFlairManagement || false}
                  onChange={handleSwitchChange('features', 'enableFlairManagement')}
                />
              }
              label="Enable Flair Management"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableMultireddit || false}
                  onChange={handleSwitchChange('features', 'enableMultireddit')}
                />
              }
              label="Enable Multireddit"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableLiveThreads || false}
                  onChange={handleSwitchChange('features', 'enableLiveThreads')}
                />
              }
              label="Enable Live Threads"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enablePolls || false}
                  onChange={handleSwitchChange('features', 'enablePolls')}
                />
              }
              label="Enable Polls"
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

          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableCrossposting || false}
                  onChange={handleSwitchChange('features', 'enableCrossposting')}
                />
              }
              label="Enable Crossposting"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableScheduledPosts || false}
                  onChange={handleSwitchChange('features', 'enableScheduledPosts')}
                />
              }
              label="Enable Scheduled Posts"
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
              label="Max Title Length"
              value={configuration.limits?.maxTitleLength || 300}
              onChange={handleNumberChange('limits', 'maxTitleLength')}
              helperText="Maximum characters in post title"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              type="number"
              label="Max Text Length"
              value={configuration.limits?.maxTextLength || 40000}
              onChange={handleNumberChange('limits', 'maxTextLength')}
              helperText="Maximum characters in text posts"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              type="number"
              label="Max Comment Length"
              value={configuration.limits?.maxCommentLength || 10000}
              onChange={handleNumberChange('limits', 'maxCommentLength')}
              helperText="Maximum characters in comments"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              type="number"
              label="Max Wiki Page Size (KB)"
              value={(configuration.limits?.maxWikiPageSize || 524288) / 1024}
              onChange={(e) => onChange({
                ...configuration,
                limits: {
                  ...configuration.limits,
                  maxWikiPageSize: parseInt(e.target.value) * 1024
                }
              })}
              helperText="Maximum wiki page size in kilobytes"
            />
          </Grid>

          <Grid item xs={12}>
            <Divider sx={{ my: 2 }} />
            <Typography variant="h6" gutterBottom>Media Limits</Typography>
          </Grid>

          <Grid item xs={12} md={4}>
            <Typography gutterBottom>Max Image Size (MB)</Typography>
            <Slider
              value={configuration.limits?.maxImageSizeMB || 20}
              onChange={handleSliderChange('limits', 'maxImageSizeMB')}
              aria-labelledby="image-size-slider"
              valueLabelDisplay="auto"
              min={1}
              max={100}
              step={1}
            />
          </Grid>

          <Grid item xs={12} md={4}>
            <Typography gutterBottom>Max Video Size (MB)</Typography>
            <Slider
              value={configuration.limits?.maxVideoSizeMB || 1024}
              onChange={handleSliderChange('limits', 'maxVideoSizeMB')}
              aria-labelledby="video-size-slider"
              valueLabelDisplay="auto"
              min={100}
              max={2048}
              step={100}
            />
          </Grid>

          <Grid item xs={12} md={4}>
            <Typography gutterBottom>Max GIF Size (MB)</Typography>
            <Slider
              value={configuration.limits?.maxGifSizeMB || 200}
              onChange={handleSliderChange('limits', 'maxGifSizeMB')}
              aria-labelledby="gif-size-slider"
              valueLabelDisplay="auto"
              min={10}
              max={500}
              step={10}
            />
          </Grid>

          <Grid item xs={12}>
            <Divider sx={{ my: 2 }} />
            <Typography variant="h6" gutterBottom>Other Limits</Typography>
          </Grid>

          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              type="number"
              label="Max Subreddits per Multi"
              value={configuration.limits?.maxSubredditsPerMulti || 100}
              onChange={handleNumberChange('limits', 'maxSubredditsPerMulti')}
              helperText="Maximum subreddits in a multireddit"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              type="number"
              label="Max Poll Options"
              value={configuration.limits?.maxPollOptions || 6}
              onChange={handleNumberChange('limits', 'maxPollOptions')}
              helperText="Maximum options in a poll"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              type="number"
              label="Max Poll Duration (days)"
              value={configuration.limits?.maxPollDurationDays || 7}
              onChange={handleNumberChange('limits', 'maxPollDurationDays')}
              helperText="Maximum poll duration"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              type="number"
              label="Max Search Results"
              value={configuration.limits?.maxSearchResults || 1000}
              onChange={handleNumberChange('limits', 'maxSearchResults')}
              helperText="Maximum search results returned"
            />
          </Grid>
        </Grid>
      </TabPanel>

      <TabPanel value={tabValue} index={3}>
        <Grid container spacing={3}>
          <Grid item xs={12}>
            <Typography variant="h6" gutterBottom>Monitoring Configuration</Typography>
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
              label="Posts Polling Interval (minutes)"
              value={configuration.postsPollingInterval || 5}
              onChange={(e) => onChange({ ...configuration, postsPollingInterval: parseInt(e.target.value) })}
              helperText="How often to check for new posts"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              type="number"
              label="Comments Polling Interval (minutes)"
              value={configuration.commentsPollingInterval || 10}
              onChange={(e) => onChange({ ...configuration, commentsPollingInterval: parseInt(e.target.value) })}
              helperText="How often to check for new comments"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              type="number"
              label="Messages Polling Interval (minutes)"
              value={configuration.messagesPollingInterval || 5}
              onChange={(e) => onChange({ ...configuration, messagesPollingInterval: parseInt(e.target.value) })}
              helperText="How often to check messages/inbox"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              type="number"
              label="Mod Queue Polling Interval (minutes)"
              value={configuration.modQueuePollingInterval || 3}
              onChange={(e) => onChange({ ...configuration, modQueuePollingInterval: parseInt(e.target.value) })}
              helperText="How often to check moderation queue"
            />
          </Grid>

          <Grid item xs={12}>
            <Divider sx={{ my: 2 }} />
            <Typography variant="h6" gutterBottom>Monitored Subreddits</Typography>
            <Paper variant="outlined" sx={{ p: 2 }}>
              <List>
                {configuration.monitoredSubreddits?.map((subreddit: string, index: number) => (
                  <ListItem key={index}>
                    <ListItemText primary={`r/${subreddit}`} />
                    <ListItemSecondaryAction>
                      <IconButton edge="end" onClick={() => handleRemoveSubreddit(subreddit)}>
                        <DeleteIcon />
                      </IconButton>
                    </ListItemSecondaryAction>
                  </ListItem>
                ))}
              </List>
              <Button
                variant="outlined"
                startIcon={<AddIcon />}
                onClick={() => setSubredditDialog(true)}
              >
                Add Subreddit
              </Button>
            </Paper>
          </Grid>

          <Grid item xs={12}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.pollSubscribedSubreddits || false}
                  onChange={(e) => onChange({ ...configuration, pollSubscribedSubreddits: e.target.checked })}
                />
              }
              label="Also monitor subscribed subreddits"
            />
          </Grid>

          <Grid item xs={12}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.pollUserComments || false}
                  onChange={(e) => onChange({ ...configuration, pollUserComments: e.target.checked })}
                />
              }
              label="Monitor comments on user's posts"
            />
          </Grid>

          <Grid item xs={12}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.pollCommentReplies || false}
                  onChange={(e) => onChange({ ...configuration, pollCommentReplies: e.target.checked })}
                />
              }
              label="Monitor replies to user's comments"
            />
          </Grid>
        </Grid>
      </TabPanel>

      <TabPanel value={tabValue} index={4}>
        <Grid container spacing={3}>
          <Grid item xs={12}>
            <Typography variant="h6" gutterBottom>Moderation Settings</Typography>
            <Alert severity="warning" sx={{ mb: 2 }}>
              Moderation features require moderator permissions in the respective subreddits.
            </Alert>
          </Grid>

          <Grid item xs={12}>
            <Typography variant="subtitle1" gutterBottom>Moderated Subreddits</Typography>
            <TextField
              fullWidth
              multiline
              rows={3}
              value={configuration.moderatedSubreddits?.join('\n') || ''}
              onChange={(e) => onChange({
                ...configuration,
                moderatedSubreddits: e.target.value.split('\n').filter(s => s.trim())
              })}
              helperText="Enter one subreddit per line (without r/)"
              placeholder="AskReddit&#10;pics&#10;funny"
            />
          </Grid>

          <Grid item xs={12}>
            <Divider sx={{ my: 2 }} />
            <Typography variant="h6" gutterBottom>Auto-Moderation</Typography>
          </Grid>

          <Grid item xs={12}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.autoApprove || false}
                  onChange={(e) => onChange({ ...configuration, autoApprove: e.target.checked })}
                />
              }
              label="Auto-approve posts from trusted users"
            />
          </Grid>

          <Grid item xs={12}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.autoRemoveSpam || false}
                  onChange={(e) => onChange({ ...configuration, autoRemoveSpam: e.target.checked })}
                />
              }
              label="Auto-remove spam (high confidence)"
            />
          </Grid>

          <Grid item xs={12}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.autoFlair || false}
                  onChange={(e) => onChange({ ...configuration, autoFlair: e.target.checked })}
                />
              }
              label="Auto-assign flair based on keywords"
            />
          </Grid>

          <Grid item xs={12}>
            <Divider sx={{ my: 2 }} />
            <Typography variant="h6" gutterBottom>Flair Templates</Typography>
            <Paper variant="outlined" sx={{ p: 2 }}>
              <List>
                {configuration.flairTemplates?.map((flair: any, index: number) => (
                  <ListItem key={index}>
                    <ListItemText
                      primary={flair.text}
                      secondary={`CSS: ${flair.cssClass || 'none'} | Editable: ${flair.editable ? 'Yes' : 'No'}`}
                    />
                    <ListItemSecondaryAction>
                      <IconButton edge="end" onClick={() => handleRemoveFlair(index)}>
                        <DeleteIcon />
                      </IconButton>
                    </ListItemSecondaryAction>
                  </ListItem>
                ))}
              </List>
              <Button
                variant="outlined"
                startIcon={<AddIcon />}
                onClick={() => setFlairDialog(true)}
              >
                Add Flair Template
              </Button>
            </Paper>
          </Grid>

          <Grid item xs={12}>
            <Typography variant="subtitle1" gutterBottom>Moderation Actions</Typography>
            {[
              'Approve/Remove Posts',
              'Distinguish Comments',
              'Sticky Posts',
              'Lock Threads',
              'Ban/Unban Users',
              'Mute/Unmute Users',
              'Manage Moderators',
              'Edit Wiki'
            ].map((action) => (
              <Chip
                key={action}
                label={action}
                sx={{ mr: 1, mb: 1 }}
                color="primary"
                variant="outlined"
              />
            ))}
          </Grid>
        </Grid>
      </TabPanel>

      <TabPanel value={tabValue} index={5}>
        <Grid container spacing={3}>
          <Grid item xs={12}>
            <Typography variant="h6" gutterBottom>API Rate Limits</Typography>
            <Alert severity="info" icon={<InfoIcon />} sx={{ mb: 2 }}>
              Reddit has different rate limits for OAuth and non-OAuth requests. OAuth typically allows 600 requests per 10 minutes.
            </Alert>
          </Grid>

          <Grid item xs={12} md={6}>
            <Typography gutterBottom>Standard Rate Limit (per minute)</Typography>
            <Slider
              value={configuration.limits?.rateLimitPerMinute || 60}
              onChange={handleSliderChange('limits', 'rateLimitPerMinute')}
              aria-labelledby="rate-limit-slider"
              valueLabelDisplay="auto"
              min={10}
              max={100}
              step={5}
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <Typography gutterBottom>OAuth Rate Limit (per minute)</Typography>
            <Slider
              value={configuration.limits?.oauthRateLimitPerMinute || 600}
              onChange={handleSliderChange('limits', 'oauthRateLimitPerMinute')}
              aria-labelledby="oauth-rate-limit-slider"
              valueLabelDisplay="auto"
              min={100}
              max={1000}
              step={50}
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
              value={configuration.retryDelay || 10}
              onChange={(e) => onChange({ ...configuration, retryDelay: parseInt(e.target.value) })}
              helperText="Delay between retry attempts"
            />
          </Grid>

          <Grid item xs={12}>
            <Divider sx={{ my: 2 }} />
            <Typography variant="h6" gutterBottom>Request Optimization</Typography>
          </Grid>

          <Grid item xs={12}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.enableRequestCaching || false}
                  onChange={(e) => onChange({ ...configuration, enableRequestCaching: e.target.checked })}
                />
              }
              label="Enable Request Caching"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              type="number"
              label="Cache TTL (minutes)"
              value={configuration.cacheTTL || 5}
              onChange={(e) => onChange({ ...configuration, cacheTTL: parseInt(e.target.value) })}
              helperText="Cache time-to-live in minutes"
              disabled={!configuration.enableRequestCaching}
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              type="number"
              label="Max Items per Request"
              value={configuration.limits?.maxListingItems || 100}
              onChange={handleNumberChange('limits', 'maxListingItems')}
              helperText="Maximum items per listing request"
            />
          </Grid>

          <Grid item xs={12}>
            <Alert severity="warning">
              Reddit strictly enforces rate limits. Exceeding them may result in temporary API bans.
            </Alert>
          </Grid>
        </Grid>
      </TabPanel>

      {/* Subreddit Dialog */}
      <Dialog open={subredditDialog} onClose={() => setSubredditDialog(false)}>
        <DialogTitle>Add Monitored Subreddit</DialogTitle>
        <DialogContent>
          <TextField
            fullWidth
            label="Subreddit Name"
            value={currentSubreddit}
            onChange={(e) => setCurrentSubreddit(e.target.value)}
            placeholder="Enter subreddit name (without r/)"
            sx={{ mt: 2 }}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setSubredditDialog(false)}>Cancel</Button>
          <Button onClick={handleAddSubreddit} variant="contained">Add</Button>
        </DialogActions>
      </Dialog>

      {/* Flair Dialog */}
      <Dialog open={flairDialog} onClose={() => setFlairDialog(false)}>
        <DialogTitle>Add Flair Template</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Flair Text"
                value={currentFlair.text || ''}
                onChange={(e) => setCurrentFlair({ ...currentFlair, text: e.target.value })}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="CSS Class"
                value={currentFlair.cssClass || ''}
                onChange={(e) => setCurrentFlair({ ...currentFlair, cssClass: e.target.value })}
              />
            </Grid>
            <Grid item xs={12}>
              <FormControlLabel
                control={
                  <Switch
                    checked={currentFlair.editable || false}
                    onChange={(e) => setCurrentFlair({ ...currentFlair, editable: e.target.checked })}
                  />
                }
                label="User Editable"
              />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setFlairDialog(false)}>Cancel</Button>
          <Button onClick={handleAddFlair} variant="contained">Add</Button>
        </DialogActions>
      </Dialog>
    </>
  );
};