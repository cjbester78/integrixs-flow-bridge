#!/usr/bin/env python3
import os
import re
from pathlib import Path

def fix_service_endpoint_inner_classes():
    """Fix the ServiceEndpoint inner classes that are missing fields"""
    file_path = "webserver/src/main/java/com/integrixs/webserver/domain/model/ServiceEndpoint.java"
    
    if not os.path.exists(file_path):
        print(f"File not found: {file_path}")
        return
    
    with open(file_path, 'r') as f:
        content = f.read()
    
    # Find and fix AuthenticationConfig inner class
    auth_config_pattern = r'(public static class AuthenticationConfig \{[^}]+)\}'
    auth_config_match = re.search(auth_config_pattern, content, re.DOTALL)
    
    if auth_config_match:
        auth_config_content = auth_config_match.group(1)
        
        # Check if fields are missing
        if 'private String authType;' not in auth_config_content:
            # Add missing fields after class declaration
            fields_to_add = """
        private String authType;
        private String username;
        private String password;
        private String clientId;
        private String clientSecret;
        private String tokenUrl;
        private Long tokenExpirySeconds;
        private Map<String, String> credentials;
"""
            # Insert fields after opening brace
            insertion_point = auth_config_content.find('{') + 1
            new_auth_config = auth_config_content[:insertion_point] + fields_to_add + auth_config_content[insertion_point:]
            
            # Add constructor, getters and setters
            methods_to_add = """
        
        // Default constructor
        public AuthenticationConfig() {
            this.credentials = new HashMap<>();
        }
        
        // All args constructor
        public AuthenticationConfig(String authType, String username, String password, String clientId, 
                                   String clientSecret, String tokenUrl, Long tokenExpirySeconds, 
                                   Map<String, String> credentials) {
            this.authType = authType;
            this.username = username;
            this.password = password;
            this.clientId = clientId;
            this.clientSecret = clientSecret;
            this.tokenUrl = tokenUrl;
            this.tokenExpirySeconds = tokenExpirySeconds;
            this.credentials = credentials != null ? credentials : new HashMap<>();
        }
        
        // Getters
        public String getAuthType() { return authType; }
        public String getUsername() { return username; }
        public String getPassword() { return password; }
        public String getClientId() { return clientId; }
        public String getClientSecret() { return clientSecret; }
        public String getTokenUrl() { return tokenUrl; }
        public Long getTokenExpirySeconds() { return tokenExpirySeconds; }
        public Map<String, String> getCredentials() { return credentials; }
        
        // Setters
        public void setAuthType(String authType) { this.authType = authType; }
        public void setUsername(String username) { this.username = username; }
        public void setPassword(String password) { this.password = password; }
        public void setClientId(String clientId) { this.clientId = clientId; }
        public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }
        public void setTokenUrl(String tokenUrl) { this.tokenUrl = tokenUrl; }
        public void setTokenExpirySeconds(Long tokenExpirySeconds) { this.tokenExpirySeconds = tokenExpirySeconds; }
        public void setCredentials(Map<String, String> credentials) { this.credentials = credentials; }
        
        // Builder
        public static AuthenticationConfigBuilder builder() {
            return new AuthenticationConfigBuilder();
        }
        
        public static class AuthenticationConfigBuilder {
            private String authType;
            private String username;
            private String password;
            private String clientId;
            private String clientSecret;
            private String tokenUrl;
            private Long tokenExpirySeconds;
            private Map<String, String> credentials = new HashMap<>();
            
            public AuthenticationConfigBuilder authType(String authType) {
                this.authType = authType;
                return this;
            }
            
            public AuthenticationConfigBuilder username(String username) {
                this.username = username;
                return this;
            }
            
            public AuthenticationConfigBuilder password(String password) {
                this.password = password;
                return this;
            }
            
            public AuthenticationConfigBuilder clientId(String clientId) {
                this.clientId = clientId;
                return this;
            }
            
            public AuthenticationConfigBuilder clientSecret(String clientSecret) {
                this.clientSecret = clientSecret;
                return this;
            }
            
            public AuthenticationConfigBuilder tokenUrl(String tokenUrl) {
                this.tokenUrl = tokenUrl;
                return this;
            }
            
            public AuthenticationConfigBuilder tokenExpirySeconds(Long tokenExpirySeconds) {
                this.tokenExpirySeconds = tokenExpirySeconds;
                return this;
            }
            
            public AuthenticationConfigBuilder credentials(Map<String, String> credentials) {
                this.credentials = credentials;
                return this;
            }
            
            public AuthenticationConfig build() {
                return new AuthenticationConfig(authType, username, password, clientId, clientSecret, 
                                               tokenUrl, tokenExpirySeconds, credentials);
            }
        }"""
            
            new_auth_config = new_auth_config + methods_to_add
            content = content.replace(auth_config_match.group(0), new_auth_config + "\n    }")
    
    # Fix ConnectionConfig inner class
    conn_config_pattern = r'(public static class ConnectionConfig \{[^}]+)\}'
    conn_config_match = re.search(conn_config_pattern, content, re.DOTALL)
    
    if conn_config_match:
        conn_config_content = conn_config_match.group(1)
        
        # Check if fields are missing
        if 'private Integer connectionTimeoutSeconds;' not in conn_config_content:
            # Add missing fields
            fields_to_add = """
        private Integer connectionTimeoutSeconds;
        private Integer readTimeoutSeconds;
        private Integer maxConnections;
        private Integer maxConnectionsPerRoute;
        private Boolean keepAlive;
        private Boolean followRedirects;
"""
            insertion_point = conn_config_content.find('{') + 1
            new_conn_config = conn_config_content[:insertion_point] + fields_to_add + conn_config_content[insertion_point:]
            
            # Add methods
            methods_to_add = """
        
        // Default constructor
        public ConnectionConfig() {
        }
        
        // All args constructor
        public ConnectionConfig(Integer connectionTimeoutSeconds, Integer readTimeoutSeconds, 
                               Integer maxConnections, Integer maxConnectionsPerRoute, 
                               Boolean keepAlive, Boolean followRedirects) {
            this.connectionTimeoutSeconds = connectionTimeoutSeconds;
            this.readTimeoutSeconds = readTimeoutSeconds;
            this.maxConnections = maxConnections;
            this.maxConnectionsPerRoute = maxConnectionsPerRoute;
            this.keepAlive = keepAlive;
            this.followRedirects = followRedirects;
        }
        
        // Getters
        public Integer getConnectionTimeoutSeconds() { return connectionTimeoutSeconds; }
        public Integer getReadTimeoutSeconds() { return readTimeoutSeconds; }
        public Integer getMaxConnections() { return maxConnections; }
        public Integer getMaxConnectionsPerRoute() { return maxConnectionsPerRoute; }
        public Boolean getKeepAlive() { return keepAlive; }
        public Boolean getFollowRedirects() { return followRedirects; }
        
        // Setters
        public void setConnectionTimeoutSeconds(Integer connectionTimeoutSeconds) { 
            this.connectionTimeoutSeconds = connectionTimeoutSeconds; 
        }
        public void setReadTimeoutSeconds(Integer readTimeoutSeconds) { 
            this.readTimeoutSeconds = readTimeoutSeconds; 
        }
        public void setMaxConnections(Integer maxConnections) { 
            this.maxConnections = maxConnections; 
        }
        public void setMaxConnectionsPerRoute(Integer maxConnectionsPerRoute) { 
            this.maxConnectionsPerRoute = maxConnectionsPerRoute; 
        }
        public void setKeepAlive(Boolean keepAlive) { 
            this.keepAlive = keepAlive; 
        }
        public void setFollowRedirects(Boolean followRedirects) { 
            this.followRedirects = followRedirects; 
        }
        
        // Builder
        public static ConnectionConfigBuilder builder() {
            return new ConnectionConfigBuilder();
        }
        
        public static class ConnectionConfigBuilder {
            private Integer connectionTimeoutSeconds;
            private Integer readTimeoutSeconds;
            private Integer maxConnections;
            private Integer maxConnectionsPerRoute;
            private Boolean keepAlive;
            private Boolean followRedirects;
            
            public ConnectionConfigBuilder connectionTimeoutSeconds(Integer connectionTimeoutSeconds) {
                this.connectionTimeoutSeconds = connectionTimeoutSeconds;
                return this;
            }
            
            public ConnectionConfigBuilder readTimeoutSeconds(Integer readTimeoutSeconds) {
                this.readTimeoutSeconds = readTimeoutSeconds;
                return this;
            }
            
            public ConnectionConfigBuilder maxConnections(Integer maxConnections) {
                this.maxConnections = maxConnections;
                return this;
            }
            
            public ConnectionConfigBuilder maxConnectionsPerRoute(Integer maxConnectionsPerRoute) {
                this.maxConnectionsPerRoute = maxConnectionsPerRoute;
                return this;
            }
            
            public ConnectionConfigBuilder keepAlive(Boolean keepAlive) {
                this.keepAlive = keepAlive;
                return this;
            }
            
            public ConnectionConfigBuilder followRedirects(Boolean followRedirects) {
                this.followRedirects = followRedirects;
                return this;
            }
            
            public ConnectionConfig build() {
                return new ConnectionConfig(connectionTimeoutSeconds, readTimeoutSeconds, 
                                          maxConnections, maxConnectionsPerRoute, 
                                          keepAlive, followRedirects);
            }
        }"""
            
            new_conn_config = new_conn_config + methods_to_add
            content = content.replace(conn_config_match.group(0), new_conn_config + "\n    }")
    
    # Fix missing getMetadata() method in ServiceEndpoint class
    if 'public Map<String, String> getMetadata()' not in content:
        # Find where to insert it (after other getters)
        insert_after = re.search(r'(public\s+\w+\s+get\w+\(\)\s*\{[^}]+\})\s*(?=//|public\s+void|private|$)', content, re.DOTALL)
        if insert_after:
            insertion_point = insert_after.end()
            getter_to_add = """
    
    public Map<String, String> getMetadata() {
        return metadata;
    }"""
            content = content[:insertion_point] + getter_to_add + content[insertion_point:]
    
    # Add missing import for HashMap if not present
    if 'import java.util.HashMap;' not in content and 'HashMap' in content:
        import_pos = content.find('import java.util.')
        if import_pos != -1:
            content = content[:import_pos] + 'import java.util.HashMap;\n' + content[import_pos:]
    
    # Write back
    with open(file_path, 'w') as f:
        f.write(content)
    
    print(f"Fixed inner classes in {file_path}")

