package com.irbre.agent.model;

import java.time.Instant;

/**
 * Represents a single trace event captured by the agent.
 */
public class TraceEvent {
    private String traceId;
    private EventType eventType;
    private Instant timestamp;
    private String className;
    private String methodName;
    private String threadName;
    private Long threadId;
    private Integer sequenceNumber;
    private String exceptionType;
    private String exceptionMessage;
    private RequestMetadata requestMetadata;
    //Unique per event
    private String spanId;
    //For hierarchical traces
    private String parentSpanId;
    //Monotonic ordering across threads/machines.
    private Long lamportClock;


    public TraceEvent() {
    }

    private TraceEvent(Builder builder) {
        this.traceId = builder.traceId;
        this.eventType = builder.eventType;
        this.timestamp = builder.timestamp;
        this.className = builder.className;
        this.methodName = builder.methodName;
        this.threadName = builder.threadName;
        this.threadId = builder.threadId;
        this.sequenceNumber = builder.sequenceNumber;
        this.exceptionType = builder.exceptionType;
        this.exceptionMessage = builder.exceptionMessage;
        this.requestMetadata = builder.requestMetadata;
        this.spanId = builder.spanId;
        this.parentSpanId = builder.parentSpanId;
        this.lamportClock = builder.lamportClock;

    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters and Setters
    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
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

    public RequestMetadata getRequestMetadata() {
        return requestMetadata;
    }

    public void setRequestMetadata(RequestMetadata requestMetadata) {
        this.requestMetadata = requestMetadata;
    }
    public String getSpanId() {
        return spanId;
    }

    public void setSpanId(String spanId) {
        this.spanId = spanId;
    }

    public String getParentSpanId() {
        return parentSpanId;
    }

    public void setParentSpanId(String parentSpanId) {
        this.parentSpanId = parentSpanId;
    }

    public Long getLamportClock() {
        return lamportClock;
    }

    public void setLamportClock(Long lamportClock) {
        this.lamportClock = lamportClock;
    }


    public static class Builder {
        private String traceId;
        private EventType eventType;
        private Instant timestamp;
        private String className;
        private String methodName;
        private String threadName;
        private Long threadId;
        private Integer sequenceNumber;
        private String exceptionType;
        private String exceptionMessage;
        private RequestMetadata requestMetadata;

        public Builder traceId(String traceId) {
            this.traceId = traceId;
            return this;
        }

        public Builder eventType(EventType eventType) {
            this.eventType = eventType;
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder className(String className) {
            this.className = className;
            return this;
        }

        public Builder methodName(String methodName) {
            this.methodName = methodName;
            return this;
        }

        public Builder threadName(String threadName) {
            this.threadName = threadName;
            return this;
        }

        public Builder threadId(Long threadId) {
            this.threadId = threadId;
            return this;
        }

        public Builder sequenceNumber(Integer sequenceNumber) {
            this.sequenceNumber = sequenceNumber;
            return this;
        }

        public Builder exceptionType(String exceptionType) {
            this.exceptionType = exceptionType;
            return this;
        }

        public Builder exceptionMessage(String exceptionMessage) {
            this.exceptionMessage = exceptionMessage;
            return this;
        }

        public Builder requestMetadata(RequestMetadata requestMetadata) {
            this.requestMetadata = requestMetadata;
            return this;
        }
        private String spanId;
        private String parentSpanId;
        private Long lamportClock;

        public Builder spanId(String spanId) {
            this.spanId = spanId;
            return this;
        }

        public Builder parentSpanId(String parentSpanId) {
            this.parentSpanId = parentSpanId;
            return this;
        }

        public Builder lamportClock(Long lamportClock) {
            this.lamportClock = lamportClock;
            return this;
        }

        public TraceEvent build() {
            return new TraceEvent(this);
        }
    }
}
