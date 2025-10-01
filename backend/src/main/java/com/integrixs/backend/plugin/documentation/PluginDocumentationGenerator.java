package com.integrixs.backend.plugin.documentation;

import com.integrixs.backend.plugin.api.*;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates documentation for plugins
 */
public class PluginDocumentationGenerator {

    private static final Logger log = LoggerFactory.getLogger(PluginDocumentationGenerator.class);


    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy - MM - dd HH:mm:ss");

    /**
     * Generate complete documentation for a plugin
     */
    public String generateDocumentation(AdapterPlugin plugin) {
        StringWriter writer = new StringWriter();
        PrintWriter pw = new PrintWriter(writer);

        try {
            AdapterMetadata metadata = plugin.getMetadata();
            ConfigurationSchema schema = plugin.getConfigurationSchema();

            // Header
            pw.println("# " + metadata.getName());
            pw.println();

            // Metadata section
            generateMetadataSection(pw, metadata);

            // Capabilities section
            if(metadata.getCapabilities() != null && !metadata.getCapabilities().isEmpty()) {
                generateCapabilitiesSection(pw, metadata.getCapabilities());
            }

            // Configuration section
            if(schema != null) {
                generateConfigurationSection(pw, schema);
            }

            // Technical details
            generateTechnicalDetailsSection(pw, metadata);

            // Usage examples
            generateUsageExamplesSection(pw, metadata, schema);

            // Troubleshooting
            generateTroubleshootingSection(pw, metadata);

            // Footer
            generateFooter(pw);

            return writer.toString();

        } catch(Exception e) {
            log.error("Failed to generate documentation", e);
            return "# Documentation Generation Error\n\nFailed to generate documentation: " + e.getMessage();
        }
    }

    /**
     * Generate Markdown documentation for a plugin
     */
    public String generateMarkdown(AdapterPlugin plugin) {
        return generateDocumentation(plugin);
    }

    /**
     * Generate HTML documentation for a plugin
     */
    public String generateHTML(AdapterPlugin plugin) {
        String markdown = generateDocumentation(plugin);
        // Convert markdown to HTML(simplified version)
        return convertMarkdownToHTML(markdown);
    }

