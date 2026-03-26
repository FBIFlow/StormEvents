package me.fbiflow.stormevents.model.config.implementation;

import me.fbiflow.stormevents.model.config.Configuration;
import me.fbiflow.stormevents.model.config.DataContainer;
import me.fbiflow.stormevents.model.config.EventQueue;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ConfigurationMock implements Configuration {
    @Override
    public Configuration init() {
        return this;
    }

    @Override
    public void reloadConfig() {

    }

    @Override
    public void saveConfig() {

    }

    @Override
    public DataContainer getDataContainer(String id) {
        Map<String, Object> data = new HashMap<>();
        data.put("key1", "key1_out");
        data.put("key2", "key2_out");
        data.put("key3", "key3_out");

        return new DataContainerImpl(data);
    }

    @Override
    public EventQueue getEventQueue() {
        Map<Integer, String> configEvents = new HashMap<>();
        configEvents.put((int) TimeUnit.HOURS.toMinutes(17) + 11, "storm-event");
        return new EventQueue(0, configEvents);
    }
}