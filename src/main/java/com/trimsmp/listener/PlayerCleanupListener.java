package com.trimsmp.listener;

import com.trimsmp.TrimSmpPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/** Clears a departing player's cached trim state so it doesn't linger in memory. */
public final class PlayerCleanupListener implements Listener {

    private final TrimSmpPlugin plugin;

    public PlayerCleanupListener(TrimSmpPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.trimSetService().forget(player);
        plugin.activationCooldowns().forget(player);
    }
}
