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
import DeleteIcon from '@mui/icons-material/Delete';
import AddIcon from '@mui/icons-material/Add';
import EditIcon from '@mui/icons-material/Edit';

interface FacebookMessengerApiConfigurationProps {
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
      id={`messenger-tabpanel-${index}`}
      aria-labelledby={`messenger-tab-${index}`}
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

export const FacebookMessengerApiConfiguration: React.FC<FacebookMessengerApiConfigurationProps> = ({
  configuration,
  onChange,
}) => {
  const [tabValue, setTabValue] = useState(0);
  const [menuItemDialog, setMenuItemDialog] = useState(false);
  const [currentMenuItem, setCurrentMenuItem] = useState<any>(null);
  const [iceBreakersDialog, setIceBreakersDialog] = useState(false);
  const [currentIceBreaker, setCurrentIceBreaker] = useState<any>(null);
  const [personaDialog, setPersonaDialog] = useState(false);
  const [currentPersona, setCurrentPersona] = useState<any>(null);

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

  // Menu Item Management
  const handleAddMenuItem = () => {
    setCurrentMenuItem({ title: '', type: 'postback', payload: '' });
    setMenuItemDialog(true);
  };

  const handleEditMenuItem = (index: number) => {
    setCurrentMenuItem({ ...configuration.persistentMenu[index], index });
    setMenuItemDialog(true);
  };

  const handleDeleteMenuItem = (index: number) => {
    const newMenu = [...configuration.persistentMenu];
    newMenu.splice(index, 1);
    onChange({
      ...configuration,
      persistentMenu: newMenu,
    });
  };

  const handleSaveMenuItem = () => {
    const newMenu = [...(configuration.persistentMenu || [])];
    if (currentMenuItem.index !== undefined) {
      newMenu[currentMenuItem.index] = currentMenuItem;
    } else {
      newMenu.push(currentMenuItem);
    }
    onChange({
      ...configuration,
      persistentMenu: newMenu,
    });
    setMenuItemDialog(false);
    setCurrentMenuItem(null);
  };

  // Ice Breakers Management
  const handleAddIceBreaker = () => {
    setCurrentIceBreaker({ question: '', payload: '' });
    setIceBreakersDialog(true);
  };

  const handleEditIceBreaker = (index: number) => {
    setCurrentIceBreaker({ ...configuration.iceBreakers[index], index });
    setIceBreakersDialog(true);
  };

  const handleDeleteIceBreaker = (index: number) => {
    const newIceBreakers = [...configuration.iceBreakers];
    newIceBreakers.splice(index, 1);
    onChange({
      ...configuration,
      iceBreakers: newIceBreakers,
    });
  };

  const handleSaveIceBreaker = () => {
    const newIceBreakers = [...(configuration.iceBreakers || [])];
    if (currentIceBreaker.index !== undefined) {
      newIceBreakers[currentIceBreaker.index] = currentIceBreaker;
    } else {
      newIceBreakers.push(currentIceBreaker);
    }
    onChange({
      ...configuration,
      iceBreakers: newIceBreakers,
    });
    setIceBreakersDialog(false);
    setCurrentIceBreaker(null);
  };

  // Personas Management
  const handleAddPersona = () => {
    setCurrentPersona({ name: '', profilePictureUrl: '' });
    setPersonaDialog(true);
  };

  const handleEditPersona = (index: number) => {
    setCurrentPersona({ ...configuration.personas[index], index });
    setPersonaDialog(true);
  };

  const handleDeletePersona = (index: number) => {
    const newPersonas = [...configuration.personas];
    newPersonas.splice(index, 1);
    onChange({
      ...configuration,
      personas: newPersonas,
    });
  };

  const handleSavePersona = () => {
    const newPersonas = [...(configuration.personas || [])];
    if (currentPersona.index !== undefined) {
      newPersonas[currentPersona.index] = currentPersona;
    } else {
      newPersonas.push(currentPersona);
    }
    onChange({
      ...configuration,
      personas: newPersonas,
    });
    setPersonaDialog(false);
    setCurrentPersona(null);
  };

  return (
    <>
      <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
        <Tabs value={tabValue} onChange={handleTabChange} aria-label="Messenger API configuration tabs">
          <Tab label="Authentication" />
          <Tab label="Features" />
          <Tab label="Message Settings" />
          <Tab label="Bot Configuration" />
          <Tab label="Personas" />
          <Tab label="Insights" />
          <Tab label="Rate Limits" />
        </Tabs>
      </Box>

      <TabPanel value={tabValue} index={0}>
        <Grid container spacing={3}>
          <Grid item xs={12}>
            <Alert severity="info">
              Configure your Facebook Messenger Platform authentication credentials. You'll need a Page Access Token and App Secret from your Facebook App.
            </Alert>
          </Grid>

          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              label="Page ID"
              value={configuration.pageId || ''}
              onChange={handleChange('pageId')}
              helperText="Your Facebook Page ID"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              label="Page Access Token"
              type="password"
              value={configuration.pageAccessToken || ''}
              onChange={handleChange('pageAccessToken')}
              helperText="Long-lived page access token"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              label="App Secret"
              type="password"
              value={configuration.appSecret || ''}
              onChange={handleChange('appSecret')}
              helperText="Your Facebook App Secret for webhook verification"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              label="Verify Token"
              value={configuration.verifyToken || ''}
              onChange={handleChange('verifyToken')}
              helperText="Custom verify token for webhook setup"
            />
          </Grid>

          <Grid item xs={12}>
            <TextField
              fullWidth
              label="Webhook URL"
              value={configuration.webhookUrl || ''}
              onChange={handleChange('webhookUrl')}
              helperText="Your webhook endpoint URL"
            />
          </Grid>
        </Grid>
      </TabPanel>

      <TabPanel value={tabValue} index={1}>
        <Grid container spacing={3}>
          <Grid item xs={12}>
            <Typography variant="h6" gutterBottom>Messaging Features</Typography>
          </Grid>

          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableMessaging || false}
                  onChange={handleSwitchChange('features', 'enableMessaging')}
                />
              }
              label="Enable Messaging"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableChatbot || false}
                  onChange={handleSwitchChange('features', 'enableChatbot')}
                />
              }
              label="Enable Chatbot"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enablePersistentMenu || false}
                  onChange={handleSwitchChange('features', 'enablePersistentMenu')}
                />
              }
              label="Enable Persistent Menu"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableGetStarted || false}
                  onChange={handleSwitchChange('features', 'enableGetStarted')}
                />
              }
              label="Enable Get Started Button"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableGreeting || false}
                  onChange={handleSwitchChange('features', 'enableGreeting')}
                />
              }
              label="Enable Greeting Text"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableQuickReplies || false}
                  onChange={handleSwitchChange('features', 'enableQuickReplies')}
                />
              }
              label="Enable Quick Replies"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableTypingIndicator || false}
                  onChange={handleSwitchChange('features', 'enableTypingIndicator')}
                />
              }
              label="Enable Typing Indicator"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableMessageTemplates || false}
                  onChange={handleSwitchChange('features', 'enableMessageTemplates')}
                />
              }
              label="Enable Message Templates"
            />
          </Grid>

          <Grid item xs={12}>
            <Divider sx={{ my: 2 }} />
            <Typography variant="h6" gutterBottom>Advanced Features</Typography>
          </Grid>

          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableAttachments || false}
                  onChange={handleSwitchChange('features', 'enableAttachments')}
                />
              }
              label="Enable File Attachments"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableBroadcasting || false}
                  onChange={handleSwitchChange('features', 'enableBroadcasting')}
                />
              }
              label="Enable Broadcasting"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableCustomerMatching || false}
                  onChange={handleSwitchChange('features', 'enableCustomerMatching')}
                />
              }
              label="Enable Customer Matching"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableHandover || false}
                  onChange={handleSwitchChange('features', 'enableHandover')}
                />
              }
              label="Enable Handover Protocol"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enablePersonas || false}
                  onChange={handleSwitchChange('features', 'enablePersonas')}
                />
              }
              label="Enable Personas"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableNaturalLanguageProcessing || false}
                  onChange={handleSwitchChange('features', 'enableNaturalLanguageProcessing')}
                />
              }
              label="Enable NLP"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableMessageInsights || false}
                  onChange={handleSwitchChange('features', 'enableMessageInsights')}
                />
              }
              label="Enable Message Insights"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableMessageTags || false}
                  onChange={handleSwitchChange('features', 'enableMessageTags')}
                />
              }
              label="Enable Message Tags"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableSponsoredMessages || false}
                  onChange={handleSwitchChange('features', 'enableSponsoredMessages')}
                />
              }
              label="Enable Sponsored Messages"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enablePrivateReplies || false}
                  onChange={handleSwitchChange('features', 'enablePrivateReplies')}
                />
              }
              label="Enable Private Replies"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableMessageReactions || false}
                  onChange={handleSwitchChange('features', 'enableMessageReactions')}
                />
              }
              label="Enable Message Reactions"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.features?.enableIceBreakers || false}
                  onChange={handleSwitchChange('features', 'enableIceBreakers')}
                />
              }
              label="Enable Ice Breakers"
            />
          </Grid>
        </Grid>
      </TabPanel>

      <TabPanel value={tabValue} index={2}>
        <Grid container spacing={3}>
          <Grid item xs={12}>
            <Typography variant="h6" gutterBottom>Message Settings</Typography>
          </Grid>

          <Grid item xs={12} md={6}>
            <FormControl fullWidth>
              <InputLabel>Default Messaging Type</InputLabel>
              <Select
                value={configuration.messagingType || 'RESPONSE'}
                onChange={handleSelectChange('messagingType')}
                label="Default Messaging Type"
              >
                <MenuItem value="RESPONSE">Response</MenuItem>
                <MenuItem value="UPDATE">Update</MenuItem>
                <MenuItem value="MESSAGE_TAG">Message Tag</MenuItem>
              </Select>
            </FormControl>
          </Grid>

          <Grid item xs={12} md={6}>
            <FormControl fullWidth>
              <InputLabel>Default Notification Type</InputLabel>
              <Select
                value={configuration.notificationType || 'REGULAR'}
                onChange={handleSelectChange('notificationType')}
                label="Default Notification Type"
              >
                <MenuItem value="REGULAR">Regular</MenuItem>
                <MenuItem value="SILENT_PUSH">Silent Push</MenuItem>
                <MenuItem value="NO_PUSH">No Push</MenuItem>
              </Select>
            </FormControl>
          </Grid>

          <Grid item xs={12}>
            <TextField
              fullWidth
              multiline
              rows={3}
              label="Greeting Text"
              value={configuration.greetingText || ''}
              onChange={handleChange('greetingText')}
              helperText="Message shown to new users"
            />
          </Grid>

          <Grid item xs={12}>
            <TextField
              fullWidth
              label="Get Started Payload"
              value={configuration.getStartedPayload || ''}
              onChange={handleChange('getStartedPayload')}
              helperText="Payload sent when user clicks Get Started"
            />
          </Grid>

          <Grid item xs={12}>
            <Typography variant="h6" gutterBottom>Message Limits</Typography>
          </Grid>

          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              type="number"
              label="Max Message Length"
              value={configuration.limits?.maxMessageLength || 2000}
              onChange={handleNumberChange('limits', 'maxMessageLength')}
              helperText="Maximum characters per message"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              type="number"
              label="Max Quick Replies"
              value={configuration.limits?.maxQuickReplies || 13}
              onChange={handleNumberChange('limits', 'maxQuickReplies')}
              helperText="Maximum quick reply buttons"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              type="number"
              label="Max Buttons per Template"
              value={configuration.limits?.maxButtonsPerTemplate || 3}
              onChange={handleNumberChange('limits', 'maxButtonsPerTemplate')}
              helperText="Maximum buttons in a template"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              type="number"
              label="Max Carousel Elements"
              value={configuration.limits?.maxElementsPerCarousel || 10}
              onChange={handleNumberChange('limits', 'maxElementsPerCarousel')}
              helperText="Maximum elements in carousel"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              type="number"
              label="Max Attachment Size (MB)"
              value={configuration.limits?.maxAttachmentSizeMB || 25}
              onChange={handleNumberChange('limits', 'maxAttachmentSizeMB')}
              helperText="Maximum file size for attachments"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              type="number"
              label="Messaging Window (hours)"
              value={configuration.limits?.messagingWindowHours || 24}
              onChange={handleNumberChange('limits', 'messagingWindowHours')}
              helperText="Time window for standard messages"
            />
          </Grid>
        </Grid>
      </TabPanel>

      <TabPanel value={tabValue} index={3}>
        <Grid container spacing={3}>
          <Grid item xs={12}>
            <Paper elevation={2} sx={{ p: 2, mb: 3 }}>
              <Typography variant="h6" gutterBottom>
                Persistent Menu
              </Typography>
              <List>
                {configuration.persistentMenu?.map((item: any, index: number) => (
                  <ListItem key={index}>
                    <ListItemText
                      primary={item.title}
                      secondary={`Type: ${item.type} | Payload: ${item.payload}`}
                    />
                    <ListItemSecondaryAction>
                      <IconButton edge="end" onClick={() => handleEditMenuItem(index)}>
                        <EditIcon />
                      </IconButton>
                      <IconButton edge="end" onClick={() => handleDeleteMenuItem(index)}>
                        <DeleteIcon />
                      </IconButton>
                    </ListItemSecondaryAction>
                  </ListItem>
                ))}
              </List>
              <Button
                variant="outlined"
                startIcon={<AddIcon />}
                onClick={handleAddMenuItem}
                disabled={(configuration.persistentMenu?.length || 0) >= (configuration.limits?.maxPersistentMenuItems || 3)}
              >
                Add Menu Item
              </Button>
            </Paper>
          </Grid>

          <Grid item xs={12}>
            <Paper elevation={2} sx={{ p: 2 }}>
              <Typography variant="h6" gutterBottom>
                Ice Breakers
              </Typography>
              <List>
                {configuration.iceBreakers?.map((item: any, index: number) => (
                  <ListItem key={index}>
                    <ListItemText
                      primary={item.question}
                      secondary={`Payload: ${item.payload}`}
                    />
                    <ListItemSecondaryAction>
                      <IconButton edge="end" onClick={() => handleEditIceBreaker(index)}>
                        <EditIcon />
                      </IconButton>
                      <IconButton edge="end" onClick={() => handleDeleteIceBreaker(index)}>
                        <DeleteIcon />
                      </IconButton>
                    </ListItemSecondaryAction>
                  </ListItem>
                ))}
              </List>
              <Button
                variant="outlined"
                startIcon={<AddIcon />}
                onClick={handleAddIceBreaker}
                disabled={(configuration.iceBreakers?.length || 0) >= (configuration.limits?.maxIceBreakers || 4)}
              >
                Add Ice Breaker
              </Button>
            </Paper>
          </Grid>

          <Grid item xs={12}>
            <Typography variant="h6" gutterBottom>Bot Settings</Typography>
          </Grid>

          <Grid item xs={12} md={6}>
            <FormControl fullWidth>
              <InputLabel>Primary Receiver App ID</InputLabel>
              <Select
                value={configuration.primaryReceiverAppId || ''}
                onChange={handleSelectChange('primaryReceiverAppId')}
                label="Primary Receiver App ID"
              >
                <MenuItem value="">Default (Page Inbox)</MenuItem>
                <MenuItem value="263902037430900">Page Inbox</MenuItem>
                <MenuItem value="custom">Custom App ID</MenuItem>
              </Select>
            </FormControl>
          </Grid>

          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              label="Custom App ID"
              value={configuration.customAppId || ''}
              onChange={handleChange('customAppId')}
              disabled={configuration.primaryReceiverAppId !== 'custom'}
              helperText="Enter custom app ID if selected above"
            />
          </Grid>

          <Grid item xs={12}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.enableAutomaticHandover || false}
                  onChange={(e) => onChange({ ...configuration, enableAutomaticHandover: e.target.checked })}
                />
              }
              label="Enable Automatic Handover"
            />
          </Grid>
        </Grid>
      </TabPanel>

      <TabPanel value={tabValue} index={4}>
        <Grid container spacing={3}>
          <Grid item xs={12}>
            <Paper elevation={2} sx={{ p: 2 }}>
              <Typography variant="h6" gutterBottom>
                Personas
              </Typography>
              <Typography variant="body2" color="text.secondary" gutterBottom>
                Create personas to represent different agents or departments in your conversations.
              </Typography>
              <List>
                {configuration.personas?.map((persona: any, index: number) => (
                  <ListItem key={index}>
                    <ListItemText
                      primary={persona.name}
                      secondary={persona.profilePictureUrl}
                    />
                    <ListItemSecondaryAction>
                      <IconButton edge="end" onClick={() => handleEditPersona(index)}>
                        <EditIcon />
                      </IconButton>
                      <IconButton edge="end" onClick={() => handleDeletePersona(index)}>
                        <DeleteIcon />
                      </IconButton>
                    </ListItemSecondaryAction>
                  </ListItem>
                ))}
              </List>
              <Button
                variant="outlined"
                startIcon={<AddIcon />}
                onClick={handleAddPersona}
                disabled={(configuration.personas?.length || 0) >= (configuration.limits?.maxPersonas || 5)}
              >
                Add Persona
              </Button>
            </Paper>
          </Grid>
        </Grid>
      </TabPanel>

      <TabPanel value={tabValue} index={5}>
        <Grid container spacing={3}>
          <Grid item xs={12}>
            <Typography variant="h6" gutterBottom>Insights Configuration</Typography>
          </Grid>

          <Grid item xs={12}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.enableInsights || false}
                  onChange={(e) => onChange({ ...configuration, enableInsights: e.target.checked })}
                />
              }
              label="Enable Insights Collection"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <FormControl fullWidth>
              <InputLabel>Insights Period</InputLabel>
              <Select
                value={configuration.insightsPeriod || 'day'}
                onChange={handleSelectChange('insightsPeriod')}
                label="Insights Period"
              >
                <MenuItem value="day">Daily</MenuItem>
                <MenuItem value="week">Weekly</MenuItem>
                <MenuItem value="days_28">28 Days</MenuItem>
              </Select>
            </FormControl>
          </Grid>

          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              type="number"
              label="Insights Polling Interval (minutes)"
              value={configuration.insightsPollingInterval || 60}
              onChange={(e) => onChange({ ...configuration, insightsPollingInterval: parseInt(e.target.value) })}
              helperText="How often to poll for insights"
            />
          </Grid>

          <Grid item xs={12}>
            <Typography variant="subtitle1" gutterBottom>Tracked Metrics</Typography>
            {[
              'Total Messaging Connections',
              'New Messaging Connections',
              'Blocked Conversations',
              'Reported Conversations',
              'Feedback by Action Type'
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
        </Grid>
      </TabPanel>

      <TabPanel value={tabValue} index={6}>
        <Grid container spacing={3}>
          <Grid item xs={12}>
            <Typography variant="h6" gutterBottom>Rate Limiting</Typography>
          </Grid>

          <Grid item xs={12} md={6}>
            <Typography gutterBottom>API Calls per Minute</Typography>
            <Slider
              value={configuration.limits?.rateLimitPerMinute || 200}
              onChange={handleSliderChange('limits', 'rateLimitPerMinute')}
              aria-labelledby="rate-limit-slider"
              valueLabelDisplay="auto"
              min={10}
              max={1000}
              step={10}
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <Typography gutterBottom>Burst Limit</Typography>
            <Slider
              value={configuration.limits?.burstLimit || 50}
              onChange={handleSliderChange('limits', 'burstLimit')}
              aria-labelledby="burst-limit-slider"
              valueLabelDisplay="auto"
              min={10}
              max={200}
              step={5}
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              type="number"
              label="Max Broadcast Recipients"
              value={configuration.limits?.maxBroadcastRecipients || 10000}
              onChange={handleNumberChange('limits', 'maxBroadcastRecipients')}
              helperText="Maximum recipients per broadcast"
            />
          </Grid>

          <Grid item xs={12} md={6}>
            <TextField
              fullWidth
              type="number"
              label="Max Templates per Account"
              value={configuration.limits?.maxTemplatesPerAccount || 250}
              onChange={handleNumberChange('limits', 'maxTemplatesPerAccount')}
              helperText="Maximum message templates allowed"
            />
          </Grid>

          <Grid item xs={12}>
            <FormControlLabel
              control={
                <Switch
                  checked={configuration.enableRateLimitAutoRetry || false}
                  onChange={(e) => onChange({ ...configuration, enableRateLimitAutoRetry: e.target.checked })}
                />
              }
              label="Auto-retry on Rate Limit"
            />
          </Grid>

          <Grid item xs={12}>
            <Alert severity="warning">
              Facebook applies rate limits to prevent abuse. Configure these settings to match your approved limits.
            </Alert>
          </Grid>
        </Grid>
      </TabPanel>

      {/* Menu Item Dialog */}
      <Dialog open={menuItemDialog} onClose={() => setMenuItemDialog(false)}>
        <DialogTitle>{currentMenuItem?.index !== undefined ? 'Edit' : 'Add'} Menu Item</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Title"
                value={currentMenuItem?.title || ''}
                onChange={(e) => setCurrentMenuItem({ ...currentMenuItem, title: e.target.value })}
              />
            </Grid>
            <Grid item xs={12}>
              <FormControl fullWidth>
                <InputLabel>Type</InputLabel>
                <Select
                  value={currentMenuItem?.type || 'postback'}
                  onChange={(e) => setCurrentMenuItem({ ...currentMenuItem, type: e.target.value })}
                  label="Type"
                >
                  <MenuItem value="postback">Postback</MenuItem>
                  <MenuItem value="web_url">Web URL</MenuItem>
                  <MenuItem value="nested">Nested</MenuItem>
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label={currentMenuItem?.type === 'web_url' ? 'URL' : 'Payload'}
                value={currentMenuItem?.payload || ''}
                onChange={(e) => setCurrentMenuItem({ ...currentMenuItem, payload: e.target.value })}
              />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setMenuItemDialog(false)}>Cancel</Button>
          <Button onClick={handleSaveMenuItem} variant="contained">Save</Button>
        </DialogActions>
      </Dialog>

      {/* Ice Breaker Dialog */}
      <Dialog open={iceBreakersDialog} onClose={() => setIceBreakersDialog(false)}>
        <DialogTitle>{currentIceBreaker?.index !== undefined ? 'Edit' : 'Add'} Ice Breaker</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Question"
                value={currentIceBreaker?.question || ''}
                onChange={(e) => setCurrentIceBreaker({ ...currentIceBreaker, question: e.target.value })}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Payload"
                value={currentIceBreaker?.payload || ''}
                onChange={(e) => setCurrentIceBreaker({ ...currentIceBreaker, payload: e.target.value })}
              />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setIceBreakersDialog(false)}>Cancel</Button>
          <Button onClick={handleSaveIceBreaker} variant="contained">Save</Button>
        </DialogActions>
      </Dialog>

      {/* Persona Dialog */}
      <Dialog open={personaDialog} onClose={() => setPersonaDialog(false)}>
        <DialogTitle>{currentPersona?.index !== undefined ? 'Edit' : 'Add'} Persona</DialogTitle>
        <DialogContent>
          <Grid container spacing={2} sx={{ mt: 1 }}>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Name"
                value={currentPersona?.name || ''}
                onChange={(e) => setCurrentPersona({ ...currentPersona, name: e.target.value })}
              />
            </Grid>
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Profile Picture URL"
                value={currentPersona?.profilePictureUrl || ''}
                onChange={(e) => setCurrentPersona({ ...currentPersona, profilePictureUrl: e.target.value })}
              />
            </Grid>
          </Grid>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setPersonaDialog(false)}>Cancel</Button>
          <Button onClick={handleSavePersona} variant="contained">Save</Button>
        </DialogActions>
      </Dialog>
    </>
  );
};