package com.trimsmp.ability.impl;

import com.trimsmp.ability.TrimAbility;
import com.trimsmp.trim.TrimPatternKind;
import com.trimsmp.trim.TrimTier;
import com.trimsmp.util.AbilityConfig;
import com.trimsmp.util.CooldownManager;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.function.LongSupplier;

/** Ward: a lingering shield that softens the next blow, then needs a moment to recharge. */
public final class WardAbility implements TrimAbility {

    private final AbilityConfig config;
    private final LongSupplier currentTick;
    private final CooldownManager cooldowns = new CooldownManager();

    public WardAbility(AbilityConfig config, LongSupplier currentTick) {
        this.config = config;
        this.currentTick = currentTick;
    }

    @Override
    public TrimPatternKind pattern() {
        return TrimPatternKind.WARD;
    }

    @Override
    public void onIncomingDamage(Player player, TrimTier tier, EntityDamageEvent event) {
        if (event.getDamage() <= 0) {
            return;
        }
        int base = config.getInt("cooldown-seconds-base", 25);
        int reductionPerTier = config.getInt("cooldown-seconds-reduction-per-tier", 4);
        long cooldownTicks = Math.max(2, base - reductionPerTier * (tier.level() - 1)) * 20L;

        if (!cooldowns.tryUse(player, "ward", currentTick.getAsLong(), cooldownTicks)) {
            return;
        }

        double percent = config.getDouble("damage-reduction-base-percent", 10)
                + config.getDouble("damage-reduction-per-tier-percent", 6) * tier.level();
        double multiplier = Math.max(0.0, 1.0 - (percent / 100.0));
        event.setDamage(event.getDamage() * multiplier);
    }
}
