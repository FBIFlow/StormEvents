package me.fbiflow.stormevents.util;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class FFLogger {

    public static void log(String text) {
        LocalTime localTime = LocalTime.now();
        String timeString =  DateTimeFormatter.ofPattern("HH:mm:ss").format(localTime);
        System.out.println(timeString + ": "  + text);
    }

}
