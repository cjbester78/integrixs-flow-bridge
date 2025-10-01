/**
 * Type guard to check if a response is an API response wrapper
 */
export function isApiResponse<T>(
 response: any
): response is { success: boolean; data: T; message?: string } {
 return (
 response &&
 typeof response === 'object' &&
 'success' in response &&
 typeof response.success === 'boolean'
 );
}

/**
 * Safely extract data from an API response that might be wrapped or unwrapped
 */
export function extractApiData<T>(
 response: T | { success: boolean; data: T; message?: string }
): T | null {
 if (isApiResponse<T>(response)) {
 return response.success ? response.data : null;
 }
 return response;
}

/**
 * Get error message from API response
 */
export function getApiError(
 response: any
): string {
 if (isApiResponse(response) && response.message) {
 return response.message;
 }
 return 'An error occurred';
}