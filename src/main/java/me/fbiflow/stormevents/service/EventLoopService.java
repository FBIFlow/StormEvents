package me.fbiflow.stormevents.service;

import me.fbiflow.stormevents.Plugin;
import me.fbiflow.stormevents.PluginContainer;
import me.fbiflow.stormevents.model.config.Configuration;
import me.fbiflow.stormevents.model.config.EventQueue;
import me.fbiflow.stormevents.model.event.Event;
import me.fbiflow.stormevents.model.event.ExecutingEvent;
import me.fbiflow.stormevents.util.FFLogger;
import me.fbiflow.stormevents.util.TimeConverter;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class EventLoopService {

    private final PluginContainer pluginContainer;
    private Configuration configuration;
    private EventQueue eventQueue;
    private ScheduledFuture<?> execute;

    public EventLoopService() {
        this.pluginContainer = Plugin.instance.getPluginContainer();
        init();
    }

    public void init() {
        configuration = pluginContainer.getConfiguration();
        eventQueue = configuration.getEventQueue();
        FFLogger.log("EventLoopService initialized");
    }

    public void reload() {
        if (execute != null) {
            execute.cancel(true);
        }
        init();
        execute();
    }

    public void execute() {
        Map<Integer, String> eventsConfig = eventQueue.getConfigEvents();
        if (eventsConfig == null || eventsConfig.isEmpty()) {
            FFLogger.log("No events configured");
            return;
        }

        execute = Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            int currentTime = TimeConverter.getCurrentTimeMinutes();

            if (eventsConfig.containsKey(currentTime) && !eventQueue.isExecuting(currentTime)) {
                String eventId = eventsConfig.get(currentTime);
                Class<? extends Event> eventClass = pluginContainer.getEventHolder().getById(eventId);
                if (eventClass == null) {
                    FFLogger.log("Event class not found for id: " + eventId);
                    return;
                }

                Event event;
                try {
                    event = eventClass.getDeclaredConstructor().newInstance();
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                         NoSuchMethodException e) {
                    FFLogger.log("Failed to instantiate event: " + eventId);
                    e.printStackTrace();
                    return;
                }

                ExecutingEvent executingEvent = ExecutingEvent.wrap(event, currentTime);
                eventQueue.setExecuting(executingEvent, true);
                pluginContainer.getExecutorService().execute(executingEvent);
            }
        }, 0, 1, TimeUnit.SECONDS);
    }
}