package com.Aivideoinsights.backend.service;

import com.Aivideoinsights.backend.client.OpenRouterClient;

import org.springframework.stereotype.Service;

@Service
public class AiService {
    private final OpenRouterClient openRouterClient;

    public AiService(OpenRouterClient openRouterClient) {
        this.openRouterClient = openRouterClient;
    }

    public String generateSummary(String transcript) {
        return openRouterClient.generateSummary(transcript);
    }

    public String generateKeyTopics(String transcript) {
        return openRouterClient.generateKeyTopics(transcript);
    }

    public String generateNotes(String transcript) {
        return openRouterClient.generateNotes(transcript);
    }
}


