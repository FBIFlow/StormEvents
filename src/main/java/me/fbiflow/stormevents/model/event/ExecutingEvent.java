package me.fbiflow.stormevents.model.event;

public class ExecutingEvent {

    public final Event event;
    public final int time;

    private ExecutingEvent(Event event, int time) {
        this.event = event;
        this.time = time;
    }

    public static ExecutingEvent wrap(Event event, int time) {
        return new ExecutingEvent(event, time);
    }

}
