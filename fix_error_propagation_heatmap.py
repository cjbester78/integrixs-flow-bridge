#!/usr/bin/env python3
import re

def fix_error_propagation_heatmap():
    file_path = "backend/src/main/java/com/integrixs/backend/dto/ErrorPropagationHeatmap.java"
    
    # Read the file
    with open(file_path, 'r') as f:
        content = f.read()
    
    # Find all method declarations
    methods = {}
    method_pattern = r'public\s+\w+(?:<[^>]+>)?\s+(\w+)\([^)]*\)\s*\{[^}]*\}'
    
    # Find method positions
    for match in re.finditer(method_pattern, content, re.DOTALL):
        method_name = match.group(1)
        if method_name not in methods:
            methods[method_name] = []
        methods[method_name].append((match.start(), match.end()))
    
    # Find duplicates
    duplicates = []
    for method_name, positions in methods.items():
        if len(positions) > 1:
            print(f"Duplicate method: {method_name} appears {len(positions)} times")
            # Keep the first occurrence, mark others for removal
            for pos in positions[1:]:
                duplicates.append(pos)
    
    # Sort duplicates in reverse order (to remove from end to start)
    duplicates.sort(reverse=True)
    
    # Remove duplicates
    for start, end in duplicates:
        # Find the method and remove it with proper spacing
        # Look for the method and any preceding whitespace/newlines
        actual_start = start
        while actual_start > 0 and content[actual_start-1] in '\n\r\t ':
            actual_start -= 1
        content = content[:actual_start] + content[end+1:]
    
    # Write the fixed content
    with open(file_path, 'w') as f:
        f.write(content)
    
    print(f"Fixed {file_path} - removed {len(duplicates)} duplicate methods")

if __name__ == "__main__":
    fix_error_propagation_heatmap()