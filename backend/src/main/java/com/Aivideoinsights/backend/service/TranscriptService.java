package com.Aivideoinsights.backend.service;

import com.Aivideoinsights.backend.client.TranscriptClient;
import com.Aivideoinsights.backend.dto.TranscriptResponse;
import org.springframework.stereotype.Service;
@Service
public class TranscriptService {
    private final TranscriptClient transcriptClient;
    public TranscriptService(TranscriptClient transcriptClient){
        this.transcriptClient=transcriptClient;
    }

    public TranscriptResponse getTranscript(String videoId){
        String transcript = transcriptClient.fetchTranscript(videoId);
        return new TranscriptResponse(videoId,transcript);
    }

}
