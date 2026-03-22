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

    private static Configuration configuration;
    private static EventQueue eventQueue;
    private final PluginContainer pluginContainer = Plugin.instance.getPluginContainer();

    private ScheduledFuture<?> execute;

    public EventLoopService() {
        init();
    }

    public void init() {
        configuration = pluginContainer.getConfiguration();
        FFLogger.log("EventLoopService init...");
        FFLogger.log("configuration is: " + configuration);
        FFLogger.log("configuration type is:" + configuration.getClass());
        eventQueue = configuration.getEventQueue();
    }

    public void reload() {
        execute.cancel(true);
        init();
        execute();
    }

    public void execute() {
        FFLogger.log("called execute");
        FFLogger.log("current local time is: " + TimeConverter.getCurrentTimeMinutes());
        Map<Integer, String> eventsConfig = eventQueue.getConfigEvents();
        if (eventsConfig == null) {
            FFLogger.log("EventsConfig is null");
        }
        Set<Integer> times = eventsConfig.keySet();
        FFLogger.log("events in config is: " + times);
        times.forEach(key -> FFLogger.log(eventsConfig.get(key)));

        execute = Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            int currentTime = TimeConverter.getCurrentTimeMinutes();

            if (eventsConfig.containsKey(currentTime) && !eventQueue.isExecuting(currentTime)) {
                String eventId = eventsConfig.get(currentTime);
                Class<? extends Event> eventClass = pluginContainer.getEventHolder().getById(eventId);
                Event event;
                try {
                    event = eventClass.getDeclaredConstructor().newInstance();
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                         NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
                ExecutingEvent executingEvent = ExecutingEvent.wrap(event, currentTime);
                System.out.println("Wrapped event:" + event);
                eventQueue.setExecuting(executingEvent, true);
                pluginContainer.getExecutorService().execute(executingEvent);
            }
        }, 0, 1, TimeUnit.SECONDS);
    }
}