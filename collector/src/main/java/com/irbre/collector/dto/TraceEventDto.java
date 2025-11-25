package com.irbre.collector.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

/**
 * DTO for a single trace event used in the collector API.
 */
public class TraceEventDto {

    @NotBlank(message = "Trace ID cannot be blank")
    private String traceId;

    @NotBlank(message = "Event type cannot be blank")
    private String eventType;

    @NotNull(message = "Timestamp cannot be null")
    private Instant timestamp;

    private String className;
    private String methodName;
    private String threadName;
    private Long threadId;

    @NotNull(message = "Sequence number cannot be null")
    private Integer sequenceNumber;

    private String exceptionType;
    private String exceptionMessage;
    private RequestMetadataDto requestMetadata;
    private String spanId;
    private String parentSpanId;
    private Long lamportClock;

    public TraceEventDto() {
    }

    // Getters and Setters
    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public Long getThreadId() {
        return threadId;
    }

    public void setThreadId(Long threadId) {
        this.threadId = threadId;
    }

    public Integer getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(Integer sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public String getExceptionType() {
        return exceptionType;
    }

    public void setExceptionType(String exceptionType) {
        this.exceptionType = exceptionType;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }

    public RequestMetadataDto getRequestMetadata() {
        return requestMetadata;
    }

    public void setRequestMetadata(RequestMetadataDto requestMetadata) {
        this.requestMetadata = requestMetadata;
    }

    @Override
    public String toString() {
        return "TraceEventDto{" +
                "traceId='" + traceId + '\'' +
                ", eventType='" + eventType + '\'' +
                ", timestamp=" + timestamp +
                ", className='" + className + '\'' +
                ", methodName='" + methodName + '\'' +
                ", sequenceNumber=" + sequenceNumber +
                '}';
    }
}