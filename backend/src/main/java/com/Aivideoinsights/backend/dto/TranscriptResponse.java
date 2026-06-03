package com.Aivideoinsights.backend.dto;

public class TranscriptResponse {
    private String videoId;
    private String transcript;
    public TranscriptResponse(){
    }
    public TranscriptResponse(String videoId, String transcript){
        this.videoId=videoId;
        this.transcript=transcript;
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
}
