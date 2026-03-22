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
    ScheduledExecutorService service;
    private List<ScheduledTask> scheduledTasks;
    private List<BukkitTask> executingBukkitTasks;
    private List<Thread> executingTasks;
    private ScheduledFuture<?> scheduledFuture;

    private int eventTick; // мнимое время существования события, для выполнения планированных задач
    private int liveTick = 0; // реальное время в тиках, которое существует событие

    public EventProcessor(ExecutingEvent event) {
        System.out.println("EVENT PROCESSOR CONSTRUCTOR");
        try {
            this.event = event;
            this.processingEvent = event.event;
            scheduledTasks = new ArrayList<>(processingEvent.getScheduledTasks().values());
            ScheduledTask scheduledTask = scheduledTasks.stream()
                    .min(Comparator.comparing(ScheduledTask::getTimeTicks)).orElse(null);
            this.eventTick = scheduledTask == null ? 0 : Math.min(scheduledTask.getTimeTicks(), 0);
            this.service = Executors.newSingleThreadScheduledExecutor();
            this.executingBukkitTasks = new ArrayList<>();
            this.executingTasks = new ArrayList<>();
            System.out.println("CREATED A NEW INSTANCE OF EVENT PROCESSOR");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("ERROR");
        }
    }

    public void process() {
        new Thread(() -> {
            if (!Plugin.instance.isActive()) {
                return;
            }
            try {
                System.out.println("PROCESSING EVENT");
                processingEvent.onInit();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }).start();
        scheduledFuture = service.scheduleAtFixedRate(tickTask(), 0, 50, TimeUnit.MILLISECONDS);
    }

    public void interrupt(int tick) {
        if (tick == -1 || tick < liveTick) {
            interrupt();
            return;
        }
        Executors.newSingleThreadScheduledExecutor().schedule(
                () -> interrupt(),
                //50 tick now, end at 70 tick -> (70 - 50) * 50 = 20 * 50 = 1000 -> wait 1000 millis (1 second)
                (tick - liveTick) * 50L,
                TimeUnit.MILLISECONDS
        );
    }

    private void interrupt() {
        FFLogger.log("interrupting task...");
        executingBukkitTasks.forEach(BukkitTask::cancel);
        executingTasks.forEach(Thread::interrupt);
        Plugin.instance.getPluginContainer().getExecutorService().unregisterTask(event);
        Executors.newSingleThreadScheduledExecutor()
                .schedule(
                        () -> {
                            Plugin.instance.getPluginContainer().getConfiguration().getEventQueue().setExecuting(event, false);
                        }, 65, TimeUnit.SECONDS
                );
        try {
            processingEvent.getBukkitTasks().forEach(BukkitTask::cancel);
            processingEvent.onRemove();
        } catch (Exception exception) {
            FFLogger.log("Ошибка при прерывании события:");
            exception.printStackTrace();
        }
        scheduledFuture.cancel(false);
        service.shutdown();
    }

    private void incrementCounter() {
        eventTick++;
        liveTick++;
    }

    private Runnable tickTask() {
        return () -> {
            if (!Plugin.instance.isActive()) {
                return;
            }
            if (!event.event.isInitialized()) {
                return;
            }
            processingEvent.onTick(eventTick);
            scheduledTasks = new ArrayList<>(processingEvent.getScheduledTasks().values());
            FFLogger.log("live-tick: " + liveTick);
            FFLogger.log("event-tick " + eventTick);
            ScheduledTask scheduledTask = null;
            try {
                scheduledTask = scheduledTasks.stream()
                        .filter(task -> task.getTimeTicks() == eventTick)
                        .findFirst().orElse(null);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            if (scheduledTask != null) {
                if (scheduledTask.getType() == TaskType.BUKKIT) {
                    BukkitTask bukkitTask = Bukkit.getScheduler().runTask(Plugin.instance, scheduledTask.getTask());
                    executingBukkitTasks.add(bukkitTask);
                } else {
                    Thread thread = new Thread(scheduledTask.getTask());
                    executingTasks.add(thread);
                    thread.start();
                }
            }
            if (liveTick == processingEvent.getLiveTimeTicks()) {
                interrupt();
            }
            incrementCounter();
        };
    }
}