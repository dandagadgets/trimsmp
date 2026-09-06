package com.trimsmp.util;

import org.bukkit.FluidCollisionMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Shared helpers for scanning nearby entities, used by several trim active abilities. */
public final class Targets {

    private Targets() {
    }

    /** The living entity the player is directly looking at within range, or null if none (line of sight, blocks included). */
    public static LivingEntity lookingAt(Player player, double range) {
        RayTraceResult result = player.getWorld().rayTrace(player.getEyeLocation(), player.getEyeLocation().getDirection(),
                range, FluidCollisionMode.NEVER, true, 0.3, entity -> !entity.equals(player));
        if (result != null && result.getHitEntity() instanceof LivingEntity living) {
            return living;
        }
        return null;
    }

    public static List<LivingEntity> nearbyHostiles(Player player, double radius, int limit) {
        List<LivingEntity> found = new ArrayList<>();
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof Monster monster && monster instanceof LivingEntity living) {
                found.add(living);
            }
        }
        found.sort(Comparator.comparingDouble(e -> e.getLocation().distanceSquared(player.getLocation())));
        if (found.size() > limit) {
            return found.subList(0, limit);
        }
        return found;
    }

    /** Every nearby living entity (mobs and players alike) - getNearbyEntities never includes the caller itself. */
    public static List<LivingEntity> nearbyLiving(Player player, double radiusXZ, double radiusY) {
        return nearbyLiving((Entity) player, radiusXZ, radiusY);
    }

    /** Same as above, but centered on any entity (used to chain effects outward from a struck target). */
    public static List<LivingEntity> nearbyLiving(Entity center, double radiusXZ, double radiusY) {
        List<LivingEntity> found = new ArrayList<>();
        for (Entity entity : center.getNearbyEntities(radiusXZ, radiusY, radiusXZ)) {
            if (entity instanceof LivingEntity living) {
                found.add(living);
            }
        }
        return found;
    }
}
