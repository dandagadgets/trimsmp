package com.trimsmp.ability.impl;

import com.trimsmp.ability.TrimAbility;
import com.trimsmp.trim.TrimPatternKind;
import com.trimsmp.trim.TrimTier;
import com.trimsmp.util.AbilityConfig;
import com.trimsmp.util.Effects;
import com.trimsmp.util.Targets;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

/** Shaper: efficient hands while tooled up; Stone Fist erupts spikes under nearby enemies. */
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

    @Override
    public boolean hasActivePower() {
        return true;
    }

    @Override
    public long activationCooldownTicks(TrimTier tier) {
        int base = config.getInt("cooldown-seconds-base", 30);
        int reductionPerTier = config.getInt("cooldown-seconds-reduction-per-tier", 3);
        return Math.max(3, base - reductionPerTier * (tier.level() - 1)) * 20L;
    }

    @Override
    public void activate(Player player, TrimTier tier) {
        double radius = config.getDouble("radius", 6.0);
        double damage = config.getDouble("damage-base", 5.0) + config.getDouble("damage-per-tier", 0.7) * tier.level();

        Location origin = player.getLocation();
        origin.getWorld().spawnParticle(Particle.CRIT, origin, 40, radius / 2, 0.3, radius / 2, 0.1);
        origin.getWorld().playSound(origin, Sound.BLOCK_STONE_BREAK, 1.5f, 0.7f);

        for (LivingEntity nearby : Targets.nearbyLiving(player, radius, 2.0)) {
            nearby.damage(damage, player);
            Vector knockup = nearby.getVelocity();
            knockup.setY(Math.max(0.6, knockup.getY() + 0.6));
            nearby.setVelocity(knockup);
        }
    }

    private static boolean isTool(Material material) {
        String name = material.name();
        return name.endsWith("_PICKAXE") || name.endsWith("_AXE") || name.endsWith("_SHOVEL") || name.endsWith("_HOE");
    }
}
