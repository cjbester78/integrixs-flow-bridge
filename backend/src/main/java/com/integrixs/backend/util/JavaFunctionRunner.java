package com.integrixs.backend.util;

import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Utility class for dynamically compiling and executing Java functions
 * Used for custom transformation functions in integration flows
 */
@Component
public class JavaFunctionRunner {

    private static final Logger log = LoggerFactory.getLogger(JavaFunctionRunner.class);

    private final Map<String, Class<?>> compiledClasses = new ConcurrentHashMap<>();
    private final Path tempDir;

    public JavaFunctionRunner() throws IOException {
        this.tempDir = Files.createTempDirectory("java-functions");
        this.tempDir.toFile().deleteOnExit();
    }

    /**
     * Execute a Java function with the given code and parameters
     *
     * @param functionName The name of the function
     * @param code The Java code containing the function
     * @param methodName The method name to invoke
     * @param parameters The parameters to pass to the method
     * @return The result of the function execution
     */
    public Object execute(String functionName, String code, String methodName, Object... parameters) {
        try {
            Class<?> compiledClass = compiledClasses.computeIfAbsent(functionName,
                name -> compileFunction(name, code));

            Method method = findMethod(compiledClass, methodName, parameters);
            if (method == null) {
                throw new IllegalArgumentException("Method " + methodName + " not found in " + functionName);
            }

            Object instance = compiledClass.getDeclaredConstructor().newInstance();
            return method.invoke(instance, parameters);

        } catch (Exception e) {
            log.error("Error executing function: " + functionName, e);
            throw new RuntimeException("Failed to execute function: " + functionName, e);
        }
    }

    /**
     * Compile Java code into a class
     */
    private Class<?> compileFunction(String functionName, String code) {
        try {
            // Create source file
            File sourceFile = new File(tempDir.toFile(), functionName + ".java");
            try (FileWriter writer = new FileWriter(sourceFile)) {
                writer.write(code);
            }

            // Compile the source file
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            if (compiler == null) {
                throw new IllegalStateException("No Java compiler available. Make sure you're running on a JDK, not a JRE.");
            }

            StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
            fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Arrays.asList(tempDir.toFile()));

            boolean success = compiler.getTask(null, fileManager, null, null, null,
                fileManager.getJavaFileObjects(sourceFile)).call();

            if (!success) {
                throw new RuntimeException("Compilation failed for function: " + functionName);
            }

            // Load the compiled class
            URLClassLoader classLoader = new URLClassLoader(new URL[]{tempDir.toUri().toURL()});
            return classLoader.loadClass(functionName);

        } catch (Exception e) {
            log.error("Error compiling function: " + functionName, e);
            throw new RuntimeException("Failed to compile function: " + functionName, e);
        }
    }

    /**
     * Find a method that matches the given name and parameter types
     */
    private Method findMethod(Class<?> clazz, String methodName, Object... parameters) {
        Method[] methods = clazz.getDeclaredMethods();

        for (Method method : methods) {
            if (!method.getName().equals(methodName)) {
                continue;
            }

            Class<?>[] paramTypes = method.getParameterTypes();
            if (paramTypes.length != parameters.length) {
                continue;
            }

            boolean matches = true;
            for (int i = 0; i < paramTypes.length; i++) {
                if (parameters[i] != null && !paramTypes[i].isAssignableFrom(parameters[i].getClass())) {
                    matches = false;
                    break;
                }
            }

            if (matches) {
                return method;
            }
        }

        return null;
    }

    /**
     * Clear cached compiled classes
     */
    public void clearCache() {
        compiledClasses.clear();
    }

    /**
     * Validate Java code syntax
     */
    public boolean validateCode(String functionName, String code) {
        try {
            compileFunction(functionName + "_validation", code);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}