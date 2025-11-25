package com.irbre.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.irbre.agent.model.EventBatch;


import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * Sends event batches to the collector service asynchronously.
 */
public class EventSender {

    private static void logInfo(String msg) {
        System.out.println("[IRBRE] " + msg);
    }
    private static void logDebug(String msg) {
        System.out.println("[IRBRE DEBUG] " + msg);
    }

    private static void logError(String msg, Throwable t) {
        System.err.println("[IRBRE ERROR] " + msg);
        t.printStackTrace();
    }
    private final AgentConfiguration config;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public EventSender(AgentConfiguration config) {
        this.config = config;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Send a batch of events to the collector asynchronously.
     */
    public CompletableFuture<Void> sendBatch(EventBatch batch) {
        try {
            String json = objectMapper.writeValueAsString(batch);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(config.getCollectorUrl()))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .timeout(Duration.ofSeconds(30))
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        if (response.statusCode() >= 200 && response.statusCode() < 300) {
                            logDebug("Successfully sent batch to collector");
                        } else {
                            logDebug("Failed to send batch, status: "+response.statusCode()+", body: "+response.body());
                        }
                    })
                    .exceptionally(ex -> {
                        logError("Error sending batch to collector", ex);
                        return null;
                    });
        } catch (Exception e) {
            logError("Error preparing batch for sending", e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Shutdown the HTTP client.
     */
    public void shutdown() {
        logInfo("Shutting down event sender...");
        // HttpClient doesn't need explicit shutdown in Java 11+
    }
}
