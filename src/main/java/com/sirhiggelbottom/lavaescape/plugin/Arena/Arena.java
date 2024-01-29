package com.sirhiggelbottom.lavaescape.plugin.Arena;

import com.sirhiggelbottom.lavaescape.plugin.managers.ArenaManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class Arena {
    private final String name;
    private Location arenaLoc1, arenaLoc2;
    private Location lobbyLoc1, lobbyLoc2;
    private ArenaManager.GameState gameState;
    private final Set<Player> players;

    public Arena(String name, Location arenaLoc1, Location arenaLoc2, Location lobbyLoc1, Location lobbyLoc2) {
        this.name = name;
        this.arenaLoc1 = arenaLoc1;
        this.arenaLoc2 = arenaLoc2;
        this.lobbyLoc1 = lobbyLoc1;
        this.lobbyLoc2 = lobbyLoc2;
        this.gameState = ArenaManager.GameState.STANDBY;
        this.players = new HashSet<>();
    }

    private int lavaTaskId = -1; // A field to store the task ID. Initialized to -1 to indicate no task is set.

    public void setLavaTaskId(int taskId) {
        this.lavaTaskId = taskId;
    }

    // Method to get the lava task ID
    public int getLavaTaskId() {
        return this.lavaTaskId;
    }

    // Method to cancel the lava task, if it's running
    public void cancelLavaTask() {
        if (this.lavaTaskId != -1) {
            Bukkit.getScheduler().cancelTask(this.lavaTaskId);
            this.lavaTaskId = -1; // Reset the task ID
        }
    }

    /*
    // Setters for locations
    public void setLocations(Location arenaLoc1, Location arenaLoc2, Location lobbyLoc1, Location lobbyLoc2) {
        this.arenaLoc1 = arenaLoc1;
        this.arenaLoc2 = arenaLoc2;
        this.lobbyLoc1 = lobbyLoc1;
        this.lobbyLoc2 = lobbyLoc2;
    }*/

    public void setArenaLocations(Location loc1, Location loc2){
        this.arenaLoc1 = loc1;
        this.arenaLoc2 = loc2;
    }

    public void setLobbyLocations(Location loc1, Location loc2){
        this.lobbyLoc1 = loc1;
        this.lobbyLoc2 = loc2;
    }

    // Getter and setter for game state
    public void setGameState(ArenaManager.GameState gameState) {
        this.gameState = gameState;
        // Additional logic for game state change can be implemented here
    }

    public ArenaManager.GameState getGameState() {
        return gameState;
    }

    // Player management methods
    public void addPlayer(Player player) {
        players.add(player);
        // Additional logic for adding a player
    }

    public void removePlayer(Player player) {

        players.remove(player);

        Bukkit.broadcastMessage("Remaining players: " + players.size());
        // Additional logic for removing a player

    }

    public Set<Player> getPlayers() {
        return new HashSet<>(players);
    }

    // Getters for arena and lobby locations
    public Location getArenaLoc1() {
        return arenaLoc1;
    }

    public Location getArenaLoc2() {
        return arenaLoc2;
    }

    public Location getLobbyLoc1() {
        return lobbyLoc1;
    }

    public Location getLobbyLoc2() {
        return lobbyLoc2;
    }

    // Get arena name
    public String getName() {
        return name;
    }

    // Additional methods and logic as needed
}

