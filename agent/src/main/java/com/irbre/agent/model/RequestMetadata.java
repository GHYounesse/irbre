package com.irbre.agent.model;

import java.util.Map;

/**
 * Captures minimal HTTP request information, attached to the first event
 * in a trace (REQUEST_START) and optionally reused in later events.
 */
public class RequestMetadata {

    private String httpMethod;
    private String requestUri;
    private Map<String, String> headers;
    private String clientIp;

    public RequestMetadata() {
    }

    public RequestMetadata(String httpMethod, String requestUri,
                           Map<String, String> headers, String clientIp) {
        this.httpMethod = httpMethod;
        this.requestUri = requestUri;
        this.headers = headers;
        this.clientIp = clientIp;
    }

    public RequestMetadata(String httpMethod, String url, Map<String, String> headers) {
        this.httpMethod = httpMethod;
        this.requestUri = url;
        this.headers = headers;
    }

    // Getters and setters
    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getRequestUri() {
        return requestUri;
    }

    public void setRequestUri(String requestUri) {
        this.requestUri = requestUri;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    // Builder pattern for consistency with TraceEvent
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String httpMethod;
        private String requestUri;
        private Map<String, String> headers;
        private String clientIp;

        public Builder httpMethod(String httpMethod) {
            this.httpMethod = httpMethod;
            return this;
        }

        public Builder requestUri(String requestUri) {
            this.requestUri = requestUri;
            return this;
        }

        public Builder headers(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public Builder clientIp(String clientIp) {
            this.clientIp = clientIp;
            return this;
        }

        public RequestMetadata build() {
            return new RequestMetadata(httpMethod, requestUri, headers, clientIp);
        }
    }
}
