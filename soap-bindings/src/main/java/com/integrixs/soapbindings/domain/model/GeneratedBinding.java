package com.integrixs.soapbindings.domain.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Domain model representing generated SOAP binding classes
 */
@Data
@Builder
public class GeneratedBinding {
    private String bindingId;
    private String generationId;
    private String wsdlId;
    private String packageName;
    private String serviceName;
    private String outputDirectory;
    @Builder.Default
    private Map<String, ClassInfo> generatedClasses = new HashMap<>();
    @Builder.Default
    private Map<String, String> generatedClassesContent = new HashMap<>();
    private LocalDateTime generatedAt;
    private LocalDateTime generationTime;
    private String generatorVersion;
    private GenerationStatus status;
    private String errorMessage;
    
    /**
     * Generation status
     */
    public enum GenerationStatus {
        PENDING,
        IN_PROGRESS,
        COMPLETED,
        FAILED
    }
    
    /**
     * Information about generated class
     */
    @Data
    @Builder
    public static class ClassInfo {
        private String className;
        private String fullQualifiedName;
        private ClassType classType;
        private String sourceFile;
        private boolean compiled;
        
        public enum ClassType {
            SERVICE,
            PORT,
            REQUEST,
            RESPONSE,
            FAULT,
            OBJECT_FACTORY,
            PACKAGE_INFO
        }
    }
    
    /**
     * Add generated class info
     * @param className Class name
     * @param classInfo Class information
     */
    public void addGeneratedClass(String className, ClassInfo classInfo) {
        this.generatedClasses.put(className, classInfo);
    }
    
    /**
     * Get service interface class
     * @return Service interface class info
     */
    public ClassInfo getServiceInterface() {
        return generatedClasses.values().stream()
                .filter(info -> info.getClassType() == ClassInfo.ClassType.SERVICE)
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Check if generation was successful
     * @return true if successful
     */
    public boolean isSuccessful() {
        return status == GenerationStatus.COMPLETED;
    }
}