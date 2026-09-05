package com.trimsmp.ability.impl;

import com.trimsmp.ability.TrimAbility;
import com.trimsmp.trim.TrimPatternKind;
import com.trimsmp.trim.TrimTier;
import com.trimsmp.util.AbilityConfig;
import com.trimsmp.util.Effects;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Shaper: not part of the vanilla PowerTrims plugin's 17 patterns, so this is an original kit -
 * efficient hands while mining, and Terraform, conjuring a temporary bridge across any gap.
 */
public final class ShaperAbility implements TrimAbility {

    private final AbilityConfig config;
    private final Plugin plugin;
    private final int passiveDurationTicks;

    public ShaperAbility(AbilityConfig config, Plugin plugin, int passiveDurationTicks) {
        this.config = config;
        this.plugin = plugin;
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

    @Override
    public boolean hasActivePower() {
        return true;
    }

    @Override
    public long activationCooldownTicks(TrimTier tier) {
        int base = config.getInt("cooldown-seconds-base", 45);
        int reductionPerTier = config.getInt("cooldown-seconds-reduction-per-tier", 5);
        return Math.max(5, base - reductionPerTier * (tier.level() - 1)) * 20L;
    }

    @Override
    public void activate(Player player, TrimTier tier) {
        int length = config.getInt("bridge-length-base", 5) + (tier.level() - 1);
        int durationTicks = config.getInt("bridge-duration-seconds", 8) * 20;

        Location origin = player.getLocation();
        Vector direction = origin.getDirection().setY(0).normalize();
        List<Block> placed = new ArrayList<>();

        for (int i = 1; i <= length; i++) {
            Location step = origin.clone().add(direction.clone().multiply(i));
            step.setY(origin.getY() - 1);
            Block block = step.getBlock();
            if (block.getType().isAir()) {
                block.setType(Material.SCAFFOLDING);
                placed.add(block);
            }
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Block block : placed) {
                if (block.getType() == Material.SCAFFOLDING) {
                    block.setType(Material.AIR);
                }
            }
        }, durationTicks);
    }

    private static boolean isTool(Material material) {
        String name = material.name();
        return name.endsWith("_PICKAXE") || name.endsWith("_AXE") || name.endsWith("_SHOVEL") || name.endsWith("_HOE");
    }
}
