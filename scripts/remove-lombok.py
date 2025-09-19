#!/usr/bin/env python3
"""
Script to help convert Lombok-annotated Java classes to plain Java classes.
This script analyzes Java files and generates the boilerplate code that Lombok would generate.
"""

import re
import sys
import os
from pathlib import Path
from typing import List, Tuple, Set

def find_lombok_annotations(content: str) -> Set[str]:
    """Find all Lombok annotations in the file."""
    lombok_pattern = r'@(Data|Getter|Setter|Builder|NoArgsConstructor|AllArgsConstructor|RequiredArgsConstructor|ToString|EqualsAndHashCode|Slf4j|Value|With|Accessors|FieldDefaults|SuperBuilder|NonNull|Cleanup|SneakyThrows|Synchronized|Log|Log4j|Log4j2|XSlf4j|CommonsLog|JBossLog|Flogger|CustomLog)\b'
    return set(re.findall(lombok_pattern, content))

def find_fields(content: str) -> List[Tuple[str, str, str]]:
    """Find all fields in the class. Returns list of (modifier, type, name) tuples."""
    field_pattern = r'^\s*(private|protected|public)?\s*(static\s+)?(final\s+)?([a-zA-Z0-9_<>,\[\]\.\s]+)\s+([a-zA-Z0-9_]+)\s*(?:=.*)?;'
    fields = []
    for line in content.split('\n'):
        match = re.match(field_pattern, line)
        if match:
            modifier = match.group(1) or 'private'
            is_static = match.group(2) is not None
            is_final = match.group(3) is not None
            field_type = match.group(4).strip()
            field_name = match.group(5)
            if not is_static and 'transient' not in line and 'volatile' not in line:
                fields.append((modifier, field_type, field_name))
    return fields

def capitalize_first(s: str) -> str:
    """Capitalize first letter of string."""
    return s[0].upper() + s[1:] if s else ''

def generate_getter(field_type: str, field_name: str) -> str:
    """Generate a getter method."""
    method_name = 'is' + capitalize_first(field_name) if field_type == 'boolean' else 'get' + capitalize_first(field_name)
    return f"""
    public {field_type} {method_name}() {{
        return {field_name};
    }}"""

def generate_setter(field_type: str, field_name: str) -> str:
    """Generate a setter method."""
    return f"""
    public void set{capitalize_first(field_name)}({field_type} {field_name}) {{
        this.{field_name} = {field_name};
    }}"""

def generate_constructor(class_name: str, fields: List[Tuple[str, str, str]], all_args: bool = False) -> str:
    """Generate constructor."""
    if not fields and not all_args:
        return f"""
    public {class_name}() {{
    }}"""
    
    params = []
    body = []
    for _, field_type, field_name in fields:
        params.append(f"{field_type} {field_name}")
        body.append(f"        this.{field_name} = {field_name};")
    
    param_str = ", ".join(params)
    body_str = "\n".join(body)
    
    return f"""
    public {class_name}({param_str}) {{
{body_str}
    }}"""

def generate_builder(class_name: str, fields: List[Tuple[str, str, str]]) -> str:
    """Generate builder pattern."""
    builder_methods = []
    builder_fields = []
    
    for _, field_type, field_name in fields:
        builder_fields.append(f"        private {field_type} {field_name};")
        builder_methods.append(f"""
        public {class_name}Builder {field_name}({field_type} {field_name}) {{
            this.{field_name} = {field_name};
            return this;
        }}""")
    
    build_assignments = []
    for _, field_type, field_name in fields:
        build_assignments.append(f"            instance.{field_name} = this.{field_name};")
    
    return f"""
    public static {class_name}Builder builder() {{
        return new {class_name}Builder();
    }}
    
    public static class {class_name}Builder {{
{chr(10).join(builder_fields)}
        
{chr(10).join(builder_methods)}
        
        public {class_name} build() {{
            {class_name} instance = new {class_name}();
{chr(10).join(build_assignments)}
            return instance;
        }}
    }}"""

def generate_toString(class_name: str, fields: List[Tuple[str, str, str]]) -> str:
    """Generate toString method."""
    field_parts = []
    for _, _, field_name in fields:
        field_parts.append(f'{field_name}=" + {field_name} + "')
    
    fields_str = ", ".join(field_parts)
    return f"""
    @Override
    public String toString() {{
        return "{class_name}{{" +
                "{fields_str}" +
                '}}';
    }}"""

def generate_equals_hashcode(class_name: str, fields: List[Tuple[str, str, str]]) -> str:
    """Generate equals and hashCode methods."""
    equals_checks = []
    hash_fields = []
    
    for _, field_type, field_name in fields:
        if field_type in ['int', 'long', 'float', 'double', 'boolean', 'char', 'byte', 'short']:
            equals_checks.append(f"        if ({field_name} != other.{field_name}) return false;")
        else:
            equals_checks.append(f"        if ({field_name} != null ? !{field_name}.equals(other.{field_name}) : other.{field_name} != null) return false;")
        hash_fields.append(field_name)
    
    equals_body = "\n".join(equals_checks) if equals_checks else "        return true;"
    hash_body = "Objects.hash(" + ", ".join(hash_fields) + ")" if hash_fields else "0"
    
    return f"""
    @Override
    public boolean equals(Object o) {{
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        {class_name} other = ({class_name}) o;
{equals_body}
        return true;
    }}
    
    @Override
    public int hashCode() {{
        return {hash_body};
    }}"""

