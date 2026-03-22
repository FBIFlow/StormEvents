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
import com.sk89q.worldedit.world.World;
import me.fbiflow.stormevents.Plugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;

public class SchematicExecutorSession {

    private final SchematicConfig config;

    private World weWorld;
    private static volatile EditSession editSession;

    public SchematicExecutorSession() {
        this.weWorld = BukkitAdapter.adapt(Objects.requireNonNull(Bukkit.getWorld("world")));
        this.config = new SchematicConfig();
    }

    public void pasteUFO(Location location, String schematicFileName) {
        // Читаем файл схемы вне BukkitRunnable
        // Создаем BukkitRunnable для выполнения вставки в основном потоке
        new BukkitRunnable() {
            @Override
            public void run() {
                File schematicFile = config.getFileByName(schematicFileName);

                if (schematicFile == null) {
                    throw new RuntimeException("Could not find schematic file: " + schematicFileName);
                }

                Clipboard clipboard;
                ClipboardFormat clipboardFormat = ClipboardFormats.findByFile(schematicFile);
                if (clipboardFormat == null) {
                    throw new RuntimeException("Unsupported schematic format for file: " + schematicFileName);
                }

                // Читаем схему из файла
                try (ClipboardReader clipboardReader = clipboardFormat.getReader(Files.newInputStream(schematicFile.toPath()))) {
                    clipboard = clipboardReader.read();
                } catch (IOException exception) {
                    exception.printStackTrace();
                    return;
                }
                // Создаем EditSession в основном потоке
                try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(location.getWorld()))) {
                    Operation operation = new ClipboardHolder(clipboard)
                            .createPaste(editSession)
                            .to(BlockVector3.at(location.getX(), location.getY(), location.getZ()))
                            .build();

                    // Выполняем операцию вставки
                    Operations.complete(operation);
                    SchematicExecutorSession.editSession = editSession;
                } catch (WorldEditException e) {
                    // Логирование ошибок при выполнении операции
                    e.printStackTrace();
                } catch (Exception e) {
                    // Логирование ошибок при создании EditSession
                    e.printStackTrace();
                }
            }
        }.runTask(Plugin.instance); // Запускаем задачу в основном потоке
    }



    public void removeUFO() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (editSession != null) {
                    try {
                        // Создаем новый EditSession для выполнения отмены
                        EditSession newEditSession = WorldEdit.getInstance().newEditSession(weWorld);
                        editSession.undo(newEditSession);
                    } finally {
                        // Закрываем editSession после использования
                        editSession.close();
                        editSession = null; // Сбрасываем ссылку
                    }
                } else {
                    System.err.println("No edit session available to undo.");
                }
            }
        }.runTask(Plugin.instance);

    }

}