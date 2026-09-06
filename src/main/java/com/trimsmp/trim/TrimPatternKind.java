package com.trimsmp.trim;

import org.bukkit.inventory.meta.trim.TrimPattern;

/**
 * Wraps every vanilla armor trim pattern with the metadata TrimSMP needs:
 * a stable config key, a display name, and a theme blurb shown in /trimsmp info.
 */
public enum TrimPatternKind {
    SENTRY(TrimPattern.SENTRY, "sentry", "Sentry", "Fires spectral arrows at nearby foes, watching the perimeter."),
    VEX(TrimPattern.VEX, "vex", "Vex", "Debuffs nearby foes, summons 3 allied Vexes, then vanishes when badly hurt."),
    WILD(TrimPattern.WILD, "wild", "Wild", "Grapples to blocks or enemies, growing damaging grass on impact; roots foes when you're low."),
    COAST(TrimPattern.COAST, "coast", "Coast", "Summons a tsunami that batters everything nearby, then fades."),
    DUNE(TrimPattern.DUNE, "dune", "Dune", "Whips up a sandstorm that blinds and batters nearby foes."),
    WARD(TrimPattern.WARD, "ward", "Ward", "A guardian slam that damages foes, then shields you."),
    EYE(TrimPattern.EYE, "eye", "Eye", "Summons an allied Enderman to fight at your side."),
    TIDE(TrimPattern.TIDE, "tide", "Tide", "Rides a tidal surge forward, smashing anything in the way."),
    SNOUT(TrimPattern.SNOUT, "snout", "Snout", "Summons Wither Skeletons to fight at your side."),
    RIB(TrimPattern.RIB, "rib", "Rib", "Summons Bogged allies and encases you in a bony shell."),
    SPIRE(TrimPattern.SPIRE, "spire", "Spire", "Dashes forward, smashing anything in the way."),
    WAYFINDER(TrimPattern.WAYFINDER, "wayfinder", "Wayfinder", "Lunges straight toward whoever you're looking at."),
    SHAPER(TrimPattern.SHAPER, "shaper", "Shaper", "Slams the ground, erupting stone spikes under nearby foes."),
    SILENCE(TrimPattern.SILENCE, "silence", "Silence", "Blinds enemies and disables their pearls; echoes back when hurt."),
    RAISER(TrimPattern.RAISER, "raiser", "Raiser", "Slams the ground, pulling in and weakening nearby foes."),
    HOST(TrimPattern.HOST, "host", "Host", "Steals health and buffs from everyone nearby."),
    FLOW(TrimPattern.FLOW, "flow", "Flow", "Grants temporary flight, fueled by your own health."),
    BOLT(TrimPattern.BOLT, "bolt", "Bolt", "Chain lightning, then charges your next 3 hits with lightning too.");

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
