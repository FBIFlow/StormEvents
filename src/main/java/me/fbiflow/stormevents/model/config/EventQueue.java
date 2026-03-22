package me.fbiflow.stormevents.model.config;

import me.fbiflow.stormevents.model.event.ExecutingEvent;

import java.util.HashMap;
import java.util.Map;

public class EventQueue {

    private final int offsetMinutes;
    //k - время в тиках. v - id ивента
    private final Map<Integer, String> registeredEvents;
    private final Map<Integer, String> executingEvents;

    /**
     * @param offsetMinutes смещение во времени относительно текущего локального времени пк
     * @param configEvents очередь запланированных событий в виде 2 строк "10:45" "event-id" в конфиге
     */
    public EventQueue(int offsetMinutes, Map<Integer, String> configEvents) {
        this.offsetMinutes = offsetMinutes;
        Map<Integer, String> updatedTimeConfigEvents = new HashMap<>();
        configEvents.keySet().forEach(
                key -> updatedTimeConfigEvents.put(key + offsetMinutes, configEvents.get(key))
        );
        this.registeredEvents = updatedTimeConfigEvents;
        this.executingEvents = new HashMap<>();
    }

    public void setExecuting(ExecutingEvent event, boolean executing) {
        if (executing) {
            executingEvents.put(event.time, event.event.getId());
            return;
        }
        executingEvents.remove(event.time);
    }

    public boolean isExecuting(int time) {
        return executingEvents.containsKey(time);
    }


    public int getOffsetMinutes() {
        return this.offsetMinutes;
    }

    public Map<Integer, String> getConfigEvents() {
        return this.registeredEvents;
    }


}