    /**
     * Generate API documentation in OpenAPI format
     */
    public String generateOpenAPISpec(AdapterPlugin plugin) {
        AdapterMetadata metadata = plugin.getMetadata();
        ConfigurationSchema schema = plugin.getConfigurationSchema();

        Map<String, Object> openapi = new LinkedHashMap<>();
        openapi.put("openapi", "3.0.3");

        Map<String, Object> info = new LinkedHashMap<>();
        info.put("title", metadata.getName() + " API");
        info.put("description", metadata.getDescription());
        info.put("version", metadata.getVersion());
        info.put("x - vendor", metadata.getVendor());
        if(metadata.getLicense() != null) {
            Map<String, String> license = new LinkedHashMap<>();
            license.put("name", metadata.getLicense());
            info.put("license", license);
        }
        openapi.put("info", info);

        // Add paths for plugin operations
        Map<String, Object> paths = new LinkedHashMap<>();

        // Health endpoint
        Map<String, Object> healthPath = new LinkedHashMap<>();
        Map<String, Object> healthGet = new LinkedHashMap<>();
        healthGet.put("summary", "Check plugin health");
        healthGet.put("operationId", "getHealth");
        healthGet.put("responses", Map.of(
            "200", Map.of(
                "description", "Health status",
                "content", Map.of(
                    "application/json", Map.of(
                        "schema", Map.of(
                            "type", "object",
                            "properties", Map.of(
                                "state", Map.of("type", "string", "enum", List.of("HEALTHY", "DEGRADED", "UNHEALTHY")),
                                "message", Map.of("type", "string")
                           )
                       )
                   )
               )
           )
       ));
        healthPath.put("get", healthGet);
        paths.put("/health", healthPath);

        // Test connection endpoint
        Map<String, Object> testPath = new LinkedHashMap<>();
        Map<String, Object> testPost = new LinkedHashMap<>();
        testPost.put("summary", "Test connection");
        testPost.put("operationId", "testConnection");
        testPost.put("parameters", List.of(
            Map.of(
                "name", "direction",
                "in", "query",
                "required", true,
                "schema", Map.of(
                    "type", "string",
                    "enum", List.of("INBOUND", "OUTBOUND")
               )
           )
       ));
        testPost.put("responses", Map.of(
            "200", Map.of(
                "description", "Connection test result",
                "content", Map.of(
                    "application/json", Map.of(
                        "schema", Map.of(
                            "type", "object",
                            "properties", Map.of(
                                "successful", Map.of("type", "boolean"),
                                "message", Map.of("type", "string")
                           )
                       )
                   )
               )
           )
       ));
        testPath.put("post", testPost);
        paths.put("/test", testPath);

        openapi.put("paths", paths);

        // Add components for configuration schema
        if(schema != null) {
            Map<String, Object> components = new LinkedHashMap<>();
            Map<String, Object> schemas = new LinkedHashMap<>();

            Map<String, Object> configSchema = new LinkedHashMap<>();
            configSchema.put("type", "object");
            Map<String, Object> properties = new LinkedHashMap<>();

            for(ConfigurationSchema.Section section : schema.getSections()) {
                for(ConfigurationSchema.Field field : section.getFields()) {
                    Map<String, Object> fieldSchema = new LinkedHashMap<>();
                    fieldSchema.put("type", mapFieldType(field.getType()));
                    fieldSchema.put("description", field.getHelp());
                    if(field.getDefaultValue() != null) {
                        fieldSchema.put("default", field.getDefaultValue());
                    }
                    properties.put(field.getName(), fieldSchema);
                }
            }

            configSchema.put("properties", properties);
            schemas.put("Configuration", configSchema);
            components.put("schemas", schemas);
            openapi.put("components", components);
        }

        // Convert to JSON string
        return toJson(openapi);
    }

    private void generateMetadataSection(PrintWriter pw, AdapterMetadata metadata) {
        pw.println("## Overview");
        pw.println();
        pw.println(metadata.getDescription() != null ? metadata.getDescription() : "No description available.");
        pw.println();

        pw.println("**Version:** " + metadata.getVersion());
        pw.println("**Vendor:** " + metadata.getVendor());
        pw.println("**Category:** " + metadata.getCategory());
        if(metadata.getLicense() != null) {
            pw.println("**License:** " + metadata.getLicense());
        }
        pw.println();
    }

    private void generateCapabilitiesSection(PrintWriter pw, Map<String, Boolean> capabilities) {
        pw.println("## Capabilities");
        pw.println();

        pw.println("| Capability | Supported |");
        pw.println("|------------|-----------|");

        capabilities.forEach((key, value) -> {
            String displayName = key.replace("_", " ").substring(0, 1).toUpperCase() +
                               key.replace("_", " ").substring(1);
            String supported = Boolean.TRUE.equals(value) ? "✓" :
                             Boolean.FALSE.equals(value) ? "✗" : String.valueOf(value);
            pw.println("| " + displayName + " | " + supported + " |");
        });
        pw.println();
    }

    private void generateConfigurationSection(PrintWriter pw, ConfigurationSchema schema) {
        pw.println("## Configuration");
        pw.println();

        for(ConfigurationSchema.Section section : schema.getSections()) {
            pw.println("### " + section.getTitle());
            if(section.getDescription() != null) {
                pw.println();
                pw.println(section.getDescription());
            }
            pw.println();

            if(!section.getFields().isEmpty()) {
                pw.println("| Field | Type | Required | Description |");
                pw.println("|-------|------|----------|-------------|");

                for(ConfigurationSchema.Field field : section.getFields()) {
                    String required = field.isRequired() ? "Yes" : "No";
                    String description = field.getHelp() != null ? field.getHelp() : field.getLabel();
                    pw.println("| " + field.getLabel() + " | " + field.getType() +
                             " | " + required + " | " + description + " |");
                }
                pw.println();
            }
        }
    }

