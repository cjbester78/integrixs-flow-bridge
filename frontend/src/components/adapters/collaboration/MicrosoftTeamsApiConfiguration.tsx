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
  Checkbox,
  Tab,
  Tabs
} from '@mui/material';
import {
  ExpandMore as ExpandMoreIcon,
  Info as InfoIcon,
  Security as SecurityIcon,
  Groups as TeamsIcon,
  Message as MessageIcon,
  VideoCall as MeetingIcon,
  Folder as FilesIcon,
  Extension as AppsIcon,
  SmartToy as BotIcon,
  Webhook as WebhookIcon,
  Settings as SettingsIcon,
  Speed as SpeedIcon,
  Check as CheckIcon,
  Warning as WarningIcon,
  HelpOutline as HelpIcon,
  ContentCopy as CopyIcon,
  OpenInNew as OpenInNewIcon,
  Business as BusinessIcon,
  Cloud as CloudIcon,
  Dashboard as DashboardIcon,
  Phone as PhoneIcon,
  Task as TaskIcon
} from '@mui/icons-material';
import { AdapterConfiguration } from '../../../types/adapters';

interface MicrosoftTeamsApiConfigurationProps {
  configuration: AdapterConfiguration;
  onConfigurationChange: (config: AdapterConfiguration) => void;
  onTest?: () => void;
  errors?: Record<string, string>;
}

