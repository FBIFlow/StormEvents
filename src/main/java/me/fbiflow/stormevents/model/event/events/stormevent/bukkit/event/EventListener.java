package me.fbiflow.stormevents.model.event.events.stormevent.bukkit.event;

import me.fbiflow.stormevents.model.event.events.stormevent.StormEvent;
import me.fbiflow.stormevents.model.event.events.stormevent.model.Triangle;
import me.fbiflow.stormevents.model.event.events.stormevent.model.Vertex2f;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

import static me.fbiflow.stormevents.model.event.events.stormevent.model.Vertex2f.ofLocation;
import static me.fbiflow.stormevents.model.event.events.stormevent.util.TrigonometryUtil.isPointInTriangle;

public class EventListener implements Listener {

    private final StormEvent event;

    private Triangle triangle;
    private Triangle bigTriangle;

    public EventListener(StormEvent event) {
        this.event = event;
        this.triangle = event.triangle;
        this.bigTriangle = event.bigTriangle;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Location location = event.getBlock().getLocation();
        if (triangle == null) {
            System.err.println(" TRIANGLE IS NULL");
        }
        boolean inTriangle = isPointInTriangle(triangle, ofLocation(location));
        event.getPlayer().sendMessage("you in triangle: " + inTriangle);
        if (inTriangle) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Location location = event.getBlock().getLocation();
        if (triangle == null) {
            System.err.println(" TRIANGLE IS NULL");
        }
        boolean inTriangle = isPointInTriangle(triangle, ofLocation(location));
        event.getPlayer().sendMessage("you in triangle: " + inTriangle);
        if (inTriangle) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        Location location = event.getPlayer().getLocation();
        if (triangle == null) {
            System.err.println(" TRIANGLE IS NULL");
        }
        if (event.getInventory().getLocation() != null) {
            location = event.getInventory().getLocation();
        }
        boolean inTriangle = isPointInTriangle(triangle, Vertex2f.ofLocation(location));
        event.getPlayer().sendMessage("you in triangle: " + inTriangle);
        if (!inTriangle) {
            return;
        }
        if (event.getInventory().getType() == org.bukkit.event.inventory.InventoryType.CHEST) {
            this.event.handleChestClick(event);
        }
    }

}
