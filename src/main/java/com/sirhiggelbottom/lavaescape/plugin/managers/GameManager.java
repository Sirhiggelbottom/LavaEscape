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
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitScheduler;

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

    public void isArenaReady(String arenaName){
        Arena arena = arenaManager.getArena(arenaName);

        ConfigurationSection arenaConfigurationSection = configManager.getArenaConfig();

        if(worldeditAPI.doesArenaSchematicExist(arenaName) && worldeditAPI.doesLobbySchematicExist(arenaName) && arenaManager.areSpawnpointsSet(arenaName)){

        }

    }


    public void isGameReady(String arenaName){
        Arena arena = arenaManager.getArena(arenaName);

        arenaManager.healArenaPlayers(arenaName);

        int minPlayers = arenaManager.getMinPlayers(arenaName);
        int remainingPlayersForStart = minPlayers - arena.getPlayers().size();
        if (arena.getPlayers().size() >= minPlayers){

            arenaManager.randomArenaTeleport(arenaName,arenaManager.getSpawnPoints(arenaName));
            arenaManager.storeAndClearPlayersInventory(arenaName);
            arenaManager.giveStartingItems(arenaName);
            arenaManager.setSurvivalGamemode(arenaName);
            arena.setGameState(ArenaManager.GameState.STARTING);
            plugin.setShouldContinueFilling(true);
            lavaTask(arenaName);

        }else {
            Bukkit.broadcastMessage("Waiting for more players to start game " + remainingPlayersForStart + " is needed before the game can start.");
            Bukkit.broadcastMessage("There is: " + arena.getPlayers().size() + " present in the lobby");
            arenaManager.setAdventureGamemode(arenaName);
            arena.setGameState(ArenaManager.GameState.WAITING);
            plugin.setShouldContinueFilling(false);
        }

    }

    public void lavaTask(String arenaName){
        Arena arena = arenaManager.getArena(arenaName);
        String basePath = "arenas." + arena.getName() + ".timeValues";

        int Ymax = worldeditAPI.findArenaMaximumPointNonDebug(arenaName).getY();

        int gracePeriod = configManager.getArenaConfig().getInt(basePath + ".gracePeriod");

        if(gracePeriod < 1){
            Bukkit.broadcastMessage("Graceperiod is less than 1, has it been set? Graceperiod is set to 1 second");
            gracePeriod = 1;
        }

        int delay = configManager.getArenaConfig().getInt(basePath + ".lavadelay");

        arena.setGameState(ArenaManager.GameState.GRACE);

        BukkitScheduler scheduler = plugin.getServer().getScheduler();
        int taskId = scheduler.scheduleSyncRepeatingTask(plugin, new Runnable() {
            int currentY = worldeditAPI.findArenaMinimumPointNonDebug(arenaName).getY();

            @Override
            public void run() {
                int maxY = Ymax;
                Bukkit.broadcastMessage("Max lava level is: " + maxY + " and currentY is set to: " + currentY);
                if(!plugin.shouldContinueFilling() || currentY >= maxY){
                    Bukkit.broadcastMessage("Lava is no longer rising.");
                    arena.setGameState(ArenaManager.GameState.WAITING);
                    scheduler.cancelTasks(plugin);
                }

                if(!arena.getGameState().equals(ArenaManager.GameState.LAVA)){
                    arena.setGameState(ArenaManager.GameState.LAVA);
                }

                fillLava(arenaName, currentY);
                Bukkit.broadcastMessage("Y-level: " + currentY + " is being filled with lava");
                currentY++;
            }
        }, 20L * gracePeriod,20L * delay);
        arena.setLavaTaskId(taskId);
    }

    public void fillLava(String arenaName, int y){

        Arena arena = arenaManager.getArena(arenaName);
        String basePath = "." + arena.getName();
        ConfigurationSection arenaSection = configManager.getArenaConfig().getConfigurationSection("arenas");

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

        int minX = minPoint.getX();
        int maxX = maxPoint.getX();
        int minZ = minPoint.getZ();
        int maxZ = maxPoint.getZ();

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

