package com.integrixs.backend.cluster;

import com.hazelcast.cluster.ClusterState;
import com.hazelcast.cluster.Member;
import com.hazelcast.cluster.MembershipEvent;
import com.hazelcast.cluster.MembershipListener;
import com.hazelcast.core.*;
import com.hazelcast.cp.IAtomicLong;
import com.hazelcast.cp.ISemaphore;
import com.hazelcast.cp.lock.FencedLock;
import com.hazelcast.partition.PartitionService;
import com.hazelcast.map.IMap;
import com.hazelcast.topic.ITopic;
import com.hazelcast.topic.Message;
import com.hazelcast.topic.MessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.Serializable;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Service for cluster coordination and distributed operations
 */
@Service
@ConditionalOnBean(HazelcastInstance.class)
public class ClusterCoordinationService {

    private static final Logger logger = LoggerFactory.getLogger(ClusterCoordinationService.class);

    @Autowired
    private HazelcastInstance hazelcastInstance;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    private final Map<String, UUID> topicListeners = new ConcurrentHashMap<>();
    private final Map<String, LeaderElection> leaderElections = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        logger.info("Initializing cluster coordination service");

        // Register cluster membership listener
        hazelcastInstance.getCluster().addMembershipListener(new MembershipListener() {
            @Override
            public void memberAdded(MembershipEvent membershipEvent) {
                Member member = membershipEvent.getMember();
                logger.info("New member joined cluster: {} - {}", member.getUuid(), member.getAddress());
                eventPublisher.publishEvent(new ClusterMemberEvent(ClusterMemberEvent.Type.JOINED, member));
            }

            @Override
            public void memberRemoved(MembershipEvent membershipEvent) {
                Member member = membershipEvent.getMember();
                logger.warn("Member left cluster: {} - {}", member.getUuid(), member.getAddress());
                eventPublisher.publishEvent(new ClusterMemberEvent(ClusterMemberEvent.Type.LEFT, member));

                // Handle leader re - election if necessary
                leaderElections.values().forEach(LeaderElection::checkLeadership);
            }
        });

