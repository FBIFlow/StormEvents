package me.fbiflow.stormevents.util.event.builder;

import me.fbiflow.stormevents.model.event.Event;
import me.fbiflow.stormevents.model.event.ScheduledTask;
import me.fbiflow.stormevents.util.ReflectionUtil;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BukkitEventBuilder implements EventBuilder {

    @Override
    @SuppressWarnings("unchecked")
    public Map<Integer, Runnable> build(Event event) {
        Map<Integer, Runnable> eventMap = new HashMap<>();
        List<ScheduledTask> scheduledTasks =
                (List<ScheduledTask>) ReflectionUtil.getFieldValue("scheduledTasks", event);
        scheduledTasks.stream()
                .forEach(task -> eventMap.put(task.getTimeTicks(), new BukkitRunnable() {
                    @Override
                    public void run() {
                        task.getTask().run();
                    }
                }));
        return eventMap;
    }
}