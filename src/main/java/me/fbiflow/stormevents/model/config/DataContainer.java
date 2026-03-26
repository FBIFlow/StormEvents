package me.fbiflow.stormevents.model.config;

import java.util.Set;

public interface DataContainer {

    Object get(String key);

    Set<String> getKeys();
}