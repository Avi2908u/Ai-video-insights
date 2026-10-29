package com.Aivideoinsights.backend.util;

import java.net.URI;
import java.util.List;

public class YoutubeUtil {
    public static String extractVideoId(String url){
        try {
            URI uri = URI.create(url);
            String host = uri.getHost();
            if (host == null) throw new IllegalArgumentException("Invalid YouTube URL");
            host = host.toLowerCase();
            String id = null;
            if (host.equals("youtu.be") || host.equals("www.youtu.be")) {
                id = firstPathPart(uri.getPath());
            } else if (List.of("youtube.com", "www.youtube.com", "m.youtube.com", "music.youtube.com").contains(host)) {
                if (uri.getRawQuery() != null) {
                    for (String pair : uri.getRawQuery().split("&")) {
                        String[] values = pair.split("=", 2);
                        if (values[0].equals("v") && values.length == 2) id = values[1];
                    }
                }
                if (id == null) {
                    String[] path = uri.getPath().split("/");
                    for (int i = 0; i + 1 < path.length; i++) {
                        if (List.of("embed", "shorts", "live").contains(path[i])) {
                            id = path[i + 1];
                            break;
                        }
                    }
                }
            }
            if (id != null && id.matches("[A-Za-z0-9_-]{6,}")) return id;
        } catch (Exception ignored) {
            // Report one consistent validation error for malformed and unsupported URLs.
        }
        throw new IllegalArgumentException("Invalid YouTube URL. Use a youtube.com/watch, youtu.be, /shorts, or /embed link.");
    }

    private static String firstPathPart(String path) {
        if (path == null) return null;
        for (String part : path.split("/")) if (!part.isBlank()) return part;
        return null;
    }
}
