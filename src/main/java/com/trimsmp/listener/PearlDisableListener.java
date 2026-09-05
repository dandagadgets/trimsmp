package com.trimsmp.listener;

import com.trimsmp.util.Msg;
import com.trimsmp.util.PearlDisableService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

/** Cancels ender pearl teleports for players Silence's Warden's Roar or Raiser's shockwave has disabled. */
public final class PearlDisableListener implements Listener {

    private final PearlDisableService pearlDisable;

    public PearlDisableListener(PearlDisableService pearlDisable) {
        this.pearlDisable = pearlDisable;
    }

    @EventHandler(ignoreCancelled = true)
    public void onEnderPearlTeleport(PlayerTeleportEvent event) {
        if (event.getCause() != PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
            return;
        }
        Player player = event.getPlayer();
        if (pearlDisable.isDisabled(player)) {
            event.setCancelled(true);
            Msg.actionBar(player, "&c✦ Your ender pearls are disabled!");
        }
    }
}
