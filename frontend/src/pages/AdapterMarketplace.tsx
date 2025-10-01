import React, { useState } from 'react';
import { useAdapterTypes } from '@/hooks/useAdapterTypes';
import { useAdapterCategories } from '@/hooks/useAdapterCategories';
import { useAdapterCounts } from '@/hooks/useAdapterCounts';
import { CategoryFilter } from '@/components/adapter/CategoryFilter';
import { AdapterGrid } from '@/components/adapter/AdapterGrid';
import { AdapterSearchBar } from '@/components/adapter/AdapterSearchBar';
import { PageHeader } from '@/components/common/PageHeader';
import { Card, CardContent } from '@/components/ui/card';
import { Skeleton } from '@/components/ui/skeleton';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { AlertCircle } from 'lucide-react';

export const AdapterMarketplace = () => {
  const [selectedCategory, setSelectedCategory] = useState<string>('all');
  const [searchQuery, setSearchQuery] = useState('');
  const [currentPage, setCurrentPage] = useState(0);

  const { data: categories, isLoading: categoriesLoading } = useAdapterCategories();
  const { data: adapterCounts, isLoading: countsLoading } = useAdapterCounts();
  const { 
    data: adapterTypesData, 
    isLoading: adapterTypesLoading, 
    error 
  } = useAdapterTypes({
    category: selectedCategory === 'all' ? undefined : selectedCategory,
    search: searchQuery,
    page: currentPage,
    size: 12,
    status: 'active'
  });

  const handleCategoryChange = (category: string) => {
    setSelectedCategory(category);
    setCurrentPage(0);
  };

  const handleSearchChange = (query: string) => {
    setSearchQuery(query);
    setCurrentPage(0);
  };

  return (
    <div className="container mx-auto p-6">
      <PageHeader
        title="Adapter Marketplace"
        description="Browse and configure integration adapters for your system"
      />

      <div className="mt-6 space-y-6">
        {/* Search Bar */}
        <AdapterSearchBar 
          value={searchQuery} 
          onChange={handleSearchChange}
          placeholder="Search adapters by name, vendor, or description..."
        />

        <div className="flex gap-6">
          {/* Category Filter */}
          <div className="w-64 flex-shrink-0">
            {categoriesLoading || countsLoading ? (
              <Card>
                <CardContent className="p-4 space-y-2">
                  {[1, 2, 3, 4, 5].map((i) => (
                    <Skeleton key={i} className="h-8 w-full" />
                  ))}
                </CardContent>
              </Card>
            ) : (
              <CategoryFilter
                categories={categories || []}
                selectedCategory={selectedCategory}
                onCategoryChange={handleCategoryChange}
                adapterCounts={adapterCounts || {}}
              />
            )}
          </div>

          {/* Adapter Grid */}
          <div className="flex-1">
            {error ? (
              <Alert variant="destructive">
                <AlertCircle className="h-4 w-4" />
                <AlertDescription>
                  Failed to load adapters. Please try again later.
                </AlertDescription>
              </Alert>
            ) : adapterTypesLoading ? (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                {[1, 2, 3, 4, 5, 6].map((i) => (
                  <Card key={i}>
                    <CardContent className="p-6">
                      <Skeleton className="h-12 w-12 rounded mb-4" />
                      <Skeleton className="h-6 w-3/4 mb-2" />
                      <Skeleton className="h-4 w-full mb-1" />
                      <Skeleton className="h-4 w-2/3" />
                    </CardContent>
                  </Card>
                ))}
              </div>
            ) : (
              <AdapterGrid
                adapters={adapterTypesData?.content || []}
                totalPages={adapterTypesData?.totalPages || 0}
                currentPage={currentPage}
                onPageChange={setCurrentPage}
              />
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default AdapterMarketplace;