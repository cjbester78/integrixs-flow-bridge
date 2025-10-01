import { lazy, Suspense } from "react";
import { Toaster } from "@/components/ui/toaster";
import { TooltipProvider } from "@/components/ui/tooltip";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider } from "@/contexts/AuthContext";
import { TenantProvider } from "@/contexts/TenantContext";
import { ProtectedRoute } from "@/components/auth/ProtectedRoute";
import { BackendStartupOverlay } from "@/components/BackendStartupOverlay";
import { Layout } from "@/components/layout/Layout";
import { Login } from "@/pages/Login";
import { QueryProvider } from "@/providers/query-provider";
import { ErrorBoundary } from "@/components/error-boundary";
import { NotificationDisplay } from "@/components/notification-display";
import { useUIStore } from "@/stores/ui-store";
import { useEffect, useState } from "react";
import { setupAxiosInterceptors } from "@/lib/axios-interceptors";
import { logger, LogCategory } from '@/lib/logger';

// Lazy load heavy pages
const Dashboard = lazy(() => import("@/pages/Dashboard").then(m => ({ default: m.Dashboard })));
const TestPage = lazy(() => import("@/pages/TestPage").then(m => ({ default: m.TestPage })));
const DeprecatedCreateFlow = lazy(() => import("@/pages/DeprecatedCreateFlow").then(m => ({ default: m.DeprecatedCreateFlow })));
const CreateCommunicationAdapter = lazy(() => import("@/pages/CreateCommunicationAdapter").then(m => ({ default: m.CreateCommunicationAdapter })));
const CommunicationAdapters = lazy(() => import("@/pages/CommunicationAdapters"));
const DataStructures = lazy(() => import("@/pages/DataStructures").then(m => ({ default: m.DataStructures })));
const CreateDataStructure = lazy(() => import("@/pages/CreateDataStructure").then(m => ({ default: m.CreateDataStructure })));
const BusinessComponents = lazy(() => import("@/pages/BusinessComponents").then(m => ({ default: m.BusinessComponents })));
const Messages = lazy(() => import("@/pages/Messages").then(m => ({ default: m.Messages })));
const AdapterMonitoring = lazy(() => import("@/pages/AdapterMonitoring"));
const InterfaceDetails = lazy(() => import("@/pages/InterfaceDetails"));
const Admin = lazy(() => import("@/pages/Admin").then(m => ({ default: m.Admin })));
const Settings = lazy(() => import("@/pages/Settings").then(m => ({ default: m.Settings })));
const RetryManagement = lazy(() => import("@/pages/RetryManagement").then(m => ({ default: m.RetryManagement })));
const DevelopmentFunctions = lazy(() => import("@/pages/DevelopmentFunctions").then(m => ({ default: m.DevelopmentFunctions })));
const AllInterfaces = lazy(() => import("@/pages/AllInterfaces"));
const Packages = lazy(() => import("@/pages/Packages").then(m => ({ default: m.Packages })));
const AdapterMarketplace = lazy(() => import("@/pages/AdapterMarketplace"));
const PluginMarketplace = lazy(() => import("@/pages/PluginMarketplace").then(m => ({ default: m.PluginMarketplace })));
const SystemConfiguration = lazy(() => import("@/pages/SystemConfiguration"));
const NotFound = lazy(() => import("@/pages/NotFound"));

// Loading component
const PageLoader = () => (
  <div className="flex items-center justify-center min-h-screen">
 <div className="text-center">
 <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto mb-4"></div>
 <p className="text-muted-foreground">Loading...</p>
 </div>
 </div>
);

const ThemeEffect = () => {
  const theme = useUIStore((state) => state.theme);
 useEffect(() => {
    if (theme === 'dark' || (theme === 'system' && window.matchMedia('(prefers-color-scheme: dark)').matches)) {
      document.documentElement.classList.add('dark');
    } else {
      document.documentElement.classList.remove('dark');
    }
 }, [theme]);

 return null;
}

const NavigationLoggerComponent = () => {
 return null;
}

