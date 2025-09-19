#!/usr/bin/env python3
"""
Batch conversion of Lombok files to plain Java
"""
import os
import re

def remove_lombok_and_add_boilerplate(file_path):
    """Remove Lombok annotations and add boilerplate code"""
    
    with open(file_path, 'r') as f:
        content = f.read()
    
    # Skip if already converted
    if '@Data' not in content and '@Builder' not in content:
        print(f"Skipping {file_path} - already converted")
        return False
        
    print(f"Converting {file_path}")
    
    # Extract class name
    class_match = re.search(r'public class (\w+)', content)
    if not class_match:
        print(f"Could not find class name in {file_path}")
        return False
    
    class_name = class_match.group(1)
    
    # Extract package
    package_match = re.search(r'package ([\w.]+);', content)
    package = package_match.group(0) if package_match else ''
    
    # Extract imports (excluding lombok)
    import_lines = []
    for line in content.split('\n'):
        if line.strip().startswith('import') and 'lombok' not in line:
            import_lines.append(line)
    
    # Extract fields
    fields = []
    lines = content.split('\n')
    in_class = False
    for i, line in enumerate(lines):
        if 'public class' in line:
            in_class = True
            continue
        if in_class and line.strip().startswith('private'):
            # Extract field type and name
            field_match = re.match(r'\s*private\s+(.+?)\s+(\w+)(\s*=.*)?;', line)
            if field_match:
                field_type = field_match.group(1)
                field_name = field_match.group(2)
                default_value = field_match.group(3) if field_match.group(3) else None
                fields.append((field_type, field_name, default_value))
    
    # Check if we need additional imports
    needs_hashmap = any('Map<' in f[0] for f in fields)
    needs_arraylist = any('List<' in f[0] for f in fields)
    needs_hashset = any('Set<' in f[0] for f in fields)
    
    # Build new content
    new_content = package + '\n\n'
    
    # Add imports
    for imp in import_lines:
        new_content += imp + '\n'
    
    if needs_hashmap and 'import java.util.HashMap;' not in content:
        new_content += 'import java.util.HashMap;\n'
    if needs_arraylist and 'import java.util.ArrayList;' not in content:
        new_content += 'import java.util.ArrayList;\n'
    if needs_hashset and 'import java.util.HashSet;' not in content:
        new_content += 'import java.util.HashSet;\n'
    
    # Extract class javadoc
    javadoc_match = re.search(r'(/\*\*.*?\*/)\s*@', content, re.DOTALL)
    if javadoc_match:
        new_content += '\n' + javadoc_match.group(1) + '\n'
    
    new_content += f'public class {class_name} {{\n\n'
    
    # Add fields
    for field_type, field_name, default_value in fields:
        if default_value:
            new_content += f'    private {field_type} {field_name}{default_value};\n'
        else:
            new_content += f'    private {field_type} {field_name};\n'
    
    # Default constructor
    new_content += '\n    // Default constructor\n'
    new_content += f'    public {class_name}() {{\n'
    
    # Add default initializations
    for field_type, field_name, _ in fields:
        if 'Map<' in field_type and 'HashMap<>' not in str(_):
            new_content += f'        this.{field_name} = new HashMap<>();\n'
        elif 'List<' in field_type and 'ArrayList<>' not in str(_):
            new_content += f'        this.{field_name} = new ArrayList<>();\n'
        elif 'Set<' in field_type and 'HashSet<>' not in str(_):
            new_content += f'        this.{field_name} = new HashSet<>();\n'
    
    new_content += '    }\n'
    
    # All args constructor
    if fields:
        new_content += '\n    // All args constructor\n'
        new_content += f'    public {class_name}('
        
        # Parameters
        params = []
        for field_type, field_name, _ in fields:
            params.append(f'{field_type} {field_name}')
        new_content += ', '.join(params)
        new_content += ') {\n'
        
        # Body
        for field_type, field_name, _ in fields:
            if 'Map<' in field_type:
                new_content += f'        this.{field_name} = {field_name} != null ? {field_name} : new HashMap<>();\n'
            elif 'List<' in field_type:
                new_content += f'        this.{field_name} = {field_name} != null ? {field_name} : new ArrayList<>();\n'
            elif 'Set<' in field_type:
                new_content += f'        this.{field_name} = {field_name} != null ? {field_name} : new HashSet<>();\n'
            else:
                new_content += f'        this.{field_name} = {field_name};\n'
        
        new_content += '    }\n'
    
    # Getters
    if fields:
        new_content += '\n    // Getters\n'
        for field_type, field_name, _ in fields:
            if field_type == 'boolean' or field_type == 'Boolean':
                getter_name = 'is' + field_name[0].upper() + field_name[1:]
            else:
                getter_name = 'get' + field_name[0].upper() + field_name[1:]
            new_content += f'    public {field_type} {getter_name}() {{ return {field_name}; }}\n'
    
    # Setters
    if fields:
        new_content += '\n    // Setters\n'
        for field_type, field_name, _ in fields:
            setter_name = 'set' + field_name[0].upper() + field_name[1:]
            new_content += f'    public void {setter_name}({field_type} {field_name}) {{ this.{field_name} = {field_name}; }}\n'
    
    # Builder
    if '@Builder' in content:
        new_content += '\n    // Builder\n'
        new_content += f'    public static {class_name}Builder builder() {{\n'
        new_content += f'        return new {class_name}Builder();\n'
        new_content += '    }\n'
        
        new_content += f'\n    public static class {class_name}Builder {{\n'
        
        # Builder fields
        for field_type, field_name, default_value in fields:
            if default_value and '@Builder.Default' in content:
                new_content += f'        private {field_type} {field_name}{default_value};\n'
            elif 'Map<' in field_type:
                new_content += f'        private {field_type} {field_name} = new HashMap<>();\n'
            elif 'List<' in field_type:
                new_content += f'        private {field_type} {field_name} = new ArrayList<>();\n'
            elif 'Set<' in field_type:
                new_content += f'        private {field_type} {field_name} = new HashSet<>();\n'
            else:
                new_content += f'        private {field_type} {field_name};\n'
        
        new_content += '\n'
        
        # Builder methods
        for field_type, field_name, _ in fields:
            new_content += f'        public {class_name}Builder {field_name}({field_type} {field_name}) {{\n'
            new_content += f'            this.{field_name} = {field_name};\n'
            new_content += '            return this;\n'
            new_content += '        }\n\n'
        
        # Build method
        new_content += f'        public {class_name} build() {{\n'
        if fields:
            new_content += f'            return new {class_name}('
            new_content += ', '.join([f[1] for f in fields])
            new_content += ');\n'
        else:
            new_content += f'            return new {class_name}();\n'
        new_content += '        }\n'
        new_content += '    }\n'
    
    new_content += '}\n'
    
    # Write back
    with open(file_path, 'w') as f:
        f.write(new_content)
    
    return True

