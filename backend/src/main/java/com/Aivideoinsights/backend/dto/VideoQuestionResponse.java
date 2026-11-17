package com.Aivideoinsights.backend.dto;

import java.util.List;

public record VideoQuestionResponse(String answer, List<VideoCitation> citations) {
    public record VideoCitation(Double timestamp, String text) {}
}
