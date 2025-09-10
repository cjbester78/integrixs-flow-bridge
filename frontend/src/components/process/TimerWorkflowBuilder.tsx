import React, { useState } from 'react';
import { 
  Clock, 
  Calendar,
  Repeat,
  Play,
  Pause,
  AlertCircle,
  CheckCircle,
  Settings,
  Plus,
  Trash2,
  Edit,
  Save,
  X
} from 'lucide-react';

interface TimerWorkflowBuilderProps {
  flowId: string;
  onTimerConfigured?: (timerConfig: TimerConfiguration) => void;
}

interface TimerConfiguration {
  id: string;
  name: string;
  type: 'cron' | 'interval' | 'date' | 'duration';
  expression: string;
  description?: string;
  action: TimerAction;
  enabled: boolean;
  retryPolicy?: RetryPolicy;
  metadata?: Record<string, any>;
}

interface TimerAction {
  type: 'start_process' | 'send_message' | 'execute_task' | 'trigger_event';
  targetId?: string;
  payload?: Record<string, any>;
}

interface RetryPolicy {
  maxRetries: number;
  retryInterval: number;
  backoffMultiplier: number;
}

interface TimerTemplate {
  name: string;
  description: string;
  type: TimerConfiguration['type'];
  expression: string;
  icon: React.ReactNode;
}

const TIMER_TEMPLATES: TimerTemplate[] = [
  {
    name: 'Every Hour',
    description: 'Runs at the start of every hour',
    type: 'cron',
    expression: '0 * * * *',
    icon: <Clock className="w-4 h-4" />
  },
  {
    name: 'Daily at 9 AM',
    description: 'Runs every day at 9:00 AM',
    type: 'cron',
    expression: '0 9 * * *',
    icon: <Calendar className="w-4 h-4" />
  },
  {
    name: 'Every 5 Minutes',
    description: 'Runs every 5 minutes',
    type: 'interval',
    expression: 'PT5M',
    icon: <Repeat className="w-4 h-4" />
  },
  {
    name: 'Weekly on Monday',
    description: 'Runs every Monday at midnight',
    type: 'cron',
    expression: '0 0 * * 1',
    icon: <Calendar className="w-4 h-4" />
  },
  {
    name: 'End of Month',
    description: 'Runs on the last day of each month',
    type: 'cron',
    expression: '0 0 L * *',
    icon: <Calendar className="w-4 h-4" />
  }
];

