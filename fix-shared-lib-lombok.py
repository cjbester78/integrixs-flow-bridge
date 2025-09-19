#!/usr/bin/env python3
"""
Fix critical Lombok DTOs in shared-lib module
"""
import os

# Most critical DTOs that are blocking compilation
critical_dtos = [
    "AdapterStatusDTO",
    "MessageStatsDTO", 
    "RecentMessageDTO",
    "RoleDTO",
    "DashboardStatsDTO",
    "JarFileDTO",
    "ChannelStatusDTO",
    "DeploymentInfoDTO",
    "ExternalAuthenticationDTO",
    "FieldMappingDTO",
    "FunctionParameterDTO",
    "GlobalRetrySettingsDTO",
    "TestFieldMappingsRequestDTO",
    "TestFieldMappingsResponseDTO",
    "FlowTransformationDTO"
]

base_path = "/Users/cjbester/git/Integrixs-Flow-Bridge/shared-lib/src/main/java/com/integrixs/shared/dto/"

for dto_name in critical_dtos:
    file_path = os.path.join(base_path, dto_name + ".java")
    if os.path.exists(file_path):
        print(f"Converting {dto_name}")
        os.system(f'python3 /Users/cjbester/git/Integrixs-Flow-Bridge/batch-convert-lombok.py')
        break  # Just do one to test