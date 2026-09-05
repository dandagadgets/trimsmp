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

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Spire: dashes forward, slamming everything in the path with bonus damage and knockback. */
public final class SpireAbility implements TrimAbility {

    private final AbilityConfig config;
    private final Plugin plugin;

    public SpireAbility(AbilityConfig config, Plugin plugin) {
        this.config = config;
        this.plugin = plugin;
    }

    @Override
    public TrimPatternKind pattern() {
        return TrimPatternKind.SPIRE;
    }

    @Override
    public void tick(Player player, TrimTier tier) {
        Effects.refresh(player, PotionEffectType.SPEED, 1 + (tier.level() - 1) / 3, 30);
    }

    @Override
    public boolean hasActivePower() {
        return true;
    }

    @Override
    public long activationCooldownTicks(TrimTier tier) {
        int base = config.getInt("cooldown-seconds-base", 30);
        int reductionPerTier = config.getInt("cooldown-seconds-reduction-per-tier", 3);
        return Math.max(3, base - reductionPerTier * (tier.level() - 1)) * 20L;
    }

    @Override
    public void activate(Player player, TrimTier tier) {
        double dashSpeed = config.getDouble("dash-speed", 2.0);
        double damageBase = config.getDouble("dash-damage-base", 4.0) + config.getDouble("dash-damage-per-tier", 0.6) * tier.level();
        double amplifier = 1.0 + config.getDouble("damage-amplification", 0.6);

        Vector direction = player.getLocation().getDirection().normalize();
        Vector dash = direction.clone().multiply(dashSpeed);
        dash.setY(Math.max(dash.getY(), 0.15));
        player.setVelocity(dash);

        Set<UUID> alreadyHit = new HashSet<>();
        new BukkitRunnable() {
            int ticksRun = 0;

            @Override
            public void run() {
                if (!player.isOnline() || ticksRun++ > 8) {
                    cancel();
                    return;
                }
                for (LivingEntity nearby : Targets.nearbyLiving(player, 2.5, 2.0)) {
                    if (!alreadyHit.add(nearby.getUniqueId())) {
                        continue;
                    }
                    nearby.damage(damageBase * amplifier, player);
                    Vector knockback = nearby.getLocation().toVector().subtract(player.getLocation().toVector());
                    if (knockback.lengthSquared() > 0.01) {
                        knockback.normalize().multiply(1.2);
                        knockback.setY(Math.max(0.3, knockback.getY()));
                        nearby.setVelocity(nearby.getVelocity().add(knockback));
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}
