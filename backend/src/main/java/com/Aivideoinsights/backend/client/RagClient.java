package com.Aivideoinsights.backend.client;

import com.Aivideoinsights.backend.dto.TranscriptSegment;
import com.Aivideoinsights.backend.dto.VideoQuestionResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
public class RagClient {
    private final RestTemplate restTemplate;
    private final String serviceUrl;

    public RagClient(RestTemplate restTemplate,
                     @Value("${transcript.service.url:http://localhost:8000}") String serviceUrl) {
        this.restTemplate = restTemplate;
        this.serviceUrl = serviceUrl;
    }

    public VideoQuestionResponse ask(String videoId, String question, String transcript, List<TranscriptSegment> segments) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var body = Map.of("videoId", videoId, "question", question, "transcript", transcript,
                "segments", segments == null ? List.of() : segments);
        return restTemplate.postForObject(serviceUrl + "/ask", new HttpEntity<>(body, headers), VideoQuestionResponse.class);
    }
}
