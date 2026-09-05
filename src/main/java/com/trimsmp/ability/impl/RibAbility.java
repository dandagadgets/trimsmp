package com.trimsmp.ability.impl;

import com.trimsmp.ability.TrimAbility;
import com.trimsmp.trim.TrimPatternKind;
import com.trimsmp.trim.TrimTier;
import com.trimsmp.util.AbilityConfig;
import org.bukkit.entity.AbstractSkeleton;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wither;
import org.bukkit.entity.Zombie;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffectType;

/** Rib: strikes harder against the undead and shrugs off the wither's curse. */
public final class RibAbility implements TrimAbility {

    private final AbilityConfig config;

    public RibAbility(AbilityConfig config) {
        this.config = config;
    }

    @Override
    public TrimPatternKind pattern() {
        return TrimPatternKind.RIB;
    }

    @Override
    public void tick(Player player, TrimTier tier) {
        if (player.hasPotionEffect(PotionEffectType.WITHER)) {
            player.removePotionEffect(PotionEffectType.WITHER);
        }
    }

    @Override
    public void onDealDamage(Player player, TrimTier tier, EntityDamageByEntityEvent event) {
        if (!isUndead(event.getEntity())) {
            return;
        }
        double percent = config.getDouble("undead-damage-bonus-base-percent", 10)
                + config.getDouble("undead-damage-bonus-per-tier-percent", 6) * tier.level();
        event.setDamage(event.getDamage() * (1.0 + percent / 100.0));
    }

    private static boolean isUndead(Entity entity) {
        return entity instanceof Zombie
                || entity instanceof AbstractSkeleton
                || entity instanceof Phantom
                || entity instanceof Wither;
    }
}
