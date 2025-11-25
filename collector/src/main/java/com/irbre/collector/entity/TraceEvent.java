package com.irbre.collector.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Entity representing a single trace event in the collector database.
 */
@Entity
@Table(name = "trace_events", indexes = {
        @Index(name = "idx_trace_id", columnList = "trace_id"),
        @Index(name = "idx_sequence", columnList = "trace_id, sequenceNumber"),
        @Index(name = "idx_timestamp", columnList = "timestamp"),
        @Index(name = "idx_span_id", columnList = "span_id"),
        @Index(name = "idx_parent_span_id", columnList = "parent_span_id"),
        @Index(name = "idx_trace_timestamp", columnList = "trace_id,timestamp"),
        @Index(name = "idx_lamport", columnList = "trace_id,lamport_clock")
})
public class TraceEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trace_id", nullable = false)
    private Trace trace;

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private EventType eventType;

    @Column(nullable = false)
    private Instant timestamp;

    @Column(length = 500)
    private String className;

    @Column(length = 200)
    private String methodName;

    @Column(length = 200)
    private String threadName;

    @Column
    private Long threadId;

    @Column(nullable = false)
    private Integer sequenceNumber;

    @Column(length = 500)
    private String exceptionType;

    @Column(length = 2000)
    private String exceptionMessage;

    @Column(name = "span_id")
    private String spanId;

    @Column(name = "parent_span_id")
    private String parentSpanId;

    @Column(name = "lamport_clock")
    private Long lamportClock;

    public TraceEvent() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Trace getTrace() {
        return trace;
    }

    public void setTrace(Trace trace) {
        this.trace = trace;
    }


    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
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

    /**
     * Event type enum for collector.
     */
    public enum EventType {
        METHOD_ENTRY,
        METHOD_EXIT,
        EXCEPTION,
        HTTP_REQUEST
    }
}