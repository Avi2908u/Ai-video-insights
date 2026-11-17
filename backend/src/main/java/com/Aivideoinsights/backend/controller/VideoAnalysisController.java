package com.Aivideoinsights.backend.controller;

import com.Aivideoinsights.backend.dto.VideoAnalysisResponse;
import com.Aivideoinsights.backend.service.VideoAnalysisService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.Aivideoinsights.backend.dto.VideoQuestionRequest;
import com.Aivideoinsights.backend.dto.VideoQuestionResponse;
import com.Aivideoinsights.backend.service.VideoQuestionService;

@RestController
@CrossOrigin(origins = "*")
public class VideoAnalysisController {
    private final VideoAnalysisService videoAnalysisService;
    private final VideoQuestionService videoQuestionService;
    public VideoAnalysisController(VideoAnalysisService videoAnalysisService, VideoQuestionService videoQuestionService){
        this.videoAnalysisService=videoAnalysisService;
        this.videoQuestionService=videoQuestionService;
    }
    @GetMapping("/")
    public java.util.Map<String, String> root() {
        return java.util.Map.of("status", "UP", "message", "AI Video Insights API is running");
    }

    @GetMapping("/api/health")
    public java.util.Map<String, String> health() {
        return java.util.Map.of("status", "UP");
    }

    @GetMapping("/api/video/analyze")
    public VideoAnalysisResponse analyzeVideo(@RequestParam String url){
        return videoAnalysisService.analyzeVideo(url);
    }

    @PostMapping("/api/video/{videoId}/ask")
    public VideoQuestionResponse askAboutVideo(@PathVariable String videoId, @RequestBody VideoQuestionRequest request) {
        return videoQuestionService.ask(videoId, request.question());
    }
}
