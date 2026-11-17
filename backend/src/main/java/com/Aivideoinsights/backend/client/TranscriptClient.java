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
        TranscriptResponse response;
        try {
            response = restTemplate.getForObject(
                    transcriptServiceUrl + "/transcript/{videoId}",
                    TranscriptResponse.class,
                    videoId);
        } catch (Exception e) {
            throw new RuntimeException("Could not connect to transcript service at " + transcriptServiceUrl + " (" + e.getMessage() + "). Check TRANSCRIPT_SERVICE_URL.", e);
        }
        if (response == null || response.getTranscript() == null || response.getTranscript().isBlank()) {
            throw new RuntimeException("Transcript service returned empty transcript for video '" + videoId + "'. Ensure the video has subtitles/captions enabled.");
        }
        return response;
    }
}
