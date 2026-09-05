package com.trimsmp.ability.impl;

import com.trimsmp.ability.TrimAbility;
import com.trimsmp.trim.TrimPatternKind;
import com.trimsmp.trim.TrimTier;
import com.trimsmp.util.AbilityConfig;
import com.trimsmp.util.Effects;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

/** Tide: carries a pocket of the ocean's current, granting a conduit-like clarity underwater. */
public final class TideAbility implements TrimAbility {

    private final AbilityConfig config;
    private final int passiveDurationTicks;

    public TideAbility(AbilityConfig config, int passiveDurationTicks) {
        this.config = config;
        this.passiveDurationTicks = passiveDurationTicks;
    }

    @Override
    public TrimPatternKind pattern() {
        return TrimPatternKind.TIDE;
    }

    @Override
    public void tick(Player player, TrimTier tier) {
        if (!player.isInWater()) {
            return;
        }
        int hasteAmplifier = config.getInt("haste-amplifier", 1) * tier.level() - 1;
        Effects.refresh(player, PotionEffectType.HASTE, hasteAmplifier, passiveDurationTicks);
        Effects.refresh(player, PotionEffectType.NIGHT_VISION, 0, passiveDurationTicks);
    }
}