const App = () => {
 const [backendReady, setBackendReady] = useState(false);

  useEffect(() => {
    const timer = setTimeout(() => {
      setupAxiosInterceptors();
    }, 100);

 logger.info(LogCategory.SYSTEM, 'Application initialized', { version: import.meta.env.VITE_APP_VERSION || '1.0.0', environment: import.meta.env.MODE });

    return () => clearTimeout(timer);
  }, []);

  if (!backendReady) {
    return (
      <BackendStartupOverlay
 onBackendReady={() => setBackendReady(true)}
      />
    );
  }

  return (
    <ErrorBoundary>
 <QueryProvider>
 <TooltipProvider>
 <ThemeEffect />
 <Toaster />
 <NotificationDisplay />
 <BrowserRouter>
 <NavigationLoggerComponent />
 <AuthProvider>
 <TenantProvider>
 <Routes>;
 <Route path="/login" element={<Login />} />

 <Route element={<ProtectedRoute />}>
 <Route path="/" element={<Layout />}>
 <Route index element={<Navigate to="/dashboard" replace />} />
 <Route path="dashboard" element={
 <Suspense fallback={<PageLoader />}>
 <Dashboard />
 </Suspense>
 } />
 <Route path="test" element={
 <Suspense fallback={<PageLoader />}>
 <TestPage />
 </Suspense>
 } />
 <Route path="create-flow" element={
 <Suspense fallback={<PageLoader />}>
 <DeprecatedCreateFlow />
 </Suspense>
 } />
 <Route path="create-direct-mapping-flow" element={
 <Suspense fallback={<PageLoader />}>
 <DeprecatedCreateFlow />
 </Suspense>
 } />
 <Route path="create-orchestration-flow" element={
 <Suspense fallback={<PageLoader />}>
 <DeprecatedCreateFlow />
 </Suspense>
 } />
 <Route path="communication-adapters" element={
 <Suspense fallback={<PageLoader />}>
 <CommunicationAdapters />
 </Suspense>
 } />
 <Route path="communication-adapters/:id" element={
 <Suspense fallback={<PageLoader />}>
 <CreateCommunicationAdapter />
 </Suspense>
 } />
 <Route path="create-communication-adapter" element={
 <Suspense fallback={<PageLoader />}>
 <CreateCommunicationAdapter />
 </Suspense>
 } />
 <Route path="data-structures" element={
 <Suspense fallback={<PageLoader />}>
 <DataStructures />
 </Suspense>
 } />
 <Route path="data-structures/:id" element={
 <Suspense fallback={<PageLoader />}>
 <CreateDataStructure />
 </Suspense>
 } />
 <Route path="create-data-structure" element={
 <Suspense fallback={<PageLoader />}>
 <CreateDataStructure />
 </Suspense>
 } />
 <Route path="business-components" element={
 <Suspense fallback={<PageLoader />}>
 <BusinessComponents />
 </Suspense>
 } />
 <Route path="all-interfaces" element={
 <Suspense fallback={<PageLoader />}>
 <AllInterfaces />
 </Suspense>
 } />
 <Route path="interfaces" element={<Navigate to="/all-interfaces" replace />} />
 <Route path="interfaces/:flowId/details" element={
 <Suspense fallback={<PageLoader />}>
 <InterfaceDetails />
 </Suspense>
 } />
 <Route path="messages" element={
 <Suspense fallback={<PageLoader />}>
 <Messages />
 </Suspense>
 } />
 <Route path="adapter-monitoring" element={
 <Suspense fallback={<PageLoader />}>
 <AdapterMonitoring />
 </Suspense>
 } />
 <Route path="admin" element={
 <Suspense fallback={<PageLoader />}>
 <Admin />
 </Suspense>
 } />
 <Route path="system-configuration" element={
 <Suspense fallback={<PageLoader />}>
 <SystemConfiguration />
 </Suspense>
 } />
 <Route path="settings" element={
 <Suspense fallback={<PageLoader />}>
 <Settings />
 </Suspense>
 } />
 <Route path="retry-management" element={
 <Suspense fallback={<PageLoader />}>
 <RetryManagement />
 </Suspense>
 } />
 <Route path="development-functions" element={
 <Suspense fallback={<PageLoader />}>
 <DevelopmentFunctions />
 </Suspense>
 } />
 <Route path="packages" element={
 <Suspense fallback={<PageLoader />}>
 <Packages />
 </Suspense>
 } />
 <Route path="adapter-marketplace" element={
 <Suspense fallback={<PageLoader />}>
 <AdapterMarketplace />
 </Suspense>
 } />
 <Route path="plugin-marketplace" element={
 <Suspense fallback={<PageLoader />}>
 <PluginMarketplace />
 </Suspense>
 } />
 <Route path="*" element={
 <Suspense fallback={<PageLoader />}>
 <NotFound />
 </Suspense>
 } />
 </Route>
 </Route>
 </Routes>
 </TenantProvider>
 </AuthProvider>
 </BrowserRouter>
 </TooltipProvider>
 </QueryProvider>
    </ErrorBoundary>
  );
};

export default App;