package com.sirhiggelbottom.lavaescape.plugin.events;

import com.sirhiggelbottom.lavaescape.plugin.API.WorldeditAPI;
import com.sirhiggelbottom.lavaescape.plugin.Arena.Arena;
import com.sirhiggelbottom.lavaescape.plugin.LavaEscapePlugin;
import com.sirhiggelbottom.lavaescape.plugin.managers.*;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.*;

import static com.sirhiggelbottom.lavaescape.plugin.managers.ArenaManager.GameState.*;

public class GameEvents implements Listener {
    private final LavaEscapePlugin plugin;
    public final Map<UUID, Location[]> playerArenaSelections;
    public final Map<UUID, Location[]> playerLobbySelections;
    private final ArenaManager arenaManager;
    private final GameManager gameManager;
    private final ItemManager itemManager;
    private final ArenaMenu arenaMenu;
    private final WorldeditAPI worldeditAPI;
    private final ConfigManager configManager;

    private boolean globalPvPmode;

    public GameEvents(LavaEscapePlugin plugin, ArenaManager arenaManager, GameManager gameManager, ItemManager itemManager, ArenaMenu arenaMenu, WorldeditAPI worldeditAPI, ConfigManager configManager) {
        this.plugin = plugin;
        this.arenaManager = arenaManager;
        this.gameManager = gameManager;
        this.itemManager = itemManager;
        this.arenaMenu = arenaMenu;
        this.worldeditAPI = worldeditAPI;
        this.configManager = configManager;
        this.playerArenaSelections = new HashMap<>();
        this.playerLobbySelections = new HashMap<>();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.globalPvPmode = arenaManager.getPvpMode();

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
        UUID playerId = player.getUniqueId();
        ItemStack item = player.getItemInHand();


        // if(item == null) return;

        ItemMeta meta = item.getItemMeta();

        if (meta == null) return;

        String itemName = ChatColor.stripColor(meta.getDisplayName());

        // Cooldown logic to stop spam messages
        long lastInteractTime = lastInteract.getOrDefault(player.getUniqueId(), 0L);
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastInteractTime > 500) {
            lastInteract.put(player.getUniqueId(), currentTime);

            if (itemName.equals("ArenaWand")) {
                Action action = event.getAction();


                // Check for left or right-click on a block
                if (action == Action.LEFT_CLICK_BLOCK || action == Action.RIGHT_CLICK_BLOCK) {
                    Location[] arenaSelections = playerArenaSelections.computeIfAbsent(playerId, k -> new Location[2]);

                    if (action == Action.LEFT_CLICK_BLOCK) {
                        arenaSelections[0] = event.getClickedBlock().getLocation();
                        event.getPlayer().sendMessage("First position set at: " + arenaSelections[0].getX() + ", " + arenaSelections[0].getY() + ", " + arenaSelections[0].getZ());
                    } else {
                        arenaSelections[1] = event.getClickedBlock().getLocation();
                        event.getPlayer().sendMessage("Second position set at: " + arenaSelections[1].getX() + ", " + arenaSelections[1].getY() + ", " + arenaSelections[1].getZ());
                    }

                    // Cancel the event to prevent double firing
                    event.setCancelled(true);
                }
            } else if (itemName.equals("LobbyWand")) {
                Action action = event.getAction();

                // Check for left or right-click on a block
                if (action == Action.LEFT_CLICK_BLOCK || action == Action.RIGHT_CLICK_BLOCK) {
                    Location[] lobbySelections = playerLobbySelections.computeIfAbsent(playerId, k -> new Location[2]);

                    if (action == Action.LEFT_CLICK_BLOCK) {
                        lobbySelections[0] = event.getClickedBlock().getLocation();
                        event.getPlayer().sendMessage("First position set");
                    } else {
                        lobbySelections[1] = event.getClickedBlock().getLocation();
                        event.getPlayer().sendMessage("Second position set");
                    }

                    // Cancel the event to prevent double firing
                    event.setCancelled(true);
                }

            }

            List<String> message = new ArrayList<>();

            if(itemName.equals("ArenaWand") && getFirstArenaPosition(playerId) != null && getSecondArenaPosition(playerId) != null){
                player.sendMessage("Arena pos set!");
                arenaManager.writtenArenaLocation1.put(playerId, getFirstArenaPosition(playerId).toString());
                arenaManager.writtenArenaLocation2.put(playerId, getSecondArenaPosition(playerId).toString());
                player.openInventory(itemManager.conformationInv(player, null, arenaMenu.getArenaNamePage(player), "arena"));
            } else if (itemName.equals("LobbyWand") && getFirstLobbyPosition(playerId) != null && getSecondLobbyPosition(playerId) != null) {
                player.sendMessage("Lobby pos set!");
                message.add("First pos: " + getFirstLobbyPosition(playerId).toString());
                message.add("Second pos: " + getSecondLobbyPosition(playerId).toString());
                player.sendMessage(message.toString());
                arenaManager.writtenLobbyLocation1.put(playerId, getFirstLobbyPosition(playerId).toString());
                arenaManager.writtenLobbyLocation2.put(playerId, getSecondLobbyPosition(playerId).toString());
                player.openInventory(itemManager.conformationInv(player, null, arenaMenu.getArenaNamePage(player), "lobby"));
            }
        }

    }

    public Location getFirstArenaPosition (UUID playerId){
        Location[] selections = playerArenaSelections.get(playerId);
        return (selections != null && selections[0] != null) ? selections[0] : null;
    }
    public Location getSecondArenaPosition (UUID playerId){
        Location[] selections = playerArenaSelections.get(playerId);
        return (selections != null && selections[1] != null) ? selections[1] : null;
    }

    public Location getFirstLobbyPosition (UUID playerId){
        Location[] selections = playerLobbySelections.get(playerId);
        return (selections != null && selections[0] != null) ? selections[0] : null;
    }
    public Location getSecondLobbyPosition (UUID playerId){
        Location[] selections = playerLobbySelections.get(playerId);
        return (selections != null && selections[1] != null) ? selections[1] : null;
    }

    @EventHandler
    public void playerWandBlockBreak (BlockBreakEvent event){
        Player player = event.getPlayer();
        ItemStack item = event.getPlayer().getItemInHand();
        //ItemStack item = event.getPlayer().getItemInUse();

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String itemName = ChatColor.stripColor(meta.getDisplayName());

        if(itemName.equals("ArenaWand")){
            event.setCancelled(true);
        } else if(itemName.equals("LobbyWand")){
            event.setCancelled(true);
        }

    }

    /*@EventHandler
    public void checkForPosSelection(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        ItemStack item = player.getItemInHand();

        ItemMeta meta = item.getItemMeta();


        if (meta == null) return;

        // Cooldown logic to stop spam messages
        long lastInteractTime = lastInteract.getOrDefault(player.getUniqueId(), 0L);
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastInteractTime < 500) {
            return;
        }
        lastInteract.put(player.getUniqueId(), currentTime);

        if(getFirstArenaPosition(playerId) != null && getSecondArenaPosition(playerId) != null){
            itemManager.conformationInv(player, null, arenaMenu.getArenaNamePage(player), "arena");
        } else if (getFirstLobbyPosition(playerId) != null && getSecondLobbyPosition(playerId) != null) {
            itemManager.conformationInv(player, null, arenaMenu.getArenaNamePage(player),"lobby");
        }
    }*/


    private final Map<UUID , Long> lastInteract = new HashMap<>();

    @EventHandler
    public void onFatalDamage (EntityDamageEvent event){

        if (event.getEntity() instanceof Player player) {


            if (player.getHealth() - event.getFinalDamage() <= 0) {

                event.setCancelled(true);

                Arena playerArena = arenaManager.findPlayerArena(player);
                if (playerArena != null) {

                    arenaManager.teleportLobby(player, playerArena.getName());
                    arenaManager.healPlayer(player);
                    arenaManager.restorePlayerInventory(player);
                    player.setGameMode(GameMode.ADVENTURE);
                    arenaManager.removePlayerFromArena(playerArena.getName(), player);

                }
            }
        }

    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event){
        Player player = event.getPlayer();
        boolean isAdmin = player.hasPermission("lavaescape.admin");

        Arena playerArena;
        String arenaName;

        Block block = event.getBlockPlaced();
        Material blockMaterial = event.getBlock().getType();
        Chest chest;

        if(!isAdmin && arenaManager.findPlayerArena(player) == null){
            event.setCancelled(true);
            player.sendMessage("You are not allowed to place blocks when you are not in a match!");
            return;
        }

        if(isAdmin){
            if(blockMaterial.equals(Material.CHEST)){
                if(arenaManager.findPlayerArena(player) == null){
                    event.setCancelled(true);
                    player.sendMessage("You can't place chest when you are not in a arena!");
                    return;
                }

                playerArena = arenaManager.findPlayerArena(player);
                arenaName = playerArena.getName();

                ItemStack itemInHand = event.getItemInHand();
                if(itemInHand.hasItemMeta()){
                    ItemMeta meta = itemInHand.getItemMeta();
                    if(meta == null) return;
                    String displayName = ChatColor.stripColor(meta.getDisplayName());


                    if(meta.hasDisplayName() && displayName.equalsIgnoreCase("get loot chest")){
                        Location lootChestLocation = block.getLocation();
                        List<Location> lootChestLoactions = arenaManager.getLootChestLocations(arenaName, player);
                        if(!lootChestLoactions.contains(lootChestLocation)){
                            int amount = arenaManager.getLootChestsAmount(arenaName);
                            arenaManager.setLootChestLocation(arenaName, amount + 1, lootChestLocation, player);
                            chest = (Chest) block.getState();
                            chest.setCustomName(ChatColor.GOLD + "Loot chest");
                            chest.update();
                            player.sendMessage("Lootchest placed at: " + lootChestLocation);
                            player.sendMessage("Name of the inventory in the placed loot chest: " + chest.getCustomName());
                        }
                    }
                }
            }
        } else {
            playerArena = arenaManager.findPlayerArena(player);

            if(playerArena.getGameState() == WAITING || playerArena.getGameState() == STARTING || playerArena.getGameState() == STANDBY){
                player.sendMessage("Game hasn't started yet!");
                event.setCancelled(true);
            }
        }

    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event){
        if(event.getPlayer() instanceof Player player){

            if(arenaManager.findPlayerArena(player) == null){
                return;
            }

            if(ChatColor.stripColor(event.getView().getTitle()).equalsIgnoreCase("loot chest")){ // This might have to be changed into Get loot chest, since that's what it's called in itemManger. The displayname is changed in the onBlockPlace event.
                // Cooldown logic to stop spam messages
                long lastInteractTime = lastInteract.getOrDefault(player.getUniqueId(), 0L);
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastInteractTime > 500) {
                    lastInteract.put(player.getUniqueId(), currentTime);

                    Arena playerArena = arenaManager.findPlayerArena(player);
                    String arenaName = playerArena.getName();
                    if(event.getInventory().getType().equals(InventoryType.CHEST)){
                        Location lootChestLocation = event.getInventory().getLocation();
                        List<Location> lootChestLocations = arenaManager.getLootChestLocations(arenaName, player);

                        if(!lootChestLocations.isEmpty() && lootChestLocations.contains(lootChestLocation)){

                            if(arenaManager.openLootchests.containsKey(player.getUniqueId())){

                                for(Map.Entry<UUID, List<Location>> entry : arenaManager.openLootchests.entrySet()){
                                    List<Location> openedLootChests = entry.getValue();

                                    if(!openedLootChests.contains(lootChestLocation)){
                                        arenaManager.spawnLootItems(event.getInventory(), arenaName);
                                        arenaManager.openLootchests.get(player.getUniqueId()).add(lootChestLocation);

                                    } else {
                                        player.sendMessage("You have already opened this loot chest!");
                                    }
                                }
                            } else {
                                arenaManager.spawnLootItems(event.getInventory(), arenaName);
                                List<Location> newOpenedLootChestList = new ArrayList<>();
                                newOpenedLootChestList.add(lootChestLocation);
                                arenaManager.openLootchests.put(player.getUniqueId(), newOpenedLootChestList);

                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerDamage (EntityDamageByEntityEvent event){

        UUID playerId = event.getDamager().getUniqueId();
        Player player = event.getDamager().getServer().getPlayer(playerId);
        Arena playerArena = arenaManager.findPlayerArena(player);

        if (playerArena != null) {
            event.setCancelled(!playerArena.getGameState().equals(ArenaManager.GameState.LAVA) && !playerArena.getGameState().equals(ArenaManager.GameState.DEATHMATCH));
        } else if(!globalPvPmode){
            event.setCancelled(true); // @ToDo Fix: Players can hurt each other when they aren't in a arena, and globalPVP doesn't work. Edit: Think it's fixed
        }

    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        Player player = event.getPlayer();
        boolean isAdmin = player.hasPermission("lavaescape.admin");

        Block block = event.getBlock();
        Material material = block.getType();
        Arena playerArena;
        String arenaName;

        Location blockLocation = block.getLocation();
        List<Location> lootChestLocations;


        if(isAdmin){
            if(material.equals(Material.CHEST)){
                lootChestLocations = arenaManager.findLootChestLocations(player);
                if(lootChestLocations.contains(blockLocation) && arenaManager.findPlayerArena(player) == null){
                    event.setCancelled(true);
                    player.sendMessage("Error, you need to be in LootChest placement mode in order to remove loot chests.");
                } else if(lootChestLocations.contains(blockLocation) && arenaManager.findPlayerArena(player) != null){
                    playerArena = arenaManager.findPlayerArena(player);
                    arenaName = playerArena.getName();

                    if(arenaManager.getGameStage(arenaName).equals("STANDBY")){
                        arenaManager.deleteLootChestlocation(arenaName, blockLocation, player);
                    } else{
                        event.setCancelled(true);
                    }
                }
            }

        } else {

            if(arenaManager.findPlayerArena(player) == null){
                event.setCancelled(true);
                return;
            }

            playerArena = arenaManager.findPlayerArena(player);
            lootChestLocations = arenaManager.findLootChestLocations(player);
            if(lootChestLocations.contains(blockLocation)){
                event.setCancelled(true);
                player.sendMessage("You're not allowed to break Loot chests!");
                return;
            }

            List<ItemStack> getBlacklistedBlocks = arenaManager.getBlacklistedBlocks(playerArena.getName());
            switch (playerArena.getGameState()){
                case LAVA, DEATHMATCH, GRACE:
                    for(ItemStack blacklistedBlock : getBlacklistedBlocks){
                        if(blacklistedBlock.getType() == material){
                            event.setCancelled(true);
                            player.sendMessage("This is a blacklisted block!");
                            break;
                        }
                    }
                    break;
                case WAITING, STARTING, STANDBY:
                    event.setCancelled(true);
                    player.sendMessage("The game hasn't started yet!");
            }

        }

        /*playerArena = arenaManager.findPlayerArena(player);

        if(playerArena == null){
            player.sendMessage("Couldn't find the arena");
            event.setCancelled(true);
        } else {

            if(block.hasMetadata("Lootchest")){
                event.setCancelled(true);
                player.sendMessage("You can't break loot chests!");
            }
            List<ItemStack> getBlacklistedBlocks = arenaManager.getBlacklistedBlocks(playerArena.getName());
            switch (playerArena.getGameState()){
                case LAVA, DEATHMATCH, GRACE:
                    for(ItemStack blacklistedBlock : getBlacklistedBlocks){
                        if(blacklistedBlock.getType() == material){
                            event.setCancelled(true);
                            player.sendMessage("This is a blacklisted block!");
                            break;
                        }
                    }
                    break;
                case WAITING, STARTING, STANDBY:
                    event.setCancelled(true);
                    player.sendMessage("The game hasn't started yet!");
            }
        }*/
    }

    @EventHandler
    public void onPlayerFallDamage (EntityDamageEvent event){

        if(event.getEntity() instanceof Player player){
            Arena playerArena = arenaManager.findPlayerArena(player);

            if (playerArena != null) {
                event.setCancelled(!playerArena.getGameState().equals(ArenaManager.GameState.LAVA) && !playerArena.getGameState().equals(ArenaManager.GameState.DEATHMATCH));
            }

        }

    }
    // Event is meant to check if a player exits the menu system. If so it should empty the relevant hashmaps in itemManager.
    /*@EventHandler
    public void onInventoryEscape(InventoryCloseEvent event){

        *//*if(arenaMenu.autoClosed){
            arenaMenu.autoClosed = false;
            return;
        }*//*

        if(event.getPlayer() instanceof Player player){



            long lastInteractTime = lastInteract.getOrDefault(player.getUniqueId(), 0L);
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastInteractTime < 500) {
                return;
            }

            if(!arenaMenu.autoClosed){
                InventoryView viewClosedInventory = event.getView();
                String inventoryName = ChatColor.stripColor(viewClosedInventory.getTitle());

                player.sendMessage("This is the name of the closed inventory: " + inventoryName);
            } else arenaMenu.autoClosed = false;

        }

    }*/

    @EventHandler
    public void playerInteract(PlayerInteractEvent event){
        Player player = event.getPlayer();
        switch (event.getAction()){
            case RIGHT_CLICK_AIR:
            case RIGHT_CLICK_BLOCK:


                ItemStack itemInHand = player.getInventory().getItemInHand();

                if(itemInHand.getType().equals(Material.AIR)){
                    return;
                } else if(!itemInHand.getType().equals(Material.BARRIER)){
                    return;
                }


                ItemMeta meta = itemInHand.getItemMeta();

                if(meta == null) {
                    player.sendMessage("Error, couldn't find item meta");
                    return;
                }

                String displayName = ChatColor.stripColor(meta.getDisplayName());
                String searchedDisplayName;
                ItemMeta searchedItemMeta;

                if(displayName.equalsIgnoreCase("stop lootchest placement")){
                    event.setCancelled(true);
                    for(ItemStack item : player.getInventory().getContents()){
                        if(item != null){
                            searchedItemMeta = item.getItemMeta();
                            if(searchedItemMeta != null){
                                searchedDisplayName = ChatColor.stripColor(searchedItemMeta.getDisplayName());
                                if(searchedDisplayName.equalsIgnoreCase("get loot chest")){
                                    player.getInventory().remove(item);
                                }
                            }
                        }
                    }
                    player.getInventory().remove(itemInHand);
                    arenaManager.stopLootChestPlacement(arenaManager.findPlayerArena(player).getName(), player);
                }
        }
    }

    @EventHandler
    public void inventoryInteract (InventoryClickEvent event){


        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        UUID playerId = player.getUniqueId();
        ItemStack clickedItem = event.getCurrentItem();
        String inventoryTitle = ChatColor.stripColor(event.getView().getTitle());


        long lastInteractTime = lastInteract.getOrDefault(player.getUniqueId(), 0L);
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastInteractTime < 50) {
            return;
        }
        lastInteract.put(player.getUniqueId(), currentTime);

        if (clickedItem == null) {
            return;
        }
        String itemName = clickedItem.toString();
        ItemMeta meta = clickedItem.getItemMeta();

        if (meta == null) {
            return;
        }

        String displayName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());

        if (itemManager.anvilGUIUsers.contains(playerId)) {
            return;
        }

        String arenaName;
        org.bukkit.World world = player.getWorld();
        String input;
        String mode;
        Arena arena;

        if(inventoryTitle.equalsIgnoreCase("starter items")){
            event.setCancelled(true);
            String cleanedItem = arenaMenu.parseDeleteItem(itemName);
            arenaMenu.deleteStartingItem(player, cleanedItem);
        } else if(inventoryTitle.equalsIgnoreCase("blacklisted blocks")){
            event.setCancelled(true);
            String cleanedItem = arenaMenu.parseDeleteItem(itemName);
            arenaMenu.deleteBlacklistedBlock(player, cleanedItem);
        }

        String cleanedItem;

        switch (inventoryTitle.toLowerCase()){

            case "starter items":
                event.setCancelled(true);
                cleanedItem = arenaMenu.parseDeleteItem(itemName);
                arenaMenu.deleteStartingItem(player, cleanedItem);
                break;
            case "blacklisted blocks":
                event.setCancelled(true);
                cleanedItem = arenaMenu.parseDeleteItem(itemName);
                arenaMenu.deleteBlacklistedBlock(player, cleanedItem);
                break;
            case "loot items":
                if(!displayName.equalsIgnoreCase("get loot chest")){
                    event.setCancelled(true);
                    cleanedItem = arenaMenu.parseDeleteItem(itemName);
                    arenaMenu.deleteLootItem(player, cleanedItem);
                } else {
                    if(!inventoryTitle.equalsIgnoreCase("loot items")){ // This is to stop duplicating the loot chest whenever the admin clicks the loot chest in their inventory
                        return;
                    }
                    event.setCancelled(true);
                    arenaMenu.autoClosed = true;
                    arenaName = arenaMenu.getArenaNamePage(player);
                    arenaManager.addPlayerToArena(arenaName, player);
                    player.getInventory().addItem(itemManager.getLootchestItem());
                    player.getInventory().addItem(itemManager.getExitLootplacementModeItem());
                    player.setGameMode(GameMode.CREATIVE);
                    arenaMenu.closeInventory(player);
                }
        }

        switch (displayName.toLowerCase()) {
            case "switch global pvp mode":
                event.setCancelled(true);
                globalPvPmode = !globalPvPmode;
                arenaManager.changeCurrentPvPMode();
                arenaManager.setPvpMode(globalPvPmode);
                player.openInventory(arenaMenu.mainMenu(player));
                break;
            case "join arena":
                event.setCancelled(true);
                arenaName = arenaMenu.getArenaNamePage(player);
                arenaManager.addPlayerToArena(arenaName, player);
                arenaManager.teleportLobby(player, arenaName);
                gameManager.isGameReady(arenaName);
                arenaMenu.autoClosed = true;
                arenaMenu.closeInventory(player);
                break;
            case "normal mode":
                event.setCancelled(true);
                mode = "server";
                arenaName = arenaMenu.getArenaNamePage(player);
                arenaManager.changeGameMode(arenaName, mode);
                arenaMenu.reloadPage(player, "config");
                break;
            case "update arena":
                event.setCancelled(true);
                arenaName = arenaMenu.getArenaNamePage(player);
                worldeditAPI.saveArenaRegionAsSchematic(player, arenaName, true);
                worldeditAPI.saveLobbyRegionAsSchematic(player, arenaName, true);
                // worldeditAPI.findLootChests(arenaName);
            case "competition mode":
                event.setCancelled(true);
                mode = "competitive";
                arenaName = arenaMenu.getArenaNamePage(player);
                arenaManager.changeGameMode(arenaName, mode);
                arenaMenu.reloadPage(player, "config");
                break;
            case "start match":
                event.setCancelled(true);
                arenaName = arenaMenu.getArenaNamePage(player);
                gameManager.adminStart(player, arenaName);
                break;
            case "restart match":
                event.setCancelled(true);
                arenaName = arenaMenu.getArenaNamePage(player);
                gameManager.adminRestartGame(player, arenaName);
                break;
            case "confirm arena placement":
                event.setCancelled(true);
                arenaMenu.autoClosed = true;
                ItemStack arenaWand = itemManager.getArenaWandItem(player);
                player.getInventory().remove(arenaWand);
                arenaName = arenaMenu.getArenaNamePage(player);
                arena = arenaManager.getArena(arenaName);
                arena.setArenaLocations(getFirstArenaPosition(playerId) , getSecondArenaPosition(playerId));
                arenaManager.saveTheArena(arena);
                worldeditAPI.saveArenaRegionAsSchematic(player,arenaName, false);
                arenaManager.writtenArenaLocation1.remove(playerId, arenaManager.writtenArenaLocation1.get(playerId));
                arenaManager.writtenArenaLocation2.remove(playerId, arenaManager.writtenArenaLocation2.get(playerId));
                //player.sendMessage(arenaName + " arena area set");
                arenaMenu.closeInventory(player);
                break;
            case "confirm lobby placement":
                event.setCancelled(true);
                arenaMenu.autoClosed = true;
                ItemStack lobbyWand = itemManager.getLobbyWandItem(player);
                player.getInventory().remove(lobbyWand);
                arenaName = arenaMenu.getArenaNamePage(player);
                arena = arenaManager.getArena(arenaName);
                arena.setLobbyLocations(getFirstLobbyPosition(playerId) , getSecondLobbyPosition(playerId));
                arenaManager.saveTheLobby(arena);
                worldeditAPI.saveLobbyRegionAsSchematic(player, arenaName, false);
                arenaManager.writtenLobbyLocation1.remove(playerId, arenaManager.writtenLobbyLocation1.get(playerId));
                arenaManager.writtenLobbyLocation2.remove(playerId, arenaManager.writtenLobbyLocation2.get(playerId));
                //player.sendMessage(arenaName + " lobby area set");
                arenaMenu.closeInventory(player);
                break;
            case "set arena area":
                event.setCancelled(true); // Denne var ikke her tidligere
                arenaMenu.autoClosed = true;
                event.setCancelled(true);
                player.getInventory().addItem(itemManager.getArenaWandItem(player));
                player.sendMessage("You have received the Arena wand.");
                arenaMenu.closeInventory(player);
                break;
            case "set lobby area":
                event.setCancelled(true); // Denne var ikke her tidligere
                arenaMenu.autoClosed = true;
                player.getInventory().addItem(itemManager.getLobbyWandItem(player));
                player.sendMessage("You have received the Lobby wand.");
                arenaMenu.closeInventory(player);
                break;
            case "set minimum players":
                event.setCancelled(true);
                arenaMenu.autoClosed = true;
                arenaName = arenaMenu.getArenaNamePage(player);
                arenaMenu.setSubPage(player, "MIN_PLAYERS");
                itemManager.setMinPlayers(player);
                break;
            case "set maximum players":
                event.setCancelled(true);
                arenaMenu.autoClosed = true;
                arenaName = arenaMenu.getArenaNamePage(player);
                arenaMenu.setSubPage(player, "MAX_PLAYERS");
                itemManager.setMaxPlayers(player);
                break;
            case "set min y-level":
                event.setCancelled(true);
                arenaMenu.autoClosed = true;
                arenaName = arenaMenu.getArenaNamePage(player);
                arenaMenu.setSubPage(player, "MIN_Y");
                itemManager.setMinY(player);
                break;
            case "set max y-level":
                event.setCancelled(true);
                arenaMenu.autoClosed = true;
                arenaName = arenaMenu.getArenaNamePage(player);
                arenaMenu.setSubPage(player, "MAX_Y");
                itemManager.setMaxY(player);
                break;
            case "generate spawns":
                event.setCancelled(true);
                arenaName = arenaMenu.getArenaNamePage(player);
                player.sendMessage("Trying to start finding spawnpoints for: " + arenaName);
                FileConfiguration config = configManager.getArenaConfig();
                File schematicDir = new File(plugin.getDataFolder().getParentFile(), "LavaEscape/schematics/" + arenaName);
                File schematicFile = new File(schematicDir, arenaName + ".schem");
                if(arenaManager.getArena(arenaName) == null){
                    player.sendMessage("Arena doesn't exist");
                    return;
                }

                if(!arenaManager.checkYlevels(arenaName)){
                    player.sendMessage("Y-levels not set");
                    return;
                }

                if (!schematicFile.exists()) {
                    player.sendMessage("schematic doesn't exist");
                    return;
                }

                String finalArenaName = arenaName;
                arenaManager.tryLogging(()-> arenaManager.setSpawnPoints(finalArenaName, player),
                        "Error when trying to create spawnpoints");
                break;
            case "set rise time":
                event.setCancelled(true);
                arenaMenu.autoClosed = true;
                arenaName = arenaMenu.getArenaNamePage(player);
                arenaMenu.setSubPage(player, "RISE_TIME");
                itemManager.setRiseTime(player);
                break;
            case "set grace time":
                event.setCancelled(true);
                arenaMenu.autoClosed = true;
                arenaName = arenaMenu.getArenaNamePage(player);
                arenaMenu.setSubPage(player, "GRACE_TIME");
                itemManager.setGraceTime(player);
                break;
            case "reset arena":
                event.setCancelled(true);
                arenaMenu.autoClosed = true;
                arenaName = arenaMenu.getArenaNamePage(player);
                worldeditAPI.placeSchematic(player, arenaName);
                break;
            case "delete arena":
                event.setCancelled(true);
                arenaMenu.autoClosed = true;
                String subpage = displayName.replace(" ", "_");
                arenaMenu.openSubPage(player, subpage);
                break;
            case "yes i want to delete":
                event.setCancelled(true);
                arenaMenu.autoClosed = true;
                String rawData = (ChatColor.stripColor(Objects.requireNonNull(clickedItem.getItemMeta().getLore()).toString()));
                String[] splitRawData = rawData.split(": ");
                String[] fixedRawData = splitRawData[1].split("]");
                arenaName = fixedRawData[0];
                arenaManager.deleteArena(player, arenaName);

                player.sendMessage("Arena: '" + arenaName + "' has been deleted.");
                arenaMenu.closeInventory(player);
                break;
            case "no, what was i thinking?":
                event.setCancelled(true); // Denne var ikke her tidligere
                arenaMenu.autoClosed = true;
                arenaMenu.openSubPage(player, arenaMenu.getPreviousSubPage(player).toString());
                break;
            case "set starter items":
                event.setCancelled(true); // Denne var ikke her tidligere
                arenaMenu.autoClosed = true;
                arenaMenu.openSubPage(player, "STARTER_ITEMS");
                break;
            case "add new starter item":
                event.setCancelled(true);
                arenaMenu.autoClosed = true;
                itemManager.setStarterItems(player);
                break;
            case "set blacklisted blocks":
                event.setCancelled(true);
                arenaMenu.autoClosed = true;
                arenaMenu.openSubPage(player, "BLACKLISTED_BLOCKS");
                break;
            case "add new blacklisted block":
                event.setCancelled(true);
                arenaMenu.autoClosed = true;
                itemManager.setBlacklistedBlocks(player);
                break;
            case "loot chest config":
                event.setCancelled(true);
                arenaMenu.autoClosed = true;
                arenaMenu.openSubPage(player, "LOOT_ITEMS");
                break;
            case "add new loot item":
                event.setCancelled(true);
                itemManager.setLootItems(player);
                break;
            case "confirm name":
                event.setCancelled(true);
                arenaName = itemManager.writtenArenaName.get(playerId);
                if(arenaName.equalsIgnoreCase("null")){
                    break;
                }
                arenaMenu.autoClosed = true;
                arenaManager.createArena(arenaName, world);
                itemManager.clearMap(player);
                player.sendMessage("Created arena: " + arenaName);
                arenaMenu.openArenaPage(player, arenaManager.getArena(arenaName));
                break;
            case "confirm miny":
                event.setCancelled(true);
                arenaName = arenaMenu.getArenaNamePage(player);
                if (itemManager.parseValueToInt(player) != -1) {
                    arenaManager.setMinY(arenaName, itemManager.parseValueToInt(player), player);
                }
                itemManager.clearMap(player);
                arenaMenu.autoClosed = true;
                arenaMenu.goBack(player);
                break;
            case "confirm maxy":
                event.setCancelled(true);
                arenaName = arenaMenu.getArenaNamePage(player);
                if (itemManager.parseValueToInt(player) != -1) {
                    arenaManager.setMaxY(arenaName, itemManager.parseValueToInt(player), player);
                }
                itemManager.clearMap(player);
                arenaMenu.autoClosed = true;
                arenaMenu.goBack(player);
                break;
            case "confirm minimum players":
                event.setCancelled(true);
                arenaName = arenaMenu.getArenaNamePage(player);
                if (itemManager.parseValueToInt(player) != -1) {
                    arenaManager.setMinPlayers(arenaName, itemManager.parseValueToInt(player));
                }
                itemManager.clearMap(player);
                arenaMenu.autoClosed = true;
                arenaMenu.goBack(player);
                break;
            case "confirm maximum players":
                event.setCancelled(true);
                arenaName = arenaMenu.getArenaNamePage(player);
                if (itemManager.parseValueToInt(player) != -1) {
                    arenaManager.setMaxPlayers(arenaName, itemManager.parseValueToInt(player));
                }
                itemManager.clearMap(player);
                arenaMenu.autoClosed = true;
                arenaMenu.goBack(player);
                break;
            case "confirm rise time":
                event.setCancelled(true);
                arenaName = arenaMenu.getArenaNamePage(player);
                if (itemManager.parseValueToInt(player) != -1) {
                    arenaManager.setRiseTime(arenaName, itemManager.parseValueToInt(player));
                }
                itemManager.clearMap(player);
                arenaMenu.autoClosed = true;
                arenaMenu.goBack(player);
                break;
            case "confirm grace time":
                event.setCancelled(true);
                arenaName = arenaMenu.getArenaNamePage(player);
                if (itemManager.parseValueToInt(player) != -1) {
                    arenaManager.setGracePeriod(arenaName, itemManager.parseValueToInt(player));
                }
                itemManager.clearMap(player);
                arenaMenu.autoClosed = true;
                arenaMenu.goBack(player);
                break;
            case "confirm starting item":
                event.setCancelled(true);
                arenaName = arenaMenu.getArenaNamePage(player);
                input = itemManager.writtenStarterItemsValue.get(playerId);
                if (itemManager.parseItemAmount(input) != -1) {
                    arenaManager.setStarterItems(arenaName, itemManager.parseItem(input), itemManager.parseItemAmount(input), player);
                }
                itemManager.clearMap(player);
                arenaMenu.autoClosed = true;
                arenaMenu.goBack(player);
                break;
            case "confirm loot item":
                event.setCancelled(true);
                arenaName = arenaMenu.getArenaNamePage(player);
                input = itemManager.writtenLootItemsValue.get(playerId);
                if (itemManager.parseItemAmount(input) != -1) {
                    arenaManager.setLootItem(arenaName, itemManager.parseItem(input), itemManager.parseItemAmount(input), player);
                }
                itemManager.clearMap(player);
                arenaMenu.autoClosed = true;
                arenaMenu.goBack(player);
                break;
            case "delete item":
                event.setCancelled(true);
                arenaMenu.autoClosed = true;
                arenaName = arenaMenu.getArenaNamePage(player);
                String item = arenaMenu.deleteItemMap.get(playerId);
                arenaManager.deleteStarterItem(arenaName, item, player);
                arenaMenu.goBack(player);
                break;
            case "delete loot item":
                event.setCancelled(true);
                arenaMenu.autoClosed = true;
                arenaName = arenaMenu.getArenaNamePage(player);
                String lootItem = arenaMenu.deleteItemMap.get(playerId);
                arenaManager.deleteLootItem(arenaName, lootItem, player);
                arenaMenu.goBack(player);
                break;
            case "delete block":
                event.setCancelled(true);
                arenaMenu.autoClosed = true;
                arenaName = arenaMenu.getArenaNamePage(player);
                String block = arenaMenu.deleteBlockMap.get(playerId);
                arenaManager.deleteBlacklistedItem(arenaName, block, player);
                arenaMenu.goBack(player);
                break;
            case "confirm blacklisted block":
                event.setCancelled(true);
                arenaMenu.autoClosed = true;
                arenaName = arenaMenu.getArenaNamePage(player);
                input = itemManager.writtenBlacklistetBlocksValue.get(playerId);
                if (itemManager.parseBlacklistedBlock(input) != null) {
                    arenaManager.setBlacklistedBlocks(arenaName, itemManager.parseBlacklistedBlock(input), player);
                }
                itemManager.clearMap(player);
                arenaMenu.goBack(player);
                break;
            case "create new arena":
                itemManager.createNewArena(player);
                break;
            case "try again":
                event.setCancelled(true);
                switch (itemManager.getWrittenMapEnum(player)) {
                    case WRITTENARENANAME:
                        arenaMenu.autoClosed = true;
                        itemManager.clearMap(player);
                        itemManager.createNewArena(player);
                        break;
                    case WRITTENMINYVALUE:
                        arenaMenu.autoClosed = true;
                        itemManager.clearMap(player);
                        itemManager.setMinY(player);
                        break;
                    case WRITTENMAXYVALUE:
                        arenaMenu.autoClosed = true;
                        itemManager.clearMap(player);
                        itemManager.setMaxY(player);
                        break;
                    case WRITTENMINPLAYERSVALUE:
                        arenaMenu.autoClosed = true;
                        itemManager.clearMap(player);
                        itemManager.setMinPlayers(player);
                        break;
                    case WRITTENMAXPLAYERSVALUE:
                        arenaMenu.autoClosed = true;
                        itemManager.clearMap(player);
                        itemManager.setMaxPlayers(player);
                        break;
                    case WRITTENRISETIMEVALUE:
                        arenaMenu.autoClosed = true;
                        itemManager.clearMap(player);
                        itemManager.setRiseTime(player);
                        break;
                    case WRITTENGRACETIMEVALUE:
                        arenaMenu.autoClosed = true;
                        itemManager.clearMap(player);
                        itemManager.setGraceTime(player);
                        break;
                    case WRITTENSTARTERITEMSVALUE:
                        arenaMenu.autoClosed = true;
                        itemManager.clearMap(player);
                        itemManager.setStarterItems(player);
                        break;
                    case WRITTENLOOTITEMSVALUE:
                        arenaMenu.autoClosed = true;
                        itemManager.clearMap(player);
                        itemManager.setLootItems(player);
                        break;
                    case WRITTENBLACKLISTEDBLOCKSVALUE:
                        arenaMenu.autoClosed = true;
                        itemManager.clearMap(player);
                        itemManager.setBlacklistedBlocks(player);
                        break;
                }

                break;
            /*case "cancel": // Closes the menu.
                event.setCancelled(true);
                arenaMenu.autoClosed = true;
                arenaMenu.closeInventory(player);
                break;*/
            case "arenas": // Sends player to the arenas menu
                event.setCancelled(true);
                arenaMenu.autoClosed = true;
                arenaMenu.createArenaPages(player, 1);
                break;
            case "exit", "cancel": // Closes the menu, replace with a InventoryCloseEvent.
                event.setCancelled(true);
                arenaMenu.autoClosed = true;
                arenaMenu.closeInventory(player);
                break;
            case "go back.": // Sends the player to the previous menu.
                event.setCancelled(true);
                arenaMenu.autoClosed = true;
                arenaMenu.goBack(player);
                break;
            case "config": // Sends the player to the config menu.
                event.setCancelled(true);
                arenaMenu.autoClosed = true;
                arenaMenu.openSubPage(player, displayName);
                break;
            case "border":
                event.setCancelled(true);
            default:
                if (arenaManager.getArenas().contains(displayName)) {
                    event.setCancelled(true);
                    arenaMenu.autoClosed = true;
                    arenaMenu.closeListPage(player);
                    arenaMenu.openArenaPage(player, arenaManager.getArena(displayName));
                }
        }
    }

}

