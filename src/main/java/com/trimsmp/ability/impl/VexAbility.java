package com.trimsmp.ability.impl;

import com.trimsmp.ability.TrimAbility;
import com.trimsmp.trim.TrimPatternKind;
import com.trimsmp.trim.TrimTier;
import com.trimsmp.util.AbilityConfig;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/** Vex: blinks the wearer forward a short distance, phasing through obstacles like the mob it's named for. */
public final class VexAbility implements TrimAbility {

    private final AbilityConfig config;

    public VexAbility(AbilityConfig config) {
        this.config = config;
    }

    @Override
    public TrimPatternKind pattern() {
        return TrimPatternKind.VEX;
    }

    @Override
    public boolean hasActivePower() {
        return true;
    }

    @Override
    public long activationCooldownTicks(TrimTier tier) {
        int base = config.getInt("cooldown-seconds-base", 20);
        int reductionPerTier = config.getInt("cooldown-seconds-reduction-per-tier", 3);
        int seconds = Math.max(2, base - reductionPerTier * (tier.level() - 1));
        return seconds * 20L;
    }

    @Override
    public void activate(Player player, TrimTier tier) {
        double maxDistance = config.getDouble("distance-base", 6.0) + config.getDouble("distance-per-tier", 2.0) * tier.level();
        Location start = player.getEyeLocation();
        Vector direction = start.getDirection().normalize();

        Location best = player.getLocation();
        for (double d = 1.0; d <= maxDistance; d += 1.0) {
            Location candidate = start.clone().add(direction.clone().multiply(d));
            Location feet = candidate.clone().subtract(0, 1.5, 0);
            Block feetBlock = feet.getBlock();
            Block headBlock = candidate.getBlock();
            if (!feetBlock.getType().isSolid() && !headBlock.getType().isSolid()) {
                Location landing = feet.clone();
                landing.setYaw(player.getLocation().getYaw());
                landing.setPitch(player.getLocation().getPitch());
                best = landing;
            } else {
                break;
            }
        }

        player.teleport(best);
        player.setFallDistance(0f);
    }
}
