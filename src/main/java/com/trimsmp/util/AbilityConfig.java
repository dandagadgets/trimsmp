package com.trimsmp.util;

import org.bukkit.configuration.ConfigurationSection;

/** Thin, null-safe reader over an ability's `abilities.<key>` config section. */
public final class AbilityConfig {

    private final ConfigurationSection section;

    public AbilityConfig(ConfigurationSection section) {
        this.section = section;
    }

    public boolean enabled() {
        return section == null || section.getBoolean("enabled", true);
    }

    public double getDouble(String path, double def) {
        return section == null ? def : section.getDouble(path, def);
    }

    public int getInt(String path, int def) {
        return section == null ? def : section.getInt(path, def);
    }

    public boolean getBoolean(String path, boolean def) {
        return section == null ? def : section.getBoolean(path, def);
    }
}
