import { useState, useEffect, useCallback } from 'react';
import {
 Dialog,
 DialogContent,
 DialogDescription,
 DialogHeader,
 DialogTitle,
} from '@/components/ui/dialog';
import { Badge } from '@/components/ui/badge';
import { ScrollArea } from '@/components/ui/scroll-area';
import { Button } from '@/components/ui/button';
import { Skeleton } from '@/components/ui/skeleton';
import { RefreshCw, CheckCircle2, XCircle } from 'lucide-react';
import { externalAuthService } from '@/services/externalAuthService';
import { AuthAttemptLog } from '@/types/externalAuth';
import { format } from 'date-fns';
import { isApiResponse } from '@/lib/api-response-utils';
import { logger, LogCategory } from '@/lib/logger';

interface AuthAttemptLogsDialogProps {
 configId: string;
 open: boolean;
 onOpenChange: (open: boolean) => void;
}

export function AuthAttemptLogsDialog({ configId, open, onOpenChange }: AuthAttemptLogsDialogProps) {
 const [attempts, setAttempts] = useState<AuthAttemptLog[]>([]);
 const [isLoading, setIsLoading] = useState(true);

 const fetchAttempts = useCallback(async () => {
 setIsLoading(true);
 try {
const response = await externalAuthService.getAuthAttempts(configId, 100);
      if (isApiResponse<AuthAttemptLog[]>(response)) {
        if (response.success && response.data) {
          setAttempts(response.data);
        }
      } else if (Array.isArray(response)) {
        setAttempts(response);
      }
} catch (error) {
 logger.error(LogCategory.AUTH, 'Failed to fetch auth attempts', { error: error });
 setAttempts([]);
 } finally {
 setIsLoading(false);
 }
 }, [configId]);

 useEffect(() => {
 if (open) {
 fetchAttempts();
 }
 }, [open, configId, fetchAttempts]);

 return (
 <Dialog open={open} onOpenChange={onOpenChange}>
 <DialogContent className="max-w-4xl">
 <DialogHeader>
 <div className="flex items-center justify-between">
 <div>
 <DialogTitle>Authentication Attempt Logs</DialogTitle>
 <DialogDescription>
 Recent authentication attempts for this configuration
 </DialogDescription>
 </div>
 <Button
 variant="outline"
 size="sm"
 onClick={fetchAttempts}
 disabled={isLoading}
 >
 <RefreshCw className="h-4 w-4 mr-2" />
 Refresh
 </Button>
 </div>
 </DialogHeader>

 <ScrollArea className="h-[500px] w-full rounded-md border p-4">
 {isLoading ? (
 <div className="space-y-3">
 {[...Array(5)].map((_, i) => (
 <div key={i} className="flex items-center justify-between p-3 border rounded-lg">
 <Skeleton className="h-4 w-24" />
 <Skeleton className="h-4 w-32" />
 <Skeleton className="h-4 w-20" />
 <Skeleton className="h-4 w-16" />
 </div>
 ))}
 </div>
 ) : attempts.length === 0 ? (
 <div className="text-center text-muted-foreground py-8">
 No authentication attempts recorded yet
 </div>
 ) : (
 <div className="space-y-3">
 {attempts.map((attempt) => (
 <div
 key={attempt.id}
 className="flex items-center justify-between p-3 border rounded-lg hover:bg-muted/50 transition-colors"
 >
 <div className="flex items-center gap-3">
 {attempt.success ? (
 <CheckCircle2 className="h-5 w-5 text-green-600" />
 ) : (
 <XCircle className="h-5 w-5 text-red-600" />
 )}
 <div>
 <div className="flex items-center gap-2">
 <span className="font-medium">
 {attempt.username || attempt.apiKeyPrefix || 'Unknown'}
 </span>
 <Badge variant={attempt.success ? 'default' : 'destructive'}>
 {attempt.success ? 'Success' : 'Failed'}
 </Badge>
 </div>
 {attempt.failureReason && (
 <p className="text-sm text-muted-foreground">
 {attempt.failureReason}
 </p>
 )}
 </div>
 </div>
 <div className="text-right text-sm">
 <p className="text-muted-foreground">
 {format(new Date(attempt.createdAt), 'MMM d, HH:mm:ss')}
 </p>
 {attempt.ipAddress && (
 <p className="text-xs text-muted-foreground">
 From {attempt.ipAddress}
 </p>
 )}
 </div>
 </div>
 ))}
 </div>
 )}
 </ScrollArea>
 </DialogContent>
 </Dialog>
 );
}