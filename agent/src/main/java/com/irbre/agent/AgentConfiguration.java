package com.irbre.agent;




import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Configuration for the IRBRE Agent.
 */
public class AgentConfiguration {
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

    private static final String DEFAULT_COLLECTOR_URL = "http://localhost:8080/api/v1/traces/ingest";
    private static final int DEFAULT_BATCH_SIZE = 50;
    private static final long DEFAULT_BATCH_INTERVAL_MS = 5000;
    private static final int DEFAULT_QUEUE_SIZE = 10000;

    private final String collectorUrl;
    private final int batchSize;
    private final long batchIntervalMs;
    private final int queueSize;
    private final Set<String> includedPackages;
    private final Set<String> excludedPackages;

    public AgentConfiguration(String collectorUrl, int batchSize, long batchIntervalMs,
                              int queueSize, Set<String> includedPackages, Set<String> excludedPackages) {
        this.collectorUrl = collectorUrl;
        this.batchSize = batchSize;
        this.batchIntervalMs = batchIntervalMs;
        this.queueSize = queueSize;
        this.includedPackages = includedPackages;
        this.excludedPackages = excludedPackages;
    }

    /**
     * Load configuration from agent.properties file.
     */
    public static AgentConfiguration load() {
        Properties props = new Properties();

        try (InputStream is = AgentConfiguration.class.getClassLoader()
                .getResourceAsStream("agent.properties")) {
            if (is != null) {
                props.load(is);
                logInfo("Loaded agent.properties");
            } else {
                logWarn("agent.properties not found, using defaults");
            }
        } catch (IOException e) {
            logWarn("Error loading agent.properties, using defaults "+ e);
        }

        String collectorUrl = props.getProperty("irbre.collector.url", DEFAULT_COLLECTOR_URL);
        int batchSize = Integer.parseInt(props.getProperty("irbre.batch.size", String.valueOf(DEFAULT_BATCH_SIZE)));
        long batchIntervalMs = Long.parseLong(props.getProperty("irbre.batch.interval.ms", String.valueOf(DEFAULT_BATCH_INTERVAL_MS)));
        int queueSize = Integer.parseInt(props.getProperty("irbre.queue.size", String.valueOf(DEFAULT_QUEUE_SIZE)));

        // Parse included packages
        Set<String> includedPackages = new HashSet<>();
        String includedProp = props.getProperty("irbre.include.packages", "");
        if (!includedProp.isEmpty()) {
            for (String pkg : includedProp.split(",")) {
                includedPackages.add(pkg.trim());
            }
        }

        // Parse excluded packages (with defaults)
        Set<String> excludedPackages = new HashSet<>();
        String excludedProp = props.getProperty("irbre.exclude.packages",
                "java.,javax.,sun.,com.sun.,jdk.,org.slf4j.,ch.qos.logback.,com.irbre.agent.");
        for (String pkg : excludedProp.split(",")) {
            excludedPackages.add(pkg.trim());
        }

        return new AgentConfiguration(collectorUrl, batchSize, batchIntervalMs, queueSize,
                includedPackages, excludedPackages);
    }

    public String getCollectorUrl() {
        return collectorUrl;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public long getBatchIntervalMs() {
        return batchIntervalMs;
    }

    public int getQueueSize() {
        return queueSize;
    }

    public Set<String> getIncludedPackages() {
        return includedPackages;
    }

    public Set<String> getExcludedPackages() {
        return excludedPackages;
    }

    public boolean shouldInstrument(String className) {
        // Check excluded packages first
        if (this.includedPackages.isEmpty()) {
            return className.startsWith("com.example.");
        }
        for (String excluded : excludedPackages) {
            if (className.startsWith(excluded)) {
                return false;
            }
        }

        // If no included packages specified, instrument everything not excluded
        if (includedPackages.isEmpty()) {
            return true;
        }

        // Check if class is in included packages
        for (String included : includedPackages) {
            if (className.startsWith(included)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String toString() {
        return "AgentConfiguration{" +
                "collectorUrl='" + collectorUrl + '\'' +
                ", batchSize=" + batchSize +
                ", batchIntervalMs=" + batchIntervalMs +
                ", queueSize=" + queueSize +
                ", includedPackages=" + includedPackages +
                ", excludedPackages=" + excludedPackages +
                '}';
    }
}