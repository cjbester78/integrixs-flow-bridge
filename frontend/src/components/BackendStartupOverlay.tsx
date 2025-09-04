import React, { useEffect, useState, useCallback } from 'react';
import { Loader2, ServerCrash, CheckCircle } from 'lucide-react';

interface BackendStartupOverlayProps {
 onBackendReady: () => void
}

export const BackendStartupOverlay: React.FC<BackendStartupOverlayProps> = ({ onBackendReady }) => {
 const [status, setStatus] = useState<'checking' | 'unavailable' | 'starting' | 'ready'>('checking');
 const [retryCount, setRetryCount] = useState(0);
 const [lastError, setLastError] = useState<string>('');

 const checkBackendHealth = useCallback(async () => {
    try {
 const response = await fetch('http://localhost:8080/api/health', {
 method: 'GET',
 headers: {
 'Accept': 'application/json',
 },
 });

 if (response.ok) {
 setStatus('ready');
 setTimeout(() => {
 onBackendReady();
 }, 500); // Small delay for smooth transition
 } else if (response.status === 503) {
 setStatus('starting');
 setLastError('Backend is starting up...');
 } else {
 setStatus('unavailable');
 setLastError(`Unexpected response: ${response.status}`);
 }
 } catch (error) {
 setStatus('unavailable');
 if (error instanceof Error) {
 setLastError(error.message);
 } else {
 setLastError('Connection refused - Backend may be starting');
 }
 }
 }, [onBackendReady]);

 useEffect(() => {
 // Initial check
        checkBackendHealth();

 // Set up polling interval
 const interval = setInterval(() => {
 if (status !== 'ready') {
 setRetryCount(prev => prev + 1);
                checkBackendHealth();
            }
 }, 2000); // Check every 2 seconds

        return () => clearInterval(interval);
    }, [status, checkBackendHealth]);

    if (status === 'ready') {
        return (
            <div className="fixed inset-0 bg-gradient-to-br from-blue-50 to-indigo-100 flex items-center justify-center z-50 transition-opacity duration-500">
 <div className="text-center">
 <CheckCircle className="h-16 w-16 text-green-500 mx-auto mb-4 animate-bounce" />
 <h2 className="text-2xl font-semibold text-gray-900 mb-2">Backend Ready!</h2>
 <p className="text-gray-600">Loading application...</p>
 </div>
            </div>
        );
    }

    return (
        <div className="fixed inset-0 bg-gradient-to-br from-gray-50 to-gray-100 flex items-center justify-center z-50">
 <div className="bg-white rounded-lg shadow-2xl p-8 max-w-md w-full mx-4">
 <div className="text-center">
 {status === 'checking' || status === 'starting' ? (
 <>
 <Loader2 className="h-16 w-16 text-blue-500 mx-auto mb-4 animate-spin" />
 <h1 className="text-2xl font-bold text-gray-900 mb-2">
 {status === 'checking' ? 'Checking System Status' : 'System Starting Up'}
 </h1>
 <div className="space-y-2">
 <p className="text-gray-600">
 Please wait while the backend services initialize...
 </p>
 {retryCount > 0 && (
 <p className="text-sm text-gray-500">
 Checking attempt #{retryCount}
 </p>
 )}
 </div>
 </>
 ) : (
 <>
 <ServerCrash className="h-16 w-16 text-orange-500 mx-auto mb-4" />
 <h1 className="text-2xl font-bold text-gray-900 mb-2">
 503 Service Unavailable
 </h1>
 <div className="space-y-2">
 <p className="text-gray-600">
 The backend service is currently unavailable.
 </p>
 <p className="text-sm text-gray-500 italic">
 {lastError}
 </p>
 {retryCount > 0 && (
 <p className="text-sm text-gray-500">
 Retrying... (Attempt #{retryCount})
 </p>
 )}
 </div>
 </>
 )}
 </div>

 <div className="mt-6 pt-6 border-t border-gray-200">
 <div className="text-center text-sm text-gray-500">
 <p>This may take up to 30 seconds during initial startup.</p>
 <p className="mt-1">The application will load automatically when ready.</p>
 </div>
 </div>
 </div>
        </div>
    );
};