import React, { useState, useEffect, useCallback } from 'react';
import { 
  Bug, 
  Play, 
  Pause, 
  SkipForward,
  RotateCcw,
  ChevronRight,
  ChevronDown,
  Code,
  Database,
  Clock,
  AlertCircle,
  CheckCircle,
  Info,
  Layers
} from 'lucide-react';
import { apiClient } from '@/lib/api-client';
import { logger } from '@/lib/logger';

interface ProcessDebuggerProps {
  processInstanceId: string;
  onStepChange?: (stepId: string) => void;
}

interface DebugStep {
  id: string;
  name: string;
  type: string;
  status: 'pending' | 'running' | 'completed' | 'failed';
  timestamp: string;
  duration?: number;
  input?: any;
  output?: any;
  error?: string;
  logs: string[];
  children?: DebugStep[];
}

interface Breakpoint {
  stepId: string;
  condition?: string;
  hitCount: number;
}

export const ProcessDebugger: React.FC<ProcessDebuggerProps> = ({
  processInstanceId,
  onStepChange
}) => {
  const [debugSteps, setDebugSteps] = useState<DebugStep[]>([]);
  const [currentStepIndex, setCurrentStepIndex] = useState(0);
  const [selectedStep, setSelectedStep] = useState<DebugStep | null>(null);
  const [isPaused, setIsPaused] = useState(true);
  const [breakpoints, setBreakpoints] = useState<Breakpoint[]>([]);
  const [expandedSteps, setExpandedSteps] = useState<Set<string>>(new Set());
  const [showVariables, setShowVariables] = useState(true);
  const [showLogs, setShowLogs] = useState(true);
  const [stepSpeed, setStepSpeed] = useState(1000); // ms between steps

  const loadDebugData = useCallback(async () => {
    try {
      const response = await apiClient.get(`/api/process-engine/instance/${processInstanceId}`);
      const instance = response.data;
      
      // Parse execution log into debug steps
      const steps = parseExecutionLog(instance.executionLog, instance.variables);
      setDebugSteps(steps);
      
      // Find current step
      const currentIndex = steps.findIndex(step => step.status === 'running');
      setCurrentStepIndex(currentIndex >= 0 ? currentIndex : steps.length - 1);
      
      if (steps.length > 0) {
        setSelectedStep(steps[currentIndex >= 0 ? currentIndex : 0]);
      }
    } catch (error) {
      logger.error('Failed to load debug data:', error);
    }
  }, [processInstanceId, parseExecutionLog]);

  // Load debug data
  useEffect(() => {
    loadDebugData();
  }, [loadDebugData]);

  const parseExecutionLog = useCallback((logs: string[], variables: any): DebugStep[] => {
    const steps: DebugStep[] = [];
    let currentStep: DebugStep | null = null;

    logs.forEach(log => {
      const timestamp = log.substring(0, 23);
      const message = log.substring(26);

      // Check for step execution
      const stepMatch = message.match(/Executing step: (.+)/);
      if (stepMatch) {
        if (currentStep) {
          steps.push(currentStep);
        }
        currentStep = {
          id: `step_${steps.length}`,
          name: stepMatch[1],
          type: 'unknown',
          status: 'running',
          timestamp,
          logs: [log],
          input: variables
        };
      } else if (currentStep) {
        // Add log to current step
        currentStep.logs.push(log);

        // Check for completion
        if (message.includes(`Step completed: ${currentStep.name}`)) {
          currentStep.status = 'completed';
          const startTime = new Date(currentStep.timestamp).getTime();
          const endTime = new Date(timestamp).getTime();
          currentStep.duration = endTime - startTime;
        }

        // Check for failure
        if (message.includes(`Step failed: ${currentStep.name}`)) {
          currentStep.status = 'failed';
          const errorMatch = message.match(/Step failed: .+ - (.+)/);
          if (errorMatch) {
            currentStep.error = errorMatch[1];
          }
        }

        // Extract step type
        if (message.includes('service task')) {
          currentStep.type = 'service';
        } else if (message.includes('user task')) {
          currentStep.type = 'user';
        } else if (message.includes('script task')) {
          currentStep.type = 'script';
        } else if (message.includes('gateway')) {
          currentStep.type = 'gateway';
        }
      }
    });

    if (currentStep) {
      steps.push(currentStep);
    }

    return steps;
  }, []);

  const calculateDuration = (start: string, end: string): number => {
    const startTime = new Date(start).getTime();
    const endTime = new Date(end).getTime();
    return endTime - startTime;
  };

  const toggleBreakpoint = (stepId: string) => {
    const existing = breakpoints.find(bp => bp.stepId === stepId);
    if (existing) {
      setBreakpoints(breakpoints.filter(bp => bp.stepId !== stepId));
    } else {
      setBreakpoints([...breakpoints, { stepId, hitCount: 0 }]);
    }
  };

  const stepForward = useCallback(() => {
    if (currentStepIndex < debugSteps.length - 1) {
      const nextIndex = currentStepIndex + 1;
      setCurrentStepIndex(nextIndex);
      setSelectedStep(debugSteps[nextIndex]);
      
      if (onStepChange) {
        onStepChange(debugSteps[nextIndex].id);
      }

      // Check for breakpoint
      const breakpoint = breakpoints.find(bp => bp.stepId === debugSteps[nextIndex].id);
      if (breakpoint) {
        breakpoint.hitCount++;
        setIsPaused(true);
      }
    }
  }, [currentStepIndex, debugSteps, onStepChange, breakpoints]);

  const restart = () => {
    setCurrentStepIndex(0);
    if (debugSteps.length > 0) {
      setSelectedStep(debugSteps[0]);
      if (onStepChange) {
        onStepChange(debugSteps[0].id);
      }
    }
  };

  const toggleExpanded = (stepId: string) => {
    const newExpanded = new Set(expandedSteps);
    if (newExpanded.has(stepId)) {
      newExpanded.delete(stepId);
    } else {
      newExpanded.add(stepId);
    }
    setExpandedSteps(newExpanded);
  };

  const getStepIcon = (step: DebugStep) => {
    if (step.status === 'failed') return <AlertCircle className="w-4 h-4 text-red-500" />;
    if (step.status === 'completed') return <CheckCircle className="w-4 h-4 text-green-500" />;
    if (step.status === 'running') return <Play className="w-4 h-4 text-blue-500" />;
    return <Clock className="w-4 h-4 text-gray-400" />;
  };

  // Auto-step when not paused
  useEffect(() => {
    if (!isPaused && currentStepIndex < debugSteps.length - 1) {
      const timer = setTimeout(stepForward, stepSpeed);
      return () => clearTimeout(timer);
    }
  }, [isPaused, currentStepIndex, stepSpeed, debugSteps.length, stepForward]);

  return (
    <div className="bg-white dark:bg-gray-800 rounded-lg shadow-sm">
      {/* Header */}
      <div className="p-4 border-b border-gray-200 dark:border-gray-700">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-3">
            <Bug className="w-5 h-5 text-purple-500" />
            <h3 className="font-semibold">Process Debugger</h3>
          </div>
          
          {/* Controls */}
          <div className="flex items-center gap-2">
            <button
              onClick={() => setIsPaused(!isPaused)}
              className="p-2 rounded hover:bg-gray-100 dark:hover:bg-gray-700"
              title={isPaused ? 'Resume' : 'Pause'}
            >
              {isPaused ? <Play className="w-4 h-4" /> : <Pause className="w-4 h-4" />}
            </button>
            <button
              onClick={stepForward}
              disabled={currentStepIndex >= debugSteps.length - 1}
              className="p-2 rounded hover:bg-gray-100 dark:hover:bg-gray-700 disabled:opacity-50"
              title="Step Forward"
            >
              <SkipForward className="w-4 h-4" />
            </button>
            <button
              onClick={restart}
              className="p-2 rounded hover:bg-gray-100 dark:hover:bg-gray-700"
              title="Restart"
            >
              <RotateCcw className="w-4 h-4" />
            </button>
            
            <div className="ml-4 flex items-center gap-2 text-sm">
              <label>Speed:</label>
              <select
                value={stepSpeed}
                onChange={(e) => setStepSpeed(Number(e.target.value))}
                className="px-2 py-1 border border-gray-300 dark:border-gray-600 rounded"
              >
                <option value={500}>0.5s</option>
                <option value={1000}>1s</option>
                <option value={2000}>2s</option>
                <option value={5000}>5s</option>
              </select>
            </div>
          </div>
        </div>
      </div>

      <div className="flex h-[600px]">
        {/* Steps Panel */}
        <div className="w-1/3 border-r border-gray-200 dark:border-gray-700 overflow-y-auto">
          <div className="p-4">
            <h4 className="font-medium mb-3 flex items-center gap-2">
              <Layers className="w-4 h-4" />
              Execution Steps
            </h4>
            <div className="space-y-1">
              {debugSteps.map((step, index) => (
                <div
                  key={step.id}
                  className={`
                    group relative rounded cursor-pointer transition-all
                    ${selectedStep?.id === step.id ? 'bg-blue-50 dark:bg-blue-900/20' : 'hover:bg-gray-50 dark:hover:bg-gray-700'}
                    ${index === currentStepIndex ? 'ring-2 ring-blue-500' : ''}
                  `}
                >
                  <div
                    onClick={() => setSelectedStep(step)}
                    className="flex items-center gap-2 p-2"
                  >
                    <button
                      onClick={(e) => {
                        e.stopPropagation();
                        toggleBreakpoint(step.id);
                      }}
                      className={`w-3 h-3 rounded-full border-2 ${
                        breakpoints.find(bp => bp.stepId === step.id)
                          ? 'bg-red-500 border-red-500'
                          : 'border-gray-300 group-hover:border-gray-400'
                      }`}
                    />
                    
                    {step.children && (
                      <button
                        onClick={(e) => {
                          e.stopPropagation();
                          toggleExpanded(step.id);
                        }}
                        className="p-0.5"
                      >
                        {expandedSteps.has(step.id) ? (
                          <ChevronDown className="w-3 h-3" />
                        ) : (
                          <ChevronRight className="w-3 h-3" />
                        )}
                      </button>
                    )}
                    
                    {getStepIcon(step)}
                    
                    <div className="flex-1 min-w-0">
                      <div className="text-sm font-medium truncate">{step.name}</div>
                      <div className="text-xs text-gray-500 dark:text-gray-400">
                        {step.type} • {step.timestamp.substring(11, 19)}
                        {step.duration && ` • ${step.duration}ms`}
                      </div>
                    </div>
                  </div>
                  
                  {/* Children */}
                  {step.children && expandedSteps.has(step.id) && (
                    <div className="ml-8 border-l border-gray-200 dark:border-gray-700">
                      {step.children.map(child => (
                        <div key={child.id} className="flex items-center gap-2 p-2 pl-4">
                          {getStepIcon(child)}
                          <div className="text-sm">{child.name}</div>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Details Panel */}
        <div className="flex-1 overflow-y-auto">
          {selectedStep ? (
            <div className="p-4 space-y-4">
              <div>
                <h4 className="font-medium mb-2">{selectedStep.name}</h4>
                <div className="flex items-center gap-4 text-sm text-gray-600 dark:text-gray-400">
                  <span>{selectedStep.type}</span>
                  <span>•</span>
                  <span className={`font-medium ${
                    selectedStep.status === 'completed' ? 'text-green-600' :
                    selectedStep.status === 'failed' ? 'text-red-600' :
                    selectedStep.status === 'running' ? 'text-blue-600' :
                    'text-gray-600'
                  }`}>
                    {selectedStep.status}
                  </span>
                  {selectedStep.duration && (
                    <>
                      <span>•</span>
                      <span>{selectedStep.duration}ms</span>
                    </>
                  )}
                </div>
              </div>

              {selectedStep.error && (
                <div className="p-3 bg-red-50 dark:bg-red-900/20 rounded-lg">
                  <div className="flex items-start gap-2">
                    <AlertCircle className="w-4 h-4 text-red-500 mt-0.5" />
                    <div>
                      <p className="font-medium text-red-900 dark:text-red-100">Error</p>
                      <p className="text-sm text-red-700 dark:text-red-300 mt-1">
                        {selectedStep.error}
                      </p>
                    </div>
                  </div>
                </div>
              )}

              {/* Variables */}
              {showVariables && selectedStep.input && (
                <div>
                  <button
                    onClick={() => setShowVariables(!showVariables)}
                    className="flex items-center gap-2 font-medium mb-2"
                  >
                    <Database className="w-4 h-4" />
                    Input Variables
                    <ChevronDown className={`w-4 h-4 transition-transform ${showVariables ? '' : '-rotate-90'}`} />
                  </button>
                  <pre className="bg-gray-50 dark:bg-gray-900 p-3 rounded-lg text-xs overflow-x-auto">
                    <code>{JSON.stringify(selectedStep.input, null, 2)}</code>
                  </pre>
                </div>
              )}

              {selectedStep.output && (
                <div>
                  <h5 className="font-medium mb-2 flex items-center gap-2">
                    <Code className="w-4 h-4" />
                    Output
                  </h5>
                  <pre className="bg-gray-50 dark:bg-gray-900 p-3 rounded-lg text-xs overflow-x-auto">
                    <code>{JSON.stringify(selectedStep.output, null, 2)}</code>
                  </pre>
                </div>
              )}

              {/* Logs */}
              {showLogs && selectedStep.logs.length > 0 && (
                <div>
                  <button
                    onClick={() => setShowLogs(!showLogs)}
                    className="flex items-center gap-2 font-medium mb-2"
                  >
                    <Info className="w-4 h-4" />
                    Execution Logs ({selectedStep.logs.length})
                    <ChevronDown className={`w-4 h-4 transition-transform ${showLogs ? '' : '-rotate-90'}`} />
                  </button>
                  <div className="bg-gray-50 dark:bg-gray-900 p-3 rounded-lg space-y-1 max-h-48 overflow-y-auto">
                    {selectedStep.logs.map((log, index) => (
                      <p key={index} className="text-xs font-mono text-gray-600 dark:text-gray-400">
                        {log}
                      </p>
                    ))}
                  </div>
                </div>
              )}

              {/* Breakpoint Info */}
              {breakpoints.find(bp => bp.stepId === selectedStep.id) && (
                <div className="p-3 bg-yellow-50 dark:bg-yellow-900/20 rounded-lg">
                  <p className="text-sm text-yellow-800 dark:text-yellow-200">
                    Breakpoint set • Hit count: {breakpoints.find(bp => bp.stepId === selectedStep.id)?.hitCount || 0}
                  </p>
                </div>
              )}
            </div>
          ) : (
            <div className="flex items-center justify-center h-full text-gray-500 dark:text-gray-400">
              <p>Select a step to view details</p>
            </div>
          )}
        </div>
      </div>

      {/* Status Bar */}
      <div className="px-4 py-2 border-t border-gray-200 dark:border-gray-700 text-sm text-gray-600 dark:text-gray-400">
        <div className="flex items-center justify-between">
          <span>
            Step {currentStepIndex + 1} of {debugSteps.length}
          </span>
          <span>
            Breakpoints: {breakpoints.length}
          </span>
        </div>
      </div>
    </div>
  );
};