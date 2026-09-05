package com.trimsmp.trim;

import org.bukkit.inventory.meta.trim.TrimPattern;

/**
 * Wraps every vanilla armor trim pattern with the metadata TrimSMP needs:
 * a stable config key, a display name, and a theme blurb shown in /trimsmp info.
 */
public enum TrimPatternKind {
    SENTRY(TrimPattern.SENTRY, "sentry", "Sentry", "Watches the perimeter for threats."),
    VEX(TrimPattern.VEX, "vex", "Vex", "Blinks through the world like a vexing spirit."),
    WILD(TrimPattern.WILD, "wild", "Wild", "Draws vigor from untamed nature."),
    COAST(TrimPattern.COAST, "coast", "Coast", "Moves through water like the tide itself."),
    DUNE(TrimPattern.DUNE, "dune", "Dune", "Endures heat and shapes the desert sands."),
    WARD(TrimPattern.WARD, "ward", "Ward", "Wards off death with a lingering shield."),
    EYE(TrimPattern.EYE, "eye", "Eye", "Sees all, and all that see it look away."),
    TIDE(TrimPattern.TIDE, "tide", "Tide", "Carries the ocean's current wherever it goes."),
    SNOUT(TrimPattern.SNOUT, "snout", "Snout", "Bartered peace with the creatures of the nether."),
    RIB(TrimPattern.RIB, "rib", "Rib", "Strikes fear into the bones of the undead."),
    SPIRE(TrimPattern.SPIRE, "spire", "Spire", "Falls from great heights without consequence."),
    WAYFINDER(TrimPattern.WAYFINDER, "wayfinder", "Wayfinder", "Always knows the way home."),
    SHAPER(TrimPattern.SHAPER, "shaper", "Shaper", "Shapes stone and ore with practiced ease."),
    SILENCE(TrimPattern.SILENCE, "silence", "Silence", "Moves through the world unnoticed."),
    RAISER(TrimPattern.RAISER, "raiser", "Raiser", "Rallies allies to greater heights in battle."),
    HOST(TrimPattern.HOST, "host", "Host", "A welcome guest wherever villagers gather."),
    FLOW(TrimPattern.FLOW, "flow", "Flow", "Rides a gust of wind across the battlefield."),
    BOLT(TrimPattern.BOLT, "bolt", "Bolt", "Strikes like lightning in the heat of combat.");

    private final TrimPattern bukkitPattern;
    private final String configKey;
    private final String displayName;
    private final String theme;

    TrimPatternKind(TrimPattern bukkitPattern, String configKey, String displayName, String theme) {
        this.bukkitPattern = bukkitPattern;
        this.configKey = configKey;
        this.displayName = displayName;
        this.theme = theme;
    }

    public TrimPattern bukkitPattern() {
        return bukkitPattern;
    }

    public String configKey() {
        return configKey;
    }

    public String displayName() {
        return displayName;
    }

    public String theme() {
        return theme;
    }

    public static TrimPatternKind fromBukkit(TrimPattern pattern) {
        for (TrimPatternKind kind : values()) {
            if (kind.bukkitPattern.equals(pattern)) {
                return kind;
            }
        }
        return null;
    }
}
