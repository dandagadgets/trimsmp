package com.trimsmp;

import com.trimsmp.ability.AbilityRegistry;
import com.trimsmp.command.TrimSmpCommand;
import com.trimsmp.listener.CombatListener;
import com.trimsmp.listener.MinionListener;
import com.trimsmp.listener.PearlDisableListener;
import com.trimsmp.listener.PlayerCleanupListener;
import com.trimsmp.listener.PowerActivationListener;
import com.trimsmp.minion.MinionService;
import com.trimsmp.trim.ActiveTrimSet;
import com.trimsmp.trim.TrimSetService;
import com.trimsmp.util.CooldownManager;
import com.trimsmp.util.Msg;
import com.trimsmp.util.PearlDisableService;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;

public final class TrimSmpPlugin extends JavaPlugin {

    private TrimSetService trimSetService;
    private AbilityRegistry abilityRegistry;
    private CooldownManager activationCooldowns;
    private MinionService minionService;
    private PearlDisableService pearlDisableService;
    private int tickIntervalTicks;
    private long currentTick = 0L;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.minionService = new MinionService(this);
        this.pearlDisableService = new PearlDisableService(this::currentTick);
        reloadPluginState();

        getServer().getPluginManager().registerEvents(new PlayerCleanupListener(this), this);
        getServer().getPluginManager().registerEvents(new CombatListener(this), this);
        getServer().getPluginManager().registerEvents(new PowerActivationListener(this), this);
        getServer().getPluginManager().registerEvents(new MinionListener(minionService), this);
        getServer().getPluginManager().registerEvents(new PearlDisableListener(pearlDisableService), this);

        TrimSmpCommand command = new TrimSmpCommand(this);
        getCommand("trimsmp").setExecutor(command);
        getCommand("trimsmp").setTabCompleter(command);

        getServer().getScheduler().runTaskTimer(this, () -> currentTick++, 1L, 1L);
        getServer().getScheduler().runTaskTimer(this, this::tickAllPlayers, tickIntervalTicks, tickIntervalTicks);
    }

    /** Re-reads config.yml and rebuilds the ability registry. Existing armor-based cache is untouched. */
    public void reloadPluginState() {
        reloadConfig();
        int seconds = Math.max(1, getConfig().getInt("general.tick-interval-seconds", 1));
        this.tickIntervalTicks = seconds * 20;
        this.abilityRegistry = new AbilityRegistry(this, getConfig(), tickIntervalTicks, minionService, pearlDisableService);
        if (this.trimSetService == null) {
            this.trimSetService = new TrimSetService();
        }
        if (this.activationCooldowns == null) {
            this.activationCooldowns = new CooldownManager();
        }
    }

    /** Ticks elapsed since this plugin enabled, incremented once per server tick. */
    public long currentTick() {
        return currentTick;
    }

    private void tickAllPlayers() {
        minionService.cleanupExpired(currentTick);

        boolean announce = getConfig().getBoolean("general.announce-activation", true);
        String template = getConfig().getString("general.activation-message",
                "&d&lTRIM SET ACTIVE &7- &f%pattern% &7(tier %tier%)");

        for (Player player : getServer().getOnlinePlayers()) {
            Optional<ActiveTrimSet> before = trimSetService.get(player);
            Optional<ActiveTrimSet> after = trimSetService.refresh(player);

            if (after.isEmpty()) {
                continue;
            }
            ActiveTrimSet active = after.get();
            if (announce && !after.equals(before)) {
                String message = template
                        .replace("%pattern%", active.pattern().displayName())
                        .replace("%tier%", String.valueOf(active.tier().level()));
                Msg.actionBar(player, message);
            }

            var ability = abilityRegistry.get(active.pattern());
            if (ability != null) {
                ability.tick(player, active.tier());
            }
        }
    }

    public TrimSetService trimSetService() {
        return trimSetService;
    }

    public AbilityRegistry abilityRegistry() {
        return abilityRegistry;
    }

    public CooldownManager activationCooldowns() {
        return activationCooldowns;
    }
}
