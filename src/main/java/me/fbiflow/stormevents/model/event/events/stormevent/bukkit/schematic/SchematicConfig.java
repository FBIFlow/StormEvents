package me.fbiflow.stormevents.model.event.events.stormevent.bukkit.schematic;

import me.fbiflow.stormevents.Plugin;
import me.fbiflow.stormevents.util.FFLogger;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class SchematicConfig {

    private static final String CONFIG_PATH = "/schematics";
    private Map<String, File> schematicFiles;

    public SchematicConfig() {
        this.schematicFiles = new HashMap<>();
        File[] files = getFolder().listFiles(File::isFile);
        if (files == null) {
            return;
        }
        for (File file : files) {
            schematicFiles.put(file.getName(), file);
        }
        FFLogger.log(String.format("Found %s files in schematic folder: ", schematicFiles.keySet()));
    }

    public File getFileByName(String schematicFileName) {
        return schematicFiles.get(schematicFileName);
    }

    private File getFolder() {
        Plugin plugin = Plugin.instance;
        File schematicFolder = new File(plugin.getDataFolder() + CONFIG_PATH);
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        if (!schematicFolder.exists()) {
            try {
                schematicFolder.mkdir();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return schematicFolder;
    }

}