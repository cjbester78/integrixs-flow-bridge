#!/usr/bin/env python3
import os
import re
import sys
from pathlib import Path

def convert_lombok_file(filepath):
    with open(filepath, 'r') as f:
        content = f.read()
    
    # Skip if already converted
    if 'import lombok' not in content:
        return False
    
    original_content = content
    
    # Remove lombok imports
    content = re.sub(r'^import lombok\.[^;]+;\s*\n', '', content, flags=re.MULTILINE)
    
    # Extract class info
    class_match = re.search(r'public\s+(?:abstract\s+)?class\s+(\w+)', content)
    if not class_match:
        return False
    
    class_name = class_match.group(1)
    
    # Extract fields with their annotations
    field_pattern = r'(@\w+(?:\([^)]*\))?\s*)*private\s+(?:final\s+)?(\S+)\s+(\w+)(?:\s*=\s*[^;]+)?;'
    fields = []
    for match in re.finditer(field_pattern, content):
        annotations = match.group(1) or ''
        field_type = match.group(2)
        field_name = match.group(3)
        # Clean up annotations - keep validation annotations
        validation_annotations = []
        for ann_match in re.finditer(r'@(NotNull|NotEmpty|NotBlank|Size|Pattern|Min|Max|Email|Valid)(?:\([^)]*\))?', annotations):
            validation_annotations.append(ann_match.group(0))
        fields.append({
            'type': field_type,
            'name': field_name,
            'annotations': '\n    '.join(validation_annotations) if validation_annotations else ''
        })
    
    # Remove Lombok annotations
    content = re.sub(r'@Data\s*\n', '', content)
    content = re.sub(r'@Getter\s*\n', '', content)
    content = re.sub(r'@Setter\s*\n', '', content)
    content = re.sub(r'@Builder\s*\n', '', content)
    content = re.sub(r'@NoArgsConstructor\s*\n', '', content)
    content = re.sub(r'@AllArgsConstructor\s*\n', '', content)
    content = re.sub(r'@RequiredArgsConstructor\s*\n', '', content)
    content = re.sub(r'@ToString\s*\n', '', content)
    content = re.sub(r'@EqualsAndHashCode\s*\n', '', content)
    content = re.sub(r'@SuperBuilder\s*\n', '', content)
    content = re.sub(r'@Slf4j\s*\n', '', content)
    content = re.sub(r'@Value\s*\n', '', content)
    content = re.sub(r'@With\s*\n', '', content)
    
    # Remove @Builder.Default
    content = re.sub(r'\s*@Builder\.Default', '', content)
    
    # Generate constructors, getters, setters, and builder
    constructors = generate_constructors(class_name, fields)
    getters = generate_getters(fields)
    setters = generate_setters(fields)
    builder = generate_builder(class_name, fields)
    
    # Insert the generated code before the closing brace
    insert_pos = content.rfind('}')
    if insert_pos == -1:
        return False
    
    generated_code = f"\n{constructors}\n{getters}\n{setters}\n{builder}"
    
    # Insert generated code
    content = content[:insert_pos] + generated_code + content[insert_pos:]
    
    # Write back
    if content != original_content:
        with open(filepath, 'w') as f:
            f.write(content)
        print(f"Converted: {filepath}")
        return True
    
    return False

def generate_constructors(class_name, fields):
    # Default constructor
    default_constructor = f"""    // Default constructor
    public {class_name}() {{"""
    
    # Initialize collections and default values
    for field in fields:
        if 'Map' in field['type'] or 'HashMap' in field['type']:
            default_constructor += f"\n        this.{field['name']} = new HashMap<>();"
        elif 'List' in field['type'] or 'ArrayList' in field['type']:
            default_constructor += f"\n        this.{field['name']} = new ArrayList<>();"
        elif 'Set' in field['type'] or 'HashSet' in field['type']:
            default_constructor += f"\n        this.{field['name']} = new HashSet<>();"
    
    default_constructor += "\n    }"
    
    # All args constructor
    if fields:
        params = ", ".join(f"{field['type']} {field['name']}" for field in fields)
        all_args = f"""
    // All args constructor
    public {class_name}({params}) {{"""
        
        for field in fields:
            if 'Map' in field['type'] or 'List' in field['type'] or 'Set' in field['type']:
                all_args += f"\n        this.{field['name']} = {field['name']} != null ? {field['name']} : new {field['type'].replace('<', '<>();')};"
            else:
                all_args += f"\n        this.{field['name']} = {field['name']};"
        
        all_args += "\n    }"
        
        return default_constructor + "\n" + all_args
    
    return default_constructor

def generate_getters(fields):
    getters = "\n    // Getters"
    for field in fields:
        field_name = field['name']
        field_type = field['type']
        getter_name = 'is' if field_type == 'boolean' else 'get'
        method_name = getter_name + field_name[0].upper() + field_name[1:]
        
        if field['annotations']:
            getters += f"\n    {field['annotations']}"
        getters += f"\n    public {field_type} {method_name}() {{"
        getters += f"\n        return {field_name};"
        getters += "\n    }"
    
    return getters

def generate_setters(fields):
    setters = "\n    // Setters"
    for field in fields:
        field_name = field['name']
        field_type = field['type']
        method_name = 'set' + field_name[0].upper() + field_name[1:]
        
        setters += f"\n    public void {method_name}({field_type} {field_name}) {{"
        setters += f"\n        this.{field_name} = {field_name};"
        setters += "\n    }"
    
    return setters

def generate_builder(class_name, fields):
    builder = f"""
    // Builder
    public static {class_name}Builder builder() {{
        return new {class_name}Builder();
    }}
    
    public static class {class_name}Builder {{"""
    
    # Builder fields
    for field in fields:
        if 'Map' in field['type'] or 'HashMap' in field['type']:
            builder += f"\n        private {field['type']} {field['name']} = new HashMap<>();"
        elif 'List' in field['type'] or 'ArrayList' in field['type']:
            builder += f"\n        private {field['type']} {field['name']} = new ArrayList<>();"
        elif 'Set' in field['type'] or 'HashSet' in field['type']:
            builder += f"\n        private {field['type']} {field['name']} = new HashSet<>();"
        else:
            builder += f"\n        private {field['type']} {field['name']};"
    
    # Builder methods
    for field in fields:
        field_name = field['name']
        field_type = field['type']
        builder += f"\n\n        public {class_name}Builder {field_name}({field_type} {field_name}) {{"
        builder += f"\n            this.{field_name} = {field_name};"
        builder += "\n            return this;"
        builder += "\n        }"
    
    # Build method
    builder += f"\n\n        public {class_name} build() {{"
    if fields:
        params = ", ".join(field['name'] for field in fields)
        builder += f"\n            return new {class_name}({params});"
    else:
        builder += f"\n            return new {class_name}();"
    builder += "\n        }"
    builder += "\n    }"
    
    return builder

def main():
    # Find all Java files in webserver module
    webserver_path = Path("webserver/src/main/java")
    java_files = list(webserver_path.rglob("*.java"))
    
    converted_count = 0
    for java_file in java_files:
        if convert_lombok_file(str(java_file)):
            converted_count += 1
    
    print(f"\nTotal files converted: {converted_count}")

if __name__ == "__main__":
    main()