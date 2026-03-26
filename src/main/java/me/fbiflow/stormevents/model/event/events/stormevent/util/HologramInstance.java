package me.fbiflow.stormevents.model.event.events.stormevent.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;

import java.util.ArrayList;
import java.util.List;

public class HologramInstance {
    private final List<ArmorStand> armorStands = new ArrayList<>();
    private final Location location;
    private final double lineSpacing; // Отступ между строками

    public HologramInstance(Location location, double lineSpacing) {
        this.location = location;
        this.lineSpacing = lineSpacing;
    }

    public void createHologram(List<String> lines) {
        for (int i = 0; i < lines.size(); i++) {
            ArmorStand armorStand = location.getWorld().spawn(location.clone().add(0, i * lineSpacing, 0), ArmorStand.class);
            armorStand.setCustomName(lines.get(i));
            armorStand.setCustomNameVisible(true);
            armorStand.setGravity(false);
            armorStand.setVisible(false);
            armorStand.setCanPickupItems(false);
            armorStand.setHelmet(new org.bukkit.inventory.ItemStack(Material.AIR));
            armorStand.setChestplate(new org.bukkit.inventory.ItemStack(Material.AIR));
            armorStand.setLeggings(new org.bukkit.inventory.ItemStack(Material.AIR));
            armorStand.setBoots(new org.bukkit.inventory.ItemStack(Material.AIR));
            armorStands.add(armorStand);
        }
    }

    public void deleteHologram() {
        for (ArmorStand armorStand : armorStands) {
            armorStand.remove();
        }
        armorStands.clear();
    }

    public void updateHologram(List<String> newLines) {
        for (int i = 0; i < newLines.size(); i++) {
            ArmorStand armorStand = armorStands.get(i);
            if (armorStand == null) {
                break;
            }
            armorStand.setCustomName(newLines.get(i));
        }
    }

    public List<ArmorStand> getArmorStands() {
        return armorStands;
    }
}
