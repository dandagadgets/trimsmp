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

/** Flow: grants temporary flight propelled by wind, at the cost of your own health each second. */
public final class FlowAbility implements TrimAbility {

    private final AbilityConfig config;
    private final Plugin plugin;

    public FlowAbility(AbilityConfig config, Plugin plugin) {
        this.config = config;
        this.plugin = plugin;
    }

    @Override
    public TrimPatternKind pattern() {
        return TrimPatternKind.FLOW;
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
        int base = config.getInt("cooldown-seconds-base", 60);
        int reductionPerTier = config.getInt("cooldown-seconds-reduction-per-tier", 6);
        return Math.max(5, base - reductionPerTier * (tier.level() - 1)) * 20L;
    }

    @Override
    public void activate(Player player, TrimTier tier) {
        int durationTicks = (config.getInt("duration-seconds", 20) + tier.level() * 2) * 20;
        int costIntervalTicks = config.getInt("heart-cost-interval-seconds", 1) * 20;
        double costAmount = config.getDouble("heart-cost-amount", 2.0);
        double burstRadius = config.getDouble("burst-radius", 5.0);
        double burstDamage = config.getDouble("burst-damage-base", 3.0) + config.getDouble("burst-damage-per-tier", 0.5) * tier.level();

        for (LivingEntity nearby : Targets.nearbyLiving(player, burstRadius, burstRadius)) {
            nearby.damage(burstDamage, player);
            Vector knockback = nearby.getLocation().toVector().subtract(player.getLocation().toVector());
            if (knockback.lengthSquared() > 0.01) {
                knockback.normalize().multiply(1.4);
                knockback.setY(Math.max(0.4, knockback.getY()));
                nearby.setVelocity(nearby.getVelocity().add(knockback));
            }
        }

        boolean previousAllowFlight = player.getAllowFlight();
        player.setAllowFlight(true);
        player.setFlying(true);

        new BukkitRunnable() {
            int elapsedTicks = 0;

            @Override
            public void run() {
                elapsedTicks += costIntervalTicks;
                boolean offline = !player.isOnline();
                boolean tooLowHealth = !offline && player.getHealth() <= costAmount + 1.0;
                boolean outOfTime = elapsedTicks >= durationTicks;

                if (!offline && !tooLowHealth && !outOfTime) {
                    player.setHealth(Math.max(1.0, player.getHealth() - costAmount));
                }

                if (offline || tooLowHealth || outOfTime) {
                    if (!offline) {
                        player.setFlying(false);
                        player.setAllowFlight(previousAllowFlight);
                    }
                    cancel();
                }
            }
        }.runTaskTimer(plugin, costIntervalTicks, costIntervalTicks);
    }
}
