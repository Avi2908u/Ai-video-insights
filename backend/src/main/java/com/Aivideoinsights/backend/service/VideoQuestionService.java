package com.Aivideoinsights.backend.service;

import com.Aivideoinsights.backend.client.RagClient;
import com.Aivideoinsights.backend.dto.TranscriptSegment;
import com.Aivideoinsights.backend.dto.VideoQuestionResponse;
import com.Aivideoinsights.backend.model.Video;
import com.Aivideoinsights.backend.repository.TranscriptRepository;
import com.Aivideoinsights.backend.repository.VideoRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class VideoQuestionService {
    private final VideoRepository videoRepository;
    private final TranscriptRepository transcriptRepository;
    private final RagClient ragClient;
    private final ObjectMapper objectMapper;

    public VideoQuestionService(VideoRepository videoRepository, TranscriptRepository transcriptRepository,
                                RagClient ragClient, ObjectMapper objectMapper) {
        this.videoRepository = videoRepository;
        this.transcriptRepository = transcriptRepository;
        this.ragClient = ragClient;
        this.objectMapper = objectMapper;
    }

    public VideoQuestionResponse ask(String videoId, String question) {
        if (question == null || question.isBlank() || question.length() > 1000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Question must contain 2 to 1000 characters.");
        }
        Video video = videoRepository.findByYoutubeVideoId(videoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Analyze this video before asking a question."));
        var transcript = transcriptRepository.findByVideo(video)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No transcript is available for this video."));
        List<TranscriptSegment> segments = List.of();
        if (transcript.getTranscriptSegments() != null && !transcript.getTranscriptSegments().isBlank()) {
            try {
                segments = objectMapper.readValue(transcript.getTranscriptSegments(), new TypeReference<>() {});
            } catch (Exception ignored) {
                // Legacy or malformed rows fall back to the saved plain transcript.
            }
        }
        return ragClient.ask(videoId, question.trim(), transcript.getTranscriptText(), segments);
    }
}
