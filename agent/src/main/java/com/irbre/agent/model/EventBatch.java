package com.irbre.agent.model;

import java.util.Collections;
import java.util.List;

/**
 * Represents a batch of TraceEvent objects ready to be sent to the collector.
 */
public class EventBatch {

    private final List<TraceEvent> events;

    public EventBatch(List<TraceEvent> events) {
        // Store an unmodifiable copy for thread safety
        this.events = Collections.unmodifiableList(events);
    }

    public List<TraceEvent> getEvents() {
        return events;
    }

    public int size() {
        return events.size();
    }

    @Override
    public String toString() {
        return "EventBatch{" +
                "size=" + events.size() +
                '}';
    }
}
