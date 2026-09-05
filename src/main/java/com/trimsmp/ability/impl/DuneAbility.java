package com.trimsmp.ability.impl;

import com.trimsmp.ability.TrimAbility;
import com.trimsmp.trim.TrimPatternKind;
import com.trimsmp.trim.TrimTier;
import com.trimsmp.util.AbilityConfig;
import com.trimsmp.util.Effects;
import com.trimsmp.util.Targets;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

/** Dune: unleashes a raging sandstorm that blinds, slows, and batters nearby enemies while empowering you. */
public final class DuneAbility implements TrimAbility {

    private final AbilityConfig config;
    private final int passiveDurationTicks;

    public DuneAbility(AbilityConfig config, int passiveDurationTicks) {
        this.config = config;
        this.passiveDurationTicks = passiveDurationTicks;
    }

    @Override
    public TrimPatternKind pattern() {
        return TrimPatternKind.DUNE;
    }

    @Override
    public void tick(Player player, TrimTier tier) {
        Effects.refresh(player, PotionEffectType.FIRE_RESISTANCE, 0, passiveDurationTicks);
        Effects.refresh(player, PotionEffectType.HASTE, (tier.level() - 1) / 2, passiveDurationTicks);
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
        double radius = config.getDouble("radius", 12.0);
        double damage = config.getDouble("damage-base", 10.0) + config.getDouble("damage-per-tier", 1.0) * tier.level();
        int stormTicks = config.getInt("duration-seconds", 10) * 20;

        Effects.refresh(player, PotionEffectType.STRENGTH, 1 + (tier.level() - 1) / 3, stormTicks);
        Effects.refresh(player, PotionEffectType.SPEED, 1 + (tier.level() - 1) / 3, stormTicks);

        for (LivingEntity nearby : Targets.nearbyLiving(player, radius, radius)) {
            Effects.refresh(nearby, PotionEffectType.BLINDNESS, 0, stormTicks);
            Effects.refresh(nearby, PotionEffectType.SLOWNESS, 1, stormTicks);
            nearby.damage(damage, player);

            Vector knockback = nearby.getLocation().toVector().subtract(player.getLocation().toVector());
            if (knockback.lengthSquared() > 0.01) {
                knockback.normalize().multiply(1.3);
                knockback.setY(Math.max(0.3, knockback.getY()));
                nearby.setVelocity(nearby.getVelocity().add(knockback));
            }
        }
    }
}
