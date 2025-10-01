import React from 'react';
import { TrendingUp, TrendingDown } from 'lucide-react';

interface MetricCardProps {
  title: string;
  value: string;
  icon: React.ReactNode;
  trend?: number;
  loading?: boolean;
  valueColor?: string;
}

export const MetricCard: React.FC<MetricCardProps> = ({
  title,
  value,
  icon,
  trend,
  loading,
  valueColor = 'text-gray-900',
}) => {
  return (
    <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
      <div className="flex items-center justify-between mb-2">
        <span className="text-sm font-medium text-gray-600">{title}</span>
        <div className="text-gray-400">{icon}</div>
      </div>
      
      {loading ? (
        <div className="animate-pulse">
          <div className="h-8 bg-gray-200 rounded w-24 mb-2"></div>
          <div className="h-4 bg-gray-200 rounded w-16"></div>
        </div>
      ) : (
        <>
          <div className={`text-2xl font-bold ${valueColor}`}>{value}</div>
          
          {trend !== undefined && trend !== 0 && (
            <div className={`flex items-center mt-2 text-sm ${
              trend > 0 ? 'text-green-600' : 'text-red-600'
            }`}>
              {trend > 0 ? (
                <TrendingUp className="w-4 h-4 mr-1" />
              ) : (
                <TrendingDown className="w-4 h-4 mr-1" />
              )}
              <span>{Math.abs(trend).toFixed(1)}%</span>
            </div>
          )}
        </>
      )}
    </div>
  );
};