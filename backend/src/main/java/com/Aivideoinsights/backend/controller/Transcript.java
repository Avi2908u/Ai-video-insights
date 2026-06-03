package com.Aivideoinsights.backend.controller;

import com.Aivideoinsights.backend.dto.TranscriptResponse;
import com.Aivideoinsights.backend.service.TranscriptService;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/transcript")
public class Transcript {
    private final TranscriptService transcriptService;
    public Transcript(TranscriptService transcriptService){
        this.transcriptService=transcriptService;
    }

    @GetMapping("/{videoId}")
    public TranscriptResponse getTranscript(@PathVariable String videoId){
        return transcriptService.getTranscript(videoId);
    }
}
