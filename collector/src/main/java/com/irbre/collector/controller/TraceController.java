package com.irbre.collector.controller;

import com.irbre.collector.dto.EventBatchDto;
import com.irbre.collector.dto.TraceListDto;
import com.irbre.collector.dto.TraceResponseDto;
import com.irbre.collector.entity.Trace;
import com.irbre.collector.entity.TraceEvent;
import com.irbre.collector.service.EventIngestService;
import com.irbre.collector.service.TraceIndexer;
import com.irbre.collector.service.TraceService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for trace operations.
 */
@RestController
@RequestMapping("/api/v1/traces")
public class TraceController {
    private static final Logger logger = LoggerFactory.getLogger(TraceController.class);

    private final EventIngestService eventIngestService;
    private final TraceService traceService;

    public TraceController(EventIngestService eventIngestService, TraceService traceService) {
        this.eventIngestService = eventIngestService;
        this.traceService = traceService;
    }

    /**
     * Ingest a batch of trace events from agents.
     * POST /api/v1/traces/ingest
     */
    @PostMapping("/ingest")
    public ResponseEntity<String> ingestEvents(@Valid @RequestBody EventBatchDto batchDto) {
        logger.info("Received batch with {} events",
                batchDto.getEvents() != null ? batchDto.getEvents().size() : 0);

        try {
            eventIngestService.ingestBatch(batchDto);
            return ResponseEntity.ok("Batch ingested successfully");
        } catch (Exception e) {
            logger.error("Error ingesting batch", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error ingesting batch: " + e.getMessage());
        }
    }

    /**
     * Get a specific trace by trace ID.
     * GET /api/v1/traces/{traceId}
     */
    @GetMapping("/{traceId}")
    public ResponseEntity<TraceResponseDto> getTrace(@PathVariable String traceId) {
        logger.info("Fetching trace: {}", traceId);
        TraceResponseDto trace = traceService.getTrace(traceId);
        return ResponseEntity.ok(trace);
    }

    /**
     * List all traces with pagination.
     * GET /api/v1/traces?page=0&size=20
     */
    @GetMapping
    public ResponseEntity<TraceListDto> listTraces(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        logger.info("Listing traces: page={}, size={}", page, size);

        if (size > 100) {
            size = 100; // Max page size
        }

        TraceListDto traces = traceService.listTraces(page, size);
        return ResponseEntity.ok(traces);
    }




}