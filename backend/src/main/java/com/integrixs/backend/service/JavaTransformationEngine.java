package com.integrixs.backend.service;

import com.integrixs.backend.exception.BusinessException;
import com.integrixs.data.model.TransformationCustomFunction;
import com.integrixs.data.sql.repository.TransformationCustomFunctionSqlRepository;
import org.springframework.stereotype.Service;

import javax.tools.*;
import java.io.*;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for executing Java transformation functions
 * Replaces the JavaScript - based execution with proper Java compilation and execution
 */
@Service
public class JavaTransformationEngine {

    private static final Logger log = LoggerFactory.getLogger(JavaTransformationEngine.class);


    private final TransformationCustomFunctionSqlRepository functionRepository;
    private final JavaCompilationService compilationService;

    // Cache for compiled classes
    private final Map<String, Class<?>> compiledFunctionCache = new ConcurrentHashMap<>();

    public JavaTransformationEngine(TransformationCustomFunctionSqlRepository functionRepository,
                                   JavaCompilationService compilationService) {
        this.functionRepository = functionRepository;
        this.compilationService = compilationService;
    }
    private final Map<String, Long> functionVersionCache = new ConcurrentHashMap<>();

    /**
     * Execute a Java transformation function by name
     * @param functionName The name of the function to execute
     * @param parameters The parameters to pass to the function
     * @return The result of the function execution
     */
    public Object executeFunction(String functionName, Object[] parameters) {
        try {
            // Get function from database
            TransformationCustomFunction function = functionRepository.findByName(functionName)
                    .orElseThrow(() -> new BusinessException("Function not found: " + functionName));

            // Check if function is Java
            if(function.getLanguage() != TransformationCustomFunction.FunctionLanguage.JAVA) {
                throw new BusinessException("Function is not a Java function: " + functionName);
            }

            // Get or compile the function class
            Class<?> functionClass = getOrCompileFunctionClass(function);

            // Find the transform method
            Method transformMethod = findTransformMethod(functionClass);

            // Create instance and execute
            Object instance = functionClass.getDeclaredConstructor().newInstance();
            return transformMethod.invoke(instance, parameters);

        } catch(Exception e) {
            log.error("Error executing Java function: {}", functionName, e);
            throw new BusinessException("Failed to execute function: " + functionName + " - " + e.getMessage());
        }
    }

    /**
     * Get or compile function class with caching
     */
    private Class<?> getOrCompileFunctionClass(TransformationCustomFunction function) throws Exception {
        String functionName = function.getName();
        long currentVersion = function.getVersion();

        // Check cache
        Long cachedVersion = functionVersionCache.get(functionName);
        if(cachedVersion != null && cachedVersion == currentVersion && compiledFunctionCache.containsKey(functionName)) {
            return compiledFunctionCache.get(functionName);
        }

        // Compile the function
        Class<?> compiledClass = compileAndLoadFunction(function);

        // Update cache
        compiledFunctionCache.put(functionName, compiledClass);
        functionVersionCache.put(functionName, currentVersion);

        return compiledClass;
    }

