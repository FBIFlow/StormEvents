package me.fbiflow.stormevents.model.config.implementation;

import me.fbiflow.stormevents.model.config.DataContainer;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DataContainerImpl implements DataContainer {

    private final HashMap<String, Object> data = new HashMap<>();

    public DataContainerImpl(final Map<String, Object> data) {
        this.data.putAll(data);
    }

    @Override
    public Object get(String key) {
        return data.get(key);
    }

    @Override
    public Set<String> getKeys() {
        return data.keySet();
    }
}