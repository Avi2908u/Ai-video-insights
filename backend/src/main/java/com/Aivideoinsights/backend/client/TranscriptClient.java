package com.Aivideoinsights.backend.client;

import com.Aivideoinsights.backend.dto.TranscriptResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.Map;
@Component
public class TranscriptClient {
    private final RestTemplate restTemplate;
    public TranscriptClient(RestTemplate restTemplate){
        this.restTemplate=restTemplate;
    }
    public TranscriptResponse fetchTranscript(String videoId){
        String url="http://localhost:5000/transcript/" + videoId;
        Map response = restTemplate.getForObject(url,Map.class);
        return restTemplate.getForObject(url,TranscriptResponse.class);
    }
}
