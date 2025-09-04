import { api } from './api';
import { DeploymentInfo } from '@/types/deployment';

export interface DeploymentResponse {
 success: boolean;
 data?: DeploymentInfo;
 error?: string;
}

class DeploymentService {
 async deployFlow(flowId: string): Promise<DeploymentResponse> {
  try {
   const response = await api.post<DeploymentInfo>(`/flows/${flowId}/deployment/deploy`);
   return response;
  } catch (error: any) {
   // Extract error message from ApiError or other error types
   let errorMessage = 'Failed to deploy flow';
   
   if (error.message) {
    errorMessage = error.message;
   } else if (error.response?.data?.message) {
    errorMessage = error.response.data.message;
   } else if (error.response?.data?.error) {
    errorMessage = error.response.data.error;
   } else if (typeof error === 'string') {
    errorMessage = error;
   }

   return {
    success: false,
    error: errorMessage
   };
  }
 }

 async undeployFlow(flowId: string): Promise<{ success: boolean; error?: string }> {
  try {
   const response = await api.post(`/flows/${flowId}/deployment/undeploy`);
   return { success: response.success };
  } catch (error) {
   return {
    success: false,
    error: error instanceof Error ? error.message : 'Failed to undeploy flow'
   };
  }
 }

 async getDeploymentInfo(flowId: string): Promise<DeploymentResponse> {
  try {
   const response = await api.get<DeploymentInfo>(`/flows/${flowId}/deployment`);
   return response;
  } catch (error) {
   return {
    success: false,
    error: error instanceof Error ? error.message : 'Failed to fetch deployment info'
   };
  }
 }
}

export const deploymentService = new DeploymentService();