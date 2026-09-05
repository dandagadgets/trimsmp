package com.trimsmp.ability.impl;

import com.trimsmp.ability.TrimAbility;
import com.trimsmp.trim.TrimPatternKind;
import com.trimsmp.trim.TrimTier;
import com.trimsmp.util.AbilityConfig;
import com.trimsmp.util.Effects;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffectType;

/** Spire: falls from great heights like it's nothing. */
public final class SpireAbility implements TrimAbility {

    private final AbilityConfig config;
    private final int passiveDurationTicks;

    public SpireAbility(AbilityConfig config, int passiveDurationTicks) {
        this.config = config;
        this.passiveDurationTicks = passiveDurationTicks;
    }

    @Override
    public TrimPatternKind pattern() {
        return TrimPatternKind.SPIRE;
    }

    @Override
    public void tick(Player player, TrimTier tier) {
        double trigger = config.getDouble("trigger-fall-distance", 3.0);
        if (!player.isOnGround() && player.getFallDistance() >= trigger) {
            Effects.refresh(player, PotionEffectType.SLOW_FALLING, 0, passiveDurationTicks);
        }
    }

    @Override
    public void onIncomingDamage(Player player, TrimTier tier, EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) {
            return;
        }
        int negateAtTier = config.getInt("negate-at-tier", 4);
        if (tier.level() >= negateAtTier) {
            event.setDamage(0);
            return;
        }
        double reduction = 0.20 * tier.level();
        event.setDamage(event.getDamage() * Math.max(0.0, 1.0 - reduction));
    }
}
