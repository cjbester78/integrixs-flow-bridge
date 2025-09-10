import React, { useState, useEffect } from 'react';
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
  ListItemIcon
} from '@mui/material';
import {
  ExpandMore as ExpandMoreIcon,
  Info as InfoIcon,
  Security as SecurityIcon,
  Send as SendIcon,
  Forum as ForumIcon,
  SmartToy as BotIcon,
  Webhook as WebhookIcon,
  Schedule as ScheduleIcon,
  Speed as SpeedIcon,
  AttachFile as FileIcon,
  EmojiEmotions as StickerIcon,
  Poll as PollIcon,
  VideogameAsset as GameIcon,
  Payment as PaymentIcon,
  Language as WebIcon,
  Groups as GroupIcon,
  Message as MessageIcon,
  Settings as SettingsIcon,
  Check as CheckIcon,
  Warning as WarningIcon,
  HelpOutline as HelpIcon
} from '@mui/icons-material';
import { AdapterConfiguration } from '../../../types/adapters';

interface TelegramBotApiConfigurationProps {
  configuration: AdapterConfiguration;
  onConfigurationChange: (config: AdapterConfiguration) => void;
  onTest?: () => void;
  errors?: Record<string, string>;
}

interface WebhookStatus {
  url?: string;
  hasCustomCertificate: boolean;
  pendingUpdateCount: number;
  lastErrorDate?: number;
  lastErrorMessage?: string;
  maxConnections?: number;
  allowedUpdates?: string[];
}

