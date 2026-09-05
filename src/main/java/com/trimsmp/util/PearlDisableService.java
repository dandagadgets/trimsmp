package com.trimsmp.util;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.LongSupplier;

/** Tracks players whose ender pearls are temporarily disabled (Silence's Warden's Roar, Raiser's shockwave). */
public final class PearlDisableService {

    private final Map<UUID, Long> disabledUntil = new HashMap<>();
    private final LongSupplier currentTick;

    public PearlDisableService(LongSupplier currentTick) {
        this.currentTick = currentTick;
    }

    public void disable(Player player, long durationTicks) {
        disabledUntil.put(player.getUniqueId(), currentTick.getAsLong() + durationTicks);
    }

    public boolean isDisabled(Player player) {
        Long until = disabledUntil.get(player.getUniqueId());
        return until != null && currentTick.getAsLong() < until;
    }
}
