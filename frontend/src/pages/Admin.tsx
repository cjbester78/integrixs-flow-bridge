import { useState, useEffect, useCallback } from 'react';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Users, Shield, Key, FileArchive, ScrollText, Network, Settings, UserCheck } from 'lucide-react';
import { AdminStats } from '@/components/admin/AdminStats';
import { UserManagement } from '@/components/admin/UserManagement';
import { RoleManagement } from '@/components/admin/RoleManagement';
import { CertificateManagement } from '@/components/admin/CertificateManagement';
import { JarFileManagement } from '@/components/admin/JarFileManagement';
import { SystemLogs } from '@/components/admin/SystemLogs';
import { AdapterTypesManagement } from '@/components/admin/AdapterTypesManagement';
import { SystemSettings } from '@/components/admin/SystemSettings';
import { ExternalAuthManagement } from '@/components/admin/ExternalAuthManagement';
import { User, Role, Certificate, JarFile } from '@/types/admin';
import { ExternalAuthConfig } from '@/types/externalAuth';
import { userService } from '@/services/userService';
import { roleService } from '@/services/roleService';
import { certificateService } from '@/services/certificateService';
import { jarFileService } from '@/services/jarFileService';
import { externalAuthService } from '@/services/externalAuthService';
import { useToast } from '@/hooks/use-toast';
import { logger, LogCategory } from '@/lib/logger';


