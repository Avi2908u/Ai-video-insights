package com.Aivideoinsights.backend.dto.openrouter;

import java.util.List;

public record OpenRouterRequest(String model,List<Message> messages) {
    }



