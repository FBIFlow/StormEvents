package me.fbiflow.stormevents.model.event.events.someotherevent;

import me.fbiflow.stormevents.model.event.Event;
import org.bukkit.Location;

import java.io.File;

public class SomeEvent extends Event {

    private boolean allowed;
    private String startMessage;
    private File schematicFile;

    private Location eventLocation;


    @Override
    public String getId() {
        return "some-event";
    }

    @Override
    public int getLiveTimeTicks() {
        return 0;
    }

    @Override
    public void onInit() {
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public void onTick(int eventTick) {

    }

    @Override
    public void onRemove() {
    }
}
