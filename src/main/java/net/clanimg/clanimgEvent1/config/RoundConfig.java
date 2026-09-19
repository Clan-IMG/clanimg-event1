package net.clanimg.clanimgEvent1.config;

import org.bukkit.Material;

public record RoundConfig(
        String id,
        String templateWorld,
        double spawnX,
        double spawnY,
        double spawnZ,
        String permission,
        long cooldownSeconds,
        Material plateMaterial,
        String targetLocationName
) {
}
