package me.fbiflow.stormevents.util.event.builder;

import me.fbiflow.stormevents.model.event.Event;
import me.fbiflow.stormevents.model.event.ScheduledTask;
import me.fbiflow.stormevents.util.ReflectionUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventBuilderImpl implements EventBuilder {

    @Override
    @SuppressWarnings("unchecked")
    public Map<Integer, Runnable> build(Event event) {
        Map<Integer, Runnable> eventMap = new HashMap<>();
        List<ScheduledTask> scheduledTasks = (List<ScheduledTask>) ReflectionUtil.getFieldValue("scheduledTasks", event);
        scheduledTasks
                .forEach(task -> {
                    eventMap.put(task.getTimeTicks(), task.getTask());
                });
        return eventMap;
    }
}