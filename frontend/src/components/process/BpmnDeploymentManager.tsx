import React, { useState, useEffect, useCallback } from 'react';
import { 
  Package, 
  Upload, 
  Play, 
  CheckCircle, 
  AlertCircle,
  Loader2,
  FileCode,
  Download,
  Eye,
  Rocket
} from 'lucide-react';
import { apiClient } from '@/lib/api-client';
import { logger } from '@/lib/logger';

interface BpmnDeploymentManagerProps {
  flowId: string;
  flowName: string;
  onDeploymentComplete?: (processDefinitionId: string) => void;
}

interface DeploymentStatus {
  deployed: boolean;
  processDefinitionId?: string;
  deploymentTime?: string;
  version?: number;
  error?: string;
}

interface ValidationResult {
  valid: boolean;
  errors: string[];
  warnings: string[];
}

export const BpmnDeploymentManager: React.FC<BpmnDeploymentManagerProps> = ({
  flowId,
  flowName,
  onDeploymentComplete
}) => {
  const [deploymentStatus, setDeploymentStatus] = useState<DeploymentStatus>({ deployed: false });
  const [bpmnXml, setBpmnXml] = useState<string>('');
  const [validationResult, setValidationResult] = useState<ValidationResult | null>(null);
  const [isDeploying, setIsDeploying] = useState(false);
  const [isValidating, setIsValidating] = useState(false);
  const [showBpmnPreview, setShowBpmnPreview] = useState(false);
  const [isGeneratingBpmn, setIsGeneratingBpmn] = useState(false);

  const checkDeploymentStatus = useCallback(async () => {
    try {
      // In a real implementation, this would check if the flow is already deployed
      const processDefId = `Process_${flowId}`;
      // For now, we'll assume it's not deployed
      setDeploymentStatus({ deployed: false });
    } catch (error) {
      logger.error('Failed to check deployment status:', error);
    }
  }, [flowId]);

  // Check deployment status
  useEffect(() => {
    checkDeploymentStatus();
  }, [checkDeploymentStatus]);

  const generateBpmn = async () => {
    setIsGeneratingBpmn(true);
    try {
      // In a real implementation, this would call the BPMN converter service
      const response = await apiClient.post(`/api/flows/${flowId}/convert-to-bpmn`);
      
      if (response.data.success) {
        setBpmnXml(response.data.bpmnXml);
        setValidationResult({
          valid: true,
          errors: [],
          warnings: response.data.warnings || []
        });
      } else {
        throw new Error(response.data.error);
      }
    } catch (error) {
      logger.error('Failed to generate BPMN:', error);
      setValidationResult({
        valid: false,
        errors: ['Failed to generate BPMN from visual flow'],
        warnings: []
      });
    } finally {
      setIsGeneratingBpmn(false);
    }
  };

  const validateBpmn = async () => {
    if (!bpmnXml) {
      await generateBpmn();
      return;
    }

    setIsValidating(true);
    try {
      const response = await apiClient.post('/api/process-engine/validate', {
        bpmnXml
      });

      setValidationResult(response.data);
    } catch (error) {
      logger.error('BPMN validation failed:', error);
      setValidationResult({
        valid: false,
        errors: ['Validation service unavailable'],
        warnings: []
      });
    } finally {
      setIsValidating(false);
    }
  };

  const deployProcess = async () => {
    if (!validationResult?.valid) {
      await validateBpmn();
      return;
    }

    setIsDeploying(true);
    try {
      const response = await apiClient.post(`/api/process-engine/deploy/${flowId}`);

      if (response.data.success) {
        const { processDefinition } = response.data;
        setDeploymentStatus({
          deployed: true,
          processDefinitionId: processDefinition.id,
          deploymentTime: processDefinition.deploymentTime,
          version: processDefinition.version
        });

        if (onDeploymentComplete) {
          onDeploymentComplete(processDefinition.id);
        }

        logger.info('Process deployed successfully:', processDefinition.id);
      } else {
        throw new Error(response.data.error);
      }
    } catch (error) {
      logger.error('Process deployment failed:', error);
      setDeploymentStatus({
        deployed: false,
        error: error instanceof Error ? error.message : 'Deployment failed'
      });
    } finally {
      setIsDeploying(false);
    }
  };

  const downloadBpmn = () => {
    if (!bpmnXml) return;

    const blob = new Blob([bpmnXml], { type: 'text/xml' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `${flowName.replace(/\s+/g, '_')}.bpmn`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  };

  return (
    <div className="bg-white dark:bg-gray-800 rounded-lg shadow-sm">
      <div className="p-6">
        <div className="flex items-center gap-3 mb-6">
          <Package className="w-6 h-6 text-blue-500" />
          <h3 className="text-lg font-semibold">BPMN Process Deployment</h3>
        </div>

        {/* Deployment Status */}
        {deploymentStatus.deployed ? (
          <div className="mb-6 p-4 bg-green-50 dark:bg-green-900/20 rounded-lg">
            <div className="flex items-start gap-3">
              <CheckCircle className="w-5 h-5 text-green-500 mt-0.5" />
              <div>
                <p className="font-medium text-green-900 dark:text-green-100">
                  Process Deployed Successfully
                </p>
                <div className="mt-2 space-y-1 text-sm text-green-700 dark:text-green-300">
                  <p>Process ID: <code className="bg-green-100 dark:bg-green-800 px-2 py-0.5 rounded">
                    {deploymentStatus.processDefinitionId}
                  </code></p>
                  <p>Version: {deploymentStatus.version}</p>
                  <p>Deployed at: {new Date(deploymentStatus.deploymentTime!).toLocaleString()}</p>
                </div>
              </div>
            </div>
          </div>
        ) : deploymentStatus.error ? (
          <div className="mb-6 p-4 bg-red-50 dark:bg-red-900/20 rounded-lg">
            <div className="flex items-start gap-3">
              <AlertCircle className="w-5 h-5 text-red-500 mt-0.5" />
              <div>
                <p className="font-medium text-red-900 dark:text-red-100">
                  Deployment Failed
                </p>
                <p className="mt-1 text-sm text-red-700 dark:text-red-300">
                  {deploymentStatus.error}
                </p>
              </div>
            </div>
          </div>
        ) : null}

        {/* Actions */}
        <div className="space-y-4">
          {/* Generate/Validate BPMN */}
          <div className="flex gap-3">
            <button
              onClick={generateBpmn}
              disabled={isGeneratingBpmn}
              className="flex-1 flex items-center justify-center gap-2 px-4 py-2 bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors disabled:opacity-50"
            >
              {isGeneratingBpmn ? (
                <Loader2 className="w-4 h-4 animate-spin" />
              ) : (
                <FileCode className="w-4 h-4" />
              )}
              {isGeneratingBpmn ? 'Generating...' : 'Generate BPMN'}
            </button>

            <button
              onClick={validateBpmn}
              disabled={isValidating || !bpmnXml}
              className="flex-1 flex items-center justify-center gap-2 px-4 py-2 bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-200 dark:hover:bg-gray-600 transition-colors disabled:opacity-50"
            >
              {isValidating ? (
                <Loader2 className="w-4 h-4 animate-spin" />
              ) : (
                <CheckCircle className="w-4 h-4" />
              )}
              {isValidating ? 'Validating...' : 'Validate BPMN'}
            </button>
          </div>

          {/* Validation Results */}
          {validationResult && (
            <div className="space-y-3">
              {validationResult.errors.length > 0 && (
                <div className="p-3 bg-red-50 dark:bg-red-900/20 rounded-lg">
                  <p className="font-medium text-red-900 dark:text-red-100 mb-2">
                    Validation Errors:
                  </p>
                  <ul className="list-disc list-inside space-y-1">
                    {validationResult.errors.map((error, index) => (
                      <li key={index} className="text-sm text-red-700 dark:text-red-300">
                        {error}
                      </li>
                    ))}
                  </ul>
                </div>
              )}

              {validationResult.warnings.length > 0 && (
                <div className="p-3 bg-yellow-50 dark:bg-yellow-900/20 rounded-lg">
                  <p className="font-medium text-yellow-900 dark:text-yellow-100 mb-2">
                    Warnings:
                  </p>
                  <ul className="list-disc list-inside space-y-1">
                    {validationResult.warnings.map((warning, index) => (
                      <li key={index} className="text-sm text-yellow-700 dark:text-yellow-300">
                        {warning}
                      </li>
                    ))}
                  </ul>
                </div>
              )}

              {validationResult.valid && validationResult.errors.length === 0 && (
                <div className="p-3 bg-green-50 dark:bg-green-900/20 rounded-lg">
                  <p className="text-green-900 dark:text-green-100">
                    ✓ BPMN is valid and ready for deployment
                  </p>
                </div>
              )}
            </div>
          )}

          {/* Deploy Button */}
          <button
            onClick={deployProcess}
            disabled={isDeploying || deploymentStatus.deployed || !validationResult?.valid}
            className="w-full flex items-center justify-center gap-2 px-4 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {isDeploying ? (
              <>
                <Loader2 className="w-5 h-5 animate-spin" />
                Deploying Process...
              </>
            ) : deploymentStatus.deployed ? (
              <>
                <CheckCircle className="w-5 h-5" />
                Process Deployed
              </>
            ) : (
              <>
                <Rocket className="w-5 h-5" />
                Deploy Process
              </>
            )}
          </button>

          {/* Additional Actions */}
          {bpmnXml && (
            <div className="flex gap-3 pt-3 border-t border-gray-200 dark:border-gray-700">
              <button
                onClick={() => setShowBpmnPreview(!showBpmnPreview)}
                className="flex-1 flex items-center justify-center gap-2 px-4 py-2 text-gray-600 dark:text-gray-400 hover:bg-gray-50 dark:hover:bg-gray-700 rounded-lg transition-colors"
              >
                <Eye className="w-4 h-4" />
                {showBpmnPreview ? 'Hide' : 'Show'} BPMN
              </button>

              <button
                onClick={downloadBpmn}
                className="flex-1 flex items-center justify-center gap-2 px-4 py-2 text-gray-600 dark:text-gray-400 hover:bg-gray-50 dark:hover:bg-gray-700 rounded-lg transition-colors"
              >
                <Download className="w-4 h-4" />
                Download BPMN
              </button>
            </div>
          )}

          {/* BPMN Preview */}
          {showBpmnPreview && bpmnXml && (
            <div className="mt-4">
              <h4 className="font-medium mb-2">BPMN XML Preview:</h4>
              <pre className="bg-gray-50 dark:bg-gray-900 p-4 rounded-lg overflow-x-auto text-xs">
                <code>{bpmnXml}</code>
              </pre>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};