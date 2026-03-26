package me.fbiflow.stormevents.model.event;

import me.fbiflow.stormevents.model.enums.TaskType;

public class ScheduledTask {

    private final Runnable task;
    private final TaskType type;
    private final int timeTicks;

    public ScheduledTask(Runnable task, TaskType type, int timeTicks) {
        this.task = task;
        this.type = type;
        this.timeTicks = timeTicks;
    }

    public Runnable getTask() {
        return this.task;
    }

    public TaskType getType() {
        return this.type;
    }

    public int getTimeTicks() {
        return this.timeTicks;
    }
}
