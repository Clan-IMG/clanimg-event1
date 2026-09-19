package net.clanimg.clanimgEvent1.config;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class ConfigManager {

    private final Map<String, LocationConfig> locations = new HashMap<>();
    private final Map<String, RoundConfig> rounds = new HashMap<>();

    public ConfigManager(FileConfiguration config, Logger logger) {
        for (String key : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(key);
            if (section == null) {
                continue;
            }

            ConfigurationSection spawnSection = section.getConfigurationSection("spawn");
            if (spawnSection == null) {
                continue;
            }

            LocationConfig location = parseLocation(spawnSection);
            locations.put(key, location);

            ConfigurationSection startSection = section.getConfigurationSection("start");
            if (startSection == null) {
                continue;
            }

            RoundConfig round = parseRound(key, location, spawnSection, startSection, section, logger);
            if (round != null) {
                rounds.put(key, round);
            }
        }
    }

    private LocationConfig parseLocation(ConfigurationSection spawnSection) {
        String world = spawnSection.getString("world");
        ConfigurationSection position = spawnSection.getConfigurationSection("position");
        double x = position != null ? position.getDouble("x") : 0;
        double y = position != null ? position.getDouble("y") : 0;
        double z = position != null ? position.getDouble("z") : 0;
        return new LocationConfig(world, x, y, z);
    }

    private RoundConfig parseRound(String key, LocationConfig location, ConfigurationSection spawnSection,
                                    ConfigurationSection startSection, ConfigurationSection section, Logger logger) {
        boolean soloworld = spawnSection.getBoolean("soloworld", false);
        if (!soloworld) {
            logger.warning("Runde '" + key + "' hat kein 'soloworld: true' - das wird aktuell nicht unterstuetzt, Runde wird ignoriert.");
            return null;
        }

        ConfigurationSection targetSection = section.getConfigurationSection("target.event");
        if (targetSection == null) {
            logger.warning("Runde '" + key + "' hat kein target.event definiert, Runde wird ignoriert.");
            return null;
        }

        String plateRaw = targetSection.getString("plate");
        Material plate = plateRaw != null ? Material.matchMaterial(plateRaw) : null;
        if (plate == null) {
            logger.warning("Runde '" + key + "' hat ein ungueltiges plate-Material '" + plateRaw + "', Runde wird ignoriert.");
            return null;
        }

        String targetLocation = targetSection.getString("tp");
        if (targetLocation == null) {
            logger.warning("Runde '" + key + "' hat kein target.event.tp definiert, Runde wird ignoriert.");
            return null;
        }

        String permission = startSection.getString("permission", "");
        long cooldownSeconds = parseDuration(startSection.getString("cooldown", "0s"), logger, key);

        return new RoundConfig(
                key,
                location.world(),
                location.x(), location.y(), location.z(),
                permission,
                cooldownSeconds,
                plate,
                targetLocation
        );
    }

    private long parseDuration(String raw, Logger logger, String context) {
        if (raw == null || raw.isEmpty()) {
            return 0L;
        }
        try {
            char unit = raw.charAt(raw.length() - 1);
            if (Character.isDigit(unit)) {
                return Long.parseLong(raw);
            }
            long value = Long.parseLong(raw.substring(0, raw.length() - 1));
            return switch (Character.toLowerCase(unit)) {
                case 's' -> value;
                case 'm' -> value * 60;
                case 'h' -> value * 3600;
                case 'd' -> value * 86400;
                default -> {
                    logger.warning("Unbekannte Zeiteinheit in Cooldown '" + raw + "' (" + context + "), nutze Sekunden.");
                    yield value;
                }
            };
        } catch (NumberFormatException e) {
            logger.warning("Konnte Cooldown '" + raw + "' nicht parsen (" + context + "), nutze 0 Sekunden.");
            return 0L;
        }
    }

    public LocationConfig getLocation(String name) {
        return locations.get(name);
    }

    public RoundConfig getRound(String id) {
        return rounds.get(id);
    }

    public Set<String> getRoundIds() {
        return rounds.keySet();
    }
}
