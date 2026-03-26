package me.fbiflow.stormevents.model.event.events.stormevent;

import me.fbiflow.stormevents.Plugin;
import me.fbiflow.stormevents.model.enums.TaskType;
import me.fbiflow.stormevents.model.enums.TimeUnit;
import me.fbiflow.stormevents.model.event.Event;
import me.fbiflow.stormevents.model.event.events.stormevent.bukkit.effects.ParticleSpawner;
import me.fbiflow.stormevents.model.event.events.stormevent.bukkit.event.EventListener;
import me.fbiflow.stormevents.model.event.events.stormevent.bukkit.schematic.SchematicExecutorSession;
import me.fbiflow.stormevents.model.event.events.stormevent.model.Triangle;
import me.fbiflow.stormevents.model.event.events.stormevent.model.Vertex2f;
import me.fbiflow.stormevents.model.event.events.stormevent.util.HologramInstance;
import me.fbiflow.stormevents.model.event.events.stormevent.util.LocationFindUtil;
import me.fbiflow.stormevents.util.FFLogger;
import me.fbiflow.stormevents.util.TimeConverter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import static me.fbiflow.stormevents.model.event.events.stormevent.model.Vertex2f.ofLocation;
import static me.fbiflow.stormevents.model.event.events.stormevent.util.TrigonometryUtil.isPointInTriangle;
import static me.fbiflow.stormevents.model.event.events.stormevent.util.TrigonometryUtil.scaleTriangle;

public class StormEvent extends Event {

    private final World eventWorld = Bukkit.getWorld((String) dataContainer.get("event-world"));
    private final List<ItemStack> lootItems = new ArrayList<>();
    private final SchematicExecutorSession schematicExecutor = new SchematicExecutorSession();
    private final String SCHEMATIC_NAME = ((String) dataContainer.get("schematic-name"));
    private final List<PotionEffectType> badEffects = List.of(PotionEffectType.BLINDNESS, PotionEffectType.CONFUSION);
    private final List<String> hologramLines = new ArrayList<>();

    public Triangle triangle;
    public Triangle bigTriangle;
    private EventListener eventListener;
    private Location location;
    private HologramInstance hologramInstance;
    private boolean chestFilling = false;

    public StormEvent() {
        super();
        registerTask(() -> {
            FFLogger.log("SPAWNING AN STRUCTURE");
        }, TaskType.THREADED, 0);
    }

    @Override
    public String getId() {
        return "storm-event";
    }

    @Override
    public int getLiveTimeTicks() {
        return TimeConverter.ticksOf(TimeUnit.MINUTES, 5);
    }

