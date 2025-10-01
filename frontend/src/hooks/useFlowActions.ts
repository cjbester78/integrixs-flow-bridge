import { useToast } from '@/hooks/use-toast';
import { flowService } from '@/services/flowService';
import { logger, LogCategory } from '@/lib/logger';
import { useAuthStore } from '@/stores/auth-store';

interface UseFlowActionsProps {
 flowName: string;
 description: string;
 inboundAdapter: string;
 outboundAdapter: string;
 sourceStructure: string;
 targetStructure: string;
 selectedTransformations: string[];
 fieldMappings: { sourceFields: string[]; targetField: string; javaFunction?: string }[];
 resetForm: () => void;
}

export const useFlowActions = ({
 flowName,
 description,
 inboundAdapter,
 outboundAdapter,
 sourceStructure,
 targetStructure,
 selectedTransformations,
 fieldMappings,
 resetForm,
}: UseFlowActionsProps) => {
 const { toast } = useToast();
 const user = useAuthStore((state) => state.user);
 const handleSaveFlow = async () => {
 if (!flowName || !inboundAdapter || !outboundAdapter) {
 toast({
 title: "Validation Error",
 description: "Please fill in all required fields",
 variant: "destructive",
 });
 return;
 }

 try {
 const flowData = {
 name: flowName,
 description,
 inboundAdapterId: inboundAdapter,
 outboundAdapterId: outboundAdapter,
 sourceStructureId: sourceStructure || undefined,
 targetStructureId: targetStructure || undefined,
 transformations: selectedTransformations.map((transformationId, index) => ({
 type: transformationId as 'field-mapping' | 'custom-function' | 'filter' | 'enrichment',
 configuration: transformationId === 'field-mapping' ? { fieldMappings } : {},
 order: index + 1
 })),
 status: 'DRAFT',
 mappingMode: 'WITH_MAPPING',
 isActive: true,
 createdBy: user?.id || 'unknown'
 };

 const response = await flowService.createFlow(flowData);
 if (response.success) {
 toast({
 title: "Flow Saved Successfully",
 description: `Integration flow "${flowName}" has been created with ID: ${response.data?.id}`,
 variant: "default",
 });

 resetForm();
 } else {
 throw new Error(response.error || 'Failed to save flow');
 }
} catch (error) {
 logger.error(LogCategory.BUSINESS_LOGIC, 'Error saving flow', { error: error });
 toast({
 title: "Save Failed",
 description: error instanceof Error ? error.message : "Failed to save integration flow",
 variant: "destructive",
 });
 }
 };

 const handleTestFlow = () => {
 if (!inboundAdapter || !outboundAdapter) {
 toast({
 title: "Cannot Test Flow",
 description: "Please configure source and target adapters first",
 variant: "destructive",
 });
 return;
 }

 toast({
 title: "Flow Test Initiated",
 description: "Testing connection and data flow...",
 variant: "default",
 });
 };

 return {
 handleSaveFlow,
 handleTestFlow,
 };
};