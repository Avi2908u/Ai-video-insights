package com.Aivideoinsights.backend.client;

import com.Aivideoinsights.backend.dto.TranscriptResponse;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

@Component
public class TranscriptClient {
    private final RestTemplate restTemplate;
    private final String transcriptServiceUrl;

    public TranscriptClient(
            RestTemplate restTemplate,
            @Value("${transcript.service.url:http://localhost:8000}") String transcriptServiceUrl) {
        this.restTemplate=restTemplate;
        this.transcriptServiceUrl=transcriptServiceUrl;
    }
    public TranscriptResponse fetchTranscript(String videoId){
        return restTemplate.getForObject(
                transcriptServiceUrl + "/transcript/{videoId}",
                TranscriptResponse.class,
                videoId);
    }
}
