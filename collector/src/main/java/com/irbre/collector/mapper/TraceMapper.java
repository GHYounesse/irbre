package com.irbre.collector.mapper;

import com.irbre.collector.dto.*;
import com.irbre.collector.entity.RequestMetadata;
import com.irbre.collector.entity.Trace;
import com.irbre.collector.entity.TraceEvent;
import org.springframework.stereotype.Component;

import java.util.HashMap;

/**
 * Mapper for converting between entities and DTOs.
 */
@Component
public class TraceMapper {

    /**
     * Convert Trace entity to full TraceResponseDto.
     */
    public TraceResponseDto toDto(Trace trace) {
        TraceResponseDto dto = new TraceResponseDto();
        dto.setTraceId(trace.getTraceId());
        dto.setCreatedAt(trace.getCreatedAt());
        dto.setUpdatedAt(trace.getUpdatedAt());
        dto.setEventCount(trace.getEventCount());

        if (trace.getRequestMetadata() != null) {
            dto.setRequestMetadata(toDto(trace.getRequestMetadata()));
        }

        dto.setEvents(trace.getEvents().stream()
                .map(this::toDto)
                .toList());

        return dto;
    }

    /**
     * Convert Trace entity to TraceSummaryDto.
     */
    public TraceSummaryDto toSummaryDto(Trace trace) {
        TraceSummaryDto dto = new TraceSummaryDto();
        dto.setTraceId(trace.getTraceId());
        dto.setCreatedAt(trace.getCreatedAt());
        dto.setUpdatedAt(trace.getUpdatedAt());
        dto.setEventCount(trace.getEventCount());

        if (trace.getRequestMetadata() != null) {
            dto.setHttpMethod(trace.getRequestMetadata().getHttpMethod());
            dto.setUrl(trace.getRequestMetadata().getUrl());
        }

        return dto;
    }

    /**
     * Convert TraceEvent entity to TraceEventDto.
     */
    public TraceEventDto toDto(TraceEvent event) {
        TraceEventDto dto = new TraceEventDto();
        dto.setTraceId(event.getTrace().getTraceId());
        dto.setEventType(event.getEventType().name());
        dto.setTimestamp(event.getTimestamp());
        dto.setClassName(event.getClassName());
        dto.setMethodName(event.getMethodName());
        dto.setThreadName(event.getThreadName());
        dto.setThreadId(event.getThreadId());
        dto.setSequenceNumber(event.getSequenceNumber());
        dto.setExceptionType(event.getExceptionType());
        dto.setExceptionMessage(event.getExceptionMessage());
        return dto;
    }

    /**
     * Convert RequestMetadata entity to RequestMetadataDto.
     */
    public RequestMetadataDto toDto(RequestMetadata metadata) {
        RequestMetadataDto dto = new RequestMetadataDto();
        dto.setHttpMethod(metadata.getHttpMethod());
        dto.setUrl(metadata.getUrl());
        dto.setHeaders(metadata.getHeaders() != null ? new HashMap<>(metadata.getHeaders()) : new HashMap<>());
        return dto;
    }
}