const MicrosoftTeamsApiConfiguration: React.FC<MicrosoftTeamsApiConfigurationProps> = ({
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
  const [appManifest, setAppManifest] = useState<any>(null);

  // Microsoft Graph API scopes
  const scopeCategories = {
    'Teams & Channels': [
      'Team.Create',
      'TeamSettings.Read.All',
      'TeamSettings.ReadWrite.All',
      'Channel.ReadBasic.All',
      'ChannelSettings.Read.All',
      'ChannelSettings.ReadWrite.All',
      'Channel.Create',
      'Channel.Delete.All',
      'TeamMember.Read.All',
      'TeamMember.ReadWrite.All'
    ],
    'Chat & Messaging': [
      'Chat.Create',
      'Chat.Read',
      'Chat.ReadWrite',
      'ChatMessage.Read',
      'ChatMessage.Send',
      'ChannelMessage.Read.All',
      'ChannelMessage.Send',
      'ChannelMessage.Edit'
    ],
    'Meetings & Calls': [
      'OnlineMeetings.Read',
      'OnlineMeetings.ReadWrite',
      'OnlineMeetingArtifact.Read.All',
      'Calls.Initiate.All',
      'Calls.AccessMedia.All',
      'CallRecords.Read.All'
    ],
    'Users & Groups': [
      'User.Read',
      'User.Read.All',
      'User.ReadBasic.All',
      'Group.Read.All',
      'Group.ReadWrite.All',
      'Directory.Read.All'
    ],
    'Files & Sites': [
      'Files.Read',
      'Files.Read.All',
      'Files.ReadWrite',
      'Files.ReadWrite.All',
      'Sites.Read.All',
      'Sites.ReadWrite.All'
    ],
    'Apps & Presence': [
      'Application.Read.All',
      'AppCatalog.Read.All',
      'Presence.Read',
      'Presence.Read.All'
    ]
  };

  useEffect(() => {
    generateAppManifest();
  }, [configuration, selectedScopes, generateAppManifest]);

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

  const generateAppManifest = useCallback(() => {
    const manifest = {
      "$schema": "https://developer.microsoft.com/en-us/json-schemas/teams/v1.14/MicrosoftTeams.schema.json",
      "manifestVersion": "1.14",
      "version": "1.0.0",
      "id": configuration.botId || "00000000-0000-0000-0000-000000000000",
      "packageName": "com.integrixs.teams",
      "developer": {
        "name": "Integrixs",
        "websiteUrl": "https://integrixs.com",
        "privacyUrl": "https://integrixs.com/privacy",
        "termsOfUseUrl": "https://integrixs.com/terms"
      },
      "name": {
        "short": configuration.botName || "Integrixs Bot",
        "full": configuration.botDescription || "Integrixs Flow Bridge Teams Integration"
      },
      "description": {
        "short": "Integration with Integrixs Flow Bridge",
        "full": "Connect Microsoft Teams with Integrixs Flow Bridge for seamless workflow automation"
      },
      "icons": {
        "color": "color.png",
        "outline": "outline.png"
      },
      "accentColor": "#5B5FC7",
      "permissions": [
        "identity",
        "messageTeamMembers"
      ],
      "validDomains": [
        configuration.notificationUrl?.replace(/https?:\/\//, '').split('/')[0] || "yourdomain.com"
      ]
    };

    if (configuration.features?.enableBots) {
      manifest.bots = [{
        "botId": configuration.botId,
        "scopes": ["personal", "team", "groupchat"],
        "supportsFiles": configuration.features?.enableFiles || false,
        "isNotificationOnly": false
      }];
    }

    if (configuration.features?.enableMessaging) {
      manifest.composeExtensions = [{
        "botId": configuration.botId,
        "commands": [{
          "id": "searchQuery",
          "title": "Search",
          "description": "Search for information",
          "initialRun": true,
          "parameters": [{
            "name": "query",
            "title": "Search query",
            "description": "Your search query",
            "inputType": "text"
          }]
        }]
      }];
    }

    setAppManifest(manifest);
  }, [configuration]);

  const copyManifest = () => {
    if (appManifest) {
      navigator.clipboard.writeText(JSON.stringify(appManifest, null, 2));
    }
  };

  const testConnection = async () => {
    if (!configuration.clientId || !configuration.clientSecret || !configuration.tenantId) {
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
        <BusinessIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
        Azure AD App Configuration
      </Typography>
      
      <Grid container spacing={3}>
        <Grid item xs={12}>
          <Alert severity="info" sx={{ mb: 2 }}>
            <Typography variant="body2">
              To configure Microsoft Teams integration:
              <ol style={{ marginBottom: 0, paddingLeft: 20 }}>
                <li>Register an app in <Link href="https://portal.azure.com/#blade/Microsoft_AAD_IAM/ActiveDirectoryMenuBlade/RegisteredApps" target="_blank">Azure Portal</Link></li>
                <li>Configure API permissions for Microsoft Graph</li>
                <li>Create a client secret</li>
                <li>Add Teams as a platform in Authentication settings</li>
                <li>Copy the values below</li>
              </ol>
            </Typography>
          </Alert>
        </Grid>

        <Grid item xs={12} md={6}>
          <TextField
            fullWidth
            label="Tenant ID"
            value={configuration.tenantId || ''}
            onChange={(e) => handleChange('tenantId', e.target.value)}
            error={!!errors.tenantId}
            helperText={errors.tenantId || 'Your Azure AD tenant ID'}
            required
            placeholder="00000000-0000-0000-0000-000000000000"
          />
        </Grid>

        <Grid item xs={12} md={6}>
          <TextField
            fullWidth
            label="Client ID (Application ID)"
            value={configuration.clientId || ''}
            onChange={(e) => handleChange('clientId', e.target.value)}
            error={!!errors.clientId}
            helperText={errors.clientId || 'Application (client) ID from Azure AD'}
            required
            placeholder="00000000-0000-0000-0000-000000000000"
          />
        </Grid>

        <Grid item xs={12}>
          <TextField
            fullWidth
            label="Client Secret"
            value={configuration.clientSecret || ''}
            onChange={(e) => handleChange('clientSecret', e.target.value)}
            error={!!errors.clientSecret}
            helperText={errors.clientSecret || 'Client secret value (not the ID)'}
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

        <Grid item xs={12} md={6}>
          <TextField
            fullWidth
            label="Bot ID"
            value={configuration.botId || ''}
            onChange={(e) => handleChange('botId', e.target.value)}
            error={!!errors.botId}
            helperText={errors.botId || 'Bot ID (usually same as Client ID)'}
            placeholder="00000000-0000-0000-0000-000000000000"
          />
        </Grid>

        <Grid item xs={12} md={6}>
          <TextField
            fullWidth
            label="Bot Name"
            value={configuration.botName || ''}
            onChange={(e) => handleChange('botName', e.target.value)}
            helperText="Display name for your bot"
            placeholder="Integrixs Bot"
          />
        </Grid>

        <Grid item xs={12}>
          <TextField
            fullWidth
            label="Notification URL"
            value={configuration.notificationUrl || ''}
            onChange={(e) => handleChange('notificationUrl', e.target.value)}
            error={!!errors.notificationUrl}
            helperText={errors.notificationUrl || 'HTTPS URL for receiving change notifications'}
            placeholder="https://yourdomain.com/teams/notifications"
          />
        </Grid>

        <Grid item xs={12}>
          <Button
            variant="contained"
            onClick={testConnection}
            disabled={!configuration.clientId || !configuration.clientSecret || testingConnection}
            startIcon={testingConnection ? <CloudIcon /> : <CheckIcon />}
          >
            {testingConnection ? 'Testing...' : 'Test Connection'}
          </Button>

          {connectionStatus === 'success' && (
            <Alert severity="success" sx={{ mt: 2 }}>
              Successfully connected to Microsoft Graph API!
            </Alert>
          )}

          {connectionStatus === 'error' && (
            <Alert severity="error" sx={{ mt: 2 }}>
              Failed to connect. Please check your credentials and permissions.
            </Alert>
          )}
        </Grid>
      </Grid>
    </Box>
  );

  const renderGraphPermissions = () => (
    <Box>
      <Typography variant="h6" gutterBottom>
        <SecurityIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
        Microsoft Graph API Permissions
      </Typography>

      <Alert severity="warning" sx={{ mb: 3 }}>
        Configure these permissions in Azure AD. Admin consent may be required for some scopes.
      </Alert>

      {Object.entries(scopeCategories).map(([category, scopes]) => (
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
        <Typography variant="subtitle2" gutterBottom>Required Permissions:</Typography>
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
              No permissions selected
            </Typography>
          )}
        </Box>
      </Paper>
    </Box>
  );

  const getScopeDescription = (scope: string): string => {
    const descriptions: Record<string, string> = {
      'Team.Create': 'Create new teams',
      'TeamSettings.Read.All': 'Read all teams settings',
      'TeamSettings.ReadWrite.All': 'Read and update all teams settings',
      'Channel.ReadBasic.All': 'Read basic channel information',
      'ChannelSettings.Read.All': 'Read all channel settings',
      'ChannelSettings.ReadWrite.All': 'Manage all channel settings',
      'Chat.Create': 'Create new chats',
      'Chat.Read': 'Read user\'s chats',
      'Chat.ReadWrite': 'Read and write user\'s chats',
      'ChatMessage.Send': 'Send chat messages',
      'OnlineMeetings.Read': 'Read user\'s online meetings',
      'OnlineMeetings.ReadWrite': 'Create and manage online meetings',
      'User.Read': 'Sign in and read user profile',
      'User.Read.All': 'Read all users\' profiles',
      'Files.Read': 'Read user files',
      'Files.ReadWrite': 'Read and write user files',
      // Add more descriptions as needed
    };
    return descriptions[scope] || '';
  };

  const renderFeaturesConfiguration = () => (
    <Box>
      <Typography variant="h6" gutterBottom>
        <AppsIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
        Features Configuration
      </Typography>

      <Grid container spacing={2}>
        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 2 }}>
            <Typography variant="subtitle1" gutterBottom>
              <MessageIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
              Messaging & Communication
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
                  checked={configuration.features?.enableTeams || true}
                  onChange={(e) => handleFeatureChange('enableTeams', e.target.checked)}
                />
              }
              label="Teams Management"
            />
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableProactiveMessaging || true}
                  onChange={(e) => handleFeatureChange('enableProactiveMessaging', e.target.checked)}
                />
              }
              label="Proactive Messaging"
            />
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableActivityFeed || true}
                  onChange={(e) => handleFeatureChange('enableActivityFeed', e.target.checked)}
                />
              }
              label="Activity Feed Notifications"
            />
          </Paper>
        </Grid>

        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 2 }}>
            <Typography variant="subtitle1" gutterBottom>
              <MeetingIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
              Meetings & Calls
            </Typography>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableMeetings || true}
                  onChange={(e) => handleFeatureChange('enableMeetings', e.target.checked)}
                />
              }
              label="Online Meetings"
            />
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableCalls || true}
                  onChange={(e) => handleFeatureChange('enableCalls', e.target.checked)}
                />
              }
              label="Voice & Video Calls"
            />
          </Paper>
        </Grid>

        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 2 }}>
            <Typography variant="subtitle1" gutterBottom>
              <BotIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
              Bot & Cards
            </Typography>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableBots || true}
                  onChange={(e) => handleFeatureChange('enableBots', e.target.checked)}
                />
              }
              label="Bot Framework"
            />
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableCards || true}
                  onChange={(e) => handleFeatureChange('enableCards', e.target.checked)}
                />
              }
              label="Rich Cards"
            />
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableAdaptiveCards || true}
                  onChange={(e) => handleFeatureChange('enableAdaptiveCards', e.target.checked)}
                />
              }
              label="Adaptive Cards"
            />
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableMessageExtensions || true}
                  onChange={(e) => handleFeatureChange('enableMessageExtensions', e.target.checked)}
                />
              }
              label="Message Extensions"
            />
          </Paper>
        </Grid>

        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 2 }}>
            <Typography variant="subtitle1" gutterBottom>
              <FilesIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
              Files & Content
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
                  checked={configuration.features?.enableTabs || true}
                  onChange={(e) => handleFeatureChange('enableTabs', e.target.checked)}
                />
              }
              label="Custom Tabs"
            />
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableSharePoint || true}
                  onChange={(e) => handleFeatureChange('enableSharePoint', e.target.checked)}
                />
              }
              label="SharePoint Integration"
            />
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableOneNote || true}
                  onChange={(e) => handleFeatureChange('enableOneNote', e.target.checked)}
                />
              }
              label="OneNote Integration"
            />
          </Paper>
        </Grid>

        <Grid item xs={12}>
          <Paper sx={{ p: 2 }}>
            <Typography variant="subtitle1" gutterBottom>
              <DashboardIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
              Microsoft 365 Integration
            </Typography>
            <Grid container>
              <Grid item xs={6} md={3}>
                <FormControlLabel
                  control={
                    <Switch
                      checked={configuration.features?.enablePlanner || true}
                      onChange={(e) => handleFeatureChange('enablePlanner', e.target.checked)}
                    />
                  }
                  label="Planner"
                />
              </Grid>
              <Grid item xs={6} md={3}>
                <FormControlLabel
                  control={
                    <Switch
                      checked={configuration.features?.enablePowerApps || true}
                      onChange={(e) => handleFeatureChange('enablePowerApps', e.target.checked)}
                    />
                  }
                  label="Power Apps"
                />
              </Grid>
              <Grid item xs={6} md={3}>
                <FormControlLabel
                  control={
                    <Switch
                      checked={configuration.features?.enablePowerAutomate || true}
                      onChange={(e) => handleFeatureChange('enablePowerAutomate', e.target.checked)}
                    />
                  }
                  label="Power Automate"
                />
              </Grid>
              <Grid item xs={6} md={3}>
                <FormControlLabel
                  control={
                    <Switch
                      checked={configuration.features?.enableForms || true}
                      onChange={(e) => handleFeatureChange('enableForms', e.target.checked)}
                    />
                  }
                  label="Forms"
                />
              </Grid>
              <Grid item xs={6} md={3}>
                <FormControlLabel
                  control={
                    <Switch
                      checked={configuration.features?.enableStream || true}
                      onChange={(e) => handleFeatureChange('enableStream', e.target.checked)}
                    />
                  }
                  label="Stream"
                />
              </Grid>
              <Grid item xs={6} md={3}>
                <FormControlLabel
                  control={
                    <Switch
                      checked={configuration.features?.enableWhiteboard || true}
                      onChange={(e) => handleFeatureChange('enableWhiteboard', e.target.checked)}
                    />
                  }
                  label="Whiteboard"
                />
              </Grid>
              <Grid item xs={6} md={3}>
                <FormControlLabel
                  control={
                    <Switch
                      checked={configuration.features?.enableLists || true}
                      onChange={(e) => handleFeatureChange('enableLists', e.target.checked)}
                    />
                  }
                  label="Lists"
                />
              </Grid>
              <Grid item xs={6} md={3}>
                <FormControlLabel
                  control={
                    <Switch
                      checked={configuration.features?.enableShifts || true}
                      onChange={(e) => handleFeatureChange('enableShifts', e.target.checked)}
                    />
                  }
                  label="Shifts"
                />
              </Grid>
            </Grid>
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );

  const renderWebhooksConfiguration = () => (
    <Box>
      <Typography variant="h6" gutterBottom>
        <WebhookIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
        Webhooks & Events
      </Typography>

      <Alert severity="info" sx={{ mb: 3 }}>
        Teams uses change notifications to notify your app about changes in Teams, chats, and channels.
      </Alert>

      <Grid container spacing={3}>
        <Grid item xs={12}>
          <FormControlLabel
            control={
              <Switch
                checked={configuration.features?.enableWebhooks || true}
                onChange={(e) => handleFeatureChange('enableWebhooks', e.target.checked)}
              />
            }
            label="Enable Change Notifications"
          />
        </Grid>

        <Grid item xs={12}>
          <TextField
            fullWidth
            label="Webhook URL"
            value={configuration.webhookUrl || ''}
            onChange={(e) => handleChange('webhookUrl', e.target.value)}
            error={!!errors.webhookUrl}
            helperText={errors.webhookUrl || 'HTTPS URL for receiving webhooks'}
            placeholder="https://yourdomain.com/teams/webhook"
            disabled={!configuration.features?.enableWebhooks}
          />
        </Grid>

        <Grid item xs={12} md={6}>
          <TextField
            fullWidth
            label="Graph API URL"
            value={configuration.graphApiUrl || 'https://graph.microsoft.com/v1.0'}
            onChange={(e) => handleChange('graphApiUrl', e.target.value)}
            helperText="Microsoft Graph API endpoint"
          />
        </Grid>

        <Grid item xs={12} md={6}>
          <TextField
            fullWidth
            label="Bot Framework URL"
            value={configuration.botFrameworkUrl || 'https://smba.trafficmanager.net/teams'}
            onChange={(e) => handleChange('botFrameworkUrl', e.target.value)}
            helperText="Bot Framework messaging endpoint"
          />
        </Grid>

        <Grid item xs={12}>
          <Typography variant="subtitle1" gutterBottom>Event Subscriptions</Typography>
          <Paper sx={{ p: 2 }}>
            <Typography variant="body2" color="textSecondary" gutterBottom>
              Configure change notification subscriptions in your application to receive these events:
            </Typography>
            <List dense>
              <ListItem>
                <ListItemText 
                  primary="/teams" 
                  secondary="Team created, updated, or deleted events" 
                />
              </ListItem>
              <ListItem>
                <ListItemText 
                  primary="/teams/getAllMessages" 
                  secondary="All messages in teams and channels" 
                />
              </ListItem>
              <ListItem>
                <ListItemText 
                  primary="/chats/getAllMessages" 
                  secondary="All chat messages" 
                />
              </ListItem>
              <ListItem>
                <ListItemText 
                  primary="/communications/onlineMeetings" 
                  secondary="Meeting created or updated events" 
                />
              </ListItem>
              <ListItem>
                <ListItemText 
                  primary="/communications/callRecords" 
                  secondary="Call records for analytics" 
                />
              </ListItem>
            </List>
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );

  const renderRateLimits = () => (
    <Box>
      <Typography variant="h6" gutterBottom>
        <SpeedIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
        Rate Limits & Constraints
      </Typography>

      <Alert severity="warning" sx={{ mb: 3 }}>
        Microsoft Graph has service-specific throttling limits. Configure these based on your usage patterns.
      </Alert>

      <Grid container spacing={3}>
        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 2 }}>
            <Typography variant="subtitle1" gutterBottom>Message Limits</Typography>
            
            <TextField
              fullWidth
              label="Max Message Length"
              type="number"
              value={configuration.limits?.maxMessageLength || 28000}
              onChange={(e) => handleLimitChange('maxMessageLength', parseInt(e.target.value))}
              helperText="Maximum characters per message"
              margin="normal"
              inputProps={{ min: 1, max: 28000 }}
            />
            
            <TextField
              fullWidth
              label="Max Attachments per Message"
              type="number"
              value={configuration.limits?.maxAttachmentsPerMessage || 10}
              onChange={(e) => handleLimitChange('maxAttachmentsPerMessage', parseInt(e.target.value))}
              helperText="Maximum attachments in a message"
              margin="normal"
              inputProps={{ min: 1, max: 10 }}
            />
            
            <TextField
              fullWidth
              label="Max Card Size (KB)"
              type="number"
              value={configuration.limits?.maxCardSize || 25}
              onChange={(e) => handleLimitChange('maxCardSize', parseInt(e.target.value))}
              helperText="Maximum size for adaptive cards"
              margin="normal"
              inputProps={{ min: 1, max: 25 }}
            />
          </Paper>
        </Grid>

        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 2 }}>
            <Typography variant="subtitle1" gutterBottom>Team & Channel Limits</Typography>
            
            <TextField
              fullWidth
              label="Max Channels per Team"
              type="number"
              value={configuration.limits?.maxChannelsPerTeam || 200}
              onChange={(e) => handleLimitChange('maxChannelsPerTeam', parseInt(e.target.value))}
              helperText="Maximum channels in a team"
              margin="normal"
              inputProps={{ min: 1, max: 200 }}
            />
            
            <TextField
              fullWidth
              label="Max Members per Team"
              type="number"
              value={configuration.limits?.maxMembersPerTeam || 25000}
              onChange={(e) => handleLimitChange('maxMembersPerTeam', parseInt(e.target.value))}
              helperText="Maximum members in a team"
              margin="normal"
              inputProps={{ min: 1, max: 25000 }}
            />
            
            <TextField
              fullWidth
              label="Max Teams per User"
              type="number"
              value={configuration.limits?.maxTeamsPerUser || 250}
              onChange={(e) => handleLimitChange('maxTeamsPerUser', parseInt(e.target.value))}
              helperText="Maximum teams a user can join"
              margin="normal"
              inputProps={{ min: 1, max: 250 }}
            />
          </Paper>
        </Grid>

        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 2 }}>
            <Typography variant="subtitle1" gutterBottom>API Rate Limits</Typography>
            
            <TextField
              fullWidth
              label="Requests per Second"
              type="number"
              value={configuration.limits?.rateLimitPerSecond || 30}
              onChange={(e) => handleLimitChange('rateLimitPerSecond', parseInt(e.target.value))}
              helperText="Maximum API requests per second"
              margin="normal"
              inputProps={{ min: 1, max: 100 }}
            />
            
            <TextField
              fullWidth
              label="Requests per Minute"
              type="number"
              value={configuration.limits?.rateLimitPerMinute || 500}
              onChange={(e) => handleLimitChange('rateLimitPerMinute', parseInt(e.target.value))}
              helperText="Maximum API requests per minute"
              margin="normal"
              inputProps={{ min: 1, max: 1000 }}
            />
            
            <TextField
              fullWidth
              label="Burst Limit"
              type="number"
              value={configuration.limits?.burstLimit || 100}
              onChange={(e) => handleLimitChange('burstLimit', parseInt(e.target.value))}
              helperText="Maximum burst requests"
              margin="normal"
              inputProps={{ min: 1, max: 200 }}
            />
          </Paper>
        </Grid>

        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 2 }}>
            <Typography variant="subtitle1" gutterBottom>Bot Limits</Typography>
            
            <TextField
              fullWidth
              label="Bot Messages per Second"
              type="number"
              value={configuration.limits?.maxBotMessagesPerSecond || 8}
              onChange={(e) => handleLimitChange('maxBotMessagesPerSecond', parseInt(e.target.value))}
              helperText="Per conversation"
              margin="normal"
              inputProps={{ min: 1, max: 10 }}
            />
            
            <TextField
              fullWidth
              label="Bot Messages per Minute"
              type="number"
              value={configuration.limits?.maxBotMessagesPerMinute || 60}
              onChange={(e) => handleLimitChange('maxBotMessagesPerMinute', parseInt(e.target.value))}
              helperText="Per conversation"
              margin="normal"
              inputProps={{ min: 1, max: 100 }}
            />
            
            <TextField
              fullWidth
              label="Bot Conversations per Second"
              type="number"
              value={configuration.limits?.maxBotConversationsPerSecond || 15}
              onChange={(e) => handleLimitChange('maxBotConversationsPerSecond', parseInt(e.target.value))}
              helperText="New conversations per second"
              margin="normal"
              inputProps={{ min: 1, max: 20 }}
            />
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );

  const renderAppManifest = () => (
    <Box>
      <Typography variant="h6" gutterBottom>
        <AppsIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
        Teams App Manifest
      </Typography>

      <Alert severity="info" sx={{ mb: 3 }}>
        Use this manifest to package and deploy your Teams app.
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
          {JSON.stringify(appManifest, null, 2)}
        </Box>
      </Paper>

      <Box sx={{ mt: 3 }}>
        <Typography variant="subtitle1" gutterBottom>Next Steps:</Typography>
        <List>
          <ListItem>
            <ListItemIcon><InfoIcon /></ListItemIcon>
            <ListItemText
              primary="Create app package"
              secondary="Add manifest.json, color.png (192x192), and outline.png (32x32) to a .zip file"
            />
          </ListItem>
          <ListItem>
            <ListItemIcon><InfoIcon /></ListItemIcon>
            <ListItemText
              primary="Upload to Teams"
              secondary="Go to Teams > Apps > Upload a custom app"
            />
          </ListItem>
          <ListItem>
            <ListItemIcon><InfoIcon /></ListItemIcon>
            <ListItemText
              primary="Publish to org"
              secondary="Submit to your org's app catalog for wider distribution"
            />
          </ListItem>
        </List>
      </Box>
    </Box>
  );

  const renderHelp = () => (
    <Box>
      <Typography variant="h6" gutterBottom>
        <HelpIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
        Help & Documentation
      </Typography>

      <Grid container spacing={3}>
        <Grid item xs={12}>
          <Accordion defaultExpanded>
            <AccordionSummary expandIcon={<ExpandMoreIcon />}>
              <Typography>Getting Started with Teams API</Typography>
            </AccordionSummary>
            <AccordionDetails>
              <Typography variant="body2" component="div">
                <ol>
                  <li>Register an app in Azure Active Directory</li>
                  <li>Configure Microsoft Graph permissions</li>
                  <li>Create and note down a client secret</li>
                  <li>Add Teams platform in Authentication settings</li>
                  <li>Configure redirect URIs if using delegated permissions</li>
                  <li>Grant admin consent for application permissions</li>
                  <li>Create and upload your Teams app package</li>
                </ol>
              </Typography>
            </AccordionDetails>
          </Accordion>

          <Accordion>
            <AccordionSummary expandIcon={<ExpandMoreIcon />}>
              <Typography>Application vs Delegated Permissions</Typography>
            </AccordionSummary>
            <AccordionDetails>
              <Typography variant="body2" component="div">
                <strong>Application Permissions:</strong>
                <ul>
                  <li>App acts on its own behalf</li>
                  <li>No user context required</li>
                  <li>Admin consent always required</li>
                  <li>Best for background services</li>
                </ul>
                
                <strong>Delegated Permissions:</strong>
                <ul>
                  <li>App acts on behalf of a user</li>
                  <li>Requires user authentication</li>
                  <li>User or admin consent</li>
                  <li>Best for user-facing features</li>
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
                  <li><strong>Team Collaboration:</strong> Automate team and channel management</li>
                  <li><strong>Meeting Automation:</strong> Schedule and manage online meetings</li>
                  <li><strong>Notifications:</strong> Send proactive messages and activity feed updates</li>
                  <li><strong>File Integration:</strong> Share and manage files through SharePoint</li>
                  <li><strong>Workflow Automation:</strong> Integrate with Power Automate</li>
                  <li><strong>Custom Apps:</strong> Build tabs, bots, and message extensions</li>
                  <li><strong>Analytics:</strong> Track usage and engagement metrics</li>
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
                  <li>Request only necessary permissions</li>
                  <li>Implement proper error handling and retries</li>
                  <li>Respect throttling limits</li>
                  <li>Use batching for multiple operations</li>
                  <li>Cache frequently accessed data</li>
                  <li>Follow Teams app design guidelines</li>
                  <li>Test in different team configurations</li>
                  <li>Monitor API usage and quotas</li>
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
                  <li><strong>401 Unauthorized:</strong> Check token validity and permissions</li>
                  <li><strong>403 Forbidden:</strong> Missing required permissions or admin consent</li>
                  <li><strong>429 Too Many Requests:</strong> Implement exponential backoff</li>
                  <li><strong>404 Not Found:</strong> Check resource IDs and API versions</li>
                  <li><strong>Consent Required:</strong> Admin must grant consent in Azure AD</li>
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
                <Link href="https://docs.microsoft.com/en-us/graph/teams-concept-overview" target="_blank" rel="noopener">
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    Microsoft Graph Teams API Documentation
                    <OpenInNewIcon fontSize="small" />
                  </Box>
                </Link>
              </ListItem>
              <ListItem>
                <Link href="https://docs.microsoft.com/en-us/microsoftteams/platform/" target="_blank" rel="noopener">
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    Teams App Development Documentation
                    <OpenInNewIcon fontSize="small" />
                  </Box>
                </Link>
              </ListItem>
              <ListItem>
                <Link href="https://developer.microsoft.com/en-us/graph/graph-explorer" target="_blank" rel="noopener">
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    Graph Explorer
                    <OpenInNewIcon fontSize="small" />
                  </Box>
                </Link>
              </ListItem>
              <ListItem>
                <Link href="https://adaptivecards.io/designer/" target="_blank" rel="noopener">
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    Adaptive Cards Designer
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
    { label: 'Azure AD Setup', icon: <BusinessIcon /> },
    { label: 'Graph Permissions', icon: <SecurityIcon /> },
    { label: 'Features', icon: <AppsIcon /> },
    { label: 'Webhooks', icon: <WebhookIcon /> },
    { label: 'Rate Limits', icon: <SpeedIcon /> },
    { label: 'App Manifest', icon: <TeamsIcon /> },
    { label: 'Help', icon: <HelpIcon /> }
  ];

  return (
    <Box>
      <Box sx={{ borderBottom: 1, borderColor: 'divider', mb: 3 }}>
        <Tabs
          value={activeTab}
          onChange={(_, newValue) => setActiveTab(newValue)}
          variant="scrollable"
          scrollButtons="auto"
        >
          {tabs.map((tab, index) => (
            <Tab
              key={index}
              label={tab.label}
              icon={tab.icon}
              iconPosition="start"
            />
          ))}
        </Tabs>
      </Box>

      <Box sx={{ mt: 3 }}>
        {activeTab === 0 && renderBasicConfiguration()}
        {activeTab === 1 && renderGraphPermissions()}
        {activeTab === 2 && renderFeaturesConfiguration()}
        {activeTab === 3 && renderWebhooksConfiguration()}
        {activeTab === 4 && renderRateLimits()}
        {activeTab === 5 && renderAppManifest()}
        {activeTab === 6 && renderHelp()}
      </Box>
    </Box>
  );
};

export default MicrosoftTeamsApiConfiguration;