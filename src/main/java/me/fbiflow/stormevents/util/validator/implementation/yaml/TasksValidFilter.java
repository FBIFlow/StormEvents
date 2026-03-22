package me.fbiflow.stormevents.util.validator.implementation.yaml;

import me.fbiflow.stormevents.util.validator.ValidFilter;
import me.fbiflow.stormevents.util.validator.ValidState;
import me.fbiflow.stormevents.util.validator.ValidationResult;
import org.bukkit.configuration.ConfigurationSection;

public class TasksValidFilter implements ValidFilter {

    @Override
    public ValidationResult apply(Object configurationSection) {
        if (configurationSection == null) {
            return new ValidationResult(ValidState.NULL);
        }
        ConfigurationSection tasks = (ConfigurationSection) configurationSection;

        for (String taskKey : tasks.getKeys(false)) {
            ConfigurationSection task = tasks.getConfigurationSection(taskKey);
            String id = task.getString("id");
            if (id == null || id.isEmpty()) {
                return new ValidationResult(ValidState.INVALID, "Id is null or empty");
            }
            ConfigurationSection dataContainer = task.getConfigurationSection("data-container");
            if (dataContainer == null || dataContainer.getKeys(false).isEmpty()) {
                return new ValidationResult(ValidState.VALID);
            }
            for (String key : dataContainer.getKeys(false)) {
                if (dataContainer.get(key) == null) {
                    return new ValidationResult(ValidState.NULL);
                }
            }
        }
        return new ValidationResult(ValidState.VALID);
    }
}