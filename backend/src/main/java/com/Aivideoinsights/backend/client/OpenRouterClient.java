package com.Aivideoinsights.backend.client;

import com.Aivideoinsights.backend.dto.openrouter.Message;
import com.Aivideoinsights.backend.dto.openrouter.OpenRouterRequest;
import com.Aivideoinsights.backend.dto.openrouter.OpenRouterResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
public class OpenRouterClient {
    private final RestTemplate restTemplate;

    @Value("${openrouter.api.key}")
    private String apiKey;
    @Value("${openrouter.url}")
    private String url;
    @Value("${openrouter.model}")
    private String model;

    public OpenRouterClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String generateResponse(String prompt) {
      OpenRouterRequest request = new OpenRouterRequest(model, List.of(new Message("user", prompt)));
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("HTTP-Referer", "http://localhost:8080");
        headers.set("X-Title", "AI Video Insights");
        HttpEntity<OpenRouterRequest> entity = new HttpEntity<>(request, headers);
        ResponseEntity<OpenRouterResponse> response = restTemplate.exchange(url, HttpMethod.POST, entity, OpenRouterResponse.class);
        OpenRouterResponse body = response.getBody();
        if (body == null
                || body.choices() == null
                || body.choices().isEmpty()) {

            throw new RuntimeException(
                    "No response received from OpenRouter"
            );
        }
        return body.choices().get(0).message().content();
    }

}
