package com.Aivideoinsights.backend.service;

import com.Aivideoinsights.backend.dto.TranscriptResponse;
import com.Aivideoinsights.backend.dto.VideoAnalysisResponse;
import com.Aivideoinsights.backend.dto.VideoResponse;
import com.Aivideoinsights.backend.model.AiResults;
import com.Aivideoinsights.backend.model.Transcript;
import com.Aivideoinsights.backend.model.Video;
import com.Aivideoinsights.backend.repository.AiResultsRepository;
import com.Aivideoinsights.backend.repository.TranscriptRepository;
import com.Aivideoinsights.backend.repository.VideoRepository;
import com.Aivideoinsights.backend.util.DurationUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.Aivideoinsights.backend.util.YoutubeUtil.extractVideoId;

@Service
public class VideoAnalysisService {
    private final VideoService videoService;
    private final TranscriptService transcriptService;
    private final AiService aiService;
    private final VideoRepository videoRepository;
    private final AiResultsRepository aiResultsRepository;
    private final TranscriptRepository transcriptRepository;
    private final ObjectMapper objectMapper;

    public VideoAnalysisService(VideoService videoService, TranscriptService transcriptService, AiService aiService,
                                VideoRepository videoRepository, AiResultsRepository aiResultsRepository,
                                TranscriptRepository transcriptRepository, ObjectMapper objectMapper){
        this.videoService=videoService;
        this.transcriptService=transcriptService;
        this.aiService = aiService;
        this.videoRepository=videoRepository;
        this.aiResultsRepository = aiResultsRepository;
        this.transcriptRepository=transcriptRepository;
        this.objectMapper=objectMapper;
    }
    public VideoAnalysisResponse analyzeVideo(String url){
        String videoId = extractVideoId(url);
        Optional<Video> existingVideo= videoRepository.findByYoutubeVideoId(videoId);
        if(existingVideo.isPresent()){
            Video video = existingVideo.get();
            Transcript transcript= transcriptRepository.findByVideo(video).orElseThrow();
            AiResults airesults= aiResultsRepository.findByVideo(video).orElseThrow();
            List<String> topics;
            try{
                topics= objectMapper.readValue(airesults.getKeyTopics(), new TypeReference<List<String>>() {
                });
            }catch (Exception e){
                throw new RuntimeException(e);
            }
            return new VideoAnalysisResponse(video.getTitle(),video.getChannel(),video.getDuration(),
                    transcript.getTranscriptText(), airesults.getSummary(), topics,
                    airesults.getNotes());
        }
        VideoResponse metadata = videoService.fetchMetadata(url);
        TranscriptResponse transcriptResponse = transcriptService.getTranscript(videoId);
        String transcriptText = transcriptResponse.getTranscript();

        String summary = aiService.generateSummary(transcriptText);
        String keyTopics= cleanJsonResponse(aiService.generateKeyTopics(transcriptText));
        String notes= aiService.generateNotes(transcriptText);
        List<String> topics;
        try {
            topics = objectMapper.readValue(
                    keyTopics,
                    new TypeReference<List<String>>() {}
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse AI topics response",e);
        }
        Video video = new Video();
        video.setYoutubeVideoId(videoId);
        video.setTitle(metadata.getTitle());
        video.setChannel(metadata.getChannel());
        video.setDuration(DurationUtil.formatDuration(metadata.getDuration()));
        video.setCreatedAt(LocalDateTime.now());
        videoRepository.save(video);

        Transcript transcript =new Transcript();
        transcript.setVideo(video);
        transcript.setTranscriptText(transcriptText);
        transcriptRepository.save(transcript);

        AiResults aiResults = new AiResults();
        aiResults.setVideo(video);
        aiResults.setSummary(summary);
        aiResults.setKeyTopics(keyTopics);
        aiResults.setNotes(notes);
        aiResultsRepository.save(aiResults);

        return new VideoAnalysisResponse(metadata.getTitle(), metadata.getChannel(), metadata.getDuration(),
                transcriptText , summary, topics, notes);
    }
    private String cleanJsonResponse(String response){
        return response.replace("```json","").replace("```","").trim();
    }
}