        // Log current cluster state
        logClusterState();
    }

    @PreDestroy
    public void cleanup() {
        logger.info("Cleaning up cluster coordination service");

        // Clean up topic listeners
        topicListeners.forEach((topic, listenerId) -> {
            ITopic<Object> iTopic = hazelcastInstance.getTopic(topic);
            iTopic.removeMessageListener(listenerId);
        });

        // Step down from leadership roles
        leaderElections.values().forEach(LeaderElection::stepDown);
    }

    // ========== Cluster Information ==========

    /**
     * Get current cluster members
     */
    public Set<ClusterMember> getClusterMembers() {
        return hazelcastInstance.getCluster().getMembers().stream()
            .map(this::toClusterMember)
            .collect(Collectors.toSet());
    }

    /**
     * Get local member information
     */
    public ClusterMember getLocalMember() {
        return toClusterMember(hazelcastInstance.getCluster().getLocalMember());
    }

    /**
     * Check if this node is the oldest member(often used for singleton services)
     */
    public boolean isOldestMember() {
        Member localMember = hazelcastInstance.getCluster().getLocalMember();
        Set<Member> members = hazelcastInstance.getCluster().getMembers();

        return members.stream()
            .min(Comparator.comparing(Member::getUuid))
            .map(oldest -> oldest.equals(localMember))
            .orElse(false);
    }

    /**
     * Get cluster state
     */
    public ClusterState getClusterState() {
        return hazelcastInstance.getCluster().getClusterState();
    }

    /**
     * Get cluster size
     */
    public int getClusterSize() {
        return hazelcastInstance.getCluster().getMembers().size();
    }

    // ========== Leader Election ==========

    /**
     * Elect leader for a specific service
     */
    public CompletableFuture<Boolean> electLeader(String serviceName, Runnable onElected, Runnable onLostLeadership) {
        logger.info("Starting leader election for service: {}", serviceName);

        LeaderElection election = new LeaderElection(serviceName, onElected, onLostLeadership);
        leaderElections.put(serviceName, election);

        return CompletableFuture.supplyAsync(election::tryBecomeLeader);
    }

    /**
     * Check if this node is leader for a service
     */
    public boolean isLeader(String serviceName) {
        LeaderElection election = leaderElections.get(serviceName);
        return election != null && election.isLeader();
    }

    /**
     * Step down from leadership
     */
    public void stepDownFromLeadership(String serviceName) {
        LeaderElection election = leaderElections.remove(serviceName);
        if(election != null) {
            election.stepDown();
        }
    }

    // ========== Distributed Execution ==========

    /**
     * Execute task on all members
     */
    public <T> Map<Member, CompletableFuture<T>> executeOnAllMembers(Callable<T> task) {
        IExecutorService executorService = hazelcastInstance.getExecutorService("distributed - executor");
        Map<Member, Future<T>> futures = executorService.submitToAllMembers(task);

        Map<Member, CompletableFuture<T>> completableFutures = new HashMap<>();
        futures.forEach((member, future) -> {
            CompletableFuture<T> cf = new CompletableFuture<>();
            cf.completeAsync(() -> {
                try {
                    return future.get();
                } catch(Exception e) {
                    logger.error("Task execution failed on member: {}", member, e);
                    cf.completeExceptionally(e);
                    return null;
                }
            });
            completableFutures.put(member, cf);
        });

        return completableFutures;
    }

    /**
     * Execute task on specific member
     */
    public <T> CompletableFuture<T> executeOnMember(Member member, Callable<T> task) {
        IExecutorService executorService = hazelcastInstance.getExecutorService("distributed - executor");
        Future<T> future = executorService.submitToMember(task, member);

        return CompletableFuture.supplyAsync(() -> {
            try {
                return future.get();
            } catch(Exception e) {
                logger.error("Task execution failed on member: {}", member, e);
                throw new RuntimeException(e);
            }
        });
    }

    // ========== Distributed Events ==========

    /**
     * Publish event to all cluster members
     */
    public void publishEvent(String topic, ClusterEvent event) {
        ITopic<ClusterEvent> iTopic = hazelcastInstance.getTopic(topic);
        iTopic.publish(event);
        logger.debug("Published event to topic {}: {}", topic, event);
    }

    /**
     * Subscribe to cluster events
     */
    public void subscribeToEvents(String topic, Consumer<ClusterEvent> handler) {
        ITopic<ClusterEvent> iTopic = hazelcastInstance.getTopic(topic);

        UUID listenerId = iTopic.addMessageListener(new MessageListener<ClusterEvent>() {
            @Override
            public void onMessage(Message<ClusterEvent> message) {
                ClusterEvent event = message.getMessageObject();
                logger.debug("Received event from topic {}: {}", topic, event);

                try {
                    handler.accept(event);
                } catch(Exception e) {
                    logger.error("Error handling cluster event", e);
                }
            }
        });

        topicListeners.put(topic, listenerId);
        logger.info("Subscribed to cluster events on topic: {}", topic);
    }

    // ========== Distributed Locking ==========

    /**
     * Acquire distributed lock
     */
    public boolean tryLock(String lockName, long timeout, TimeUnit unit) {
        FencedLock lock = hazelcastInstance.getCPSubsystem().getLock(lockName);
        try {
            return lock.tryLock(timeout, unit);
        } catch(Exception e) {
            logger.error("Failed to acquire lock: {}", lockName, e);
            return false;
        }
    }

    /**
     * Release distributed lock
     */
    public void unlock(String lockName) {
        FencedLock lock = hazelcastInstance.getCPSubsystem().getLock(lockName);
        try {
            if(lock.isLockedByCurrentThread()) {
                lock.unlock();
            }
        } catch(Exception e) {
            logger.error("Failed to release lock: {}", lockName, e);
        }
    }

    /**
     * Execute with distributed lock
     */
    public <T> T executeWithLock(String lockName, long timeout, TimeUnit unit, Callable<T> task) {
        FencedLock lock = hazelcastInstance.getCPSubsystem().getLock(lockName);
        boolean acquired = false;

        try {
            acquired = lock.tryLock(timeout, unit);
            if(!acquired) {
                throw new RuntimeException("Failed to acquire lock: " + lockName);
            }

            return task.call();
        } catch(Exception e) {
            logger.error("Error executing with lock: {}", lockName, e);
            throw new RuntimeException(e);
        } finally {
            if(acquired && lock.isLockedByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    // ========== Distributed Counters ==========

    /**
     * Get or create distributed counter
     */
    public IAtomicLong getCounter(String name) {
        return hazelcastInstance.getCPSubsystem().getAtomicLong(name);
    }

    /**
     * Increment counter
     */
    public long incrementCounter(String name) {
        return getCounter(name).incrementAndGet();
    }

    // ========== Distributed Semaphores ==========

    /**
     * Get or create distributed semaphore
     */
    public ISemaphore getSemaphore(String name, int permits) {
        ISemaphore semaphore = hazelcastInstance.getCPSubsystem().getSemaphore(name);
        semaphore.init(permits);
        return semaphore;
    }

    // ========== Cluster Health ==========

    /**
     * Perform cluster health check
     */
    public ClusterHealth checkClusterHealth() {
        ClusterHealth health = new ClusterHealth();

        try {
            // Check cluster state
            ClusterState state = getClusterState();
            health.setState(state);
            health.setHealthy(state == ClusterState.ACTIVE);

            // Check member count
            int memberCount = getClusterSize();
            health.setMemberCount(memberCount);
            health.setMinimumMembersAvailable(memberCount >= getMinimumClusterSize());

            // Check partition health
            PartitionService partitionService = hazelcastInstance.getPartitionService();
            health.setPartitionCount(partitionService.getPartitions().size());

            // Add member details
            Set<ClusterMember> members = getClusterMembers();
            health.setMembers(members);

            // Test distributed operations
            testDistributedOperations(health);

        } catch(Exception e) {
            logger.error("Cluster health check failed", e);
            health.setHealthy(false);
            health.setError(e.getMessage());
        }

        return health;
    }

    private void testDistributedOperations(ClusterHealth health) {
        try {
            // Test distributed map
            IMap<String, String> testMap = hazelcastInstance.getMap("health - check - map");
            String testKey = "test-" + System.currentTimeMillis();
            testMap.put(testKey, "value", 5, TimeUnit.SECONDS);
            testMap.remove(testKey);
            health.setDistributedMapHealthy(true);

            // Test distributed lock
            String lockName = "health - check - lock";
            if(tryLock(lockName, 1, TimeUnit.SECONDS)) {
                unlock(lockName);
                health.setDistributedLockHealthy(true);
            }

        } catch(Exception e) {
            logger.warn("Distributed operations test failed", e);
        }
    }

    private int getMinimumClusterSize() {
        // In production, this should be configurable
        return 2;
    }

    // ========== Helper Methods ==========

    private ClusterMember toClusterMember(Member member) {
        ClusterMember cm = new ClusterMember();
        cm.setUuid(member.getUuid().toString());
        cm.setAddress(member.getAddress().toString());
        cm.setLocal(member.localMember());
        cm.setLite(member.isLiteMember());
        cm.setAttributes(member.getAttributes());
        return cm;
    }

    private void logClusterState() {
        logger.info("=== Cluster State ===");
        logger.info("Cluster Name: {}", hazelcastInstance.getConfig().getClusterName());
        logger.info("Cluster State: {}", getClusterState());
        logger.info("Cluster Size: {}", getClusterSize());
        logger.info("Local Member: {}", getLocalMember().getAddress());
        logger.info("Is Oldest Member: {}", isOldestMember());
        logger.info("===================");
    }

    // ========== Inner Classes ==========

    /**
     * Leader election implementation
     */
    private class LeaderElection {
        private final String serviceName;
        private final Runnable onElected;
        private final Runnable onLostLeadership;
        private volatile boolean isLeader = false;
        private volatile Member leaderMember = null;

        public LeaderElection(String serviceName, Runnable onElected, Runnable onLostLeadership) {
            this.serviceName = serviceName;
            this.onElected = onElected;
            this.onLostLeadership = onLostLeadership;
        }

        public boolean tryBecomeLeader() {
            IMap<String, Member> leaderMap = hazelcastInstance.getMap("leader - election");
            Member localMember = hazelcastInstance.getCluster().getLocalMember();

            // Try to become leader
            Member currentLeader = leaderMap.putIfAbsent(serviceName, localMember);

            if(currentLeader == null) {
                // We became the leader
                isLeader = true;
                leaderMember = localMember;
                logger.info("Became leader for service: {}", serviceName);

                if(onElected != null) {
                    onElected.run();
                }

                return true;
            } else if(currentLeader.equals(localMember)) {
                // We were already the leader
                isLeader = true;
                leaderMember = localMember;
                return true;
            } else {
                // Someone else is leader
                isLeader = false;
                leaderMember = currentLeader;
                logger.info("Not leader for service: {}, current leader: {}",
                    serviceName, currentLeader.getAddress());
                return false;
            }
        }

        public void checkLeadership() {
            if(isLeader) {
                // Verify we're still in the leader map
                IMap<String, Member> leaderMap = hazelcastInstance.getMap("leader - election");
                Member currentLeader = leaderMap.get(serviceName);
                Member localMember = hazelcastInstance.getCluster().getLocalMember();

                if(currentLeader == null || !currentLeader.equals(localMember)) {
                    // Lost leadership
                    isLeader = false;
                    logger.warn("Lost leadership for service: {}", serviceName);

                    if(onLostLeadership != null) {
                        onLostLeadership.run();
                    }

                    // Try to become leader again
                    tryBecomeLeader();
                }
            } else {
                // Check if leader left and try to become leader
                IMap<String, Member> leaderMap = hazelcastInstance.getMap("leader - election");
                Member currentLeader = leaderMap.get(serviceName);

                if(currentLeader == null || !hazelcastInstance.getCluster().getMembers().contains(currentLeader)) {
                    // Leader is gone, try to become leader
                    leaderMap.remove(serviceName);
                    tryBecomeLeader();
                }
            }
        }

        public void stepDown() {
            if(isLeader) {
                IMap<String, Member> leaderMap = hazelcastInstance.getMap("leader - election");
                Member localMember = hazelcastInstance.getCluster().getLocalMember();

                // Remove only if we're the current leader
                leaderMap.remove(serviceName, localMember);
                isLeader = false;

                logger.info("Stepped down from leadership for service: {}", serviceName);

                if(onLostLeadership != null) {
                    onLostLeadership.run();
                }
            }
        }

        public boolean isLeader() {
            return isLeader;
        }
    }

    /**
     * Cluster event base class
     */
    public static class ClusterEvent implements Serializable {
        private final String source;
        private final Instant timestamp;
        private final Map<String, Object> data;

        public ClusterEvent(String source) {
            this.source = source;
            this.timestamp = Instant.now();
            this.data = new HashMap<>();
        }

        public String getSource() { return source; }
        public Instant getTimestamp() { return timestamp; }
        public Map<String, Object> getData() { return data; }
    }

    /**
     * Cluster member event
     */
    public static class ClusterMemberEvent extends ClusterEvent {
        public enum Type { JOINED, LEFT }

        private final Type type;
        private final Member member;

        public ClusterMemberEvent(Type type, Member member) {
            super("cluster");
            this.type = type;
            this.member = member;
        }

        public Type getType() { return type; }
        public Member getMember() { return member; }
    }

    /**
     * Cluster member information
     */
    public static class ClusterMember {
        private String uuid;
        private String address;
        private boolean local;
        private boolean lite;
        private Map<String, String> attributes;

        // Getters and setters
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }

        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }

        public boolean isLocal() { return local; }
        public void setLocal(boolean local) { this.local = local; }

        public boolean isLite() { return lite; }
        public void setLite(boolean lite) { this.lite = lite; }

        public Map<String, String> getAttributes() { return attributes; }
        public void setAttributes(Map<String, String> attributes) { this.attributes = attributes; }
    }

    /**
     * Cluster health information
     */
    public static class ClusterHealth {
        private boolean healthy;
        private ClusterState state;
        private int memberCount;
        private boolean minimumMembersAvailable;
        private int partitionCount;
        private Set<ClusterMember> members;
        private boolean distributedMapHealthy;
        private boolean distributedLockHealthy;
        private String error;

        // Getters and setters
        public boolean isHealthy() { return healthy; }
        public void setHealthy(boolean healthy) { this.healthy = healthy; }

        public ClusterState getState() { return state; }
        public void setState(ClusterState state) { this.state = state; }

        public int getMemberCount() { return memberCount; }
        public void setMemberCount(int memberCount) { this.memberCount = memberCount; }

        public boolean isMinimumMembersAvailable() { return minimumMembersAvailable; }
        public void setMinimumMembersAvailable(boolean available) { this.minimumMembersAvailable = available; }

        public int getPartitionCount() { return partitionCount; }
        public void setPartitionCount(int partitionCount) { this.partitionCount = partitionCount; }

        public Set<ClusterMember> getMembers() { return members; }
        public void setMembers(Set<ClusterMember> members) { this.members = members; }

        public boolean isDistributedMapHealthy() { return distributedMapHealthy; }
        public void setDistributedMapHealthy(boolean healthy) { this.distributedMapHealthy = healthy; }

        public boolean isDistributedLockHealthy() { return distributedLockHealthy; }
        public void setDistributedLockHealthy(boolean healthy) { this.distributedLockHealthy = healthy; }

        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }
}
