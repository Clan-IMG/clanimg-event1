package net.clanimg.clanimgEvent1.event;

import net.clanimg.clanimgEvent1.config.RoundConfig;

import java.util.UUID;

public class RoundInstance {

    private final RoundConfig round;
    private final String worldName;
    private final UUID playerId;
    private boolean finished = false;

    public RoundInstance(RoundConfig round, String worldName, UUID playerId) {
        this.round = round;
        this.worldName = worldName;
        this.playerId = playerId;
    }

    public RoundConfig getRound() {
        return round;
    }

    public String getWorldName() {
        return worldName;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }
}
