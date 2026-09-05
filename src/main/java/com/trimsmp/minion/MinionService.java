package com.trimsmp.minion;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Tracks temporary allied minions summoned by a trim ability (Rib's Bogged, Snout's Wither
 * Skeletons): tags them with their owner, lets combat listeners redirect their aggro to/from
 * that owner, and despawns them once their lifespan runs out.
 */
public final class MinionService {

    private final NamespacedKey ownerKey;
    private final Map<UUID, UUID> minionOwner = new HashMap<>();
    private final Map<UUID, Set<UUID>> ownerMinions = new HashMap<>();
    private final Map<UUID, Long> expiryTick = new HashMap<>();

    public MinionService(Plugin plugin) {
        this.ownerKey = new NamespacedKey(plugin, "trim_minion_owner");
    }

    public void spawn(Player owner, EntityType type, int count, long lifespanTicks, long currentTick) {
        Location base = owner.getLocation();
        for (int i = 0; i < count; i++) {
            Location spawnAt = base.clone().add(
                    (Math.random() - 0.5) * 3.0, 0, (Math.random() - 0.5) * 3.0);
            Entity entity = owner.getWorld().spawnEntity(spawnAt, type);
            entity.getPersistentDataContainer().set(ownerKey, PersistentDataType.STRING, owner.getUniqueId().toString());
            if (entity instanceof Mob mob) {
                mob.setTarget(null);
            }
            minionOwner.put(entity.getUniqueId(), owner.getUniqueId());
            ownerMinions.computeIfAbsent(owner.getUniqueId(), k -> new HashSet<>()).add(entity.getUniqueId());
            expiryTick.put(entity.getUniqueId(), currentTick + lifespanTicks);
        }
    }

    public boolean isMinion(Entity entity) {
        return minionOwner.containsKey(entity.getUniqueId());
    }

    /** Assigns `target` to any of owner's minions near them that don't already have a target. */
    public void redirectAggro(Player owner, LivingEntity target) {
        Set<UUID> ids = ownerMinions.get(owner.getUniqueId());
        if (ids == null) {
            return;
        }
        for (UUID id : ids) {
            Entity entity = Bukkit.getEntity(id);
            if (entity instanceof Mob mob && mob.isValid() && mob.getTarget() == null) {
                mob.setTarget(target);
            }
        }
    }

    /** Removes minions whose lifespan has expired. Call once per plugin tick-interval. */
    public void cleanupExpired(long currentTick) {
        expiryTick.entrySet().removeIf(entry -> {
            if (currentTick < entry.getValue()) {
                return false;
            }
            UUID id = entry.getKey();
            Entity entity = Bukkit.getEntity(id);
            if (entity != null) {
                entity.remove();
            }
            UUID owner = minionOwner.remove(id);
            if (owner != null) {
                Set<UUID> ids = ownerMinions.get(owner);
                if (ids != null) {
                    ids.remove(id);
                }
            }
            return true;
        });
    }
}
