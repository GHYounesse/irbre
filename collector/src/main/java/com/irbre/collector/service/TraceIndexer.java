package com.irbre.collector.service;

import com.irbre.collector.entity.Trace;
import com.irbre.collector.entity.TraceEvent;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Indexes and builds trace hierarchies from flat event streams.
 */
@Service
public class TraceIndexer {

    /**
     * Build hierarchical structure from events.
     * Returns map of spanId -> list of child spanIds
     */
    public Map<String, List<String>> buildSpanHierarchy(List<TraceEvent> events) {
        Map<String, List<String>> hierarchy = new HashMap<>();

        for (TraceEvent event : events) {
            String spanId = event.getSpanId();
            String parentSpanId = event.getParentSpanId();

            if (parentSpanId != null) {
                hierarchy.computeIfAbsent(parentSpanId, k -> new ArrayList<>())
                        .add(spanId);
            }
        }

        return hierarchy;
    }

    /**
     * Find root spans (spans with no parent in this trace).
     */
    public List<String> findRootSpans(List<TraceEvent> events) {
        Set<String> allSpans = events.stream()
                .map(TraceEvent::getSpanId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<String> childSpans = events.stream()
                .map(TraceEvent::getParentSpanId)
                .filter(Objects::nonNull)
                .filter(allSpans::contains)  // Parent exists in this trace
                .collect(Collectors.toSet());

        // Root spans are those that are not children of any span in this trace
        return events.stream()
                .map(TraceEvent::getSpanId)
                .filter(Objects::nonNull)
                .filter(spanId -> !childSpans.contains(spanId))
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Calculate trace depth (max nesting level).
     */
    public int calculateTraceDepth(List<TraceEvent> events) {
        Map<String, List<String>> hierarchy = buildSpanHierarchy(events);
        List<String> roots = findRootSpans(events);

        int maxDepth = 0;
        for (String root : roots) {
            maxDepth = Math.max(maxDepth, calculateDepth(root, hierarchy, 1));
        }

        return maxDepth;
    }

    private int calculateDepth(String spanId, Map<String, List<String>> hierarchy, int currentDepth) {
        List<String> children = hierarchy.get(spanId);
        if (children == null || children.isEmpty()) {
            return currentDepth;
        }

        int maxChildDepth = currentDepth;
        for (String child : children) {
            maxChildDepth = Math.max(maxChildDepth, calculateDepth(child, hierarchy, currentDepth + 1));
        }

        return maxChildDepth;
    }

    /**
     * Order events by Lamport clock for causal ordering.
     */
    public List<TraceEvent> orderByLamportClock(List<TraceEvent> events) {
        return events.stream()
                .sorted(Comparator.comparing(TraceEvent::getLamportClock,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
    }

    /**
     * Detect if trace spans multiple services (distributed trace).
     */
    public boolean isDistributedTrace(List<TraceEvent> events) {
        // Check if any event has a parent span not in this event list
        Set<String> localSpans = events.stream()
                .map(TraceEvent::getSpanId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        return events.stream()
                .map(TraceEvent::getParentSpanId)
                .filter(Objects::nonNull)
                .anyMatch(parentId -> !localSpans.contains(parentId));
    }

    /**
     * Get critical path (longest duration path through the trace).
     */
    public List<String> getCriticalPath(List<TraceEvent> events) {
        // Group events by span
        Map<String, List<TraceEvent>> eventsBySpan = events.stream()
                .filter(e -> e.getSpanId() != null)
                .collect(Collectors.groupingBy(TraceEvent::getSpanId));

        // Calculate span durations
        Map<String, Long> spanDurations = new HashMap<>();
        for (Map.Entry<String, List<TraceEvent>> entry : eventsBySpan.entrySet()) {
            List<TraceEvent> spanEvents = entry.getValue();
            if (spanEvents.size() >= 2) {
                spanEvents.sort(Comparator.comparing(TraceEvent::getTimestamp));
                long duration = spanEvents.get(spanEvents.size() - 1).getTimestamp().toEpochMilli()
                        - spanEvents.get(0).getTimestamp().toEpochMilli();
                spanDurations.put(entry.getKey(), duration);
            }
        }

        // Find path with maximum total duration (simplified - just longest span)
        return spanDurations.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}