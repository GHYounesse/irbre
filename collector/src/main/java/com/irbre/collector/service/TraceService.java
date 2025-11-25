package com.irbre.collector.service;

import com.irbre.collector.dto.TraceListDto;
import com.irbre.collector.dto.TraceResponseDto;
import com.irbre.collector.entity.Trace;
import com.irbre.collector.exception.TraceNotFoundException;
import com.irbre.collector.mapper.TraceMapper;
import com.irbre.collector.repository.TraceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for retrieving and managing traces in the collector.
 */
@Service
public class TraceService {
    private static final Logger logger = LoggerFactory.getLogger(TraceService.class);

    private final TraceRepository traceRepository;
    private final TraceMapper traceMapper;

    public TraceService(TraceRepository traceRepository, TraceMapper traceMapper) {
        this.traceRepository = traceRepository;
        this.traceMapper = traceMapper;
    }

    /**
     * Get a trace by its trace ID with all events and metadata.
     *
     * @param traceId The unique trace identifier
     * @return TraceResponseDto containing full trace details
     * @throws TraceNotFoundException if trace doesn't exist
     */
    @Transactional(readOnly = true)
    public TraceResponseDto getTrace(String traceId) {
        logger.debug("Fetching trace: {}", traceId);

        Trace trace = traceRepository.findByTraceId(traceId)
                .orElseThrow(() -> new TraceNotFoundException("Trace not found: " + traceId));

        // Force load lazy collections
        trace.getEvents().size();
        if (trace.getRequestMetadata() != null) {
            trace.getRequestMetadata().getHeaders().size();
        }

        return traceMapper.toDto(trace);
    }

    /**
     * List all traces with pagination.
     *
     * @param page Page number (0-indexed)
     * @param size Page size
     * @return TraceListDto containing paginated trace summaries
     */
    @Transactional(readOnly = true)
    public TraceListDto listTraces(int page, int size) {
        logger.debug("Listing traces: page={}, size={}", page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<Trace> tracePage = traceRepository.findAllOrderByCreatedAtDesc(pageable);

        TraceListDto dto = new TraceListDto();
        dto.setTraces(tracePage.getContent().stream()
                .map(trace -> {
                    // Load request metadata if present
                    if (trace.getRequestMetadata() != null) {
                        trace.getRequestMetadata().getHttpMethod();
                    }
                    return traceMapper.toSummaryDto(trace);
                })
                .toList());
        dto.setTotalElements(tracePage.getTotalElements());
        dto.setTotalPages(tracePage.getTotalPages());
        dto.setCurrentPage(page);
        dto.setPageSize(size);

        return dto;
    }

    /**
     * Check if a trace exists by trace ID.
     *
     * @param traceId The unique trace identifier
     * @return true if trace exists, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean traceExists(String traceId) {
        return traceRepository.existsByTraceId(traceId);
    }

    /**
     * Get count of all traces in the system.
     *
     * @return Total number of traces
     */
    @Transactional(readOnly = true)
    public long getTraceCount() {
        return traceRepository.count();
    }

    /**
     * Delete a trace by trace ID.
     *
     * @param traceId The unique trace identifier
     * @throws TraceNotFoundException if trace doesn't exist
     */
    @Transactional
    public void deleteTrace(String traceId) {
        logger.info("Deleting trace: {}", traceId);

        Trace trace = traceRepository.findByTraceId(traceId)
                .orElseThrow(() -> new TraceNotFoundException("Trace not found: " + traceId));

        traceRepository.delete(trace);
        logger.info("Trace deleted: {}", traceId);
    }
}