    private void generateTechnicalDetailsSection(PrintWriter pw, AdapterMetadata metadata) {
        pw.println("## Technical Details");
        pw.println();

        if(metadata.getSupportedProtocols() != null && !metadata.getSupportedProtocols().isEmpty()) {
            pw.println("### Supported Protocols");
            metadata.getSupportedProtocols().forEach(protocol -> pw.println("- " + protocol));
            pw.println();
        }

        if(metadata.getSupportedFormats() != null && !metadata.getSupportedFormats().isEmpty()) {
            pw.println("### Supported Formats");
            metadata.getSupportedFormats().forEach(format -> pw.println("- " + format));
            pw.println();
        }

        if(metadata.getAuthenticationMethods() != null && !metadata.getAuthenticationMethods().isEmpty()) {
            pw.println("### Authentication Methods");
            metadata.getAuthenticationMethods().forEach(method -> pw.println("- " + method));
            pw.println();
        }

        if(metadata.getMinPlatformVersion() != null) {
            pw.println("### Platform Requirements");
            pw.println("- Minimum Platform Version: " + metadata.getMinPlatformVersion());
            pw.println();
        }
    }

    private void generateUsageExamplesSection(PrintWriter pw, AdapterMetadata metadata,
                                            ConfigurationSchema schema) {
        pw.println("## Usage Examples");
        pw.println();

        pw.println("### Basic Configuration");
        pw.println();
        pw.println("```json");
        pw.println(" {");

        if(schema != null) {
            List<String> configLines = new ArrayList<>();
            for(ConfigurationSchema.Section section : schema.getSections()) {
                for(ConfigurationSchema.Field field : section.getFields()) {
                    if(field.isRequired()) {
                        String value = field.getDefaultValue() != null ?
                            toJson(field.getDefaultValue()) :
                            getExampleValue(field);
                        configLines.add(" \"" + field.getName() + "\": " + value);
                    }
                }
            }
            pw.println(String.join(",\n", configLines));
        }

        pw.println("}");
        pw.println("```");
        pw.println();

        // Add protocol - specific examples
        if(metadata.getSupportedProtocols() != null &&
            metadata.getSupportedProtocols().contains("REST")) {
            pw.println("### REST API Example");
            pw.println();
            pw.println("```bash");
            pw.println("# Test connection");
            pw.println("curl -X POST http://localhost:8080/api/plugins/" + metadata.getId() +
                     "/test - connection?direction = OUTBOUND");
            pw.println();
            pw.println("# Send message");
            pw.println("curl -X POST http://localhost:8080/api/plugins/" + metadata.getId() +
                     "/send \\");
            pw.println(" -H \"Content - Type: application/json\" \\");
            pw.println(" -d ' {\"data\": \"example\"}'");
            pw.println("```");
            pw.println();
        }
    }

    private void generateTroubleshootingSection(PrintWriter pw, AdapterMetadata metadata) {
        pw.println("## Troubleshooting");
        pw.println();

        pw.println("### Common Issues");
        pw.println();

        pw.println("1. **Connection Failed**");
        pw.println("   - Verify the endpoint URL is correct and accessible");
        pw.println("   - Check authentication credentials");
        pw.println("   - Ensure firewall rules allow the connection");
        pw.println();

        pw.println("2. **Configuration Errors**");
        pw.println("   - Verify all required fields are provided");
        pw.println("   - Check field types match expected values");
        pw.println("   - Review validation rules for each field");
        pw.println();

        pw.println("3. **Performance Issues**");
        pw.println("   - Monitor message processing rates");
        pw.println("   - Check system resources(CPU, memory)");
        pw.println("   - Review batch size configuration if applicable");
        pw.println();

        pw.println("### Debug Mode");
        pw.println();
        pw.println("Enable debug logging by setting the log level:");
        pw.println("```");
        pw.println("logging.level.com.integrixs.backend.plugin = " + metadata.getId() + " = DEBUG");
        pw.println("```");
        pw.println();
    }

