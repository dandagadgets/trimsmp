package com.trimsmp.ability.impl;

import com.trimsmp.ability.TrimAbility;
import com.trimsmp.trim.TrimPatternKind;
import com.trimsmp.trim.TrimTier;
import com.trimsmp.util.AbilityConfig;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

/** Bolt: strikes like lightning on a critical hit, and is never struck by the real thing. */
public final class BoltAbility implements TrimAbility {

    private final AbilityConfig config;

    public BoltAbility(AbilityConfig config) {
        this.config = config;
    }

    @Override
    public TrimPatternKind pattern() {
        return TrimPatternKind.BOLT;
    }

    @Override
    public void onIncomingDamage(Player player, TrimTier tier, EntityDamageEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.LIGHTNING) {
            event.setDamage(0);
        }
    }

    @Override
    public void onDealDamage(Player player, TrimTier tier, EntityDamageByEntityEvent event) {
        if (!isCritical(player)) {
            return;
        }
        double bonus = config.getDouble("crit-bonus-damage-base", 1.0) + config.getDouble("crit-bonus-damage-per-tier", 0.75) * tier.level();
        event.setDamage(event.getDamage() + bonus);
        player.getWorld().strikeLightningEffect(event.getEntity().getLocation());
    }

    private static boolean isCritical(Player player) {
        return player.getFallDistance() > 0.0f
                && !player.isOnGround()
                && !player.isInWater()
                && !player.isSprinting();
    }
}
