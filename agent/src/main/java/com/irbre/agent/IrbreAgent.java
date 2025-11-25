package com.irbre.agent;





import java.lang.instrument.Instrumentation;

/**
 * Main entry point for the IRBRE Java Agent.
 * This agent intercepts method calls and captures execution traces.
 */
public class IrbreAgent {


    private static EventCollector eventCollector;
    private static EventBatcher eventBatcher;
    private static EventSender eventSender;

    private static void logInfo(String msg) {
        System.out.println("[IRBRE] " + msg);
    }

    private static void logError(String msg, Throwable t) {
        System.err.println("[IRBRE ERROR] " + msg);
        t.printStackTrace();
    }
    /**
     * Premain method called by JVM when agent is attached at startup.
     *
     * @param agentArgs Arguments passed to the agent
     * @param inst Instrumentation instance
     */
    public static void premain(String agentArgs, Instrumentation inst) {
        logInfo("Starting IRBRE Agent...");

        try {
            // Load configuration
            AgentConfiguration config = AgentConfiguration.load();
            logInfo("Agent configuration loaded: " + config);

            // Initialize components
            eventCollector = EventCollector.getInstance();
            eventSender = new EventSender(config);
            eventBatcher = new EventBatcher(config, eventSender);

            // Set the batcher in the collector
            eventCollector.setBatcher(eventBatcher);

            // Add transformer
            IrbreTransformer transformer = new IrbreTransformer(config);
            inst.addTransformer(transformer, false);

            // Add shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logInfo("Shutting down IRBRE Agent...");
                try {
                    eventBatcher.flush();
                    eventBatcher.shutdown();
                    eventSender.shutdown();
                } catch (Exception e) {
                    logError("Error during agent shutdown", e);
                }
            }));

            logInfo("IRBRE Agent started successfully");
        } catch (Exception e) {
            logError("Failed to start IRBRE Agent", e);
            throw new RuntimeException("Agent initialization failed", e);
        }
    }

    public static EventCollector getEventCollector() {
        return eventCollector;
    }
}