export const TimerWorkflowBuilder: React.FC<TimerWorkflowBuilderProps> = ({
  flowId,
  onTimerConfigured
}) => {
  const [timers, setTimers] = useState<TimerConfiguration[]>([]);
  const [editingTimer, setEditingTimer] = useState<TimerConfiguration | null>(null);
  const [showAddTimer, setShowAddTimer] = useState(false);
  const [selectedTemplate, setSelectedTemplate] = useState<TimerTemplate | null>(null);

  // Form state
  const [timerName, setTimerName] = useState('');
  const [timerType, setTimerType] = useState<TimerConfiguration['type']>('cron');
  const [timerExpression, setTimerExpression] = useState('');
  const [timerDescription, setTimerDescription] = useState('');
  const [actionType, setActionType] = useState<TimerAction['type']>('start_process');
  const [actionPayload, setActionPayload] = useState('{}');
  const [retryEnabled, setRetryEnabled] = useState(false);
  const [maxRetries, setMaxRetries] = useState(3);
  const [retryInterval, setRetryInterval] = useState(60000);

  const saveTimer = () => {
    try {
      const payload = JSON.parse(actionPayload);
      
      const newTimer: TimerConfiguration = {
        id: editingTimer?.id || `timer_${Date.now()}`,
        name: timerName,
        type: timerType,
        expression: timerExpression,
        description: timerDescription,
        action: {
          type: actionType,
          targetId: flowId,
          payload
        },
        enabled: true,
        retryPolicy: retryEnabled ? {
          maxRetries,
          retryInterval,
          backoffMultiplier: 1.5
        } : undefined
      };

      if (editingTimer) {
        setTimers(timers.map(t => t.id === editingTimer.id ? newTimer : t));
      } else {
        setTimers([...timers, newTimer]);
      }

      if (onTimerConfigured) {
        onTimerConfigured(newTimer);
      }

      resetForm();
    } catch (error) {
      console.error('Invalid JSON payload');
    }
  };

  const editTimer = (timer: TimerConfiguration) => {
    setEditingTimer(timer);
    setTimerName(timer.name);
    setTimerType(timer.type);
    setTimerExpression(timer.expression);
    setTimerDescription(timer.description || '');
    setActionType(timer.action.type);
    setActionPayload(JSON.stringify(timer.action.payload || {}, null, 2));
    setRetryEnabled(!!timer.retryPolicy);
    setMaxRetries(timer.retryPolicy?.maxRetries || 3);
    setRetryInterval(timer.retryPolicy?.retryInterval || 60000);
    setShowAddTimer(true);
  };

  const deleteTimer = (timerId: string) => {
    setTimers(timers.filter(t => t.id !== timerId));
  };

  const toggleTimer = (timerId: string) => {
    setTimers(timers.map(t => 
      t.id === timerId ? { ...t, enabled: !t.enabled } : t
    ));
  };

  const applyTemplate = (template: TimerTemplate) => {
    setTimerName(template.name);
    setTimerType(template.type);
    setTimerExpression(template.expression);
    setTimerDescription(template.description);
    setSelectedTemplate(template);
  };

  const resetForm = () => {
    setEditingTimer(null);
    setTimerName('');
    setTimerType('cron');
    setTimerExpression('');
    setTimerDescription('');
    setActionType('start_process');
    setActionPayload('{}');
    setRetryEnabled(false);
    setMaxRetries(3);
    setRetryInterval(60000);
    setShowAddTimer(false);
    setSelectedTemplate(null);
  };

  const getNextExecution = (type: string, expression: string): string => {
    // Simplified next execution calculation
    if (type === 'interval') {
      return 'In ' + expression.replace('PT', '').toLowerCase();
    }
    if (type === 'cron') {
      // This would use a cron parser in real implementation
      return 'Next run time based on cron';
    }
    return 'Scheduled';
  };

  const formatExpression = (type: string, expression: string): string => {
    if (type === 'cron') return expression;
    if (type === 'interval') return expression.replace('PT', '').toLowerCase();
    if (type === 'duration') return expression.replace('P', '').toLowerCase();
    return expression;
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow-sm p-6">
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center gap-3">
            <Clock className="w-6 h-6 text-orange-500" />
            <h3 className="text-lg font-semibold">Timer-Based Workflows</h3>
          </div>
          <button
            onClick={() => setShowAddTimer(true)}
            className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
          >
            <Plus className="w-4 h-4" />
            Add Timer
          </button>
        </div>

        <p className="text-gray-600 dark:text-gray-400">
          Configure timers to automatically trigger workflow execution based on schedules, intervals, or specific dates.
        </p>
      </div>

      {/* Timer Templates */}
      <div className="bg-white dark:bg-gray-800 rounded-lg shadow-sm p-6">
        <h4 className="font-medium mb-4">Quick Templates</h4>
        <div className="grid grid-cols-2 md:grid-cols-3 gap-3">
          {TIMER_TEMPLATES.map((template, index) => (
            <button
              key={index}
              onClick={() => applyTemplate(template)}
              className="flex items-start gap-3 p-3 border border-gray-200 dark:border-gray-700 rounded-lg hover:border-blue-500 hover:bg-blue-50 dark:hover:bg-blue-900/20 transition-all text-left"
            >
              <div className="p-2 bg-gray-100 dark:bg-gray-700 rounded">
                {template.icon}
              </div>
              <div>
                <p className="font-medium text-sm">{template.name}</p>
                <p className="text-xs text-gray-600 dark:text-gray-400">
                  {template.description}
                </p>
              </div>
            </button>
          ))}
        </div>
      </div>

      {/* Active Timers */}
      {timers.length > 0 && (
        <div className="bg-white dark:bg-gray-800 rounded-lg shadow-sm p-6">
          <h4 className="font-medium mb-4">Active Timers</h4>
          <div className="space-y-3">
            {timers.map(timer => (
              <div
                key={timer.id}
                className={`border rounded-lg p-4 ${
                  timer.enabled
                    ? 'border-gray-200 dark:border-gray-700'
                    : 'border-gray-200 dark:border-gray-700 opacity-50'
                }`}
              >
                <div className="flex items-start justify-between">
                  <div className="flex-1">
                    <div className="flex items-center gap-3">
                      <h5 className="font-medium">{timer.name}</h5>
                      {timer.enabled ? (
                        <span className="flex items-center gap-1 text-xs text-green-600 bg-green-50 px-2 py-1 rounded-full">
                          <CheckCircle className="w-3 h-3" />
                          Active
                        </span>
                      ) : (
                        <span className="flex items-center gap-1 text-xs text-gray-600 bg-gray-50 px-2 py-1 rounded-full">
                          <Pause className="w-3 h-3" />
                          Paused
                        </span>
                      )}
                    </div>
                    {timer.description && (
                      <p className="text-sm text-gray-600 dark:text-gray-400 mt-1">
                        {timer.description}
                      </p>
                    )}
                    <div className="flex items-center gap-4 mt-2 text-xs text-gray-500">
                      <span>Type: {timer.type}</span>
                      <span>Expression: <code className="bg-gray-100 dark:bg-gray-700 px-1 rounded">
                        {formatExpression(timer.type, timer.expression)}
                      </code></span>
                      <span>Next: {getNextExecution(timer.type, timer.expression)}</span>
                    </div>
                    <div className="mt-2 text-xs">
                      <span className="text-gray-500">Action: </span>
                      <span className="font-medium">{timer.action.type.replace('_', ' ')}</span>
                      {timer.retryPolicy && (
                        <span className="ml-2 text-gray-500">
                          • Retry: {timer.retryPolicy.maxRetries} times
                        </span>
                      )}
                    </div>
                  </div>
                  <div className="flex items-center gap-2">
                    <button
                      onClick={() => toggleTimer(timer.id)}
                      className="p-2 text-gray-400 hover:text-gray-600 dark:hover:text-gray-300"
                      title={timer.enabled ? 'Pause' : 'Resume'}
                    >
                      {timer.enabled ? <Pause className="w-4 h-4" /> : <Play className="w-4 h-4" />}
                    </button>
                    <button
                      onClick={() => editTimer(timer)}
                      className="p-2 text-gray-400 hover:text-gray-600 dark:hover:text-gray-300"
                      title="Edit"
                    >
                      <Edit className="w-4 h-4" />
                    </button>
                    <button
                      onClick={() => deleteTimer(timer.id)}
                      className="p-2 text-red-400 hover:text-red-600"
                      title="Delete"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Add/Edit Timer Form */}
      {showAddTimer && (
        <div className="bg-white dark:bg-gray-800 rounded-lg shadow-sm p-6">
          <div className="flex items-center justify-between mb-4">
            <h4 className="font-medium">
              {editingTimer ? 'Edit Timer' : 'Add New Timer'}
            </h4>
            <button
              onClick={resetForm}
              className="text-gray-400 hover:text-gray-600"
            >
              <X className="w-5 h-5" />
            </button>
          </div>

          <div className="space-y-4">
            {/* Basic Information */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium mb-2">Timer Name</label>
                <input
                  type="text"
                  value={timerName}
                  onChange={(e) => setTimerName(e.target.value)}
                  placeholder="e.g., Daily Report Generator"
                  className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg"
                />
              </div>
              
              <div>
                <label className="block text-sm font-medium mb-2">Timer Type</label>
                <select
                  value={timerType}
                  onChange={(e) => setTimerType(e.target.value as any)}
                  className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg"
                >
                  <option value="cron">Cron Expression</option>
                  <option value="interval">Fixed Interval</option>
                  <option value="date">Specific Date/Time</option>
                  <option value="duration">After Duration</option>
                </select>
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium mb-2">
                {timerType === 'cron' ? 'Cron Expression' :
                 timerType === 'interval' ? 'Interval (ISO 8601)' :
                 timerType === 'date' ? 'Date/Time' :
                 'Duration (ISO 8601)'}
              </label>
              <input
                type="text"
                value={timerExpression}
                onChange={(e) => setTimerExpression(e.target.value)}
                placeholder={
                  timerType === 'cron' ? '0 0 * * *' :
                  timerType === 'interval' ? 'PT1H' :
                  timerType === 'date' ? '2024-12-31T23:59:59' :
                  'P1D'
                }
                className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg font-mono"
              />
              {selectedTemplate && (
                <p className="text-xs text-blue-600 mt-1">
                  Using template: {selectedTemplate.name}
                </p>
              )}
            </div>

            <div>
              <label className="block text-sm font-medium mb-2">Description (optional)</label>
              <input
                type="text"
                value={timerDescription}
                onChange={(e) => setTimerDescription(e.target.value)}
                placeholder="Describe what this timer does"
                className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg"
              />
            </div>

            {/* Action Configuration */}
            <div className="border-t pt-4">
              <h5 className="font-medium mb-3">Action Configuration</h5>
              
              <div className="space-y-3">
                <div>
                  <label className="block text-sm font-medium mb-2">Action Type</label>
                  <select
                    value={actionType}
                    onChange={(e) => setActionType(e.target.value as any)}
                    className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg"
                  >
                    <option value="start_process">Start Process Instance</option>
                    <option value="send_message">Send Message Event</option>
                    <option value="execute_task">Execute Service Task</option>
                    <option value="trigger_event">Trigger Custom Event</option>
                  </select>
                </div>

                <div>
                  <label className="block text-sm font-medium mb-2">Payload (JSON)</label>
                  <textarea
                    value={actionPayload}
                    onChange={(e) => setActionPayload(e.target.value)}
                    rows={4}
                    className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg font-mono text-sm"
                    placeholder='{"key": "value"}'
                  />
                </div>
              </div>
            </div>

            {/* Retry Policy */}
            <div className="border-t pt-4">
              <div className="flex items-center gap-2 mb-3">
                <input
                  type="checkbox"
                  id="retryEnabled"
                  checked={retryEnabled}
                  onChange={(e) => setRetryEnabled(e.target.checked)}
                  className="rounded"
                />
                <label htmlFor="retryEnabled" className="font-medium">
                  Enable Retry Policy
                </label>
              </div>
              
              {retryEnabled && (
                <div className="grid grid-cols-2 gap-3 ml-6">
                  <div>
                    <label className="block text-sm mb-1">Max Retries</label>
                    <input
                      type="number"
                      value={maxRetries}
                      onChange={(e) => setMaxRetries(Number(e.target.value))}
                      min={1}
                      max={10}
                      className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg"
                    />
                  </div>
                  <div>
                    <label className="block text-sm mb-1">Retry Interval (ms)</label>
                    <input
                      type="number"
                      value={retryInterval}
                      onChange={(e) => setRetryInterval(Number(e.target.value))}
                      min={1000}
                      step={1000}
                      className="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg"
                    />
                  </div>
                </div>
              )}
            </div>

            {/* Actions */}
            <div className="flex gap-2 pt-4">
              <button
                onClick={saveTimer}
                disabled={!timerName || !timerExpression}
                className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50"
              >
                <Save className="w-4 h-4" />
                {editingTimer ? 'Update Timer' : 'Create Timer'}
              </button>
              <button
                onClick={resetForm}
                className="px-4 py-2 text-gray-600 hover:bg-gray-100 dark:hover:bg-gray-700 rounded-lg"
              >
                Cancel
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Information */}
      <div className="bg-blue-50 dark:bg-blue-900/20 rounded-lg p-4">
        <div className="flex items-start gap-3">
          <Settings className="w-5 h-5 text-blue-600 dark:text-blue-400 mt-0.5" />
          <div className="text-sm text-blue-700 dark:text-blue-300">
            <p className="font-medium mb-1">Timer Expression Formats:</p>
            <ul className="list-disc list-inside space-y-1 ml-2">
              <li><strong>Cron:</strong> Standard cron syntax (e.g., "0 0 * * *" for daily at midnight)</li>
              <li><strong>Interval:</strong> ISO 8601 duration (e.g., "PT1H" for every hour)</li>
              <li><strong>Date:</strong> ISO 8601 date/time (e.g., "2024-12-31T23:59:59")</li>
              <li><strong>Duration:</strong> ISO 8601 period (e.g., "P7D" for after 7 days)</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  );
};