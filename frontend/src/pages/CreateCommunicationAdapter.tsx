import { useState, useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import { useNavigationHistory } from '@/hooks/useNavigationHistory';
import { BusinessComponent } from '@/types/businessComponent';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Combobox } from '@/components/ui/combobox';
import { Badge } from '@/components/ui/badge';
import { Separator } from '@/components/ui/separator';
import { Checkbox } from '@/components/ui/checkbox';
import { PasswordConfirmation } from '@/components/ui/password-confirmation';
import { JarSelector } from '@/components/JarSelector';
import { BusinessComponentSelectionAdapterCard } from '@/components/adapter/BusinessComponentSelectionAdapterCard';
import { FieldMappingScreen } from '@/components/FieldMappingScreen';
import { FtpAdapterConfiguration } from '@/components/adapter/FtpAdapterConfiguration';
import { SftpAdapterConfiguration } from '@/components/adapter/SftpAdapterConfiguration';
import { FileAdapterConfiguration } from '@/components/adapter/FileAdapterConfiguration';
import { HttpInboundAdapterConfiguration } from '@/components/adapter/HttpInboundAdapterConfiguration';
import { HttpOutboundAdapterConfiguration } from '@/components/adapter/HttpOutboundAdapterConfiguration';
import { IdocInboundAdapterConfiguration } from '@/components/adapter/IdocInboundAdapterConfiguration';
import { IdocOutboundAdapterConfiguration } from '@/components/adapter/IdocOutboundAdapterConfiguration';
import { IbmmqInboundAdapterConfiguration } from '@/components/adapter/IbmmqInboundAdapterConfiguration';
import { IbmmqOutboundAdapterConfiguration } from '@/components/adapter/IbmmqOutboundAdapterConfiguration';
import { RestInboundAdapterConfiguration } from '@/components/adapter/RestInboundAdapterConfiguration';
import { RestOutboundAdapterConfiguration } from '@/components/adapter/RestOutboundAdapterConfiguration';
import { SoapInboundAdapterConfiguration } from '@/components/adapter/SoapInboundAdapterConfiguration';
import { SoapOutboundAdapterConfiguration } from '@/components/adapter/SoapOutboundAdapterConfiguration';
import { OdataOutboundAdapterConfiguration } from '@/components/adapter/OdataOutboundAdapterConfiguration';
import { OdataInboundAdapterConfiguration } from '@/components/adapter/OdataInboundAdapterConfiguration';
import { RfcOutboundAdapterConfiguration } from '@/components/adapter/RfcOutboundAdapterConfiguration';
import { RfcInboundAdapterConfiguration } from '@/components/adapter/RfcInboundAdapterConfiguration';
import { MailOutboundAdapterConfiguration } from '@/components/adapter/MailOutboundAdapterConfiguration';
import { MailInboundAdapterConfiguration } from '@/components/adapter/MailInboundAdapterConfiguration';
import { FacebookGraphApiConfiguration } from '@/components/adapter/FacebookGraphApiConfiguration';
import { FacebookAdsApiConfiguration } from '@/components/adapter/FacebookAdsApiConfiguration';
import { InstagramGraphApiConfiguration } from '@/components/adapter/InstagramGraphApiConfiguration';
import { WhatsAppBusinessApiConfiguration } from '@/components/adapter/WhatsAppBusinessApiConfiguration';
import TwitterApiV2Configuration from '@/components/adapters/configurations/TwitterApiV2Configuration';
import TwitterAdsApiConfiguration from '@/components/adapters/configurations/TwitterAdsApiConfiguration';
import LinkedInApiConfiguration from '@/components/adapters/configurations/LinkedInApiConfiguration';
import { LinkedInAdsApiConfiguration } from '@/components/adapters/configurations/LinkedInAdsApiConfiguration';
import { YouTubeDataApiConfiguration } from '@/components/adapters/configurations/YouTubeDataApiConfiguration';
import YouTubeAnalyticsApiConfiguration from '@/components/adapters/configurations/YouTubeAnalyticsApiConfiguration';
import TikTokBusinessApiConfiguration from '@/components/adapters/configurations/TikTokBusinessApiConfiguration';
import TikTokContentApiConfiguration from '@/components/adapters/configurations/TikTokContentApiConfiguration';
import { FacebookMessengerApiConfiguration } from '@/components/adapters/configurations/FacebookMessengerApiConfiguration';
import { PinterestApiConfiguration } from '@/components/adapters/configurations/PinterestApiConfiguration';
import { RedditApiConfiguration } from '@/components/adapters/configurations/RedditApiConfiguration';
import { SnapchatAdsApiConfiguration } from '@/components/adapters/configurations/SnapchatAdsApiConfiguration';
import { DiscordApiConfiguration } from '@/components/adapters/configurations/DiscordApiConfiguration';
import TelegramBotApiConfiguration from '@/components/adapters/social/TelegramBotApiConfiguration';
import SlackApiConfiguration from '@/components/adapters/collaboration/SlackApiConfiguration';
import { useToast } from '@/hooks/use-toast';
import { adapterService } from '@/services/adapter';
import { AdapterValidationDialog } from '@/components/adapter/AdapterValidationDialog';
import {
 Mail,
 Smartphone,
 Webhook,
 MessageCircle,
 Phone,
 Globe,
 Settings,
 TestTube,
 Save,
 Play,
 CheckCircle,
 AlertCircle,
 Info,
 Zap,
 Send,
 Shield,
 Key,
 FileText,
 HardDrive,
 Lock,
 Network,
 ShieldCheck,
 Database,
 FileCode,
 MessageSquare,
 Layers,
 FileSpreadsheet,
 Cable,
 Cloud,
 Server,
 Activity,
 ArrowLeftRight,
 Facebook,
 Instagram,
 Twitter,
 Linkedin,
 Youtube,
 Send,
 Users,
 Radio,
 Share2,
 Repeat
} from 'lucide-react';
import { RestrictedPage } from '@/components/common/RestrictedPage';
import { logger, LogCategory } from '@/lib/logger';

interface AdapterField {
 name: string;
 label: string;
 type: string;
 required: boolean;
 placeholder?: string;
 options?: string[];
 conditionalField?: string;
 driverTypeFilter?: string;
 parentField?: string;
 parentValue?: string;
}

interface CommunicationAdapter {
 id: string;
 name: string;
 icon: any;
 description: string;
 category: string;
 fields: AdapterField[];
}

