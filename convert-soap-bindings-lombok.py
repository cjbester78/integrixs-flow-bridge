#!/usr/bin/env python3
"""
Quick script to help convert Lombok DTOs in soap-bindings module
"""
import os
import re

# DTOs that need conversion
dto_files = [
    "SoapBindingDTO",
    "UpdateBindingRequestDTO",
    "BindingTestResultDTO",
    "SoapOperationRequestDTO",
    "SoapOperationResponseDTO",
    "GeneratedBindingDTO",
    "UploadWsdlRequestDTO",
    "WsdlDetailsDTO"
]

# Simple field patterns
simple_fields = {
    "SoapBindingDTO": [
        ("String", "bindingId"),
        ("String", "bindingName"),
        ("String", "wsdlId"),
        ("String", "serviceName"),
        ("String", "portName"),
        ("String", "endpointUrl"),
        ("String", "bindingStyle"),
        ("String", "transport"),
        ("boolean", "active"),
        ("boolean", "requiresAuth"),
        ("boolean", "secureTransport"),
        ("LocalDateTime", "createdAt"),
        ("LocalDateTime", "updatedAt")
    ],
    "UpdateBindingRequestDTO": [
        ("String", "endpointUrl"),
        ("Map<String, String>", "soapHeaders"),
        ("SecurityConfigurationDTO", "security")
    ],
    "BindingTestResultDTO": [
        ("boolean", "success"),
        ("String", "message"),
        ("String", "response"),
        ("Long", "responseTimeMs")
    ],
    "SoapOperationRequestDTO": [
        ("String", "bindingId"),
        ("String", "operationName"),
        ("String", "soapAction"),
        ("String", "requestPayload"),
        ("Map<String, String>", "headers")
    ],
    "SoapOperationResponseDTO": [
        ("String", "responsePayload"),
        ("int", "statusCode"),
        ("Map<String, String>", "responseHeaders"),
        ("Long", "responseTimeMs")
    ],
    "GeneratedBindingDTO": [
        ("boolean", "success"),
        ("String", "message"),
        ("String", "generatedCode"),
        ("String", "packageName"),
        ("List<String>", "generatedFiles")
    ],
    "UploadWsdlRequestDTO": [
        ("String", "name"),
        ("String", "description"),
        ("byte[]", "wsdlContent")
    ],
    "WsdlDetailsDTO": [
        ("String", "wsdlId"),
        ("String", "name"),
        ("String", "namespace"),
        ("String", "targetNamespace"),
        ("String", "description"),
        ("String", "wsdlLocation"),
        ("LocalDateTime", "createdAt"),
        ("LocalDateTime", "updatedAt"),
        ("List<String>", "services"),
        ("List<String>", "operations")
    ]
}

def generate_getters_setters(fields):
    """Generate getters and setters for fields"""
    getters = []
    setters = []
    
    for field_type, field_name in fields:
        # Getter
        if field_type == "boolean":
            getter_name = "is" + field_name[0].upper() + field_name[1:]
        else:
            getter_name = "get" + field_name[0].upper() + field_name[1:]
        getters.append(f"    public {field_type} {getter_name}() {{ return {field_name}; }}")
        
        # Setter
        setter_name = "set" + field_name[0].upper() + field_name[1:]
        setters.append(f"    public void {setter_name}({field_type} {field_name}) {{ this.{field_name} = {field_name}; }}")
    
    return "\n".join(getters), "\n".join(setters)

def generate_builder(class_name, fields):
    """Generate builder pattern"""
    builder_class = f"""    // Builder
    public static {class_name}Builder builder() {{
        return new {class_name}Builder();
    }}
    
    public static class {class_name}Builder {{
"""
    
    # Add fields
    for field_type, field_name in fields:
        # Handle default values
        if field_type == "Map<String, String>":
            builder_class += f"        private {field_type} {field_name} = new HashMap<>();\n"
        elif field_type.startswith("List<"):
            builder_class += f"        private {field_type} {field_name} = new ArrayList<>();\n"
        else:
            builder_class += f"        private {field_type} {field_name};\n"
    
    builder_class += "\n"
    
    # Add builder methods
    for field_type, field_name in fields:
        builder_class += f"""        public {class_name}Builder {field_name}({field_type} {field_name}) {{
            this.{field_name} = {field_name};
            return this;
        }}
        
"""
    
    # Add build method
    builder_class += f"        public {class_name} build() {{\n"
    builder_class += f"            return new {class_name}("
    builder_class += ", ".join([f[1] for f in fields])
    builder_class += ");\n"
    builder_class += "        }\n"
    builder_class += "    }"
    
    return builder_class

def generate_constructors(class_name, fields):
    """Generate default and all-args constructors"""
    default_constructor = f"""    // Default constructor
    public {class_name}() {{"""
    
    # Add default initializations
    for field_type, field_name in fields:
        if field_type == "Map<String, String>":
            default_constructor += f"\n        this.{field_name} = new HashMap<>();"
        elif field_type.startswith("List<"):
            default_constructor += f"\n        this.{field_name} = new ArrayList<>();"
    
    default_constructor += "\n    }"
    
    # All args constructor
    all_args_params = []
    all_args_body = []
    
    for field_type, field_name in fields:
        all_args_params.append(f"{field_type} {field_name}")
        if field_type == "Map<String, String>":
            all_args_body.append(f"        this.{field_name} = {field_name} != null ? {field_name} : new HashMap<>();")
        elif field_type.startswith("List<"):
            all_args_body.append(f"        this.{field_name} = {field_name} != null ? {field_name} : new ArrayList<>();")
        else:
            all_args_body.append(f"        this.{field_name} = {field_name};")
    
    all_args_constructor = f"""    
    // All args constructor
    public {class_name}({', '.join(all_args_params)}) {{
{chr(10).join(all_args_body)}
    }}"""
    
    return default_constructor, all_args_constructor

# Print conversion code for each DTO
for dto_name, fields in simple_fields.items():
    print(f"\n{'='*60}")
    print(f"// {dto_name}.java")
    print(f"{'='*60}\n")
    
    # Field declarations
    print("    // Fields")
    for field_type, field_name in fields:
        if field_type == "Map<String, String>":
            print(f"    private {field_type} {field_name} = new HashMap<>();")
        elif field_type.startswith("List<"):
            print(f"    private {field_type} {field_name} = new ArrayList<>();")
        else:
            print(f"    private {field_type} {field_name};")
    
    print()
    
    # Constructors
    default_constructor, all_args_constructor = generate_constructors(dto_name, fields)
    print(default_constructor)
    print(all_args_constructor)
    
    print("\n    // Getters")
    getters, setters = generate_getters_setters(fields)
    print(getters)
    
    print("\n    // Setters")
    print(setters)
    
    print()
    print(generate_builder(dto_name, fields))