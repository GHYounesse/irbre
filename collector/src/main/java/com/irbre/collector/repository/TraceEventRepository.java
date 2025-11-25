package com.irbre.collector.repository;

import com.irbre.collector.entity.TraceEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for TraceEvent entities in the collector.
 */
@Repository
public interface TraceEventRepository extends JpaRepository<TraceEvent, Long> {

    /**
     * Find all events for a specific trace, ordered by sequence number.
     * @param traceId The internal database ID of the trace
     * @return List of trace events ordered by sequence
     */
    @Query("SELECT te FROM TraceEvent te WHERE te.trace.id = :traceId ORDER BY te.sequenceNumber ASC")
    List<TraceEvent> findByTraceIdOrderBySequenceNumberAsc(@Param("traceId") Long traceId);

    /**
     * Find all events for a trace by trace ID string, ordered by sequence number.
     * @param traceId The trace ID string
     * @return List of trace events ordered by sequence
     */
    @Query("SELECT te FROM TraceEvent te WHERE te.trace.traceId = :traceId ORDER BY te.sequenceNumber ASC")
    List<TraceEvent> findByTraceTraceIdOrderBySequenceNumberAsc(@Param("traceId") String traceId);

    /**
     * Count events for a specific trace.
     * @param traceId The internal database ID of the trace
     * @return Number of events
     */
    @Query("SELECT COUNT(te) FROM TraceEvent te WHERE te.trace.id = :traceId")
    long countByTraceId(@Param("traceId") Long traceId);

    /**
     * Find all exception events for a trace.
     * @param traceId The trace ID string
     * @return List of exception events
     */
    @Query("SELECT te FROM TraceEvent te WHERE te.trace.traceId = :traceId AND te.eventType = 'EXCEPTION' ORDER BY te.sequenceNumber ASC")
    List<TraceEvent> findExceptionEventsByTraceId(@Param("traceId") String traceId);
}