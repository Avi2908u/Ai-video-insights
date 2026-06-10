package com.Aivideoinsights.backend.dto;

import java.util.List;

public class TranscriptResponse {
    private String videoId;
    private String transcript;
    private List<TranscriptSegment> segments;

    public TranscriptResponse(){
    }
    public TranscriptResponse(String videoId, String transcript, List<TranscriptSegment> segments){
        this.videoId=videoId;
        this.transcript=transcript;
        this.segments=segments;
    }
    public String getVideoId(){
        return videoId;
    }
    public void setVideoId(String videoId){
        this.videoId=videoId;
    }
    public String getTranscript(){
        return transcript;
    }
    public void setTranscript(String transcript){
        this.transcript=transcript;
    }
    public List<TranscriptSegment> getSegments(){
        return segments;
    }
    public void setSegments(List<TranscriptSegment> segments){
        this.segments=segments;
    }
}
