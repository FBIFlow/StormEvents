package me.fbiflow.stormevents.model.config;

public interface Configuration {

    Configuration init();

    void reloadConfig();

    void saveConfig();

    DataContainer getDataContainer(String id);

    EventQueue getEventQueue();
}