const communicationAdapters: CommunicationAdapter[] = [
 {
 id: 'sms',
 name: 'SMS',
 icon: Smartphone,
 description: 'Send and receive SMS messages via various providers',
 category: 'Messaging',
 fields: [
 { name: 'provider', label: 'SMS Provider', type: 'select', required: true, options: ['Twilio', 'Vonage/Nexmo', 'AWS SNS', 'MessageBird', 'Clickatell', 'Plivo', 'Infobip'] },
 { name: 'accountId', label: 'Account ID/SID', type: 'text', required: true, placeholder: 'Your account identifier' },
 { name: 'authToken', label: 'Auth Token', type: 'password', required: true, placeholder: 'Your authentication token' },
 { name: 'apiKey', label: 'API Key', type: 'text', required: false, placeholder: 'API key (if required)' },
 { name: 'apiSecret', label: 'API Secret', type: 'password', required: false, placeholder: 'API secret (if required)' },
 { name: 'defaultSenderNumber', label: 'Default Sender Number', type: 'text', required: true, placeholder: '+1234567890' },
 { name: 'webhookUrl', label: 'Webhook URL', type: 'text', required: false, placeholder: 'https://your-domain.com/sms/webhook' },
 { name: 'enableDeliveryReceipts', label: 'Enable Delivery Receipts', type: 'checkbox', required: false },
 { name: 'enableIncoming', label: 'Enable Incoming Messages', type: 'checkbox', required: false }
 ]
 },
 {
 id: 'rabbitmq',
 name: 'RabbitMQ',
 icon: Share2,
 description: 'Advanced message queuing protocol broker',
 category: 'Messaging',
 fields: [
 { name: 'host', label: 'Host', type: 'text', required: true, placeholder: 'localhost or rabbitmq.example.com' },
 { name: 'port', label: 'Port', type: 'number', required: true, placeholder: '5672' },
 { name: 'virtualHost', label: 'Virtual Host', type: 'text', required: false, placeholder: '/ (default)' },
 { name: 'username', label: 'Username', type: 'text', required: true, placeholder: 'guest' },
 { name: 'password', label: 'Password', type: 'password', required: true, placeholder: 'RabbitMQ password' },
 { name: 'exchangeName', label: 'Exchange Name', type: 'text', required: false, placeholder: 'my-exchange' },
 { name: 'exchangeType', label: 'Exchange Type', type: 'select', required: false, options: ['direct', 'topic', 'fanout', 'headers'] },
 { name: 'queueName', label: 'Queue Name', type: 'text', required: false, placeholder: 'my-queue' },
 { name: 'routingKey', label: 'Routing Key', type: 'text', required: false, placeholder: 'routing.key' },
 { name: 'durable', label: 'Durable', type: 'checkbox', required: false },
 { name: 'autoAck', label: 'Auto Acknowledge', type: 'checkbox', required: false },
 { name: 'enableSSL', label: 'Enable SSL/TLS', type: 'checkbox', required: false },
 { name: 'prefetchCount', label: 'Prefetch Count', type: 'number', required: false, placeholder: '1' }
 ]
 },
 {
 id: 'amqp',
 name: 'AMQP',
 icon: Repeat,
 description: 'Advanced Message Queuing Protocol (AMQP 1.0)',
 category: 'Messaging',
 fields: [
 { name: 'host', label: 'Host', type: 'text', required: true, placeholder: 'amqp.example.com' },
 { name: 'port', label: 'Port', type: 'number', required: true, placeholder: '5672' },
 { name: 'username', label: 'Username', type: 'text', required: false, placeholder: 'Username' },
 { name: 'password', label: 'Password', type: 'password', required: false, placeholder: 'Password' },
 { name: 'connectionUrl', label: 'Connection URL', type: 'text', required: false, placeholder: 'amqp://user:pass@host:port' },
 { name: 'sourceAddress', label: 'Source Address', type: 'text', required: false, placeholder: 'queue://my-queue', conditionalField: 'sender' },
 { name: 'targetAddress', label: 'Target Address', type: 'text', required: false, placeholder: 'queue://target-queue', conditionalField: 'receiver' },
 { name: 'brokerType', label: 'Broker Type', type: 'select', required: false, options: ['Generic', 'ActiveMQ Artemis', 'Azure Service Bus', 'Qpid', 'Solace'] },
 { name: 'saslMechanism', label: 'SASL Mechanism', type: 'select', required: false, options: ['PLAIN', 'ANONYMOUS', 'EXTERNAL', 'SCRAM-SHA-256'] },
 { name: 'enableSSL', label: 'Enable SSL/TLS', type: 'checkbox', required: false },
 { name: 'enableTransactions', label: 'Enable Transactions', type: 'checkbox', required: false },
 { name: 'maxFrameSize', label: 'Max Frame Size', type: 'number', required: false, placeholder: '65536' }
 ]
 },
 {
 id: 'file',
 name: 'FILE',
 icon: FileText,
 description: 'File system operations and transfers',
 category: 'File System',
 fields: [
 { name: 'basePath', label: 'Base Path', type: 'text', required: true, placeholder: '/data/files' },
 { name: 'encoding', label: 'File Encoding', type: 'select', required: false, options: ['UTF-8', 'ASCII', 'ISO-8859-1', 'UTF-16'] },
 { name: 'permissions', label: 'File Permissions', type: 'text', required: false, placeholder: '755' },
 { name: 'createDirs', label: 'Create Directories', type: 'select', required: false, options: ['true', 'false'] }
 ]
 },
 {
 id: 'ftp',
 name: 'FTP',
 icon: HardDrive,
 description: 'File Transfer Protocol connections',
 category: 'File Transfer',
 fields: [
 { name: 'host', label: 'FTP Host', type: 'text', required: true, placeholder: 'ftp.example.com' },
 { name: 'port', label: 'Port', type: 'number', required: false, placeholder: '21' },
 { name: 'username', label: 'Username', type: 'text', required: true, placeholder: 'ftpuser' },
 { name: 'password', label: 'Password', type: 'password', required: true, placeholder: 'FTP Password' },
 { name: 'passiveMode', label: 'Passive Mode', type: 'select', required: false, options: ['true', 'false'] },
 { name: 'transferMode', label: 'Transfer Mode', type: 'select', required: false, options: ['binary', 'ascii'] },
 { name: 'authType', label: 'Authentication', type: 'select', required: false, options: ['None', 'Basic Auth', 'Bearer Token', 'API Key', 'OAuth', 'OAuth 2.0', 'SSL Certificate'] }
 ]
 },
 {
 id: 'sftp',
 name: 'SFTP',
 icon: Lock,
 description: 'Secure File Transfer Protocol',
 category: 'File Transfer',
 fields: [
 { name: 'host', label: 'SFTP Host', type: 'text', required: true, placeholder: 'sftp.example.com' },
 { name: 'port', label: 'Port', type: 'number', required: false, placeholder: '22' },
 { name: 'username', label: 'Username', type: 'text', required: true, placeholder: 'sftpuser' },
 { name: 'password', label: 'Password', type: 'password', required: false, placeholder: 'SFTP Password' },
 { name: 'privateKey', label: 'Private Key Path', type: 'text', required: false, placeholder: '/path/to/private/key' },
 { name: 'keyPassphrase', label: 'Key Passphrase', type: 'password', required: false, placeholder: 'Key passphrase' },
 { name: 'authType', label: 'Authentication', type: 'select', required: false, options: ['None', 'Basic Auth', 'Bearer Token', 'API Key', 'OAuth', 'OAuth 2.0', 'SSL Certificate'] }
 ]
 },
 {
 id: 'http',
 name: 'HTTP',
 icon: Network,
 description: 'HTTP/HTTPS protocol connections',
 category: 'HTTP',
 fields: [
 { name: 'protocol', label: 'Protocol', type: 'select', required: true, options: ['HTTP', 'HTTPS'] },
 { name: 'url', label: 'URL', type: 'text', required: true, placeholder: 'https://api.example.com/endpoint' },
 { name: 'method', label: 'HTTP Method', type: 'select', required: true, options: ['POST', 'PUT', 'PATCH', 'GET'] },
 { name: 'contentType', label: 'Content Type', type: 'select', required: true, options: ['application/json', 'application/xml', 'application/x-www-form-urlencoded'] },
 { name: 'authType', label: 'Authentication', type: 'select', required: false, options: ['None', 'Basic Auth', 'Bearer Token', 'API Key', 'OAuth', 'OAuth 2.0', 'SSL Certificate'] },
 { name: 'authValue', label: 'Auth Value', type: 'password', required: false, placeholder: 'Token or credentials' },
 { name: 'timeout', label: 'Timeout (ms)', type: 'number', required: false, placeholder: '30000' }
 ]
 },
 {
 id: 'rest',
 name: 'REST',
 icon: Globe,
 description: 'REST API integrations',
 category: 'API',
 fields: [
 { name: 'url', label: 'REST API URL', type: 'text', required: true, placeholder: 'https://api.example.com/v1/endpoint' },
 { name: 'method', label: 'HTTP Method', type: 'select', required: true, options: ['POST', 'PUT', 'PATCH', 'GET'] },
 { name: 'contentType', label: 'Content Type', type: 'select', required: true, options: ['application/json', 'application/xml', 'application/x-www-form-urlencoded'] },
 { name: 'authType', label: 'Authentication', type: 'select', required: false, options: ['None', 'Basic Auth', 'Bearer Token', 'API Key', 'OAuth', 'OAuth 2.0', 'SSL Certificate'] },
 { name: 'authValue', label: 'Auth Value', type: 'password', required: false, placeholder: 'Token or credentials' },
 { name: 'timeout', label: 'Timeout (ms)', type: 'number', required: false, placeholder: '30000' }
 ]
 },
 {
 id: 'soap',
 name: 'SOAP',
 icon: FileCode,
 description: 'SOAP web services integration',
 category: 'Web Services',
 fields: [
 // Inbound Fields (show when mode is 'sender') - Only Address
 { name: 'senderAddress', label: 'Address', type: 'text', required: true, placeholder: '/UtilitiesDeviceERPSmartMeterRegisterBulkCreateConfirmation', conditionalField: 'sender' },

 // Outbound Fields (show when mode is 'receiver')
 { name: 'receiverAddress', label: 'Target Address', type: 'text', required: true, placeholder: 'https://api.example.com/soap', conditionalField: 'receiver' },
 { name: 'wsdl', label: 'WSDL', type: 'select', required: true, options: ['Select WSDL...', 'UserService.wsdl', 'PaymentService.wsdl', 'NotificationService.wsdl'], conditionalField: 'receiver' },
 { name: 'receiverAuthentication', label: 'Authentication', type: 'select', required: true, options: ['None', 'Basic', 'Client Certificate', 'OAuth', 'OAuth 2'], conditionalField: 'receiver' },

 // Authentication conditional fields
 { name: 'username', label: 'Username', type: 'text', required: true, placeholder: 'Enter username', conditionalField: 'receiver', parentField: 'receiverAuthentication', parentValue: 'Basic' },
 { name: 'password', label: 'Password', type: 'password', required: true, placeholder: 'Enter password', conditionalField: 'receiver', parentField: 'receiverAuthentication', parentValue: 'Basic' },

 { name: 'clientCertificate', label: 'Client Certificate', type: 'certificate-dropdown', required: true, conditionalField: 'receiver', parentField: 'receiverAuthentication', parentValue: 'Client Certificate' },

 { name: 'oauthClientId', label: 'Client ID', type: 'text', required: true, placeholder: 'Enter OAuth client ID', conditionalField: 'receiver', parentField: 'receiverAuthentication', parentValue: 'OAuth' },
 { name: 'oauthClientSecret', label: 'Client Secret', type: 'password', required: true, placeholder: 'Enter OAuth client secret', conditionalField: 'receiver', parentField: 'receiverAuthentication', parentValue: 'OAuth' },
 { name: 'oauthTokenUrl', label: 'Token URL', type: 'text', required: true, placeholder: 'https://auth.example.com/oauth/token', conditionalField: 'receiver', parentField: 'receiverAuthentication', parentValue: 'OAuth' },

 { name: 'oauth2ClientId', label: 'Client ID', type: 'text', required: true, placeholder: 'Enter OAuth 2 client ID', conditionalField: 'receiver', parentField: 'receiverAuthentication', parentValue: 'OAuth 2' },
 { name: 'oauth2ClientSecret', label: 'Client Secret', type: 'password', required: true, placeholder: 'Enter OAuth 2 client secret', conditionalField: 'receiver', parentField: 'receiverAuthentication', parentValue: 'OAuth 2' },
 { name: 'oauth2TokenUrl', label: 'Token URL', type: 'text', required: true, placeholder: 'https://auth.example.com/oauth2/token', conditionalField: 'receiver', parentField: 'receiverAuthentication', parentValue: 'OAuth 2' },
 { name: 'oauth2Scope', label: 'Scope', type: 'text', required: false, placeholder: 'read write', conditionalField: 'receiver', parentField: 'receiverAuthentication', parentValue: 'OAuth 2' },

 { name: 'receiverTimeout', label: 'Timeout (seconds)', type: 'number', required: false, placeholder: '30', conditionalField: 'receiver' },
 { name: 'connectionType', label: 'Connection Type', type: 'select', required: true, options: ['Synchronous', 'Asynchronous'], conditionalField: 'receiver' }
 ]
 },
 {
 id: 'ibmmq',
 name: 'IBM MQ',
 icon: MessageSquare,
 description: 'IBM MQ (formerly WebSphere MQ) connections',
 category: 'Messaging',
 fields: [
 { name: 'driverJar', label: 'IBM MQ Driver JAR', type: 'jar-selector', required: true, driverTypeFilter: 'Message Queue' },
 { name: 'brokerUrl', label: 'Broker URL', type: 'text', required: true, placeholder: 'tcp://localhost:61616' },
 { name: 'queueName', label: 'Queue Name', type: 'text', required: false, placeholder: 'myqueue' },
 { name: 'topicName', label: 'Topic Name', type: 'text', required: false, placeholder: 'mytopic' },
 { name: 'username', label: 'Username', type: 'text', required: false, placeholder: 'JMS Username' },
 { name: 'password', label: 'Password', type: 'password', required: false, placeholder: 'JMS Password' },
 { name: 'connectionFactory', label: 'Connection Factory', type: 'text', required: false, placeholder: 'ConnectionFactory' },
 { name: 'authType', label: 'Authentication', type: 'select', required: false, options: ['None', 'Basic Auth', 'Bearer Token', 'API Key', 'OAuth', 'OAuth 2.0', 'SSL Certificate'] }
 ]
 },
 {
 id: 'odata',
 name: 'ODATA',
 icon: Layers,
 description: 'Open Data Protocol services',
 category: 'API',
 fields: [
 { name: 'serviceUrl', label: 'Service URL', type: 'text', required: true, placeholder: 'https://services.odata.org/V4/service' },
 { name: 'version', label: 'OData Version', type: 'select', required: true, options: ['V4', 'V3', 'V2'] },
 { name: 'authType', label: 'Authentication', type: 'select', required: false, options: ['None', 'Basic Auth', 'Bearer Token', 'SSL Certificate'] },
 { name: 'authValue', label: 'Auth Value', type: 'password', required: false, placeholder: 'Token or credentials' },
 { name: 'maxPageSize', label: 'Max Page Size', type: 'number', required: false, placeholder: '1000' }
 ]
 },
 {
 id: 'idoc',
 name: 'IDOC',
 icon: FileSpreadsheet,
 description: 'SAP Intermediate Document format',
 category: 'SAP',
 fields: [
 { name: 'sapHost', label: 'SAP Host', type: 'text', required: true, placeholder: 'sap.example.com' },
 { name: 'systemNumber', label: 'System Number', type: 'text', required: true, placeholder: '00' },
 { name: 'client', label: 'Client', type: 'text', required: true, placeholder: '100' },
 { name: 'username', label: 'Username', type: 'text', required: true, placeholder: 'SAP Username' },
 { name: 'password', label: 'Password', type: 'password', required: true, placeholder: 'SAP Password' },
 { name: 'language', label: 'Language', type: 'text', required: false, placeholder: 'EN' },
 { name: 'idocType', label: 'IDOC Type', type: 'text', required: false, placeholder: 'ORDERS05' },
 { name: 'authType', label: 'Authentication', type: 'select', required: false, options: ['None', 'Basic Auth', 'Bearer Token', 'API Key', 'OAuth', 'OAuth 2.0', 'SSL Certificate'] }
 ]
 },
 {
 id: 'jdbc',
 name: 'JDBC',
 icon: Database,
 description: 'Java Database Connectivity',
 category: 'Database',
 fields: [
 { name: 'driverJar', label: 'JDBC Driver JAR', type: 'jar-selector', required: true, driverTypeFilter: 'Database' },
 { name: 'jdbcUrl', label: 'JDBC URL', type: 'text', required: true, placeholder: 'jdbc:postgresql://localhost:5432/mydb' },
 { name: 'username', label: 'Username', type: 'text', required: true, placeholder: 'dbuser' },
 { name: 'password', label: 'Password', type: 'password', required: true, placeholder: 'Database Password' },
 { name: 'maxPoolSize', label: 'Max Pool Size', type: 'number', required: false, placeholder: '10' },
 { name: 'connectionTimeout', label: 'Connection Timeout (ms)', type: 'number', required: false, placeholder: '30000' }
 ]
 },
 {
 id: 'as2',
 name: 'AS2',
 icon: Cable,
 description: 'Applicability Statement 2 protocol',
 category: 'EDI',
 fields: [
 { name: 'as2Url', label: 'AS2 URL', type: 'text', required: true, placeholder: 'https://partner.example.com/as2' },
 { name: 'as2From', label: 'AS2 From', type: 'text', required: true, placeholder: 'MyCompany' },
 { name: 'as2To', label: 'AS2 To', type: 'text', required: true, placeholder: 'PartnerCompany' },
 { name: 'certificate', label: 'Certificate', type: 'certificate-dropdown', required: true, placeholder: 'Select certificate' },
 { name: 'encryptionAlgorithm', label: 'Encryption Algorithm', type: 'select', required: false, options: ['3DES', 'AES128', 'AES192', 'AES256'] },
 { name: 'authType', label: 'Authentication', type: 'select', required: false, options: ['None', 'Basic Auth', 'Bearer Token', 'API Key', 'OAuth', 'OAuth 2.0', 'SSL Certificate'] }
 ]
 },
 {
 id: 'rfc',
 name: 'RFC',
 icon: ArrowLeftRight,
 description: 'SAP Remote Function Call protocol',
 category: 'SAP',
 fields: [
 { name: 'sapSystemId', label: 'SAP System ID (SID)', type: 'text', required: true, placeholder: 'PRD, DEV' },
 { name: 'sapClientNumber', label: 'SAP Client Number', type: 'text', required: true, placeholder: '100' },
 { name: 'sapSystemNumber', label: 'SAP System Number', type: 'text', required: true, placeholder: '00' },
 { name: 'sapApplicationServerHost', label: 'SAP Application Server Host', type: 'text', required: true, placeholder: 'sapserver.example.com' },
 { name: 'sapUser', label: 'SAP User', type: 'text', required: true, placeholder: 'sapuser' },
 { name: 'sapPassword', label: 'SAP Password', type: 'password', required: true, placeholder: 'secret' },
 { name: 'rfcDestinationName', label: 'RFC Destination Name', type: 'text', required: true, placeholder: 'MIDDLEWARE_DEST' }
 ]
 },
 {
 id: 'kafka',
 name: 'KAFKA',
 icon: Activity,
 description: 'Apache Kafka messaging platform',
 category: 'Streaming',
 fields: [
 { name: 'bootstrapServers', label: 'Bootstrap Servers', type: 'text', required: true, placeholder: 'localhost:9092' },
 { name: 'topicName', label: 'Topic Name', type: 'text', required: true, placeholder: 'my-topic' },
 { name: 'groupId', label: 'Consumer Group ID', type: 'text', required: false, placeholder: 'my-consumer-group' },
 { name: 'securityProtocol', label: 'Security Protocol', type: 'select', required: false, options: ['PLAINTEXT', 'SSL', 'SASL_PLAINTEXT', 'SASL_SSL'] },
 { name: 'saslMechanism', label: 'SASL Mechanism', type: 'select', required: false, options: ['PLAIN', 'SCRAM-SHA-256', 'SCRAM-SHA-512'] },
 { name: 'username', label: 'Username', type: 'text', required: false, placeholder: 'Kafka Username' },
 { name: 'password', label: 'Password', type: 'password', required: false, placeholder: 'Kafka Password' },
 { name: 'authType', label: 'Authentication', type: 'select', required: false, options: ['None', 'Basic Auth', 'Bearer Token', 'API Key', 'OAuth', 'OAuth 2.0', 'SSL Certificate'] }
 ]
 },
 {
 id: 'mail',
 name: 'MAIL',
 icon: Mail,
 description: 'Email communication via IMAP/POP3/SMTP',
 category: 'Email',
    fields: [
      { name: 'mailServerHost', label: 'Mail Server Host', type: 'text', required: true, placeholder: 'imap.mailserver.com' },
      { name: 'mailServerPort', label: 'Mail Server Port', type: 'number', required: true, placeholder: '993' },
      { name: 'mailProtocol', label: 'Protocol', type: 'select', required: true, options: ['IMAP', 'POP3'] },
      { name: 'mailUsername', label: 'Username', type: 'text', required: true, placeholder: 'user@example.com' },
      { name: 'mailPassword', label: 'Password', type: 'password', required: true, placeholder: 'password' },
      { name: 'useSSLTLS', label: 'Use SSL/TLS', type: 'checkbox', required: false },
      { name: 'folderName', label: 'Folder Name', type: 'text', required: false, placeholder: 'INBOX' }
    ]
  },
  {
    id: 'facebook',
    name: 'Facebook',
    icon: Facebook,
    description: 'Facebook Graph API for social media integration',
    category: 'Social Media',
    fields: [
      { name: 'appId', label: 'Facebook App ID', type: 'text', required: true, placeholder: 'Your Facebook App ID' },
      { name: 'appSecret', label: 'Facebook App Secret', type: 'password', required: true, placeholder: 'Your Facebook App Secret' },
      { name: 'pageId', label: 'Facebook Page ID', type: 'text', required: true, placeholder: 'Your Facebook Page ID' },
      { name: 'accessToken', label: 'Page Access Token', type: 'password', required: false, placeholder: 'Will be obtained via OAuth' },
      { name: 'webhookEnabled', label: 'Enable Webhooks', type: 'checkbox', required: false },
      { name: 'webhookVerifyToken', label: 'Webhook Verify Token', type: 'text', required: false, placeholder: 'Create a secure verify token' },
      { name: 'apiVersion', label: 'API Version', type: 'text', required: false, placeholder: 'v18.0' }
    ]
  },
  {
    id: 'facebookads',
    name: 'Facebook Ads',
    icon: Facebook,
    description: 'Facebook Ads API for advertising campaign management',
    category: 'Social Media',
    fields: [
      { name: 'appId', label: 'Facebook App ID', type: 'text', required: true, placeholder: 'Your Facebook App ID' },
      { name: 'appSecret', label: 'Facebook App Secret', type: 'password', required: true, placeholder: 'Your Facebook App Secret' },
      { name: 'adAccountId', label: 'Ad Account ID', type: 'text', required: true, placeholder: 'Your Ad Account ID (without act_ prefix)' },
      { name: 'businessId', label: 'Business Manager ID', type: 'text', required: false, placeholder: 'Your Business Manager ID' },
      { name: 'accessToken', label: 'Access Token', type: 'password', required: false, placeholder: 'Will be obtained via OAuth' },
      { name: 'apiVersion', label: 'API Version', type: 'text', required: false, placeholder: 'v18.0' }
    ]
  },
  {
    id: 'instagram',
    name: 'Instagram',
    icon: Instagram,
    description: 'Instagram Graph API for social media content and analytics',
    category: 'Social Media',
    fields: [
      { name: 'appId', label: 'Facebook App ID', type: 'text', required: true, placeholder: 'Your Facebook App ID' },
      { name: 'appSecret', label: 'Facebook App Secret', type: 'password', required: true, placeholder: 'Your Facebook App Secret' },
      { name: 'instagramBusinessAccountId', label: 'Instagram Business Account ID', type: 'text', required: true, placeholder: 'Your Instagram Business Account ID' },
      { name: 'facebookPageId', label: 'Facebook Page ID', type: 'text', required: true, placeholder: 'Connected Facebook Page ID' },
      { name: 'accessToken', label: 'Access Token', type: 'password', required: false, placeholder: 'Will be obtained via OAuth' },
      { name: 'apiVersion', label: 'API Version', type: 'text', required: false, placeholder: 'v18.0' }
    ]
  },
  {
    id: 'whatsapp',
    name: 'WhatsApp Business',
    icon: MessageCircle,
    description: 'WhatsApp Business API for messaging and customer communication',
    category: 'Social Media',
    fields: [
      { name: 'appId', label: 'Facebook App ID', type: 'text', required: true, placeholder: 'Your Facebook App ID' },
      { name: 'appSecret', label: 'Facebook App Secret', type: 'password', required: true, placeholder: 'Your Facebook App Secret' },
      { name: 'phoneNumberId', label: 'Phone Number ID', type: 'text', required: true, placeholder: 'WhatsApp Phone Number ID' },
      { name: 'businessAccountId', label: 'Business Account ID', type: 'text', required: true, placeholder: 'WhatsApp Business Account ID' },
      { name: 'verifyToken', label: 'Webhook Verify Token', type: 'text', required: false, placeholder: 'Secure token for webhook verification' },
      { name: 'accessToken', label: 'Access Token', type: 'password', required: false, placeholder: 'Will be obtained via OAuth' },
      { name: 'systemUserAccessToken', label: 'System User Token', type: 'password', required: false, placeholder: 'For production use' },
      { name: 'apiVersion', label: 'API Version', type: 'text', required: false, placeholder: 'v18.0' }
    ]
  },
  {
    id: 'twitter',
    name: 'Twitter',
    icon: Twitter,
    description: 'Twitter/X API v2 for social media engagement and analytics',
    category: 'Social Media',
    fields: [
      { name: 'apiKey', label: 'API Key (Consumer Key)', type: 'text', required: true, placeholder: 'Your Twitter API Key' },
      { name: 'apiKeySecret', label: 'API Key Secret', type: 'password', required: true, placeholder: 'Your Twitter API Key Secret' },
      { name: 'accessToken', label: 'Access Token', type: 'text', required: false, placeholder: 'User access token' },
      { name: 'accessTokenSecret', label: 'Access Token Secret', type: 'password', required: false, placeholder: 'User access token secret' },
      { name: 'bearerToken', label: 'Bearer Token', type: 'password', required: false, placeholder: 'App-only authentication token' },
      { name: 'clientId', label: 'OAuth 2.0 Client ID', type: 'text', required: false, placeholder: 'For OAuth 2.0 flows' },
      { name: 'clientSecret', label: 'OAuth 2.0 Client Secret', type: 'password', required: false, placeholder: 'For OAuth 2.0 flows' }
    ]
  },
  {
    id: 'twitterads',
    name: 'Twitter Ads',
    icon: Twitter,
    description: 'Twitter/X Ads API for advertising campaign management',
    category: 'Social Media',
    fields: [
      { name: 'adsAccountId', label: 'Ads Account ID', type: 'text', required: true, placeholder: 'Your Twitter Ads account ID' },
      { name: 'apiKey', label: 'API Key (Consumer Key)', type: 'text', required: true, placeholder: 'Your Twitter API Key' },
      { name: 'apiKeySecret', label: 'API Key Secret', type: 'password', required: true, placeholder: 'Your Twitter API Key Secret' },
      { name: 'accessToken', label: 'Access Token', type: 'text', required: true, placeholder: 'User access token with ads permissions' },
      { name: 'accessTokenSecret', label: 'Access Token Secret', type: 'password', required: true, placeholder: 'User access token secret' }
    ]
  },
  {
    id: 'linkedin',
    name: 'LinkedIn',
    icon: Linkedin,
    description: 'LinkedIn API for professional networking and content management',
    category: 'Social Media',
    fields: [
      { name: 'clientId', label: 'Client ID', type: 'text', required: true, placeholder: 'Your LinkedIn app Client ID' },
      { name: 'clientSecret', label: 'Client Secret', type: 'password', required: true, placeholder: 'Your LinkedIn app Client Secret' },
      { name: 'accessToken', label: 'Access Token', type: 'password', required: false, placeholder: 'OAuth 2.0 access token' },
      { name: 'refreshToken', label: 'Refresh Token', type: 'password', required: false, placeholder: 'OAuth 2.0 refresh token' },
      { name: 'memberUrn', label: 'Member URN', type: 'text', required: false, placeholder: 'urn:li:person:ABC123' },
      { name: 'organizationId', label: 'Organization ID', type: 'text', required: false, placeholder: 'Company page ID' }
    ]
  },
  {
    id: 'linkedinads',
    name: 'LinkedIn Ads',
    icon: Linkedin,
    description: 'LinkedIn Ads API for advertising campaign management',
    category: 'Social Media',
    fields: [
      { name: 'clientId', label: 'Client ID', type: 'text', required: true, placeholder: 'Your LinkedIn app Client ID' },
      { name: 'clientSecret', label: 'Client Secret', type: 'password', required: true, placeholder: 'Your LinkedIn app Client Secret' },
      { name: 'adAccountId', label: 'Ad Account ID', type: 'text', required: true, placeholder: 'Your LinkedIn Ads account ID' },
      { name: 'accessToken', label: 'Access Token', type: 'password', required: false, placeholder: 'OAuth 2.0 access token' },
      { name: 'refreshToken', label: 'Refresh Token', type: 'password', required: false, placeholder: 'OAuth 2.0 refresh token' }
    ]
  },
  {
    id: 'youtube',
    name: 'YouTube Data API',
    icon: Youtube,
    description: 'YouTube Data API v3 for video, playlist, and channel management',
    category: 'Social Media',
    fields: [
      { name: 'clientId', label: 'Client ID', type: 'text', required: true, placeholder: 'Google OAuth2 Client ID' },
      { name: 'clientSecret', label: 'Client Secret', type: 'password', required: true, placeholder: 'Google OAuth2 Client Secret' },
      { name: 'channelId', label: 'Channel ID', type: 'text', required: false, placeholder: 'Your YouTube channel ID' },
      { name: 'accessToken', label: 'Access Token', type: 'password', required: false, placeholder: 'OAuth 2.0 access token' },
      { name: 'refreshToken', label: 'Refresh Token', type: 'password', required: false, placeholder: 'OAuth 2.0 refresh token' }
    ]
  },
  {
    id: 'youtubeanalytics',
    name: 'YouTube Analytics API',
    icon: Youtube,
    description: 'YouTube Analytics API v2 for comprehensive channel and video analytics',
    category: 'Social Media',
    fields: [
      { name: 'clientId', label: 'Client ID', type: 'text', required: true, placeholder: 'Google OAuth2 Client ID' },
      { name: 'clientSecret', label: 'Client Secret', type: 'password', required: true, placeholder: 'Google OAuth2 Client Secret' },
      { name: 'channelId', label: 'Channel ID', type: 'text', required: true, placeholder: 'YouTube channel ID to analyze' },
      { name: 'accessToken', label: 'Access Token', type: 'password', required: false, placeholder: 'OAuth 2.0 access token' },
      { name: 'refreshToken', label: 'Refresh Token', type: 'password', required: false, placeholder: 'OAuth 2.0 refresh token' }
    ]
  },
  {
    id: 'tiktokbusiness',
    name: 'TikTok Business API',
    icon: Share2,
    description: 'TikTok Business API for advertising campaigns, creative management, and analytics',
    category: 'Social Media',
    fields: [
      { name: 'appId', label: 'App ID', type: 'text', required: true, placeholder: 'TikTok App ID' },
      { name: 'appSecret', label: 'App Secret', type: 'password', required: true, placeholder: 'TikTok App Secret' },
      { name: 'advertiserId', label: 'Advertiser ID', type: 'text', required: true, placeholder: 'TikTok Advertiser ID' },
      { name: 'businessId', label: 'Business ID', type: 'text', required: false, placeholder: 'Business Center ID (optional)' },
      { name: 'accessToken', label: 'Access Token', type: 'password', required: false, placeholder: 'OAuth 2.0 access token' },
      { name: 'refreshToken', label: 'Refresh Token', type: 'password', required: false, placeholder: 'OAuth 2.0 refresh token' }
    ]
  },
  {
    id: 'tiktokcontent',
    name: 'TikTok Content API',
    icon: Share2,
    description: 'TikTok Content API for video publishing, engagement, trends, and creator tools',
    category: 'Social Media',
    fields: [
      { name: 'clientKey', label: 'Client Key', type: 'text', required: true, placeholder: 'TikTok Client Key' },
      { name: 'clientSecret', label: 'Client Secret', type: 'password', required: true, placeholder: 'TikTok Client Secret' },
      { name: 'userId', label: 'User ID', type: 'text', required: true, placeholder: 'TikTok User ID' },
      { name: 'username', label: 'Username', type: 'text', required: false, placeholder: 'TikTok Username' },
      { name: 'accessToken', label: 'Access Token', type: 'password', required: false, placeholder: 'OAuth 2.0 access token' },
      { name: 'refreshToken', label: 'Refresh Token', type: 'password', required: false, placeholder: 'OAuth 2.0 refresh token' }
    ]
  },
  {
    id: 'facebookmessenger',
    name: 'Facebook Messenger',
    icon: MessageSquare,
    description: 'Facebook Messenger Platform for chatbots, messaging automation, and customer service',
    category: 'Social Media',
    fields: [
      { name: 'pageId', label: 'Page ID', type: 'text', required: true, placeholder: 'Your Facebook Page ID' },
      { name: 'pageAccessToken', label: 'Page Access Token', type: 'password', required: true, placeholder: 'Long-lived page access token' },
      { name: 'appSecret', label: 'App Secret', type: 'password', required: true, placeholder: 'Facebook App Secret' },
      { name: 'verifyToken', label: 'Verify Token', type: 'text', required: true, placeholder: 'Custom verify token for webhook' },
      { name: 'webhookUrl', label: 'Webhook URL', type: 'text', required: false, placeholder: 'Your webhook endpoint URL' }
    ]
  },
  {
    id: 'pinterest',
    name: 'Pinterest',
    icon: Share2,
    description: 'Pinterest API for pins, boards, shopping, and advertising',
    category: 'Social Media',
    fields: [
      { name: 'appId', label: 'App ID', type: 'text', required: true, placeholder: 'Your Pinterest App ID' },
      { name: 'appSecret', label: 'App Secret', type: 'password', required: true, placeholder: 'Your Pinterest App Secret' },
      { name: 'advertiserId', label: 'Advertiser ID', type: 'text', required: false, placeholder: 'Pinterest Advertiser ID (for ads)' },
      { name: 'accessToken', label: 'Access Token', type: 'password', required: false, placeholder: 'OAuth 2.0 access token' },
      { name: 'refreshToken', label: 'Refresh Token', type: 'password', required: false, placeholder: 'OAuth 2.0 refresh token' }
    ]
  },
  {
    id: 'reddit',
    name: 'Reddit',
    icon: MessageCircle,
    description: 'Reddit API for posts, comments, moderation, and community management',
    category: 'Social Media',
    fields: [
      { name: 'clientId', label: 'Client ID', type: 'text', required: true, placeholder: 'Your Reddit App ID' },
      { name: 'clientSecret', label: 'Client Secret', type: 'password', required: true, placeholder: 'Your Reddit App Secret' },
      { name: 'userAgent', label: 'User Agent', type: 'text', required: true, placeholder: 'platform:app_id:version (by /u/username)' },
      { name: 'username', label: 'Username', type: 'text', required: false, placeholder: 'Reddit username (for script apps)' },
      { name: 'password', label: 'Password', type: 'password', required: false, placeholder: 'Reddit password (for script apps)' }
    ]
  },
  {
    id: 'snapchatads',
    name: 'Snapchat Ads',
    icon: Camera,
    description: 'Snapchat Ads API for advertising campaigns and creative management',
    category: 'Social Media',
    fields: [
      { name: 'clientId', label: 'Client ID', type: 'text', required: true, placeholder: 'Your Snapchat Ads Client ID' },
      { name: 'clientSecret', label: 'Client Secret', type: 'password', required: true, placeholder: 'Your Snapchat Ads Client Secret' },
      { name: 'adAccountId', label: 'Ad Account ID', type: 'text', required: true, placeholder: 'Your Snapchat Ad Account ID' },
      { name: 'organizationId', label: 'Organization ID', type: 'text', required: false, placeholder: 'Your Snapchat Organization ID' },
      { name: 'pixelId', label: 'Pixel ID', type: 'text', required: false, placeholder: 'Your Snapchat Pixel ID' },
      { name: 'refreshToken', label: 'Refresh Token', type: 'password', required: false, placeholder: 'OAuth 2.0 refresh token' }
    ]
  },
  {
    id: 'discord',
    name: 'Discord',
    icon: MessageSquare,
    description: 'Discord API for bots, servers, channels, and real-time messaging',
    category: 'Social Media',
    fields: [
      { name: 'clientId', label: 'Client ID', type: 'text', required: true, placeholder: 'Your Discord Application Client ID' },
      { name: 'clientSecret', label: 'Client Secret', type: 'password', required: true, placeholder: 'Your Discord Application Client Secret' },
      { name: 'botToken', label: 'Bot Token', type: 'password', required: true, placeholder: 'Your Discord Bot Token' },
      { name: 'publicKey', label: 'Public Key', type: 'text', required: true, placeholder: 'Your Discord Application Public Key' },
      { name: 'applicationId', label: 'Application ID', type: 'text', required: true, placeholder: 'Your Discord Application ID' },
      { name: 'guildId', label: 'Guild ID', type: 'text', required: false, placeholder: 'Specific Guild/Server ID (optional)' }
    ]
  },
  {
    id: 'telegram',
    name: 'Telegram',
    icon: Send,
    description: 'Telegram Bot API for messaging, groups, channels, and bot interactions',
    category: 'Social Media',
    fields: [
      { name: 'botToken', label: 'Bot Token', type: 'password', required: true, placeholder: 'Your Telegram Bot Token from @BotFather' },
      { name: 'botUsername', label: 'Bot Username', type: 'text', required: false, placeholder: 'Your bot username (without @)' },
      { name: 'webhookUrl', label: 'Webhook URL', type: 'text', required: false, placeholder: 'HTTPS URL for webhook (optional)' },
      { name: 'defaultChatId', label: 'Default Chat ID', type: 'text', required: false, placeholder: 'Default chat/channel ID (optional)' }
    ]
  },
  {
    id: 'slack',
    name: 'Slack',
    icon: MessageSquare,
    description: 'Slack API for workspace messaging, apps, workflows, and integrations',
    category: 'Collaboration',
    fields: [
      { name: 'clientId', label: 'Client ID', type: 'text', required: true, placeholder: 'Your Slack App Client ID' },
      { name: 'clientSecret', label: 'Client Secret', type: 'password', required: true, placeholder: 'Your Slack App Client Secret' },
      { name: 'botToken', label: 'Bot User OAuth Token', type: 'password', required: true, placeholder: 'xoxb-...' },
      { name: 'signingSecret', label: 'Signing Secret', type: 'password', required: true, placeholder: 'Your Slack App Signing Secret' }
    ]
  },
  {
    id: 'teams',
    name: 'Microsoft Teams',
    icon: Users,
    description: 'Microsoft Teams API for collaboration, meetings, channels, and Microsoft 365 integration',
    category: 'Collaboration',
    fields: [
      { name: 'tenantId', label: 'Tenant ID', type: 'text', required: true, placeholder: 'Your Azure AD Tenant ID' },
      { name: 'clientId', label: 'Client ID', type: 'text', required: true, placeholder: 'Application (client) ID' },
      { name: 'clientSecret', label: 'Client Secret', type: 'password', required: true, placeholder: 'Client secret value' },
      { name: 'botId', label: 'Bot ID', type: 'text', required: false, placeholder: 'Bot ID (usually same as Client ID)' }
    ]
  }
];

