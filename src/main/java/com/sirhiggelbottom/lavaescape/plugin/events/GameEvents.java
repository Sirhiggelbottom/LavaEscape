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
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.*;

public class GameEvents implements Listener {
    private final LavaEscapePlugin plugin;
    public final Map<UUID, Location[]> playerArenaSelections;
    public final Map<UUID, Location[]> playerLobbySelections;
    private final Arena arena;
    private final ArenaManager arenaManager;
    private final GameManager gameManager;
    private final MenuManager menuManager;
    private final ArenaMenu arenaMenu;
    private final WorldeditAPI worldeditAPI;
    private final ConfigManager configManager;
    private Map<UUID, Integer> playerPage;
    private Map<UUID, Integer> waitingForInput;
    private Map<UUID, Integer> previousPage;
    private boolean globalPvPmode;

    public GameEvents(LavaEscapePlugin plugin, Arena arena, ArenaManager arenaManager, GameManager gameManager, MenuManager menuManager, ArenaMenu arenaMenu, WorldeditAPI worldeditAPI, ConfigManager configManager) {
        this.plugin = plugin;
        this.arenaManager = arenaManager;
        this.gameManager = gameManager;
        this.menuManager = menuManager;
        this.arenaMenu = arenaMenu;
        this.worldeditAPI = worldeditAPI;
        this.configManager = configManager;
        this.playerArenaSelections = new HashMap<>();
        this.playerLobbySelections = new HashMap<>();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.arena = arena;
        this.globalPvPmode = arenaManager.getPvpMode();
        playerPage = new HashMap<>();
        waitingForInput = new HashMap<>();
        previousPage = new HashMap<>();
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
        if (currentTime - lastInteractTime < 500) {
            return;
        }
        lastInteract.put(player.getUniqueId(), currentTime);

        if (itemName.equals("ArenaWand")) {
            Action action = event.getAction();


            // Check for left or right-click on a block
            if (action == Action.LEFT_CLICK_BLOCK || action == Action.RIGHT_CLICK_BLOCK) {
                Location[] arenaSelections = playerArenaSelections.computeIfAbsent(playerId, k -> new Location[2]);

                if (action == Action.LEFT_CLICK_BLOCK) {
                    arenaSelections[0] = event.getClickedBlock().getLocation();
                    event.getPlayer().sendMessage("First position set at: " + arenaSelections[0]);
                } else {
                    arenaSelections[1] = event.getClickedBlock().getLocation();
                    event.getPlayer().sendMessage("Second position set at: " + arenaSelections[1]);
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
            player.openInventory(menuManager.conformationInv(player, null, arenaMenu.getArenaNamePage(player), "arena"));
        } else if (itemName.equals("LobbyWand") && getFirstLobbyPosition(playerId) != null && getSecondLobbyPosition(playerId) != null) {
            player.sendMessage("Lobby pos set!");
            message.add("First pos: " + getFirstLobbyPosition(playerId).toString());
            message.add("Second pos: " + getSecondLobbyPosition(playerId).toString());
            player.sendMessage(message.toString());
            arenaManager.writtenLobbyLocation1.put(playerId, getFirstLobbyPosition(playerId).toString());
            arenaManager.writtenLobbyLocation2.put(playerId, getSecondLobbyPosition(playerId).toString());
            player.openInventory(menuManager.conformationInv(player, null, arenaMenu.getArenaNamePage(player), "lobby"));
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
            menuManager.conformationInv(player, null, arenaMenu.getArenaNamePage(player), "arena");
        } else if (getFirstLobbyPosition(playerId) != null && getSecondLobbyPosition(playerId) != null) {
            menuManager.conformationInv(player, null, arenaMenu.getArenaNamePage(player),"lobby");
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
    public void onPlayerDamage (EntityDamageByEntityEvent event){

        UUID playerId = event.getDamager().getUniqueId();
        Player player = event.getDamager().getServer().getPlayer(playerId);
        Arena playerArena = arenaManager.findPlayerArena(player);

        if (playerArena != null) {
            event.setCancelled(!playerArena.getGameState().equals(ArenaManager.GameState.LAVA) && !playerArena.getGameState().equals(ArenaManager.GameState.DEATHMATCH));
        } else if(!globalPvPmode){
            event.setCancelled(true); // @ToDo Fix: Players can hurt each other when they aren't in a arena, and globalPVP doesn't work.
        }

    }

    /*@EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        Player player = event.getPlayer();
        Arena playerArena = arenaManager.findPlayerArena(player);

        if (playerArena != null) {
            event.setCancelled((!playerArena.getGameState().equals(ArenaManager.GameState.LAVA) && !playerArena.getGameState().equals(ArenaManager.GameState.DEATHMATCH)));
        } else event.setCancelled(true);

    }*/

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        Player player = event.getPlayer();
        Arena playerArena = arenaManager.findPlayerArena(player);

        if(playerArena == null){
            player.sendMessage("Couldn't find the arena");
            event.setCancelled(true);
        } else {
            Block block = event.getBlock();
            Material material = block.getType();
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

        if (menuManager.anvilGUIUsers.contains(playerId)) {
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
                arenaMenu.closeInventory(player);
                break;
            case "normal mode":
                event.setCancelled(true);
                mode = "server";
                arenaName = arenaMenu.getArenaNamePage(player);
                arenaManager.changeGameMode(arenaName, mode);
                arenaMenu.reloadPage(player, "config");
                break;
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
                ItemStack arenaWand = menuManager.getArenaWandItem(player);
                player.getInventory().remove(arenaWand);
                arenaName = arenaMenu.getArenaNamePage(player);
                arena = arenaManager.getArena(arenaName);
                arena.setArenaLocations(getFirstArenaPosition(playerId) , getSecondArenaPosition(playerId));
                arenaManager.saveTheArena(arena);
                worldeditAPI.saveArenaRegionAsSchematic(player,arenaName);
                arenaManager.writtenArenaLocation1.remove(playerId, arenaManager.writtenArenaLocation1.get(playerId));
                arenaManager.writtenArenaLocation2.remove(playerId, arenaManager.writtenArenaLocation2.get(playerId));
                //player.sendMessage(arenaName + " arena area set");
                arenaMenu.closeInventory(player);
                break;
            case "confirm lobby placement":
                event.setCancelled(true);
                ItemStack lobbyWand = menuManager.getLobbyWandItem(player);
                player.getInventory().remove(lobbyWand);
                arenaName = arenaMenu.getArenaNamePage(player);
                arena = arenaManager.getArena(arenaName);
                arena.setLobbyLocations(getFirstLobbyPosition(playerId) , getSecondLobbyPosition(playerId));
                arenaManager.saveTheLobby(arena);
                worldeditAPI.saveLobbyRegionAsSchematic(player,arenaName);
                arenaManager.writtenLobbyLocation1.remove(playerId, arenaManager.writtenLobbyLocation1.get(playerId));
                arenaManager.writtenLobbyLocation2.remove(playerId, arenaManager.writtenLobbyLocation2.get(playerId));
                //player.sendMessage(arenaName + " lobby area set");
                arenaMenu.closeInventory(player);
                break;
            case "set arena area":
                event.setCancelled(true);
                player.getInventory().addItem(menuManager.getArenaWandItem(player));
                player.sendMessage("You have received the Arena wand.");
                arenaMenu.closeInventory(player);
                break;
            case "set lobby area":
                player.getInventory().addItem(menuManager.getLobbyWandItem(player));
                player.sendMessage("You have received the Lobby wand.");
                arenaMenu.closeInventory(player);
                break;
            case "set minimum players":
                event.setCancelled(true);
                arenaName = arenaMenu.getArenaNamePage(player);
                arenaMenu.setSubPage(player, "MIN_PLAYERS");
                menuManager.setMinPlayers(player, arenaName);
                break;
            case "set maximum players":
                event.setCancelled(true);
                arenaName = arenaMenu.getArenaNamePage(player);
                arenaMenu.setSubPage(player, "MAX_PLAYERS");
                menuManager.setMaxPlayers(player, arenaName);
                break;
            case "set min y-level":
                event.setCancelled(true);
                arenaName = arenaMenu.getArenaNamePage(player);
                arenaMenu.setSubPage(player, "MIN_Y");
                menuManager.setMinY(player, arenaName);
                break;
            case "set max y-level":
                event.setCancelled(true);
                arenaName = arenaMenu.getArenaNamePage(player);
                arenaMenu.setSubPage(player, "MAX_Y");
                menuManager.setMaxY(player, arenaName);
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
                arenaName = arenaMenu.getArenaNamePage(player);
                arenaMenu.setSubPage(player, "RISE_TIME");
                menuManager.setRiseTime(player, arenaName);
                break;
            case "set grace time":
                event.setCancelled(true);
                arenaName = arenaMenu.getArenaNamePage(player);
                arenaMenu.setSubPage(player, "GRACE_TIME");
                menuManager.setGraceTime(player, arenaName);
                break;
            case "reset arena":
                event.setCancelled(true);
                arenaName = arenaMenu.getArenaNamePage(player);
                worldeditAPI.placeSchematic(player, arenaName);
                break;
            case "delete arena":
                event.setCancelled(true);
                String subpage = displayName.replace(" ", "_");
                arenaMenu.openSubPage(player, subpage);
                break;
            case "yes i want to delete":
                event.setCancelled(true);
                String rawData = (ChatColor.stripColor(Objects.requireNonNull(clickedItem.getItemMeta().getLore()).toString()));
                String[] splitRawData = rawData.split(": ");
                String[] fixedRawData = splitRawData[1].split("]");
                arenaName = fixedRawData[0];
                arenaManager.deleteArena(player, arenaName);
                player.sendMessage("Arena: '" + arenaName + "' has been deleted.");
                arenaMenu.closeInventory(player);
                break;
            case "no, what was i thinking?":
                arenaMenu.openSubPage(player, arenaMenu.getPreviousSubPage(player).toString());
                break;
            case "set starter items":
                arenaMenu.openSubPage(player, "STARTER_ITEMS");
                break;
            case "add new starter item":
                event.setCancelled(true);
                arenaName = arenaMenu.getArenaNamePage(player);
                menuManager.setStarterItems(player, arenaName);
                break;
            case "add new blacklisted block":
                event.setCancelled(true);
                arenaName = arenaMenu.getArenaNamePage(player);
                menuManager.setBlacklistedBlocks(player, arenaName);
                break;
            case "set blacklisted blocks":
                event.setCancelled(true);
                arenaName = arenaMenu.getArenaNamePage(player);
                // arenaManager.reloadArenaYml(arenaName);
                arenaMenu.openSubPage(player, "BLACKLISTED_BLOCKS");
                break;
            case "confirm name":
                event.setCancelled(true);
                arenaName = menuManager.writtenArenaName.get(playerId);
                if(arenaName.equalsIgnoreCase("null")){
                    break;
                }
                arenaManager.createArena(arenaName, world);
                menuManager.clearMap(player);
                player.sendMessage("Created arena: " + arenaName);
                arenaMenu.openArenaPage(player, arenaManager.getArena(arenaName));
                break;
            case "confirm miny":
                event.setCancelled(true);
                arenaName = arenaMenu.getArenaNamePage(player);
                if (menuManager.parseValueToInt(player) != -1) {
                    arenaManager.setMinY(arenaName, menuManager.parseValueToInt(player), player);
                }
                menuManager.clearMap(player);
                arenaMenu.goBack(player);
                break;
            case "confirm maxy":
                event.setCancelled(true);
                arenaName = arenaMenu.getArenaNamePage(player);
                if (menuManager.parseValueToInt(player) != -1) {
                    arenaManager.setMaxY(arenaName, menuManager.parseValueToInt(player), player);
                }
                menuManager.clearMap(player);
                arenaMenu.goBack(player);
                break;
            case "confirm minimum players":
                event.setCancelled(true);
                arenaName = arenaMenu.getArenaNamePage(player);
                if (menuManager.parseValueToInt(player) != -1) {
                    arenaManager.setMinPlayers(arenaName, menuManager.parseValueToInt(player));
                }
                menuManager.clearMap(player);
                arenaMenu.goBack(player);
                break;
            case "confirm maximum players":
                event.setCancelled(true);
                arenaName = arenaMenu.getArenaNamePage(player);
                if (menuManager.parseValueToInt(player) != -1) {
                    arenaManager.setMaxPlayers(arenaName, menuManager.parseValueToInt(player));
                }
                menuManager.clearMap(player);
                arenaMenu.goBack(player);
                break;
            case "confirm rise time":
                event.setCancelled(true);
                arenaName = arenaMenu.getArenaNamePage(player);
                if (menuManager.parseValueToInt(player) != -1) {
                    arenaManager.setRiseTime(arenaName, menuManager.parseValueToInt(player));
                }
                menuManager.clearMap(player);
                arenaMenu.goBack(player);
                break;
            case "confirm grace time":
                event.setCancelled(true);
                arenaName = arenaMenu.getArenaNamePage(player);
                if (menuManager.parseValueToInt(player) != -1) {
                    arenaManager.setGracePeriod(arenaName, menuManager.parseValueToInt(player));
                }
                menuManager.clearMap(player);
                arenaMenu.goBack(player);
                break;
            case "confirm starting item":
                event.setCancelled(true);
                arenaName = arenaMenu.getArenaNamePage(player);
                input = menuManager.writtenStarterItemsValue.get(playerId);
                if (menuManager.parseStartingItemAmount(input) != -1) {
                    arenaManager.setStarterItems(arenaName, menuManager.parseStartingItem(input), menuManager.parseStartingItemAmount(input), player);
                }
                menuManager.clearMap(player);
                arenaMenu.goBack(player);
                break;
            case "delete item":
                event.setCancelled(true);
                arenaName = arenaMenu.getArenaNamePage(player);
                String item = arenaMenu.deleteItemMap.get(playerId);
                arenaManager.deleteStarterItem(arenaName, item, player);
                arenaMenu.goBack(player);
                break;
            case "delete block":
                event.setCancelled(true);
                arenaName = arenaMenu.getArenaNamePage(player);
                String block = arenaMenu.deleteBlockMap.get(playerId);
                arenaManager.deleteBlacklistedItem(arenaName, block, player);
                arenaMenu.goBack(player);
                break;
            case "confirm blacklisted block":
                event.setCancelled(true);
                arenaName = arenaMenu.getArenaNamePage(player);
                input = menuManager.writtenBlacklistetBlocksValue.get(playerId);
                if (menuManager.parseBlacklistedBlock(input) != null) {
                    arenaManager.setBlacklistedBlocks(arenaName, menuManager.parseBlacklistedBlock(input), player);
                }
                menuManager.clearMap(player);
                arenaMenu.goBack(player);
                break;
            case "create new arena":
                menuManager.createNewArena(player);
                break;
            case "try again":
                event.setCancelled(true);
                switch (menuManager.getWrittenMapEnum(player)) {
                    case WRITTENARENANAME:
                        menuManager.clearMap(player);
                        menuManager.createNewArena(player);
                        break;
                    case WRITTENMINYVALUE:
                        menuManager.clearMap(player);
                        menuManager.setMinY(player, arenaMenu.getArenaNamePage(player));
                        break;
                    case WRITTENMAXYVALUE:
                        menuManager.clearMap(player);
                        menuManager.setMaxY(player, arenaMenu.getArenaNamePage(player));
                        break;
                    case WRITTENMINPLAYERSVALUE:
                        menuManager.clearMap(player);
                        menuManager.setMinPlayers(player, arenaMenu.getArenaNamePage(player));
                        break;
                    case WRITTENMAXPLAYERSVALUE:
                        menuManager.clearMap(player);
                        menuManager.setMaxPlayers(player, arenaMenu.getArenaNamePage(player));
                        break;
                    case WRITTENRISETIMEVALUE:
                        menuManager.clearMap(player);
                        menuManager.setRiseTime(player, arenaMenu.getArenaNamePage(player));
                        break;
                    case WRITTENGRACETIMEVALUE:
                        menuManager.clearMap(player);
                        menuManager.setGraceTime(player, arenaMenu.getArenaNamePage(player));
                        break;
                    case WRITTENSTARTERITEMSVALUE:
                        menuManager.clearMap(player);
                        menuManager.setStarterItems(player, arenaMenu.getArenaNamePage(player));
                        break;
                    case WRITTENBLACKLISTEDBLOCKSVALUE:
                        menuManager.clearMap(player);
                        menuManager.setBlacklistedBlocks(player, arenaMenu.getArenaNamePage(player));
                        break;
                }

                break;
            case "cancel": // Closes the menu.
                event.setCancelled(true);
                arenaMenu.closeInventory(player);
                break;
            case "arenas": // Sends player to the arenas menu
                event.setCancelled(true);
                arenaMenu.createArenaPages(player, 1);
                break;
            case "exit": // Closes the menu, replace with a InventoryCloseEvent.
                arenaMenu.closeInventory(player);
                event.setCancelled(true);
                break;
            case "go back.": // Sends the player to the previous menu.
                event.setCancelled(true);
                arenaMenu.goBack(player);
                break;
            case "config": // Sends the player to the config menu.
                arenaMenu.openSubPage(player, displayName);
                event.setCancelled(true);
                break;
            case "border":
                event.setCancelled(true);
            default:
                if (arenaManager.getArenaS().contains(displayName)) {
                    event.setCancelled(true);
                    arenaMenu.openArenaPage(player, arenaManager.getArena(displayName));
                }
        }
    }

}

