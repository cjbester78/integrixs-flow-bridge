package com.integrixs.data.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "adapter_plugins")
@Getter
@Setter
public class AdapterPlugin {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "adapter_type_id", nullable = false)
    private AdapterType adapterType;

    @Column(name = "plugin_class", nullable = false, length = 500)
    private String pluginClass;

    @Column(name = "plugin_version", nullable = false, length = 20)
    private String pluginVersion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "jar_file_id")
    private JarFile jarFile;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> configuration;

    @Column(name = "is_active")
    private boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
