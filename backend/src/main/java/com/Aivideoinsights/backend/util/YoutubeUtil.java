package com.Aivideoinsights.backend.util;

public class YoutubeUtil {
    public static String extractVideoId(String url){
        if(url.contains("v="))
              return url.split("v=")[1].split("&")[0];
        throw new RuntimeException("Invalid YouTube URL");
    }
}
