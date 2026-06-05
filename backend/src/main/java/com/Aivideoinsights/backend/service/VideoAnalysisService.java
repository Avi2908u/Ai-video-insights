package com.Aivideoinsights.backend.service;

import com.Aivideoinsights.backend.dto.TranscriptResponse;
import com.Aivideoinsights.backend.dto.VideoAnalysisResponse;
import com.Aivideoinsights.backend.dto.VideoResponse;
import org.springframework.stereotype.Service;

import static com.Aivideoinsights.backend.util.YoutubeUtil.extractVideoId;

@Service
public class VideoAnalysisService {
    private final VideoService videoService;
    private final TranscriptService transcriptService;
    private final SummaryService summaryService;

    public VideoAnalysisService(VideoService videoService, TranscriptService transcriptService, SummaryService summaryService){
        this.videoService=videoService;
        this.transcriptService=transcriptService;
        this.summaryService=summaryService;
    }
    public VideoAnalysisResponse analyzeVideo(String url){
        VideoResponse metadata = videoService.fetchMetadata(url);
        String videoId = extractVideoId(url);
        TranscriptResponse transcriptResponse = transcriptService.getTranscript(videoId);
        String transcript = transcriptResponse.getTranscript();
        String summary =summaryService.generateSummary(transcript);
        return new VideoAnalysisResponse(metadata.getTitle(),
                metadata.getChannel(), metadata.getDuration(), transcript,summary);

    }


}
