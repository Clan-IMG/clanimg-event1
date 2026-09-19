package net.clanimg.clanimgEvent1;

import net.clanimg.clanimgEvent1.command.EventCommand;
import net.clanimg.clanimgEvent1.config.ConfigManager;
import net.clanimg.clanimgEvent1.event.RoundManager;
import net.clanimg.clanimgEvent1.listener.PlateListener;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class ClanimgEvent1 extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();

        ConfigManager configManager = new ConfigManager(getConfig(), getLogger());
        RoundManager roundManager = new RoundManager(this, configManager);

        EventCommand eventCommand = new EventCommand(roundManager, configManager);
        PluginCommand command = getCommand("event");
        if (command != null) {
            command.setExecutor(eventCommand);
            command.setTabCompleter(eventCommand);
        } else {
            getLogger().warning("Command 'event' ist nicht in der plugin.yml registriert.");
        }

        getServer().getPluginManager().registerEvents(new PlateListener(roundManager), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
