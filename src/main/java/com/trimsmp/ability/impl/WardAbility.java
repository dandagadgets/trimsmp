package com.trimsmp.ability.impl;

import com.trimsmp.ability.TrimAbility;
import com.trimsmp.trim.TrimPatternKind;
import com.trimsmp.trim.TrimTier;
import com.trimsmp.util.AbilityConfig;
import com.trimsmp.util.Effects;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

/** Ward: a personal barrier of Absorption, Resistance, and Fire Resistance on demand. */
public final class WardAbility implements TrimAbility {

    private final AbilityConfig config;

    public WardAbility(AbilityConfig config) {
        this.config = config;
    }

    @Override
    public TrimPatternKind pattern() {
        return TrimPatternKind.WARD;
    }

    @Override
    public void tick(Player player, TrimTier tier) {
        Effects.refresh(player, PotionEffectType.RESISTANCE, (tier.level() - 1) / 2, 30);
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
        int barrierTicks = config.getInt("barrier-duration-seconds", 10) * 20;
        int absorptionAmplifier = config.getInt("absorption-amplifier", 3) + (tier.level() - 1) / 2;
        int resistanceAmplifier = config.getInt("resistance-amplifier", 1) + (tier.level() - 1) / 3;

        Effects.refresh(player, PotionEffectType.ABSORPTION, absorptionAmplifier, barrierTicks);
        Effects.refresh(player, PotionEffectType.RESISTANCE, resistanceAmplifier, barrierTicks);
        Effects.refresh(player, PotionEffectType.FIRE_RESISTANCE, 0, barrierTicks);
    }
}
