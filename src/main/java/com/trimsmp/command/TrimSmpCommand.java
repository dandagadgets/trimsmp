package com.trimsmp.command;

import com.trimsmp.TrimSmpPlugin;
import com.trimsmp.trim.ActiveTrimSet;
import com.trimsmp.trim.TrimPatternKind;
import com.trimsmp.util.Msg;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public final class TrimSmpCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUBCOMMANDS = List.of("reload", "info", "list");

    private final TrimSmpPlugin plugin;

    public TrimSmpCommand(TrimSmpPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String sub = args.length > 0 ? args[0].toLowerCase() : "info";

        switch (sub) {
            case "reload" -> handleReload(sender);
            case "list" -> handleList(sender);
            default -> handleInfo(sender);
        }
        return true;
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("trimsmp.admin")) {
            sender.sendMessage(Msg.legacy("&cYou don't have permission to reload TrimSMP."));
            return;
        }
        plugin.reloadPluginState();
        sender.sendMessage(Msg.legacy("&aTrimSMP configuration reloaded."));
    }

    private void handleInfo(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Msg.legacy("&cOnly players have an active trim set."));
            return;
        }
        Optional<ActiveTrimSet> active = plugin.trimSetService().get(player);
        if (active.isEmpty()) {
            sender.sendMessage(Msg.legacy(
                    "&7No trim power active. Wear a matching helmet, chestplate, leggings and boots with the same trim pattern."));
            return;
        }
        ActiveTrimSet set = active.get();
        sender.sendMessage(Msg.legacy("&d&l" + set.pattern().displayName()
                + " &7(tier " + set.tier().level() + " - " + set.tier().label() + ")"));
        sender.sendMessage(Msg.legacy("&7" + set.pattern().theme()));
    }

    private void handleList(CommandSender sender) {
        sender.sendMessage(Msg.legacy("&d&lTrim Patterns:"));
        for (TrimPatternKind kind : TrimPatternKind.values()) {
            sender.sendMessage(Msg.legacy("&f" + kind.displayName() + " &7- " + kind.theme()));
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> results = new ArrayList<>();
            Stream.of(SUBCOMMANDS.toArray(new String[0]))
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .forEach(results::add);
            return results;
        }
        return List.of();
    }
}