    private void generateFooter(PrintWriter pw) {
        pw.println("---");
        pw.println();
        pw.println("*Documentation generated on " +
                 LocalDateTime.now().format(DATE_FORMAT) + "*");
    }

    private String getExampleValue(ConfigurationSchema.Field field) {
        switch(field.getType()) {
            case "text":
            case "password":
                return field.getPlaceholder() != null ?
                    "\"" + field.getPlaceholder() + "\"" : "\"example\"";
            case "number":
                return field.getDefaultValue() != null ?
                    String.valueOf(field.getDefaultValue()) : "0";
            case "boolean":
            case "checkbox":
                return "false";
            case "select":
            case "radio":
                return field.getOptions() != null && !field.getOptions().isEmpty() ?
                    "\"" + field.getOptions().get(0).getValue() + "\"" : "\"option1\"";
            case "multiselect":
                return "[]";
            case "json":
                return " {}";
            default:
                return "\"\"";
        }
    }

    private String mapFieldType(String fieldType) {
        switch(fieldType) {
            case "number":
                return "number";
            case "boolean":
            case "checkbox":
                return "boolean";
            case "multiselect":
                return "array";
            case "json":
                return "object";
            default:
                return "string";
        }
    }

    private String convertMarkdownToHTML(String markdown) {
        // Simplified markdown to HTML conversion
        String html = markdown
            .replaceAll("^# (. + )$", "<h1>$1</h1>")
            .replaceAll("^## (. + )$", "<h2>$1</h2>")
            .replaceAll("^### (. + )$", "<h3>$1</h3>")
            .replaceAll("\\*\\*(. + ?)\\*\\*", "<strong>$1</strong>")
            .replaceAll("\\*(. + ?)\\*", "<em>$1</em>")
            .replaceAll("^- (. + )$", "<li>$1</li>")
            .replaceAll("```([^`] + )```", "<pre><code>$1</code></pre>")
            .replaceAll("`([^`] + )`", "<code>$1</code>")
            .replaceAll("\\n\\n", "</p><p>")
            .replaceAll("\\|(. + )\\|", "<table>$1</table>");

        return "<!DOCTYPE html>\n<html>\n<head>\n" +
               "<meta charset = \"UTF-8\">\n" +
               "<style>\n" +
               "body { font - family: Arial, sans - serif; margin: 40px; }\n" +
               "h1, h2, h3 { color: #333; }\n" +
               "code { background: #f4f4f4; padding: 2px 4px; }\n" +
               "pre { background: #f4f4f4; padding: 10px; overflow - x: auto; }\n" +
               "table { border - collapse: collapse; width: 100%; }\n" +
               "th, td { border: 1px solid #ddd; padding: 8px; text - align: left; }\n" +
               "th { background: #f2f2f2; }\n" +
               "</style>\n" +
               "</head>\n<body>\n" +
               "<p>" + html + "</p>\n" +
               "</body>\n</html>";
    }

    private String toJson(Object obj) {
        // Simple JSON serialization(in production, use Jackson or Gson)
        if(obj == null) return "null";
        if(obj instanceof String) return "\"" + obj + "\"";
        if(obj instanceof Number || obj instanceof Boolean) return obj.toString();
        if(obj instanceof List) {
            List<?> list = (List<?>) obj;
            return "[" + list.stream()
                .map(this::toJson)
                .collect(Collectors.joining(", ")) + "]";
        }
        if(obj instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) obj;
            return " {" + map.entrySet().stream()
                .map(e -> "\"" + e.getKey() + "\": " + toJson(e.getValue()))
                .collect(Collectors.joining(", ")) + "}";
        }
        return "\"" + obj.toString() + "\"";
    }
}
