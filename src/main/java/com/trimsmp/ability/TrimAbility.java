package com.trimsmp.ability;

import com.trimsmp.trim.TrimPatternKind;
import com.trimsmp.trim.TrimTier;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;

/**
 * One trim pattern's special power. A player only ever has one ability active at a
 * time (a full matching set is required), so implementations don't need to worry
 * about stacking with each other - only about scaling cleanly across tiers 1-5.
 */
public interface TrimAbility {

    TrimPatternKind pattern();

    /** Called once per tick-interval (see config general.tick-interval-seconds) while active. */
    default void tick(Player player, TrimTier tier) {
    }

    /** Called when the wearer is about to take damage. */
    default void onIncomingDamage(Player player, TrimTier tier, EntityDamageEvent event) {
    }

    /** Called when the wearer deals damage to another entity. */
    default void onDealDamage(Player player, TrimTier tier, EntityDamageByEntityEvent event) {
    }

    /** Called when a mob is about to target the wearer. */
    default void onTargetedBy(Player player, TrimTier tier, EntityTargetLivingEntityEvent event) {
    }

    /** Whether this pattern has a manually-triggered power (sneak + swap hands). */
    default boolean hasActivePower() {
        return false;
    }

    /** Cooldown, in ticks, between manual activations at the given tier. */
    default long activationCooldownTicks(TrimTier tier) {
        return 0L;
    }

    /** Fires the manual power. Only called when {@link #hasActivePower()} is true and off cooldown. */
    default void activate(Player player, TrimTier tier) {
    }
}
