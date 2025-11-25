package com.irbre.collector.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for a batch of trace events received from agents.
 * Used in the POST /api/v1/traces/ingest endpoint.
 */
public class EventBatchDto {

    @NotNull(message = "Events list cannot be null")
    @Valid
    private List<TraceEventDto> events = new ArrayList<>();

    public EventBatchDto() {
    }

    public EventBatchDto(List<TraceEventDto> events) {
        this.events = events != null ? new ArrayList<>(events) : new ArrayList<>();
    }

    // Getters and Setters
    public List<TraceEventDto> getEvents() {
        return events;
    }

    public void setEvents(List<TraceEventDto> events) {
        this.events = events;
    }

    @Override
    public String toString() {
        return "EventBatchDto{" +
                "events=" + (events != null ? events.size() : 0) +
                '}';
    }
}