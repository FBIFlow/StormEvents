package me.fbiflow.stormevents.model.config.implementation.yaml;

import me.fbiflow.stormevents.Plugin;
import me.fbiflow.stormevents.model.config.Configuration;
import me.fbiflow.stormevents.model.config.DataContainer;
import me.fbiflow.stormevents.model.config.EventQueue;
import me.fbiflow.stormevents.model.config.implementation.DataContainerImpl;
import me.fbiflow.stormevents.util.validator.ValidState;
import me.fbiflow.stormevents.util.validator.ValidationResult;
import me.fbiflow.stormevents.util.validator.implementation.yaml.QueueCallsValidFilter;
import me.fbiflow.stormevents.util.validator.implementation.yaml.QueueTimeValidFilter;
import me.fbiflow.stormevents.util.validator.implementation.yaml.TasksValidFilter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ConfigurationImpl implements Configuration {

    private final String CONFIG_PATH = "config.yml";
    private YamlConfiguration yaml;

    private EventQueue eventQueue;

    public ConfigurationImpl() {
        init();
    }

    @Override
    public Configuration init() {
        this.yaml = YamlConfiguration.loadConfiguration(getFile());
        if (!checkValid(yaml)) {
            throw new RuntimeException("Incorrect config");
        }
        // k - timer, v - event id
        Map<Integer, String> configEvents = new HashMap<>();
        ConfigurationSection section = yaml.getConfigurationSection("queue.calls");
        section.getKeys(false)
                .forEach(key -> {
                    String[] time = key.split(":");
                    int hour = Integer.parseInt(time[0]);
                    int minute = Integer.parseInt(time[1]);
                    int timeMinutes = (hour * 60) + minute;
                    configEvents.put(
                            timeMinutes,
                            section.getString(key));
                });
        this.eventQueue = new EventQueue(
                yaml.getInt("queue.settings.time-offset-minutes"),
                configEvents
        );
        return this;
    }

    @Override
    public void reloadConfig() {
        init();
    }

    @Override
    public void saveConfig() {
        try {
            yaml.save(getFile());
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    @Nullable
    public DataContainer getDataContainer(String id) {
        Map<String, Object> data = new HashMap<>();
        ConfigurationSection task = findById(id);
        ConfigurationSection containerSection = task != null ? task.getConfigurationSection("data-container") : null;
        if (containerSection == null) {
            return null;
        }
        containerSection.getKeys(false).forEach(key -> {
            Object o = containerSection.get(key);
            if (o != null) {
                data.put(key, o);
            }
        });
        return new DataContainerImpl(data);
    }

    @Override
    public EventQueue getEventQueue() {
        return this.eventQueue;
    }

    private ConfigurationSection findById(String id) {
        return yaml.getConfigurationSection("tasks").getKeys(false).stream()
                .map(task -> yaml.getConfigurationSection(String.format("tasks.%s", task)))
                .filter(section -> section != null && section.getString("id")
                        .equals(id)).findFirst().orElse(null);
    }

    private boolean checkValid(YamlConfiguration yaml) {
        return check(new QueueTimeValidFilter(), yaml.getString("queue.settings.time-offset-minutes"))
                && check(new QueueCallsValidFilter(), yaml.getConfigurationSection("queue.calls"))
                && check(new TasksValidFilter(), yaml.getConfigurationSection("tasks"));
    }

    private boolean check(Function<Object, ValidationResult> validator, Object toCheck) {
        ValidationResult validationResult = validator.apply(toCheck);
        if (validationResult.state != ValidState.VALID) {
            Bukkit.getLogger().severe(validationResult.message + " " + validator.getClass());
            return false;
        }
        return true;
    }

    private File getFile() {
        Plugin plugin = Plugin.instance;
        File configFile = new File(plugin.getDataFolder(), CONFIG_PATH);
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        if (!configFile.exists()) {
            try {
                plugin.saveResource(CONFIG_PATH, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return configFile;
    }
}