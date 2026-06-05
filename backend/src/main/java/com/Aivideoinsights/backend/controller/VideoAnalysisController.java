package com.Aivideoinsights.backend.controller;

import com.Aivideoinsights.backend.dto.VideoAnalysisResponse;
import com.Aivideoinsights.backend.service.VideoAnalysisService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VideoAnalysisController {
    private final VideoAnalysisService videoAnalysisService;
    public VideoAnalysisController(VideoAnalysisService videoAnalysisService){
        this.videoAnalysisService=videoAnalysisService;
    }
    @GetMapping("/api/video/analyze")
    public VideoAnalysisResponse analyzeVideo(@RequestParam String url){
        return videoAnalysisService.analyzeVideo(url);
    }
}
