package com.trimsmp.listener;

import com.trimsmp.TrimSmpPlugin;
import com.trimsmp.ability.TrimAbility;
import com.trimsmp.trim.ActiveTrimSet;
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

        long cooldownTicks = ability.activationCooldownTicks(set.tier());
        boolean ready = plugin.activationCooldowns()
                .tryUse(player, set.pattern().configKey(), plugin.currentTick(), cooldownTicks);
        if (!ready) {
            return;
        }

        event.setCancelled(true);
        ability.activate(player, set.tier());
    }
}