    @Override
    public void onInit() {
        location = LocationFindUtil.getLocation(eventWorld);
        if (location == null) {
            FFLogger.log("Could not find suitable location for StormEvent");
            return;
        }

        Bukkit.getOnlinePlayers().forEach(x -> x.sendMessage("LOCATION IS: " + location));

        triangle = new Triangle(
                ofLocation(location.clone().add(25, 0, 0)),
                ofLocation(location.clone().add(-20, 0, -25)),
                ofLocation(location.clone().add(-20, 0, 25))
        );

        Bukkit.getOnlinePlayers().forEach(x -> x.sendMessage(String.format("TRIANGLE IS: %s %s %s", triangle.A, triangle.B, triangle.C)));
        bigTriangle = scaleTriangle(triangle, 15);
        eventListener = new EventListener(this);

        Set<String> holoLineKeys = dataContainer.getKeys().stream()
                .filter(item -> item.contains("holo-line-"))
                .collect(Collectors.toSet());
        holoLineKeys.forEach(key -> {
            String line = (String) dataContainer.get(key);
            hologramLines.add(line);
        });

        Set<String> lootKeys = dataContainer.getKeys().stream()
                .filter(item -> item.contains("loot-item-"))
                .collect(Collectors.toSet());
        lootKeys.forEach(loot -> {
            String data = (String) dataContainer.get(loot);
            String[] lootData = data.split(":");
            Material material = Material.valueOf(lootData[0]);
            int amount = Integer.parseInt(lootData[1]);
            ItemStack item = new ItemStack(material, amount);
            lootItems.add(item);
        });

        String[] offsets = ((String) dataContainer.get("schematic-offset")).split(" ");
        Location addition = new Location(eventWorld, Double.parseDouble(offsets[0]), Double.parseDouble(offsets[1]), Double.parseDouble(offsets[2]));

        Bukkit.getPluginManager().registerEvents(eventListener, Plugin.instance);
        schematicExecutor.pasteUFO(location.clone().add(addition), SCHEMATIC_NAME);
        this.initialized = true;
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public void onRemove() {
        HandlerList.unregisterAll(eventListener);
        schematicExecutor.removeUFO();
        if (hologramInstance != null) {
            hologramInstance.deleteHologram();
        }
        FFLogger.log("called onRemove");
    }

    @Override
    public void onTick(int eventTick) {
        performPotionEffects();
        if (eventTick % 25 == 0 && location != null) {
            ParticleSpawner.drawTriangle(location.getWorld(), location.clone().add(0, 3, 0).getBlockY(), triangle, 10);
            ParticleSpawner.drawTriangle(location.getWorld(), location.clone().add(0, 6, 0).getBlockY(), triangle, 10);
        }
    }

    public void handleChestClick(InventoryOpenEvent event) {
        if (!event.getInventory().isEmpty() && event.getInventory().getType() == InventoryType.CHEST) {
            return;
        }
        if (!chestFilling) {
            fillChest(event.getInventory());
            hologramInstance = new HologramInstance(event.getInventory().getLocation(), 0.2);
            hologramInstance.createHologram(hologramLines);
        }
        event.setCancelled(true);
    }

    private void fillChest(Inventory chest) {
        int maxChestTimeSeconds = 60;
        chestFilling = true;

        BukkitTask task = new BukkitRunnable() {
            int chestTimer = 0;

            @Override
            public void run() {
                List<String> newLines = new ArrayList<>();
                for (int i = 0; i < hologramLines.size(); i++) {
                    String newLine = hologramLines.get(i)
                            .replace("%t1%", String.valueOf(chestTimer))
                            .replace("%t2%", String.valueOf(maxChestTimeSeconds));
                    newLines.add(i, newLine);
                }
                if (hologramInstance != null) {
                    hologramInstance.updateHologram(newLines);
                }
                chestTimer++;
            }
        }.runTaskTimer(Plugin.instance, 20, 20);
        this.bukkitTasks.add(task);

        BukkitTask task1 = new BukkitRunnable() {
            @Override
            public void run() {
                int size = chest.getSize();
                int slotsToFill = (int) (size * 0.6);
                Random random = new Random();
                List<Integer> filledSlots = new ArrayList<>();

                while (filledSlots.size() < slotsToFill) {
                    int slot = random.nextInt(size);
                    if (!filledSlots.contains(slot)) {
                        filledSlots.add(slot);
                        ItemStack randomItem = lootItems.get(random.nextInt(lootItems.size()));
                        int amountToAdd = random.nextInt(randomItem.getAmount()) + 1;
                        ItemStack itemToAdd = new ItemStack(randomItem.getType(), amountToAdd);
                        chest.setItem(slot, itemToAdd);
                    }
                }
            }
        }.runTaskLater(Plugin.instance, TimeConverter.ticksOf(TimeUnit.SECONDS, maxChestTimeSeconds));
        this.bukkitTasks.add(task1);
    }

    private void performPotionEffects() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (location == null || triangle == null) {
                    return;
                }
                List<Player> players = Bukkit.getOnlinePlayers().stream()
                        .filter(player -> player.getWorld() == eventWorld)
                        .collect(Collectors.toList());
                players.forEach(player -> {
                    Location l = player.getLocation();
                    Vertex2f playerPoint = new Vertex2f((float) l.getX(), (float) l.getZ());
                    if (isPointInTriangle(triangle, playerPoint)) {
                        badEffects.forEach(effectType ->
                                player.addPotionEffect(new PotionEffect(effectType, 3, 1, true, false, false))
                        );
                    } else {
                        badEffects.forEach(player::removePotionEffect);
                    }
                });
            }
        }.runTask(Plugin.instance);
    }
}