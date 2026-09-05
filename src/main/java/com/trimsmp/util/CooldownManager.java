package com.trimsmp.util;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Tracks per-player, per-pattern cooldowns for manually activated trim powers. */
public final class CooldownManager {

    private final Map<UUID, Map<String, Long>> readyAtTick = new HashMap<>();

    /**
     * @return true and starts the cooldown if the player's power for this key is ready;
     *         false if it's still on cooldown.
     */
    public boolean tryUse(Player player, String key, long currentTick, long cooldownTicks) {
        Map<String, Long> perPlayer = readyAtTick.computeIfAbsent(player.getUniqueId(), id -> new HashMap<>());
        long readyAt = perPlayer.getOrDefault(key, 0L);
        if (currentTick < readyAt) {
            return false;
        }
        perPlayer.put(key, currentTick + cooldownTicks);
        return true;
    }

    public void forget(Player player) {
        readyAtTick.remove(player.getUniqueId());
    }
}
