package com.Aivideoinsights.backend.dto.youtube;

import java.util.List;

public class YoutubeResponse {
    private List<Item> items;
    public List<Item> getItems(){
        return items;
    }

    public void setItems(List<Item> items){
        this.items=items;
    }
}
