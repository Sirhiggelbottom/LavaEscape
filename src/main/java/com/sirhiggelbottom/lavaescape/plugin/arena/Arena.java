package com.sirhiggelbottom.lavaescape.plugin.arena;

import com.sirhiggelbottom.lavaescape.plugin.Main;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class Arena implements Listener {

    private final Main plugin;
    private final Player player;
    private Block pos1;
    private Block pos2;

    private boolean awaitingArenaName = false; // Flag to indicate if we are waiting for the arena name

    public Arena(JavaPlugin plugin, Player player, String arenaName) {
        this.plugin = (Main) plugin;
        this.player = player;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getPlayer() == player) {
            Action action = event.getAction();
            ItemStack handItem = player.getInventory().getItemInMainHand();

            // Check if the player is holding the custom location wand stick
            if (handItem.getType() == Material.STICK && handItem.hasItemMeta() && Objects.requireNonNull(handItem.getItemMeta()).getDisplayName().equals("Location Wand")) {
                if (action == Action.LEFT_CLICK_BLOCK) {
                    pos1 = event.getClickedBlock();
                    player.sendMessage(ChatColor.GREEN + "Position 1 set.");
                } else if (action == Action.RIGHT_CLICK_BLOCK) {
                    pos2 = event.getClickedBlock();
                    player.sendMessage(ChatColor.GREEN + "Position 2 set.");

                    // Check if both positions have been set
                    if (pos1 != null && pos2 != null) {
                        // Prompt the user for an arena name
                        player.sendMessage(ChatColor.YELLOW + "Please enter a name for the arena:");
                        awaitingArenaName = true; // Set the flag to true to indicate we are waiting for the arena name.
                    } else {
                        player.sendMessage(ChatColor.RED + "You must set both positions (pos1 and pos2) before entering the arena name.");
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (event.getPlayer() == player && awaitingArenaName) {
            event.setCancelled(true); // Cancel the chat event to prevent the message from being sent to the chat.

            // Get the arena name from the player's chat input
            String arenaName = event.getMessage();

            // Perform any necessary validation on the arenaName here (e.g., check if it's a valid name).

            // Save the arena to the YAML file
            ArenaManager arenaManager = new ArenaManager(plugin);
            arenaManager.saveArena(arenaName, pos1, pos2);

            // Reset the flag and notify the player
            awaitingArenaName = false;
            player.sendMessage(ChatColor.GREEN + "Arena '" + arenaName + "' has been created and saved.");
        }
    }

    // Implement other methods as needed.
}