# Convert critical DTOs in shared-lib - including subdirectories
dto_files = [
    # Export subdirectory
    '/Users/cjbester/git/Integrixs-Flow-Bridge/shared-lib/src/main/java/com/integrixs/shared/dto/export/FlowExportDTO.java',
    '/Users/cjbester/git/Integrixs-Flow-Bridge/shared-lib/src/main/java/com/integrixs/shared/dto/export/FlowImportValidationDTO.java',
    '/Users/cjbester/git/Integrixs-Flow-Bridge/shared-lib/src/main/java/com/integrixs/shared/dto/export/FlowExportRequestDTO.java',
    '/Users/cjbester/git/Integrixs-Flow-Bridge/shared-lib/src/main/java/com/integrixs/shared/dto/export/FlowImportResultDTO.java',
    '/Users/cjbester/git/Integrixs-Flow-Bridge/shared-lib/src/main/java/com/integrixs/shared/dto/export/FlowImportRequestDTO.java',
    # Log subdirectory
    '/Users/cjbester/git/Integrixs-Flow-Bridge/shared-lib/src/main/java/com/integrixs/shared/dto/log/LogExportRequest.java',
    '/Users/cjbester/git/Integrixs-Flow-Bridge/shared-lib/src/main/java/com/integrixs/shared/dto/log/FrontendLogBatchRequest.java',
    '/Users/cjbester/git/Integrixs-Flow-Bridge/shared-lib/src/main/java/com/integrixs/shared/dto/log/FrontendLogEntry.java',
    '/Users/cjbester/git/Integrixs-Flow-Bridge/shared-lib/src/main/java/com/integrixs/shared/dto/log/LogSearchResult.java',
    '/Users/cjbester/git/Integrixs-Flow-Bridge/shared-lib/src/main/java/com/integrixs/shared/dto/log/LogSearchCriteria.java',
    '/Users/cjbester/git/Integrixs-Flow-Bridge/shared-lib/src/main/java/com/integrixs/shared/dto/log/FlowExecutionTimeline.java',
    '/Users/cjbester/git/Integrixs-Flow-Bridge/shared-lib/src/main/java/com/integrixs/shared/dto/log/CorrelatedLogGroup.java',
    # Adapter subdirectory
    '/Users/cjbester/git/Integrixs-Flow-Bridge/shared-lib/src/main/java/com/integrixs/shared/dto/adapter/XmlMappingConfig.java'
]

for dto_file in dto_files:
    if os.path.exists(dto_file):
        remove_lombok_and_add_boilerplate(dto_file)