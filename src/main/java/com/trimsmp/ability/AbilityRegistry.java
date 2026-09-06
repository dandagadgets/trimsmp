package com.trimsmp.ability;

import com.trimsmp.TrimSmpPlugin;
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
import com.trimsmp.minion.MinionService;
import com.trimsmp.trim.TrimPatternKind;
import com.trimsmp.util.AbilityConfig;
import com.trimsmp.util.PearlDisableService;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

/** Builds and holds the one {@link TrimAbility} instance for each enabled trim pattern. */
public final class AbilityRegistry {

    private final Map<TrimPatternKind, TrimAbility> abilities = new EnumMap<>(TrimPatternKind.class);

    public AbilityRegistry(TrimSmpPlugin plugin, FileConfiguration config, int tickIntervalTicks,
                            MinionService minions, PearlDisableService pearlDisable) {
        int passiveDuration = tickIntervalTicks + 20;

        registerIfEnabled(config, "sentry", () -> new SentryAbility(section(config, "sentry"), passiveDuration));
        registerIfEnabled(config, "vex", () -> new VexAbility(section(config, "vex"), plugin::currentTick, plugin, minions));
        registerIfEnabled(config, "wild", () -> new WildAbility(section(config, "wild"), plugin::currentTick, plugin));
        registerIfEnabled(config, "coast", () -> new CoastAbility(section(config, "coast"), plugin));
        registerIfEnabled(config, "dune", () -> new DuneAbility(section(config, "dune"), passiveDuration));
        registerIfEnabled(config, "ward", () -> new WardAbility(section(config, "ward")));
        registerIfEnabled(config, "eye", () -> new EyeAbility(section(config, "eye"), passiveDuration, minions, plugin::currentTick));
        registerIfEnabled(config, "tide", () -> new TideAbility(section(config, "tide"), plugin));
        registerIfEnabled(config, "snout", () -> new SnoutAbility(section(config, "snout"), minions, plugin::currentTick));
        registerIfEnabled(config, "rib", () -> new RibAbility(section(config, "rib"), minions, plugin::currentTick, plugin));
        registerIfEnabled(config, "spire", () -> new SpireAbility(section(config, "spire"), plugin));
        registerIfEnabled(config, "wayfinder", () -> new WayfinderAbility(section(config, "wayfinder"), plugin));
        registerIfEnabled(config, "shaper", () -> new ShaperAbility(section(config, "shaper"), passiveDuration));
        registerIfEnabled(config, "silence", () -> new SilenceAbility(section(config, "silence"), plugin::currentTick, pearlDisable));
        registerIfEnabled(config, "raiser", () -> new RaiserAbility(section(config, "raiser"), pearlDisable));
        registerIfEnabled(config, "host", () -> new HostAbility(section(config, "host")));
        registerIfEnabled(config, "flow", () -> new FlowAbility(section(config, "flow"), plugin));
        registerIfEnabled(config, "bolt", () -> new BoltAbility(section(config, "bolt"), plugin::currentTick));
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
