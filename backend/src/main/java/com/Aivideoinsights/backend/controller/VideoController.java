package com.Aivideoinsights.backend.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.Aivideoinsights.backend.dto.VideoRequest;
import com.Aivideoinsights.backend.dto.VideoResponse;
import com.Aivideoinsights.backend.service.VideoService;

@RestController
@RequestMapping("/api/videos")
public class VideoController {

    @Autowired
    private VideoService vid;

    @PostMapping("/analyze")
    public VideoResponse analyzeVideo(@RequestBody VideoRequest request){
        return vid.fetchMetadata(request.getUrl());
    }
}
