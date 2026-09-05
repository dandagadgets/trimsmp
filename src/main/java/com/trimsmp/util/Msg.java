package com.trimsmp.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;

/** Small helper for turning '&'-coded config strings into Adventure components. */
public final class Msg {

    private Msg() {
    }

    public static Component legacy(String raw) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(raw);
    }

    public static void actionBar(Player player, String raw) {
        player.sendActionBar(legacy(raw));
    }

    public static void chat(Player player, String raw) {
        player.sendMessage(legacy(raw));
    }
}
