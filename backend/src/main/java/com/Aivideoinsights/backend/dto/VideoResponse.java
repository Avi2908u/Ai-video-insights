package com.Aivideoinsights.backend.dto;

public class VideoResponse {

    private String title;
    private String channel;
    private String duration;

    public VideoResponse(String title, String channel, String duration){
        this.title=title;
        this.channel=channel;
        this.duration=duration;
    }
    public String getTitle(){
        return title;
    }
    public String getChannel(){
        return channel;
    }
    public String getDuration(){
        return duration;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
}
