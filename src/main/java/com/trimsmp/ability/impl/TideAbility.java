package com.trimsmp.ability.impl;

import com.trimsmp.ability.TrimAbility;
import com.trimsmp.trim.TrimPatternKind;
import com.trimsmp.trim.TrimTier;
import com.trimsmp.util.AbilityConfig;
import com.trimsmp.util.Effects;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/** Tide: a wall of water surges forward, pushing back and slowing everything caught in it. */
public final class TideAbility implements TrimAbility {

    private final AbilityConfig config;
    private final Plugin plugin;

    public TideAbility(AbilityConfig config, Plugin plugin) {
        this.config = config;
        this.plugin = plugin;
    }

    @Override
    public TrimPatternKind pattern() {
        return TrimPatternKind.TIDE;
    }

    @Override
    public void tick(Player player, TrimTier tier) {
        Effects.refresh(player, PotionEffectType.DOLPHINS_GRACE, 2 + (tier.level() - 1) / 3, 30);
    }

    @Override
    public boolean hasActivePower() {
        return true;
    }

    @Override
    public long activationCooldownTicks(TrimTier tier) {
        int base = config.getInt("cooldown-seconds-base", 120);
        int reductionPerTier = config.getInt("cooldown-seconds-reduction-per-tier", 12);
        return Math.max(10, base - reductionPerTier * (tier.level() - 1)) * 20L;
    }

    @Override
    public void activate(Player player, TrimTier tier) {
        double waveWidth = config.getDouble("wave-width", 3.0);
        double wallHeight = config.getDouble("wall-height", 6.0);
        int effectTicks = config.getInt("effect-duration-seconds", 15) * 20;
        double knockback = config.getDouble("knockback-strength", 1.8);
        int moveDelay = config.getInt("move-delay-ticks", 2);
        int maxMoves = config.getInt("max-moves", 20);

        Vector direction = player.getLocation().getDirection().setY(0).normalize();
        Location origin = player.getLocation();

        new BukkitRunnable() {
            int moves = 0;

            @Override
            public void run() {
                if (!player.isOnline() || moves >= maxMoves) {
                    cancel();
                    return;
                }
                moves++;
                Location point = origin.clone().add(direction.clone().multiply(moves * 1.5));
                point.getWorld().spawnParticle(Particle.SPLASH, point, 25,
                        waveWidth / 2, wallHeight / 2, waveWidth / 2, 0.05);

                for (Entity entity : point.getWorld().getNearbyEntities(point, waveWidth, wallHeight, waveWidth)) {
                    if (entity instanceof LivingEntity living && !entity.equals(player)) {
                        Vector push = direction.clone().multiply(knockback);
                        push.setY(Math.max(0.3, push.getY()));
                        living.setVelocity(living.getVelocity().add(push));
                        Effects.refresh(living, PotionEffectType.SLOWNESS, 1, effectTicks);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, moveDelay);
    }
}
