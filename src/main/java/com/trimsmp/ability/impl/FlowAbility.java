package com.trimsmp.ability.impl;

import com.trimsmp.ability.TrimAbility;
import com.trimsmp.trim.TrimPatternKind;
import com.trimsmp.trim.TrimTier;
import com.trimsmp.util.AbilityConfig;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

/** Flow: rides a gust of wind across the battlefield, landing softly. */
public final class FlowAbility implements TrimAbility {

    private final AbilityConfig config;

    public FlowAbility(AbilityConfig config) {
        this.config = config;
    }

    @Override
    public TrimPatternKind pattern() {
        return TrimPatternKind.FLOW;
    }

    @Override
    public boolean hasActivePower() {
        return true;
    }

    @Override
    public long activationCooldownTicks(TrimTier tier) {
        int base = config.getInt("cooldown-seconds-base", 18);
        int reductionPerTier = config.getInt("cooldown-seconds-reduction-per-tier", 2);
        int seconds = Math.max(2, base - reductionPerTier * (tier.level() - 1));
        return seconds * 20L;
    }

    @Override
    public void activate(Player player, TrimTier tier) {
        double power = config.getDouble("launch-power-base", 1.2) + config.getDouble("launch-power-per-tier", 0.25) * tier.level();
        Vector direction = player.getLocation().getDirection().normalize().multiply(power);
        direction.setY(Math.max(0.5, direction.getY() + 0.5));
        player.setVelocity(direction);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 100, 0, true, false, true));
    }
}
