package com.irbre.collector.configuration;

import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestConfig {
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        restTemplate.getInterceptors().add((request, body, execution) -> {
            String correlationId = MDC.get("X-Correlation-ID");
            if (correlationId != null) {
                request.getHeaders().add("X-Correlation-ID", correlationId);
            }
            return execution.execute(request, body);
        });

        return restTemplate;
    }

}
