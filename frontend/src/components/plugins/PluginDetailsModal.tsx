import React, { useState } from 'react';
import { X, ExternalLink, Code2, Shield, Activity, Settings, Play, AlertCircle, CheckCircle, Package, Globe, Key } from 'lucide-react';
import type { Plugin, PluginDetails, ConnectionTestResult } from '../../types/plugin';
import { pluginApi } from '../../api/plugin';
import { PluginConfigurationForm } from './PluginConfigurationForm';

interface PluginDetailsModalProps {
  plugin: Plugin;
  details: PluginDetails;
  onClose: () => void;
}

export const PluginDetailsModal: React.FC<PluginDetailsModalProps> = ({
  plugin,
  details,
  onClose,
}) => {
  const [activeTab, setActiveTab] = useState<'overview' | 'configuration' | 'health'>('overview');
  const [isTestingConnection, setIsTestingConnection] = useState(false);
  const [testResult, setTestResult] = useState<ConnectionTestResult | null>(null);
  const [configuration, setConfiguration] = useState<Record<string, any>>({});

  const handleTestConnection = async (direction: 'INBOUND' | 'OUTBOUND') => {
    setIsTestingConnection(true);
    setTestResult(null);
    
    try {
      const result = await pluginApi.testConnection(plugin.id, direction, configuration);
      setTestResult(result);
    } catch (error) {
      setTestResult({
        successful: false,
        message: 'Connection test failed',
        errorDetails: error instanceof Error ? error.message : 'Unknown error',
      });
    } finally {
      setIsTestingConnection(false);
    }
  };

  const getHealthStateColor = (state?: string) => {
    switch (state) {
      case 'HEALTHY':
        return 'text-green-600 bg-green-100';
      case 'DEGRADED':
        return 'text-yellow-600 bg-yellow-100';
      case 'UNHEALTHY':
        return 'text-red-600 bg-red-100';
      default:
        return 'text-gray-600 bg-gray-100';
    }
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg shadow-xl w-full max-w-4xl max-h-[90vh] flex flex-col">
        {/* Header */}
        <div className="flex justify-between items-start p-6 border-b">
          <div className="flex items-start gap-4">
            <div className="w-16 h-16 bg-blue-100 rounded-lg flex items-center justify-center">
              <Package className="w-8 h-8 text-blue-600" />
            </div>
            <div>
              <h2 className="text-xl font-semibold text-gray-900">{plugin.name}</h2>
              <p className="text-sm text-gray-500 mt-1">
                v{plugin.version} by {plugin.vendor}
              </p>
              <div className="flex items-center gap-4 mt-2 text-xs">
                <span className="inline-flex items-center px-2 py-1 bg-blue-50 text-blue-700 rounded-md capitalize">
                  {plugin.category}
                </span>
                {plugin.license && (
                  <span className="flex items-center gap-1 text-gray-500">
                    <Shield className="w-3 h-3" />
                    {plugin.license}
                  </span>
                )}
                {details.isInitialized && (
                  <span className="flex items-center gap-1 text-green-600">
                    <CheckCircle className="w-3 h-3" />
                    Initialized
                  </span>
                )}
              </div>
            </div>
          </div>
          <button onClick={onClose} className="text-gray-400 hover:text-gray-600">
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Tabs */}
        <div className="border-b">
          <div className="flex">
            <button
              onClick={() => setActiveTab('overview')}
              className={`px-6 py-3 text-sm font-medium border-b-2 transition-colors ${
                activeTab === 'overview'
                  ? 'border-blue-600 text-blue-600'
                  : 'border-transparent text-gray-600 hover:text-gray-900'
              }`}
            >
              Overview
            </button>
            <button
              onClick={() => setActiveTab('configuration')}
              className={`px-6 py-3 text-sm font-medium border-b-2 transition-colors ${
                activeTab === 'configuration'
                  ? 'border-blue-600 text-blue-600'
                  : 'border-transparent text-gray-600 hover:text-gray-900'
              }`}
            >
              Configuration
            </button>
            <button
              onClick={() => setActiveTab('health')}
              className={`px-6 py-3 text-sm font-medium border-b-2 transition-colors ${
                activeTab === 'health'
                  ? 'border-blue-600 text-blue-600'
                  : 'border-transparent text-gray-600 hover:text-gray-900'
              }`}
            >
              Health
            </button>
          </div>
        </div>

        {/* Content */}
        <div className="flex-1 overflow-y-auto p-6">
          {activeTab === 'overview' && (
            <div className="space-y-6">
              {/* Description */}
              <div>
                <h3 className="text-lg font-medium text-gray-900 mb-2">Description</h3>
                <p className="text-gray-600">
                  {plugin.description || 'No description available'}
                </p>
              </div>

              {/* Capabilities */}
              {plugin.capabilities && Object.keys(plugin.capabilities).length > 0 && (
                <div>
                  <h3 className="text-lg font-medium text-gray-900 mb-2">Capabilities</h3>
                  <div className="grid grid-cols-2 gap-3">
                    {Object.entries(plugin.capabilities).map(([key, value]) => (
                      <div key={key} className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                        <span className="text-sm font-medium text-gray-700 capitalize">
                          {key.replace(/_/g, ' ')}
                        </span>
                        <span className="text-sm text-gray-600">
                          {typeof value === 'boolean' ? (
                            value ? (
                              <CheckCircle className="w-4 h-4 text-green-600" />
                            ) : (
                              <X className="w-4 h-4 text-gray-400" />
                            )
                          ) : (
                            String(value)
                          )}
                        </span>
                      </div>
                    ))}
                  </div>
                </div>
              )}

              {/* Technical Details */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                {plugin.supportedProtocols && plugin.supportedProtocols.length > 0 && (
                  <div>
                    <h3 className="text-sm font-medium text-gray-700 mb-2 flex items-center gap-2">
                      <Globe className="w-4 h-4" />
                      Supported Protocols
                    </h3>
                    <div className="flex flex-wrap gap-2">
                      {plugin.supportedProtocols.map(protocol => (
                        <span key={protocol} className="px-2 py-1 bg-gray-100 text-gray-700 text-sm rounded">
                          {protocol}
                        </span>
                      ))}
                    </div>
                  </div>
                )}

                {plugin.authenticationMethods && plugin.authenticationMethods.length > 0 && (
                  <div>
                    <h3 className="text-sm font-medium text-gray-700 mb-2 flex items-center gap-2">
                      <Key className="w-4 h-4" />
                      Authentication Methods
                    </h3>
                    <div className="flex flex-wrap gap-2">
                      {plugin.authenticationMethods.map(method => (
                        <span key={method} className="px-2 py-1 bg-gray-100 text-gray-700 text-sm rounded">
                          {method}
                        </span>
                      ))}
                    </div>
                  </div>
                )}
              </div>

              {/* Tags */}
              {plugin.tags && plugin.tags.length > 0 && (
                <div>
                  <h3 className="text-sm font-medium text-gray-700 mb-2">Tags</h3>
                  <div className="flex flex-wrap gap-2">
                    {plugin.tags.map(tag => (
                      <span key={tag} className="px-3 py-1 bg-blue-50 text-blue-700 text-sm rounded-full">
                        {tag}
                      </span>
                    ))}
                  </div>
                </div>
              )}

              {/* Documentation Link */}
              {plugin.documentationUrl && (
                <div className="pt-4 border-t">
                  <a
                    href={plugin.documentationUrl}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="inline-flex items-center gap-2 text-blue-600 hover:text-blue-700"
                  >
                    <ExternalLink className="w-4 h-4" />
                    View Documentation
                  </a>
                </div>
              )}
            </div>
          )}

          {activeTab === 'configuration' && (
            <div className="space-y-6">
              {details.configurationSchema ? (
                <>
                  <PluginConfigurationForm
                    schema={details.configurationSchema}
                    values={configuration}
                    onChange={setConfiguration}
                  />

                  {/* Connection Test */}
                  <div className="border-t pt-6">
                    <h3 className="text-lg font-medium text-gray-900 mb-4">Test Connection</h3>
                    <div className="flex gap-3">
                      <button
                        onClick={() => handleTestConnection('INBOUND')}
                        disabled={isTestingConnection}
                        className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 flex items-center gap-2"
                      >
                        {isTestingConnection ? (
                          <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
                        ) : (
                          <Play className="w-4 h-4" />
                        )}
                        Test Inbound
                      </button>
                      <button
                        onClick={() => handleTestConnection('OUTBOUND')}
                        disabled={isTestingConnection}
                        className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 flex items-center gap-2"
                      >
                        {isTestingConnection ? (
                          <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white"></div>
                        ) : (
                          <Play className="w-4 h-4" />
                        )}
                        Test Outbound
                      </button>
                    </div>

                    {testResult && (
                      <div className={`mt-4 p-4 rounded-lg border ${
                        testResult.successful
                          ? 'bg-green-50 border-green-200'
                          : 'bg-red-50 border-red-200'
                      }`}>
                        <div className="flex items-start gap-3">
                          {testResult.successful ? (
                            <CheckCircle className="w-5 h-5 text-green-600 flex-shrink-0" />
                          ) : (
                            <AlertCircle className="w-5 h-5 text-red-600 flex-shrink-0" />
                          )}
                          <div className="flex-1">
                            <p className={`font-medium ${
                              testResult.successful ? 'text-green-900' : 'text-red-900'
                            }`}>
                              {testResult.message}
                            </p>
                            {testResult.errorDetails && (
                              <p className="text-sm text-red-800 mt-1">{testResult.errorDetails}</p>
                            )}
                            {testResult.responseTime && (
                              <p className="text-sm text-gray-600 mt-1">
                                Response time: {testResult.responseTime}ms
                              </p>
                            )}
                          </div>
                        </div>
                      </div>
                    )}
                  </div>
                </>
              ) : (
                <div className="text-center py-8 text-gray-500">
                  <Settings className="w-12 h-12 mx-auto mb-3 text-gray-400" />
                  <p>No configuration required for this plugin</p>
                </div>
              )}
            </div>
          )}

          {activeTab === 'health' && (
            <div className="space-y-6">
              {details.health ? (
                <>
                  {/* Overall Health */}
                  <div className="bg-gray-50 rounded-lg p-4">
                    <div className="flex items-center justify-between">
                      <div className="flex items-center gap-3">
                        <Activity className="w-5 h-5 text-gray-600" />
                        <span className="font-medium text-gray-900">Overall Status</span>
                      </div>
                      <span className={`px-3 py-1 rounded-full text-sm font-medium ${getHealthStateColor(details.health.state)}`}>
                        {details.health.state}
                      </span>
                    </div>
                    {details.health.message && (
                      <p className="text-sm text-gray-600 mt-2">{details.health.message}</p>
                    )}
                  </div>

                  {/* Components */}
                  {details.health.components && details.health.components.length > 0 && (
                    <div>
                      <h3 className="text-lg font-medium text-gray-900 mb-3">Components</h3>
                      <div className="space-y-2">
                        {details.health.components.map((component, index) => (
                          <div key={index} className="border rounded-lg p-4">
                            <div className="flex items-center justify-between">
                              <span className="font-medium text-gray-900">{component.name}</span>
                              <span className={`px-2 py-1 rounded text-xs font-medium ${getHealthStateColor(component.state)}`}>
                                {component.state}
                              </span>
                            </div>
                            {component.message && (
                              <p className="text-sm text-gray-600 mt-1">{component.message}</p>
                            )}
                          </div>
                        ))}
                      </div>
                    </div>
                  )}

                  {/* Metrics */}
                  {details.health.metrics && (
                    <div>
                      <h3 className="text-lg font-medium text-gray-900 mb-3">Performance Metrics</h3>
                      <div className="grid grid-cols-2 gap-4">
                        {details.health.metrics.messagesProcessed !== undefined && (
                          <div className="bg-blue-50 rounded-lg p-4">
                            <p className="text-sm text-blue-600 font-medium">Messages Processed</p>
                            <p className="text-2xl font-bold text-blue-900">
                              {details.health.metrics.messagesProcessed.toLocaleString()}
                            </p>
                          </div>
                        )}
                        {details.health.metrics.successRate !== undefined && (
                          <div className="bg-green-50 rounded-lg p-4">
                            <p className="text-sm text-green-600 font-medium">Success Rate</p>
                            <p className="text-2xl font-bold text-green-900">
                              {details.health.metrics.successRate.toFixed(1)}%
                            </p>
                          </div>
                        )}
                        {details.health.metrics.errors !== undefined && (
                          <div className="bg-red-50 rounded-lg p-4">
                            <p className="text-sm text-red-600 font-medium">Errors</p>
                            <p className="text-2xl font-bold text-red-900">
                              {details.health.metrics.errors.toLocaleString()}
                            </p>
                          </div>
                        )}
                        {details.health.metrics.averageResponseTime !== undefined && (
                          <div className="bg-purple-50 rounded-lg p-4">
                            <p className="text-sm text-purple-600 font-medium">Avg Response Time</p>
                            <p className="text-2xl font-bold text-purple-900">
                              {details.health.metrics.averageResponseTime}ms
                            </p>
                          </div>
                        )}
                      </div>
                    </div>
                  )}
                </>
              ) : (
                <div className="text-center py-8 text-gray-500">
                  <Activity className="w-12 h-12 mx-auto mb-3 text-gray-400" />
                  <p>Health information not available</p>
                </div>
              )}
            </div>
          )}
        </div>

        {/* Footer */}
        <div className="border-t p-6">
          <div className="flex justify-between">
            <button
              onClick={onClose}
              className="px-4 py-2 text-gray-700 border border-gray-300 rounded-lg hover:bg-gray-50"
            >
              Close
            </button>
            {activeTab === 'configuration' && details.configurationSchema && (
              <button className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700">
                Save Configuration
              </button>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};