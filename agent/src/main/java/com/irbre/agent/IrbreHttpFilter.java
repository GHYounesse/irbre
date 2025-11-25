package com.irbre.agent;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Servlet filter that starts/ends traces and propagates correlation IDs.
 */
public class IrbreHttpFilter implements Filter {
    public static final String TRACE_ID_HEADER = "X-Trace-Id";
    public static final String PARENT_SPAN_ID_HEADER = "X-Parent-Span-Id";

    private final EventCollector collector = EventCollector.getInstance();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("[IRBRE] HTTP Filter initialized");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (!(request instanceof HttpServletRequest)) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Extract headers for propagation
        Map<String, String> headers = new HashMap<>();
        String incomingTraceId = httpRequest.getHeader(TRACE_ID_HEADER);
        String incomingParentSpanId = httpRequest.getHeader(PARENT_SPAN_ID_HEADER);

        if (incomingTraceId != null) {
            headers.put(TRACE_ID_HEADER, incomingTraceId);
        }
        if (incomingParentSpanId != null) {
            headers.put(PARENT_SPAN_ID_HEADER, incomingParentSpanId);
        }

        // Start trace with correlation info
        collector.startRequest(
                httpRequest.getMethod(),
                httpRequest.getRequestURI(),
                headers,
                incomingTraceId,  // Pass incoming trace ID
                incomingParentSpanId  // Pass incoming parent span ID
        );

        try {
            // Add trace ID to response headers for debugging
            String currentTraceId = collector.getCurrentTraceId();
            if (currentTraceId != null) {
                httpResponse.setHeader(TRACE_ID_HEADER, currentTraceId);
            }

            chain.doFilter(request, response);
        } finally {
            collector.endRequest();
        }
    }

    @Override
    public void destroy() {
        System.out.println("[IRBRE] HTTP Filter destroyed");
    }
}