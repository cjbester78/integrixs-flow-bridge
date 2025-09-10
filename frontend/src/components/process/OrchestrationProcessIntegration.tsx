import React, { useState } from 'react';
import { 
  Settings, 
  Zap, 
  GitBranch,
  Play,
  Package,
  Eye,
  CheckCircle,
  Info
} from 'lucide-react';
import { BpmnDeploymentManager } from './BpmnDeploymentManager';
import { ProcessExecutionManager } from './ProcessExecutionManager';

interface OrchestrationProcessIntegrationProps {
  flowId: string;
  flowName: string;
  hasVisualFlow: boolean;
}

type TabType = 'deployment' | 'execution' | 'info';

export const OrchestrationProcessIntegration: React.FC<OrchestrationProcessIntegrationProps> = ({
  flowId,
  flowName,
  hasVisualFlow
}) => {
  const [activeTab, setActiveTab] = useState<TabType>('deployment');
  const [deployedProcessId, setDeployedProcessId] = useState<string | null>(null);

  const tabs = [
    { id: 'deployment' as TabType, label: 'Deployment', icon: Package },
    { id: 'execution' as TabType, label: 'Execution', icon: Play, disabled: !deployedProcessId },
    { id: 'info' as TabType, label: 'Information', icon: Info }
  ];

  const handleDeploymentComplete = (processDefinitionId: string) => {
    setDeployedProcessId(processDefinitionId);
    setActiveTab('execution');
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow-sm p-6">
        <div className="flex items-center gap-4 mb-4">
          <div className="p-3 bg-blue-100 dark:bg-blue-900 rounded-lg">
            <GitBranch className="w-6 h-6 text-blue-600 dark:text-blue-400" />
          </div>
          <div>
            <h2 className="text-xl font-semibold">Process Engine Integration</h2>
            <p className="text-gray-600 dark:text-gray-400">
              Deploy and execute orchestration flows using BPMN process engine
            </p>
          </div>
        </div>

        {!hasVisualFlow && (
          <div className="mt-4 p-4 bg-yellow-50 dark:bg-yellow-900/20 rounded-lg">
            <div className="flex items-start gap-3">
              <Zap className="w-5 h-5 text-yellow-600 dark:text-yellow-400 mt-0.5" />
              <div>
                <p className="font-medium text-yellow-900 dark:text-yellow-100">
                  Visual Flow Required
                </p>
                <p className="mt-1 text-sm text-yellow-700 dark:text-yellow-300">
                  This flow needs a visual orchestration design to be deployed as a BPMN process.
                  Please create a visual flow using the Orchestration Editor first.
                </p>
              </div>
            </div>
          </div>
        )}
      </div>

      {hasVisualFlow && (
        <>
          {/* Tabs */}
          <div className="bg-white dark:bg-gray-800 rounded-lg shadow-sm">
            <div className="border-b border-gray-200 dark:border-gray-700">
              <nav className="flex">
                {tabs.map((tab) => (
                  <button
                    key={tab.id}
                    onClick={() => !tab.disabled && setActiveTab(tab.id)}
                    disabled={tab.disabled}
                    className={`
                      flex items-center gap-2 px-6 py-3 text-sm font-medium border-b-2 transition-colors
                      ${activeTab === tab.id
                        ? 'border-blue-500 text-blue-600 dark:text-blue-400'
                        : 'border-transparent text-gray-600 dark:text-gray-400 hover:text-gray-800 dark:hover:text-gray-200'
                      }
                      ${tab.disabled ? 'opacity-50 cursor-not-allowed' : ''}
                    `}
                  >
                    <tab.icon className="w-4 h-4" />
                    {tab.label}
                  </button>
                ))}
              </nav>
            </div>

            {/* Tab Content */}
            <div className="p-6">
              {activeTab === 'deployment' && (
                <BpmnDeploymentManager
                  flowId={flowId}
                  flowName={flowName}
                  onDeploymentComplete={handleDeploymentComplete}
                />
              )}

              {activeTab === 'execution' && deployedProcessId && (
                <ProcessExecutionManager
                  processDefinitionId={deployedProcessId}
                  flowName={flowName}
                />
              )}

              {activeTab === 'info' && (
                <div className="space-y-6">
                  <div>
                    <h3 className="text-lg font-semibold mb-4">Process Engine Information</h3>
                    
                    <div className="space-y-4">
                      <div className="p-4 bg-gray-50 dark:bg-gray-700 rounded-lg">
                        <h4 className="font-medium mb-2 flex items-center gap-2">
                          <Settings className="w-4 h-4" />
                          How It Works
                        </h4>
                        <ol className="list-decimal list-inside space-y-2 text-sm text-gray-600 dark:text-gray-400">
                          <li>Visual orchestration designs are converted to BPMN 2.0 XML</li>
                          <li>BPMN processes are deployed to the process engine</li>
                          <li>Process instances can be started with initial variables</li>
                          <li>The engine executes steps according to the flow definition</li>
                          <li>Each step (service tasks, transformations, etc.) is executed in order</li>
                          <li>Gateways control conditional flow routing</li>
                          <li>Process state and variables are maintained throughout execution</li>
                        </ol>
                      </div>

                      <div className="p-4 bg-blue-50 dark:bg-blue-900/20 rounded-lg">
                        <h4 className="font-medium mb-2 flex items-center gap-2">
                          <Eye className="w-4 h-4 text-blue-600" />
                          Supported BPMN Elements
                        </h4>
                        <div className="grid grid-cols-2 gap-4 text-sm">
                          <div>
                            <p className="font-medium text-blue-900 dark:text-blue-100 mb-1">Events</p>
                            <ul className="list-disc list-inside text-blue-700 dark:text-blue-300">
                              <li>Start Event</li>
                              <li>End Event</li>
                              <li>Timer Event</li>
                              <li>Message Event</li>
                              <li>Error Event</li>
                            </ul>
                          </div>
                          <div>
                            <p className="font-medium text-blue-900 dark:text-blue-100 mb-1">Activities</p>
                            <ul className="list-disc list-inside text-blue-700 dark:text-blue-300">
                              <li>Service Task</li>
                              <li>User Task</li>
                              <li>Script Task</li>
                              <li>Business Rule Task</li>
                            </ul>
                          </div>
                          <div>
                            <p className="font-medium text-blue-900 dark:text-blue-100 mb-1">Gateways</p>
                            <ul className="list-disc list-inside text-blue-700 dark:text-blue-300">
                              <li>Exclusive Gateway</li>
                              <li>Parallel Gateway</li>
                              <li>Inclusive Gateway</li>
                              <li>Event Gateway</li>
                            </ul>
                          </div>
                          <div>
                            <p className="font-medium text-blue-900 dark:text-blue-100 mb-1">Integration</p>
                            <ul className="list-disc list-inside text-blue-700 dark:text-blue-300">
                              <li>Adapter Calls</li>
                              <li>Transformations</li>
                              <li>Routing Rules</li>
                              <li>Field Mappings</li>
                            </ul>
                          </div>
                        </div>
                      </div>

                      <div className="p-4 bg-green-50 dark:bg-green-900/20 rounded-lg">
                        <h4 className="font-medium mb-2 flex items-center gap-2">
                          <CheckCircle className="w-4 h-4 text-green-600" />
                          Process Execution Features
                        </h4>
                        <ul className="list-disc list-inside space-y-1 text-sm text-green-700 dark:text-green-300">
                          <li>Real-time process monitoring</li>
                          <li>Suspend and resume running processes</li>
                          <li>Process variable management</li>
                          <li>Execution history and logs</li>
                          <li>User task assignment and completion</li>
                          <li>Error handling and compensation</li>
                          <li>Performance metrics and statistics</li>
                        </ul>
                      </div>

                      <div className="p-4 bg-purple-50 dark:bg-purple-900/20 rounded-lg">
                        <h4 className="font-medium mb-2">Process Variables</h4>
                        <p className="text-sm text-purple-700 dark:text-purple-300 mb-2">
                          Process variables can be used to:
                        </p>
                        <ul className="list-disc list-inside space-y-1 text-sm text-purple-700 dark:text-purple-300">
                          <li>Pass data between process steps</li>
                          <li>Control gateway decisions</li>
                          <li>Configure service task parameters</li>
                          <li>Store intermediate results</li>
                          <li>Track process state</li>
                        </ul>
                        <p className="mt-3 text-sm text-purple-700 dark:text-purple-300">
                          Example variables:
                        </p>
                        <pre className="mt-2 p-3 bg-purple-100 dark:bg-purple-800/20 rounded text-xs overflow-x-auto">
                          <code>{`{
  "customerId": "12345",
  "orderAmount": 1500.00,
  "approvalRequired": true,
  "shippingAddress": {
    "street": "123 Main St",
    "city": "New York",
    "zip": "10001"
  }
}`}</code>
                        </pre>
                      </div>
                    </div>
                  </div>

                  {deployedProcessId && (
                    <div className="mt-6 p-4 bg-gray-100 dark:bg-gray-700 rounded-lg">
                      <p className="text-sm text-gray-600 dark:text-gray-400">
                        Deployed Process ID:
                      </p>
                      <code className="text-sm font-mono text-gray-800 dark:text-gray-200">
                        {deployedProcessId}
                      </code>
                    </div>
                  )}
                </div>
              )}
            </div>
          </div>
        </>
      )}
    </div>
  );
};