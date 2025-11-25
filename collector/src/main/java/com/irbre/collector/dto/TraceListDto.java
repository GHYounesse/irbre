package com.irbre.collector.dto;



import java.util.List;

public class TraceListDto {

    private List<TraceSummaryDto> traces;
    private long totalElements;
    private int totalPages;
    private int currentPage;
    private int pageSize;

    // Getters & Setters

    public List<TraceSummaryDto> getTraces() {
        return traces;
    }

    public void setTraces(List<TraceSummaryDto> traces) {
        this.traces = traces;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}
