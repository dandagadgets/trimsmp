package com.trimsmp.ability.impl;

import com.trimsmp.ability.TrimAbility;
import com.trimsmp.trim.TrimPatternKind;
import com.trimsmp.trim.TrimTier;
import com.trimsmp.util.AbilityConfig;
import com.trimsmp.util.Targets;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Set;

/** Host: most mobs leave you alone; on demand, you steal health and positive effects from everyone nearby. */
public final class HostAbility implements TrimAbility {

    private static final Set<PotionEffectType> POSITIVE_EFFECTS = Set.of(
            PotionEffectType.SPEED, PotionEffectType.HASTE, PotionEffectType.STRENGTH,
            PotionEffectType.JUMP_BOOST, PotionEffectType.REGENERATION, PotionEffectType.RESISTANCE,
            PotionEffectType.FIRE_RESISTANCE, PotionEffectType.WATER_BREATHING, PotionEffectType.NIGHT_VISION,
            PotionEffectType.HEALTH_BOOST, PotionEffectType.ABSORPTION, PotionEffectType.SATURATION,
            PotionEffectType.LUCK, PotionEffectType.SLOW_FALLING, PotionEffectType.DOLPHINS_GRACE,
            PotionEffectType.CONDUIT_POWER, PotionEffectType.HERO_OF_THE_VILLAGE);

    private final AbilityConfig config;

    public HostAbility(AbilityConfig config) {
        this.config = config;
    }

    @Override
    public TrimPatternKind pattern() {
        return TrimPatternKind.HOST;
    }

    @Override
    public void onTargetedBy(Player player, TrimTier tier, EntityTargetLivingEntityEvent event) {
        if (event.getEntity() instanceof Monster) {
            event.setCancelled(true);
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
        return Math.max(10, base - reductionPerTier * (tier.level() - 1)) * 20L;
    }

    @Override
    public void activate(Player player, TrimTier tier) {
        double radius = config.getDouble("radius", 10.0);
        double healthSteal = config.getDouble("health-steal-base", 4.0) + config.getDouble("health-steal-per-tier", 0.5) * tier.level();

        double maxHealth = player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue();
        double stolenTotal = 0;

        for (LivingEntity nearby : Targets.nearbyLiving(player, radius, radius)) {
            for (PotionEffect effect : nearby.getActivePotionEffects()) {
                if (POSITIVE_EFFECTS.contains(effect.getType())) {
                    nearby.removePotionEffect(effect.getType());
                    player.addPotionEffect(new PotionEffect(effect.getType(), effect.getDuration(),
                            Math.min(effect.getAmplifier() + 1, 4), true, false, true));
                }
            }

            double take = Math.min(healthSteal, Math.max(0, nearby.getHealth() - 1.0));
            nearby.setHealth(nearby.getHealth() - take);
            stolenTotal += take;

            Vector knockback = nearby.getLocation().toVector().subtract(player.getLocation().toVector());
            if (knockback.lengthSquared() > 0.01) {
                knockback.normalize().multiply(0.8);
                knockback.setY(Math.max(0.2, knockback.getY()));
                nearby.setVelocity(nearby.getVelocity().add(knockback));
            }
        }

        player.setHealth(Math.min(maxHealth, player.getHealth() + stolenTotal));
    }
}
