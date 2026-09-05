package com.trimsmp.ability.impl;

import com.trimsmp.ability.TrimAbility;
import com.trimsmp.trim.TrimPatternKind;
import com.trimsmp.trim.TrimTier;
import com.trimsmp.util.AbilityConfig;
import com.trimsmp.util.Effects;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

/** Dune: endures desert heat and shapes sand and sandstone with ease. */
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

        String biome = player.getLocation().getBlock().getBiome().getKey().getKey();
        if (biome.contains("desert") || biome.contains("badlands")) {
            int hasteAmplifier = config.getInt("haste-amplifier-per-tier", 1) * tier.level() - 1;
            Effects.refresh(player, PotionEffectType.HASTE, hasteAmplifier, passiveDurationTicks);
        }
    }
}
