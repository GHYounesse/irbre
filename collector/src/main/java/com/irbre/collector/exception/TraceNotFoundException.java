package com.irbre.collector.exception;

/**
 * Exception thrown when a requested trace cannot be found in the collector database.
 */
public class TraceNotFoundException extends RuntimeException {

    public TraceNotFoundException(String message) {
        super(message);
    }

    public TraceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public TraceNotFoundException(String traceId, Throwable cause, boolean forTraceId) {
        super("Trace not found: " + traceId, cause);
    }
}