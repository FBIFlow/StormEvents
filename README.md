# StormEvents

**StormEvents** is a Bukkit/Spigot framework for creating timed in-game events with a flexible scheduling system and full lifecycle control.

## Project Overview

This project demonstrates:

- Custom event scheduling with time-based triggers
- Full event lifecycle management (`onInit`, `onTick`, `onRemove`)
- Internal task scheduler for time‑relative execution
- YAML configuration with runtime validation
- Thread‑safe Bukkit integration

## Architecture
```
me/fbiflow/stormevents/
├── command/
│   └── ReloadCommand.java
├── model/
│   ├── config/          # Configuration interfaces + YAML implementation
│   ├── enums/           # TaskType, TimeUnit
│   └── event/           # Event, ScheduledTask, ExecutingEvent
├── service/
│   ├── EventHolder.java
│   ├── EventLoopService.java
│   ├── EventExecutorService.java
│   └── EventProcessor.java
├── util/
│   ├── FFLogger.java
│   ├── TimeConverter.java
│   └── validator/       # Config validation filters
├── Plugin.java
└── PluginContainer.java
```
## How It Works

1. Events are defined in `config.yml` with a time (e.g., `"15:30": "my-event"`) and optional data.
2. `EventLoopService` monitors the current time and triggers events when their time comes.
3. `EventHolder` creates a new instance of the event class.
4. `EventProcessor` manages the event's lifecycle:
   - `onInit()` — called once at start
   - `onTick()` — called every game tick
   - Scheduled tasks execute at predefined ticks
   - `onRemove()` — called when the event ends

## Creating an Event

Extend `Event` and implement the abstract methods:

```java

package me.fbiflow.stormevents.yourpackage;

import me.fbiflow.stormevents.model.event.Event;
import me.fbiflow.stormevents.model.enums.TaskType;
import me.fbiflow.stormevents.model.enums.TimeUnit;
import me.fbiflow.stormevents.util.TimeConverter;

public class YourEvent extends Event {

    @Override
    public String getId() {
        return "your-event-id";
    }

    @Override
    public int getLiveTimeTicks() {
        return TimeConverter.ticksOf(TimeUnit.MINUTES, 5);
    }

    @Override
    public void onInit() {
        String worldName = (String) dataContainer.get("event-world");
        String message = (String) dataContainer.get("pre-spawn-message");
        // Your initialization logic
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public void onTick(int eventTick) {
        if (eventTick == 100) {
            registerTask(() -> {
                // Runs 5 seconds after start
            }, TaskType.BUKKIT, 0);
        }
    }

    @Override
    public void onRemove() {
        // Cleanup
    }
}
```

Register event in `Plugin.java`:

```java
pluginContainer.getEventHolder().registerEvents(
    StormEvent.class,
    SomeEvent.class
    // Add your events here
);
```

## Configuration

The plugin uses `config.yml` with the following structure:

```yaml
queue:
  settings:
    time-offset-minutes: 0
  calls:
    "19:56": "storm-event"
    "00:28": "storm-event"

tasks:
  storm-event:
    id: "storm-event"
    data-container:
      pre-spawn-message: "What's happening at %x %z?!"
      event-world: "world"
      loot-item-1: "DIAMOND:20"
      holo-line-down: "Time left: %t1%/%t2%"
      schematic-offset: "-7 0 7"
      schematic-name: "sputnik2.schem"
```

- `queue.calls` — maps time (HH:MM) to event IDs
- `tasks.<id>.data-container` — arbitrary key-value pairs accessible via `DataContainer`

## Key Classes

| Class | Responsibility |
|-------|----------------|
| `Event` | Abstract base for all events |
| `EventProcessor` | Manages ticks, scheduled tasks, and lifecycle |
| `EventLoopService` | Triggers events based on system time |
| `DataContainer` | Provides access to `data-container` config section |
| `EventQueue` | Handles time offset and tracks executing events |

## Commands

- `/reload-plugin` — reloads configuration (console only)
