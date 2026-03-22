package me.fbiflow.stormevents.model.event.events.stormevent.model;

import org.bukkit.Location;

public class Vertex2f {

    public final float x;
    public final float y;

    public Vertex2f(float x, float y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return String.format("{%f; %f}",x, y);
    }

    public static Vertex2f ofLocation(Location location) {
        return new Vertex2f((float) location.getX(), (float) location.getZ());
    }

}