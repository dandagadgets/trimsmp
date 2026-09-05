package com.trimsmp.ability.impl;

import com.trimsmp.ability.TrimAbility;
import com.trimsmp.trim.TrimPatternKind;
import com.trimsmp.trim.TrimTier;
import com.trimsmp.util.AbilityConfig;
import com.trimsmp.util.Effects;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.potion.PotionEffectType;

/** Eye: an unblinking gaze that endermen refuse to meet. */
public final class EyeAbility implements TrimAbility {

    private final AbilityConfig config;
    private final int passiveDurationTicks;

    public EyeAbility(AbilityConfig config, int passiveDurationTicks) {
        this.config = config;
        this.passiveDurationTicks = passiveDurationTicks;
    }

    @Override
    public TrimPatternKind pattern() {
        return TrimPatternKind.EYE;
    }

    @Override
    public void tick(Player player, TrimTier tier) {
        if (config.getBoolean("night-vision", true)) {
            Effects.refresh(player, PotionEffectType.NIGHT_VISION, 0, passiveDurationTicks);
        }
    }

    @Override
    public void onTargetedBy(Player player, TrimTier tier, EntityTargetLivingEntityEvent event) {
        if (event.getEntity() instanceof Enderman) {
            event.setCancelled(true);
        }
    }
}
