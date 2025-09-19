package com.integrixs.backend.config;

import com.hazelcast.config.*;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.spring.cache.HazelcastCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.hazelcast.HazelcastIndexedSessionRepository;
import org.springframework.session.hazelcast.config.annotation.SpringSessionHazelcastInstance;
import org.springframework.session.hazelcast.config.annotation.web.http.EnableHazelcastHttpSession;

import java.util.List;

/**
 * Hazelcast configuration for distributed caching and session management
 */
@Configuration
@ConditionalOnProperty(name = "hazelcast.enabled", havingValue = "true", matchIfMissing = false)
@EnableHazelcastHttpSession(maxInactiveIntervalInSeconds = 3600)
public class HazelcastConfig {

    private static final Logger logger = LoggerFactory.getLogger(HazelcastConfig.class);

    @Value("$ {hazelcast.cluster.name:integrixs - cluster}")
    private String clusterName;

    @Value("$ {hazelcast.instance.name:integrixs - node}")
    private String instanceName;

    @Value("$ {hazelcast.network.port:5701}")
    private int networkPort;

    @Value("$ {hazelcast.network.port.auto - increment:true}")
    private boolean portAutoIncrement;

    @Value("$ {hazelcast.network.multicast.enabled:false}")
    private boolean multicastEnabled;

    @Value("$ {hazelcast.network.tcp.enabled:true}")
    private boolean tcpEnabled;

    @Value("$ {hazelcast.network.tcp.members:127.0.0.1}")
    private String tcpMembers;

    @Value("$ {hazelcast.network.kubernetes.enabled:false}")
    private boolean kubernetesEnabled;

    @Value("$ {hazelcast.network.kubernetes.namespace:default}")
    private String kubernetesNamespace;

    @Value("$ {hazelcast.network.kubernetes.service - name:integrixs - hazelcast}")
    private String kubernetesServiceName;

    /**
     * Main Hazelcast instance configuration
     */
    @Bean
    // @SpringSessionHazelcastInstance
    public HazelcastInstance hazelcastInstance() {
        logger.info("Configuring Hazelcast cluster: {}", clusterName);

        Config config = new Config();
        config.setClusterName(clusterName);
        config.setInstanceName(instanceName);

        // Configure network
        configureNetwork(config);

        // Configure distributed maps
        configureDistributedMaps(config);

        // Configure distributed queues
        configureDistributedQueues(config);

        // Configure distributed topics
        configureDistributedTopics(config);

        // Configure CP subsystem for strong consistency
        configureCPSubsystem(config);

        // Configure management center
        configureManagementCenter(config);

        // Configure serialization
        configureSerialization(config);

        // Configure partition groups
        configurePartitionGroups(config);

        return Hazelcast.newHazelcastInstance(config);
    }

    /**
     * Configure network settings
     */
    private void configureNetwork(Config config) {
        NetworkConfig networkConfig = config.getNetworkConfig();
        networkConfig.setPort(networkPort);
        networkConfig.setPortAutoIncrement(portAutoIncrement);

        // Configure join mechanisms
        JoinConfig joinConfig = networkConfig.getJoin();

        // Multicast discovery
        MulticastConfig multicastConfig = joinConfig.getMulticastConfig();
        multicastConfig.setEnabled(multicastEnabled);
        if(multicastEnabled) {
            multicastConfig.setMulticastGroup("224.2.2.3");
            multicastConfig.setMulticastPort(54327);
        }

        // TCP/IP discovery
        TcpIpConfig tcpIpConfig = joinConfig.getTcpIpConfig();
        tcpIpConfig.setEnabled(tcpEnabled);
        if(tcpEnabled) {
            List<String> members = List.of(tcpMembers.split(","));
            tcpIpConfig.setMembers(members);
            tcpIpConfig.setRequiredMember(null);
        }

        // Kubernetes discovery
        if(kubernetesEnabled) {
            KubernetesConfig k8sConfig = joinConfig.getKubernetesConfig();
            k8sConfig.setEnabled(true);
            k8sConfig.setProperty("namespace", kubernetesNamespace);
            k8sConfig.setProperty("service - name", kubernetesServiceName);
        }

        // Configure SSL/TLS if needed
        configureSSL(networkConfig);
    }

