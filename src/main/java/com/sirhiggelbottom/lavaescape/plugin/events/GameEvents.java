package com.sirhiggelbottom.lavaescape.plugin.events;

import com.sirhiggelbottom.lavaescape.plugin.LavaEscapePlugin;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GameEvents implements Listener {
    private final LavaEscapePlugin plugin;
    private final Map<UUID, Location[]> playerSelections;

    public GameEvents(LavaEscapePlugin plugin) {
        this.plugin = plugin;
        this.playerSelections = new HashMap<>();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item != null && item.getType() == Material.STICK && item.getItemMeta().getDisplayName().equals("Wand")) {
            if (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                UUID playerId = event.getPlayer().getUniqueId();
                Location[] selections = playerSelections.computeIfAbsent(playerId, k -> new Location[2]);

                if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                    selections[0] = event.getClickedBlock().getLocation();
                    event.getPlayer().sendMessage("First position set.");
                } else {
                    selections[1] = event.getClickedBlock().getLocation();
                    event.getPlayer().sendMessage("Second position set.");
                }
            }
        }
    }

    public Location getFirstPosition(UUID playerId) {
        Location[] selections = playerSelections.get(playerId);
        return (selections != null && selections[0] != null) ? selections[0] : null;
    }

    public Location getSecondPosition(UUID playerId) {
        Location[] selections = playerSelections.get(playerId);
        return (selections != null && selections[1] != null) ? selections[1] : null;
    }
}

