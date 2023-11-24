package com.sirhiggelbottom.lavaescape.plugin.managers;

import com.sirhiggelbottom.lavaescape.plugin.API.WorldeditAPI;
import com.sirhiggelbottom.lavaescape.plugin.Arena.Arena;
import com.sirhiggelbottom.lavaescape.plugin.LavaEscapePlugin;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.block.BlockState;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class ArenaManager {
    private final LavaEscapePlugin plugin;
    private final ConfigManager configManager;
    private final Map<String, Arena> arenaNames;
    private final WorldeditAPI worldeditAPI;

    public ArenaManager(LavaEscapePlugin plugin, ConfigManager configManager, WorldeditAPI worldeditAPI) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.arenaNames = new HashMap<>();
        this.worldeditAPI = new WorldeditAPI(plugin,configManager, this);
        loadArenas();
    }

    private void loadArenas() {
        ConfigurationSection arenasSection = configManager.getArenaConfig().getConfigurationSection("arenas");
        if (arenasSection != null) {
            for (String arenaName : arenasSection.getKeys(false)) {
                // Create a new Arena object with the specified name and null locations
                Arena arena = new Arena(arenaName, null, null, null, null);
                // Store the arena in the HashMap with the arena's name as the key
                this.arenaNames.put(arenaName, arena);
            }
        }
    }

    private Arena loadArena(String arenaName) {
        ConfigurationSection section = configManager.getArenaConfig().getConfigurationSection("arenas." + arenaName);
        if (section == null) return null;

        // Load arena and lobby locations
        Location arenaLoc1 = getLocationFromSection(section, "arena.pos1");
        Location arenaLoc2 = getLocationFromSection(section, "arena.pos2");
        Location lobbyLoc1 = getLocationFromSection(section, "lobby.pos1");
        Location lobbyLoc2 = getLocationFromSection(section, "lobby.pos2");

        return new Arena(arenaName, arenaLoc1, arenaLoc2, lobbyLoc1, lobbyLoc2);
    }

    private Location getLocationFromSection(ConfigurationSection section, String path) {
        World world = plugin.getServer().getWorld(section.getString(path + ".world"));
        int x = section.getInt(path + ".x");
        int y = section.getInt(path + ".y");
        int z = section.getInt(path + ".z");
        return new Location(world, x, y, z);
    }


    public void createArena(String arenaName) {

        if(arenaNames.containsKey(arenaName)){
            return;
        }
        Arena arena = new Arena(arenaName, null, null, null, null);
        arenaNames.put(arenaName, arena);
        saveNewArena(arena);
    }

    public void saveTheArena(Arena arena){
        String basePath = "arenas." + arena.getName();
        saveLocationToConfig(basePath + ".arena.pos1", arena.getArenaLoc1());
        saveLocationToConfig(basePath + ".arena.pos2", arena.getArenaLoc2());
        configManager.saveArenaConfig();
    }

    public void saveTheLobby(Arena arena){
        String basePath = "arenas." + arena.getName();
        saveLocationToConfig(basePath + ".lobby.pos1", arena.getLobbyLoc1());
        saveLocationToConfig(basePath + ".lobby.pos2", arena.getLobbyLoc2());
        configManager.saveArenaConfig();
    }

    public void saveArena(Arena arena) {
        String basePath = "arenas." + arena.getName();
        saveLocationToConfig(basePath + ".arena.pos1", arena.getArenaLoc1());
        saveLocationToConfig(basePath + ".arena.pos2", arena.getArenaLoc2());
        saveLocationToConfig(basePath + ".lobby.pos1", arena.getLobbyLoc1());
        saveLocationToConfig(basePath + ".lobby.pos2", arena.getLobbyLoc2());
        configManager.saveArenaConfig();
    }

    public void saveNewArena(Arena arena){
        // Setting arena path in arena.yml file
        String basePath = "arenas." + arena.getName();

        // Creating a new arena section in arena.yml
        tryLogging(() -> configManager.getArenaConfig().createSection(basePath),
                "an error occurred while creating a new section in arena.yml");

        // Sets the arena name
        tryLogging(() -> configManager.getArenaConfig().set(basePath + ".name", arena.getName()),
                "an error occurred while assigning a name to new arena in arena.yml");

        // Setting placeholder values for the locations to "none"
        tryLogging(() ->{
            ConfigurationSection configurationSection = configManager.getArenaConfig();
            configurationSection.set(basePath + ".arena.pos1", "none");
            configurationSection.set(basePath + ".arena.pos2", "none");
            configurationSection.set(basePath + ".lobby.pos1", "none");
            configurationSection.set(basePath + ".lobby.pos2", "none");
        }, "an error occurred while assigning placeholder values to new arena in arena.yml");

        // Saving the arena config to arena.yml
        tryLogging(configManager::saveArenaConfig,
                "an error occurred while saving the arena config to arena.yml");

    }
    public void tryLogging(Runnable action, String errorMessage){
        try {
            action.run();
        } catch (Exception e){
            plugin.getLogger().log(Level.SEVERE, errorMessage, e);
        }
    }

    public void deleteArena(CommandSender sender, String arenaName){

        if(!(arenaNames.containsKey(arenaName))){
            return;
        }

        arenaNames.remove(arenaName);

        String basePath = "arenas." + arenaName;

        configManager.getArenaConfig().set(basePath,null);
        worldeditAPI.deleteSchematic(sender, arenaName);
        configManager.saveArenaConfig();

    }
    private void saveLocationToConfig(String path, Location location) {
        configManager.getArenaConfig().set(path + ".world", location.getWorld().getName());
        configManager.getArenaConfig().set(path + ".x", location.getBlockX());
        configManager.getArenaConfig().set(path + ".y", location.getBlockY());
        configManager.getArenaConfig().set(path + ".z", location.getBlockZ());
    }

    public void changeGameState(String arenaName, GameState newState) {
        Arena arena = arenaNames.get(arenaName);
        if (arena != null) {
            arena.setGameState(newState);
            // Additional logic for game state change can be implemented here
        }
    }

    public void addPlayerToArena(String arenaName, Player player) {
        Arena arena = arenaNames.get(arenaName);
        if (arena != null) {
            arena.addPlayer(player);
            // Additional logic for adding player to arena can be implemented here
        }
    }

    public void removePlayerFromArena(String arenaName, Player player) {
        Arena arena = arenaNames.get(arenaName);
        if (arena != null) {
            arena.removePlayer(player);
            // Additional logic for removing player from arena can be implemented here
        }
    }

    public Set<Player> getPlayersInArena(String arenaName) {
        Arena arena = arenaNames.get(arenaName);
        if (arena != null) {
            return new HashSet<>(arena.getPlayers());
        }
        return new HashSet<>();
    }

    public enum GameState {
        WAITING,
        STARTING,
        GRACE,
        LAVA,
        DEATHMATCH
    }
    public Arena getArena(String arenaName) {
        return arenaNames.get(arenaName);
    }
    public List<String> getArenaS() {
        List<String> arenaNames;
        ConfigurationSection arenasSection = configManager.getArenaConfig().getConfigurationSection("arenas.");
        if (arenasSection != null) {
            arenaNames = new ArrayList<>(arenasSection.getKeys(false));
        } else return null;
        return arenaNames;
    }
    public List<BlockVector3> findSpawnPoints(String arenaName, int yMin, int yMax) {
        File schematicFile = new File("path/to/schematic/" + arenaName + ".schem");
        List<BlockVector3> spawnPoints = new ArrayList<>();
        while (spawnPoints.size() <= 150){
            try (ClipboardReader reader = ClipboardFormats.findByFile(schematicFile).getReader(new FileInputStream(schematicFile))) {
                Clipboard clipboard = reader.read();
                for (BlockVector3 point : clipboard.getRegion()) {
                    int y = point.getBlockY();
                    if (y >= yMin && y <= yMax) {
                        Block ground = (Block) clipboard.getBlock(point);
                        Block space1 = (Block) clipboard.getBlock(point.add(0, 1, 0));
                        Block space2 = (Block) clipboard.getBlock(point.add(0, 2, 0));



                        // Check criteria: on solid ground, not a tree, not inside a block, enough space above
                        if (isSolidGround(ground) && !isTree(ground) && isAir(space1) && isAir(space2) && spawnPoints.stream().noneMatch(item -> item.equals(point))) {
                            spawnPoints.add(point);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        return spawnPoints;
    }

    private boolean isSolidGround(Block block){
        return block.getType().isSolid();
    }

    private boolean isTree(Block block){
        Material type = block.getType();
        return type.toString().endsWith("_LOG") || type.toString().endsWith("_LEAVES");
    }

    private boolean isAir(Block block){
        return block.getType() == Material.AIR;
    }
    public boolean setMinYLevel(Arena arena, int i, Player player){
        String basepath = "arenas." + arena.getName();
        ConfigurationSection configurationSection = configManager.getArenaConfig();

        configurationSection.set(basepath + ".Y-levels.Ymin", i);
        configManager.saveArenaConfig();
        player.sendMessage("Ymin-level set to Ylevel: " + i);
        return true;
    }

    public boolean setMaxYLevel(Arena arena, int i,Player player){
        String basepath = "arenas." + arena.getName();
        ConfigurationSection configurationSection = configManager.getArenaConfig();

        configurationSection.set(basepath + ".Y-levels.Ymax", i);
        configManager.saveArenaConfig();
        player.sendMessage("Ymax-level set to Ylevel: " + i);
        return true;
    }

}

// Additional methods and utilities as necessary