def generate_logger(class_name: str, log_type: str = "Slf4j") -> str:
    """Generate logger field."""
    if log_type == "Slf4j":
        return f"    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger({class_name}.class);"
    elif log_type == "Log4j2":
        return f"    private static final org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger({class_name}.class);"
    else:
        return f"    private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger({class_name}.class.getName());"

def process_java_file(file_path: str) -> str:
    """Process a Java file and return the converted content."""
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()
    
    # Find all Lombok annotations
    lombok_annotations = find_lombok_annotations(content)
    if not lombok_annotations:
        return content
    
    # Extract class name
    class_match = re.search(r'(?:public\s+)?(?:abstract\s+)?class\s+(\w+)', content)
    if not class_match:
        return content
    class_name = class_match.group(1)
    
    # Find fields
    fields = find_fields(content)
    
    # Remove Lombok imports
    content = re.sub(r'import\s+lombok\.[^;]+;\s*\n', '', content)
    
    # Remove Lombok annotations
    for annotation in lombok_annotations:
        content = re.sub(rf'@{annotation}(?:\([^)]*\))?\s*\n', '', content)
    
    # Generate code based on annotations
    generated_code = []
    
    # Add logger if needed
    if 'Slf4j' in lombok_annotations:
        # Find where to insert logger (after class declaration)
        class_pos = content.find('{', content.find('class ' + class_name))
        if class_pos != -1:
            content = content[:class_pos+1] + '\n' + generate_logger(class_name, 'Slf4j') + '\n' + content[class_pos+1:]
    
    # Generate methods
    if 'Data' in lombok_annotations or 'Getter' in lombok_annotations:
        for _, field_type, field_name in fields:
            generated_code.append(generate_getter(field_type, field_name))
    
    if 'Data' in lombok_annotations or 'Setter' in lombok_annotations:
        for _, field_type, field_name in fields:
            generated_code.append(generate_setter(field_type, field_name))
    
    if 'NoArgsConstructor' in lombok_annotations:
        generated_code.append(generate_constructor(class_name, []))
    
    if 'AllArgsConstructor' in lombok_annotations:
        generated_code.append(generate_constructor(class_name, fields, all_args=True))
    
    if 'RequiredArgsConstructor' in lombok_annotations:
        final_fields = [(m, t, n) for m, t, n in fields if 'final' in content.split(n)[0].split('\n')[-1]]
        generated_code.append(generate_constructor(class_name, final_fields))
    
    if 'Builder' in lombok_annotations:
        generated_code.append(generate_builder(class_name, fields))
    
    if 'Data' in lombok_annotations or 'ToString' in lombok_annotations:
        generated_code.append(generate_toString(class_name, fields))
    
    if 'Data' in lombok_annotations or 'EqualsAndHashCode' in lombok_annotations:
        generated_code.append(generate_equals_hashcode(class_name, fields))
    
    # Insert generated code before the last closing brace
    if generated_code:
        # Find the last closing brace of the class
        last_brace = content.rfind('}')
        if last_brace != -1:
            # Add imports if needed
            if 'equals' in ''.join(generated_code) and 'import java.util.Objects;' not in content:
                import_pos = content.find('package')
                if import_pos != -1:
                    import_end = content.find('\n\n', import_pos)
                    if import_end != -1:
                        content = content[:import_end] + '\nimport java.util.Objects;' + content[import_end:]
            
            content = content[:last_brace] + '\n'.join(generated_code) + '\n' + content[last_brace:]
    
    return content

def main():
    """Main function to process files."""
    if len(sys.argv) < 2:
        print("Usage: python remove-lombok.py <file_or_directory>")
        print("Examples:")
        print("  python remove-lombok.py MyClass.java")
        print("  python remove-lombok.py src/main/java/")
        sys.exit(1)
    
    target = sys.argv[1]
    
    if os.path.isfile(target):
        # Process single file
        print(f"Processing {target}...")
        result = process_java_file(target)
        
        # Save to new file or overwrite based on flag
        output_file = target if len(sys.argv) > 2 and sys.argv[2] == '--overwrite' else target.replace('.java', '_converted.java')
        with open(output_file, 'w', encoding='utf-8') as f:
            f.write(result)
        print(f"Converted file saved to {output_file}")
    
    elif os.path.isdir(target):
        # Process directory
        java_files = list(Path(target).rglob("*.java"))
        print(f"Found {len(java_files)} Java files")
        
        for java_file in java_files:
            try:
                content = process_java_file(str(java_file))
                
                # Check if file was modified
                with open(java_file, 'r', encoding='utf-8') as f:
                    original = f.read()
                
                if content != original:
                    print(f"Converting {java_file}...")
                    if len(sys.argv) > 2 and sys.argv[2] == '--overwrite':
                        with open(java_file, 'w', encoding='utf-8') as f:
                            f.write(content)
                    else:
                        print(f"  Preview mode - use --overwrite to modify files")
            except Exception as e:
                print(f"Error processing {java_file}: {e}")
    
    else:
        print(f"Error: {target} is not a valid file or directory")
        sys.exit(1)

if __name__ == "__main__":
    main()