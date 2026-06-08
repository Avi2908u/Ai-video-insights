package com.Aivideoinsights.backend.util;

import java.time.Duration;

public class DurationUtil {
    public static String formatDuration(String isoDuration){
        Duration duration=Duration.parse(isoDuration);
        long hours=duration.toHours();
        long minutes=duration.toMinutesPart();
        long seconds= duration.toSecondsPart();

        if(hours>0){
            return String.format("%d:%02d:%02d",hours,minutes,seconds);
        }
        return String.format("%02d:%02d",minutes,seconds);

    }
}
