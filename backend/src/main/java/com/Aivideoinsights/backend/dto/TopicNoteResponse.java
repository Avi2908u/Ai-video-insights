package com.Aivideoinsights.backend.dto;

import java.util.List;

public record TopicNoteResponse(String summary, List<String> keyPoints, List<String> examples) {
}
