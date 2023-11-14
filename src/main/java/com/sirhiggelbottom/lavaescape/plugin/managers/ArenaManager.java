package com.sirhiggelbottom.lavaescape.plugin.managers;

import com.sirhiggelbottom.lavaescape.plugin.LavaEscapePlugin;
import com.sirhiggelbottom.lavaescape.plugin.Arena.Arena;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;


import java.util.*;

public class ArenaManager {
    private final LavaEscapePlugin plugin;
    private final ConfigManager configManager;
    private final Map<String, Arena> arenas;

    public ArenaManager(LavaEscapePlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        arenas = new HashMap<>();
        loadArenas();
    }

    private void loadArenas() {
        ConfigurationSection arenasSection = configManager.getArenaConfig().getConfigurationSection("arenas");
        if (arenasSection != null) {
            for (String arenaName : arenasSection.getKeys(false)) {
                arenas.put(arenaName, loadArena(arenaName));
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

    public void createOrUpdateArena(String arenaName, Location arenaLoc1, Location arenaLoc2, Location lobbyLoc1, Location lobbyLoc2) {
        Arena arena = arenas.get(arenaName);
        if (arena == null) {
            arena = new Arena(arenaName, arenaLoc1, arenaLoc2, lobbyLoc1, lobbyLoc2);
            arenas.put(arenaName, arena);
        } else {
            arena.setLocations(arenaLoc1, arenaLoc2, lobbyLoc1, lobbyLoc2);
        }
        saveArena(arena);
    }

    public void saveArena(Arena arena) {
        String basePath = "arenas." + arena.getName();
        saveLocationToConfig(basePath + ".arena.pos1", arena.getArenaLoc1());
        saveLocationToConfig(basePath + ".arena.pos2", arena.getArenaLoc2());
        saveLocationToConfig(basePath + ".lobby.pos1", arena.getLobbyLoc1());
        saveLocationToConfig(basePath + ".lobby.pos2", arena.getLobbyLoc2());
        configManager.saveArenaConfig();
    }

    private void saveLocationToConfig(String path, Location location) {
        configManager.getArenaConfig().set(path + ".world", location.getWorld().getName());
        configManager.getArenaConfig().set(path + ".x", location.getBlockX());
        configManager.getArenaConfig().set(path + ".y", location.getBlockY());
        configManager.getArenaConfig().set(path + ".z", location.getBlockZ());
    }

    public void changeGameState(String arenaName, GameState newState) {
        Arena arena = arenas.get(arenaName);
        if (arena != null) {
            arena.setGameState(newState);
            // Additional logic for game state change can be implemented here
        }
    }

    public void addPlayerToArena(String arenaName, Player player) {
        Arena arena = arenas.get(arenaName);
        if (arena != null) {
            arena.addPlayer(player);
            // Additional logic for adding player to arena can be implemented here
        }
    }

    public void removePlayerFromArena(String arenaName, Player player) {
        Arena arena = arenas.get(arenaName);
        if (arena != null) {
            arena.removePlayer(player);
            // Additional logic for removing player from arena can be implemented here
        }
    }

    public Set<Player> getPlayersInArena(String arenaName) {
        Arena arena = arenas.get(arenaName);
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
        return arenas.get(arenaName);
    }
    public List<String> getArenaS() {
        List<String> arenaNames = new ArrayList<>();
        ConfigurationSection arenasSection = configManager.getArenaConfig().getConfigurationSection("arenas.");
        if (arenasSection != null) {
            arenaNames.addAll(arenasSection.getKeys(false));
        } else if (arenasSection == null) return null;
        return arenaNames;
    }
}

// Additional methods and utilities as necessary

