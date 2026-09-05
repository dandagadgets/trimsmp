package com.trimsmp.ability.impl;

import com.trimsmp.ability.TrimAbility;
import com.trimsmp.trim.TrimPatternKind;
import com.trimsmp.trim.TrimTier;
import com.trimsmp.util.AbilityConfig;
import com.trimsmp.util.Effects;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

/** Coast: breathes water like air and swims with the current. */
public final class CoastAbility implements TrimAbility {

    private final AbilityConfig config;
    private final int passiveDurationTicks;

    public CoastAbility(AbilityConfig config, int passiveDurationTicks) {
        this.config = config;
        this.passiveDurationTicks = passiveDurationTicks;
    }

    @Override
    public TrimPatternKind pattern() {
        return TrimPatternKind.COAST;
    }

    @Override
    public void tick(Player player, TrimTier tier) {
        if (!player.isInWater()) {
            return;
        }
        Effects.refresh(player, PotionEffectType.WATER_BREATHING, 0, passiveDurationTicks);
        int graceAmplifier = config.getInt("grace-amplifier-per-tier", 1) * tier.level() - 1;
        Effects.refresh(player, PotionEffectType.DOLPHINS_GRACE, graceAmplifier, passiveDurationTicks);
    }
}
