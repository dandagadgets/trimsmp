package com.trimsmp.ability.impl;

import com.trimsmp.ability.TrimAbility;
import com.trimsmp.trim.TrimPatternKind;
import com.trimsmp.trim.TrimTier;
import org.bukkit.entity.PiglinAbstract;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;

/** Snout: bartered a lasting peace with the nether's piglins. */
public final class SnoutAbility implements TrimAbility {

    @Override
    public TrimPatternKind pattern() {
        return TrimPatternKind.SNOUT;
    }

    @Override
    public void onTargetedBy(Player player, TrimTier tier, EntityTargetLivingEntityEvent event) {
        if (event.getEntity() instanceof PiglinAbstract) {
            event.setCancelled(true);
        }
    }
}