export const Admin = () => {
 const { toast } = useToast();
 const [users, setUsers] = useState<User[]>([]);
 const [isLoadingUsers, setIsLoadingUsers] = useState(true);
 const [roles, setRoles] = useState<Role[]>([]);
 const [isLoadingRoles, setIsLoadingRoles] = useState(true);
 const [certificates, setCertificates] = useState<Certificate[]>([]);
 const [isLoadingCertificates, setIsLoadingCertificates] = useState(true);
 const [jarFiles, setJarFiles] = useState<JarFile[]>([]);
 const [isLoadingJarFiles, setIsLoadingJarFiles] = useState(true);
 const [authConfigs, setAuthConfigs] = useState<ExternalAuthConfig[]>([]);
 const [isLoadingAuthConfigs, setIsLoadingAuthConfigs] = useState(true);
 const [activeTab, setActiveTab] = useState('users');

 // Fetch users from backend
 const fetchUsers = useCallback(async () => {
    try {
 setIsLoadingUsers(true);
 const response = await userService.getAllUsers();
 logger.info(LogCategory.SYSTEM, 'Debug info', { data: { data: 'User fetch response:', extra: response } }); // Debug log

 if (response.success && response.data) {
 // Handle different possible response structures
 const userData = Array.isArray(response.data) ? response.data : response.data.users;
 setUsers(userData || []);
 } else {
 logger.error(LogCategory.ERROR, 'Failed to fetch users', { error: response.error });
 // Show empty list when API fails
 logger.info(LogCategory.SYSTEM, 'API not available, showing empty user list');
 setUsers([]);
 toast({ title: "Error", description: 'Failed to load users', variant: "destructive" })}
} catch (error) {
 logger.error(LogCategory.ERROR, 'Error fetching users', { error: error });
 // Show empty list when API fails
 logger.info(LogCategory.SYSTEM, 'API error, showing empty user list');
 setUsers([]);
 toast({ title: "Error", description: 'Failed to load users', variant: "destructive" });
 } finally {
 setIsLoadingUsers(false);
 }
 }, [toast]);

 // Fetch roles from backend
 const fetchRoles = useCallback(async () => {
    try {
 setIsLoadingRoles(true);
 const response = await roleService.getAllRoles();
 logger.info(LogCategory.SYSTEM, 'Role fetch response', { data: response });
 if (response.success && response.data) {
 // Backend returns array directly, not wrapped in {roles: [...]}
 const roleData = Array.isArray(response.data) ? response.data : [];
 setRoles(roleData);
 } else {
 logger.error(LogCategory.ERROR, 'Failed to fetch roles', { error: response.error });
 setRoles([]);
 toast({ title: "Error", description: 'Failed to load roles', variant: "destructive" })}
} catch (error) {
 logger.error(LogCategory.ERROR, 'Error fetching roles', { error: error });
 setRoles([]);
 toast({ title: "Error", description: 'Failed to load roles', variant: "destructive" });
 } finally {
 setIsLoadingRoles(false);
 }
 }, [toast]);

 // Fetch certificates from backend
 const fetchCertificates = useCallback(async () => {
    try {
 setIsLoadingCertificates(true);
 const response = await certificateService.getAllCertificates();
 logger.info(LogCategory.SYSTEM, 'Certificate fetch response', { data: response });
 if (response.success && response.data) {
 setCertificates(response.data);
 } else {
 logger.error(LogCategory.ERROR, 'Failed to fetch certificates', { error: response.error });
 setCertificates([]);
 toast({ title: "Error", description: 'Failed to load certificates', variant: "destructive" })}
} catch (error) {
 logger.error(LogCategory.ERROR, 'Error fetching certificates', { error: error });
 setCertificates([]);
 toast({ title: "Error", description: 'Failed to load certificates', variant: "destructive" });
 } finally {
 setIsLoadingCertificates(false);
 }
 }, [toast]);

 // Fetch JAR files from backend
 const fetchJarFiles = useCallback(async () => {
    try {
 setIsLoadingJarFiles(true);
 const response = await jarFileService.getAllJarFiles();
 logger.info(LogCategory.SYSTEM, 'JAR files fetch response', { data: response });
 if (response.success && response.data) {
 setJarFiles(response.data);
 } else {
 logger.error(LogCategory.ERROR, 'Failed to fetch JAR files', { error: response.error });
 setJarFiles([]);
 toast({ title: "Error", description: 'Failed to load JAR files', variant: "destructive" })}
} catch (error) {
 logger.error(LogCategory.ERROR, 'Error fetching JAR files', { error: error });
 setJarFiles([]);
 toast({ title: "Error", description: 'Failed to load JAR files', variant: "destructive" });
 } finally {
 setIsLoadingJarFiles(false);
 }
 }, [toast]);

 // Fetch external auth configs from backend
 const fetchAuthConfigs = useCallback(async () => {
    try {
 setIsLoadingAuthConfigs(true);
 const response = await externalAuthService.getAllAuthConfigs();
 logger.info(LogCategory.AUTH, 'Auth configs fetch response', { data: response });
 if ('success' in response && response.success && response.data) {
 setAuthConfigs(response.data);
 } else if (Array.isArray(response)) {
 setAuthConfigs(response);
 } else {
 logger.error(LogCategory.AUTH, 'Failed to fetch auth configs:', 'message' in response ? response.message : 'Unknown error');
 setAuthConfigs([]);
 toast({ title: "Error", description: 'Failed to load authentication configurations', variant: "destructive" });
 }
 } catch (error) {
 logger.error(LogCategory.AUTH, 'Error fetching auth configs', { error: error });
 setAuthConfigs([]);
 toast({ title: "Error", description: 'Failed to load authentication configurations', variant: "destructive" });
 } finally {
 setIsLoadingAuthConfigs(false);
 }
 }, [toast]);

 useEffect(() => {
 fetchUsers();
 fetchRoles();
 fetchCertificates();
 fetchJarFiles();
 fetchAuthConfigs();
 }, [fetchUsers, fetchRoles, fetchCertificates, fetchJarFiles, fetchAuthConfigs]);

 const handleJarFileAdded = (jarFile: JarFile) => {
 setJarFiles(prev => [...prev, jarFile]);
 };

 const handleJarFileDeleted = (id: string) => {
 setJarFiles(prev => prev.filter(jar => jar.id !== id));
 };

 return (
 <div className="p-6 space-y-6">
 <div>
 <h1 className="text-3xl font-bold text-foreground flex items-center gap-3">
 <Users className="h-8 w-8" />
 Admin Panel
 </h1>
 <p className="text-muted-foreground">Manage users, roles, and system security</p>
 </div>

 <AdminStats
 users={users}
 roles={roles}
 certificates={certificates}
 jarFiles={jarFiles}
 />

 <Tabs value={activeTab} onValueChange={setActiveTab} className="space-y-6 animate-fade-in" style={{ animationDelay: '0.1s' }}>
 <TabsList className="grid w-full grid-cols-8">
 <TabsTrigger value="users" className="flex items-center gap-2">
 <Users className="h-4 w-4" />
 Users
 </TabsTrigger>
 <TabsTrigger value="roles" className="flex items-center gap-2">
 <Shield className="h-4 w-4" />
 Roles
 </TabsTrigger>
 <TabsTrigger value="certificates" className="flex items-center gap-2">
 <Key className="h-4 w-4" />
 Certificates
 </TabsTrigger>
 <TabsTrigger value="external-auth" className="flex items-center gap-2">
 <UserCheck className="h-4 w-4" />
 External Auth
 </TabsTrigger>
 <TabsTrigger value="jar-files" className="flex items-center gap-2">
 <FileArchive className="h-4 w-4" />
 Drivers
 </TabsTrigger>
 <TabsTrigger value="adapter-types" className="flex items-center gap-2">
 <Network className="h-4 w-4" />
 Adapters
 </TabsTrigger>
 <TabsTrigger value="system-settings" className="flex items-center gap-2">
 <Settings className="h-4 w-4" />
 Settings
 </TabsTrigger>
 <TabsTrigger value="system-logs" className="flex items-center gap-2">
 <ScrollText className="h-4 w-4" />
 Logs
 </TabsTrigger>
 </TabsList>

 <TabsContent value="users" className="space-y-4">
 <UserManagement users={users} isLoading={isLoadingUsers} onRefresh={fetchUsers} />
 </TabsContent>

 <TabsContent value="roles" className="space-y-4">
 <RoleManagement roles={roles} isLoading={isLoadingRoles} onRefresh={fetchRoles} />
 </TabsContent>

 <TabsContent value="certificates" className="space-y-4">
 <CertificateManagement certificates={certificates} isLoading={isLoadingCertificates} onRefresh={fetchCertificates} />
 </TabsContent>

 <TabsContent value="external-auth" className="space-y-4">
 <ExternalAuthManagement authConfigs={authConfigs} isLoading={isLoadingAuthConfigs} onRefresh={fetchAuthConfigs} />
 </TabsContent>

 <TabsContent value="jar-files" className="space-y-4">
 <JarFileManagement
 jarFiles={jarFiles}
 isLoading={isLoadingJarFiles}
 onRefresh={fetchJarFiles}
 onJarFileAdded={handleJarFileAdded}
 onJarFileDeleted={handleJarFileDeleted}
 />
 </TabsContent>

 <TabsContent value="adapter-types" className="space-y-4">
 <AdapterTypesManagement />
 </TabsContent>

 <TabsContent value="system-settings" className="space-y-4">
 <SystemSettings />
 </TabsContent>

 <TabsContent value="system-logs" className="space-y-4">
 <SystemLogs />
 </TabsContent>
 </Tabs>
 </div>
 );
};