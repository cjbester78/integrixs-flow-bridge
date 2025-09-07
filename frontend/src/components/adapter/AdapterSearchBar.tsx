import React, { useState, useCallback } from 'react';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Search, X } from 'lucide-react';
import { useDebouncedCallback } from 'use-debounce';

interface AdapterSearchBarProps {
  value: string;
  onChange: (value: string) => void;
  placeholder?: string;
}

export const AdapterSearchBar: React.FC<AdapterSearchBarProps> = ({
  value,
  onChange,
  placeholder = "Search adapters..."
}) => {
  const [localValue, setLocalValue] = useState(value);

  const debouncedOnChange = useDebouncedCallback((value: string) => {
    onChange(value);
  }, 300);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const newValue = e.target.value;
    setLocalValue(newValue);
    debouncedOnChange(newValue);
  };

  const handleClear = () => {
    setLocalValue('');
    onChange('');
  };

  return (
    <div className="relative">
      <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
      <Input
        type="text"
        value={localValue}
        onChange={handleChange}
        placeholder={placeholder}
        className="pl-10 pr-10"
      />
      {localValue && (
        <Button
          size="sm"
          variant="ghost"
          onClick={handleClear}
          className="absolute right-1 top-1/2 h-7 w-7 -translate-y-1/2 p-0"
        >
          <X className="h-4 w-4" />
        </Button>
      )}
    </div>
  );
};