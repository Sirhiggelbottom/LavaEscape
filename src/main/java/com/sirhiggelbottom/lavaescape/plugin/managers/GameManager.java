package com.sirhiggelbottom.lavaescape.plugin.managers;



/*
   ToDo Implement the following:
 *  - Logic that checks if arenas are ready for use.
 *  - Logic that checks if all the prerequisites for game start are ok.
 *  - Handle game logic such as:
 *       - Starting period, where it checks if there is enough players.
 *       - Grace period, where players can start gathering resources and loot chests.
 *       - Lava phase, where the lava starts rising and PvP is turned on.
 *       - Deathmatch, where the lava stops rising and the focus turns to PvP only
 *       - Player death, when a player dies update list with alive players and teleport dead player to lobby.
 *       - Game end, when there is only one player left, output message to all players that played and announce the winner. Then reset the arena.
 * */


import com.sirhiggelbottom.lavaescape.plugin.API.WorldeditAPI;
import com.sirhiggelbottom.lavaescape.plugin.Arena.Arena;
import com.sirhiggelbottom.lavaescape.plugin.LavaEscapePlugin;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.World;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GameManager {

    private final ArenaManager arenaManager;
    private final ConfigManager configManager;
    private final WorldeditAPI worldeditAPI;
    private final LavaEscapePlugin plugin;
    public GameManager(ArenaManager arenaManager, ConfigManager configManager, WorldeditAPI worldeditAPI, LavaEscapePlugin plugin) {
        this.arenaManager = arenaManager;
        this.configManager = configManager;
        this.worldeditAPI = worldeditAPI;
        this.plugin = plugin;
    }
    public boolean isArenaReady(Player player,String arenaName){
        Arena arena = arenaManager.getArena(arenaName);

        ConfigurationSection arenaConfigurationSection = configManager.getArenaConfig();

        if(!worldeditAPI.doesArenaSchematicExist(arenaName) && !worldeditAPI.doesLobbySchematicExist(arenaName) && !arenaManager.areSpawnpointsSet(arenaName)){
            player.sendMessage("Arena, Lobby and spawnpoints haven't been set");
            return false;
        }


        return true;
    }

    Map<UUID, Boolean> stopFilter = new HashMap<>();

    public void isGameReady(String arenaName){
        Arena arena = arenaManager.getArena(arenaName);

        arenaManager.healArenaPlayers(arenaName);

        int minPlayers = arenaManager.getMinPlayers(arenaName);
        int remainingPlayersForStart = minPlayers - arena.getPlayers().size();

        if(!arenaManager.getGameMode(arenaName).isBlank() || !arenaManager.getGameMode(arenaName).isEmpty()){
            if(arenaManager.getGameMode(arenaName).equalsIgnoreCase("server")){
                if (arena.getPlayers().size() >= minPlayers){
                    int countdownUntilStart = 1;
                    new BukkitRunnable() {
                        public void run() {

                            arenaManager.randomArenaTeleport(arenaName,arenaManager.getSpawnPoints(arenaName));
                            arenaManager.storeAndClearPlayersInventory(arenaName);
                            arenaManager.giveStartingItems(arenaName);
                            arenaManager.setSurvivalGamemode(arenaName);
                            arena.setGameState(ArenaManager.GameState.STARTING);
                            plugin.setShouldContinueFilling(true);
                            lavaTask(arenaName);

                        }
                    }.runTaskLater(JavaPlugin.getProvidingPlugin(getClass()), 20L * countdownUntilStart);


                }else {
                    Bukkit.broadcastMessage("Waiting for more players to start game " + remainingPlayersForStart + " is needed before the game can start.");
                    Bukkit.broadcastMessage("There is: " + arena.getPlayers().size() + " present in the lobby");
                    arenaManager.setAdventureGamemode(arenaName);
                    arena.setGameState(ArenaManager.GameState.WAITING);
                    plugin.setShouldContinueFilling(false);
                }
            }
        }

    }
    public void adminStart(Player player, String arenaName){
        Arena arena = arenaManager.getArena(arenaName);
        arenaManager.healArenaPlayers(arenaName);

        if(arenaManager.getGameMode(arenaName).equalsIgnoreCase("server")){
            player.sendMessage("Wrong gamemode, change to Competition mode.");
            return;
        }

        arena.setGameState(ArenaManager.GameState.STARTING);
        arenaManager.randomArenaTeleport(arenaName,arenaManager.getSpawnPoints(arenaName));
        arenaManager.storeAndClearPlayersInventory(arenaName);
        arenaManager.giveStartingItems(arenaName);
        arenaManager.setSurvivalGamemode(arenaName);
        plugin.setShouldContinueFilling(true);
        lavaTask(arenaName);

    }

    public void adminStopGame(Player admin, String arenaName){

        if(stopFilter.containsKey(admin.getUniqueId()) && stopFilter.get(admin.getUniqueId())){
            stopFilter.remove(admin.getUniqueId());
            return;
        }

        Arena arena = arenaManager.getArena(arenaName);
        plugin.setShouldContinueFilling(false);
        arena.cancelLavaTask();

        for(Player player : arenaManager.getPlayersInArena(arenaName)){

            arenaManager.healPlayer(player);
            arenaManager.teleportLobby(player, arenaName);
            arenaManager.restorePlayerInventory(player);

        }

        new BukkitRunnable() {
            public void run() {
                Bukkit.getLogger().info("Resetting arena");
                worldeditAPI.placeSchematic(admin, arenaName);

            }
        }.runTaskLater(JavaPlugin.getProvidingPlugin(getClass()), 100L);

        arena.setGameState(ArenaManager.GameState.STANDBY);
        Bukkit.broadcastMessage("Game stopped");
        stopFilter.put(admin.getUniqueId(), true);

    }
    public void lavaTask(String arenaName){
        Arena arena = arenaManager.getArena(arenaName);
        String basePath = "arenas." + arena.getName() + ".timeValues";

        arena.setGameState(ArenaManager.GameState.GRACE);
        String riseMessage = ChatColor.RED + "The lava is rising and PvP is turned on!";
        String deathMatchMessage = ChatColor.YELLOW + "Lava is no longer rising, it's time for a Deathmatch!";

        // Deprecated
        int Ymax = worldeditAPI.findArenaMaximumPointNonDebug(arenaName).getY() - 2; // Stops the lava 2 blocks below the maxY point.

        /*int Ymax = worldeditAPI.findArenaMaximumPointNonDebug(arenaName).y();*/

        int gracePeriod = configManager.getArenaConfig().getInt(basePath + ".gracePeriod");

        if(gracePeriod < 1){
            Bukkit.broadcastMessage("Graceperiod is less than 1, has it been set? Graceperiod is set to 1 second");
            gracePeriod = 1;
        }

        int riseTime = configManager.getArenaConfig().getInt(basePath + ".lavadelay");

        BukkitScheduler scheduler = plugin.getServer().getScheduler();

        int taskId = scheduler.scheduleSyncRepeatingTask(plugin, new Runnable() {
            // Deprecated
            int currentY = worldeditAPI.findArenaMinimumPointNonDebug(arenaName).getY();
            /*int currentY = worldeditAPI.findArenaMinimumPointNonDebug(arenaName).y();*/

            @Override
            public void run() {

                // Deprecated
                if(currentY == worldeditAPI.findArenaMinimumPointNonDebug(arenaName).getY()){
                    Bukkit.broadcastMessage(ChatColor.BOLD + riseMessage);
                }

                /*if(currentY == worldeditAPI.findArenaMinimumPointNonDebug(arenaName).y()){
                    Bukkit.broadcastMessage("The lava is rising!");
                }*/

                if(!plugin.shouldContinueFilling()){

                    arena.setGameState(ArenaManager.GameState.WAITING);
                    scheduler.cancelTasks(plugin);

                } else if (currentY >= Ymax) {

                    Bukkit.broadcastMessage(ChatColor.BOLD + deathMatchMessage);
                    arena.setGameState(ArenaManager.GameState.DEATHMATCH);
                    scheduler.cancelTasks(plugin);

                } else if(!arena.getGameState().equals(ArenaManager.GameState.LAVA)){
                    arena.setGameState(ArenaManager.GameState.LAVA);
                }

                fillLava(arenaName, currentY);
                currentY++;
            }
        }, 20L * gracePeriod,20L * riseTime);

        arena.setLavaTaskId(taskId);
    }
    public void fillLava(String arenaName, int y){

        Arena arena = arenaManager.getArena(arenaName);
        String basePath = "." + arena.getName();
        ConfigurationSection arenaSection = configManager.getArenaConfig().getConfigurationSection("arenas");

        if(arenaSection == null) return;

        String worldName = arenaSection.getString(basePath + ".worldName");

        if(worldName == null){
            Bukkit.broadcastMessage("This worldName doesn't exist.");
            return;
        }

        org.bukkit.World bukkitWorld = Bukkit.getWorld(worldName);

        if(bukkitWorld == null){
            return;
        }

        World world = BukkitAdapter.adapt(bukkitWorld);

        BlockVector3 minPoint = worldeditAPI.findArenaMinimumPointNonDebug(arenaName);
        BlockVector3 maxPoint = worldeditAPI.findArenaMaximumPointNonDebug(arenaName);

        // Deprecated
        int minX = minPoint.getX();
        int maxX = maxPoint.getX();
        int minZ = minPoint.getZ();
        int maxZ = maxPoint.getZ();

        /*int minX = minPoint.x();
        int maxX = maxPoint.x();
        int minZ = minPoint.z();
        int maxZ = maxPoint.z();*/

        org.bukkit.World buWorld = BukkitAdapter.adapt(world);

        for (int x = minX; x <= maxX; x++){
            for (int z = minZ; z <= maxZ; z++){

                Block block = buWorld.getBlockAt(x,y,z);
                if(block.getType() == Material.AIR){
                    block.setType(Material.LAVA);

                }
            }
        }

    }
}

