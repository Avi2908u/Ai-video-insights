package com.Aivideoinsights.backend.controller;

import com.Aivideoinsights.backend.dto.SummaryRequest;
import com.Aivideoinsights.backend.dto.SummaryResponse;
import com.Aivideoinsights.backend.service.SummaryService;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/summary")
public class Summary {
    private final SummaryService summaryService;
    public Summary(SummaryService summaryService){
        this.summaryService=summaryService;
    }
    @PostMapping
    public SummaryResponse summarize(@RequestBody SummaryRequest request){
        String summary=summaryService.generateSummary(request.transcript());
        return new SummaryResponse(summary);
    }
}
