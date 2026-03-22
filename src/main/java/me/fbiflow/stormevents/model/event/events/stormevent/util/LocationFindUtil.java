package me.fbiflow.stormevents.model.event.events.stormevent.util;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.lang.Math.abs;

public class LocationFindUtil {

    private static final int RANGE = 3000;
    private static final int MAX_HEIGHT = 100;
    private static final List<Biome> allowedBiomes = new ArrayList<>(List.of(
            Biome.PLAINS
    ));

    public static Location getLocation(World world) {
        final Location location;
                int x = getRandomInt();
                int z = getRandomInt();
                location = new Location(world, x, world.getHighestBlockYAt(x, z), z);
                if (world.getHighestBlockAt(location).getLocation().getY() > MAX_HEIGHT) {
                    return null;
                }
                Biome biome = world.getBiome(
                        location.getBlockX(),
                        location.getBlockY(),
                        location.getBlockZ());
                if (!allowedBiomes.contains(biome)) {
                    return null;
                }
        return location;
    }

    private static int getRandomInt() {
        return new Random().nextInt(abs(RANGE*2)) - abs(RANGE);
    }

}
