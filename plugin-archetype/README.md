# Integrix Plugin Archetype

Maven archetype for quickly creating new Integrix Flow Bridge plugins.

## Installation

First, install the archetype to your local Maven repository:

```bash
cd plugin-archetype
mvn clean install
```

## Usage

### Interactive Mode

Run the following command and follow the prompts:

```bash
mvn archetype:generate \
  -DarchetypeGroupId=com.integrixs \
  -DarchetypeArtifactId=integrix-plugin-archetype \
  -DarchetypeVersion=1.0.0
```

### Non-Interactive Mode

Specify all properties on the command line:

```bash
mvn archetype:generate \
  -DarchetypeGroupId=com.integrixs \
  -DarchetypeArtifactId=integrix-plugin-archetype \
  -DarchetypeVersion=1.0.0 \
  -DgroupId=com.example \
  -DartifactId=salesforce-plugin \
  -Dversion=1.0.0 \
  -Dpackage=com.example.salesforce \
  -DpluginId=salesforce-adapter \
  -DpluginName="Salesforce Adapter" \
  -DpluginClass=SalesforcePlugin \
  -DpluginDescription="Integrix adapter for Salesforce CRM" \
  -DpluginVendor="Example Corp" \
  -DpluginCategory=crm \
  -DpluginIcon=salesforce \
  -DpluginProtocol=REST \
  -DpluginAuth=OAuth2 \
  -DpluginDocUrl=https://example.com/salesforce-docs \
  -DpluginLicense=Apache-2.0 \
  -DpluginTag=salesforce \
  -DinteractiveMode=false
```

## Archetype Properties

| Property | Description | Default | Example |
|----------|-------------|---------|---------|
| `groupId` | Maven group ID | - | `com.example` |
| `artifactId` | Maven artifact ID | - | `salesforce-plugin` |
| `version` | Plugin version | `1.0.0` | `1.0.0` |
| `package` | Java package name | - | `com.example.salesforce` |
| `pluginId` | Unique plugin identifier | `my-adapter` | `salesforce-adapter` |
| `pluginName` | Display name | `My Adapter` | `Salesforce Adapter` |
| `pluginClass` | Main plugin class name | `MyAdapterPlugin` | `SalesforcePlugin` |
| `pluginDescription` | Plugin description | `Custom adapter for integration` | `Connects to Salesforce CRM` |
| `pluginVendor` | Vendor name | `My Company` | `Example Corp` |
| `pluginCategory` | Category | `custom` | `crm`, `database`, `messaging`, etc. |
| `pluginIcon` | Icon name | `plug` | `salesforce`, `database`, `cloud`, etc. |
| `pluginProtocol` | Primary protocol | `HTTP` | `HTTP`, `REST`, `SOAP`, `JDBC`, etc. |
| `pluginAuth` | Authentication method | `APIKey` | `OAuth2`, `Basic`, `APIKey`, etc. |
| `pluginDocUrl` | Documentation URL | `https://example.com/docs` | Your docs URL |
| `pluginLicense` | License | `Apache-2.0` | `MIT`, `GPL-3.0`, etc. |
| `pluginTag` | Primary tag | `custom` | `salesforce`, `api`, etc. |

## Generated Project Structure

```
salesforce-plugin/
├── pom.xml                                    # Maven build file
├── README.md                                  # Plugin documentation
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/example/salesforce/
    │   │       ├── SalesforcePlugin.java              # Main plugin class
    │   │       ├── SalesforcePluginInboundHandler.java # Inbound data handler
    │   │       └── SalesforcePluginOutboundHandler.java # Outbound data handler
    │   └── resources/
    │       └── plugin.properties              # Plugin metadata
    └── test/
        └── java/
            └── com/example/salesforce/
                └── SalesforcePluginTest.java  # Unit tests
```

## Next Steps

After generating your plugin:

1. **Navigate to the project**:
   ```bash
   cd salesforce-plugin
   ```

2. **Review and update the generated code**:
   - Update configuration schema in the main plugin class
   - Implement connection logic in `testConnection()`
   - Add data fetching logic in the inbound handler
   - Add data sending logic in the outbound handler

3. **Build the plugin**:
   ```bash
   mvn clean package
   ```

4. **Test the plugin**:
   ```bash
   mvn test
   ```

5. **Deploy the plugin**:
   - Upload the generated JAR file through the Integrix UI
   - Or use the REST API to upload programmatically

## Example: Creating a Slack Plugin

```bash
mvn archetype:generate \
  -DarchetypeGroupId=com.integrixs \
  -DarchetypeArtifactId=integrix-plugin-archetype \
  -DarchetypeVersion=1.0.0 \
  -DgroupId=com.example \
  -DartifactId=slack-plugin \
  -Dversion=1.0.0 \
  -Dpackage=com.example.slack \
  -DpluginId=slack-adapter \
  -DpluginName="Slack Adapter" \
  -DpluginClass=SlackPlugin \
  -DpluginDescription="Send notifications and messages to Slack channels" \
  -DpluginVendor="Example Corp" \
  -DpluginCategory=messaging \
  -DpluginIcon=slack \
  -DpluginProtocol=REST \
  -DpluginAuth=OAuth2 \
  -DpluginDocUrl=https://example.com/slack-adapter-docs \
  -DpluginLicense=MIT \
  -DpluginTag=slack \
  -DinteractiveMode=false
```

This will create a fully functional plugin skeleton ready for Slack integration development.

## Tips

- Use descriptive plugin IDs that won't conflict with other plugins
- Choose the appropriate category to help users find your plugin
- Include comprehensive documentation in the README
- Add thorough unit tests for reliability
- Follow the naming convention: `YourServicePlugin` for the main class
- Use semantic versioning for your plugin versions