package com.Aivideoinsights.backend.dto;

import java.util.List;

public record VideoAnalysisResponse(String title, String channel, String duration,
                                    String transcript, String summary, List<KeyTopic> keyTopics, List<TimestampedNote> notes) {
}
