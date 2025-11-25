package com.irbre.collector.entity;



import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a complete trace (group of related events).
 */
@Entity
@Table(name = "traces", indexes = {
        @Index(name = "idx_trace_id", columnList = "traceId"),
        @Index(name = "idx_created_at", columnList = "createdAt")
})
public class Trace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String traceId;

    @Column(nullable = false)
    private Instant createdAt;

    @Column
    private Instant updatedAt;

    @OneToMany(mappedBy = "trace", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("sequenceNumber ASC")
    private List<TraceEvent> events = new ArrayList<>();

    @OneToOne(mappedBy = "trace", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private RequestMetadata requestMetadata;

    @Column
    private Integer eventCount = 0;

    @Column(name = "root_span_id")
    private String rootSpanId;  // First span in the trace

    @Column(name = "distributed_trace")
    private Boolean distributedTrace = false;  // Cross-service trace?

    @Column(name = "service_count")
    private Integer serviceCount = 1;  // Number of services involved

    public Trace() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public Trace(String traceId) {
        this();
        this.traceId = traceId;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public void addEvent(TraceEvent event) {
        events.add(event);
        event.setTrace(this);
        this.eventCount = events.size();
    }

    public void removeEvent(TraceEvent event) {
        events.remove(event);
        event.setTrace(null);
        this.eventCount = events.size();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<TraceEvent> getEvents() {
        return events;
    }

    public void setEvents(List<TraceEvent> events) {
        this.events = events;
    }

    public RequestMetadata getRequestMetadata() {
        return requestMetadata;
    }

    public void setRequestMetadata(RequestMetadata requestMetadata) {
        this.requestMetadata = requestMetadata;
        if (requestMetadata != null) {
            requestMetadata.setTrace(this);
        }
    }

    public Integer getEventCount() {
        return eventCount;
    }

    public void setEventCount(Integer eventCount) {
        this.eventCount = eventCount;
    }
}
