package com.trimsmp.ability.impl;

import com.trimsmp.ability.TrimAbility;
import com.trimsmp.trim.TrimPatternKind;
import com.trimsmp.trim.TrimTier;
import com.trimsmp.util.AbilityConfig;
import com.trimsmp.util.Effects;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/** Sentry: spots hostile mobs at range and steels the wearer when surrounded. */
public final class SentryAbility implements TrimAbility {

    private final AbilityConfig config;

    public SentryAbility(AbilityConfig config) {
        this.config = config;
    }

    @Override
    public TrimPatternKind pattern() {
        return TrimPatternKind.SENTRY;
    }

    @Override
    public void tick(Player player, TrimTier tier) {
        double radius = config.getDouble("radius-base", 10.0) + config.getDouble("radius-per-tier", 2.0) * tier.level();
        int glowTicks = config.getInt("glow-seconds", 4) * 20;

        int spotted = 0;
        for (Entity nearby : player.getNearbyEntities(radius, radius, radius)) {
            if (nearby instanceof Monster && nearby instanceof LivingEntity living) {
                living.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, glowTicks, 0, true, false, true));
                spotted++;
            }
        }

        if (spotted >= 2) {
            int resistTicks = config.getInt("overwatch-resistance-seconds", 3) * 20;
            Effects.refresh(player, PotionEffectType.RESISTANCE, 0, resistTicks);
        }
    }
}
