package com.trimsmp.trim;

import org.bukkit.inventory.meta.trim.TrimMaterial;

/**
 * Maps a vanilla trim material to a power tier (1-5). A full trim set's tier is the
 * MINIMUM tier among its four pieces' materials, so a genuinely high-quality matching
 * set is required to reach the top tier - mixing in one weak piece drags it down.
 */
public enum TrimTier {
    COMMON(1, "Common"),
    UNCOMMON(2, "Uncommon"),
    RARE(3, "Rare"),
    EPIC(4, "Epic"),
    LEGENDARY(5, "Legendary");

    private final int level;
    private final String label;

    TrimTier(int level, String label) {
        this.level = level;
        this.label = label;
    }

    public int level() {
        return level;
    }

    public String label() {
        return label;
    }

    public static TrimTier of(TrimMaterial material) {
        if (material == null) {
            return COMMON;
        }
        if (material.equals(TrimMaterial.NETHERITE)) {
            return LEGENDARY;
        }
        if (material.equals(TrimMaterial.DIAMOND)) {
            return EPIC;
        }
        if (material.equals(TrimMaterial.AMETHYST) || material.equals(TrimMaterial.EMERALD)) {
            return RARE;
        }
        if (material.equals(TrimMaterial.GOLD) || material.equals(TrimMaterial.REDSTONE)
                || material.equals(TrimMaterial.LAPIS)) {
            return UNCOMMON;
        }
        // Iron, copper, quartz, and any future material default to the common tier.
        return COMMON;
    }

    public static TrimTier min(TrimTier a, TrimTier b) {
        return a.level <= b.level ? a : b;
    }
}
