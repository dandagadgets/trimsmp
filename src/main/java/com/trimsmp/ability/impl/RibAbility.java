package com.trimsmp.ability.impl;

import com.trimsmp.ability.TrimAbility;
import com.trimsmp.minion.MinionService;
import com.trimsmp.trim.TrimPatternKind;
import com.trimsmp.trim.TrimTier;
import com.trimsmp.util.AbilityConfig;
import com.trimsmp.util.Effects;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.function.LongSupplier;

/** Rib: summons a few Bogged to fight at your side for a while. */
public final class RibAbility implements TrimAbility {

    private final AbilityConfig config;
    private final MinionService minions;
    private final LongSupplier currentTick;

    public RibAbility(AbilityConfig config, MinionService minions, LongSupplier currentTick) {
        this.config = config;
        this.minions = minions;
        this.currentTick = currentTick;
    }

    @Override
    public TrimPatternKind pattern() {
        return TrimPatternKind.RIB;
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
        int base = config.getInt("cooldown-seconds-base", 60);
        int reductionPerTier = config.getInt("cooldown-seconds-reduction-per-tier", 6);
        return Math.max(5, base - reductionPerTier * (tier.level() - 1)) * 20L;
    }

    @Override
    public void activate(Player player, TrimTier tier) {
        int count = config.getInt("minion-count-base", 2) + (tier.level() - 1) / 2;
        long lifespanTicks = config.getInt("minion-lifespan-seconds", 60) * 20L;
        minions.spawn(player, EntityType.BOGGED, count, lifespanTicks, currentTick.getAsLong());
    }
}
