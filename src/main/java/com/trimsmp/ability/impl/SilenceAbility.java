package com.trimsmp.ability.impl;

import com.trimsmp.ability.TrimAbility;
import com.trimsmp.trim.TrimPatternKind;
import com.trimsmp.trim.TrimTier;
import com.trimsmp.util.AbilityConfig;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;

import java.util.concurrent.ThreadLocalRandom;

/** Silence: moves through the world unnoticed, slipping past a hostile mob's attention. */
public final class SilenceAbility implements TrimAbility {

    private final AbilityConfig config;

    public SilenceAbility(AbilityConfig config) {
        this.config = config;
    }

    @Override
    public TrimPatternKind pattern() {
        return TrimPatternKind.SILENCE;
    }

    @Override
    public void onTargetedBy(Player player, TrimTier tier, EntityTargetLivingEntityEvent event) {
        double chance = config.getDouble("unnotice-chance-base-percent", 15)
                + config.getDouble("unnotice-chance-per-tier-percent", 10) * tier.level();
        chance = Math.min(90.0, chance);
        if (ThreadLocalRandom.current().nextDouble(100.0) < chance) {
            event.setCancelled(true);
        }
    }
}
