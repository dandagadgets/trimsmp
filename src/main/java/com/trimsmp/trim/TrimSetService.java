package com.trimsmp.trim;

import org.bukkit.entity.Player;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Computes and caches each online player's active trim set: all four armor pieces
 * must share the same trim pattern for a power to activate. Recomputed whenever the
 * player's armor changes, so the passive tick loop can just read the cache.
 */
public final class TrimSetService {

    private final Map<UUID, ActiveTrimSet> active = new HashMap<>();

    public Optional<ActiveTrimSet> get(Player player) {
        return Optional.ofNullable(active.get(player.getUniqueId()));
    }

    public void forget(Player player) {
        active.remove(player.getUniqueId());
    }

    /**
     * Recomputes the player's active set from their current armor and updates the cache.
     *
     * @return the newly active set, or empty if no matching set is currently worn.
     */
    public Optional<ActiveTrimSet> refresh(Player player) {
        Optional<ActiveTrimSet> computed = compute(player);
        UUID id = player.getUniqueId();
        if (computed.isPresent()) {
            active.put(id, computed.get());
        } else {
            active.remove(id);
        }
        return computed;
    }

    private Optional<ActiveTrimSet> compute(Player player) {
        EntityEquipment equipment = player.getEquipment();
        if (equipment == null) {
            return Optional.empty();
        }
        ItemStack[] pieces = {
                equipment.getHelmet(),
                equipment.getChestplate(),
                equipment.getLeggings(),
                equipment.getBoots()
        };

        TrimPatternKind pattern = null;
        TrimTier tier = null;

        for (ItemStack piece : pieces) {
            ArmorTrim trim = trimOf(piece);
            if (trim == null) {
                return Optional.empty();
            }
            TrimPatternKind kind = TrimPatternKind.fromBukkit(trim.getPattern());
            if (kind == null) {
                return Optional.empty();
            }
            if (pattern == null) {
                pattern = kind;
            } else if (pattern != kind) {
                return Optional.empty();
            }
            TrimTier pieceTier = TrimTier.of(trim.getMaterial());
            tier = (tier == null) ? pieceTier : TrimTier.min(tier, pieceTier);
        }

        if (pattern == null || tier == null) {
            return Optional.empty();
        }
        return Optional.of(new ActiveTrimSet(pattern, tier));
    }

    private static ArmorTrim trimOf(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return null;
        }
        ItemMeta meta = item.getItemMeta();
        if (!(meta instanceof ArmorMeta armorMeta) || !armorMeta.hasTrim()) {
            return null;
        }
        return armorMeta.getTrim();
    }
}
