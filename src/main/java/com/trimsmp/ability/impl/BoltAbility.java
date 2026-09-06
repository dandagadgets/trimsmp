package com.trimsmp.ability.impl;

import com.trimsmp.ability.TrimAbility;
import com.trimsmp.trim.TrimPatternKind;
import com.trimsmp.trim.TrimTier;
import com.trimsmp.util.AbilityConfig;
import com.trimsmp.util.Effects;
import com.trimsmp.util.Targets;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffectType;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.LongSupplier;

/**
 * Bolt: calls down a lightning strike that arcs between nearby enemies, and charges your next 3
 * hits (with anything) so each one also strikes the target with lightning; immune to real lightning.
 */
public final class BoltAbility implements TrimAbility {

    private final AbilityConfig config;
    private final LongSupplier currentTick;
    private final Map<UUID, Integer> chargesRemaining = new HashMap<>();
    private final Map<UUID, Long> chargeExpiry = new HashMap<>();

    public BoltAbility(AbilityConfig config, LongSupplier currentTick) {
        this.config = config;
        this.currentTick = currentTick;
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
    public void onDealDamage(Player player, TrimTier tier, EntityDamageByEntityEvent event) {
        UUID id = player.getUniqueId();
        Integer remaining = chargesRemaining.get(id);
        Long expiry = chargeExpiry.get(id);
        if (remaining == null || remaining <= 0 || expiry == null || currentTick.getAsLong() > expiry) {
            chargesRemaining.remove(id);
            chargeExpiry.remove(id);
            return;
        }

        double chargedDamage = config.getDouble("charged-hit-damage-base", 2.0) + config.getDouble("charged-hit-damage-per-tier", 0.3) * tier.level();
        event.getEntity().getWorld().strikeLightningEffect(event.getEntity().getLocation());
        event.setDamage(event.getDamage() + chargedDamage);

        int left = remaining - 1;
        if (left <= 0) {
            chargesRemaining.remove(id);
            chargeExpiry.remove(id);
        } else {
            chargesRemaining.put(id, left);
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

        int chargeCount = config.getInt("charged-hits", 3);
        int chargeDurationTicks = config.getInt("charged-hits-duration-seconds", 20) * 20;
        chargesRemaining.put(player.getUniqueId(), chargeCount);
        chargeExpiry.put(player.getUniqueId(), currentTick.getAsLong() + chargeDurationTicks);

        List<LivingEntity> candidates = Targets.nearbyLiving(player, targetRange, targetRange);
        candidates.sort(Comparator.comparingDouble(e -> e.getLocation().distanceSquared(player.getLocation())));
        if (candidates.isEmpty()) {
            // Nothing to chain to - still call down a cosmetic bolt in front of you.
            player.getWorld().strikeLightningEffect(player.getLocation().add(player.getLocation().getDirection().multiply(3)));
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
