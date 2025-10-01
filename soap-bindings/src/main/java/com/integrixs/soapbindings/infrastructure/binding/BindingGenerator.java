package com.integrixs.soapbindings.infrastructure.binding;

import com.integrixs.soapbindings.domain.model.GeneratedBinding;
import com.integrixs.soapbindings.domain.model.WsdlDefinition;
import com.integrixs.soapbindings.domain.enums.GenerationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Binding generator using wsimport
 */
@Component
public class BindingGenerator {

    private static final Logger logger = LoggerFactory.getLogger(BindingGenerator.class);

    private static final String TEMP_DIR_PREFIX = "soap - bindings-";
    private static final String WSDL_FILE_NAME = "service.wsdl";

    /**
     * Generate Java bindings from WSDL
     * @param wsdl WSDL definition
     * @param packageName Target package name
     * @param outputDirectory Output directory
     * @return Generated binding information
     */
    public GeneratedBinding generateFromWsdl(WsdlDefinition wsdl, String packageName, String outputDirectory) {
        logger.info("Generating bindings for WSDL: {} with package: {}", wsdl.getName(), packageName);

        Path tempDir = null;
        Path outputPath = null;

        try {
            // Create temporary directory for WSDL file
            tempDir = Files.createTempDirectory(TEMP_DIR_PREFIX);
            Path wsdlFile = tempDir.resolve(WSDL_FILE_NAME);
            Files.write(wsdlFile, wsdl.getContent().getBytes());

            // Create output directory
            outputPath = Paths.get(outputDirectory);
            Files.createDirectories(outputPath);

            // Build wsimport command
            List<String> command = buildWsimportCommand(wsdlFile.toString(), packageName, outputPath.toString());

            // Execute wsimport
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);

            logger.debug("Executing wsimport command: {}", String.join(" ", command));

            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            if(exitCode != 0) {
                String error = readProcessOutput(process);
                logger.error("wsimport failed with exit code {}: {}", exitCode, error);
                throw new RuntimeException("wsimport failed: " + error);
            }

            // Collect generated files
            Map<String, String> generatedClasses = collectGeneratedClasses(outputPath, packageName);

            return GeneratedBinding.builder()
                    .bindingId(UUID.randomUUID().toString())
                    .wsdlId(wsdl.getWsdlId())
                    .packageName(packageName)
                    .outputDirectory(outputDirectory)
                    .generatedClassesContent(generatedClasses)
                    .generationTime(LocalDateTime.now())
                    .status(GenerationStatus.SUCCESS)
                    .build();

        } catch(Exception e) {
            logger.error("Failed to generate bindings: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate bindings", e);
        } finally {
            // Cleanup temporary directory
            if(tempDir != null) {
                try {
                    deleteDirectory(tempDir);
                } catch(IOException e) {
                    logger.warn("Failed to delete temporary directory: {}", e.getMessage());
                }
            }
        }
    }

    private List<String> buildWsimportCommand(String wsdlFile, String packageName, String outputDirectory) {
        List<String> command = new ArrayList<>();

        // Use wsimport command
        command.add("wsimport");

        // Keep generated source files
        command.add(" - keep");

        // Generate source files only(no compilation)
        command.add(" - s");
        command.add(outputDirectory);

        // Target package
        command.add(" - p");
        command.add(packageName);

        // Generate JAX - WS 2.2 compatible code
        command.add(" - target");
        command.add("2.2");

        // WSDL file
        command.add(wsdlFile);

        return command;
    }

    private String readProcessOutput(Process process) throws IOException {
        Scanner scanner = new Scanner(process.getInputStream());
        StringBuilder output = new StringBuilder();
        while(scanner.hasNextLine()) {
            output.append(scanner.nextLine()).append("\n");
        }
        scanner.close();
        return output.toString();
    }

    private Map<String, String> collectGeneratedClasses(Path outputPath, String packageName) throws IOException {
        Map<String, String> classes = new HashMap<>();

        String packagePath = packageName.replace('.', '/');
        Path packageDir = outputPath.resolve(packagePath);

        if(!Files.exists(packageDir)) {
            logger.warn("Package directory does not exist: {}", packageDir);
            return classes;
        }

        Files.walk(packageDir)
                .filter(path -> path.toString().endsWith(".java"))
                .forEach(path -> {
                    String fileName = path.getFileName().toString();
                    String className = fileName.substring(0, fileName.length() - 5); // Remove .java
                    String fullClassName = packageName + "." + className;

                    try {
                        String content = Files.readString(path);
                        classes.put(fullClassName, content);
                        logger.debug("Found generated class: {}", fullClassName);
                    } catch(IOException e) {
                        logger.error("Failed to read generated file {}: {}", path, e.getMessage());
                    }
                });

        logger.info("Collected {} generated classes", classes.size());
        return classes;
    }

    private void deleteDirectory(Path directory) throws IOException {
        Files.walk(directory)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }
}
