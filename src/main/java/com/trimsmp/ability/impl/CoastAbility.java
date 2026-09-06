package com.trimsmp.ability.impl;

import com.trimsmp.ability.TrimAbility;
import com.trimsmp.trim.TrimPatternKind;
import com.trimsmp.trim.TrimTier;
import com.trimsmp.util.AbilityConfig;
import com.trimsmp.util.Effects;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

/** Coast: permanent Dolphin's Grace; Tsunami - a huge wave spawns around you, damaging everything caught in it, then vanishes. */
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
        double radius = config.getDouble("radius", 10.0) + config.getDouble("radius-per-tier", 1.0) * tier.level();
        double damage = config.getDouble("damage-base", 6.0) + config.getDouble("damage-per-tier", 1.0) * tier.level();
        int debuffTicks = config.getInt("debuff-duration-seconds", 4) * 20;
        int pulseCount = config.getInt("pulse-count", 3);
        int pulseIntervalTicks = config.getInt("pulse-interval-ticks", 8);

        Location origin = player.getLocation();
        UUID casterId = player.getUniqueId();

        new BukkitRunnable() {
            int pulsesRun = 0;

            @Override
            public void run() {
                if (pulsesRun++ >= pulseCount) {
                    cancel();
                    return;
                }
                origin.getWorld().spawnParticle(Particle.SPLASH, origin, 80, radius / 2, 1.0, radius / 2, 0.1);
                origin.getWorld().playSound(origin, Sound.ENTITY_GENERIC_SPLASH, 2.0f, 0.6f);

                for (Entity entity : origin.getWorld().getNearbyEntities(origin, radius, radius, radius)) {
                    if (entity.getUniqueId().equals(casterId) || !(entity instanceof LivingEntity nearby)) {
                        continue;
                    }
                    nearby.damage(damage, player);
                    Effects.refresh(nearby, PotionEffectType.WEAKNESS, 1, debuffTicks);
                    Effects.refresh(nearby, PotionEffectType.SLOWNESS, 1, debuffTicks);
                }
            }
        }.runTaskTimer(plugin, 0L, pulseIntervalTicks);
    }
}
