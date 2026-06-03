package com.Aivideoinsights.backend.service;
import com.Aivideoinsights.backend.dto.youtube.*;
import com.Aivideoinsights.backend.dto.VideoResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class VideoService {
    @Value("${youtube.api.key}")
    private String apiKey;

    public VideoResponse fetchMetadata(String url){
        String videoId=extractVideoId(url);
        String apiUrl="https://www.googleapis.com/youtube/v3/videos" +
                "?part=snippet,contentDetails" + "&id=" + videoId + "&key=" + apiKey;

        RestTemplate restTemplate = new RestTemplate();
        YoutubeResponse response = restTemplate.getForObject(apiUrl,YoutubeResponse.class);
        Item item= response.getItems().get(0);
        String title= item.getSnippet().getTitle();
        String channel=item.getSnippet().getChannelTitle();
        String duration=item.getContentDetails().getDuration();
        return new VideoResponse(title,channel,duration);
    }

    private String extractVideoId(String url){
        return url.split("v=")[1];
    }
}
