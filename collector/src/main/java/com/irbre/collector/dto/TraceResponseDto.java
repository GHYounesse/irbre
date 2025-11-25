package com.irbre.collector.dto;


import java.time.Instant;
import java.util.List;
import java.util.Map;

public class TraceResponseDto {

    private String traceId;
    private Instant createdAt;
    private Instant updatedAt;
    private int eventCount;

    private RequestMetadataDto requestMetadata;

    private List<TraceEventDto> events;
    private String rootSpanId;
    private Boolean distributedTrace;
    private Integer traceDepth;
    private Map<String, List<String>> spanHierarchy;

}

