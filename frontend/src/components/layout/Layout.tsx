// Main layout component with full-screen flex layout
import { Outlet } from 'react-router-dom';
import { Header } from './Header';
import { Sidebar } from './Sidebar';
import { Breadcrumb } from './Breadcrumb';
import { SessionTimeoutNotification } from '@/components/SessionTimeoutNotification';
import { SessionTimeoutWarning } from '@/components/auth/SessionTimeoutWarning';
import { SidebarProvider } from '@/components/ui/sidebar';

export const Layout = () => {
 return (
 <SidebarProvider>
 <div className="min-h-screen w-full bg-background flex flex-col">
 <Header />
 <div className="flex flex-1 w-full">
 <Sidebar />
 <div className="flex-1 flex flex-col min-w-0 w-full max-w-none">
 <Breadcrumb />
 <main className="flex-1 overflow-auto p-6 w-full">
 <Outlet />
 </main>
 </div>
 </div>
 <SessionTimeoutNotification />
 <SessionTimeoutWarning />
 </div>
 </SidebarProvider>
 );
};