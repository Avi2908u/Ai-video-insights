package com.Aivideoinsights.backend.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.Map;
@Component
public class TranscriptClient {
    private final RestTemplate restTemplate;
    public TranscriptClient(RestTemplate restTemplate){
        this.restTemplate=restTemplate;
    }
    public String fetchTranscript(String videoId){
        String url="http://localhost:5000/transcript/" + videoId;
        Map response = restTemplate.getForObject(url,Map.class);
        return (String) response.get("transcript");
    }
}
