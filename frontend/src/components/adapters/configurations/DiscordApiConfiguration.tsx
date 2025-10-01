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
  Forum,
  VolumeUp,
  Settings,
  Security,
  People,
  EmojiEmotions,
  Event,
  Code,
  Webhook,
  AutoAwesome,
  Badge,
  Notifications,
  RecordVoiceOver,
  Games,
} from '@mui/icons-material';

interface DiscordApiConfiguration {
  clientId: string;
  clientSecret: string;
  botToken: string;
  publicKey: string;
  applicationId: string;
  guildId: string;
  features: {
    enableGuildManagement: boolean;
    enableChannelOperations: boolean;
    enableMessageManagement: boolean;
    enableVoiceSupport: boolean;
    enableSlashCommands: boolean;
    enableWebhooks: boolean;
    enableRoleManagement: boolean;
    enableMemberManagement: boolean;
    enableEmojiManagement: boolean;
    enableEventManagement: boolean;
    enableThreadSupport: boolean;
    enableStageChannels: boolean;
    enableAutoModeration: boolean;
    enableInteractions: boolean;
    enableEmbeds: boolean;
    enableReactions: boolean;
    enableDirectMessages: boolean;
    enableFileUploads: boolean;
    enableVoiceRecording: boolean;
    enableStreamNotifications: boolean;
  };
  gatewayIntents: string[];
  permissions: string[];
  eventSubscriptions: string[];
  webhookEndpoints: Array<{ url: string; events: string[] }>;
  autoModRules: Array<{ name: string; trigger: string; action: string }>;
  slashCommands: Array<{ name: string; description: string; options: any[] }>;
}

interface DiscordApiConfigurationProps {
  configuration: DiscordApiConfiguration;
  onChange: (config: DiscordApiConfiguration) => void;
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
      id={`discord-tabpanel-${index}`}
      aria-labelledby={`discord-tab-${index}`}
      {...other}
    >
      {value === index && <Box sx={{ p: 3 }}>{children}</Box>}
    </div>
  );
};