    /**
     * Compile and load a function
     */
    private Class<?> compileAndLoadFunction(TransformationCustomFunction function) throws Exception {
        String className = "TransformFunction_" + function.getName();
        String fullClassName = "com.integrixs.generated." + className;

        // Create the full Java source code
        String sourceCode = createFunctionSource(fullClassName, function);

        // Create temp directory for compilation
        Path tempDir = Files.createTempDirectory("java_functions");

        try {
            // Write source file
            Path sourceFile = tempDir.resolve(className + ".java");
            Files.write(sourceFile, sourceCode.getBytes());

            // Compile
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            if(compiler == null) {
                throw new BusinessException("Java compiler not available. Ensure JDK is installed.");
            }

            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
            StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);

            Iterable<? extends JavaFileObject> compilationUnits =
                    fileManager.getJavaFileObjectsFromFiles(Arrays.asList(sourceFile.toFile()));

            JavaCompiler.CompilationTask task = compiler.getTask(
                    null, fileManager, diagnostics,
                    Arrays.asList(" - d", tempDir.toString()),
                    null, compilationUnits);

            boolean success = task.call();
            fileManager.close();

            if(!success) {
                StringBuilder errors = new StringBuilder();
                for(Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                    errors.append(diagnostic.getMessage(null)).append("\n");
                }
                throw new BusinessException("Compilation failed: " + errors.toString());
            }

            // Load the compiled class
            URLClassLoader classLoader = new URLClassLoader(
                    new URL[] {tempDir.toUri().toURL()},
                    this.getClass().getClassLoader());

            return classLoader.loadClass(fullClassName);

        } finally {
            // Clean up temp files
            deleteDirectory(tempDir.toFile());
        }
    }

    /**
     * Create the full Java source code for a function
     */
    private String createFunctionSource(String fullClassName, TransformationCustomFunction function) {
        String packageName = fullClassName.substring(0, fullClassName.lastIndexOf('.'));
        String className = fullClassName.substring(fullClassName.lastIndexOf('.') + 1);

        StringBuilder source = new StringBuilder();
        source.append("package ").append(packageName).append(";\n\n");

        // Add imports
        source.append("import java.util.*;\n");
        source.append("import java.text.*;\n");
        source.append("import java.time.*;\n");
        source.append("import java.time.format.*;\n");
        source.append("import java.math.*;\n\n");

        // Add class declaration
        source.append("public class ").append(className).append(" {\n");

        // Add the function body
        String functionBody = function.getFunctionBody();

        // If the function body doesn't contain a method declaration, wrap it
        if(!functionBody.contains("public Object transform")) {
            source.append("    public Object transform(Object... args) {\n");

            // Parse parameters and create variable assignments
            List<Map<String, Object>> params = parseParameters(function.getParameters());
            for(int i = 0; i < params.size(); i++) {
                Map<String, Object> param = params.get(i);
                String paramName = (String) param.get("name");
                String paramType = (String) param.get("type");

                source.append("        ");
                source.append(getJavaType(paramType)).append(" ").append(paramName);
                source.append(" = args.length > ").append(i).append(" ? ");
                source.append(getCastExpression(paramType, "args[" + i + "]"));
                source.append(" : ").append(getDefaultValue(paramType)).append(";\n");
            }

            source.append("        ").append(functionBody).append("\n");
            source.append("    }\n");
        } else {
            // Function body contains method declaration, use as is
            source.append(functionBody);
        }

        source.append("}\n");

        return source.toString();
    }

    /**
     * Find the transform method in the compiled class
     */
    private Method findTransformMethod(Class<?> functionClass) throws NoSuchMethodException {
        // Try to find method with varargs
        try {
            return functionClass.getMethod("transform", Object[].class);
        } catch(NoSuchMethodException e) {
            // Try to find method with specific parameter types
            for(Method method : functionClass.getMethods()) {
                if("transform".equals(method.getName())) {
                    return method;
                }
            }
            throw new NoSuchMethodException("No transform method found in class");
        }
    }

    /**
     * Parse parameters JSON
     */
    private List<Map<String, Object>> parseParameters(String parametersJson) {
        try {
            if(parametersJson == null || parametersJson.trim().isEmpty()) {
                return Collections.emptyList();
            }
            return new com.fasterxml.jackson.databind.ObjectMapper()
                    .readValue(parametersJson, List.class);
        } catch(Exception e) {
            log.warn("Failed to parse parameters JSON: {}", parametersJson, e);
            return Collections.emptyList();
        }
    }

    /**
     * Get Java type for parameter type
     */
    private String getJavaType(String type) {
        switch(type) {
            case "string": return "String";
            case "number": return "Double";
            case "integer": return "Integer";
            case "boolean": return "Boolean";
            case "array": return "List<Object>";
            case "object": return "Map<String, Object>";
            default: return "Object";
        }
    }

    /**
     * Get cast expression for parameter type
     */
    private String getCastExpression(String type, String expression) {
        switch(type) {
            case "string": return "(String) " + expression;
            case "number": return "((Number) " + expression + ").doubleValue()";
            case "integer": return "((Number) " + expression + ").intValue()";
            case "boolean": return "(Boolean) " + expression;
            case "array": return "(List<Object>) " + expression;
            case "object": return "(Map<String, Object>) " + expression;
            default: return expression;
        }
    }

    /**
     * Get default value for parameter type
     */
    private String getDefaultValue(String type) {
        switch(type) {
            case "string": return "\"\"";
            case "number": return "0.0";
            case "integer": return "0";
            case "boolean": return "false";
            case "array": return "new ArrayList<>()";
            case "object": return "new HashMap<>()";
            default: return "null";
        }
    }

    /**
     * Delete directory recursively
     */
    private void deleteDirectory(File dir) {
        File[] files = dir.listFiles();
        if(files != null) {
            for(File file : files) {
                if(file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        dir.delete();
    }

    /**
     * Clear the function cache
     */
    public void clearCache() {
        compiledFunctionCache.clear();
        functionVersionCache.clear();
        log.info("Java transformation engine cache cleared");
    }
}
