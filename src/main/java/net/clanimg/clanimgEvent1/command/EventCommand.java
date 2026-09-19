package net.clanimg.clanimgEvent1.command;

import net.clanimg.clanimgEvent1.config.ConfigManager;
import net.clanimg.clanimgEvent1.event.RoundManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EventCommand implements CommandExecutor, TabCompleter {

    private final RoundManager roundManager;
    private final ConfigManager configManager;

    public EventCommand(RoundManager roundManager, ConfigManager configManager) {
        this.roundManager = roundManager;
        this.configManager = configManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length != 2 || !args[0].equalsIgnoreCase("start")) {
            sender.sendMessage("§cVerwendung: /event start <round>");
            return true;
        }

        roundManager.startRound(sender, args[1]);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return filter(args[0], List.of("start"));
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("start")) {
            return filter(args[1], new ArrayList<>(configManager.getRoundIds()));
        }
        return List.of();
    }

    private List<String> filter(String prefix, List<String> options) {
        String lower = prefix.toLowerCase(Locale.ROOT);
        List<String> result = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase(Locale.ROOT).startsWith(lower)) {
                result.add(option);
            }
        }
        return result;
    }
}
