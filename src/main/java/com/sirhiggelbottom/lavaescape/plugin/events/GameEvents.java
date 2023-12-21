package com.sirhiggelbottom.lavaescape.plugin.events;

import com.sirhiggelbottom.lavaescape.plugin.Arena.Arena;
import com.sirhiggelbottom.lavaescape.plugin.LavaEscapePlugin;
import com.sirhiggelbottom.lavaescape.plugin.managers.ArenaManager;
import com.sirhiggelbottom.lavaescape.plugin.managers.GameManager;
import com.sirhiggelbottom.lavaescape.plugin.managers.MenuManager;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class GameEvents implements Listener {
    private final LavaEscapePlugin plugin;
    private final Map<UUID, Location[]> playerSelections;
    private final Arena arena;
    private final ArenaManager arenaManager;
    private final GameManager gameManager;
    private final MenuManager menuManager;
    private Map<UUID, Integer> playerPage;
    private Map<UUID, Integer> waitingForInput;

    public GameEvents(LavaEscapePlugin plugin, Arena arena, ArenaManager arenaManager, GameManager gameManager, MenuManager menuManager) {
        this.plugin = plugin;
        this.arenaManager = arenaManager;
        this.gameManager = gameManager;
        this.menuManager = menuManager;
        this.playerSelections = new HashMap<>();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.arena = arena;
        playerPage = new HashMap<>();
        waitingForInput = new HashMap<>();
    }

    /*
     waitingForInput
     0 = Not waiting for input.
     1 = Waiting for input for arenaName.
     2 = Waiting for input for minimum players.
     3 = Waiting for input for maximum players.
     4 = Waiting for input for Min Y.
     5 = Waiting for input for Max Y.
     6 = Waiting for input for Rise time.
     7 = Waiting for input for Grace time.
     8 = Waiting for input for Starter items.
     9 = Waiting for input for Blacklisted blocks.
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item != null && item.getType() == Material.STICK && item.getItemMeta().getDisplayName().equals("Wand")) {
            Action action = event.getAction();

            // Cooldown logic to stop spam messages
            long lastInteractTime = lastInteract.getOrDefault(player.getUniqueId(), 0L);
            long currentTime = System.currentTimeMillis();
            if(currentTime - lastInteractTime < 500){
                return;
            }
            lastInteract.put(player.getUniqueId(), currentTime);

            // Check for left or right-click on a block
            if (action == Action.LEFT_CLICK_BLOCK || action == Action.RIGHT_CLICK_BLOCK) {
                UUID playerId = event.getPlayer().getUniqueId();
                Location[] selections = playerSelections.computeIfAbsent(playerId, k -> new Location[2]);

                if (action == Action.LEFT_CLICK_BLOCK) {
                    selections[0] = event.getClickedBlock().getLocation();
                    event.getPlayer().sendMessage("First position set.");
                } else if (action == Action.RIGHT_CLICK_BLOCK) {
                    selections[1] = event.getClickedBlock().getLocation();
                    event.getPlayer().sendMessage("Second position set.");
                }

                // Cancel the event to prevent double firing
                event.setCancelled(true);
            }
        }
    }
    @EventHandler
    public void playerWandBlockBreak(BlockBreakEvent event){
        ItemStack item = event.getPlayer().getItemInHand();
        //ItemStack item = event.getPlayer().getItemInUse();
        Block block = event.getBlock();

        if (block == null) return;

        event.setCancelled(item != null && item.getType() == Material.STICK && item.getItemMeta().getDisplayName().equals("Wand"));
        //event.setCancelled(item != null && item.getType() == Material.STICK && Objects.requireNonNull(item.getItemMeta()).getDisplayName().equals("Wand"));

    }
    public Location getFirstPosition(UUID playerId) {
        Location[] selections = playerSelections.get(playerId);
        return (selections != null && selections[0] != null) ? selections[0] : null;
    }
    public Location getSecondPosition(UUID playerId) {
        Location[] selections = playerSelections.get(playerId);
        return (selections != null && selections[1] != null) ? selections[1] : null;
    }
    private final Map<UUID , Long> lastInteract = new HashMap<>();
    @EventHandler
    public void onFatalDamage(EntityDamageEvent event){

        if(event.getEntity() instanceof Player){
            Player player = (Player) event.getEntity();

            if(player.getHealth() - event.getFinalDamage() <= 0){

                event.setCancelled(true);

                Arena playerArena = arenaManager.findPlayerArena(player);
                if(playerArena != null){

                    arenaManager.teleportLobby(player, playerArena.getName());
                    arenaManager.healPlayer(player);
                    arenaManager.restorePlayerInventory(player);
                    player.setGameMode(GameMode.ADVENTURE);
                    arenaManager.removePlayerFromArena(playerArena.getName(),player);

                }
            }
        }

    }
    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {

        UUID playerId = event.getDamager().getUniqueId();
        Player player = event.getDamager().getServer().getPlayer(playerId);
        Arena playerArena = arenaManager.findPlayerArena(player);

        if(playerArena != null){
            event.setCancelled(!playerArena.getGameState().equals(ArenaManager.GameState.LAVA) && !playerArena.getGameState().equals(ArenaManager.GameState.DEATHMATCH));
        } else event.setCancelled(true);

    }
    @EventHandler
    public void onPlayerFallDamage(EntityDamageEvent event){
    }
    @EventHandler void onPlayerChat(AsyncPlayerChatEvent event){
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if(waitingForInput.getOrDefault(playerId, 0) != 0){
            int inputStatus = waitingForInput.get(playerId);
            switch (inputStatus){
                case 1:
                    String input = event.getMessage();

            }
        }


    }



    @EventHandler void onInventoryInteract(InventoryClickEvent event){
        if(event.getWhoClicked() instanceof Player player){
            //Player player = (Player) event.getWhoClicked();
            ItemStack clickedItem = event.getCurrentItem();

            if(clickedItem != null && clickedItem.hasItemMeta()){
                String displayName = clickedItem.getItemMeta().getDisplayName();
                UUID playerId = player.getUniqueId();
                int currentPage = playerPage.getOrDefault(playerId,1);

                switch (displayName){
                    case "Arenas": // Sends player to the arenas menu
                        menuManager.createArenaPage(player, currentPage);
                    case "Create New Arena": // Prompts the player to input an arenaName for new arena.
                        //@Todo: Create logic equal to /Lava create <arenaName>, prompts player to input arenaName.
                        waitingForInput.put(playerId, 1);
                    case "Exit": // Closes the menu
                        player.closeInventory();
                    case "Next Page": // Sends player to next page in Arenas menu
                        menuManager.createArenaPage(player, currentPage + 1);
                    case "Previous page": // Sends player to previous page in Arenas menu
                        menuManager.createArenaPage(player, currentPage - 1);
                    case "Join": // Makes the player join the selected arena.
                        //@Todo: Create logic equal to /Lava join <arenaName>.
                    case "Back": // Sends the player to the previous menu.
                        //@Todo: Create logic that sends player back to previous menu.
                    case "Config": // Sends the player to the config menu.
                        //@Todo: Create logic that sends the player to the config menu, also create the config menu.
                    case "Normal mode": // Sets the gameMode to Normal or Server mode.
                        //@Todo: Create logic that sets the gameMode to normal mode for the selected arena.
                    case "Competition mode": // Sets the gameMode to Competition mode.
                        //@Todo: Create logic that sets the gameMode to Competition for the selected arena.
                    case "Set arena": // Gives the player a wand to set the area for the Arena, when the player has set 2 pos, the positions are then saved.
                        //@Todo: Create logic that gives the player a wand, and checks if the player has set 2 positions for the arena.
                    case "Set lobby": // Gives the player a wand to set the area for the Lobby, when the player has set 2 pos, the positions are then saved.
                        //@Todo: Create logic that gives the player a wand, and checks if the player has set 2 positions for the lobby.
                    case "Confirm": // Confirms the selection of positions for creating either arena or lobby.
                        //@Todo: Create logic that saves the pos selection to the corresponding area.
                    case "Cancel": // Closes the menu.
                        player.closeInventory();
                    case "Try again": // Lets the player try again.
                        //@Todo: Create logic that lets the player select positions again.
                    case "Min players": // Sets the minimum amount of players required for the match to start for the specific arena, when in Normal mode.
                        //@Todo: Create logic that sets the minimum amount of players required for the match to start for the specific arena, when in Normal mode.
                    case "Max players": // Sets the maximum amount players allowed in the specific arena.
                        //@Todo: Create logic that sets the maximum amount of players allowed in the specific arena.
                    case "Min Y": // Sets the lowest y-level for where the spawnpoints can be generated.
                        //@Todo: Create logic that sets the minimum Y-level for creating spawnpoints.
                    case "Max Y": // Sets the highest y-level for where the spawnpoints can be generated.
                        //@Todo: Create logic that sets the maximum Y-level for creating spawnpoints.
                    case "Generate spawns": // Creates spawnpoints based on the y-levels and the area that has been set for the arena.
                        //@Todo: Create logic that generates spawnpoints.
                    case "Rise time": // Sets the time between each time the y-level of the lava increases.
                        //@Todo: Create logic that sets the Lava delay.
                    case "Grace time": // Sets the time from when the match starts to when the lava starts rising.
                        //@Todo: Create logic that sets the Grace time.
                    case "Reset arena": // Resets the arena
                        //@Todo: Create logic that resets the arena.
                    case "Delete arena": // Opens up a new menu that asks if the player is sure that they want to delete the selected arena.
                        //@Todo: Create logic that sends the player to the deleteMenu.
                    case "Yes": // Confirms that the player wants to delete the selected arena.
                        //@Todo: Create logic that deletes the selected arena.
                    case "No": // Stops the player from deleting the selected arena, this sends the player back to the config menu for the selected arena.
                        //@Todo: Create logic that sends the player back to the config menu for the selected arena.
                    case "Starter items": // Sends the player to the menu for starting-items menu
                        //@Todo: Create logic that sends the player to the correct menu, and create the menu. It should display all the starting items and the amount.
                    case "Blacklisted blocks": // Sends the player to the menu for blacklisted blocks.
                        //@Todo: Create logic that sends the player to the correct menu, and create the menu. It should display all the blacklisted blocks.
                    default:
                        player.sendMessage("Error, closing the menu");
                        player.closeInventory();
                        break;
                }

            }
        }
    }
}

