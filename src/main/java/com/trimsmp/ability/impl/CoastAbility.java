package com.trimsmp.ability.impl;

import com.trimsmp.ability.TrimAbility;
import com.trimsmp.trim.TrimPatternKind;
import com.trimsmp.trim.TrimTier;
import com.trimsmp.util.AbilityConfig;
import com.trimsmp.util.Effects;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Coast: permanent Dolphin's Grace; Tsunami - floods the area with a real wave of water, then drains away. */
public final class CoastAbility implements TrimAbility {

    private final AbilityConfig config;
    private final Plugin plugin;

    public CoastAbility(AbilityConfig config, Plugin plugin) {
        this.config = config;
        this.plugin = plugin;
    }

    @Override
    public TrimPatternKind pattern() {
        return TrimPatternKind.COAST;
    }

    @Override
    public void tick(Player player, TrimTier tier) {
        Effects.refresh(player, PotionEffectType.DOLPHINS_GRACE, (tier.level() - 1) / 2, 30);
    }

    @Override
    public boolean hasActivePower() {
        return true;
    }

    @Override
    public long activationCooldownTicks(TrimTier tier) {
        int base = config.getInt("cooldown-seconds-base", 60);
        int reductionPerTier = config.getInt("cooldown-seconds-reduction-per-tier", 6);
        return Math.max(5, base - reductionPerTier * (tier.level() - 1)) * 20L;
    }

    @Override
    public void activate(Player player, TrimTier tier) {
        double radius = config.getDouble("radius", 8.0) + config.getDouble("radius-per-tier", 0.5) * tier.level();
        double damage = config.getDouble("damage-base", 6.0) + config.getDouble("damage-per-tier", 1.0) * tier.level();
        int debuffTicks = config.getInt("debuff-duration-seconds", 4) * 20;
        int floodDurationTicks = config.getInt("flood-duration-seconds", 4) * 20;
        int pulseCount = config.getInt("pulse-count", 4);
        int pulseIntervalTicks = Math.max(1, floodDurationTicks / pulseCount);

        Location origin = player.getLocation();
        UUID casterId = player.getUniqueId();
        int radiusBlocks = (int) Math.ceil(radius);
        double radiusSquared = radius * radius;
        List<Block> flooded = new ArrayList<>();

        for (int dx = -radiusBlocks; dx <= radiusBlocks; dx++) {
            for (int dz = -radiusBlocks; dz <= radiusBlocks; dz++) {
                if (dx * dx + dz * dz > radiusSquared) {
                    continue;
                }
                for (int dy = 0; dy <= 1; dy++) {
                    Block block = origin.clone().add(dx, dy, dz).getBlock();
                    if (block.getType().isAir()) {
                        block.setType(Material.WATER);
                        flooded.add(block);
                    }
                }
            }
        }
        origin.getWorld().playSound(origin, Sound.ENTITY_GENERIC_SPLASH, 2.0f, 0.5f);

        new BukkitRunnable() {
            int pulsesRun = 0;

            @Override
            public void run() {
                if (pulsesRun++ >= pulseCount) {
                    cancel();
                    return;
                }
                for (Entity entity : origin.getWorld().getNearbyEntities(origin, radius, 2.0, radius)) {
                    if (entity.getUniqueId().equals(casterId) || !(entity instanceof LivingEntity nearby)) {
                        continue;
                    }
                    nearby.damage(damage, player);
                    Effects.refresh(nearby, PotionEffectType.WEAKNESS, 1, debuffTicks);
                    Effects.refresh(nearby, PotionEffectType.SLOWNESS, 1, debuffTicks);
                }
            }
        }.runTaskTimer(plugin, 0L, pulseIntervalTicks);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Block block : flooded) {
                if (block.getType() == Material.WATER) {
                    block.setType(Material.AIR);
                }
            }
        }, floodDurationTicks);
    }
}
