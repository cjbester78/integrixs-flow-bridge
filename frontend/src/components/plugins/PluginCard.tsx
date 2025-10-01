import React from 'react';
import { Package, Download, Star, Code2, Shield, ChevronRight } from 'lucide-react';
import type { Plugin } from '../../types/plugin';

interface PluginCardProps {
  plugin: Plugin;
  onClick: () => void;
}

export const PluginCard: React.FC<PluginCardProps> = ({ plugin, onClick }) => {
  const getIconComponent = (iconName?: string) => {
    // Map icon names to components
    const icons: Record<string, React.ComponentType<any>> = {
      package: Package,
      code: Code2,
      shield: Shield,
      // Add more icon mappings as needed
    };
    
    const IconComponent = iconName ? icons[iconName] : Package;
    return IconComponent || Package;
  };

  const Icon = getIconComponent(plugin.icon);

  return (
    <div
      onClick={onClick}
      className="bg-white rounded-lg shadow-sm border border-gray-200 p-6 hover:shadow-md transition-shadow cursor-pointer group"
    >
      <div className="flex justify-between items-start mb-4">
        <div className="flex items-center gap-3">
          <div className="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
            <Icon className="w-6 h-6 text-blue-600" />
          </div>
          <div>
            <h3 className="font-semibold text-gray-900 group-hover:text-blue-600 transition-colors">
              {plugin.name}
            </h3>
            <p className="text-sm text-gray-500">by {plugin.vendor}</p>
          </div>
        </div>
        <ChevronRight className="w-5 h-5 text-gray-400 group-hover:text-gray-600 transition-colors" />
      </div>

      <p className="text-gray-600 text-sm mb-4 line-clamp-2">
        {plugin.description || 'No description available'}
      </p>

      <div className="space-y-2">
        <div className="flex items-center gap-4 text-xs text-gray-500">
          <span className="flex items-center gap-1">
            <Code2 className="w-3 h-3" />
            v{plugin.version}
          </span>
          {plugin.license && (
            <span className="flex items-center gap-1">
              <Shield className="w-3 h-3" />
              {plugin.license}
            </span>
          )}
        </div>

        {plugin.tags && plugin.tags.length > 0 && (
          <div className="flex gap-1 flex-wrap">
            {plugin.tags.slice(0, 3).map(tag => (
              <span
                key={tag}
                className="px-2 py-1 bg-gray-100 text-gray-600 text-xs rounded-full"
              >
                {tag}
              </span>
            ))}
            {plugin.tags.length > 3 && (
              <span className="px-2 py-1 text-gray-500 text-xs">
                +{plugin.tags.length - 3} more
              </span>
            )}
          </div>
        )}

        <div className="pt-2 border-t border-gray-100">
          <span className="inline-flex items-center px-2 py-1 bg-blue-50 text-blue-700 text-xs rounded-md capitalize">
            {plugin.category}
          </span>
        </div>
      </div>
    </div>
  );
};