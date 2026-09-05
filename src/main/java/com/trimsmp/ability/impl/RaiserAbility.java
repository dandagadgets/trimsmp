package com.trimsmp.ability.impl;

import com.trimsmp.ability.TrimAbility;
import com.trimsmp.trim.TrimPatternKind;
import com.trimsmp.trim.TrimTier;
import com.trimsmp.util.AbilityConfig;
import com.trimsmp.util.Effects;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

/** Raiser: rallies nearby allies, lending them strength and speed in a fight. */
public final class RaiserAbility implements TrimAbility {

    private final AbilityConfig config;

    public RaiserAbility(AbilityConfig config) {
        this.config = config;
    }

    @Override
    public TrimPatternKind pattern() {
        return TrimPatternKind.RAISER;
    }

    @Override
    public void tick(Player player, TrimTier tier) {
        double radius = config.getDouble("radius-base", 8.0) + config.getDouble("radius-per-tier", 1.5) * tier.level();
        int buffTicks = config.getInt("buff-seconds", 5) * 20;
        int amplifier = (tier.level() - 1) / 2;

        Effects.refresh(player, PotionEffectType.STRENGTH, Math.max(0, amplifier - 1), buffTicks);

        for (Entity nearby : player.getNearbyEntities(radius, radius, radius)) {
            if (nearby instanceof Player ally && !ally.equals(player)) {
                Effects.refresh(ally, PotionEffectType.STRENGTH, amplifier, buffTicks);
                Effects.refresh(ally, PotionEffectType.SPEED, amplifier, buffTicks);
            }
        }
    }
}
