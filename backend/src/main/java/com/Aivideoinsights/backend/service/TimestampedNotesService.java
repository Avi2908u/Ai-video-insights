package com.Aivideoinsights.backend.service;

import com.Aivideoinsights.backend.dto.KeyTopic;
import com.Aivideoinsights.backend.dto.TimestampedNote;
import com.Aivideoinsights.backend.dto.TranscriptSegment;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TimestampedNotesService {
    private final AiService aiService;
    private final ObjectMapper objectMapper;

    public TimestampedNotesService(AiService aiService, ObjectMapper objectMapper){
        this.aiService=aiService;
        this.objectMapper=objectMapper;
    }

    private String extractTopicTranscript(List<TranscriptSegment> segments, int startSec, int endSec){
        StringBuilder builder= new StringBuilder();
        for(TranscriptSegment segment : segments){
            int segmentTime = (int) segment.start();
            if(segmentTime>=startSec && segmentTime< endSec){
                builder.append(segment.text()).append(" ");
            }
        }
        return builder.toString();
    }

    public List<TimestampedNote> generateNotes(List<TranscriptSegment> segments) {
        try {
            String fullTranscript = segments.stream().map(TranscriptSegment::text)
                    .reduce("", (a, b) -> a + " " + b);
            String topicsJson = aiService.generateKeyTopics(fullTranscript);
            topicsJson = topicsJson.replace("```json", "").replace("```", "").trim();
            List<KeyTopic> topics = objectMapper.readValue(topicsJson, objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, KeyTopic.class));
            List<TimestampedNote> notes = new ArrayList<>();
            for (int i = 0; i < topics.size(); i++) {
                KeyTopic current = topics.get(i);
                int start = timestampToSeconds(current.timestamp());
                int end;
                if (i == topics.size() - 1) {
                    end = Integer.MAX_VALUE;
                } else {
                    end = timestampToSeconds(topics.get(i + 1).timestamp());
                }
                String topicTranscript = extractTopicTranscript(segments, start, end);
                String aiResponse = aiService.generateTimestampedNotes(current.topic(), topicTranscript);
                String cleanJson = extractJsonObject(aiResponse);
                JsonNode json = objectMapper.readTree(cleanJson);
                String title = json.has("title") && !json.get("title").isNull() ? json.get("title").asText() : current.topic();
                String noteText = json.has("notes") && !json.get("notes").isNull() ? json.get("notes").asText() : "";
                notes.add(new TimestampedNote(current.timestamp(), title, noteText));
            }
            return notes;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate timestamped notes: " + e.getMessage(), e);
        }
    }

    private String extractJsonObject(String raw) {
        if (raw == null) return "{}";
        String trimmed = raw.replace("```json", "").replace("```", "").trim();
        int firstBrace = trimmed.indexOf('{');
        int lastBrace = trimmed.lastIndexOf('}');
        if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
            return trimmed.substring(firstBrace, lastBrace + 1);
        }
        return trimmed;
    }

    private int timestampToSeconds(String timestamp){
        if (timestamp == null || timestamp.isBlank()) return 0;
        String[] parts = timestamp.trim().split(":");
        try {
            if (parts.length >= 3) {
                return Integer.parseInt(parts[0]) * 3600 + Integer.parseInt(parts[1]) * 60 + Integer.parseInt(parts[2]);
            }
            if (parts.length == 2) {
                return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
            }
            return Integer.parseInt(parts[0]);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
