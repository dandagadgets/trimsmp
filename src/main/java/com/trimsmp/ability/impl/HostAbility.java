package com.trimsmp.ability.impl;

import com.trimsmp.ability.TrimAbility;
import com.trimsmp.trim.TrimPatternKind;
import com.trimsmp.trim.TrimTier;
import com.trimsmp.util.AbilityConfig;
import com.trimsmp.util.Effects;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.potion.PotionEffectType;

/** Host: a welcome guest wherever villagers gather, resting easy in their company. */
public final class HostAbility implements TrimAbility {

    private final AbilityConfig config;

    public HostAbility(AbilityConfig config) {
        this.config = config;
    }

    @Override
    public TrimPatternKind pattern() {
        return TrimPatternKind.HOST;
    }

    @Override
    public void tick(Player player, TrimTier tier) {
        double radius = config.getDouble("radius", 8.0);
        boolean nearVillager = false;
        for (Entity nearby : player.getNearbyEntities(radius, radius, radius)) {
            if (nearby instanceof Villager) {
                nearVillager = true;
                break;
            }
        }
        if (!nearVillager) {
            return;
        }
        int regenTicks = config.getInt("regen-seconds", 4) * 20;
        Effects.refresh(player, PotionEffectType.REGENERATION, tier.level() - 1, regenTicks);
    }
}
