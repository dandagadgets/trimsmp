package com.trimsmp.util;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Shared helpers for scanning nearby entities, used by several trim active abilities. */
public final class Targets {

    private Targets() {
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
