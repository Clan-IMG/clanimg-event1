package net.clanimg.clanimgEvent1.event;

import net.clanimg.clanimgEvent1.config.ConfigManager;
import net.clanimg.clanimgEvent1.config.LocationConfig;
import net.clanimg.clanimgEvent1.config.RoundConfig;
import net.clanimg.clanimgEvent1.world.WorldCloner;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class RoundManager {

    private static final long CLEANUP_DELAY_TICKS = 20L * 15;

    private final Plugin plugin;
    private final ConfigManager configManager;
    private final WorldCloner worldCloner;

    private final Map<String, Long> lastStart = new HashMap<>();
    private final Map<String, Integer> instanceCounters = new HashMap<>();
    private final Map<String, RoundInstance> activeInstances = new HashMap<>();

    public RoundManager(Plugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.worldCloner = new WorldCloner(plugin);
    }

    public void startRound(CommandSender sender, String roundId) {
        RoundConfig round = configManager.getRound(roundId);
        if (round == null) {
            sender.sendMessage("§cUnbekannte Runde: " + roundId);
            return;
        }

        if (!sender.hasPermission(round.permission())) {
            sender.sendMessage("§cDazu hast du keine Berechtigung.");
            return;
        }

        long now = System.currentTimeMillis();
        long last = lastStart.getOrDefault(roundId, 0L);
        long remainingMs = (last + round.cooldownSeconds() * 1000L) - now;
        if (remainingMs > 0) {
            sender.sendMessage("§cBitte warte noch " + (remainingMs / 1000 + 1) + " Sekunden.");
            return;
        }

        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        if (players.isEmpty()) {
            sender.sendMessage("§cKeine Spieler online.");
            return;
        }

        lastStart.put(roundId, now);
        sender.sendMessage("§aErstelle " + players.size() + " Welt(en) fuer '" + roundId + "' ...");

        List<Player> orderedPlayers = new ArrayList<>(players);
        List<CompletableFuture<World>> futures = new ArrayList<>();
        for (Player ignored : orderedPlayers) {
            int index = instanceCounters.merge(roundId, 1, Integer::sum);
            String worldName = roundId + "-" + index;
            futures.add(worldCloner.cloneAndLoad(round.templateWorld(), worldName));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).whenComplete((v, throwable) ->
                Bukkit.getScheduler().runTask(plugin, () -> {
                    for (int i = 0; i < orderedPlayers.size(); i++) {
                        Player player = orderedPlayers.get(i);
                        World world;
                        try {
                            world = futures.get(i).getNow(null);
                        } catch (Exception e) {
                            plugin.getLogger().warning("Instanzwelt fuer '" + roundId + "' konnte nicht erstellt werden: " + e.getMessage());
                            continue;
                        }

                        if (world == null) {
                            continue;
                        }

                        if (!player.isOnline()) {
                            worldCloner.unloadAndDelete(world.getName());
                            continue;
                        }

                        Location destination = new Location(world, round.spawnX(), round.spawnY(), round.spawnZ());
                        player.teleport(destination);
                        activeInstances.put(world.getName(), new RoundInstance(round, world.getName(), player.getUniqueId()));
                    }
                }));
    }

    public RoundInstance getActiveInstance(String worldName) {
        return activeInstances.get(worldName);
    }

    public void onPlateTriggered(RoundInstance instance) {
        if (instance.isFinished()) {
            return;
        }
        instance.setFinished(true);

        Player player = Bukkit.getPlayer(instance.getPlayerId());
        LocationConfig target = configManager.getLocation(instance.getRound().targetLocationName());
        if (player != null && target != null) {
            World targetWorld = Bukkit.getWorld(target.world());
            if (targetWorld != null) {
                player.teleport(new Location(targetWorld, target.x(), target.y(), target.z()));
            }
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            activeInstances.remove(instance.getWorldName());
            worldCloner.unloadAndDelete(instance.getWorldName());
        }, CLEANUP_DELAY_TICKS);
    }
}
