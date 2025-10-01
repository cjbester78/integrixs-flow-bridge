import React, { useState, useEffect, useCallback } from 'react';
import {
  Box,
  TextField,
  Switch,
  FormControlLabel,
  Button,
  Typography,
  Alert,
  Grid,
  Paper,
  Divider,
  IconButton,
  Tooltip,
  Accordion,
  AccordionSummary,
  AccordionDetails,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Chip,
  InputAdornment,
  Link,
  List,
  ListItem,
  ListItemText,
  ListItemIcon,
  FormGroup,
  Checkbox
} from '@mui/material';
import {
  ExpandMore as ExpandMoreIcon,
  Info as InfoIcon,
  Security as SecurityIcon,
  Message as MessageIcon,
  Groups as GroupsIcon,
  Folder as FolderIcon,
  Code as CodeIcon,
  SmartToy as BotIcon,
  Webhook as WebhookIcon,
  Schedule as ScheduleIcon,
  Speed as SpeedIcon,
  Extension as ExtensionIcon,
  BusinessCenter as BusinessIcon,
  AdminPanelSettings as AdminIcon,
  Notifications as NotificationsIcon,
  Settings as SettingsIcon,
  Check as CheckIcon,
  Warning as WarningIcon,
  HelpOutline as HelpIcon,
  ContentCopy as CopyIcon,
  OpenInNew as OpenInNewIcon,
  WebAsset as WebAssetIcon,
  Cloud as CloudIcon
} from '@mui/icons-material';
import { AdapterConfiguration } from '../../../types/adapters';

interface SlackApiConfigurationProps {
  configuration: AdapterConfiguration;
  onConfigurationChange: (config: AdapterConfiguration) => void;
  onTest?: () => void;
  errors?: Record<string, string>;
}

interface ManifestJson {
  display_information: {
    name: string;
    description: string;
  };
  features: {
    bot_user?: {
      display_name: string;
      always_online: boolean;
    };
    slash_commands?: Array<{
      command: string;
      description: string;
      usage_hint?: string;
    }>;
  };
  oauth_config: {
    scopes: {
      bot?: string[];
      user?: string[];
    };
  };
  settings: {
    event_subscriptions?: {
      bot_events?: string[];
    };
    interactivity?: {
      is_enabled: boolean;
    };
    org_deploy_enabled: boolean;
    socket_mode_enabled: boolean;
  };
}

