package net.clanimg.clanimgEvent1.listener;

import net.clanimg.clanimgEvent1.event.RoundInstance;
import net.clanimg.clanimgEvent1.event.RoundManager;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlateListener implements Listener {

    private final RoundManager roundManager;

    public PlateListener(RoundManager roundManager) {
        this.roundManager = roundManager;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.PHYSICAL) {
            return;
        }

        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }

        RoundInstance instance = roundManager.getActiveInstance(block.getWorld().getName());
        if (instance == null || instance.isFinished()) {
            return;
        }

        if (block.getType() != instance.getRound().plateMaterial()) {
            return;
        }

        if (!event.getPlayer().getUniqueId().equals(instance.getPlayerId())) {
            return;
        }

        roundManager.onPlateTriggered(instance);
    }
}
