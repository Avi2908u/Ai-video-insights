package com.Aivideoinsights.backend.service;

import com.Aivideoinsights.backend.client.OpenRouterClient;
import org.springframework.stereotype.Service;

@Service
public class SummaryService {
    private final OpenRouterClient openRouterClient;
    public SummaryService(OpenRouterClient openRouterClient){
        this.openRouterClient = openRouterClient;
    }

    public String generateSummary(String transcript){
        return openRouterClient.generateSummary(transcript);
    }
}