const SlackApiConfiguration: React.FC<SlackApiConfigurationProps> = ({
  configuration,
  onConfigurationChange,
  onTest,
  errors = {}
}) => {
  const [activeTab, setActiveTab] = useState(0);
  const [showSecrets, setShowSecrets] = useState(false);
  const [testingConnection, setTestingConnection] = useState(false);
  const [connectionStatus, setConnectionStatus] = useState<'success' | 'error' | null>(null);
  const [selectedScopes, setSelectedScopes] = useState<string[]>(configuration.scopes || []);
  const [manifest, setManifest] = useState<ManifestJson | null>(null);

  // Common OAuth scopes
  const commonScopes = {
    'Messaging': [
      'chat:write',
      'chat:write:public',
      'chat:write:customize',
      'channels:read',
      'channels:write',
      'channels:history',
      'groups:read',
      'groups:write',
      'groups:history',
      'im:read',
      'im:write',
      'im:history',
      'mpim:read',
      'mpim:write',
      'mpim:history'
    ],
    'Users & Workspace': [
      'users:read',
      'users:read.email',
      'users.profile:read',
      'team:read'
    ],
    'Files': [
      'files:read',
      'files:write'
    ],
    'App Features': [
      'commands',
      'incoming-webhook',
      'app_mentions:read',
      'workflow.steps:execute'
    ],
    'Reactions & Pins': [
      'reactions:read',
      'reactions:write',
      'pins:read',
      'pins:write'
    ],
    'Other': [
      'search:read',
      'emoji:read',
      'dnd:read',
      'dnd:write',
      'calls:read',
      'calls:write',
      'bookmarks:read',
      'bookmarks:write',
      'reminders:read',
      'reminders:write'
    ]
  };

  useEffect(() => {
    // Generate app manifest when configuration changes
    generateManifest();
  }, [configuration, selectedScopes, generateManifest]);

  const handleChange = (field: string, value: any) => {
    onConfigurationChange({
      ...configuration,
      [field]: value
    });
  };

  const handleFeatureChange = (feature: string, value: boolean) => {
    onConfigurationChange({
      ...configuration,
      features: {
        ...configuration.features,
        [feature]: value
      }
    });
  };

  const handleLimitChange = (limit: string, value: number) => {
    onConfigurationChange({
      ...configuration,
      limits: {
        ...configuration.limits,
        [limit]: value
      }
    });
  };

  const handleScopeToggle = (scope: string) => {
    const newScopes = selectedScopes.includes(scope)
      ? selectedScopes.filter(s => s !== scope)
      : [...selectedScopes, scope];
    
    setSelectedScopes(newScopes);
    handleChange('scopes', newScopes);
  };

  const generateManifest = useCallback(() => {
    const manifest: ManifestJson = {
      display_information: {
        name: configuration.appName || 'Integrixs Slack App',
        description: configuration.appDescription || 'Integration with Integrixs Flow Bridge'
      },
      features: {
        bot_user: {
          display_name: configuration.botDisplayName || 'Integrixs Bot',
          always_online: true
        }
      },
      oauth_config: {
        scopes: {
          bot: selectedScopes
        }
      },
      settings: {
        event_subscriptions: {
          bot_events: configuration.eventTypes || []
        },
        interactivity: {
          is_enabled: configuration.features?.enableInteractiveComponents || true
        },
        org_deploy_enabled: configuration.features?.enableEnterpriseGrid || false,
        socket_mode_enabled: configuration.features?.enableSocketMode || true
      }
    };

    setManifest(manifest);
  }, [configuration, selectedScopes]);

  const copyManifest = () => {
    if (manifest) {
      navigator.clipboard.writeText(JSON.stringify(manifest, null, 2));
    }
  };

  const testConnection = async () => {
    if (!configuration.botToken) {
      setConnectionStatus('error');
      return;
    }

    setTestingConnection(true);
    try {
      if (onTest) {
        await onTest();
      }
      setConnectionStatus('success');
    } catch (error) {
      setConnectionStatus('error');
    } finally {
      setTestingConnection(false);
    }
  };

  const renderBasicConfiguration = () => (
    <Box>
      <Typography variant="h6" gutterBottom>
        <BotIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
        Basic Configuration
      </Typography>
      
      <Grid container spacing={3}>
        <Grid item xs={12}>
          <Alert severity="info" sx={{ mb: 2 }}>
            <Typography variant="body2">
              To configure your Slack app:
              <ol style={{ marginBottom: 0, paddingLeft: 20 }}>
                <li>Go to <Link href="https://api.slack.com/apps" target="_blank">api.slack.com/apps</Link></li>
                <li>Create a new app or select an existing one</li>
                <li>Choose "From scratch" or "From an app manifest" (use the generated manifest)</li>
                <li>Install the app to your workspace</li>
                <li>Copy the credentials below</li>
              </ol>
            </Typography>
          </Alert>
        </Grid>

        <Grid item xs={12} md={6}>
          <TextField
            fullWidth
            label="Client ID"
            value={configuration.clientId || ''}
            onChange={(e) => handleChange('clientId', e.target.value)}
            error={!!errors.clientId}
            helperText={errors.clientId || 'Found in App Credentials'}
            required
          />
        </Grid>

        <Grid item xs={12} md={6}>
          <TextField
            fullWidth
            label="Client Secret"
            value={configuration.clientSecret || ''}
            onChange={(e) => handleChange('clientSecret', e.target.value)}
            error={!!errors.clientSecret}
            helperText={errors.clientSecret || 'Found in App Credentials'}
            required
            type={showSecrets ? 'text' : 'password'}
            InputProps={{
              endAdornment: (
                <InputAdornment position="end">
                  <IconButton onClick={() => setShowSecrets(!showSecrets)} edge="end">
                    {showSecrets ? <SecurityIcon /> : <InfoIcon />}
                  </IconButton>
                </InputAdornment>
              )
            }}
          />
        </Grid>

        <Grid item xs={12}>
          <TextField
            fullWidth
            label="Bot User OAuth Token"
            value={configuration.botToken || ''}
            onChange={(e) => handleChange('botToken', e.target.value)}
            error={!!errors.botToken}
            helperText={errors.botToken || 'xoxb-... token from OAuth & Permissions'}
            required
            type={showSecrets ? 'text' : 'password'}
            placeholder="xoxb-..."
          />
        </Grid>

        <Grid item xs={12} md={6}>
          <TextField
            fullWidth
            label="Signing Secret"
            value={configuration.signingSecret || ''}
            onChange={(e) => handleChange('signingSecret', e.target.value)}
            error={!!errors.signingSecret}
            helperText={errors.signingSecret || 'For verifying requests from Slack'}
            required
            type={showSecrets ? 'text' : 'password'}
          />
        </Grid>

        <Grid item xs={12} md={6}>
          <TextField
            fullWidth
            label="Workspace/Team ID"
            value={configuration.workspaceId || ''}
            onChange={(e) => handleChange('workspaceId', e.target.value)}
            error={!!errors.workspaceId}
            helperText={errors.workspaceId || 'Optional: Specific workspace ID'}
            placeholder="T0123456789"
          />
        </Grid>

        <Grid item xs={12} md={6}>
          <TextField
            fullWidth
            label="App Name"
            value={configuration.appName || ''}
            onChange={(e) => handleChange('appName', e.target.value)}
            helperText="Display name for your Slack app"
            placeholder="Integrixs Bot"
          />
        </Grid>

        <Grid item xs={12} md={6}>
          <TextField
            fullWidth
            label="Bot Display Name"
            value={configuration.botDisplayName || ''}
            onChange={(e) => handleChange('botDisplayName', e.target.value)}
            helperText="How the bot appears in conversations"
            placeholder="Integrixs"
          />
        </Grid>

        <Grid item xs={12}>
          <Button
            variant="contained"
            onClick={testConnection}
            disabled={!configuration.botToken || testingConnection}
            startIcon={testingConnection ? <ScheduleIcon /> : <CheckIcon />}
          >
            {testingConnection ? 'Testing...' : 'Test Connection'}
          </Button>

          {connectionStatus === 'success' && (
            <Alert severity="success" sx={{ mt: 2 }}>
              Successfully connected to Slack!
            </Alert>
          )}

          {connectionStatus === 'error' && (
            <Alert severity="error" sx={{ mt: 2 }}>
              Failed to connect. Please check your credentials.
            </Alert>
          )}
        </Grid>
      </Grid>
    </Box>
  );

  const renderOAuthScopes = () => (
    <Box>
      <Typography variant="h6" gutterBottom>
        <SecurityIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
        OAuth Scopes
      </Typography>

      <Alert severity="warning" sx={{ mb: 3 }}>
        Select only the scopes your integration needs. Requesting unnecessary permissions may cause users to reject the installation.
      </Alert>

      {Object.entries(commonScopes).map(([category, scopes]) => (
        <Accordion key={category}>
          <AccordionSummary expandIcon={<ExpandMoreIcon />}>
            <Typography>{category}</Typography>
            <Box sx={{ ml: 2 }}>
              <Chip
                size="small"
                label={`${scopes.filter(s => selectedScopes.includes(s)).length}/${scopes.length}`}
              />
            </Box>
          </AccordionSummary>
          <AccordionDetails>
            <FormGroup>
              {scopes.map(scope => (
                <FormControlLabel
                  key={scope}
                  control={
                    <Checkbox
                      checked={selectedScopes.includes(scope)}
                      onChange={() => handleScopeToggle(scope)}
                    />
                  }
                  label={
                    <Box>
                      <Typography variant="body2">{scope}</Typography>
                      <Typography variant="caption" color="textSecondary">
                        {getScopeDescription(scope)}
                      </Typography>
                    </Box>
                  }
                />
              ))}
            </FormGroup>
          </AccordionDetails>
        </Accordion>
      ))}

      <Paper sx={{ p: 2, mt: 3 }}>
        <Typography variant="subtitle2" gutterBottom>Selected Scopes:</Typography>
        <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
          {selectedScopes.length > 0 ? (
            selectedScopes.map(scope => (
              <Chip
                key={scope}
                label={scope}
                size="small"
                onDelete={() => handleScopeToggle(scope)}
              />
            ))
          ) : (
            <Typography variant="body2" color="textSecondary">
              No scopes selected
            </Typography>
          )}
        </Box>
      </Paper>
    </Box>
  );

  const getScopeDescription = (scope: string): string => {
    const descriptions: Record<string, string> = {
      'chat:write': 'Send messages as the app',
      'chat:write:public': 'Send messages to channels without joining',
      'chat:write:customize': 'Send messages with customized name and avatar',
      'channels:read': 'View basic channel information',
      'channels:write': 'Manage channels',
      'channels:history': 'View messages in public channels',
      'users:read': 'View users in the workspace',
      'users:read.email': 'View email addresses of users',
      'files:read': 'View files shared in channels',
      'files:write': 'Upload, edit, and delete files',
      'commands': 'Add slash commands',
      'incoming-webhook': 'Post messages to specific channels',
      'app_mentions:read': 'View messages that mention the app',
      // Add more descriptions as needed
    };
    return descriptions[scope] || '';
  };

  const renderFeaturesConfiguration = () => (
    <Box>
      <Typography variant="h6" gutterBottom>
        <ExtensionIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
        Features Configuration
      </Typography>

      <Grid container spacing={2}>
        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 2 }}>
            <Typography variant="subtitle1" gutterBottom>
              <MessageIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
              Messaging Features
            </Typography>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableMessaging || true}
                  onChange={(e) => handleFeatureChange('enableMessaging', e.target.checked)}
                />
              }
              label="Messaging"
            />
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableChannels || true}
                  onChange={(e) => handleFeatureChange('enableChannels', e.target.checked)}
                />
              }
              label="Channels"
            />
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableDirectMessages || true}
                  onChange={(e) => handleFeatureChange('enableDirectMessages', e.target.checked)}
                />
              }
              label="Direct Messages"
            />
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableGroups || true}
                  onChange={(e) => handleFeatureChange('enableGroups', e.target.checked)}
                />
              }
              label="Private Channels"
            />
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableThreads || true}
                  onChange={(e) => handleFeatureChange('enableThreads', e.target.checked)}
                />
              }
              label="Thread Support"
            />
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableScheduledMessages || true}
                  onChange={(e) => handleFeatureChange('enableScheduledMessages', e.target.checked)}
                />
              }
              label="Scheduled Messages"
            />
          </Paper>
        </Grid>

        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 2 }}>
            <Typography variant="subtitle1" gutterBottom>
              <CodeIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
              Interactive Features
            </Typography>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableSlashCommands || true}
                  onChange={(e) => handleFeatureChange('enableSlashCommands', e.target.checked)}
                />
              }
              label="Slash Commands"
            />
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableInteractiveComponents || true}
                  onChange={(e) => handleFeatureChange('enableInteractiveComponents', e.target.checked)}
                />
              }
              label="Interactive Components"
            />
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableWorkflows || true}
                  onChange={(e) => handleFeatureChange('enableWorkflows', e.target.checked)}
                />
              }
              label="Workflows"
            />
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableShortcuts || true}
                  onChange={(e) => handleFeatureChange('enableShortcuts', e.target.checked)}
                />
              }
              label="Shortcuts"
            />
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableAppHome || true}
                  onChange={(e) => handleFeatureChange('enableAppHome', e.target.checked)}
                />
              }
              label="App Home Tab"
            />
          </Paper>
        </Grid>

        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 2 }}>
            <Typography variant="subtitle1" gutterBottom>
              <FolderIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
              Content Features
            </Typography>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableFiles || true}
                  onChange={(e) => handleFeatureChange('enableFiles', e.target.checked)}
                />
              }
              label="File Sharing"
            />
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableSnippets || true}
                  onChange={(e) => handleFeatureChange('enableSnippets', e.target.checked)}
                />
              }
              label="Code Snippets"
            />
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableReactions || true}
                  onChange={(e) => handleFeatureChange('enableReactions', e.target.checked)}
                />
              }
              label="Reactions"
            />
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enablePins || true}
                  onChange={(e) => handleFeatureChange('enablePins', e.target.checked)}
                />
              }
              label="Pins"
            />
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableBookmarks || true}
                  onChange={(e) => handleFeatureChange('enableBookmarks', e.target.checked)}
                />
              }
              label="Bookmarks"
            />
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableCanvas || true}
                  onChange={(e) => handleFeatureChange('enableCanvas', e.target.checked)}
                />
              }
              label="Canvas"
            />
          </Paper>
        </Grid>

        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 2 }}>
            <Typography variant="subtitle1" gutterBottom>
              <CloudIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
              Connection & Enterprise
            </Typography>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableSocketMode || true}
                  onChange={(e) => handleFeatureChange('enableSocketMode', e.target.checked)}
                />
              }
              label="Socket Mode (Recommended)"
            />
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableEventSubscriptions || true}
                  onChange={(e) => handleFeatureChange('enableEventSubscriptions', e.target.checked)}
                />
              }
              label="Event Subscriptions"
            />
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableEnterpriseGrid || false}
                  onChange={(e) => handleFeatureChange('enableEnterpriseGrid', e.target.checked)}
                />
              }
              label="Enterprise Grid Support"
            />
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableAdminApis || false}
                  onChange={(e) => handleFeatureChange('enableAdminApis', e.target.checked)}
                />
              }
              label="Admin APIs"
            />
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableSharedChannels || true}
                  onChange={(e) => handleFeatureChange('enableSharedChannels', e.target.checked)}
                />
              }
              label="Shared Channels"
            />
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableConnectChannels || true}
                  onChange={(e) => handleFeatureChange('enableConnectChannels', e.target.checked)}
                />
              }
              label="Slack Connect"
            />
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );

  const renderEventsConfiguration = () => (
    <Box>
      <Typography variant="h6" gutterBottom>
        <NotificationsIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
        Events & Webhooks
      </Typography>

      <Alert severity="info" sx={{ mb: 3 }}>
        {configuration.features?.enableSocketMode ? (
          "Socket Mode is enabled. Events will be received through WebSocket connection without needing a public webhook URL."
        ) : (
          "Configure your webhook URL in the Slack app settings under Event Subscriptions."
        )}
      </Alert>

      <Grid container spacing={3}>
        <Grid item xs={12}>
          <TextField
            fullWidth
            label="Webhook URL"
            value={configuration.webhookUrl || ''}
            onChange={(e) => handleChange('webhookUrl', e.target.value)}
            error={!!errors.webhookUrl}
            helperText={errors.webhookUrl || 'URL where Slack will send events (not needed for Socket Mode)'}
            placeholder="https://yourdomain.com/slack/events"
            disabled={configuration.features?.enableSocketMode}
          />
        </Grid>

        <Grid item xs={12} md={6}>
          <TextField
            fullWidth
            label="Slash Command Prefix"
            value={configuration.slashCommandPrefix || '/'}
            onChange={(e) => handleChange('slashCommandPrefix', e.target.value)}
            helperText="Prefix for slash commands"
          />
        </Grid>

        <Grid item xs={12} md={6}>
          <TextField
            fullWidth
            label="API URL"
            value={configuration.apiUrl || 'https://slack.com/api/'}
            onChange={(e) => handleChange('apiUrl', e.target.value)}
            helperText="Slack API endpoint (change for enterprise)"
          />
        </Grid>

        <Grid item xs={12}>
          <Typography variant="subtitle1" gutterBottom>Event Types to Subscribe:</Typography>
          <Paper sx={{ p: 2, maxHeight: 300, overflow: 'auto' }}>
            <Typography variant="body2" color="textSecondary">
              Configure specific event types in your Slack app settings. Common events include:
            </Typography>
            <List dense>
              <ListItem>
                <ListItemText primary="message.channels" secondary="Messages posted to channels" />
              </ListItem>
              <ListItem>
                <ListItemText primary="app_mention" secondary="When users mention the app" />
              </ListItem>
              <ListItem>
                <ListItemText primary="channel_created" secondary="When channels are created" />
              </ListItem>
              <ListItem>
                <ListItemText primary="member_joined_channel" secondary="When users join channels" />
              </ListItem>
              <ListItem>
                <ListItemText primary="file_shared" secondary="When files are shared" />
              </ListItem>
            </List>
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );

  const renderRateLimitsConfiguration = () => (
    <Box>
      <Typography variant="h6" gutterBottom>
        <SpeedIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
        Rate Limits Configuration
      </Typography>

      <Alert severity="warning" sx={{ mb: 3 }}>
        Slack has tiered rate limits. Configure these based on your app's tier and usage patterns.
      </Alert>

      <Grid container spacing={3}>
        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 2 }}>
            <Typography variant="subtitle1" gutterBottom>API Rate Limits</Typography>
            
            <TextField
              fullWidth
              label="Tier 1 (per minute)"
              type="number"
              value={configuration.limits?.rateLimitTier1PerMinute || 1}
              onChange={(e) => handleLimitChange('rateLimitTier1PerMinute', parseInt(e.target.value))}
              helperText="Rarely used methods (1/min)"
              margin="normal"
              inputProps={{ min: 1, max: 10 }}
            />
            
            <TextField
              fullWidth
              label="Tier 2 (per minute)"
              type="number"
              value={configuration.limits?.rateLimitTier2PerMinute || 20}
              onChange={(e) => handleLimitChange('rateLimitTier2PerMinute', parseInt(e.target.value))}
              helperText="Less frequently used (20/min)"
              margin="normal"
              inputProps={{ min: 1, max: 50 }}
            />
            
            <TextField
              fullWidth
              label="Tier 3 (per minute)"
              type="number"
              value={configuration.limits?.rateLimitTier3PerMinute || 50}
              onChange={(e) => handleLimitChange('rateLimitTier3PerMinute', parseInt(e.target.value))}
              helperText="Frequently used (50/min)"
              margin="normal"
              inputProps={{ min: 1, max: 100 }}
            />
            
            <TextField
              fullWidth
              label="Tier 4 (per minute)"
              type="number"
              value={configuration.limits?.rateLimitTier4PerMinute || 100}
              onChange={(e) => handleLimitChange('rateLimitTier4PerMinute', parseInt(e.target.value))}
              helperText="Very frequently used (100/min)"
              margin="normal"
              inputProps={{ min: 1, max: 200 }}
            />
          </Paper>
        </Grid>

        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 2 }}>
            <Typography variant="subtitle1" gutterBottom>Message & Content Limits</Typography>
            
            <TextField
              fullWidth
              label="Max Message Length"
              type="number"
              value={configuration.limits?.maxMessageLength || 40000}
              onChange={(e) => handleLimitChange('maxMessageLength', parseInt(e.target.value))}
              helperText="Maximum characters per message"
              margin="normal"
              inputProps={{ min: 1, max: 40000 }}
            />
            
            <TextField
              fullWidth
              label="Max Blocks per Message"
              type="number"
              value={configuration.limits?.maxBlocksPerMessage || 50}
              onChange={(e) => handleLimitChange('maxBlocksPerMessage', parseInt(e.target.value))}
              helperText="Maximum blocks in a message"
              margin="normal"
              inputProps={{ min: 1, max: 50 }}
            />
            
            <TextField
              fullWidth
              label="Max File Size (MB)"
              type="number"
              value={configuration.limits?.maxFileSizeMB || 1000}
              onChange={(e) => handleLimitChange('maxFileSizeMB', parseInt(e.target.value))}
              helperText="Maximum file upload size"
              margin="normal"
              inputProps={{ min: 1, max: 1000 }}
            />
            
            <TextField
              fullWidth
              label="Max Snippet Size (KB)"
              type="number"
              value={configuration.limits?.maxSnippetSizeKB || 1000}
              onChange={(e) => handleLimitChange('maxSnippetSizeKB', parseInt(e.target.value))}
              helperText="Maximum code snippet size"
              margin="normal"
              inputProps={{ min: 1, max: 1000 }}
            />
          </Paper>
        </Grid>

        <Grid item xs={12}>
          <Paper sx={{ p: 2 }}>
            <Typography variant="subtitle1" gutterBottom>App Limits</Typography>
            
            <Grid container spacing={2}>
              <Grid item xs={12} md={4}>
                <TextField
                  fullWidth
                  label="Max Slash Commands"
                  type="number"
                  value={configuration.limits?.maxSlashCommandsPerApp || 25}
                  onChange={(e) => handleLimitChange('maxSlashCommandsPerApp', parseInt(e.target.value))}
                  helperText="Maximum slash commands per app"
                  inputProps={{ min: 1, max: 25 }}
                />
              </Grid>
              
              <Grid item xs={12} md={4}>
                <TextField
                  fullWidth
                  label="Max Shortcuts"
                  type="number"
                  value={configuration.limits?.maxShortcutsPerApp || 10}
                  onChange={(e) => handleLimitChange('maxShortcutsPerApp', parseInt(e.target.value))}
                  helperText="Maximum shortcuts per app"
                  inputProps={{ min: 1, max: 10 }}
                />
              </Grid>
              
              <Grid item xs={12} md={4}>
                <TextField
                  fullWidth
                  label="Max Workflow Steps"
                  type="number"
                  value={configuration.limits?.maxWorkflowSteps || 20}
                  onChange={(e) => handleLimitChange('maxWorkflowSteps', parseInt(e.target.value))}
                  helperText="Maximum workflow steps"
                  inputProps={{ min: 1, max: 20 }}
                />
              </Grid>
            </Grid>
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );

  const renderManifestConfiguration = () => (
    <Box>
      <Typography variant="h6" gutterBottom>
        <WebAssetIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
        App Manifest
      </Typography>

      <Alert severity="info" sx={{ mb: 3 }}>
        Use this generated manifest to quickly configure your Slack app with the correct settings.
      </Alert>

      <Paper sx={{ p: 2 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
          <Typography variant="subtitle1">Generated Manifest</Typography>
          <Button
            startIcon={<CopyIcon />}
            onClick={copyManifest}
            size="small"
          >
            Copy Manifest
          </Button>
        </Box>
        
        <Box
          component="pre"
          sx={{
            backgroundColor: 'action.hover',
            p: 2,
            borderRadius: 1,
            overflow: 'auto',
            maxHeight: 400,
            fontSize: '0.875rem'
          }}
        >
          {JSON.stringify(manifest, null, 2)}
        </Box>
      </Paper>

      <Box sx={{ mt: 3 }}>
        <Typography variant="subtitle1" gutterBottom>How to use this manifest:</Typography>
        <List>
          <ListItem>
            <ListItemIcon><InfoIcon /></ListItemIcon>
            <ListItemText
              primary="Create from manifest"
              secondary="When creating a new app, choose 'From an app manifest' and paste this JSON"
            />
          </ListItem>
          <ListItem>
            <ListItemIcon><InfoIcon /></ListItemIcon>
            <ListItemText
              primary="Update existing app"
              secondary="Go to App Manifest in your app settings and replace with this JSON"
            />
          </ListItem>
          <ListItem>
            <ListItemIcon><InfoIcon /></ListItemIcon>
            <ListItemText
              primary="Review and adjust"
              secondary="After applying, review all settings and make adjustments as needed"
            />
          </ListItem>
        </List>
      </Box>
    </Box>
  );

  const renderHelpDocumentation = () => (
    <Box>
      <Typography variant="h6" gutterBottom>
        <HelpIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
        Help & Documentation
      </Typography>

      <Grid container spacing={3}>
        <Grid item xs={12}>
          <Accordion defaultExpanded>
            <AccordionSummary expandIcon={<ExpandMoreIcon />}>
              <Typography>Getting Started with Slack API</Typography>
            </AccordionSummary>
            <AccordionDetails>
              <Typography variant="body2" component="div">
                <ol>
                  <li>Create a Slack app at <Link href="https://api.slack.com/apps" target="_blank">api.slack.com/apps</Link></li>
                  <li>Configure OAuth scopes under "OAuth & Permissions"</li>
                  <li>Install the app to your workspace</li>
                  <li>Copy the Bot User OAuth Token (xoxb-...)</li>
                  <li>Enable Socket Mode for easier development (no public URL needed)</li>
                  <li>Configure event subscriptions if not using Socket Mode</li>
                  <li>Add slash commands, shortcuts, and interactivity as needed</li>
                </ol>
              </Typography>
            </AccordionDetails>
          </Accordion>

          <Accordion>
            <AccordionSummary expandIcon={<ExpandMoreIcon />}>
              <Typography>Socket Mode vs Webhooks</Typography>
            </AccordionSummary>
            <AccordionDetails>
              <Typography variant="body2" component="div">
                <strong>Socket Mode (Recommended for Development):</strong>
                <ul>
                  <li>WebSocket connection - no public URL needed</li>
                  <li>Easier to develop locally</li>
                  <li>All events come through one connection</li>
                  <li>Built-in reconnection handling</li>
                </ul>
                
                <strong>Webhooks (Production):</strong>
                <ul>
                  <li>Requires public HTTPS URL</li>
                  <li>More scalable for high-volume apps</li>
                  <li>Direct HTTP requests from Slack</li>
                  <li>Need to handle verification and retries</li>
                </ul>
              </Typography>
            </AccordionDetails>
          </Accordion>

          <Accordion>
            <AccordionSummary expandIcon={<ExpandMoreIcon />}>
              <Typography>Common Use Cases</Typography>
            </AccordionSummary>
            <AccordionDetails>
              <Typography variant="body2" component="div">
                <ul>
                  <li><strong>Notifications:</strong> Send alerts and updates to channels</li>
                  <li><strong>Slash Commands:</strong> Create custom commands like /deploy</li>
                  <li><strong>Workflows:</strong> Automate processes with Workflow Builder</li>
                  <li><strong>Interactive Messages:</strong> Buttons, select menus, and modals</li>
                  <li><strong>App Home:</strong> Custom tab in the app's messages</li>
                  <li><strong>File Management:</strong> Upload and share files</li>
                  <li><strong>Enterprise Grid:</strong> Manage multiple workspaces</li>
                </ul>
              </Typography>
            </AccordionDetails>
          </Accordion>

          <Accordion>
            <AccordionSummary expandIcon={<ExpandMoreIcon />}>
              <Typography>Best Practices</Typography>
            </AccordionSummary>
            <AccordionDetails>
              <Typography variant="body2" component="div">
                <ul>
                  <li>Request only necessary OAuth scopes</li>
                  <li>Handle rate limits with exponential backoff</li>
                  <li>Use blocks for rich message formatting</li>
                  <li>Implement proper error handling</li>
                  <li>Cache user and channel data when possible</li>
                  <li>Use ephemeral messages for sensitive info</li>
                  <li>Test in a development workspace first</li>
                  <li>Monitor API usage and rate limits</li>
                </ul>
              </Typography>
            </AccordionDetails>
          </Accordion>

          <Accordion>
            <AccordionSummary expandIcon={<ExpandMoreIcon />}>
              <Typography>Troubleshooting</Typography>
            </AccordionSummary>
            <AccordionDetails>
              <Typography variant="body2" component="div">
                <strong>Common Issues:</strong>
                <ul>
                  <li><strong>not_authed:</strong> Check your bot token</li>
                  <li><strong>missing_scope:</strong> Add required OAuth scopes</li>
                  <li><strong>channel_not_found:</strong> Bot may not be in the channel</li>
                  <li><strong>rate_limited:</strong> Implement proper rate limiting</li>
                  <li><strong>invalid_auth:</strong> Token may be revoked or invalid</li>
                </ul>
              </Typography>
            </AccordionDetails>
          </Accordion>
        </Grid>

        <Grid item xs={12}>
          <Paper sx={{ p: 2, bgcolor: 'action.hover' }}>
            <Typography variant="subtitle1" gutterBottom>
              Useful Resources
            </Typography>
            <List dense>
              <ListItem>
                <Link href="https://api.slack.com/docs" target="_blank" rel="noopener">
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    Official Slack API Documentation
                    <OpenInNewIcon fontSize="small" />
                  </Box>
                </Link>
              </ListItem>
              <ListItem>
                <Link href="https://api.slack.com/block-kit" target="_blank" rel="noopener">
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    Block Kit Builder
                    <OpenInNewIcon fontSize="small" />
                  </Box>
                </Link>
              </ListItem>
              <ListItem>
                <Link href="https://api.slack.com/events" target="_blank" rel="noopener">
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    Events API Documentation
                    <OpenInNewIcon fontSize="small" />
                  </Box>
                </Link>
              </ListItem>
              <ListItem>
                <Link href="https://api.slack.com/apis/rate-limits" target="_blank" rel="noopener">
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    Rate Limits Guide
                    <OpenInNewIcon fontSize="small" />
                  </Box>
                </Link>
              </ListItem>
            </List>
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );

  const tabs = [
    { label: 'Basic Setup', icon: <BotIcon /> },
    { label: 'OAuth Scopes', icon: <SecurityIcon /> },
    { label: 'Features', icon: <ExtensionIcon /> },
    { label: 'Events', icon: <NotificationsIcon /> },
    { label: 'Rate Limits', icon: <SpeedIcon /> },
    { label: 'App Manifest', icon: <WebAssetIcon /> },
    { label: 'Help', icon: <HelpIcon /> }
  ];

  return (
    <Box>
      <Box sx={{ borderBottom: 1, borderColor: 'divider', mb: 3 }}>
        <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
          {tabs.map((tab, index) => (
            <Button
              key={index}
              variant={activeTab === index ? 'contained' : 'outlined'}
              onClick={() => setActiveTab(index)}
              startIcon={tab.icon}
              sx={{ mb: 1 }}
            >
              {tab.label}
            </Button>
          ))}
        </Box>
      </Box>

      <Box sx={{ mt: 3 }}>
        {activeTab === 0 && renderBasicConfiguration()}
        {activeTab === 1 && renderOAuthScopes()}
        {activeTab === 2 && renderFeaturesConfiguration()}
        {activeTab === 3 && renderEventsConfiguration()}
        {activeTab === 4 && renderRateLimitsConfiguration()}
        {activeTab === 5 && renderManifestConfiguration()}
        {activeTab === 6 && renderHelpDocumentation()}
      </Box>
    </Box>
  );
};

export default SlackApiConfiguration;