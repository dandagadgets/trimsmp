package com.trimsmp.ability.impl;

import com.trimsmp.ability.TrimAbility;
import com.trimsmp.trim.TrimPatternKind;
import com.trimsmp.trim.TrimTier;
import com.trimsmp.util.AbilityConfig;
import com.trimsmp.util.Effects;
import com.trimsmp.util.Targets;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/** Wayfinder: moves fast while sneaking; lunges quickly toward whoever you're looking at to close the gap. */
public final class WayfinderAbility implements TrimAbility {

    private final AbilityConfig config;
    private final Plugin plugin;

    public WayfinderAbility(AbilityConfig config, Plugin plugin) {
        this.config = config;
        this.plugin = plugin;
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
        int base = config.getInt("cooldown-seconds-base", 25);
        int reductionPerTier = config.getInt("cooldown-seconds-reduction-per-tier", 2);
        return Math.max(3, base - reductionPerTier * (tier.level() - 1)) * 20L;
    }

    @Override
    public void activate(Player player, TrimTier tier) {
        double range = config.getDouble("lunge-range", 20.0);
        double speed = config.getDouble("lunge-speed", 1.6) + config.getDouble("lunge-speed-per-tier", 0.1) * tier.level();

        LivingEntity target = Targets.lookingAt(player, range);
        Vector targetPoint = (target != null)
                ? target.getLocation().toVector()
                : player.getEyeLocation().toVector().add(player.getEyeLocation().getDirection().multiply(range));

        new BukkitRunnable() {
            int ticksRun = 0;

            @Override
            public void run() {
                if (!player.isOnline() || ticksRun++ > 15) {
                    cancel();
                    return;
                }
                Vector toTarget = targetPoint.clone().subtract(player.getLocation().toVector());
                if (toTarget.lengthSquared() < 3.0) {
                    cancel();
                    return;
                }
                Vector velocity = toTarget.normalize().multiply(speed);
                velocity.setY(Math.max(0.15, velocity.getY()));
                player.setVelocity(velocity);
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}