const TelegramBotApiConfiguration: React.FC<TelegramBotApiConfigurationProps> = ({
  configuration,
  onConfigurationChange,
  onTest,
  errors = {}
}) => {
  const [activeTab, setActiveTab] = useState(0);
  const [showApiKey, setShowApiKey] = useState(false);
  const [webhookStatus, setWebhookStatus] = useState<WebhookStatus | null>(null);
  const [testingConnection, setTestingConnection] = useState(false);
  const [connectionStatus, setConnectionStatus] = useState<'success' | 'error' | null>(null);

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
      // In a real implementation, this would make an API call to test the bot token
      setConnectionStatus('success');
      
      // Simulate fetching webhook status
      setTimeout(() => {
        setWebhookStatus({
          url: configuration.webhookUrl || '',
          hasCustomCertificate: false,
          pendingUpdateCount: 0,
          maxConnections: 40,
          allowedUpdates: ['message', 'callback_query']
        });
      }, 1000);
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
        Bot Configuration
      </Typography>
      
      <Grid container spacing={3}>
        <Grid item xs={12}>
          <Alert severity="info" sx={{ mb: 2 }}>
            <Typography variant="body2">
              To get your bot token:
              <ol style={{ marginBottom: 0, paddingLeft: 20 }}>
                <li>Open Telegram and search for @BotFather</li>
                <li>Send /newbot to create a new bot or /mybots to manage existing ones</li>
                <li>Follow the instructions to get your bot token</li>
                <li>Configure your bot settings with @BotFather (description, profile picture, etc.)</li>
              </ol>
            </Typography>
          </Alert>
        </Grid>

        <Grid item xs={12}>
          <TextField
            fullWidth
            label="Bot Token"
            value={configuration.botToken || ''}
            onChange={(e) => handleChange('botToken', e.target.value)}
            error={!!errors.botToken}
            helperText={errors.botToken || 'Your bot token from @BotFather'}
            required
            type={showApiKey ? 'text' : 'password'}
            InputProps={{
              endAdornment: (
                <InputAdornment position="end">
                  <IconButton onClick={() => setShowApiKey(!showApiKey)} edge="end">
                    {showApiKey ? <SecurityIcon /> : <InfoIcon />}
                  </IconButton>
                </InputAdornment>
              )
            }}
          />
        </Grid>

        <Grid item xs={12} md={6}>
          <TextField
            fullWidth
            label="Bot Username"
            value={configuration.botUsername || ''}
            onChange={(e) => handleChange('botUsername', e.target.value)}
            error={!!errors.botUsername}
            helperText={errors.botUsername || 'Your bot username (without @)'}
            placeholder="mybot"
          />
        </Grid>

        <Grid item xs={12} md={6}>
          <TextField
            fullWidth
            label="API URL"
            value={configuration.apiUrl || 'https://api.telegram.org'}
            onChange={(e) => handleChange('apiUrl', e.target.value)}
            error={!!errors.apiUrl}
            helperText={errors.apiUrl || 'Default: https://api.telegram.org (change for local bot API server)'}
          />
        </Grid>

        <Grid item xs={12} md={6}>
          <TextField
            fullWidth
            label="Default Chat ID"
            value={configuration.defaultChatId || ''}
            onChange={(e) => handleChange('defaultChatId', e.target.value)}
            error={!!errors.defaultChatId}
            helperText={errors.defaultChatId || 'Optional: Default chat/channel to monitor (e.g., -1001234567890)'}
            placeholder="-1001234567890"
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
              Successfully connected to Telegram Bot API!
            </Alert>
          )}

          {connectionStatus === 'error' && (
            <Alert severity="error" sx={{ mt: 2 }}>
              Failed to connect. Please check your bot token.
            </Alert>
          )}
        </Grid>
      </Grid>

      {webhookStatus && (
        <Paper sx={{ p: 2, mt: 3 }}>
          <Typography variant="subtitle1" gutterBottom>
            <WebhookIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
            Webhook Status
          </Typography>
          <List dense>
            <ListItem>
              <ListItemIcon><InfoIcon fontSize="small" /></ListItemIcon>
              <ListItemText 
                primary="Webhook URL" 
                secondary={webhookStatus.url || 'Not configured'} 
              />
            </ListItem>
            <ListItem>
              <ListItemIcon><MessageIcon fontSize="small" /></ListItemIcon>
              <ListItemText 
                primary="Pending Updates" 
                secondary={webhookStatus.pendingUpdateCount} 
              />
            </ListItem>
            {webhookStatus.lastErrorMessage && (
              <ListItem>
                <ListItemIcon><WarningIcon fontSize="small" color="error" /></ListItemIcon>
                <ListItemText 
                  primary="Last Error" 
                  secondary={webhookStatus.lastErrorMessage} 
                />
              </ListItem>
            )}
          </List>
        </Paper>
      )}
    </Box>
  );

  const renderWebhookConfiguration = () => (
    <Box>
      <Typography variant="h6" gutterBottom>
        <WebhookIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
        Webhook Configuration
      </Typography>

      <Alert severity="info" sx={{ mb: 3 }}>
        Choose between webhooks (recommended for production) or polling (easier for development).
        You cannot use both simultaneously.
      </Alert>

      <Grid container spacing={3}>
        <Grid item xs={12}>
          <FormControlLabel
            control={
              <Switch
                checked={configuration.features?.enableWebhooks || false}
                onChange={(e) => handleFeatureChange('enableWebhooks', e.target.checked)}
              />
            }
            label="Enable Webhooks"
          />
        </Grid>

        {configuration.features?.enableWebhooks && (
          <>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Webhook URL"
                value={configuration.webhookUrl || ''}
                onChange={(e) => handleChange('webhookUrl', e.target.value)}
                error={!!errors.webhookUrl}
                helperText={errors.webhookUrl || 'HTTPS URL where Telegram will send updates'}
                placeholder="https://yourdomain.com"
                disabled={!configuration.features?.enableWebhooks}
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label="Webhook Path"
                value={configuration.webhookPath || '/webhook/telegram'}
                onChange={(e) => handleChange('webhookPath', e.target.value)}
                error={!!errors.webhookPath}
                helperText={errors.webhookPath || 'Path for webhook endpoint'}
                disabled={!configuration.features?.enableWebhooks}
              />
            </Grid>

            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label="Webhook Secret"
                value={configuration.webhookSecret || ''}
                onChange={(e) => handleChange('webhookSecret', e.target.value)}
                error={!!errors.webhookSecret}
                helperText={errors.webhookSecret || 'Optional: Secret token for webhook verification'}
                type="password"
                disabled={!configuration.features?.enableWebhooks}
              />
            </Grid>

            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Max Webhook Connections"
                type="number"
                value={configuration.limits?.webhookMaxConnections || 40}
                onChange={(e) => handleLimitChange('webhookMaxConnections', parseInt(e.target.value))}
                error={!!errors.webhookMaxConnections}
                helperText={errors.webhookMaxConnections || 'Maximum concurrent webhook connections (1-100)'}
                inputProps={{ min: 1, max: 100 }}
                disabled={!configuration.features?.enableWebhooks}
              />
            </Grid>
          </>
        )}

        <Grid item xs={12}>
          <Divider sx={{ my: 2 }} />
        </Grid>

        <Grid item xs={12}>
          <FormControlLabel
            control={
              <Switch
                checked={configuration.features?.enablePolling || false}
                onChange={(e) => handleFeatureChange('enablePolling', e.target.checked)}
                disabled={configuration.features?.enableWebhooks}
              />
            }
            label="Enable Long Polling"
          />
          <Typography variant="body2" color="textSecondary" sx={{ mt: 1 }}>
            Long polling is simpler to set up but less efficient than webhooks. Good for development.
          </Typography>
        </Grid>
      </Grid>
    </Box>
  );

  const renderFeaturesConfiguration = () => (
    <Box>
      <Typography variant="h6" gutterBottom>
        <SettingsIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
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
                  checked={configuration.features?.enableMessages || true}
                  onChange={(e) => handleFeatureChange('enableMessages', e.target.checked)}
                />
              }
              label="Messages"
            />
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableInlineQueries || true}
                  onChange={(e) => handleFeatureChange('enableInlineQueries', e.target.checked)}
                />
              }
              label="Inline Queries"
            />
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableCallbackQueries || true}
                  onChange={(e) => handleFeatureChange('enableCallbackQueries', e.target.checked)}
                />
              }
              label="Callback Queries"
            />
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableKeyboards || true}
                  onChange={(e) => handleFeatureChange('enableKeyboards', e.target.checked)}
                />
              }
              label="Keyboards & Buttons"
            />
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableCommands || true}
                  onChange={(e) => handleFeatureChange('enableCommands', e.target.checked)}
                />
              }
              label="Bot Commands"
            />
          </Paper>
        </Grid>

        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 2 }}>
            <Typography variant="subtitle1" gutterBottom>
              <GroupIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
              Group & Channel Features
            </Typography>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableChannelPosts || true}
                  onChange={(e) => handleFeatureChange('enableChannelPosts', e.target.checked)}
                />
              }
              label="Channel Posts"
            />
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableGroupManagement || true}
                  onChange={(e) => handleFeatureChange('enableGroupManagement', e.target.checked)}
                />
              }
              label="Group Management"
            />
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableForumSupport || true}
                  onChange={(e) => handleFeatureChange('enableForumSupport', e.target.checked)}
                />
              }
              label="Forum/Topic Support"
            />
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableReactions || true}
                  onChange={(e) => handleFeatureChange('enableReactions', e.target.checked)}
                />
              }
              label="Message Reactions"
            />
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableTopics || true}
                  onChange={(e) => handleFeatureChange('enableTopics', e.target.checked)}
                />
              }
              label="Forum Topics"
            />
          </Paper>
        </Grid>

        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 2 }}>
            <Typography variant="subtitle1" gutterBottom>
              <FileIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
              Media & Content Features
            </Typography>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableFileHandling || true}
                  onChange={(e) => handleFeatureChange('enableFileHandling', e.target.checked)}
                />
              }
              label="File Handling"
            />
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableStickers || true}
                  onChange={(e) => handleFeatureChange('enableStickers', e.target.checked)}
                />
              }
              label="Stickers"
            />
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enablePolls || true}
                  onChange={(e) => handleFeatureChange('enablePolls', e.target.checked)}
                />
              }
              label="Polls"
            />
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableGames || true}
                  onChange={(e) => handleFeatureChange('enableGames', e.target.checked)}
                />
              }
              label="Games"
            />
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableInlineMode || true}
                  onChange={(e) => handleFeatureChange('enableInlineMode', e.target.checked)}
                />
              }
              label="Inline Mode"
            />
          </Paper>
        </Grid>

        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 2 }}>
            <Typography variant="subtitle1" gutterBottom>
              <PaymentIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
              Advanced Features
            </Typography>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enablePayments || true}
                  onChange={(e) => handleFeatureChange('enablePayments', e.target.checked)}
                />
              }
              label="Payments"
            />
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableWebApps || true}
                  onChange={(e) => handleFeatureChange('enableWebApps', e.target.checked)}
                />
              }
              label="Web Apps"
            />
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableBusinessConnection || true}
                  onChange={(e) => handleFeatureChange('enableBusinessConnection', e.target.checked)}
                />
              }
              label="Business Connection"
            />
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableBotAPI60 || true}
                  onChange={(e) => handleFeatureChange('enableBotAPI60', e.target.checked)}
                />
              }
              label="Bot API 6.0+ Features"
            />
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableBotAPI70 || true}
                  onChange={(e) => handleFeatureChange('enableBotAPI70', e.target.checked)}
                />
              }
              label="Bot API 7.0+ Features"
            />
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );

  const renderRateLimitsConfiguration = () => (
    <Box>
      <Typography variant="h6" gutterBottom>
        <SpeedIcon sx={{ mr: 1, verticalAlign: 'middle' }} />
        Rate Limits & Constraints
      </Typography>

      <Alert severity="warning" sx={{ mb: 3 }}>
        Telegram has strict rate limits. Exceeding them may result in temporary bans.
        Configure these limits based on your bot's needs and Telegram's current policies.
      </Alert>

      <Grid container spacing={3}>
        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 2 }}>
            <Typography variant="subtitle1" gutterBottom>Message Limits</Typography>
            
            <TextField
              fullWidth
              label="Max Message Length"
              type="number"
              value={configuration.limits?.maxMessageLength || 4096}
              onChange={(e) => handleLimitChange('maxMessageLength', parseInt(e.target.value))}
              helperText="Maximum UTF-8 characters per message"
              margin="normal"
              inputProps={{ min: 1, max: 4096 }}
            />
            
            <TextField
              fullWidth
              label="Max Caption Length"
              type="number"
              value={configuration.limits?.maxCaptionLength || 1024}
              onChange={(e) => handleLimitChange('maxCaptionLength', parseInt(e.target.value))}
              helperText="Maximum caption length for media"
              margin="normal"
              inputProps={{ min: 1, max: 1024 }}
            />
            
            <TextField
              fullWidth
              label="Messages Per Second"
              type="number"
              value={configuration.limits?.rateLimitPerSecond || 30}
              onChange={(e) => handleLimitChange('rateLimitPerSecond', parseInt(e.target.value))}
              helperText="Maximum messages per second"
              margin="normal"
              inputProps={{ min: 1, max: 30 }}
            />
            
            <TextField
              fullWidth
              label="Different Chats Per Minute"
              type="number"
              value={configuration.limits?.rateLimitPerMinute || 20}
              onChange={(e) => handleLimitChange('rateLimitPerMinute', parseInt(e.target.value))}
              helperText="Maximum different chats per minute"
              margin="normal"
              inputProps={{ min: 1, max: 20 }}
            />
          </Paper>
        </Grid>

        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 2 }}>
            <Typography variant="subtitle1" gutterBottom>File Size Limits</Typography>
            
            <TextField
              fullWidth
              label="Max File Size (MB)"
              type="number"
              value={configuration.limits?.maxFileSizeMB || 50}
              onChange={(e) => handleLimitChange('maxFileSizeMB', parseInt(e.target.value))}
              helperText="Maximum file size for downloads"
              margin="normal"
              inputProps={{ min: 1, max: 50 }}
            />
            
            <TextField
              fullWidth
              label="Max Photo Size (MB)"
              type="number"
              value={configuration.limits?.maxPhotoSizeMB || 10}
              onChange={(e) => handleLimitChange('maxPhotoSizeMB', parseInt(e.target.value))}
              helperText="Maximum photo file size"
              margin="normal"
              inputProps={{ min: 1, max: 10 }}
            />
            
            <TextField
              fullWidth
              label="Max Video Size (MB)"
              type="number"
              value={configuration.limits?.maxVideoSizeMB || 50}
              onChange={(e) => handleLimitChange('maxVideoSizeMB', parseInt(e.target.value))}
              helperText="Maximum video file size"
              margin="normal"
              inputProps={{ min: 1, max: 50 }}
            />
            
            <TextField
              fullWidth
              label="Max Sticker Size (KB)"
              type="number"
              value={configuration.limits?.maxStickerSizeKB || 512}
              onChange={(e) => handleLimitChange('maxStickerSizeKB', parseInt(e.target.value))}
              helperText="Maximum static sticker size"
              margin="normal"
              inputProps={{ min: 1, max: 512 }}
            />
          </Paper>
        </Grid>

        <Grid item xs={12}>
          <Paper sx={{ p: 2 }}>
            <Typography variant="subtitle1" gutterBottom>Interactive Elements Limits</Typography>
            
            <Grid container spacing={2}>
              <Grid item xs={12} md={4}>
                <TextField
                  fullWidth
                  label="Max Keyboard Buttons"
                  type="number"
                  value={configuration.limits?.maxKeyboardButtons || 100}
                  onChange={(e) => handleLimitChange('maxKeyboardButtons', parseInt(e.target.value))}
                  helperText="Maximum buttons in reply keyboard"
                  inputProps={{ min: 1, max: 100 }}
                />
              </Grid>
              
              <Grid item xs={12} md={4}>
                <TextField
                  fullWidth
                  label="Max Inline Keyboard Buttons"
                  type="number"
                  value={configuration.limits?.maxInlineKeyboardButtons || 100}
                  onChange={(e) => handleLimitChange('maxInlineKeyboardButtons', parseInt(e.target.value))}
                  helperText="Maximum buttons in inline keyboard"
                  inputProps={{ min: 1, max: 100 }}
                />
              </Grid>
              
              <Grid item xs={12} md={4}>
                <TextField
                  fullWidth
                  label="Max Callback Data Length"
                  type="number"
                  value={configuration.limits?.maxCallbackDataLength || 64}
                  onChange={(e) => handleLimitChange('maxCallbackDataLength', parseInt(e.target.value))}
                  helperText="Maximum callback data bytes"
                  inputProps={{ min: 1, max: 64 }}
                />
              </Grid>
            </Grid>
          </Paper>
        </Grid>
      </Grid>
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
          <Accordion>
            <AccordionSummary expandIcon={<ExpandMoreIcon />}>
              <Typography>Getting Started with Telegram Bot API</Typography>
            </AccordionSummary>
            <AccordionDetails>
              <Typography variant="body2" component="div">
                <ol>
                  <li>Create a bot with @BotFather on Telegram</li>
                  <li>Get your bot token and configure it here</li>
                  <li>Choose between webhooks (production) or polling (development)</li>
                  <li>Set up the features you want to use</li>
                  <li>Test your connection</li>
                  <li>Start receiving and sending messages!</li>
                </ol>
              </Typography>
            </AccordionDetails>
          </Accordion>

          <Accordion>
            <AccordionSummary expandIcon={<ExpandMoreIcon />}>
              <Typography>Webhook vs Polling</Typography>
            </AccordionSummary>
            <AccordionDetails>
              <Typography variant="body2" component="div">
                <strong>Webhooks (Recommended for Production):</strong>
                <ul>
                  <li>More efficient - Telegram sends updates to your server</li>
                  <li>Requires HTTPS URL with valid certificate</li>
                  <li>Lower latency</li>
                  <li>Can handle high message volumes</li>
                </ul>
                
                <strong>Long Polling (Good for Development):</strong>
                <ul>
                  <li>Simpler setup - no need for public HTTPS URL</li>
                  <li>Works behind firewalls and NAT</li>
                  <li>Higher latency</li>
                  <li>Less efficient for high volumes</li>
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
                  <li><strong>Customer Support:</strong> Automate responses, handle FAQs</li>
                  <li><strong>Notifications:</strong> Send alerts, updates, reminders</li>
                  <li><strong>Group Management:</strong> Moderate content, manage members</li>
                  <li><strong>E-commerce:</strong> Product catalogs, order tracking</li>
                  <li><strong>Content Distribution:</strong> Share news, media, updates</li>
                  <li><strong>Interactive Services:</strong> Polls, quizzes, games</li>
                  <li><strong>Workflow Automation:</strong> Integrate with other services</li>
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
                  <li>Always respect Telegram's rate limits</li>
                  <li>Handle errors gracefully with retries</li>
                  <li>Use inline keyboards for better UX</li>
                  <li>Implement proper command handling</li>
                  <li>Cache user and chat data when possible</li>
                  <li>Use markdown or HTML for rich text formatting</li>
                  <li>Test in groups and channels before production</li>
                  <li>Monitor webhook health regularly</li>
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
                <Link href="https://core.telegram.org/bots/api" target="_blank" rel="noopener">
                  Official Telegram Bot API Documentation
                </Link>
              </ListItem>
              <ListItem>
                <Link href="https://core.telegram.org/bots/features" target="_blank" rel="noopener">
                  Bot Features Overview
                </Link>
              </ListItem>
              <ListItem>
                <Link href="https://core.telegram.org/bots/webhooks" target="_blank" rel="noopener">
                  Webhook Setup Guide
                </Link>
              </ListItem>
              <ListItem>
                <Link href="https://core.telegram.org/bots/faq" target="_blank" rel="noopener">
                  Frequently Asked Questions
                </Link>
              </ListItem>
            </List>
          </Paper>
        </Grid>

        <Grid item xs={12}>
          <Alert severity="info" icon={<InfoIcon />}>
            <Typography variant="body2">
              <strong>Need more help?</strong> Check the Telegram Bot API documentation or join @BotDevelopment group for community support.
            </Typography>
          </Alert>
        </Grid>
      </Grid>
    </Box>
  );

  const tabs = [
    { label: 'Basic Configuration', icon: <BotIcon /> },
    { label: 'Webhook Settings', icon: <WebhookIcon /> },
    { label: 'Features', icon: <SettingsIcon /> },
    { label: 'Rate Limits', icon: <SpeedIcon /> },
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
        {activeTab === 1 && renderWebhookConfiguration()}
        {activeTab === 2 && renderFeaturesConfiguration()}
        {activeTab === 3 && renderRateLimitsConfiguration()}
        {activeTab === 4 && renderHelpDocumentation()}
      </Box>
    </Box>
  );
};

export default TelegramBotApiConfiguration;