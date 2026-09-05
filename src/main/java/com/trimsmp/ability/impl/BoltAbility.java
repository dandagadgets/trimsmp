package com.trimsmp.ability.impl;

import com.trimsmp.ability.TrimAbility;
import com.trimsmp.trim.TrimPatternKind;
import com.trimsmp.trim.TrimTier;
import com.trimsmp.util.AbilityConfig;
import com.trimsmp.util.Effects;
import com.trimsmp.util.Targets;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffectType;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Bolt: calls down a lightning strike that arcs between nearby enemies; immune to real lightning. */
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
    public void tick(Player player, TrimTier tier) {
        Effects.refresh(player, PotionEffectType.SPEED, 2 + (tier.level() - 1) / 3, 30);
    }

    @Override
    public void onIncomingDamage(Player player, TrimTier tier, EntityDamageEvent event) {
        if (event.getCause() == EntityDamageEvent.DamageCause.LIGHTNING) {
            event.setDamage(0);
        }
    }

    @Override
    public boolean hasActivePower() {
        return true;
    }

    @Override
    public long activationCooldownTicks(TrimTier tier) {
        int base = config.getInt("cooldown-seconds-base", 20);
        int reductionPerTier = config.getInt("cooldown-seconds-reduction-per-tier", 2);
        return Math.max(3, base - reductionPerTier * (tier.level() - 1)) * 20L;
    }

    @Override
    public void activate(Player player, TrimTier tier) {
        double targetRange = config.getDouble("target-range", 20.0);
        double chainRange = config.getDouble("chain-range", 10.0);
        int maxChains = config.getInt("max-chains", 3);
        double initialDamage = config.getDouble("initial-damage-base", 6.0) + config.getDouble("initial-damage-per-tier", 0.6) * tier.level();
        double subsequentDamage = config.getDouble("subsequent-damage-base", 4.0) + config.getDouble("subsequent-damage-per-tier", 0.4) * tier.level();
        int weaknessTicks = config.getInt("weakness-duration-seconds", 5) * 20;

        List<LivingEntity> candidates = Targets.nearbyLiving(player, targetRange, targetRange);
        candidates.sort(Comparator.comparingDouble(e -> e.getLocation().distanceSquared(player.getLocation())));
        if (candidates.isEmpty()) {
            return;
        }

        Set<UUID> hit = new HashSet<>();
        LivingEntity current = candidates.get(0);
        double damage = initialDamage;

        for (int chain = 0; chain < maxChains && current != null; chain++) {
            current.getWorld().strikeLightningEffect(current.getLocation());
            current.damage(damage, player);
            Effects.refresh(current, PotionEffectType.WEAKNESS, 0, weaknessTicks);
            hit.add(current.getUniqueId());

            LivingEntity next = null;
            double bestDistanceSq = chainRange * chainRange;
            for (LivingEntity candidate : Targets.nearbyLiving(current, chainRange, chainRange)) {
                if (hit.contains(candidate.getUniqueId())) {
                    continue;
                }
                double distSq = candidate.getLocation().distanceSquared(current.getLocation());
                if (distSq <= bestDistanceSq) {
                    bestDistanceSq = distSq;
                    next = candidate;
                }
            }
            current = next;
            damage = subsequentDamage;
        }
    }
}
