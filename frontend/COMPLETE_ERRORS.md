# Complete List of Current Parsing Errors

## Summary (Updated 2025-09-06)
- **Current status**: 55 parsing errors, 26 warnings (81 total problems)
- **Files with errors**: 49 files
- **Error reduction achieved**: 65% (from 156 to 55)

## Parsing Errors by Category

### Field Mapping Components (3 files)
- [ ] fieldMapping/FieldSelectorDialog.tsx - Line 136:53: Expression expected
- [ ] fieldMapping/FunctionMappingModal.tsx - Line 77:29: Invalid character
- [ ] fieldMapping/VisualFlowEditor.tsx - Line 220:1: ',' expected

### Flow Components (7 files)
- [ ] flow/DeploymentDetailsDialog.tsx - Line 194:75: ',' expected
- [ ] flow/FlowExecutionMonitor.tsx - Line 117:74: ':' expected
- [ ] flow/FlowExecutionVisualizer.tsx - Line 114:0: 'catch' or 'finally' expected
- [ ] flow/FlowExportDialog.tsx - Line 159:62: '}' expected
- [ ] flow/FlowScheduler.tsx - Line 154:2: ')' expected
- [ ] flow/TestFlowDialog.tsx - Line 160:1: Declaration or statement expected

### Messages Components (2 files)
- [ ] messages/MessageList.tsx - Line 58:43: ',' expected
- [ ] messages/components/MessageCard.tsx - Line 52:1: ',' expected

### Orchestration Components (3 files)
- [ ] orchestration/VisualOrchestrationEditor.tsx - Line 144:42: Expression or comma expected
- [ ] orchestration/nodes/StartProcessNode.tsx - Line 137:43: ',' expected  
- [ ] orchestration/nodes/AdapterNode.tsx - Line 259:0: Declaration or statement expected

### Package Components (1 file)
- [ ] packages/PackageCreationWizard.tsx - Line 685:3: Expected corresponding JSX closing tag for 'Card'

### UI Components (20 files)
- [ ] ui/alert.tsx - Line 6:26: ')' expected
- [ ] ui/badge.tsx - Line 6:26: ')' expected
- [ ] ui/button.tsx - Line 7:27: ')' expected
- [ ] ui/carousel.tsx - Line 82:41: ')' expected
- [ ] ui/chart.tsx - Line 35:40: Expression expected
- [ ] ui/data-table.tsx - Line 196:1: '}' expected
- [ ] ui/form.tsx - Line 58:31: Property assignment expected
- [ ] ui/input-otp.tsx - Line 7:34: Expression expected
- [ ] ui/input.tsx - Line 5:80: ')' expected
- [ ] ui/label.tsx - Line 7:26: ')' expected
- [ ] ui/navigation-menu.tsx - Line 42:39: ')' expected
- [ ] ui/password-confirmation.tsx - Line 154:4: Unterminated template literal
- [ ] ui/progress.tsx - Line 27:1: Unterminated template literal
- [ ] ui/radio-group.tsx - Line 12:26: Identifier expected
- [ ] ui/sheet.tsx - Line 27:26: ')' expected
- [ ] ui/sidebar.tsx - Line 48:41: Expression expected
- [ ] ui/textarea.tsx - Line 7:70: ')' expected
- [ ] ui/toast.tsx - Line 10:39: Expression expected
- [ ] ui/toggle-group.tsx - Line 8:47: Expression expected
- [ ] ui/toggle.tsx - Line 7:27: ')' expected

### Pages (5 files)
- [ ] AdapterMonitoring.tsx - Line 237:0: Unterminated template literal
- [ ] AllInterfaces.tsx - Line 459:1: '}' expected
- [ ] CreateDataStructure.tsx - Line 148:2: ')' expected
- [ ] CreateDirectMappingFlow.tsx - Line 337:2: 'catch' or 'finally' expected

### Services (9 files)
- [x] flowExportImportService.ts - Fixed parsing errors (extra backticks)
- [x] flowMonitoringService.ts - Fixed all parsing errors (semicolons, extra braces)
- [ ] messageService.ts - Line 62:2: Declaration or statement expected
- [x] structureService.ts - Fixed all parsing errors (commas to semicolons, etc.)
- [x] systemConfigService.ts - Fixed all parsing errors (commas to semicolons)
- [x] systemErrorLogger.ts - Fixed all parsing errors
- [x] systemMonitoringService.ts - Fixed all parsing errors
- [ ] userService.ts - Line 8:19: Unterminated string literal
- [ ] webserviceService.ts - Line 23:7: Unterminated string literal

### Utils (1 file)
- [ ] utils/structureParsers.ts - Line 150:2: Declaration or statement expected

## Non-Parsing Errors (2 files)
- [ ] admin/SystemSettings.tsx - Line 31:32: Unexpected empty object pattern (no-empty-pattern)
- [ ] fieldMapping/MappingArea.tsx - Line 107:7: 'fieldPath' is never reassigned. Use 'const' instead (prefer-const)