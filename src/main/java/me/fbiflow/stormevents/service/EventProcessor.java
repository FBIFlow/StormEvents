package me.fbiflow.stormevents.service;

import me.fbiflow.stormevents.Plugin;
import me.fbiflow.stormevents.model.enums.TaskType;
import me.fbiflow.stormevents.model.event.Event;
import me.fbiflow.stormevents.model.event.ExecutingEvent;
import me.fbiflow.stormevents.model.event.ScheduledTask;
import me.fbiflow.stormevents.util.FFLogger;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class EventProcessor {

    private final ExecutingEvent event;
    private final Event processingEvent;
    private final ScheduledExecutorService service;
    private List<ScheduledTask> scheduledTasks;
    private final List<BukkitTask> executingBukkitTasks;
    private final List<Thread> executingThreads;
    private ScheduledFuture<?> scheduledFuture;
    private int eventTick;
    private int liveTick;

    public EventProcessor(ExecutingEvent event) {
        this.event = event;
        this.processingEvent = event.event;
        this.scheduledTasks = new ArrayList<>(processingEvent.getScheduledTasks().values());
        ScheduledTask scheduledTask = scheduledTasks.stream()
                .min(Comparator.comparing(ScheduledTask::getTimeTicks)).orElse(null);
        this.eventTick = scheduledTask == null ? 0 : Math.min(scheduledTask.getTimeTicks(), 0);
        this.service = Executors.newSingleThreadScheduledExecutor();
        this.executingBukkitTasks = new ArrayList<>();
        this.executingThreads = new ArrayList<>();
    }

    public void process() {
        Bukkit.getScheduler().runTask(Plugin.instance, () -> {
            if (!Plugin.instance.isActive()) {
                return;
            }
            processingEvent.onInit();
        });
        scheduledFuture = service.scheduleAtFixedRate(this::tickTask, 0, 50, TimeUnit.MILLISECONDS);
    }

    public void interrupt(int tick) {
        if (tick == -1 || tick < liveTick) {
            interruptNow();
            return;
        }
        Executors.newSingleThreadScheduledExecutor().schedule(
                this::interruptNow,
                (tick - liveTick) * 50L,
                TimeUnit.MILLISECONDS
        );
    }

    private void interruptNow() {
        FFLogger.log("interrupting task...");
        executingBukkitTasks.forEach(BukkitTask::cancel);
        executingThreads.forEach(Thread::interrupt);

        Bukkit.getScheduler().runTask(Plugin.instance, () -> {
            processingEvent.getBukkitTasks().forEach(BukkitTask::cancel);
            processingEvent.onRemove();
        });

        Plugin.instance.getPluginContainer().getConfiguration().getEventQueue().setExecuting(event, false);
        Plugin.instance.getPluginContainer().getExecutorService().unregisterTask(event);

        scheduledFuture.cancel(false);
        service.shutdown();
    }

    private void incrementCounter() {
        eventTick++;
        liveTick++;
    }

    private void tickTask() {
        if (!Plugin.instance.isActive()) {
            return;
        }
        if (!processingEvent.isInitialized()) {
            return;
        }

        Bukkit.getScheduler().runTask(Plugin.instance, () -> processingEvent.onTick(eventTick));

        scheduledTasks = new ArrayList<>(processingEvent.getScheduledTasks().values());
        ScheduledTask scheduledTask = scheduledTasks.stream()
                .filter(task -> task.getTimeTicks() == eventTick)
                .findFirst().orElse(null);

        if (scheduledTask != null) {
            if (scheduledTask.getType() == TaskType.BUKKIT) {
                BukkitTask bukkitTask = Bukkit.getScheduler().runTask(Plugin.instance, scheduledTask.getTask());
                executingBukkitTasks.add(bukkitTask);
            } else {
                Thread thread = new Thread(scheduledTask.getTask());
                executingThreads.add(thread);
                thread.start();
            }
        }

        if (liveTick == processingEvent.getLiveTimeTicks()) {
            interruptNow();
        }

        incrementCounter();
    }
}