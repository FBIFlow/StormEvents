package me.fbiflow.stormevents.model.event.events.stormevent.bukkit.effects;

import me.fbiflow.stormevents.model.event.events.stormevent.model.Triangle;
import me.fbiflow.stormevents.model.event.events.stormevent.model.Vertex2f;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;

import static me.fbiflow.stormevents.model.event.events.stormevent.util.TrigonometryUtil.getInterpolatedWay;

public class ParticleSpawner {

    private static final Particle PARTICLE_TYPE = Particle.PORTAL;
    private static final int PARTICLE_COUNT = 10;

    public static void drawTriangle(World world, float height, Triangle triangle, int interpolation_rate) {
        Vertex2f[] ABWay = getInterpolatedWay(triangle.A, triangle.B, interpolation_rate);
        Vertex2f[] BCWay = getInterpolatedWay(triangle.B, triangle.C, interpolation_rate);
        Vertex2f[] ACWay = getInterpolatedWay(triangle.A, triangle.C, interpolation_rate);
        drawLine(world, height, ABWay);
        drawLine(world, height, BCWay);
        drawLine(world, height, ACWay);
    }

    private static void drawLine(World world, float height, Vertex2f[] way) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Arrays.stream(way).forEach(point -> {
                    Location location = new Location(world, point.x, height, point.y);
                    world.spawnParticle(PARTICLE_TYPE, location, PARTICLE_COUNT);
                });
            }
        }.run();
    }
}