export const DiscordApiConfiguration: React.FC<DiscordApiConfigurationProps> = ({
  configuration,
  onChange,
}) => {
  const [tabValue, setTabValue] = useState(0);
  const [newWebhookUrl, setNewWebhookUrl] = useState('');
  const [newWebhookEvents, setNewWebhookEvents] = useState<string[]>([]);

  const handleTabChange = (event: React.SyntheticEvent, newValue: number) => {
    setTabValue(newValue);
  };

  const updateConfig = (updates: Partial<DiscordApiConfiguration>) => {
    onChange({ ...configuration, ...updates });
  };

  const updateFeature = (feature: keyof DiscordApiConfiguration['features'], value: boolean) => {
    updateConfig({
      features: { ...configuration.features, [feature]: value },
    });
  };

  const updateStringArray = (field: keyof DiscordApiConfiguration, values: string[]) => {
    updateConfig({ [field]: values });
  };

  const gatewayIntentOptions = [
    'GUILDS',
    'GUILD_MEMBERS',
    'GUILD_BANS',
    'GUILD_EMOJIS',
    'GUILD_INTEGRATIONS',
    'GUILD_WEBHOOKS',
    'GUILD_INVITES',
    'GUILD_VOICE_STATES',
    'GUILD_PRESENCES',
    'GUILD_MESSAGES',
    'GUILD_MESSAGE_REACTIONS',
    'GUILD_MESSAGE_TYPING',
    'DIRECT_MESSAGES',
    'DIRECT_MESSAGE_REACTIONS',
    'DIRECT_MESSAGE_TYPING',
    'MESSAGE_CONTENT',
    'GUILD_SCHEDULED_EVENTS',
    'AUTO_MODERATION_CONFIGURATION',
    'AUTO_MODERATION_EXECUTION',
  ];

  const permissionOptions = [
    'CREATE_INSTANT_INVITE',
    'KICK_MEMBERS',
    'BAN_MEMBERS',
    'ADMINISTRATOR',
    'MANAGE_CHANNELS',
    'MANAGE_GUILD',
    'ADD_REACTIONS',
    'VIEW_AUDIT_LOG',
    'PRIORITY_SPEAKER',
    'STREAM',
    'VIEW_CHANNEL',
    'SEND_MESSAGES',
    'SEND_TTS_MESSAGES',
    'MANAGE_MESSAGES',
    'EMBED_LINKS',
    'ATTACH_FILES',
    'READ_MESSAGE_HISTORY',
    'MENTION_EVERYONE',
    'USE_EXTERNAL_EMOJIS',
    'VIEW_GUILD_INSIGHTS',
    'CONNECT',
    'SPEAK',
    'MUTE_MEMBERS',
    'DEAFEN_MEMBERS',
    'MOVE_MEMBERS',
    'USE_VAD',
    'CHANGE_NICKNAME',
    'MANAGE_NICKNAMES',
    'MANAGE_ROLES',
    'MANAGE_WEBHOOKS',
    'MANAGE_EMOJIS_AND_STICKERS',
    'USE_APPLICATION_COMMANDS',
    'REQUEST_TO_SPEAK',
    'MANAGE_EVENTS',
    'MANAGE_THREADS',
    'CREATE_PUBLIC_THREADS',
    'CREATE_PRIVATE_THREADS',
    'USE_EXTERNAL_STICKERS',
    'SEND_MESSAGES_IN_THREADS',
    'USE_EMBEDDED_ACTIVITIES',
    'MODERATE_MEMBERS',
  ];

  const eventOptions = [
    'MESSAGE_CREATE',
    'MESSAGE_UPDATE',
    'MESSAGE_DELETE',
    'CHANNEL_CREATE',
    'CHANNEL_UPDATE',
    'CHANNEL_DELETE',
    'GUILD_UPDATE',
    'GUILD_MEMBER_ADD',
    'GUILD_MEMBER_REMOVE',
    'GUILD_MEMBER_UPDATE',
    'GUILD_ROLE_CREATE',
    'GUILD_ROLE_UPDATE',
    'GUILD_ROLE_DELETE',
    'VOICE_STATE_UPDATE',
    'PRESENCE_UPDATE',
    'INTERACTION_CREATE',
    'THREAD_CREATE',
    'THREAD_UPDATE',
    'THREAD_DELETE',
    'GUILD_SCHEDULED_EVENT_CREATE',
    'GUILD_SCHEDULED_EVENT_UPDATE',
    'GUILD_SCHEDULED_EVENT_DELETE',
  ];

  const addWebhookEndpoint = () => {
    if (newWebhookUrl && newWebhookEvents.length > 0) {
      const newEndpoints = [
        ...(configuration.webhookEndpoints || []),
        { url: newWebhookUrl, events: newWebhookEvents },
      ];
      updateConfig({ webhookEndpoints: newEndpoints });
      setNewWebhookUrl('');
      setNewWebhookEvents([]);
    }
  };

  const removeWebhookEndpoint = (index: number) => {
    const newEndpoints = configuration.webhookEndpoints?.filter((_, i) => i !== index) || [];
    updateConfig({ webhookEndpoints: newEndpoints });
  };

  return (
    <Box>
      <Paper sx={{ width: '100%' }}>
        <Tabs
          value={tabValue}
          onChange={handleTabChange}
          aria-label="discord configuration tabs"
          variant="scrollable"
          scrollButtons="auto"
        >
          <Tab label="Basic Settings" />
          <Tab label="Features" />
          <Tab label="Permissions & Intents" />
          <Tab label="Messages & Channels" />
          <Tab label="Voice & Stage" />
          <Tab label="Webhooks & Events" />
          <Tab label="Commands & Interactions" />
          <Tab label="Auto-Moderation" />
        </Tabs>

        <TabPanel value={tabValue} index={0}>
          <Grid container spacing={3}>
            <Grid item xs={12}>
              <Alert severity="info">
                Configure your Discord bot credentials and application settings.
              </Alert>
            </Grid>

            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label="Client ID"
                value={configuration.clientId || ''}
                onChange={(e) => updateConfig({ clientId: e.target.value })}
                helperText="Your Discord application client ID"
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
                helperText="Your Discord application client secret"
                required
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label="Bot Token"
                type="password"
                value={configuration.botToken || ''}
                onChange={(e) => updateConfig({ botToken: e.target.value })}
                helperText="Your Discord bot token"
                required
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label="Public Key"
                value={configuration.publicKey || ''}
                onChange={(e) => updateConfig({ publicKey: e.target.value })}
                helperText="For webhook signature verification"
                required
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label="Application ID"
                value={configuration.applicationId || ''}
                onChange={(e) => updateConfig({ applicationId: e.target.value })}
                helperText="Your Discord application ID"
                required
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label="Guild ID (Optional)"
                value={configuration.guildId || ''}
                onChange={(e) => updateConfig({ guildId: e.target.value })}
                helperText="Specific guild/server to operate in"
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
                Control which Discord features are available in your integration.
              </Typography>
            </Grid>

            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableGuildManagement || false}
                    onChange={(e) => updateFeature('enableGuildManagement', e.target.checked)}
                  />
                }
                label={
                  <Box display="flex" alignItems="center">
                    <People sx={{ mr: 1 }} />
                    Guild Management
                  </Box>
                }
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableChannelOperations || false}
                    onChange={(e) => updateFeature('enableChannelOperations', e.target.checked)}
                  />
                }
                label={
                  <Box display="flex" alignItems="center">
                    <Forum sx={{ mr: 1 }} />
                    Channel Operations
                  </Box>
                }
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableMessageManagement || false}
                    onChange={(e) => updateFeature('enableMessageManagement', e.target.checked)}
                  />
                }
                label="Message Management"
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableVoiceSupport || false}
                    onChange={(e) => updateFeature('enableVoiceSupport', e.target.checked)}
                  />
                }
                label={
                  <Box display="flex" alignItems="center">
                    <VolumeUp sx={{ mr: 1 }} />
                    Voice Support
                  </Box>
                }
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableSlashCommands || false}
                    onChange={(e) => updateFeature('enableSlashCommands', e.target.checked)}
                  />
                }
                label={
                  <Box display="flex" alignItems="center">
                    <Code sx={{ mr: 1 }} />
                    Slash Commands
                  </Box>
                }
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableWebhooks || false}
                    onChange={(e) => updateFeature('enableWebhooks', e.target.checked)}
                  />
                }
                label={
                  <Box display="flex" alignItems="center">
                    <Webhook sx={{ mr: 1 }} />
                    Webhooks
                  </Box>
                }
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableRoleManagement || false}
                    onChange={(e) => updateFeature('enableRoleManagement', e.target.checked)}
                  />
                }
                label={
                  <Box display="flex" alignItems="center">
                    <Badge sx={{ mr: 1 }} />
                    Role Management
                  </Box>
                }
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableMemberManagement || false}
                    onChange={(e) => updateFeature('enableMemberManagement', e.target.checked)}
                  />
                }
                label="Member Management"
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableEmojiManagement || false}
                    onChange={(e) => updateFeature('enableEmojiManagement', e.target.checked)}
                  />
                }
                label={
                  <Box display="flex" alignItems="center">
                    <EmojiEmotions sx={{ mr: 1 }} />
                    Emoji Management
                  </Box>
                }
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableEventManagement || false}
                    onChange={(e) => updateFeature('enableEventManagement', e.target.checked)}
                  />
                }
                label={
                  <Box display="flex" alignItems="center">
                    <Event sx={{ mr: 1 }} />
                    Event Management
                  </Box>
                }
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableThreadSupport || false}
                    onChange={(e) => updateFeature('enableThreadSupport', e.target.checked)}
                  />
                }
                label="Thread Support"
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableAutoModeration || false}
                    onChange={(e) => updateFeature('enableAutoModeration', e.target.checked)}
                  />
                }
                label={
                  <Box display="flex" alignItems="center">
                    <Security sx={{ mr: 1 }} />
                    Auto-Moderation
                  </Box>
                }
              />
            </Grid>
          </Grid>
        </TabPanel>

        <TabPanel value={tabValue} index={2}>
          <Grid container spacing={3}>
            <Grid item xs={12}>
              <Typography variant="h6" gutterBottom>
                Gateway Intents
              </Typography>
              <Typography variant="body2" color="text.secondary" gutterBottom>
                Select the events your bot needs to receive from Discord.
              </Typography>
            </Grid>

            <Grid item xs={12}>
              <Autocomplete
                multiple
                options={gatewayIntentOptions}
                value={configuration.gatewayIntents || []}
                onChange={(_, values) => updateStringArray('gatewayIntents', values)}
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
                    label="Gateway Intents"
                    helperText="Select gateway intents for your bot"
                  />
                )}
              />
            </Grid>

            <Grid item xs={12}>
              <Alert severity="warning">
                <Typography variant="body2">
                  Some intents like MESSAGE_CONTENT and GUILD_PRESENCES are privileged and 
                  require verification for bots in 100+ servers.
                </Typography>
              </Alert>
            </Grid>

            <Grid item xs={12}>
              <Typography variant="h6" gutterBottom sx={{ mt: 2 }}>
                Bot Permissions
              </Typography>
              <Typography variant="body2" color="text.secondary" gutterBottom>
                Select the permissions your bot needs in servers.
              </Typography>
            </Grid>

            <Grid item xs={12}>
              <Autocomplete
                multiple
                options={permissionOptions}
                value={configuration.permissions || []}
                onChange={(_, values) => updateStringArray('permissions', values)}
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
                    label="Bot Permissions"
                    helperText="Select permissions for your bot"
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
                Message & Channel Settings
              </Typography>
            </Grid>

            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableEmbeds || false}
                    onChange={(e) => updateFeature('enableEmbeds', e.target.checked)}
                  />
                }
                label="Rich Embeds"
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableReactions || false}
                    onChange={(e) => updateFeature('enableReactions', e.target.checked)}
                  />
                }
                label="Message Reactions"
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableDirectMessages || false}
                    onChange={(e) => updateFeature('enableDirectMessages', e.target.checked)}
                  />
                }
                label="Direct Messages"
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableFileUploads || false}
                    onChange={(e) => updateFeature('enableFileUploads', e.target.checked)}
                  />
                }
                label="File Uploads"
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableInteractions || false}
                    onChange={(e) => updateFeature('enableInteractions', e.target.checked)}
                  />
                }
                label="Interactive Components"
              />
            </Grid>

            <Grid item xs={12}>
              <Alert severity="info">
                <Typography variant="body2">
                  Message features include embeds, buttons, select menus, and other 
                  interactive components. File upload limits: 8MB (standard), 50MB (Nitro).
                </Typography>
              </Alert>
            </Grid>
          </Grid>
        </TabPanel>

        <TabPanel value={tabValue} index={4}>
          <Grid container spacing={3}>
            <Grid item xs={12}>
              <Typography variant="h6" gutterBottom>
                Voice & Stage Channel Settings
              </Typography>
            </Grid>

            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableVoiceSupport || false}
                    onChange={(e) => updateFeature('enableVoiceSupport', e.target.checked)}
                  />
                }
                label={
                  <Box display="flex" alignItems="center">
                    <VolumeUp sx={{ mr: 1 }} />
                    Voice Channels
                  </Box>
                }
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableStageChannels || false}
                    onChange={(e) => updateFeature('enableStageChannels', e.target.checked)}
                  />
                }
                label={
                  <Box display="flex" alignItems="center">
                    <RecordVoiceOver sx={{ mr: 1 }} />
                    Stage Channels
                  </Box>
                }
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableVoiceRecording || false}
                    onChange={(e) => updateFeature('enableVoiceRecording', e.target.checked)}
                  />
                }
                label="Voice Recording (Beta)"
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableStreamNotifications || false}
                    onChange={(e) => updateFeature('enableStreamNotifications', e.target.checked)}
                  />
                }
                label={
                  <Box display="flex" alignItems="center">
                    <Notifications sx={{ mr: 1 }} />
                    Stream Notifications
                  </Box>
                }
              />
            </Grid>

            <Grid item xs={12}>
              <Alert severity="warning">
                <Typography variant="body2">
                  Voice features require additional permissions and may have limitations 
                  based on server boost level. Voice recording is a beta feature.
                </Typography>
              </Alert>
            </Grid>
          </Grid>
        </TabPanel>

        <TabPanel value={tabValue} index={5}>
          <Grid container spacing={3}>
            <Grid item xs={12}>
              <Typography variant="h6" gutterBottom>
                Event Subscriptions
              </Typography>
              <Typography variant="body2" color="text.secondary" gutterBottom>
                Select which events to subscribe to.
              </Typography>
            </Grid>

            <Grid item xs={12}>
              <Autocomplete
                multiple
                options={eventOptions}
                value={configuration.eventSubscriptions || []}
                onChange={(_, values) => updateStringArray('eventSubscriptions', values)}
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
                    label="Event Subscriptions"
                    helperText="Select Discord events to receive"
                  />
                )}
              />
            </Grid>

            <Grid item xs={12}>
              <Typography variant="h6" gutterBottom sx={{ mt: 2 }}>
                Webhook Endpoints
              </Typography>
              <Typography variant="body2" color="text.secondary" gutterBottom>
                Configure webhook endpoints for specific events.
              </Typography>
            </Grid>

            <Grid item xs={12}>
              <Grid container spacing={2}>
                <Grid item xs={12} md={6}>
                  <TextField
                    fullWidth
                    label="Webhook URL"
                    value={newWebhookUrl}
                    onChange={(e) => setNewWebhookUrl(e.target.value)}
                    placeholder="https://your-server.com/webhook"
                  />
                </Grid>
                <Grid item xs={12} md={4}>
                  <Autocomplete
                    multiple
                    options={eventOptions}
                    value={newWebhookEvents}
                    onChange={(_, values) => setNewWebhookEvents(values)}
                    renderInput={(params) => (
                      <TextField {...params} label="Events" />
                    )}
                  />
                </Grid>
                <Grid item xs={12} md={2}>
                  <Button
                    fullWidth
                    variant="contained"
                    onClick={addWebhookEndpoint}
                    disabled={!newWebhookUrl || newWebhookEvents.length === 0}
                    sx={{ height: '56px' }}
                  >
                    Add
                  </Button>
                </Grid>
              </Grid>
            </Grid>

            <Grid item xs={12}>
              <List>
                {configuration.webhookEndpoints?.map((endpoint, index) => (
                  <ListItem key={index}>
                    <ListItemText
                      primary={endpoint.url}
                      secondary={`Events: ${endpoint.events.join(', ')}`}
                    />
                    <ListItemSecondaryAction>
                      <IconButton edge="end" onClick={() => removeWebhookEndpoint(index)}>
                        <Delete />
                      </IconButton>
                    </ListItemSecondaryAction>
                  </ListItem>
                ))}
              </List>
            </Grid>
          </Grid>
        </TabPanel>

        <TabPanel value={tabValue} index={6}>
          <Grid container spacing={3}>
            <Grid item xs={12}>
              <Typography variant="h6" gutterBottom>
                Slash Commands & Interactions
              </Typography>
            </Grid>

            <Grid item xs={12}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableSlashCommands || false}
                    onChange={(e) => updateFeature('enableSlashCommands', e.target.checked)}
                  />
                }
                label="Enable Slash Commands"
              />
            </Grid>

            <Grid item xs={12}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableInteractions || false}
                    onChange={(e) => updateFeature('enableInteractions', e.target.checked)}
                  />
                }
                label="Enable Interactive Components"
              />
            </Grid>

            <Grid item xs={12}>
              <Alert severity="info">
                <Typography variant="body2">
                  Slash commands and interactions allow users to interact with your bot 
                  using Discord's native UI components. Commands need to be registered 
                  with Discord before they can be used.
                </Typography>
              </Alert>
            </Grid>

            <Grid item xs={12}>
              <Typography variant="subtitle1" gutterBottom>
                Slash Command Examples
              </Typography>
              <List>
                <ListItem>
                  <ListItemText
                    primary="/ping"
                    secondary="Simple ping command to test bot responsiveness"
                  />
                </ListItem>
                <ListItem>
                  <ListItemText
                    primary="/userinfo [user]"
                    secondary="Get information about a user"
                  />
                </ListItem>
                <ListItem>
                  <ListItemText
                    primary="/poll [question] [options]"
                    secondary="Create a poll with multiple choices"
                  />
                </ListItem>
              </List>
            </Grid>
          </Grid>
        </TabPanel>

        <TabPanel value={tabValue} index={7}>
          <Grid container spacing={3}>
            <Grid item xs={12}>
              <Typography variant="h6" gutterBottom>
                Auto-Moderation Settings
              </Typography>
            </Grid>

            <Grid item xs={12}>
              <FormControlLabel
                control={
                  <Switch
                    checked={configuration.features?.enableAutoModeration || false}
                    onChange={(e) => updateFeature('enableAutoModeration', e.target.checked)}
                  />
                }
                label="Enable Auto-Moderation"
              />
            </Grid>

            <Grid item xs={12}>
              <Alert severity="info">
                <Typography variant="body2">
                  Auto-moderation allows you to automatically detect and handle rule 
                  violations in your Discord server. Configure rules for spam detection, 
                  harmful content filtering, and custom keyword blocking.
                </Typography>
              </Alert>
            </Grid>

            <Grid item xs={12}>
              <Typography variant="subtitle1" gutterBottom>
                Common Auto-Mod Rules
              </Typography>
              <List>
                <ListItem>
                  <ListItemText
                    primary="Spam Detection"
                    secondary="Detect and remove messages with repeated text or rapid posting"
                  />
                </ListItem>
                <ListItem>
                  <ListItemText
                    primary="Harmful Content Filter"
                    secondary="Block messages containing harmful or inappropriate content"
                  />
                </ListItem>
                <ListItem>
                  <ListItemText
                    primary="Mention Spam Prevention"
                    secondary="Limit excessive mentions in a single message"
                  />
                </ListItem>
                <ListItem>
                  <ListItemText
                    primary="Custom Keyword Blocking"
                    secondary="Block messages containing specified keywords or phrases"
                  />
                </ListItem>
              </List>
            </Grid>
          </Grid>
        </TabPanel>
      </Paper>
    </Box>
  );
};