    /**
     * Configure SSL/TLS
     */
    private void configureSSL(NetworkConfig networkConfig) {
        // In production, enable SSL
        SSLConfig sslConfig = networkConfig.getSSLConfig();
        sslConfig.setEnabled(false); // Enable in production

        if(sslConfig.isEnabled()) {
            sslConfig.setFactoryClassName("com.hazelcast.nio.ssl.BasicSSLContextFactory");
            sslConfig.setProperty("protocol", "TLS");
            sslConfig.setProperty("keyStore", "/path/to/keystore.jks");
            sslConfig.setProperty("keyStorePassword", "password");
            sslConfig.setProperty("trustStore", "/path/to/truststore.jks");
            sslConfig.setProperty("trustStorePassword", "password");
        }
    }

    /**
     * Configure distributed maps
     */
    private void configureDistributedMaps(Config config) {
        // Session map configuration
        MapConfig sessionMapConfig = new MapConfig("spring:session:sessions");
        sessionMapConfig.setTimeToLiveSeconds(3600); // 1 hour
        sessionMapConfig.setMaxIdleSeconds(1800); // 30 minutes
        sessionMapConfig.setEvictionConfig(new EvictionConfig()
            .setEvictionPolicy(EvictionPolicy.LRU)
            .setMaxSizePolicy(MaxSizePolicy.PER_NODE)
            .setSize(10000));
        sessionMapConfig.setBackupCount(1);
        sessionMapConfig.setAsyncBackupCount(0);
        config.addMapConfig(sessionMapConfig);

        // Cache map configuration
        MapConfig cacheMapConfig = new MapConfig("distributed - cache-*");
        cacheMapConfig.setTimeToLiveSeconds(1800); // 30 minutes default
        cacheMapConfig.setEvictionConfig(new EvictionConfig()
            .setEvictionPolicy(EvictionPolicy.LFU)
            .setMaxSizePolicy(MaxSizePolicy.PER_NODE)
            .setSize(5000));
        cacheMapConfig.setBackupCount(1);
        cacheMapConfig.setAsyncBackupCount(1);
        cacheMapConfig.setReadBackupData(true);
        config.addMapConfig(cacheMapConfig);

        // Flow state map
        MapConfig flowStateMapConfig = new MapConfig("flow - state-*");
        flowStateMapConfig.setTimeToLiveSeconds(86400); // 24 hours
        flowStateMapConfig.setBackupCount(2); // Higher backup for critical data
        flowStateMapConfig.setAsyncBackupCount(0);
        flowStateMapConfig.setInMemoryFormat(InMemoryFormat.BINARY);
        config.addMapConfig(flowStateMapConfig);

        // Rate limit map
        MapConfig rateLimitMapConfig = new MapConfig("rate - limit-*");
        rateLimitMapConfig.setTimeToLiveSeconds(300); // 5 minutes
        rateLimitMapConfig.setEvictionConfig(new EvictionConfig()
            .setEvictionPolicy(EvictionPolicy.LRU)
            .setMaxSizePolicy(MaxSizePolicy.PER_NODE)
            .setSize(100000));
        rateLimitMapConfig.setBackupCount(0); // No backup for transient data
        config.addMapConfig(rateLimitMapConfig);
    }

    /**
     * Configure distributed queues
     */
    private void configureDistributedQueues(Config config) {
        // Job queue configuration
        QueueConfig jobQueueConfig = new QueueConfig("job - queue-*");
        jobQueueConfig.setMaxSize(10000);
        jobQueueConfig.setBackupCount(1);
        jobQueueConfig.setAsyncBackupCount(0);
        jobQueueConfig.setEmptyQueueTtl(300); // 5 minutes
        config.addQueueConfig(jobQueueConfig);

        // Message queue configuration
        QueueConfig messageQueueConfig = new QueueConfig("message - queue-*");
        messageQueueConfig.setMaxSize(100000);
        messageQueueConfig.setBackupCount(2);
        messageQueueConfig.setAsyncBackupCount(0);
        config.addQueueConfig(messageQueueConfig);
    }

