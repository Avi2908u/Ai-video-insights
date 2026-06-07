package com.Aivideoinsights.backend.service;

import com.Aivideoinsights.backend.dto.TranscriptResponse;
import com.Aivideoinsights.backend.dto.VideoAnalysisResponse;
import com.Aivideoinsights.backend.dto.VideoResponse;
import com.Aivideoinsights.backend.model.Summary;
import com.Aivideoinsights.backend.model.Transcript;
import com.Aivideoinsights.backend.model.Video;
import com.Aivideoinsights.backend.repository.SummaryRepository;
import com.Aivideoinsights.backend.repository.TranscriptRepository;
import com.Aivideoinsights.backend.repository.VideoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.Aivideoinsights.backend.util.YoutubeUtil.extractVideoId;

@Service
public class VideoAnalysisService {
    private final VideoService videoService;
    private final TranscriptService transcriptService;
    private final SummaryService summaryService;
    private final VideoRepository videoRepository;
    private final SummaryRepository summaryRepository;
    private final TranscriptRepository transcriptRepository;

    public VideoAnalysisService(VideoService videoService, TranscriptService transcriptService, SummaryService summaryService,
    VideoRepository videoRepository, SummaryRepository summaryRepository, TranscriptRepository transcriptRepository){
        this.videoService=videoService;
        this.transcriptService=transcriptService;
        this.summaryService=summaryService;
        this.videoRepository=videoRepository;
        this.summaryRepository=summaryRepository;
        this.transcriptRepository=transcriptRepository;
    }
    public VideoAnalysisResponse analyzeVideo(String url){
        String videoId = extractVideoId(url);
        Optional<Video> existingVideo= videoRepository.findByYoutubeVideoId(videoId);
        if(existingVideo.isPresent()){
            Video video = existingVideo.get();
            Transcript transcript= transcriptRepository.findByVideo(video).orElseThrow();
            Summary summary= summaryRepository.findByVideoAndSummaryType(video,"DEFAULT").orElseThrow();
            return new VideoAnalysisResponse(video.getTitle(),video.getChannel(),video.getDuration(),transcript.getTranscriptText(),summary.getSummaryText());
        }
        VideoResponse metadata = videoService.fetchMetadata(url);
        TranscriptResponse transcriptResponse = transcriptService.getTranscript(videoId);
        String transcriptText = transcriptResponse.getTranscript();
        String summaryText =summaryService.generateSummary(transcriptText);
        Video video = new Video();
        video.setYoutubeVideoId(videoId);
        video.setTitle(metadata.getTitle());
        video.setChannel(metadata.getChannel());
        video.setDuration(metadata.getDuration());
        video.setCreatedAt(LocalDateTime.now());
        videoRepository.save(video);
        Transcript transcript =new Transcript();
        transcript.setVideo(video);
        transcript.setTranscriptText(transcriptText);
        transcriptRepository.save(transcript);
        Summary summary = new Summary();
        summary.setVideo(video);
        summary.setSummaryType("DEFAULT");
        summary.setSummaryText(summaryText);
        summaryRepository.save(summary);

        return new VideoAnalysisResponse(metadata.getTitle(),
                metadata.getChannel(), metadata.getDuration(), transcriptText ,summaryText);
    }
}
