package com.irbre.agent;



import com.irbre.agent.model.EventBatch;
import com.irbre.agent.model.TraceEvent;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Batches trace events and sends them asynchronously to the collector.
 */
public class EventBatcher {
    //private static final Logger logger = LoggerFactory.getLogger(EventBatcher.class);


    private static void logDebug(String msg) {
        System.out.println("[IRBRE DEBUG] " + msg);
    }

    private static void logError(String msg, Throwable t) {
        System.err.println("[IRBRE ERROR] " + msg);
        t.printStackTrace();
    }
    private static void logInfo(String msg) {
        System.out.println("[IRBRE] " + msg);
    }
    private static void logWarn(String msg) {
        System.err.println("[IRBRE WARN] " + msg);
    }

    private final AgentConfiguration config;
    private final EventSender sender;
    private final BlockingQueue<TraceEvent> eventQueue;
    private final ScheduledExecutorService scheduler;
    private final ExecutorService batchProcessor;

    private volatile boolean running = true;

    public EventBatcher(AgentConfiguration config, EventSender sender) {
        this.config = config;
        this.sender = sender;
        this.eventQueue = new ArrayBlockingQueue<>(config.getQueueSize());
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "irbre-batch-scheduler");
            t.setDaemon(true);
            return t;
        });
        this.batchProcessor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "irbre-batch-processor");
            t.setDaemon(true);
            return t;
        });

        startBatchingTimer();
    }

    /**
     * Add an event to the batch queue.
     */
    public void addEvent(TraceEvent event) {
        if (!running) {
            return;
        }

        try {
            boolean added = eventQueue.offer(event, 100, TimeUnit.MILLISECONDS);
            if (!added) {
                logWarn("Event queue full, dropping event");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logWarn("Interrupted while adding event to queue");
        }
    }

    /**
     * Start periodic batch flushing.
     */
    private void startBatchingTimer() {
        scheduler.scheduleAtFixedRate(
                this::processBatch,
                config.getBatchIntervalMs(),
                config.getBatchIntervalMs(),
                TimeUnit.MILLISECONDS
        );
    }

    /**
     * Process and send a batch of events.
     */
    private void processBatch() {
        if (!running) {
            return;
        }

        batchProcessor.submit(() -> {
            try {
                List<TraceEvent> events = new ArrayList<>(config.getBatchSize());
                eventQueue.drainTo(events, config.getBatchSize());

                if (!events.isEmpty()) {
                    EventBatch batch = new EventBatch(events);
                    sender.sendBatch(batch);
                    logDebug("Sent batch of "+events.size()+" events" );
                }
            } catch (Exception e) {
                logError("Error processing batch", e);
            }
        });
    }

    /**
     * Flush any remaining events.
     */
    public void flush() {
        logInfo("Flushing remaining events...");

        try {
            List<TraceEvent> events = new ArrayList<>();
            eventQueue.drainTo(events);

            if (!events.isEmpty()) {
                EventBatch batch = new EventBatch(events);
                sender.sendBatch(batch);
                logInfo("Flushed "+events.size()+" events" );
            }
        } catch (Exception e) {
            logError("Error flushing events", e);
        }
    }

    /**
     * Shutdown the batcher.
     */
    public void shutdown() {
        logInfo("Shutting down event batcher...");
        running = false;

        try {
            scheduler.shutdown();
            scheduler.awaitTermination(5, TimeUnit.SECONDS);

            batchProcessor.shutdown();
            batchProcessor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logWarn("Interrupted during shutdown");
        }

        logInfo("Event batcher shutdown complete");
    }
}
