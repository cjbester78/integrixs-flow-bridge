package com.integrixs.data.model;
import java.util.UUID;

/**
 * Entity representing Role.
 * This maps to the corresponding table in the database.
 */
public class Role {

        /** Unique identifier(UUID) for the entity */
    private UUID id;

    /** Name of the component */
    private String name;

    private String permissions;

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPermissions() { return permissions; }
    public void setPermissions(String permissions) { this.permissions = permissions; }
}
