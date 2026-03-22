package me.fbiflow.stormevents.service;

import me.fbiflow.stormevents.model.event.Event;
import me.fbiflow.stormevents.model.event.ExecutingEvent;
import org.stringtemplate.v4.ST;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class EventExecutorService {


    private final Map<ExecutingEvent, EventProcessor> eventProcessors;

    public EventExecutorService() {
        this.eventProcessors = new ConcurrentHashMap<>();
    }

    public Map<ExecutingEvent, EventProcessor> getEventProcessors() {
        return this.eventProcessors;
    }

    public void reload() {
        Set<ExecutingEvent> keys = eventProcessors.keySet();
        for (ExecutingEvent key : keys) {
            eventProcessors.get(key).interrupt(-1);
        }
    }

    /**
     * для вызова события вручную необходимо наличие соответствующего поля в конфигурации ".tasks"
     * @param task
     */
    public void execute(ExecutingEvent task) {
        executeTask(task);
    }

    public void unregisterTask(ExecutingEvent task) {
        eventProcessors.remove(task);
    }

    /**
     * прервать выполнение события,
     * @param task тик жизни события, когда оно будет прервано. -1 если необходимо прервать моментально
     */
    public void interruptTask(Event task, int tick) {
        eventProcessors.get(task).interrupt(tick);
    }

    /**
     * вызов фактической логики выполнения события
     * @param task
     */
    private void executeTask(ExecutingEvent task) {
        System.out.println("EXECUTE TASK METHOD CALL");
        EventProcessor eventProcessor = new EventProcessor(task);
        eventProcessors.put(task, eventProcessor);
        System.out.println("PUT EVENTPROCESSOR TO");
        eventProcessors.get(task).process();
    }
}