package com.trimsmp.ability.impl;

import com.trimsmp.ability.TrimAbility;
import com.trimsmp.trim.TrimPatternKind;
import com.trimsmp.trim.TrimTier;
import com.trimsmp.util.AbilityConfig;
import com.trimsmp.util.Effects;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

/** Wild: draws vigor from natural ground, with a burst of speed while sprinting through it. */
public final class WildAbility implements TrimAbility {

    private final AbilityConfig config;
    private final int passiveDurationTicks;

    public WildAbility(AbilityConfig config, int passiveDurationTicks) {
        this.config = config;
        this.passiveDurationTicks = passiveDurationTicks;
    }

    @Override
    public TrimPatternKind pattern() {
        return TrimPatternKind.WILD;
    }

    @Override
    public void tick(Player player, TrimTier tier) {
        Material below = player.getLocation().subtract(0, 1, 0).getBlock().getType();
        if (!isNatural(below)) {
            return;
        }

        int regenAmplifier = config.getInt("regen-amplifier-per-tier", 1) * tier.level() - 1;
        Effects.refresh(player, PotionEffectType.REGENERATION, regenAmplifier, passiveDurationTicks);

        if (player.isSprinting()) {
            int sprintTicks = config.getInt("sprint-speed-seconds", 3) * 20;
            Effects.refresh(player, PotionEffectType.SPEED, tier.level() - 1, Math.max(passiveDurationTicks, sprintTicks));
        }
    }

    private static boolean isNatural(Material material) {
        return switch (material) {
            case GRASS_BLOCK, DIRT, COARSE_DIRT, ROOTED_DIRT, PODZOL, MYCELIUM, MOSS_BLOCK,
                 JUNGLE_LEAVES, OAK_LEAVES, DARK_OAK_LEAVES, BIRCH_LEAVES, SPRUCE_LEAVES,
                 ACACIA_LEAVES, CHERRY_LEAVES, MANGROVE_LEAVES, AZALEA_LEAVES,
                 FLOWERING_AZALEA_LEAVES -> true;
            default -> false;
        };
    }
}
