package com.integrixs.testing.engine;

import com.integrixs.testing.core.FlowTestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * Core engine for executing flows in test environment
 */
public class FlowEngine {
    
    private static final Logger logger = LoggerFactory.getLogger(FlowEngine.class);
    
    private final FlowTestContext context;
    private final ExecutorService executorService;
    private final ScheduledExecutorService scheduledExecutor;
    private final BlockingQueue<FlowTask> taskQueue;
    private volatile boolean running;
    
    public FlowEngine(FlowTestContext context) {
        this.context = context;
        this.executorService = new ThreadPoolExecutor(
            2, 10,
            60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(),
            new ThreadFactory() {
                private int counter = 0;
                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "flow-engine-" + counter++);
                }
            }
       );
        this.scheduledExecutor = Executors.newScheduledThreadPool(2);
        this.taskQueue = new LinkedBlockingQueue<>();
        this.running = true;
        startTaskProcessor();
    }
    
    /**
     * Start the task processor
     */
    private void startTaskProcessor() {
        executorService.submit(() -> {
            while (running) {
                try {
                    FlowTask task = taskQueue.poll(100, TimeUnit.MILLISECONDS);
                    if (task != null) {
                        processTask(task);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    logger.error("Error processing task", e);
                }
            }
        });
    }
    
    /**
     * Process a flow task
     */
    private void processTask(FlowTask task) {
        try {
            task.execute();
        } catch (Exception e) {
            logger.error("Task execution failed", e);
            task.fail(e);
        }
    }
    
    /**
     * Submit a task for execution
     */
    public Future<?> submitTask(Runnable task) {
        return executorService.submit(task);
    }
    
    /**
     * Submit a callable task
     */
    public <T> Future<T> submitTask(Callable<T> task) {
        return executorService.submit(task);
    }
    
    /**
     * Schedule a task
     */
    public ScheduledFuture<?> scheduleTask(Runnable task, long delay, TimeUnit unit) {
        return scheduledExecutor.schedule(task, delay, unit);
    }
    
    /**
     * Schedule a periodic task
     */
    public ScheduledFuture<?> schedulePeriodicTask(Runnable task, long initialDelay, long period, TimeUnit unit) {
        return scheduledExecutor.scheduleAtFixedRate(task, initialDelay, period, unit);
    }
    
    /**
     * Queue a flow task
     */
    public void queueTask(FlowTask task) {
        try {
            taskQueue.put(task);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to queue task", e);
        }
    }
    
    /**
     * Shutdown the engine
     */
    public void shutdown() {
        running = false;
        
        scheduledExecutor.shutdown();
        executorService.shutdown();
        
        try {
            if (!scheduledExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                scheduledExecutor.shutdownNow();
            }
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduledExecutor.shutdownNow();
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Get engine statistics
     */
    public EngineStatistics getStatistics() {
        ThreadPoolExecutor executor = (ThreadPoolExecutor) executorService;
        
        EngineStatistics stats = new EngineStatistics();
        stats.setActiveThreads(executor.getActiveCount());
        stats.setPoolSize(executor.getPoolSize());
        stats.setQueueSize(executor.getQueue().size());
        stats.setCompletedTasks(executor.getCompletedTaskCount());
        stats.setTotalTasks(executor.getTaskCount());
        
        return stats;
    }
    
    /**
     * Engine statistics
     */
    public static class EngineStatistics {
        private int activeThreads;
        private int poolSize;
        private int queueSize;
        private long completedTasks;
        private long totalTasks;
        
        // Getters and setters
        public int getActiveThreads() {
            return activeThreads;
        }
        
        public void setActiveThreads(int activeThreads) {
            this.activeThreads = activeThreads;
        }
        
        public int getPoolSize() {
            return poolSize;
        }
        
        public void setPoolSize(int poolSize) {
            this.poolSize = poolSize;
        }
        
        public int getQueueSize() {
            return queueSize;
        }
        
        public void setQueueSize(int queueSize) {
            this.queueSize = queueSize;
        }
        
        public long getCompletedTasks() {
            return completedTasks;
        }
        
        public void setCompletedTasks(long completedTasks) {
            this.completedTasks = completedTasks;
        }
        
        public long getTotalTasks() {
            return totalTasks;
        }
        
        public void setTotalTasks(long totalTasks) {
            this.totalTasks = totalTasks;
        }
    }
    
    /**
     * Base class for flow tasks
     */
    public abstract static class FlowTask {
        private final CompletableFuture<Void> future = new CompletableFuture<>();
        
        public abstract void execute() throws Exception;
        
        public void complete() {
            future.complete(null);
        }
        
        public void fail(Throwable error) {
            future.completeExceptionally(error);
        }
        
        public CompletableFuture<Void> getFuture() {
            return future;
        }
    }
}