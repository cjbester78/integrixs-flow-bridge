/**
 * Hook to create a reset function for error boundaries
 */
export const useErrorHandler = () => {
 return (error: Error) => {
 throw error; // This will be caught by the nearest error boundary
 };
};