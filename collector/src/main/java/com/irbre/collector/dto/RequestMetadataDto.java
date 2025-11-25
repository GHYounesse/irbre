package com.irbre.collector.dto;

import java.util.Map;

public class RequestMetadataDto {
    private String httpMethod;
    private String url;
    private Map<String, String> headers;




    // Getters and setters
    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String requestUri) {
        this.url = requestUri;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

}
