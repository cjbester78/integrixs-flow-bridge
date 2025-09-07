import React, { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Upload, Search, Package, CheckCircle, AlertCircle, Filter } from 'lucide-react';
import { pluginApi } from '../api/plugin';
import { PluginUploadModal } from '../components/plugins/PluginUploadModal';
import { PluginCard } from '../components/plugins/PluginCard';
import { PluginDetailsModal } from '../components/plugins/PluginDetailsModal';
import type { Plugin, PluginDetails } from '../types/plugin';

export const PluginMarketplace: React.FC = () => {
  const queryClient = useQueryClient();
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState<string | null>(null);
  const [showUploadModal, setShowUploadModal] = useState(false);
  const [selectedPlugin, setSelectedPlugin] = useState<Plugin | null>(null);
  const [showDetailsModal, setShowDetailsModal] = useState(false);

  // Fetch plugins
  const { data: plugins, isLoading } = useQuery({
    queryKey: ['plugins', searchQuery, selectedCategory],
    queryFn: () => pluginApi.searchPlugins({ query: searchQuery, category: selectedCategory }),
  });

  // Fetch plugin categories
  const { data: categories } = useQuery({
    queryKey: ['plugin-categories'],
    queryFn: () => pluginApi.getCategories(),
  });

  // Plugin details query
  const { data: pluginDetails } = useQuery({
    queryKey: ['plugin-details', selectedPlugin?.id],
    queryFn: () => pluginApi.getPluginDetails(selectedPlugin!.id),
    enabled: !!selectedPlugin,
  });

  // Upload mutation
  const uploadMutation = useMutation({
    mutationFn: (file: File) => pluginApi.uploadPlugin(file),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['plugins'] });
      setShowUploadModal(false);
    },
  });

  const handlePluginClick = (plugin: Plugin) => {
    setSelectedPlugin(plugin);
    setShowDetailsModal(true);
  };

  const filteredPlugins = plugins?.filter(plugin => {
    if (!searchQuery) return true;
    const query = searchQuery.toLowerCase();
    return (
      plugin.name.toLowerCase().includes(query) ||
      plugin.description?.toLowerCase().includes(query) ||
      plugin.vendor.toLowerCase().includes(query) ||
      plugin.tags?.some(tag => tag.toLowerCase().includes(query))
    );
  });

  return (
    <div className="container mx-auto px-6 py-8">
      {/* Header */}
      <div className="flex justify-between items-center mb-8">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Plugin Marketplace</h1>
          <p className="text-gray-600 mt-1">
            Extend your integration capabilities with custom plugins
          </p>
        </div>
        <button
          onClick={() => setShowUploadModal(true)}
          className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
        >
          <Upload className="w-4 h-4" />
          Upload Plugin
        </button>
      </div>

      {/* Search and Filters */}
      <div className="mb-6 space-y-4">
        <div className="flex gap-4">
          <div className="flex-1 relative">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
            <input
              type="text"
              placeholder="Search plugins..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
          </div>
          <button className="flex items-center gap-2 px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50">
            <Filter className="w-4 h-4" />
            Filters
          </button>
        </div>

        {/* Category Filter */}
        {categories && categories.length > 0 && (
          <div className="flex gap-2 flex-wrap">
            <button
              onClick={() => setSelectedCategory(null)}
              className={`px-3 py-1 rounded-full text-sm transition-colors ${
                !selectedCategory
                  ? 'bg-blue-100 text-blue-700'
                  : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
              }`}
            >
              All Categories
            </button>
            {categories.map(category => (
              <button
                key={category}
                onClick={() => setSelectedCategory(category)}
                className={`px-3 py-1 rounded-full text-sm transition-colors capitalize ${
                  selectedCategory === category
                    ? 'bg-blue-100 text-blue-700'
                    : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                }`}
              >
                {category}
              </button>
            ))}
          </div>
        )}
      </div>

      {/* Plugin Grid */}
      {isLoading ? (
        <div className="flex justify-center items-center h-64">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
        </div>
      ) : filteredPlugins && filteredPlugins.length > 0 ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
          {filteredPlugins.map(plugin => (
            <PluginCard
              key={plugin.id}
              plugin={plugin}
              onClick={() => handlePluginClick(plugin)}
            />
          ))}
        </div>
      ) : (
        <div className="text-center py-12">
          <Package className="w-16 h-16 text-gray-400 mx-auto mb-4" />
          <h3 className="text-lg font-medium text-gray-900 mb-2">No plugins found</h3>
          <p className="text-gray-600">
            {searchQuery
              ? 'Try adjusting your search criteria'
              : 'Upload your first plugin to get started'}
          </p>
        </div>
      )}

      {/* Upload Modal */}
      {showUploadModal && (
        <PluginUploadModal
          onClose={() => setShowUploadModal(false)}
          onUpload={uploadMutation.mutate}
          isUploading={uploadMutation.isPending}
          error={uploadMutation.error}
        />
      )}

      {/* Plugin Details Modal */}
      {showDetailsModal && selectedPlugin && pluginDetails && (
        <PluginDetailsModal
          plugin={selectedPlugin}
          details={pluginDetails}
          onClose={() => {
            setShowDetailsModal(false);
            setSelectedPlugin(null);
          }}
        />
      )}
    </div>
  );
};