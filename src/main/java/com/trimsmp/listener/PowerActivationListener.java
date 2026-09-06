package com.trimsmp.listener;

import com.trimsmp.TrimSmpPlugin;
import com.trimsmp.ability.TrimAbility;
import com.trimsmp.trim.ActiveTrimSet;
import com.trimsmp.util.Msg;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

import java.util.Optional;

/** Sneak + swap-hands triggers the wearer's active trim power, when their set has one and it's off cooldown. */
public final class PowerActivationListener implements Listener {

    private final TrimSmpPlugin plugin;

    public PowerActivationListener(TrimSmpPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onSwapHands(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        if (!player.isSneaking()) {
            return;
        }

        Optional<ActiveTrimSet> active = plugin.trimSetService().get(player);
        if (active.isEmpty()) {
            return;
        }
        ActiveTrimSet set = active.get();
        TrimAbility ability = plugin.abilityRegistry().get(set.pattern());
        if (ability == null || !ability.hasActivePower()) {
            return;
        }

        event.setCancelled(true);
        String cooldownKey = set.pattern().configKey();
        long currentTick = plugin.currentTick();

        long remaining = plugin.activationCooldowns().remainingTicks(player, cooldownKey, currentTick);
        if (remaining > 0) {
            long seconds = (remaining + 19) / 20;
            Msg.actionBar(player, "&7" + set.pattern().displayName() + " on cooldown - &f" + seconds + "s");
            return;
        }

        long cooldownTicks = ability.activationCooldownTicks(set.tier());
        boolean ready = plugin.activationCooldowns().tryUse(player, cooldownKey, currentTick, cooldownTicks);
        if (!ready) {
            return;
        }

        ability.activate(player, set.tier());
        Msg.actionBar(player, "&d✦ " + set.pattern().displayName() + " activated!");
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.6f, 1.4f);
    }
}