    /**
     * Configure distributed topics
     */
    private void configureDistributedTopics(Config config) {
        // Event topic configuration
        TopicConfig eventTopicConfig = new TopicConfig("event - topic-*");
        eventTopicConfig.setGlobalOrderingEnabled(true);
        eventTopicConfig.setMultiThreadingEnabled(false);
        config.addTopicConfig(eventTopicConfig);

        // Notification topic
        TopicConfig notificationTopicConfig = new TopicConfig("notification - topic");
        notificationTopicConfig.setGlobalOrderingEnabled(false);
        notificationTopicConfig.setMultiThreadingEnabled(true);
        config.addTopicConfig(notificationTopicConfig);
    }

    /**
     * Configure CP subsystem for strong consistency
     */
    private void configureCPSubsystem(Config config) {
        CPSubsystemConfig cpConfig = config.getCPSubsystemConfig();
        cpConfig.setCPMemberCount(3); // Minimum 3 for fault tolerance
        cpConfig.setGroupSize(3);
        cpConfig.setSessionTimeToLiveSeconds(300);
        cpConfig.setSessionHeartbeatIntervalSeconds(5);
        cpConfig.setMissingCPMemberAutoRemovalSeconds(14400); // 4 hours

        // Configure Raft algorithm
        RaftAlgorithmConfig raftConfig = cpConfig.getRaftAlgorithmConfig();
        raftConfig.setLeaderElectionTimeoutInMillis(2000);
        raftConfig.setLeaderHeartbeatPeriodInMillis(500);
        raftConfig.setMaxMissedLeaderHeartbeatCount(5);
        raftConfig.setAppendRequestMaxEntryCount(100);
        raftConfig.setCommitIndexAdvanceCountToSnapshot(10000);
        raftConfig.setUncommittedEntryCountToRejectNewAppends(200);
        raftConfig.setAppendRequestBackoffTimeoutInMillis(100);

        // Configure semaphores
        cpConfig.addSemaphoreConfig(new SemaphoreConfig("distributed - lock-*")
            .setInitialPermits(1)
            .setJDKCompatible(false));
    }

    /**
     * Configure management center
     */
    private void configureManagementCenter(Config config) {
        ManagementCenterConfig mcConfig = config.getManagementCenterConfig();
        mcConfig.setConsoleEnabled(true);

        // In production, connect to Management Center
        // mcConfig.addTrustedInterface("192.168.1.*");
        // mcConfig.setUrl("http://localhost:8080/hazelcast - mancenter");
    }

    /**
     * Configure serialization
     */
    private void configureSerialization(Config config) {
        SerializationConfig serializationConfig = config.getSerializationConfig();
        serializationConfig.setAllowUnsafe(true);
        serializationConfig.setEnableCompression(true);
        serializationConfig.setEnableSharedObject(true);

        // Configure portable serialization for cross - platform compatibility
        serializationConfig.setPortableVersion(1);
    }

    /**
     * Configure partition groups for rack awareness
     */
    private void configurePartitionGroups(Config config) {
        PartitionGroupConfig partitionGroupConfig = config.getPartitionGroupConfig();
        partitionGroupConfig.setEnabled(true);
        partitionGroupConfig.setGroupType(PartitionGroupConfig.MemberGroupType.ZONE_AWARE);
    }

    /**
     * Hazelcast - based cache manager
     */
    @Bean
    @ConditionalOnProperty(name = "hazelcast.cache.enabled", havingValue = "true", matchIfMissing = true)
    public CacheManager hazelcastCacheManager(HazelcastInstance hazelcastInstance) {
        logger.info("Configuring Hazelcast cache manager");
        return new HazelcastCacheManager(hazelcastInstance);
    }

    /**
     * Custom session repository configuration
     */
    @Bean
    public HazelcastIndexedSessionRepository hazelcastSessionRepository(HazelcastInstance hazelcastInstance) {
        HazelcastIndexedSessionRepository repository = new HazelcastIndexedSessionRepository(hazelcastInstance);
        repository.setDefaultMaxInactiveInterval(3600); // 1 hour
        repository.setSessionMapName("spring:session:sessions");
        repository.setFlushMode(HazelcastIndexedSessionRepository.HazelcastFlushMode.IMMEDIATE);
        return repository;
    }
}
