package me.fbiflow.stormevents.util.validator.implementation.yaml;

import me.fbiflow.stormevents.util.validator.ValidState;
import me.fbiflow.stormevents.util.validator.ValidationResult;
import me.fbiflow.stormevents.util.validator.ValidFilter;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Set;
import java.util.stream.Collectors;

public class QueueCallsValidFilter implements ValidFilter {

    @Override
    public ValidationResult apply(Object configurationSection) {
        if (configurationSection == null) {
            return new ValidationResult(ValidState.NULL);
        }

        ConfigurationSection section;

        try {
             section = (ConfigurationSection) configurationSection;
        } catch (ClassCastException exception) {
            return new ValidationResult(ValidState.EXCEPTION, "Invalid method parameter, required ConfigurationSection, " +
                    "but accessed " + configurationSection.getClass().getName());
        }

        Set<String> callingTasks = section.getKeys(false);

        ConfigurationSection tasksSection = section
                .getParent()
                .getParent()
                .getConfigurationSection("tasks");

        System.out.println("KEYSET IS: " + callingTasks);

        for (String key : callingTasks) {
            if (key.length() != 5) {
                return new ValidationResult(ValidState.INVALID, "Time string length may have 5 chars." +
                        " Example: '15:31', but current value is: " + key);
            }
            int hour, minute;
            try {
                hour = Integer.parseInt(key.substring(0, 2));
                minute = Integer.parseInt(key.substring(3));
            } catch (NumberFormatException exception) {
                return new ValidationResult(ValidState.EXCEPTION, "Invalid number format");
            }

            if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
                return new ValidationResult(ValidState.INVALID, "Invalid time, may be at least 00:00 and less then 23:59");
            }

            Set<String> taskTypes = tasksSection.getKeys(false)
                    .stream()
                    .map(x -> tasksSection.getString(String.format("%s.id", x)))
                    .collect(Collectors.toSet());

            if (!taskTypes.contains(section.getString(key))) {
                return new ValidationResult(
                        ValidState.INVALID,
                        String.format("Could not to find task with id '%s' in config", key)
                );
            }
        }
        return new ValidationResult(ValidState.VALID);
    }
}
