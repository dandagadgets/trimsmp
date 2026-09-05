package com.trimsmp.listener;

import com.trimsmp.TrimSmpPlugin;
import com.trimsmp.ability.TrimAbility;
import com.trimsmp.trim.ActiveTrimSet;
import com.trimsmp.trim.TrimTier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;

import java.util.Optional;

/** Routes combat and mob-targeting events to whichever trim ability the involved player has active. */
public final class CombatListener implements Listener {

    private final TrimSmpPlugin plugin;

    public CombatListener(TrimSmpPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onIncomingDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            withAbility(player, (ability, tier) -> ability.onIncomingDamage(player, tier, event));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDealDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            withAbility(player, (ability, tier) -> ability.onDealDamage(player, tier, event));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onTarget(EntityTargetLivingEntityEvent event) {
        if (event.getTarget() instanceof Player player) {
            withAbility(player, (ability, tier) -> ability.onTargetedBy(player, tier, event));
        }
    }

    private void withAbility(Player player, AbilityAction action) {
        Optional<ActiveTrimSet> active = plugin.trimSetService().get(player);
        if (active.isEmpty()) {
            return;
        }
        TrimAbility ability = plugin.abilityRegistry().get(active.get().pattern());
        if (ability != null) {
            action.run(ability, active.get().tier());
        }
    }

    @FunctionalInterface
    private interface AbilityAction {
        void run(TrimAbility ability, TrimTier tier);
    }
}