def fix_auth_config_dto():
    """Fix AuthenticationConfigDTO missing fields"""
    file_path = "webserver/src/main/java/com/integrixs/webserver/api/dto/AuthenticationConfigDTO.java"
    
    if not os.path.exists(file_path):
        print(f"File not found: {file_path}")
        return
    
    with open(file_path, 'r') as f:
        content = f.read()
    
    # Check if credentials field exists
    if 'private Map<String, String> credentials' not in content:
        # Find where to insert field (after other private fields)
        field_pattern = r'(private\s+\w+(?:<[^>]+>)?\s+\w+;)(?!.*private)'
        last_field_match = None
        for match in re.finditer(field_pattern, content):
            last_field_match = match
        
        if last_field_match:
            insertion_point = last_field_match.end()
            field_to_add = "\n    private Map<String, String> credentials;"
            content = content[:insertion_point] + field_to_add + content[insertion_point:]
            
            # Add getter
            getter_pattern = r'(public\s+\w+(?:<[^>]+>)?\s+get\w+\(\)\s*\{[^}]+\})(?!.*public\s+\w+(?:<[^>]+>)?\s+get)'
            last_getter_match = None
            for match in re.finditer(getter_pattern, content, re.DOTALL):
                last_getter_match = match
            
            if last_getter_match:
                insertion_point = last_getter_match.end()
                getter_to_add = """
    public Map<String, String> getCredentials() {
        return credentials;
    }"""
                content = content[:insertion_point] + getter_to_add + content[insertion_point:]
            
            # Add setter
            setter_pattern = r'(public\s+void\s+set\w+\([^)]+\)\s*\{[^}]+\})(?!.*public\s+void\s+set)'
            last_setter_match = None
            for match in re.finditer(setter_pattern, content, re.DOTALL):
                last_setter_match = match
            
            if last_setter_match:
                insertion_point = last_setter_match.end()
                setter_to_add = """
    public void setCredentials(Map<String, String> credentials) {
        this.credentials = credentials;
    }"""
                content = content[:insertion_point] + setter_to_add + content[insertion_point:]
    
    # Add missing import
    if 'import java.util.Map;' not in content:
        import_pos = content.find('package')
        import_end = content.find('\n', import_pos)
        content = content[:import_end + 1] + '\nimport java.util.Map;\n' + content[import_end + 1:]
    
    # Write back
    with open(file_path, 'w') as f:
        f.write(content)
    
    print(f"Fixed {file_path}")

def main():
    # Fix the specific compilation errors
    fix_service_endpoint_inner_classes()
    fix_auth_config_dto()
    print("\nFixed specific compilation errors in webserver module")

if __name__ == "__main__":
    main()