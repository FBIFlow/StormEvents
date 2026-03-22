package me.fbiflow.stormevents.util.event.builder;

import me.fbiflow.stormevents.model.event.Event;

import java.util.Map;

public interface EventBuilder {

    Map<Integer, Runnable> build(Event event);

}
