import { IntegrationFlow } from '@/services/integrationFlowService';
import { TimeFilter } from '../types/timeFilter';

export const filterIntegrationFlowsByTime = (integrationFlows: IntegrationFlow[], filter: TimeFilter): IntegrationFlow[] => {
  // Handle undefined/null integration flows
  if (!integrationFlows || !Array.isArray(integrationFlows)) {
    return [];
  }

  const now = new Date();
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());

  switch (filter) {
    case 'today':
      return integrationFlows.filter(integrationFlow => {
        const flowDate = new Date(integrationFlow.timestamp);
        return flowDate >= today;
      });

    case 'yesterday': {
      const yesterday = new Date(today.getTime() - 24 * 60 * 60 * 1000);
      return integrationFlows.filter(integrationFlow => {
        const flowDate = new Date(integrationFlow.timestamp);
        return flowDate >= yesterday && flowDate < today;
      })
    }

    case 'last-7-days': {
      const last7d = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000);
      return integrationFlows.filter(integrationFlow => {
        const flowDate = new Date(integrationFlow.timestamp);
        return flowDate >= last7d;
      })
    }

    case 'last-30-days': {
      const last30d = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000);
      return integrationFlows.filter(integrationFlow => {
        const flowDate = new Date(integrationFlow.timestamp);
        return flowDate >= last30d;
      })
    }

    case 'all':
    default:
      return integrationFlows;
  }
};

export const getFilterDescription = (timeFilter: TimeFilter, integrationFlowCount: number): string => {
  if (timeFilter === 'today') {
    return `${integrationFlowCount} integration flows today`;
  } else if (timeFilter === 'yesterday') {
    return `${integrationFlowCount} integration flows yesterday`;
  } else if (timeFilter === 'last-7-days') {
    return `${integrationFlowCount} integration flows in last 7 days`;
  } else if (timeFilter === 'last-30-days') {
    return `${integrationFlowCount} integration flows in last 30 days`;
  } else {
    return `${integrationFlowCount} integration flows total`;
  }
};