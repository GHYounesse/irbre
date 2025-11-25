package com.irbre.collector.repository;

import com.irbre.collector.entity.Trace;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Trace entities.
 */
@Repository
public interface TraceRepository extends JpaRepository<Trace, Long> {

    /**
     * Find a trace by its trace ID.
     */
    Optional<Trace> findByTraceId(String traceId);

    /**
     * Check if a trace exists by trace ID.
     */
    boolean existsByTraceId(String traceId);

    /**
     * Find all traces with pagination, ordered by creation time descending.
     */
    @Query("SELECT t FROM Trace t ORDER BY t.createdAt DESC")
    Page<Trace> findAllOrderByCreatedAtDesc(Pageable pageable);
}
