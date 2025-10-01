import { apiClient } from '@/lib/api-client';
import type {
 IntegrationPackage,
 CreatePackageRequest,
 UpdatePackageRequest,
 PackageValidationResult,
 AddComponentRequest
} from '@/types/package';

const API_PREFIX = '/integration-packages';

export const packageService = {
 // Create a new package
 createPackage: async (data: CreatePackageRequest) => {
 const response = await apiClient.post<IntegrationPackage>(API_PREFIX, data);
 return response;
 },

 // Update an existing package
 updatePackage: async (packageId: string, data: UpdatePackageRequest) => {
 const response = await apiClient.put<IntegrationPackage>(`${API_PREFIX}/${packageId}`, data);
 return response;
 },

 // Get a single package by ID
 getPackage: async (packageId: string) => {
 const response = await apiClient.get<IntegrationPackage>(`${API_PREFIX}/${packageId}`);
 return response;
 },

 // Get all packages with pagination
 getAllPackages: async (page: number = 0, size: number = 20, sort?: string) => {
 const params = new URLSearchParams({
 page: page.toString(),
 size: size.toString()
 });
 if (sort) params.append('sort', sort);
 const response = await apiClient.get(`${API_PREFIX}?${params.toString()}`);
 return response;
 },

 // Search packages
 searchPackages: async (params: {
 searchTerm?: string;
 status?: string;
 syncType?: string;
 transformationRequired?: boolean;
 isTemplate?: boolean;
 page?: number;
 size?: number;
 }) => {
 const queryParams = new URLSearchParams();

 if (params.searchTerm) queryParams.append('searchTerm', params.searchTerm);
 if (params.status) queryParams.append('status', params.status);
 if (params.syncType) queryParams.append('syncType', params.syncType);
 if (params.transformationRequired !== undefined) {
 queryParams.append('transformationRequired', params.transformationRequired.toString());
 }
 if (params.isTemplate !== undefined) {
 queryParams.append('isTemplate', params.isTemplate.toString());
 }
 queryParams.append('page', (params.page || 0).toString());
 queryParams.append('size', (params.size || 20).toString());

 const response = await apiClient.get(`${API_PREFIX}/search?${queryParams.toString()}`);
 return response;
 },

 // Add a component to a package
 addComponent: async (packageId: string, data: AddComponentRequest) => {
 const params = new URLSearchParams({
 componentType: data.componentType,
 componentId: data.componentId
 });


 const response = await apiClient.post(
 `${API_PREFIX}/${packageId}/components?${params.toString()}`,
 data.configuration || {}
 );
 return response;
 },

 // Remove a component from a package
 removeComponent: async (packageId: string, componentId: string) => {
 const response = await apiClient.delete(
 `${API_PREFIX}/${packageId}/components/${componentId}`
 );
 return response;
 },

 // Validate a package
 validatePackage: async (packageId: string) => {
 const response = await apiClient.post<PackageValidationResult>(
 `${API_PREFIX}/${packageId}/validate`
 );
 return response;
 },

 // Deploy a package
 deployPackage: async (packageId: string, environment: string, notes?: string) => {
 const params = new URLSearchParams({ environment });
 if (notes) params.append('notes', notes);

 const response = await apiClient.post(
 `${API_PREFIX}/${packageId}/deploy?${params.toString()}`
 );
 return response;
 },

 // Delete a package
 deletePackage: async (packageId: string) => {
 const response = await apiClient.delete(`${API_PREFIX}/${packageId}`);
 return response;
 },

 // Export a package
 exportPackage: async (packageId: string) => {
 const response = await fetch(`/api${API_PREFIX}/${packageId}/export`, {
 method: 'GET',
 headers: {
 'Authorization': `Bearer ${localStorage.getItem('token')}`
 }
 });

 if (!response.ok) {
 throw new Error('Failed to export package');
 }

 const blob = await response.blob();
 const url = window.URL.createObjectURL(blob);
 const link = document.createElement('a');
 link.href = url;
 link.download = `package_${packageId}_export.json`;
 document.body.appendChild(link);
 link.click();
 document.body.removeChild(link);
 window.URL.revokeObjectURL(url);

 return { success: true };
  },

 // Import a package
 importPackage: async (file: File) => {
 const formData = new FormData();
 formData.append('file', file);
 const response = await fetch(`/api${API_PREFIX}/import`, {
 method: 'POST',
 headers: {
 'Authorization': `Bearer ${localStorage.getItem('token')}`
 },
 body: formData
 });

 if (!response.ok) {
 const error = await response.json();
 throw new Error(error.message || 'Failed to import package');
 }

 const result = await response.json();
 return result;
   }
};
