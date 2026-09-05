package com.trimsmp.ability.impl;

import com.trimsmp.ability.TrimAbility;
import com.trimsmp.trim.TrimPatternKind;
import com.trimsmp.trim.TrimTier;
import com.trimsmp.util.AbilityConfig;
import com.trimsmp.util.Effects;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffectType;

/** Shaper: shapes stone, ore, and earth with practiced, efficient hands. */
public final class ShaperAbility implements TrimAbility {

    private final AbilityConfig config;
    private final int passiveDurationTicks;

    public ShaperAbility(AbilityConfig config, int passiveDurationTicks) {
        this.config = config;
        this.passiveDurationTicks = passiveDurationTicks;
    }

    @Override
    public TrimPatternKind pattern() {
        return TrimPatternKind.SHAPER;
    }

    @Override
    public void tick(Player player, TrimTier tier) {
        PlayerInventory inventory = player.getInventory();
        if (!isTool(inventory.getItemInMainHand().getType()) && !isTool(inventory.getItemInOffHand().getType())) {
            return;
        }
        int hasteAmplifier = config.getInt("haste-amplifier-per-tier", 1) * tier.level() - 1;
        Effects.refresh(player, PotionEffectType.HASTE, hasteAmplifier, passiveDurationTicks);
    }

    private static boolean isTool(Material material) {
        String name = material.name();
        return name.endsWith("_PICKAXE") || name.endsWith("_AXE") || name.endsWith("_SHOVEL") || name.endsWith("_HOE");
    }
}
