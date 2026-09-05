package com.trimsmp.listener;

import com.trimsmp.minion.MinionService;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;

/** Keeps summoned trim minions (Rib's Bogged, Snout's Wither Skeletons) fighting for their owner, not against them. */
public final class MinionListener implements Listener {

    private final MinionService minions;

    public MinionListener(MinionService minions) {
        this.minions = minions;
    }

    @EventHandler(ignoreCancelled = true)
    public void onMinionTarget(EntityTargetLivingEntityEvent event) {
        if (minions.isMinion(event.getEntity()) && event.getTarget() instanceof Player) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onCombat(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player attacker && event.getEntity() instanceof LivingEntity victim) {
            minions.redirectAggro(attacker, victim);
        } else if (event.getEntity() instanceof Player defender && event.getDamager() instanceof LivingEntity attacker
                && !minions.isMinion(event.getDamager())) {
            minions.redirectAggro(defender, attacker);
        }
    }
}
