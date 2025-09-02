import { logger, LogCategory } from '@/lib/logger';


export const TestPage = () => {
 logger.info(LogCategory.SYSTEM, 'TestPage - rendering without React Query');
 return (
 <div className="p-6">
 <h1 className="text-2xl font-bold mb-4">Test Page</h1>
 <p>This is a simple test page without React Query to verify navigation works.</p>
 </div>
 );
};