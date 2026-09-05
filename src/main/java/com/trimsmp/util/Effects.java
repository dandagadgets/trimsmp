package com.trimsmp.util;

import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/** Helper for applying ambient, particle-free potion effects from the tick loop. */
public final class Effects {

    private Effects() {
    }

    public static void refresh(LivingEntity entity, PotionEffectType type, int amplifier, int durationTicks) {
        entity.addPotionEffect(new PotionEffect(type, durationTicks, Math.max(0, amplifier), true, false, true));
    }
}