export const CreateCommunicationAdapter = () => {
 const location = useLocation();
 const { navigateBack } = useNavigationHistory();
 const [selectedBusinessComponent, setSelectedBusinessComponent] = useState<BusinessComponent | null>(null);
 const [selectedAdapter, setSelectedAdapter] = useState('');
 const [adapterName, setAdapterName] = useState('');
 const [adapterMode, setAdapterMode] = useState('sender');
 const [description, setDescription] = useState('');
 const [isActive, setIsActive] = useState(true);
 const [configuration, setConfiguration] = useState<Record<string, any>>({});

 const [isTestingConnection, setIsTestingConnection] = useState(false);
 const [connectionStatus, setConnectionStatus] = useState<'idle' | 'success' | 'error'>('idle');
 const [showMappingScreen, setShowMappingScreen] = useState(false);
 const [isEditMode, setIsEditMode] = useState(false);
 const [editingAdapterId, setEditingAdapterId] = useState<string | null>(null);
 const { toast } = useToast();

 const selectedAdapterConfig = communicationAdapters.find(adapter => adapter.id === selectedAdapter);
 // Load adapter data when editing
 useEffect(() => {
 if (location.state?.adapter && location.state?.isEdit) {
 const adapter = location.state.adapter;
 logger.info(LogCategory.SYSTEM, '[CreateCommunicationAdapter] Loading adapter for edit', { data: adapter });
 setIsEditMode(true);
 setEditingAdapterId(adapter.id);
 setAdapterName(adapter.name || '');
 setDescription(adapter.description || '');
 setIsActive(adapter.active !== false);

 // Map backend mode to frontend mode
 const frontendMode = adapter.mode === 'INBOUND' ? 'sender' : 'receiver';
 setAdapterMode(frontendMode);

 // Map backend type to frontend adapter ID
 const frontendAdapterType = adapter.type.toLowerCase();
 setSelectedAdapter(frontendAdapterType);

 // Parse configuration JSON
 if (adapter.configJson) {
 try {
 const configData = JSON.parse(adapter.configJson);
 setConfiguration(configData);
 } catch (error) {
 logger.error(LogCategory.ERROR, 'Error parsing adapter configuration', { error: error });
 }
 }

 // Set business component
 if (adapter.businessComponentId) {
 setSelectedBusinessComponent({
 id: adapter.businessComponentId,
 name: adapter.businessComponentName || '',
 description: '',
 contactEmail: '',
 contactPhone: ''
 } as BusinessComponent);
 }
 }
 }, [location.state]);

 // Get dynamic auth fields based on selected auth type
 const getAuthFields = (authType: string) => {
 switch (authType) {
 case 'Basic Auth':
 return [
 { name: 'authUsername', label: 'Username', type: 'text', required: true, placeholder: 'Enter username' },
 { name: 'authPassword', label: 'Password', type: 'password', required: true, placeholder: 'Enter password' }
 ];
 case 'Bearer Token':
 return [
 { name: 'bearerToken', label: 'Bearer Token', type: 'password', required: true, placeholder: 'Enter bearer token' }
 ];
 case 'API Key':
 return [
 { name: 'apiKeyValue', label: 'API Key', type: 'password', required: true, placeholder: 'Enter API key' },
 { name: 'apiKeyLocation', label: 'API Key Location', type: 'select', required: true, options: ['Header', 'Query Parameter'] },
 { name: 'apiKeyName', label: 'API Key Name', type: 'text', required: true, placeholder: 'e.g., X-API-Key or api_key' }
 ];
 case 'OAuth':
 return [
 { name: 'oauthClientId', label: 'Client ID', type: 'text', required: true, placeholder: 'Enter client ID' },
 { name: 'oauthClientSecret', label: 'Client Secret', type: 'password', required: true, placeholder: 'Enter client secret' },
 { name: 'oauthAuthUrl', label: 'Authorization URL', type: 'text', required: true, placeholder: 'https://auth.example.com/oauth/authorize' },
 { name: 'oauthTokenUrl', label: 'Token URL', type: 'text', required: true, placeholder: 'https://auth.example.com/oauth/token' },
 { name: 'oauthScope', label: 'Scope', type: 'text', required: false, placeholder: 'read write' }
 ];
 case 'OAuth 2.0':
 return [
 { name: 'oauth2ClientId', label: 'Client ID', type: 'text', required: true, placeholder: 'Enter client ID' },
 { name: 'oauth2ClientSecret', label: 'Client Secret', type: 'password', required: true, placeholder: 'Enter client secret' },
 { name: 'oauth2AuthUrl', label: 'Authorization URL', type: 'text', required: true, placeholder: 'https://auth.example.com/oauth2/authorize' },
 { name: 'oauth2TokenUrl', label: 'Token URL', type: 'text', required: true, placeholder: 'https://auth.example.com/oauth2/token' },
 { name: 'oauth2GrantType', label: 'Grant Type', type: 'select', required: true, options: ['authorization_code', 'client_credentials', 'password', 'refresh_token'] },
 { name: 'oauth2Scope', label: 'Scope', type: 'text', required: false, placeholder: 'read write' }
 ];
 case 'SSL Certificate':
 return [
 { name: 'sslCertPath', label: 'Certificate Path', type: 'text', required: true, placeholder: '/path/to/certificate.pem' },
 { name: 'sslKeyPath', label: 'Private Key Path', type: 'text', required: true, placeholder: '/path/to/private-key.pem' },
 { name: 'sslKeyPassword', label: 'Key Password', type: 'password', required: false, placeholder: 'Private key password (if encrypted)' },
 { name: 'sslCaPath', label: 'CA Certificate Path', type: 'text', required: false, placeholder: '/path/to/ca-certificate.pem' },
 { name: 'sslVerifyPeer', label: 'Verify Peer', type: 'select', required: false, options: ['true', 'false'] }
 ];
 default:
 return [];
 }
 };


 const handleConfigurationChange = (fieldName: string, value: string | number | boolean) => {
 logger.info(LogCategory.SYSTEM, '[CreateCommunicationAdapter] Configuration change', { data: { fieldName, value } });
 setConfiguration(prev => {
 const newConfig = {
 ...prev,
 [fieldName]: value
 };
 logger.info(LogCategory.SYSTEM, '[CreateCommunicationAdapter] New configuration state', { data: newConfig });
 return newConfig;
 })
 };

 const handleTestConnection = async () => {
 if (!selectedAdapter) {
 toast({
 title: "No Adapter Selected",
 description: "Please select a communication adapter first",
 variant: "destructive",
 });
 return;
 }

 setIsTestingConnection(true);
 setConnectionStatus('idle');

 // Simulate connection testing
 try {
 await new Promise(resolve => setTimeout(resolve, 2000));

 // Mock success for demo
 const success = Math.random() > 0.3; // 70% success rate for demo
 if (success) {
 setConnectionStatus('success');
 toast({
 title: "Connection Successful",
 description: `Successfully connected to ${selectedAdapterConfig?.name}`,
 variant: "default",
 });
 } else {
 setConnectionStatus('error');
 toast({
 title: "Connection Failed",
 description: "Please check your configuration and try again",
 variant: "destructive",
 });
 }
 } catch (error) {
 setConnectionStatus('error');
 toast({
 title: "Connection Error",
 description: "An error occurred while testing the connection",
 variant: "destructive",
 });
 } finally {
 setIsTestingConnection(false);
 }
 };

 const handleSaveAdapter = async () => {
 // Basic validation - Business Component is REQUIRED
 if (!selectedBusinessComponent || !selectedBusinessComponent.id) {
 toast({
 title: "Validation Error",
 description: "Business Component is required. Please select a business component for this adapter.",
 variant: "destructive",
 });
 return;
 }

 if (!adapterName || !selectedAdapter || !adapterMode) {
 toast({
 title: "Validation Error",
 description: "Please provide adapter name, select a type, and choose adapter mode",
 variant: "destructive",
 });
 return;
 }

 // Mode-specific validation
 logger.info(LogCategory.VALIDATION, 'Debug info', { 
 message: `[CreateCommunicationAdapter] Validating adapter`,
 name: adapterName,
 type: selectedAdapter,
 mode: adapterMode,
 businessComponent: selectedBusinessComponent.id,
 configuration
 });

 // Mode-specific required field validation
 if (adapterMode === 'receiver') {
 // Outbound adapters send data TO external systems
 // Check for receiver-specific required fields based on adapter type
 if (selectedAdapter === 'rest') {
 if (!configuration.targetEndpointUrl) {
 toast({
 title: "Validation Error",
 description: "Target Endpoint URL is required for REST Outbound adapter",
 variant: "destructive",
 });
 return;
 }
 if (!configuration.httpMethod) {
 toast({
 title: "Validation Error",
 description: "HTTP Method is required for REST Outbound adapter",
 variant: "destructive",
 });
 return;
 }
 if (!configuration.contentType) {
 toast({
 title: "Validation Error",
 description: "Content Type is required for REST Outbound adapter",
 variant: "destructive",
 });
 return;
 }
 }
 } else if (adapterMode === 'sender') {
 // Inbound adapters receive data FROM external systems
 // Check for sender-specific required fields based on adapter type
 if (selectedAdapter === 'rest') {
 if (!configuration.port) {
 toast({
 title: "Validation Error",
 description: "Port is required for REST Inbound adapter",
 variant: "destructive",
 });
 return;
 }
 } else if (selectedAdapter === 'soap') {
 if (!configuration.serviceEndpointUrl) {
 toast({
 title: "Validation Error",
 description: "Endpoint URL is required for SOAP Inbound adapter",
 variant: "destructive",
 });
 return;
 }
 }
 }

 // Skip generic field validation for REST and SOAP adapters as they use custom components
 // with different field names
 if (selectedAdapter !== 'rest' && selectedAdapter !== 'soap') {
 // Generic field validation (for fields defined in adapterTypes)
 const requiredFields = selectedAdapterConfig?.fields.filter(field => {
 if (!field.required) return false;

 // Skip fields that don't apply to the current adapter mode
 if (field.conditionalField === 'receiver' && adapterMode === 'sender') {
 return false;
 }
 if (field.conditionalField === 'sender' && adapterMode === 'receiver') {
 return false;
 }

 // Skip fields that depend on parent field values (for conditional auth fields)
 if (field.parentField && field.parentValue) {
 const parentFieldValue = configuration[field.parentField];
 if (parentFieldValue !== field.parentValue) {
 return false;
 }
 }

 return true;
 }) || [];

 const missingFields = requiredFields.filter(field => !configuration[field.name]);
 if (missingFields.length > 0) {
 logger.info(LogCategory.SYSTEM, '[CreateCommunicationAdapter] Missing required fields', { data: missingFields });
 toast({
 title: "Missing Required Fields",
 description: `Please fill in: ${missingFields.map(f => f.label).join(', ')}`,
 variant: "destructive",
 });
 return;
 }
 }

 // Map frontend modes to backend modes (following reversed middleware terminology)
 // Inbound Adapter = Receives data FROM external systems (OUTBOUND)
 // Outbound Adapter = Sends data TO external systems (INBOUND)
 const backendMode = adapterMode === 'sender' ? 'INBOUND' : 'OUTBOUND';
 const direction = adapterMode === 'sender' ? 'OUTBOUND' : 'INBOUND';
 // Prepare adapter data for backend matching AdapterConfigDTO format
 const adapterData = {
 name: adapterName,
 type: selectedAdapter.toUpperCase(),
 mode: backendMode,
 direction: direction,
 description: description,
 active: isActive,
 businessComponentId: selectedBusinessComponent.id,
 configJson: JSON.stringify(configuration)
 };

 try {
 logger.info(LogCategory.SYSTEM, '[CreateCommunicationAdapter] Sending adapter data to backend', { data: adapterData });
 // Call backend service with mode-specific handling
 const response = isEditMode && editingAdapterId
 ? await adapterService.updateAdapter(editingAdapterId, adapterData)
 : await adapterService.createAdapter(adapterData);

 if (response.success) {
 toast({
 title: isEditMode ? "Adapter Updated Successfully" : "Adapter Saved Successfully",
 description: `Communication adapter "${adapterName}" has been ${isEditMode ? 'updated' : 'created'}`,
 variant: "default",
 });

 // Navigate back after successful save
 setTimeout(() => {
 navigateBack('/communication-adapters');
 }, 500);
 } else {
 logger.info(LogCategory.SYSTEM, '[CreateCommunicationAdapter] Error response from backend', { data: response });
 toast({
 title: "Save Failed",
 description: response.error || "Failed to save adapter configuration",
 variant: "destructive",
 });
 }
 } catch (error: any) {
 logger.error(LogCategory.ERROR, '[CreateCommunicationAdapter] Error saving adapter', error);
 logger.error(LogCategory.ERROR, 'Error occurred', { 
 error: `[CreateCommunicationAdapter] Error details:`,
 message: error?.message,
 response: error?.response,
 data: error?.response?.data
 });

 // Extract error message from various possible sources
 const errorMessage = error?.response?.data?.message ||
 error?.response?.data?.error ||
 error?.message ||
 "An error occurred while saving the adapter";

 toast({
 title: "Save Error",
 description: errorMessage,
 variant: "destructive",
 });
 }
 };


 return (
 <RestrictedPage>
 <>
 {showMappingScreen && (
 <FieldMappingScreen onClose={() => setShowMappingScreen(false)} />
 )}

 <div className="p-6 space-y-6 animate-fade-in">
 <div className="animate-slide-up">
 <h1 className="text-3xl font-bold text-foreground flex items-center gap-3">
 <Send className="h-8 w-8" />
 {isEditMode ? 'Edit' : 'Create'} Communication Adapter
 </h1>
 <p className="text-muted-foreground">Configure communication channels for sending messages, emails, and notifications</p>
 </div>

 <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
 {/* Adapter Selection & Configuration */}
 <div className="lg:col-span-2 space-y-6">
 <BusinessComponentSelectionAdapterCard
 selectedBusinessComponent={selectedBusinessComponent}
 setSelectedBusinessComponent={setSelectedBusinessComponent}
 />

 <Card className="animate-scale-in">
 <CardHeader>
 <CardTitle>Adapter Details</CardTitle>
 <CardDescription>Basic information for your communication adapter</CardDescription>
 </CardHeader>
 <CardContent className="space-y-4">
 <div className="space-y-2">
 <Label htmlFor="adapterName">Adapter Name *</Label>
 <Input
 id="adapterName"
 placeholder="e.g., Production Email Server"
 value={adapterName}
 onChange={(e) => setAdapterName(e.target.value)}
 className="transition-all duration-300 focus:scale-[1.01]"
 />
 </div>
 <div className="space-y-2">
 <Label htmlFor="description">Description</Label>
 <Textarea
 id="description"
 placeholder="Describe the purpose of this communication adapter..."
 value={description}
 onChange={(e) => setDescription(e.target.value)}
 className="transition-all duration-300 focus:scale-[1.01]"
 rows={3}
 />
 </div>
 <div className="flex items-center space-x-2">
 <Checkbox
 id="isActive"
 checked={isActive}
 onCheckedChange={(checked) => setIsActive(checked === true)}
 />
 <Label htmlFor="isActive" className="text-sm font-normal flex items-center gap-1">
 Active adapter
 <Badge variant={isActive ? "default" : "secondary"} className="text-xs">
 {isActive ? "Active" : "Inactive"}
 </Badge>
 </Label>
 </div>
 </CardContent>
 </Card>

 <Card className="animate-scale-in" style={{ animationDelay: '0.1s' }}>
 <CardHeader>
 <CardTitle>Adapter Type Selection</CardTitle>
 <CardDescription>Choose the type of communication adapter to configure</CardDescription>
 </CardHeader>
 <CardContent className="space-y-4">
 <div className="space-y-2">
 <Label htmlFor="adapterType">Adapter Type *</Label>
 <Combobox
 options={communicationAdapters.map((adapter) => ({
 value: adapter.id,
 label: adapter.name,
 icon: adapter.icon,
 category: adapter.category,
 description: adapter.description
 }))}
 value={selectedAdapter}
 onValueChange={(value) => {
 setSelectedAdapter(value);
 logger.info(LogCategory.SYSTEM, '[CreateCommunicationAdapter] Adapter type changed to', { data: value });
 // Only clear configuration if changing to a different adapter type
 // This prevents losing data when re-selecting the same adapter
 if (selectedAdapter && selectedAdapter !== value) {
 logger.info(LogCategory.SYSTEM, '[CreateCommunicationAdapter] Clearing configuration due to adapter type change');
 setConfiguration({});
 }
 }}
 placeholder="Select communication adapter type"
 searchPlaceholder="Search adapters..."
 emptyMessage="No adapter found."
 className="transition-all duration-300 hover:bg-accent/50"
 />
 </div>

 {selectedAdapterConfig && (
 <>
 <div className="p-3 bg-muted/50 rounded-lg">
 <div className="flex items-center gap-2 mb-2">
 <selectedAdapterConfig.icon className="h-4 w-4 text-primary" />
 <span className="font-medium">{selectedAdapterConfig.name}</span>
 <Badge variant="outline" className="text-xs">{selectedAdapterConfig.category}</Badge>
 </div>
 <p className="text-sm text-muted-foreground">
 {selectedAdapterConfig.description}
 </p>
 </div>

 <div className="space-y-2">
 <Label htmlFor="adapterMode">Adapter Mode *</Label>
 <Select value={adapterMode} onValueChange={(value) => {
 setAdapterMode(value);
 // Don't clear configuration when mode changes
 // The user might have already filled in some fields
 logger.info(LogCategory.SYSTEM, '[CreateCommunicationAdapter] Adapter mode changed to', { data: value });
 }}>
 <SelectTrigger className="transition-all duration-300 hover:bg-accent/50">
 <SelectValue placeholder="Select adapter mode" />
 </SelectTrigger>
 <SelectContent>
 <SelectItem value="sender">Inbound</SelectItem>
 <SelectItem value="receiver">Outbound</SelectItem>
 </SelectContent>
 </Select>
 </div>
 </>
 )}
 </CardContent>
 </Card>

 {selectedAdapterConfig && adapterMode && (
 <>
 {selectedAdapter === 'ftp' ? (
 <FtpAdapterConfiguration mode={adapterMode as 'sender' | 'receiver'} onConfigChange={setConfiguration} />
 ) : selectedAdapter === 'sftp' ? (
 <SftpAdapterConfiguration mode={adapterMode as 'sender' | 'receiver'} onConfigChange={setConfiguration} />
 ) : selectedAdapter === 'file' ? (
 <FileAdapterConfiguration mode={adapterMode as 'sender' | 'receiver'} onConfigChange={setConfiguration} />
 ) : selectedAdapter === 'http' && adapterMode === 'sender' ? (
 <HttpInboundAdapterConfiguration
 configuration={configuration}
 onConfigurationChange={(field, value) => handleConfigurationChange(field, value)}
 />
 ) : selectedAdapter === 'http' && adapterMode === 'receiver' ? (
 <HttpOutboundAdapterConfiguration
 configuration={configuration}
 onConfigurationChange={(field, value) => handleConfigurationChange(field, value)}
 />
 ) : selectedAdapter === 'idoc' && adapterMode === 'sender' ? (
 <IdocInboundAdapterConfiguration
 configuration={configuration}
 onConfigurationChange={(field, value) => handleConfigurationChange(field, value)}
 />
 ) : selectedAdapter === 'idoc' && adapterMode === 'receiver' ? (
 <IdocOutboundAdapterConfiguration
 configuration={configuration}
 onConfigurationChange={(field, value) => handleConfigurationChange(field, value)}
 />
 ) : selectedAdapter === 'ibmmq' && adapterMode === 'sender' ? (
 <IbmmqInboundAdapterConfiguration
 adapter={{
 name: '',
 type: 'jms',
 mode: 'outbound',
 configuration: configuration,
 status: 'inactive'
 }}
 onUpdate={(adapter) => setConfiguration(adapter.configuration)}
 />
 ) : selectedAdapter === 'ibmmq' && adapterMode === 'receiver' ? (
 <IbmmqOutboundAdapterConfiguration
 adapter={{
 name: '',
 type: 'jms',
 mode: 'inbound',
 configuration: configuration,
 status: 'inactive'
 }}
 onUpdate={(adapter) => setConfiguration(adapter.configuration)}
 />
 ) : selectedAdapter === 'rest' && adapterMode === 'sender' ? (
 <RestInboundAdapterConfiguration
 configuration={configuration}
 onConfigurationChange={(field, value) => handleConfigurationChange(field, value)}
 />
 ) : selectedAdapter === 'rest' && adapterMode === 'receiver' ? (
 <RestOutboundAdapterConfiguration
 configuration={configuration}
 onConfigurationChange={(field, value) => handleConfigurationChange(field, value)}
 />
 ) : selectedAdapter === 'soap' && adapterMode === 'sender' ? (
 <SoapInboundAdapterConfiguration
 configuration={configuration}
 onConfigurationChange={(field, value) => handleConfigurationChange(field, value)}
 businessComponentId={selectedBusinessComponent?.id}
 />
 ) : selectedAdapter === 'soap' && adapterMode === 'receiver' ? (
 <SoapOutboundAdapterConfiguration
 configuration={configuration}
 onConfigurationChange={(field, value) => handleConfigurationChange(field, value)}
 businessComponentId={selectedBusinessComponent?.id}
 />
 ) : selectedAdapter === 'odata' && adapterMode === 'sender' ? (
 <OdataInboundAdapterConfiguration
 configuration={configuration}
 onConfigurationChange={(field, value) => handleConfigurationChange(field, value)}
 />
 ) : selectedAdapter === 'odata' && adapterMode === 'receiver' ? (
 <OdataOutboundAdapterConfiguration
 configuration={configuration}
 onConfigurationChange={(field, value) => handleConfigurationChange(field, value)}
 />
 ) : selectedAdapter === 'rfc' && adapterMode === 'sender' ? (
 <RfcInboundAdapterConfiguration
 configuration={configuration}
 onConfigurationChange={(field, value) => handleConfigurationChange(field, value)}
 />
 ) : selectedAdapter === 'rfc' && adapterMode === 'receiver' ? (
 <RfcOutboundAdapterConfiguration
 configuration={configuration}
 onConfigurationChange={(field, value) => handleConfigurationChange(field, value)}
 />
 ) : selectedAdapter === 'mail' && adapterMode === 'sender' ? (
 <MailInboundAdapterConfiguration
 configuration={configuration}
 onConfigurationChange={(field, value) => handleConfigurationChange(field, value)}
 />
 ) : selectedAdapter === 'mail' && adapterMode === 'receiver' ? (
 <MailOutboundAdapterConfiguration
 configuration={configuration}
 onConfigurationChange={(field, value) => handleConfigurationChange(field, value)}
 />
                ) : selectedAdapter === 'facebook' ? (
                  <FacebookGraphApiConfiguration
                    config={configuration}
                    onChange={(newConfig) => setConfiguration(newConfig)}
                    onTest={() => handleTestConnection()}
                    mode={isEditMode ? 'edit' : 'create'}
                  />
                ) : selectedAdapter === 'facebookads' ? (
                  <FacebookAdsApiConfiguration
                    config={configuration}
                    onChange={(newConfig) => setConfiguration(newConfig)}
                    onTest={() => handleTestConnection()}
                    mode={isEditMode ? 'edit' : 'create'}
                  />
                ) : selectedAdapter === 'instagram' ? (
                  <InstagramGraphApiConfiguration
                    config={configuration}
                    onChange={(newConfig) => setConfiguration(newConfig)}
                    onTest={() => handleTestConnection()}
                    mode={isEditMode ? 'edit' : 'create'}
                  />
                ) : selectedAdapter === 'whatsapp' ? (
                  <WhatsAppBusinessApiConfiguration
                    config={configuration}
                    onChange={(newConfig) => setConfiguration(newConfig)}
                    onTest={() => handleTestConnection()}
                    mode={isEditMode ? 'edit' : 'create'}
                  />
                ) : selectedAdapter === 'twitter' ? (
                  <TwitterApiV2Configuration
                    configuration={configuration}
                    onConfigurationChange={(newConfig) => setConfiguration(newConfig)}
                    onValidation={(isValid) => {
                      // Handle validation state if needed
                    }}
                  />
                ) : selectedAdapter === 'twitterads' ? (
                  <TwitterAdsApiConfiguration
                    configuration={configuration}
                    onConfigurationChange={(newConfig) => setConfiguration(newConfig)}
                    onValidation={(isValid) => {
                      // Handle validation state if needed
                    }}
                  />
                ) : selectedAdapter === 'linkedin' ? (
                  <LinkedInApiConfiguration
                    configuration={configuration}
                    onConfigurationChange={(newConfig) => setConfiguration(newConfig)}
                    onValidation={(isValid) => {
                      // Handle validation state if needed
                    }}
                  />
                ) : selectedAdapter === 'linkedinads' ? (
                  <LinkedInAdsApiConfiguration
                    configuration={configuration}
                    onChange={(newConfig) => setConfiguration(newConfig)}
                  />
                ) : selectedAdapter === 'youtube' ? (
                  <YouTubeDataApiConfiguration
                    configuration={configuration}
                    onChange={(newConfig) => setConfiguration(newConfig)}
                  />
                ) : selectedAdapter === 'youtubeanalytics' ? (
                  <YouTubeAnalyticsApiConfiguration
                    configuration={configuration}
                    onChange={(newConfig) => setConfiguration(newConfig)}
                  />
                ) : selectedAdapter === 'tiktokbusiness' ? (
                  <TikTokBusinessApiConfiguration
                    configuration={configuration}
                    onChange={(newConfig) => setConfiguration(newConfig)}
                  />
                ) : selectedAdapter === 'tiktokcontent' ? (
                  <TikTokContentApiConfiguration
                    configuration={configuration}
                    onChange={(newConfig) => setConfiguration(newConfig)}
                  />
                ) : selectedAdapter === 'facebookmessenger' ? (
                  <FacebookMessengerApiConfiguration
                    configuration={configuration}
                    onChange={(newConfig) => setConfiguration(newConfig)}
                  />
                ) : selectedAdapter === 'pinterest' ? (
                  <PinterestApiConfiguration
                    configuration={configuration}
                    onChange={(newConfig) => setConfiguration(newConfig)}
                  />
                ) : selectedAdapter === 'reddit' ? (
                  <RedditApiConfiguration
                    configuration={configuration}
                    onChange={(newConfig) => setConfiguration(newConfig)}
                  />
                ) : selectedAdapter === 'snapchatads' ? (
                  <SnapchatAdsApiConfiguration
                    configuration={configuration}
                    onChange={(newConfig) => setConfiguration(newConfig)}
                  />
                ) : selectedAdapter === 'discord' ? (
                  <DiscordApiConfiguration
                    configuration={configuration}
                    onChange={(newConfig) => setConfiguration(newConfig)}
                  />
                ) : selectedAdapter === 'telegram' ? (
                  <TelegramBotApiConfiguration
                    configuration={configuration}
                    onConfigurationChange={(newConfig) => setConfiguration(newConfig)}
                  />
                ) : selectedAdapter === 'slack' ? (
                  <SlackApiConfiguration
                    configuration={configuration}
                    onConfigurationChange={(newConfig) => setConfiguration(newConfig)}
                  />
                ) : (
 <Card className="animate-scale-in" style={{ animationDelay: '0.2s' }}>
 <CardHeader>
 <CardTitle className="flex items-center gap-2">
 <selectedAdapterConfig.icon className="h-5 w-5" />
 {selectedAdapterConfig.name} Configuration
 </CardTitle>
 <CardDescription>Configure the connection parameters and authentication</CardDescription>
 </CardHeader>
 <CardContent>
 <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
 {selectedAdapterConfig.fields
 .filter((field: any) => {
 // Filter fields based on adapter mode
 if (field.conditionalField === 'receiver' && adapterMode === 'sender') {
 return false;
 }
 if (field.conditionalField === 'sender' && adapterMode === 'receiver') {
 return false;
 }

 // Filter fields based on parent field values (for authentication)
 if (field.parentField && field.parentValue) {
 const parentFieldValue = configuration[field.parentField];
 if (parentFieldValue !== field.parentValue) {
 return false;
 }
 }

 return true;
 })
 .map((field) => (
 <div key={field.name} className={field.name === 'url' || field.name === 'webhookUrl' || field.name === 'customHeaders' || field.type === 'password' ? 'md:col-span-2' : ''}>
 {field.type === 'jar-selector' ? (
 <>
 <Label htmlFor={field.name} className="flex items-center gap-1">
 {field.label}
 {field.required && <span className="text-destructive">*</span>}
 </Label>
 <JarSelector
 selectedJarId={configuration[field.name] || ''}
 onJarSelect={(jarId) => handleConfigurationChange(field.name, jarId)}
 label=""
 placeholder={`Choose ${field.label}`}
 driverTypeFilter={field.driverTypeFilter || undefined}
 />
 </>
 ) : field.type === 'select' ? (
 <>
 <Label htmlFor={field.name} className="flex items-center gap-1">
 {field.label}
 {field.required && <span className="text-destructive">*</span>}
 </Label>
 <Select
 value={configuration[field.name] || ''}
 onValueChange={(value) => handleConfigurationChange(field.name, value)}
 >
 <SelectTrigger className="transition-all duration-300 hover:bg-accent/50">
 <SelectValue placeholder={`Select ${field.label}`} />
 </SelectTrigger>
 <SelectContent className="bg-card border-border shadow-lg z-50">
 {field.options?.map((option) => (
 <SelectItem key={option} value={option}>
 {option}
 </SelectItem>
 ))}
 </SelectContent>
 </Select>
 </>
 ) : field.type === 'textarea' ? (
 <>
 <Label htmlFor={field.name} className="flex items-center gap-1">
 {field.label}
 {field.required && <span className="text-destructive">*</span>}
 </Label>
 <Textarea
 id={field.name}
 placeholder={field.placeholder}
 value={configuration[field.name] || ''}
 onChange={(e) => handleConfigurationChange(field.name, e.target.value)}
 className="transition-all duration-300 focus:scale-[1.01]"
 rows={3}
 />
 </>
 ) : field.type === 'checkbox' ? (
 <div className="flex items-center space-x-2">
 <input
 id={field.name}
 type="checkbox"
 checked={configuration[field.name] === 'true' || configuration[field.name] === true || configuration[field.name] === 'on'}
 onChange={(e) => handleConfigurationChange(field.name, e.target.checked.toString())}
 className="h-4 w-4 rounded border-border"
 />
 <Label htmlFor={field.name} className="text-sm font-normal">
 {field.label}
 </Label>
 </div>
 ) : field.type === 'password' ? (
 <PasswordConfirmation
 name={field.name}
 label={field.label}
 placeholder={field.placeholder}
 required={field.required}
 value={configuration[field.name] || ''}
 onValueChange={(value) => handleConfigurationChange(field.name, value)}
 />
 ) : (
 <>
 <Label htmlFor={field.name} className="flex items-center gap-1">
 {field.label}
 {field.required && <span className="text-destructive">*</span>}
 </Label>
 <Input
 id={field.name}
 type={field.type}
 placeholder={field.placeholder}
 value={configuration[field.name] || ''}
 onChange={(e) => handleConfigurationChange(field.name, e.target.value)}
 className="transition-all duration-300 focus:scale-[1.01]"
 />
 </>
 )}
 </div>
 ))}

 {/* Dynamic Authentication Fields */}
 {configuration.authType && configuration.authType !== 'None' && (
 <>
 <div className="md:col-span-2">
 <Separator className="my-4" />
 <h4 className="font-medium text-sm text-muted-foreground mb-4">
 {configuration.authType} Configuration
 </h4>
 </div>
 {getAuthFields(configuration.authType).map((authField) => (
 <div key={authField.name} className={authField.name.includes('Url') || authField.name.includes('url') || authField.type === 'password' ? 'md:col-span-2' : ''}>
 {authField.type === 'select' ? (
 <>
 <Label htmlFor={authField.name} className="flex items-center gap-1">
 {authField.label}
 {authField.required && <span className="text-destructive">*</span>}
 </Label>
 <Select
 value={configuration[authField.name] || ''}
 onValueChange={(value) => handleConfigurationChange(authField.name, value)}
 >
 <SelectTrigger className="transition-all duration-300 hover:bg-accent/50">
 <SelectValue placeholder={`Select ${authField.label}`} />
 </SelectTrigger>
 <SelectContent className="bg-card border-border shadow-lg z-50">
 {authField.options?.map((option) => (
 <SelectItem key={option} value={option}>
 {option}
 </SelectItem>
 ))}
 </SelectContent>
 </Select>
 </>
 ) : authField.type === 'password' ? (
 <PasswordConfirmation
 name={authField.name}
 label={authField.label}
 placeholder={authField.placeholder}
 required={authField.required}
 value={configuration[authField.name] || ''}
 onValueChange={(value) => handleConfigurationChange(authField.name, value)}
 />
 ) : (
 <>
 <Label htmlFor={authField.name} className="flex items-center gap-1">
 {authField.label}
 {authField.required && <span className="text-destructive">*</span>}
 </Label>
 <Input
 id={authField.name}
 type={authField.type}
 placeholder={authField.placeholder}
 value={configuration[authField.name] || ''}
 onChange={(e) => handleConfigurationChange(authField.name, e.target.value)}
 className="transition-all duration-300 focus:scale-[1.01]"
 />
 </>
 )}
 </div>
 ))}
 </>
 )}
 </div>
 </CardContent>
 </Card>
 )}
 </>
 )}
 </div>

 {/* Actions & Status Panel */}
 <div className="space-y-6">
 <Card className="animate-scale-in" style={{ animationDelay: '0.3s' }}>
 <CardHeader>
 <CardTitle>Connection Testing</CardTitle>
 <CardDescription>Test your adapter configuration</CardDescription>
 </CardHeader>
 <CardContent className="space-y-4">
 <Button
 onClick={handleTestConnection}
 disabled={!selectedAdapter || isTestingConnection}
 className="w-full bg-accent hover:opacity-90 transition-all duration-300"
 variant="outline"
 >
 {isTestingConnection ? (
 <>
 <TestTube className="h-4 w-4 mr-2 animate-spin" />
 Testing...
 </>
 ) : (
 <>
 <TestTube className="h-4 w-4 mr-2" />
 Test Connection
 </>
 )}
 </Button>

 {selectedAdapter === 'soap' && (
 <Button
 onClick={() => setShowMappingScreen(true)}
 className="w-full bg-primary hover:opacity-90 transition-all duration-300"
 variant="outline"
 >
 <ArrowLeftRight className="h-4 w-4 mr-2" />
 Configure Field Mapping
 </Button>
 )}



 {connectionStatus !== 'idle' && (
 <div className={`flex items-center gap-2 p-3 rounded-lg ${
 connectionStatus === 'success'
 ? 'bg-success/10 text-success'
 : 'bg-destructive/10 text-destructive'
 }`}>
 {connectionStatus === 'success' ? (
 <CheckCircle className="h-4 w-4" />
 ) : (
 <AlertCircle className="h-4 w-4" />
 )}
 <span className="text-sm font-medium">
 {connectionStatus === 'success' ? 'Connection Successful' : 'Connection Failed'}
 </span>
 </div>
 )}

 <Separator />

 <div className="space-y-2">
 <AdapterValidationDialog
 adapterType={selectedAdapterConfig?.id || ''}
 configuration={configuration}
 >
 <Button
 variant="outline"
 className="w-full"
 disabled={!selectedAdapterConfig}
 >
 <TestTube className="h-4 w-4 mr-2" />
 Validate Configuration
 </Button>
 </AdapterValidationDialog>

 <div className="flex gap-2">
 <Button
 variant="outline"
 onClick={() => navigateBack('/communication-adapters')}
 className="flex-1"
 >
 Cancel
 </Button>
 <Button
 onClick={handleSaveAdapter}
 className="flex-1 bg-primary hover:opacity-90 transition-all duration-300"
 >
 <Save className="h-4 w-4 mr-2" />
 {isEditMode ? 'Update' : 'Save'} Adapter
 </Button>
 </div>
 </div>
 </CardContent>
 </Card>

 <Card className="animate-scale-in" style={{ animationDelay: '0.4s' }}>
 <CardHeader>
 <CardTitle>Configuration Summary</CardTitle>
 </CardHeader>
 <CardContent className="space-y-3">
 <div className="space-y-2 text-sm">
 <div className="flex justify-between">
 <span className="text-muted-foreground">Status:</span>
 <Badge variant="secondary">Draft</Badge>
 </div>
 <div className="flex justify-between">
 <span className="text-muted-foreground">Type:</span>
 <span>{selectedAdapterConfig?.name || 'Not selected'}</span>
 </div>
 <div className="flex justify-between">
 <span className="text-muted-foreground">Mode:</span>
 <span>{adapterMode || 'Not selected'}</span>
 </div>
 <div className="flex justify-between">
 <span className="text-muted-foreground">Category:</span>
 <span>{selectedAdapterConfig?.category || 'N/A'}</span>
 </div>
 <div className="flex justify-between">
 <span className="text-muted-foreground">Connection:</span>
 <Badge variant={
 connectionStatus === 'success' ? 'default' :
 connectionStatus === 'error' ? 'destructive' : 'secondary'
 }>
 {connectionStatus === 'success' ? 'Verified' :
 connectionStatus === 'error' ? 'Failed' : 'Not tested'}
 </Badge>
 </div>
 </div>
 </CardContent>
 </Card>

 <Card className="animate-scale-in" style={{ animationDelay: '0.5s' }}>
 <CardHeader>
 <CardTitle>Security Notes</CardTitle>
 </CardHeader>
 <CardContent>
 <div className="space-y-2 text-sm text-muted-foreground">
 <div className="flex items-start gap-2">
 <Shield className="h-4 w-4 mt-0.5 text-primary" />
 <span>Credentials are encrypted and stored securely</span>
 </div>
 <div className="flex items-start gap-2">
 <Info className="h-4 w-4 mt-0.5 text-primary" />
 <span>Test connections before using in production</span>
 </div>
 <div className="flex items-start gap-2">
 <Key className="h-4 w-4 mt-0.5 text-primary" />
 <span>Use environment-specific configurations</span>
 </div>
 </div>
 </CardContent>
 </Card>
 </div>
 </div>
 </div>
 </>
 </RestrictedPage>
 );
};