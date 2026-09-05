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

/** Coast: a burst of water pulls every nearby entity toward you, weakening and slowing them. */
public final class CoastAbility implements TrimAbility {

    private final AbilityConfig config;

    public CoastAbility(AbilityConfig config) {
        this.config = config;
    }

    @Override
    public TrimPatternKind pattern() {
        return TrimPatternKind.COAST;
    }

    @Override
    public void tick(Player player, TrimTier tier) {
        Effects.refresh(player, PotionEffectType.DOLPHINS_GRACE, (tier.level() - 1) / 2, 30);
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
        double radius = config.getDouble("radius", 30.0);
        double damage = config.getDouble("damage-base", 10.0) + config.getDouble("damage-per-tier", 1.0) * tier.level();
        int debuffTicks = config.getInt("debuff-duration-seconds", 4) * 20;

        for (LivingEntity nearby : Targets.nearbyLiving(player, radius, radius)) {
            Vector pull = player.getLocation().toVector().subtract(nearby.getLocation().toVector());
            if (pull.lengthSquared() > 0.01) {
                pull.normalize().multiply(1.4);
                pull.setY(Math.max(0.25, pull.getY()));
                nearby.setVelocity(nearby.getVelocity().add(pull));
            }
            nearby.damage(damage, player);
            Effects.refresh(nearby, PotionEffectType.WEAKNESS, 1, debuffTicks);
            Effects.refresh(nearby, PotionEffectType.SLOWNESS, 1, debuffTicks);
        }
    }
}
