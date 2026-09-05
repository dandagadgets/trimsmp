package com.trimsmp.ability.impl;

import com.trimsmp.ability.TrimAbility;
import com.trimsmp.trim.TrimPatternKind;
import com.trimsmp.trim.TrimTier;
import com.trimsmp.util.AbilityConfig;
import com.trimsmp.util.Effects;
import com.trimsmp.util.PearlDisableService;
import com.trimsmp.util.Targets;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

/** Raiser: negates fall damage, and slams the ground on demand to pull in and weaken every nearby enemy. */
public final class RaiserAbility implements TrimAbility {

    private final AbilityConfig config;
    private final PearlDisableService pearlDisable;

    public RaiserAbility(AbilityConfig config, PearlDisableService pearlDisable) {
        this.config = config;
        this.pearlDisable = pearlDisable;
    }

    @Override
    public TrimPatternKind pattern() {
        return TrimPatternKind.RAISER;
    }

    @Override
    public void onIncomingDamage(Player player, TrimTier tier, EntityDamageEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            event.setDamage(0);
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
        double radius = config.getDouble("entity-pull-radius", 15.0);
        int debuffTicks = config.getInt("debuff-duration-seconds", 4) * 20;
        long pearlDisableTicks = config.getInt("pearl-cooldown-seconds", 10) * 20L;

        for (LivingEntity nearby : Targets.nearbyLiving(player, radius, radius)) {
            Vector pull = player.getLocation().toVector().subtract(nearby.getLocation().toVector());
            if (pull.lengthSquared() > 0.01) {
                pull.normalize().multiply(1.2);
                pull.setY(Math.max(0.3, pull.getY()));
                nearby.setVelocity(nearby.getVelocity().add(pull));
            }
            Effects.refresh(nearby, PotionEffectType.SLOWNESS, 1, debuffTicks);
            Effects.refresh(nearby, PotionEffectType.WEAKNESS, 1, debuffTicks);
            if (nearby instanceof Player enemy) {
                pearlDisable.disable(enemy, pearlDisableTicks);
            }
        }
    }
}
