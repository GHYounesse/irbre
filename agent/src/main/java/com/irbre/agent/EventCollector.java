package com.irbre.agent;

import com.irbre.agent.model.EventType;
import com.irbre.agent.model.RequestMetadata;
import com.irbre.agent.model.TraceEvent;
import com.irbre.agent.util.LamportClock;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Collects trace events from instrumented code.
 * Thread-safe singleton that maintains context per thread.
 */
public class EventCollector {
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

    private static final EventCollector INSTANCE = new EventCollector();

    private final ThreadLocal<TraceContext> traceContext = ThreadLocal.withInitial(TraceContext::new);
    private final Map<String, RequestMetadata> activeTraces = new ConcurrentHashMap<>();
    private EventBatcher batcher;

    private EventCollector() {
    }

    public static EventCollector getInstance() {
        return INSTANCE;
    }

    public void setBatcher(EventBatcher batcher) {
        this.batcher = batcher;
    }

//    /**
//     * Called when an HTTP request starts (would be called by servlet filter in real app).
//     */
//    public void startRequest(String httpMethod, String url, Map<String, String> headers) {
//        TraceContext ctx = traceContext.get();
//        String traceId = UUID.randomUUID().toString();
//        ctx.traceId = traceId;
//        ctx.sequenceNumber = 0;
//        ctx.spanStack.clear(); // Clear any existing spans
//
//        RequestMetadata metadata = new RequestMetadata(httpMethod, url, headers);
//        activeTraces.put(traceId, metadata);
//
//        logDebug("Started trace: " + traceId);
//    }

    /**
     * Called when an HTTP request ends.
     */
    public void endRequest() {
        TraceContext ctx = traceContext.get();
        if (ctx.traceId != null) {
            activeTraces.remove(ctx.traceId);
            logDebug("Ended trace: " + ctx.traceId);
        }
        traceContext.remove();
    }

    /**
     * Called when a method is entered.
     */
//    public void onMethodEntry(String className, String methodName) {
//        try {
//            TraceContext ctx = traceContext.get();
//
//            // If no trace context, create one (for non-HTTP threads)
//            if (ctx.traceId == null) {
//                ctx.traceId = "thread-" + Thread.currentThread().getId();
//                ctx.sequenceNumber = 0;
//            }
//
//            RequestMetadata metadata = activeTraces.get(ctx.traceId);
//
//            // Get parent span ID from the top of the stack (current active span)
//            String parentSpanId = ctx.spanStack.isEmpty() ? null : ctx.spanStack.peek();
//
//            // Generate new span ID for this method
//            String currentSpanId = UUID.randomUUID().toString();
//
//            TraceEvent event = TraceEvent.builder()
//                    .traceId(ctx.traceId)
//                    .eventType(EventType.METHOD_ENTRY)
//                    .timestamp(Instant.now())
//                    .className(className)
//                    .methodName(methodName)
//                    .threadName(Thread.currentThread().getName())
//                    .threadId(Thread.currentThread().getId())
//                    .sequenceNumber(ctx.sequenceNumber++)
//                    .requestMetadata(metadata)
//                    .spanId(currentSpanId)
//                    .parentSpanId(parentSpanId)  // This will be null for root spans
//                    .lamportClock(LamportClock.next())
//                    .build();
//
//            // Push current span onto stack (it becomes the parent for nested calls)
//            ctx.spanStack.push(currentSpanId);
//
//            if (batcher != null) {
//                batcher.addEvent(event);
//            }
//        } catch (Exception e) {
//            // Never throw from instrumented code
//            logError("Error collecting method entry event", e);
//        }
//    }
    public void onMethodEntry(String className, String methodName) {
        try {
            TraceContext ctx = traceContext.get();

            if (ctx.traceId == null) {
                ctx.traceId = "thread-" + Thread.currentThread().getId();
                ctx.sequenceNumber = 0;
            }

            RequestMetadata metadata = activeTraces.get(ctx.traceId);

            // Get parent span ID:
            // - If this is the first span in a distributed trace, use distributedParentSpanId
            // - Otherwise use the top of the stack
            String parentSpanId = null;
            if (ctx.spanStack.isEmpty() && ctx.distributedParentSpanId != null) {
                parentSpanId = ctx.distributedParentSpanId;
            } else if (!ctx.spanStack.isEmpty()) {
                parentSpanId = ctx.spanStack.peek();
            }

            String currentSpanId = UUID.randomUUID().toString();

            TraceEvent event = TraceEvent.builder()
                    .traceId(ctx.traceId)
                    .eventType(EventType.METHOD_ENTRY)
                    .timestamp(Instant.now())
                    .className(className)
                    .methodName(methodName)
                    .threadName(Thread.currentThread().getName())
                    .threadId(Thread.currentThread().getId())
                    .sequenceNumber(ctx.sequenceNumber++)
                    .requestMetadata(metadata)
                    .spanId(currentSpanId)
                    .parentSpanId(parentSpanId)  // Now includes distributed parent
                    .lamportClock(LamportClock.next())
                    .build();

            ctx.spanStack.push(currentSpanId);

            if (batcher != null) {
                batcher.addEvent(event);
            }
        } catch (Exception e) {
            logError("Error collecting method entry event", e);
        }
    }
    /**
     * Called when a method exits (ADD THIS METHOD).
     * This is crucial for maintaining the span hierarchy.
     */
    public void onMethodExit(String className, String methodName) {
        try {
            TraceContext ctx = traceContext.get();

            if (ctx.traceId == null || ctx.spanStack.isEmpty()) {
                return; // No active trace or mismatched entry/exit
            }

            // Pop the current span from the stack
            String currentSpanId = ctx.spanStack.pop();

            RequestMetadata metadata = activeTraces.get(ctx.traceId);
            String parentSpanId = ctx.spanStack.isEmpty() ? null : ctx.spanStack.peek();

            TraceEvent event = TraceEvent.builder()
                    .traceId(ctx.traceId)
                    .eventType(EventType.METHOD_EXIT)
                    .timestamp(Instant.now())
                    .className(className)
                    .methodName(methodName)
                    .threadName(Thread.currentThread().getName())
                    .threadId(Thread.currentThread().getId())
                    .sequenceNumber(ctx.sequenceNumber++)
                    .requestMetadata(metadata)
                    .spanId(currentSpanId)
                    .parentSpanId(parentSpanId)
                    .lamportClock(LamportClock.next())
                    .build();

            if (batcher != null) {
                batcher.addEvent(event);
            }
        } catch (Exception e) {
            logError("Error collecting method exit event", e);
        }
    }

