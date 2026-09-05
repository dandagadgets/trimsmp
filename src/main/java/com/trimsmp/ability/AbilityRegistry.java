package com.trimsmp.ability;

import com.trimsmp.ability.impl.BoltAbility;
import com.trimsmp.ability.impl.CoastAbility;
import com.trimsmp.ability.impl.DuneAbility;
import com.trimsmp.ability.impl.EyeAbility;
import com.trimsmp.ability.impl.FlowAbility;
import com.trimsmp.ability.impl.HostAbility;
import com.trimsmp.ability.impl.RaiserAbility;
import com.trimsmp.ability.impl.RibAbility;
import com.trimsmp.ability.impl.SentryAbility;
import com.trimsmp.ability.impl.ShaperAbility;
import com.trimsmp.ability.impl.SilenceAbility;
import com.trimsmp.ability.impl.SnoutAbility;
import com.trimsmp.ability.impl.SpireAbility;
import com.trimsmp.ability.impl.TideAbility;
import com.trimsmp.ability.impl.VexAbility;
import com.trimsmp.ability.impl.WardAbility;
import com.trimsmp.ability.impl.WayfinderAbility;
import com.trimsmp.ability.impl.WildAbility;
import com.trimsmp.TrimSmpPlugin;
import com.trimsmp.trim.TrimPatternKind;
import com.trimsmp.util.AbilityConfig;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

/** Builds and holds the one {@link TrimAbility} instance for each enabled trim pattern. */
public final class AbilityRegistry {

    private final Map<TrimPatternKind, TrimAbility> abilities = new EnumMap<>(TrimPatternKind.class);

    public AbilityRegistry(TrimSmpPlugin plugin, FileConfiguration config, int tickIntervalTicks) {
        int passiveDuration = tickIntervalTicks + 20;

        registerIfEnabled(config, "sentry", () -> new SentryAbility(section(config, "sentry")));
        registerIfEnabled(config, "vex", () -> new VexAbility(section(config, "vex")));
        registerIfEnabled(config, "wild", () -> new WildAbility(section(config, "wild"), passiveDuration));
        registerIfEnabled(config, "coast", () -> new CoastAbility(section(config, "coast"), passiveDuration));
        registerIfEnabled(config, "dune", () -> new DuneAbility(section(config, "dune"), passiveDuration));
        registerIfEnabled(config, "ward", () -> new WardAbility(section(config, "ward"), plugin::currentTick));
        registerIfEnabled(config, "eye", () -> new EyeAbility(section(config, "eye"), passiveDuration));
        registerIfEnabled(config, "tide", () -> new TideAbility(section(config, "tide"), passiveDuration));
        registerIfEnabled(config, "snout", SnoutAbility::new);
        registerIfEnabled(config, "rib", () -> new RibAbility(section(config, "rib")));
        registerIfEnabled(config, "spire", () -> new SpireAbility(section(config, "spire"), passiveDuration));
        registerIfEnabled(config, "wayfinder", () -> new WayfinderAbility(section(config, "wayfinder"), passiveDuration));
        registerIfEnabled(config, "shaper", () -> new ShaperAbility(section(config, "shaper"), passiveDuration));
        registerIfEnabled(config, "silence", () -> new SilenceAbility(section(config, "silence")));
        registerIfEnabled(config, "raiser", () -> new RaiserAbility(section(config, "raiser")));
        registerIfEnabled(config, "host", () -> new HostAbility(section(config, "host")));
        registerIfEnabled(config, "flow", () -> new FlowAbility(section(config, "flow")));
        registerIfEnabled(config, "bolt", () -> new BoltAbility(section(config, "bolt")));
    }

    private void registerIfEnabled(FileConfiguration config, String key, Supplier<TrimAbility> factory) {
        if (config.getBoolean("abilities." + key + ".enabled", true)) {
            TrimAbility ability = factory.get();
            abilities.put(ability.pattern(), ability);
        }
    }

    private static AbilityConfig section(FileConfiguration config, String key) {
        return new AbilityConfig(config.getConfigurationSection("abilities." + key));
    }

    /** Returns the ability for this pattern, or null if it's disabled or unregistered. */
    public TrimAbility get(TrimPatternKind pattern) {
        return abilities.get(pattern);
    }
}
