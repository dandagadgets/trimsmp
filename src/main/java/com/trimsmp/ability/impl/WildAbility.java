package com.trimsmp.ability.impl;

import com.trimsmp.ability.TrimAbility;
import com.trimsmp.trim.TrimPatternKind;
import com.trimsmp.trim.TrimTier;
import com.trimsmp.util.AbilityConfig;
import com.trimsmp.util.CooldownManager;
import com.trimsmp.util.Effects;
import com.trimsmp.util.Targets;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.function.LongSupplier;

/** Wild: a grapple hook that pulls you to blocks or enemies (poisoning them); roots trap enemies when you're hurt badly. */
public final class WildAbility implements TrimAbility {

    private final AbilityConfig config;
    private final LongSupplier currentTick;
    private final Plugin plugin;
    private final CooldownManager rootTrapCooldown = new CooldownManager();

    public WildAbility(AbilityConfig config, LongSupplier currentTick, Plugin plugin) {
        this.config = config;
        this.currentTick = currentTick;
        this.plugin = plugin;
    }

    @Override
    public TrimPatternKind pattern() {
        return TrimPatternKind.WILD;
    }

    @Override
    public void tick(Player player, TrimTier tier) {
        Effects.refresh(player, PotionEffectType.REGENERATION, (tier.level() - 1) / 2, 30);
    }

    @Override
    public boolean hasActivePower() {
        return true;
    }

    @Override
    public long activationCooldownTicks(TrimTier tier) {
        int base = config.getInt("cooldown-seconds-base", 20);
        int reductionPerTier = config.getInt("cooldown-seconds-reduction-per-tier", 2);
        return Math.max(3, base - reductionPerTier * (tier.level() - 1)) * 20L;
    }

    @Override
    public void activate(Player player, TrimTier tier) {
        double maxRange = config.getDouble("grapple-range", 60.0);
        double speed = config.getDouble("grapple-speed", 1.8);
        int poisonTicks = config.getInt("poison-duration-seconds", 10) * 20;

        Vector direction = player.getEyeLocation().getDirection();
        RayTraceResult result = player.getWorld().rayTrace(player.getEyeLocation(), direction, maxRange,
                FluidCollisionMode.NEVER, true, 0.3, entity -> !entity.equals(player));

        Vector targetPoint = (result != null)
                ? result.getHitPosition()
                : player.getEyeLocation().toVector().add(direction.clone().multiply(maxRange));

        if (result != null && result.getHitEntity() instanceof LivingEntity target) {
            Effects.refresh(target, PotionEffectType.POISON, 0, poisonTicks);
            growGrassPatch(target.getLocation().clone(), player, tier);
        }

        new BukkitRunnable() {
            int ticksRun = 0;

            @Override
            public void run() {
                if (!player.isOnline() || ticksRun++ > 20) {
                    cancel();
                    return;
                }
                Vector toTarget = targetPoint.clone().subtract(player.getLocation().toVector());
                if (toTarget.lengthSquared() < 4.0) {
                    cancel();
                    return;
                }
                player.setVelocity(toTarget.normalize().multiply(speed));
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void growGrassPatch(Location patchLocation, Player player, TrimTier tier) {
        double patchRadius = config.getDouble("grass-patch-radius", 2.5);
        double patchDamage = config.getDouble("grass-patch-damage-base", 1.0) + config.getDouble("grass-patch-damage-per-tier", 0.2) * tier.level();
        int patchDurationTicks = config.getInt("grass-patch-duration-seconds", 4) * 20;
        int tickInterval = config.getInt("grass-patch-tick-interval", 10);

        new BukkitRunnable() {
            int elapsedTicks = 0;

            @Override
            public void run() {
                if (elapsedTicks >= patchDurationTicks) {
                    cancel();
                    return;
                }
                elapsedTicks += tickInterval;

                patchLocation.getWorld().spawnParticle(Particle.CRIT, patchLocation, 20,
                        patchRadius / 2, 0.3, patchRadius / 2, 0.05);

                for (Entity entity : patchLocation.getWorld().getNearbyEntities(patchLocation, patchRadius, 1.5, patchRadius)) {
                    if (entity instanceof LivingEntity living) {
                        living.damage(patchDamage, player);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, tickInterval);
    }

    @Override
    public void onIncomingDamage(Player player, TrimTier tier, EntityDamageEvent event) {
        double threshold = config.getDouble("root-trigger-health", 8.0);
        if (player.getHealth() - event.getFinalDamage() > threshold) {
            return;
        }
        long cooldownTicks = config.getInt("root-cooldown-seconds", 20) * 20L;
        if (!rootTrapCooldown.tryUse(player, "root_trap", currentTick.getAsLong(), cooldownTicks)) {
            return;
        }

        double radiusXZ = config.getDouble("root-radius-xz", 5.0);
        double radiusY = config.getDouble("root-radius-y", 3.0);
        int rootTicks = config.getInt("root-duration-seconds", 10) * 20;

        for (LivingEntity nearby : Targets.nearbyLiving(player, radiusXZ, radiusY)) {
            Effects.refresh(nearby, PotionEffectType.SLOWNESS, 9, rootTicks);
        }
    }
}
