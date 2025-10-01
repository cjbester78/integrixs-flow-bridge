import React from 'react';
import { AdapterCard } from './AdapterCard';
import { AdapterType } from '@/services/adapterTypeService';
import { Pagination } from '@/components/ui/pagination';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { Info } from 'lucide-react';

interface AdapterGridProps {
  adapters: AdapterType[];
  totalPages: number;
  currentPage: number;
  onPageChange: (page: number) => void;
}

export const AdapterGrid: React.FC<AdapterGridProps> = ({
  adapters,
  totalPages,
  currentPage,
  onPageChange
}) => {
  if (adapters.length === 0) {
    return (
      <Alert>
        <Info className="h-4 w-4" />
        <AlertDescription>
          No adapters found matching your criteria. Try adjusting your filters or search query.
        </AlertDescription>
      </Alert>
    );
  }

  return (
    <div>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {adapters.map((adapter) => (
          <AdapterCard key={adapter.id} adapter={adapter} />
        ))}
      </div>

      {totalPages > 1 && (
        <div className="mt-6 flex justify-center">
          <Pagination
            currentPage={currentPage}
            totalPages={totalPages}
            onPageChange={onPageChange}
          />
        </div>
      )}
    </div>
  );
};