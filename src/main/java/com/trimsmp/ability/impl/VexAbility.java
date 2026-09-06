package com.trimsmp.ability.impl;

import com.trimsmp.ability.TrimAbility;
import com.trimsmp.minion.MinionService;
import com.trimsmp.trim.TrimPatternKind;
import com.trimsmp.trim.TrimTier;
import com.trimsmp.util.AbilityConfig;
import com.trimsmp.util.CooldownManager;
import com.trimsmp.util.Effects;
import com.trimsmp.util.Targets;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;

import java.util.function.LongSupplier;

/** Vex: debuffs nearby enemies and summons 3 allied Vexes; when badly hurt, vanishes into True Invisibility. */
public final class VexAbility implements TrimAbility {

    private final AbilityConfig config;
    private final LongSupplier currentTick;
    private final Plugin plugin;
    private final MinionService minions;
    private final CooldownManager disruptCooldown = new CooldownManager();

    public VexAbility(AbilityConfig config, LongSupplier currentTick, Plugin plugin, MinionService minions) {
        this.config = config;
        this.currentTick = currentTick;
        this.plugin = plugin;
        this.minions = minions;
    }

    @Override
    public TrimPatternKind pattern() {
        return TrimPatternKind.VEX;
    }

    @Override
    public void tick(Player player, TrimTier tier) {
        Effects.refresh(player, PotionEffectType.SPEED, 1 + (tier.level() - 1) / 3, 30);
    }

    @Override
    public boolean hasActivePower() {
        return true;
    }

    @Override
    public long activationCooldownTicks(TrimTier tier) {
        int base = config.getInt("cooldown-seconds-base", 120);
        int reductionPerTier = config.getInt("cooldown-seconds-reduction-per-tier", 12);
        return Math.max(10, base - reductionPerTier * (tier.level() - 1)) * 20L;
    }

    @Override
    public void activate(Player player, TrimTier tier) {
        double radius = config.getDouble("radius", 30.0);
        double damage = config.getDouble("damage-base", 8.0) + config.getDouble("damage-per-tier", 1.0) * tier.level();
        int debuffTicks = config.getInt("debuff-duration-seconds", 20) * 20;
        int blindTicks = config.getInt("blindness-duration-seconds", 5) * 20;

        for (var enemy : Targets.nearbyLiving(player, radius, radius)) {
            enemy.damage(damage, player);
            Effects.refresh(enemy, PotionEffectType.SLOWNESS, 1, debuffTicks);
            Effects.refresh(enemy, PotionEffectType.WEAKNESS, 1, debuffTicks);
            Effects.refresh(enemy, PotionEffectType.BLINDNESS, 0, blindTicks);
        }

        int minionCount = config.getInt("minion-count-base", 3);
        long lifespanTicks = config.getInt("minion-lifespan-seconds", 45) * 20L;
        minions.spawn(player, EntityType.VEX, minionCount, lifespanTicks, currentTick.getAsLong());
    }

    @Override
    public void onIncomingDamage(Player player, TrimTier tier, EntityDamageEvent event) {
        double threshold = config.getDouble("disrupt-health-threshold", 8.0);
        if (player.getHealth() - event.getFinalDamage() > threshold) {
            return;
        }
        long cooldownTicks = config.getInt("disrupt-cooldown-seconds", 120) * 20L;
        if (!disruptCooldown.tryUse(player, "vex_invis", currentTick.getAsLong(), cooldownTicks)) {
            return;
        }

        int hideTicks = config.getInt("disrupt-hide-duration-seconds", 10) * 20;
        Effects.refresh(player, PotionEffectType.INVISIBILITY, 0, hideTicks);
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (!viewer.equals(player)) {
                viewer.hidePlayer(plugin, player);
            }
        }
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Player viewer : Bukkit.getOnlinePlayers()) {
                viewer.showPlayer(plugin, player);
            }
        }, hideTicks);
    }
}
