package com.trimsmp.ability.impl;

import com.trimsmp.ability.TrimAbility;
import com.trimsmp.trim.TrimPatternKind;
import com.trimsmp.trim.TrimTier;
import com.trimsmp.util.AbilityConfig;
import com.trimsmp.util.Effects;
import com.trimsmp.util.Targets;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.List;

/** Sentry: fires spectral arrows dealing true damage at nearby enemies; steady Resistance while worn. */
public final class SentryAbility implements TrimAbility {

    private final AbilityConfig config;
    private final int passiveDurationTicks;

    public SentryAbility(AbilityConfig config, int passiveDurationTicks) {
        this.config = config;
        this.passiveDurationTicks = passiveDurationTicks;
    }

    @Override
    public TrimPatternKind pattern() {
        return TrimPatternKind.SENTRY;
    }

    @Override
    public void tick(Player player, TrimTier tier) {
        Effects.refresh(player, PotionEffectType.RESISTANCE, (tier.level() - 1) / 2, passiveDurationTicks);
    }

    @Override
    public boolean hasActivePower() {
        return true;
    }

    @Override
    public long activationCooldownTicks(TrimTier tier) {
        int base = config.getInt("cooldown-seconds-base", 90);
        int reductionPerTier = config.getInt("cooldown-seconds-reduction-per-tier", 10);
        return Math.max(5, base - reductionPerTier * (tier.level() - 1)) * 20L;
    }

    @Override
    public void activate(Player player, TrimTier tier) {
        double radius = config.getDouble("radius", 15.0);
        int arrowCount = config.getInt("arrow-count", 3);
        double trueDamage = config.getDouble("true-damage-base", 0.5)
                + config.getDouble("true-damage-per-tier", 0.4) * tier.level();

        List<LivingEntity> targets = Targets.nearbyHostiles(player, radius, arrowCount);
        for (LivingEntity target : targets) {
            Vector direction = target.getEyeLocation().toVector().subtract(player.getEyeLocation().toVector()).normalize();
            Arrow arrow = player.launchProjectile(Arrow.class, direction);
            arrow.setDamage(0.0);
            arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);

            double newHealth = Math.max(0.0, target.getHealth() - trueDamage);
            target.setHealth(newHealth);
        }
    }
}
