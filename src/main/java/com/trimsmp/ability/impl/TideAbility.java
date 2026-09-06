package com.trimsmp.ability.impl;

import com.trimsmp.ability.TrimAbility;
import com.trimsmp.trim.TrimPatternKind;
import com.trimsmp.trim.TrimTier;
import com.trimsmp.util.AbilityConfig;
import com.trimsmp.util.Effects;
import com.trimsmp.util.Targets;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Tide: Dolphin's Grace III; Tidal Surge - ride a current forward, damaging and knocking aside anything in your path. */
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
        int base = config.getInt("cooldown-seconds-base", 45);
        int reductionPerTier = config.getInt("cooldown-seconds-reduction-per-tier", 4);
        return Math.max(5, base - reductionPerTier * (tier.level() - 1)) * 20L;
    }

    @Override
    public void activate(Player player, TrimTier tier) {
        double surgeSpeed = config.getDouble("surge-speed", 1.8) + config.getDouble("surge-speed-per-tier", 0.1) * tier.level();
        double damage = config.getDouble("damage-base", 4.0) + config.getDouble("damage-per-tier", 0.6) * tier.level();
        double knockback = config.getDouble("knockback-strength", 1.4);

        Vector direction = player.getLocation().getDirection().normalize();
        Vector velocity = direction.clone().multiply(surgeSpeed);
        velocity.setY(Math.max(0.2, velocity.getY()));
        player.setVelocity(velocity);

        Set<UUID> alreadyHit = new HashSet<>();
        new BukkitRunnable() {
            int ticksRun = 0;

            @Override
            public void run() {
                if (!player.isOnline() || ticksRun++ > 12) {
                    cancel();
                    return;
                }
                player.getWorld().spawnParticle(Particle.SPLASH, player.getLocation(), 15, 0.4, 0.4, 0.4, 0.05);

                for (LivingEntity nearby : Targets.nearbyLiving(player, 2.0, 2.0)) {
                    if (!alreadyHit.add(nearby.getUniqueId())) {
                        continue;
                    }
                    nearby.damage(damage, player);
                    Vector push = nearby.getLocation().toVector().subtract(player.getLocation().toVector());
                    if (push.lengthSquared() > 0.01) {
                        push.normalize().multiply(knockback);
                        push.setY(Math.max(0.3, push.getY()));
                        nearby.setVelocity(nearby.getVelocity().add(push));
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}
