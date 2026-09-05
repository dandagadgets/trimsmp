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

/** Eye: reveals every nearby entity - even the invisible ones - making them glow, slow, and weak. */
public final class EyeAbility implements TrimAbility {

    private final AbilityConfig config;
    private final int passiveDurationTicks;

    public EyeAbility(AbilityConfig config, int passiveDurationTicks) {
        this.config = config;
        this.passiveDurationTicks = passiveDurationTicks;
    }

    @Override
    public TrimPatternKind pattern() {
        return TrimPatternKind.EYE;
    }

    @Override
    public void tick(Player player, TrimTier tier) {
        Effects.refresh(player, PotionEffectType.NIGHT_VISION, 0, passiveDurationTicks);
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
        double radiusXZ = config.getDouble("true-sight-radius", 80.0);
        double radiusY = config.getDouble("true-sight-vertical-radius", 50.0);
        int sightTicks = config.getInt("true-sight-duration-seconds", 30) * 20;

        for (LivingEntity nearby : Targets.nearbyLiving(player, radiusXZ, radiusY)) {
            nearby.removePotionEffect(PotionEffectType.INVISIBILITY);
            Effects.refresh(nearby, PotionEffectType.GLOWING, 0, sightTicks);
            Effects.refresh(nearby, PotionEffectType.SLOWNESS, 0, sightTicks);
            Effects.refresh(nearby, PotionEffectType.WEAKNESS, 0, sightTicks);
        }
    }
}
