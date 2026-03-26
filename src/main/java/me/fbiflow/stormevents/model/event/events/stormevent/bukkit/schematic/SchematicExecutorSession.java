package me.fbiflow.stormevents.model.event.events.stormevent.bukkit.schematic;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import me.fbiflow.stormevents.Plugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class SchematicExecutorSession {

    private final SchematicConfig config;
    private EditSession editSession;

    public SchematicExecutorSession() {
        this.config = new SchematicConfig();
    }

    public void pasteUFO(Location location, String schematicFileName) {
        new BukkitRunnable() {
            @Override
            public void run() {
                File schematicFile = config.getFileByName(schematicFileName);
                if (schematicFile == null) {
                    throw new RuntimeException("Could not find schematic file: " + schematicFileName);
                }

                ClipboardFormat clipboardFormat = ClipboardFormats.findByFile(schematicFile);
                if (clipboardFormat == null) {
                    throw new RuntimeException("Unsupported schematic format for file: " + schematicFileName);
                }

                Clipboard clipboard;
                try (ClipboardReader clipboardReader = clipboardFormat.getReader(Files.newInputStream(schematicFile.toPath()))) {
                    clipboard = clipboardReader.read();
                } catch (IOException exception) {
                    exception.printStackTrace();
                    return;
                }

                try (EditSession newEditSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(location.getWorld()))) {
                    Operation operation = new ClipboardHolder(clipboard)
                            .createPaste(newEditSession)
                            .to(BlockVector3.at(location.getX(), location.getY(), location.getZ()))
                            .build();
                    Operations.complete(operation);
                    editSession = newEditSession;
                } catch (WorldEditException e) {
                    e.printStackTrace();
                }
            }
        }.runTask(Plugin.instance);
    }

    public void removeUFO() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (editSession != null) {
                    try {
                        EditSession newEditSession = WorldEdit.getInstance().newEditSession(editSession.getWorld());
                        editSession.undo(newEditSession);
                        newEditSession.close();
                        editSession.close();
                        editSession = null;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }.runTask(Plugin.instance);
    }
}