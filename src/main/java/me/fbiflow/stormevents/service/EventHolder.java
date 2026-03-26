package me.fbiflow.stormevents.service;

import me.fbiflow.stormevents.model.event.Event;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class EventHolder {

    private final Map<String, Class<? extends Event>> events;

    public EventHolder() {
        this.events = new HashMap<>();
    }

    public <T extends Event> void registerEvent(Class<T> eventClass) {
        try {
            Event event = eventClass.getDeclaredConstructor().newInstance();
            if (events.get(event.getId()) != null) {
                throw new RuntimeException("This event is already registered");
            }
            events.put(event.getId(), eventClass);
        }  catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException exception) {
            throw new RuntimeException(exception);
        }
    }

    @SuppressWarnings("unchecked")
    public void registerEvents(Class<? extends Event>... events) {
        for (Class<? extends Event> event : events) {
            registerEvent(event);
        }
    }

    public Class<? extends Event> getById(String id) {
        return events.get(id);
    }
}