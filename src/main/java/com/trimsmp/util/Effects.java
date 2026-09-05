package com.trimsmp.util;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/** Helper for applying ambient, particle-free potion effects from the tick loop. */
public final class Effects {

    private Effects() {
    }

    public static void refresh(Player player, PotionEffectType type, int amplifier, int durationTicks) {
        player.addPotionEffect(new PotionEffect(type, durationTicks, Math.max(0, amplifier), true, false, true));
    }
}
