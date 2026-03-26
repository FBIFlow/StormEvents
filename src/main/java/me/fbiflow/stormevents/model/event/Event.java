package me.fbiflow.stormevents.model.event;

import me.fbiflow.stormevents.Plugin;
import me.fbiflow.stormevents.model.config.DataContainer;
import me.fbiflow.stormevents.model.enums.TaskType;
import me.fbiflow.stormevents.model.enums.TimeUnit;
import me.fbiflow.stormevents.util.TimeConverter;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Event {

    protected final DataContainer dataContainer;
    private final Map<Integer, ScheduledTask> scheduledTasks;
    protected final List<BukkitTask> bukkitTasks;
    protected boolean initialized;

    public Event() {
        this.scheduledTasks = new HashMap<>();
        this.dataContainer = Plugin.instance.getPluginContainer().getConfiguration().getDataContainer(getId());
        this.bukkitTasks = new ArrayList<>();
    }

    public abstract String getId();
    public abstract int getLiveTimeTicks();
    public abstract void onInit();
    public abstract boolean isInitialized();
    public abstract void onTick(int eventTick);
    public abstract void onRemove();

    public Map<Integer, ScheduledTask> getScheduledTasks() {
        return scheduledTasks;
    }

    public List<BukkitTask> getBukkitTasks() {
        return this.bukkitTasks;
    }

    protected void registerTask(Runnable task, TaskType taskType, TimeUnit timeUnit, int time) {
        ScheduledTask scheduledTask = new ScheduledTask(
                task,
                taskType,
                TimeConverter.ticksOf(timeUnit, time));
        scheduledTasks.put(scheduledTask.getTimeTicks(), scheduledTask);
    }

    protected void registerTask(Runnable task, TaskType taskType, int timeTicks) {
        ScheduledTask scheduledTask = new ScheduledTask(
                task,
                taskType,
                timeTicks);
        scheduledTasks.put(scheduledTask.getTimeTicks(), scheduledTask);
    }
}