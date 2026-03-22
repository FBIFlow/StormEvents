package me.fbiflow.stormevents.util;

import me.fbiflow.stormevents.model.enums.TimeUnit;

import java.time.LocalTime;

public class TimeConverter {

    public static int ticksOf(TimeUnit timeUnit, int time) {
        switch (timeUnit) {
            case TICKS: return time;
            case SECONDS: return (time * 20);
            case MINUTES: return (time * 1200);
        }
        throw new RuntimeException("xD в свич кейс обработаны не все возможные состояния TimeUnit");
    }

    public static int toMillis(TimeUnit timeUnit, int time) {
        return ticksOf(timeUnit, time) * 50;
    }

    public static int getCurrentTimeMinutes() {
        LocalTime localTime = LocalTime.now();
        return (localTime.getHour() * 60) + localTime.getMinute();
    }

}