    /**
     * Called when an exception is thrown.
     */
    public void onException(String className, String methodName, Throwable exception) {
        try {
            TraceContext ctx = traceContext.get();

            if (ctx.traceId == null) {
                return; // No active trace
            }

            RequestMetadata metadata = activeTraces.get(ctx.traceId);

            // Get current span (the one where exception occurred)
            String currentSpanId = ctx.spanStack.isEmpty() ? null : ctx.spanStack.peek();

            // Parent is the one below current in stack
            String parentSpanId = null;
            if (ctx.spanStack.size() > 1) {
                // Temporarily pop to see parent
                String temp = ctx.spanStack.pop();
                parentSpanId = ctx.spanStack.isEmpty() ? null : ctx.spanStack.peek();
                ctx.spanStack.push(temp); // Push back
            }

            TraceEvent event = TraceEvent.builder()
                    .traceId(ctx.traceId)
                    .eventType(EventType.EXCEPTION)
                    .timestamp(Instant.now())
                    .className(className)
                    .methodName(methodName)
                    .threadName(Thread.currentThread().getName())
                    .threadId(Thread.currentThread().getId())
                    .sequenceNumber(ctx.sequenceNumber++)
                    .exceptionType(exception.getClass().getName())
                    .exceptionMessage(exception.getMessage())
                    .requestMetadata(metadata)
                    .spanId(currentSpanId)
                    .parentSpanId(parentSpanId)
                    .lamportClock(LamportClock.next())
                    .build();

            if (batcher != null) {
                batcher.addEvent(event);
            }
        } catch (Exception e) {
            logError("Error collecting exception event", e);
        }
    }
    /**
     * Enhanced startRequest that accepts incoming correlation IDs for distributed tracing.
     */
    public void startRequest(String httpMethod, String url, Map<String, String> headers,
                             String incomingTraceId, String incomingParentSpanId) {
        TraceContext ctx = traceContext.get();

        // Use incoming trace ID if present (for distributed tracing), otherwise create new
        String traceId = (incomingTraceId != null && !incomingTraceId.isEmpty())
                ? incomingTraceId
                : UUID.randomUUID().toString();

        ctx.traceId = traceId;
        ctx.sequenceNumber = 0;
        ctx.spanStack.clear();

        // If there's an incoming parent span ID, it means this request is part of a larger trace
        if (incomingParentSpanId != null && !incomingParentSpanId.isEmpty()) {
            ctx.distributedParentSpanId = incomingParentSpanId;
        }

        RequestMetadata metadata = new RequestMetadata(httpMethod, url, headers);
        activeTraces.put(traceId, metadata);

        logDebug("Started trace: " + traceId +
                (incomingTraceId != null ? " (propagated)" : " (new)"));
    }

    /**
     * Backward compatible startRequest.
     */
    public void startRequest(String httpMethod, String url, Map<String, String> headers) {
        startRequest(httpMethod, url, headers, null, null);
    }

    /**
     * Get current trace ID (useful for response headers).
     */
    public String getCurrentTraceId() {
        TraceContext ctx = traceContext.get();
        return ctx.traceId;
    }

    /**
     * Get current span ID (useful for outgoing HTTP calls).
     */
    public String getCurrentSpanId() {
        TraceContext ctx = traceContext.get();
        return ctx.spanStack.isEmpty() ? null : ctx.spanStack.peek();
    }

    /**
     * Context maintained per thread.
     * Now includes a stack to track the span hierarchy.
     */
    private static class TraceContext {
        String traceId;
        int sequenceNumber;
        Deque<String> spanStack = new ArrayDeque<>();
        String distributedParentSpanId;  // ADD THIS: For cross-service tracing
    }
}