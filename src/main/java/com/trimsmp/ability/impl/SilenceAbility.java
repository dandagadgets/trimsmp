package com.trimsmp.ability.impl;

import com.trimsmp.ability.TrimAbility;
import com.trimsmp.trim.TrimPatternKind;
import com.trimsmp.trim.TrimTier;
import com.trimsmp.util.AbilityConfig;
import com.trimsmp.util.CooldownManager;
import com.trimsmp.util.Effects;
import com.trimsmp.util.PearlDisableService;
import com.trimsmp.util.Targets;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.function.LongSupplier;

/**
 * Silence: Warden's Roar disables nearby enemies' ender pearls and blinds/slows them; a
 * Warden's Echo automatically pulses out when the wearer drops low on health.
 */
public final class SilenceAbility implements TrimAbility {

    private final AbilityConfig config;
    private final LongSupplier currentTick;
    private final PearlDisableService pearlDisable;
    private final CooldownManager echoCooldown = new CooldownManager();

    public SilenceAbility(AbilityConfig config, LongSupplier currentTick, PearlDisableService pearlDisable) {
        this.config = config;
        this.currentTick = currentTick;
        this.pearlDisable = pearlDisable;
    }

    @Override
    public TrimPatternKind pattern() {
        return TrimPatternKind.SILENCE;
    }

    @Override
    public void tick(Player player, TrimTier tier) {
        int amplifier = 1 + (tier.level() - 1) / 3;
        Effects.refresh(player, PotionEffectType.STRENGTH, amplifier, 30);
    }

    @Override
    public boolean hasActivePower() {
        return true;
    }

    @Override
    public long activationCooldownTicks(TrimTier tier) {
        int base = config.getInt("cooldown-seconds-base", 90);
        int reductionPerTier = config.getInt("cooldown-seconds-reduction-per-tier", 8);
        return Math.max(10, base - reductionPerTier * (tier.level() - 1)) * 20L;
    }

    @Override
    public void activate(Player player, TrimTier tier) {
        double radius = config.getDouble("radius", 15.0);
        int potionTicks = config.getInt("potion-duration-seconds", 20) * 20;
        long pearlDisableTicks = config.getInt("pearl-disable-seconds", 10) * 20L;

        // The roar always empowers you, whether or not there's anyone around to hear it.
        int selfBuffTicks = config.getInt("roar-self-buff-seconds", 8) * 20;
        Effects.refresh(player, PotionEffectType.SPEED, 1, selfBuffTicks);
        Effects.refresh(player, PotionEffectType.RESISTANCE, 1, selfBuffTicks);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WARDEN_ROAR, 1.5f, 0.8f);

        for (LivingEntity entity : Targets.nearbyLiving(player, radius, radius)) {
            Effects.refresh(entity, PotionEffectType.BLINDNESS, 0, potionTicks);
            Effects.refresh(entity, PotionEffectType.SLOWNESS, 1, potionTicks);
            if (entity instanceof Player enemy) {
                pearlDisable.disable(enemy, pearlDisableTicks);
            }
        }
    }

    @Override
    public void onIncomingDamage(Player player, TrimTier tier, EntityDamageEvent event) {
        double threshold = config.getDouble("echo-health-threshold", 4.0);
        if (player.getHealth() - event.getFinalDamage() > threshold) {
            return;
        }
        long cooldownTicks = config.getInt("echo-cooldown-seconds", 120) * 20L;
        if (!echoCooldown.tryUse(player, "echo", currentTick.getAsLong(), cooldownTicks)) {
            return;
        }

        double radius = config.getDouble("radius", 15.0);
        double knockback = config.getDouble("echo-knockback-strength", 1.3);
        int buffTicks = config.getInt("echo-resistance-regen-seconds", 15) * 20;
        int weaknessTicks = config.getInt("echo-weakness-seconds", 5) * 20;

        Effects.refresh(player, PotionEffectType.RESISTANCE, 1, buffTicks);
        Effects.refresh(player, PotionEffectType.REGENERATION, 1, buffTicks);

        for (LivingEntity nearby : Targets.nearbyLiving(player, radius, radius)) {
            Vector push = nearby.getLocation().toVector().subtract(player.getLocation().toVector());
            if (push.lengthSquared() > 0) {
                push.normalize().multiply(knockback);
                push.setY(Math.max(0.3, push.getY()));
                nearby.setVelocity(nearby.getVelocity().add(push));
            }
            Effects.refresh(nearby, PotionEffectType.WEAKNESS, 0, weaknessTicks);
        }
    }
}
