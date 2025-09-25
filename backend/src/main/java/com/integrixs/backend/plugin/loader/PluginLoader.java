package com.integrixs.backend.plugin.loader;

import com.integrixs.backend.plugin.api.AdapterPlugin;
import com.integrixs.backend.plugin.registry.PluginDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Loads adapter plugins from JAR files
 */
@Component
public class PluginLoader {

    private static final Logger logger = LoggerFactory.getLogger(PluginLoader.class);
    private static final String PLUGIN_DESCRIPTOR_FILE = "META-INF/integrix-plugin.properties";
    private static final String PLUGIN_CLASS_PROPERTY = "plugin.class";

    @Value("${integrix.plugins.directory:plugins}")
    private String pluginsDirectory;

    @Value("${integrix.plugins.scan-classpath:true}")
    private boolean scanClasspath;

    private final Map<String, PluginClassLoader> classLoaders = new HashMap<>();

    @PostConstruct
    public void init() {
        // Create plugins directory if it doesn't exist
        try {
            Files.createDirectories(Paths.get(pluginsDirectory));
            logger.info("Plugin directory: {}", Paths.get(pluginsDirectory).toAbsolutePath());
        } catch(IOException e) {
            logger.error("Failed to create plugins directory", e);
        }
    }

    /**
     * Scan for available plugins
     */
    public List<PluginDescriptor> scanForPlugins() {
        List<PluginDescriptor> descriptors = new ArrayList<>();

        // Scan plugin directory
        if(pluginsDirectory != null) {
            descriptors.addAll(scanDirectory(pluginsDirectory));
        }

        // Scan classpath
        if(scanClasspath) {
            descriptors.addAll(scanClasspath());
        }

        logger.info("Found {} plugin(s)", descriptors.size());
        return descriptors;
    }

    /**
     * Load a plugin class
     */
    public Class<? extends AdapterPlugin> loadPlugin(PluginDescriptor descriptor)
            throws ClassNotFoundException {

        if(descriptor.getJarPath() != null) {
            // Load from JAR file
            return loadFromJar(descriptor);
        } else {
            // Load from classpath
            return loadFromClasspath(descriptor.getPluginClass());
        }
    }

    /**
     * Load plugin from a JAR file
     */
    public PluginDescriptor loadPluginFromJar(Path jarPath) throws IOException {
        if(!Files.exists(jarPath) || !jarPath.toString().endsWith(".jar")) {
            throw new IllegalArgumentException("Invalid JAR file: " + jarPath);
        }

        try(JarFile jarFile = new JarFile(jarPath.toFile())) {
            // Look for plugin descriptor
            JarEntry descriptorEntry = jarFile.getJarEntry(PLUGIN_DESCRIPTOR_FILE);
            if(descriptorEntry == null) {
                throw new PluginLoadException(
                    "No plugin descriptor found in JAR: " + jarPath
               );
            }

            // Load descriptor properties
            Properties props = new Properties();
            props.load(jarFile.getInputStream(descriptorEntry));

            String pluginClass = props.getProperty(PLUGIN_CLASS_PROPERTY);
            if(pluginClass == null || pluginClass.trim().isEmpty()) {
                throw new PluginLoadException(
                    "No plugin class specified in descriptor"
               );
            }

            return PluginDescriptor.builder()
                    .pluginId(props.getProperty("plugin.id", UUID.randomUUID().toString()))
                    .pluginClass(pluginClass)
                    .jarPath(jarPath.toString())
                    .properties(props)
                    .build();
        }
    }

    private List<PluginDescriptor> scanDirectory(String directory) {
        List<PluginDescriptor> descriptors = new ArrayList<>();
        Path pluginPath = Paths.get(directory);

        if(!Files.exists(pluginPath)) {
            logger.warn("Plugin directory does not exist: {}", pluginPath);
            return descriptors;
        }

        try(Stream<Path> paths = Files.walk(pluginPath, 1)) {
            List<Path> jarFiles = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".jar"))
                    .collect(Collectors.toList());

            for(Path jarFile : jarFiles) {
                try {
                    PluginDescriptor descriptor = loadPluginFromJar(jarFile);
                    descriptors.add(descriptor);
                    logger.info("Found plugin in JAR: {} ( {})",
                        descriptor.getPluginId(), jarFile.getFileName());
                } catch(Exception e) {
                    logger.error("Failed to load plugin from JAR: " + jarFile, e);
                }
            }
        } catch(IOException e) {
            logger.error("Error scanning plugin directory", e);
        }

        return descriptors;
    }

    private List<PluginDescriptor> scanClasspath() {
        // This would scan for plugins in the classpath
        // Implementation depends on how built-in plugins are packaged
        return new ArrayList<>();
    }

    @SuppressWarnings("unchecked")
    private Class<? extends AdapterPlugin> loadFromJar(PluginDescriptor descriptor)
            throws ClassNotFoundException {

        String jarPath = descriptor.getJarPath();
        PluginClassLoader classLoader = classLoaders.get(jarPath);

        if(classLoader == null) {
            try {
                URL jarUrl = new File(jarPath).toURI().toURL();
                classLoader = new PluginClassLoader(
                    new URL[] {jarUrl},
                    getClass().getClassLoader()
               );
                classLoaders.put(jarPath, classLoader);
            } catch(Exception e) {
                throw new PluginLoadException(
                    "Failed to create class loader for JAR: " + jarPath, e
               );
            }
        }

        Class<?> clazz = classLoader.loadClass(descriptor.getPluginClass());

        if(!AdapterPlugin.class.isAssignableFrom(clazz)) {
            throw new ClassCastException(
                "Plugin class does not implement AdapterPlugin: " + clazz.getName()
           );
        }

        return(Class<? extends AdapterPlugin>) clazz;
    }

    @SuppressWarnings("unchecked")
    private Class<? extends AdapterPlugin> loadFromClasspath(String className)
            throws ClassNotFoundException {

        Class<?> clazz = Class.forName(className);

        if(!AdapterPlugin.class.isAssignableFrom(clazz)) {
            throw new ClassCastException(
                "Plugin class does not implement AdapterPlugin: " + className
           );
        }

        return(Class<? extends AdapterPlugin>) clazz;
    }

    /**
     * Unload a plugin
     */
    public void unloadPlugin(String jarPath) {
        PluginClassLoader classLoader = classLoaders.remove(jarPath);
        if(classLoader != null) {
            try {
                classLoader.close();
            } catch(IOException e) {
                logger.error("Error closing class loader for: " + jarPath, e);
            }
        }
    }

    /**
     * Custom class loader for plugin isolation
     */
    private static class PluginClassLoader extends URLClassLoader {
        public PluginClassLoader(URL[] urls, ClassLoader parent) {
            super(urls, parent);
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve)
                throws ClassNotFoundException {
            // First try to load from plugin JAR
            try {
                return findClass(name);
            } catch(ClassNotFoundException e) {
                // Fall back to parent class loader
                return super.loadClass(name, resolve);
            }
        }
    }
}
