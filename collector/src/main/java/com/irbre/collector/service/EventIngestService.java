package com.irbre.collector.service;

import com.irbre.collector.dto.EventBatchDto;
import com.irbre.collector.dto.RequestMetadataDto;
import com.irbre.collector.dto.TraceEventDto;
import com.irbre.collector.entity.RequestMetadata;
import com.irbre.collector.entity.Trace;
import com.irbre.collector.entity.TraceEvent;
import com.irbre.collector.repository.TraceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for ingesting trace event batches from agents.
 */
@Service
public class EventIngestService {
    private static final Logger logger = LoggerFactory.getLogger(EventIngestService.class);

    private final TraceRepository traceRepository;
    @Autowired
    private TraceIndexer traceIndexer;

    public EventIngestService(TraceRepository traceRepository) {
        this.traceRepository = traceRepository;
    }

    /**
     * Ingest a batch of trace events.
     */
//    @Transactional
//    public void ingestBatch(EventBatchDto batchDto) {
//        if (batchDto == null || batchDto.getEvents() == null || batchDto.getEvents().isEmpty()) {
//            logger.warn("Received empty batch");
//            return;
//        }
//
//        logger.info("Ingesting batch of {} events", batchDto.getEvents().size());
//
//        // Group events by trace ID
//        Map<String, Trace> traceMap = new HashMap<>();
//
//        for (TraceEventDto eventDto : batchDto.getEvents()) {
//            String traceId = eventDto.getTraceId();
//
//            if (traceId == null || traceId.isEmpty()) {
//                logger.warn("Skipping event with null/empty traceId");
//                continue;
//            }
//
//            // Get or create trace
//            Trace trace = traceMap.computeIfAbsent(traceId, tid -> {
//                return traceRepository.findByTraceId(tid)
//                        .orElseGet(() -> {
//                            Trace newTrace = new Trace(tid);
//                            logger.debug("Creating new trace: {}", tid);
//                            return newTrace;
//                        });
//            });
//
//            // Convert DTO to entity
//            TraceEvent event = convertToEntity(eventDto);
//            trace.addEvent(event);
//
//            // Set request metadata if present and not already set
//            if (eventDto.getRequestMetadata() != null && trace.getRequestMetadata() == null) {
//                RequestMetadata metadata = convertMetadataToEntity(eventDto.getRequestMetadata());
//                trace.setRequestMetadata(metadata);
//            }
//        }
//
//        // Save all traces
//        for (Trace trace : traceMap.values()) {
//            traceRepository.save(trace);
//            logger.debug("Saved trace {} with {} events", trace.getTraceId(), trace.getEventCount());
//        }
//
//        logger.info("Successfully ingested batch into {} traces", traceMap.size());
//    }
    @Transactional
    public void ingestBatch(EventBatchDto batchDto) {
        String traceId = batchDto.getTraceId();

        // Find or create trace
        Trace trace = traceRepository.findByTraceId(traceId)
                .orElseGet(() -> createNewTrace(batchDto));

        // Save events
        List<TraceEvent> events = new ArrayList<>();
        for (TraceEventDto eventDto : batchDto.getEvents()) {
            TraceEvent event = traceMapper.toEntity(eventDto);
            event.setTrace(trace);
            events.add(event);
        }
        traceEventRepository.saveAll(events);

        // Update trace metadata using indexer
        updateTraceMetadata(trace, traceEventRepository.findByTrace(trace));

        traceRepository.save(trace);
    }

    private void updateTraceMetadata(Trace trace, List<TraceEvent> allEvents) {
        if (allEvents.isEmpty()) return;

        // Find root span
        List<String> rootSpans = traceIndexer.findRootSpans(allEvents);
        if (!rootSpans.isEmpty()) {
            trace.setRootSpanId(rootSpans.get(0));
        }

        // Calculate depth
        int depth = traceIndexer.calculateTraceDepth(allEvents);
        // You can add a depth field to Trace entity if needed

        // Check if distributed
        boolean isDistributed = traceIndexer.isDistributedTrace(allEvents);
        trace.setDistributedTrace(isDistributed);

        // Update timestamps
        allEvents.sort(Comparator.comparing(TraceEvent::getTimestamp));
        trace.setStartTime(allEvents.get(0).getTimestamp());
        trace.setEndTime(allEvents.get(allEvents.size() - 1).getTimestamp());
    }
    /**
     * Convert TraceEventDto to TraceEvent entity.
     */
    private TraceEvent convertToEntity(TraceEventDto dto) {
        TraceEvent event = new TraceEvent();
        event.setEventType(convertEventType(dto.getEventType()));
        event.setTimestamp(dto.getTimestamp());
        event.setClassName(dto.getClassName());
        event.setMethodName(dto.getMethodName());
        event.setThreadName(dto.getThreadName());
        event.setThreadId(dto.getThreadId());
        event.setSequenceNumber(dto.getSequenceNumber());
        event.setExceptionType(dto.getExceptionType());
        event.setExceptionMessage(dto.getExceptionMessage());
        return event;
    }

    /**
     * Convert event type string to enum.
     */
    private TraceEvent.EventType convertEventType(String typeStr) {
        if (typeStr == null) {
            return TraceEvent.EventType.METHOD_ENTRY;
        }
        try {
            return TraceEvent.EventType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            logger.warn("Unknown event type: {}, defaulting to METHOD_ENTRY", typeStr);
            return TraceEvent.EventType.METHOD_ENTRY;
        }
    }

    /**
     * Convert RequestMetadataDto to RequestMetadata entity.
     */
    private RequestMetadata convertMetadataToEntity(RequestMetadataDto dto) {
        RequestMetadata metadata = new RequestMetadata();
        metadata.setHttpMethod(dto.getHttpMethod());
        metadata.setUrl(dto.getUrl());
        metadata.setHeaders(dto.getHeaders() != null ? new HashMap<>(dto.getHeaders()) : new HashMap<>());
        return metadata;
    }
}
