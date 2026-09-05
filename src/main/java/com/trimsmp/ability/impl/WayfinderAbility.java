package com.trimsmp.ability.impl;

import com.trimsmp.ability.TrimAbility;
import com.trimsmp.trim.TrimPatternKind;
import com.trimsmp.trim.TrimTier;
import com.trimsmp.util.AbilityConfig;
import com.trimsmp.util.Effects;
import com.trimsmp.util.Msg;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Wayfinder: always knows the way home, and finds a rhythm on a long sprint. */
public final class WayfinderAbility implements TrimAbility {

    private static final String[] COMPASS = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};

    private final AbilityConfig config;
    private final int passiveDurationTicks;
    private final Map<UUID, Long> sprintStartMillis = new HashMap<>();

    public WayfinderAbility(AbilityConfig config, int passiveDurationTicks) {
        this.config = config;
        this.passiveDurationTicks = passiveDurationTicks;
    }

    @Override
    public TrimPatternKind pattern() {
        return TrimPatternKind.WAYFINDER;
    }

    @Override
    public void tick(Player player, TrimTier tier) {
        Location home = player.getBedSpawnLocation();
        Location target = (home != null && home.getWorld() == player.getWorld())
                ? home
                : player.getWorld().getSpawnLocation();

        double dx = target.getX() - player.getX();
        double dz = target.getZ() - player.getZ();
        double distance = Math.sqrt(dx * dx + dz * dz);
        String direction = bearing(dx, dz);
        Msg.actionBar(player, "&d✦ &7" + direction + " &f" + Math.round(distance) + "m &7to home");

        long now = System.currentTimeMillis();
        UUID id = player.getUniqueId();
        if (player.isSprinting()) {
            long startedAt = sprintStartMillis.computeIfAbsent(id, k -> now);
            int requiredSeconds = config.getInt("speed-seconds", 4);
            if (now - startedAt >= requiredSeconds * 1000L) {
                Effects.refresh(player, PotionEffectType.SPEED, tier.level() - 1, passiveDurationTicks);
            }
        } else {
            sprintStartMillis.remove(id);
        }
    }

    private static String bearing(double dx, double dz) {
        double angle = Math.toDegrees(Math.atan2(dx, -dz));
        if (angle < 0) {
            angle += 360;
        }
        int index = (int) Math.round(angle / 45.0) % 8;
        return COMPASS[index];
    }
}
