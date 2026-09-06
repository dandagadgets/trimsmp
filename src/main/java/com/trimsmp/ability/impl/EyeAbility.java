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

/** Eye: permanent Night Vision; summons an allied Enderman to fight at your side. */
public final class EyeAbility implements TrimAbility {

    private final AbilityConfig config;
    private final int passiveDurationTicks;
    private final MinionService minions;
    private final LongSupplier currentTick;

    public EyeAbility(AbilityConfig config, int passiveDurationTicks, MinionService minions, LongSupplier currentTick) {
        this.config = config;
        this.passiveDurationTicks = passiveDurationTicks;
        this.minions = minions;
        this.currentTick = currentTick;
    }

    @Override
    public TrimPatternKind pattern() {
        return TrimPatternKind.EYE;
    }

    @Override
    public void tick(Player player, TrimTier tier) {
        Effects.refresh(player, PotionEffectType.NIGHT_VISION, 0, passiveDurationTicks);
    }

    @Override
    public boolean hasActivePower() {
        return true;
    }

    @Override
    public long activationCooldownTicks(TrimTier tier) {
        int base = config.getInt("cooldown-seconds-base", 90);
        int reductionPerTier = config.getInt("cooldown-seconds-reduction-per-tier", 9);
        return Math.max(10, base - reductionPerTier * (tier.level() - 1)) * 20L;
    }

    @Override
    public void activate(Player player, TrimTier tier) {
        int count = config.getInt("minion-count-base", 1) + (tier.level() - 1) / 3;
        long lifespanTicks = config.getInt("minion-lifespan-seconds", 60) * 20L;
        minions.spawn(player, EntityType.ENDERMAN, count, lifespanTicks, currentTick.getAsLong());
    }
}
