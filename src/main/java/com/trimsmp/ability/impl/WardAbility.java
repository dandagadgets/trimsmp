package com.trimsmp.ability.impl;

import com.trimsmp.ability.TrimAbility;
import com.trimsmp.trim.TrimPatternKind;
import com.trimsmp.trim.TrimTier;
import com.trimsmp.util.AbilityConfig;
import com.trimsmp.util.Effects;
import com.trimsmp.util.Targets;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

/** Ward: Guardian Slam - a shockwave that damages and knocks back nearby enemies, then shields you. */
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
        int base = config.getInt("cooldown-seconds-base", 45);
        int reductionPerTier = config.getInt("cooldown-seconds-reduction-per-tier", 5);
        return Math.max(5, base - reductionPerTier * (tier.level() - 1)) * 20L;
    }

    @Override
    public void activate(Player player, TrimTier tier) {
        double radius = config.getDouble("radius", 6.0);
        double damage = config.getDouble("damage-base", 5.0) + config.getDouble("damage-per-tier", 0.7) * tier.level();
        double knockback = config.getDouble("knockback-strength", 1.4);
        int resistTicks = config.getInt("resistance-duration-seconds", 6) * 20;
        int resistAmplifier = config.getInt("resistance-amplifier", 2) + (tier.level() - 1) / 3;

        player.getWorld().spawnParticle(Particle.EXPLOSION, player.getLocation(), 1);
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1.5f, 0.8f);

        for (LivingEntity nearby : Targets.nearbyLiving(player, radius, radius)) {
            nearby.damage(damage, player);
            Vector push = nearby.getLocation().toVector().subtract(player.getLocation().toVector());
            if (push.lengthSquared() > 0.01) {
                push.normalize().multiply(knockback);
                push.setY(Math.max(0.4, push.getY()));
                nearby.setVelocity(nearby.getVelocity().add(push));
            }
        }

        Effects.refresh(player, PotionEffectType.RESISTANCE, resistAmplifier, resistTicks);
    }
}
