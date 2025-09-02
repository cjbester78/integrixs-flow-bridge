import { AlertCircle } from 'lucide-react';
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert';
import { Button } from '@/components/ui/button';
import { useNavigate } from 'react-router-dom';

export function DeprecatedPageNotice() {
 const navigate = useNavigate();
;
 return (
 <div className="min-h-screen flex items-center justify-center p-8">
 <Alert className="max-w-2xl">
 <AlertCircle className="h-4 w-4" />
 <AlertTitle>This page has been deprecated</AlertTitle>
 <AlertDescription>
 <div className="space-y-4 mt-2">
 <p>
 Flow creation is now integrated into the Package Creation Wizard.
 This provides a more streamlined and comprehensive workflow for creating integration packages.
 </p>
 <p>
 The new wizard supports both Direct Integration and Orchestration flows with improved field mapping capabilities.
 </p>
 <Button
 onClick={() => navigate('/packages')}
 className="mt-4"
 >
 Go to Packages
 </Button>
 </div>
 </AlertDescription>
 </Alert>
 </div>
 );
}