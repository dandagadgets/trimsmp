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

/** Wayfinder: moves fast while sneaking; mark a location, then teleport back to it - any distance. */
public final class WayfinderAbility implements TrimAbility {

    private final AbilityConfig config;
    private final Map<UUID, Location> waypoints = new HashMap<>();

    public WayfinderAbility(AbilityConfig config) {
        this.config = config;
    }

    @Override
    public TrimPatternKind pattern() {
        return TrimPatternKind.WAYFINDER;
    }

    @Override
    public void tick(Player player, TrimTier tier) {
        if (player.isSneaking()) {
            Effects.refresh(player, PotionEffectType.SPEED, (tier.level() - 1) / 2, 30);
        }
    }

    @Override
    public boolean hasActivePower() {
        return true;
    }

    @Override
    public long activationCooldownTicks(TrimTier tier) {
        int base = config.getInt("cooldown-seconds-base", 120);
        int reductionPerTier = config.getInt("cooldown-seconds-reduction-per-tier", 12);
        return Math.max(5, base - reductionPerTier * (tier.level() - 1)) * 20L;
    }

    @Override
    public void activate(Player player, TrimTier tier) {
        UUID id = player.getUniqueId();
        Location marked = waypoints.get(id);
        if (marked == null || marked.getWorld() == null) {
            waypoints.put(id, player.getLocation().clone());
            Msg.actionBar(player, "&b✦ Waypoint set.");
        } else {
            player.teleport(marked);
            waypoints.remove(id);
            Msg.actionBar(player, "&b✦ Teleported to waypoint.");
        }
    }
}
