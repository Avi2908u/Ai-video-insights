package com.Aivideoinsights.backend.dto.youtube;

public class Snippet {
    private String title;
    private String channelTitle;
    public String getTitle(){
        return title;
    }

    public String getChannelTitle(){
        return channelTitle;
    }

    public void setTitle(String title){
        this.title=title;
    }

    public void setChannelTitle(String channelTitle){
        this.channelTitle=channelTitle;
    }
}
