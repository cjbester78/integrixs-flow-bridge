import React, { useState, useEffect } from 'react';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Card, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover';
import { ScrollArea } from '@/components/ui/scroll-area';
import { Separator } from '@/components/ui/separator';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Calculator, Search } from 'lucide-react';
import { logger, LogCategory } from '@/lib/logger';
import {
 functionsByCategory,
 TransformationFunction,
 generateJavaFunctionCall,
 TransformationFunctionService
} from '@/services/transformationFunctions';

interface FunctionPickerProps {
 onFunctionSelect: (functionName: string, javaCode: string) => void;
 trigger?: React.ReactNode;
}

export const FunctionPicker: React.FC<FunctionPickerProps> = ({
 onFunctionSelect,
 trigger
}) => {
 const [searchTerm, setSearchTerm] = useState('');
 const [selectedFunction, setSelectedFunction] = useState<TransformationFunction | null>(null);
 const [parameters, setParameters] = useState<Record<string, any>>({});
 const [availableFunctions, setAvailableFunctions] = useState<Record<string, TransformationFunction[]>>({});
 const [loading, setLoading] = useState(true);

 const [open, setOpen] = useState(false);
 
 // Load functions from backend on mount
 useEffect(() => {
 const loadFunctions = async () => {
 try {
 setLoading(true);
 const functions = await TransformationFunctionService.getFunctionsByCategoryAsync();
 setAvailableFunctions(functions);
 } catch (error) {
 logger.error(LogCategory.UI, 'Failed to load functions', error);
 // Fallback to local functions
 setAvailableFunctions(functionsByCategory);
 } finally {
 setLoading(false);
 }
 };
 loadFunctions();
 }, []);

 const filteredFunctions = Object.entries(availableFunctions).reduce((acc, [category, functions]) => {
 const filtered = functions.filter(fn =>
 fn.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
 fn.description.toLowerCase().includes(searchTerm.toLowerCase())
 );
 if (filtered.length > 0) {
 acc[category] = filtered;
 }
 return acc;
 }, {} as Record<string, TransformationFunction[]>);

 const handleFunctionClick = (func: TransformationFunction) => {
 logger.info(LogCategory.UI, '🔍 FunctionPicker: Function clicked', { data: func.name });
 // For visual mapper, immediately call onFunctionSelect when function is clicked;
 onFunctionSelect(func.name, '');
 setOpen(false);
 };

 const handleParameterChange = (paramName: string, value: any) => {
 setParameters(prev => ({
 ...prev,
 [paramName]: value
 }));
 };

 const handleApplyFunction = () => {
 logger.info(LogCategory.UI, '🔍 FunctionPicker: handleApplyFunction called');
 logger.info(LogCategory.UI, '🔍 FunctionPicker: selectedFunction', { data: selectedFunction });
 if (!selectedFunction) {
 logger.info(LogCategory.UI, '❌ FunctionPicker: No function selected');
 return;
 }

 const paramValues = selectedFunction.parameters.map(param =>
 parameters[param.name] || (param.type === 'number' ? 0 : '')
 );

 const javaCode = generateJavaFunctionCall(selectedFunction.name, paramValues);
 logger.info(LogCategory.UI, '🔍 FunctionPicker: Generated Java code', { data: javaCode });
 logger.info(LogCategory.UI, '🔍 FunctionPicker: Calling onFunctionSelect with', { data: selectedFunction.name, javaCode });
 onFunctionSelect(selectedFunction.name, javaCode);
 setOpen(false);
 setSelectedFunction(null);
 setParameters({});
 };

 const renderFunctionList = () => (
 <div className="space-y-4">
 <div className="relative">
 <Search className="absolute left-2 top-2.5 h-4 w-4 text-muted-foreground" />
 <Input
 placeholder="Search functions..."
 value={searchTerm}
 onChange={(e) => setSearchTerm(e.target.value)}
 className="pl-8"
 />
 </div>

 <Tabs defaultValue="math" className="w-full">
 <TabsList className="grid w-full" style={{ gridTemplateColumns: `repeat(${Object.keys(filteredFunctions).length}, 1fr)` }}>
 {Object.keys(filteredFunctions).map((category) => (
 <TabsTrigger key={category} value={category} className="capitalize text-xs">
 {category}
 </TabsTrigger>
 ))}
 </TabsList>

 {Object.entries(filteredFunctions).map(([category, functions]) => (
 <TabsContent key={category} value={category}>
 <ScrollArea className="h-64">
 <div className="space-y-2">
 {functions.map((func) => (
 <Card
 key={func.name}
 className="cursor-pointer hover:bg-accent/50 transition-colors"
 onClick={() => handleFunctionClick(func)}
 >
 <CardHeader className="pb-2">
 <div className="flex items-center justify-between">
 <CardTitle className="text-sm font-medium">{func.name}</CardTitle>
 <Badge variant="secondary" className="text-xs">
 {func.category}
 </Badge>
 </div>
 <CardDescription className="text-xs">
 {func.description}
 </CardDescription>
 </CardHeader>
 </Card>
 ))}
 </div>
 </ScrollArea>
 </TabsContent>
 ))}
 </Tabs>
 </div>
 );

 const renderFunctionConfig = () => {
 if (!selectedFunction) return null;

 return (
 <div className="space-y-4">
 <div className="flex items-center gap-2">
 <Button
 variant="ghost"
 size="sm"
 onClick={() => setSelectedFunction(null)}
 >
 ← Back
 </Button>
 <h3 className="font-semibold">{selectedFunction.name}</h3>
 </div>

 <p className="text-sm text-muted-foreground">
 {selectedFunction.description}
 </p>

 <Separator />

 <div className="space-y-3">
 <Label className="text-sm font-medium">Parameters:</Label>
 {selectedFunction.parameters.map((param) => (
 <div key={param.name} className="space-y-1">
 <Label className="text-xs">
 {param.name}
 {param.required && <span className="text-destructive">*</span>}
 </Label>
 <Input
 type={param.type === 'number' ? 'number' : 'text'}
 placeholder={param.description || `Enter ${param.name}`}
 value={parameters[param.name] || ''}
 onChange={(e) => handleParameterChange(param.name, e.target.value)}
 className="h-8 text-xs"
 />
 </div>
 ))}
 </div>

 <Separator />

 <div className="space-y-2">
 <Label className="text-xs font-medium">Java Code Preview:</Label>
 <div className="bg-muted p-2 rounded text-xs font-mono">
 {generateJavaFunctionCall(
 selectedFunction.name,
 selectedFunction.parameters.map(param => parameters[param.name] || `{${param.name}}`)
 )}
 </div>
 </div>

 <Button
 onClick={handleApplyFunction}
 className="w-full"
 size="sm"
 disabled={selectedFunction.parameters.some(p => p.required && !parameters[p.name])}
 >
 Apply Function
 </Button>
 </div>
 );
 };

 return (
 <Popover open={open} onOpenChange={setOpen}>
 <PopoverTrigger asChild>
 {trigger || (
 <Button variant="outline" size="sm">
 <Calculator className="h-4 w-4 mr-2" />
 Add Function
 </Button>
 )}
 </PopoverTrigger>
 <PopoverContent className="w-[32rem] p-4 z-50" align="start" sideOffset={4}>
 {selectedFunction ? renderFunctionConfig() : renderFunctionList()}
 </PopoverContent>
 </Popover>
 );
};