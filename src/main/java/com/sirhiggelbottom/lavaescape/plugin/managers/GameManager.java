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
import org.bukkit.configuration.ConfigurationSection;

public class GameManager {

    private final ArenaManager arenaManager;
    private final ConfigManager configManager;
    private final WorldeditAPI worldeditAPI;

    public GameManager(ArenaManager arenaManager, ConfigManager configManager, WorldeditAPI worldeditAPI) {
        this.arenaManager = arenaManager;
        this.configManager = configManager;
        this.worldeditAPI = worldeditAPI;
    }

    public void isArenaReady(String arenaName){
        Arena arena = arenaManager.getArena(arenaName);

        ConfigurationSection arenaConfigurationSection = configManager.getArenaConfig();

        if(worldeditAPI.doesArenaSchematicExist(arenaName) && worldeditAPI.doesLobbySchematicExist(arenaName) && arenaManager.areSpawnpointsSet(arenaName)){

        }

    } cx

    /*

    GameState currentState;
    List<Player> playersInArena;
    Arena currentArena;
    Configuration arenaConfig;
    Configuration gameConfig;
    int minPlayers;
    int maxPlayers;

    // Constructor to initialize the GameManager
    GameManager() {
        currentState = GameState.WAITING;
        playersInArena = new ArrayList<>();
        alivePlayers = new ArrayList<>();
        loadConfigurations();
    }

    // Load configurations from .yml files
    void loadConfigurations() {
        arenaConfig = loadYML("Arena.yml");
        gameConfig = loadYML("config.yml");
        minPlayers = gameConfig.getInt("minPlayers");
        maxPlayers = gameConfig.getInt("maxPlayers");
    }

    // Method to add a player to the arena
    void addPlayerToArena(Player player) {
        if (playersInArena.size() < maxPlayers) {
            playersInArena.add(player);
            player.teleport(currentArena.getLobbyLocation());
            checkIfGameCanStart();
        } else {
            player.sendMessage("Arena is full.");
        }
    }

    // Check if the arena is ready for use
    boolean checkArenaReadiness() {
        return currentArena.isReady();
    }

    // Check if the prerequisites for game start are okay
    void checkIfGameCanStart() {
        if (currentState == GameState.WAITING && playersInArena.size() >= minPlayers) {
            currentState = GameState.STARTING;
            startGame();
        }
    }

    // Handle the start of the game
    void startGame() {
        if (!checkArenaReadiness()) {
            broadcastMessage("Arena is not ready yet.");
            return;
        }

        alivePlayers = new ArrayList<>(playersInArena); // Clone the list
        broadcastMessage("Game is starting!");
        currentState = GameState.GRACE;
        teleportPlayersToArena();
        startGracePeriod();
    }

    // Teleport players to random locations inside of the arena
    void teleportPlayersToArena() {
        for (Player player : playersInArena) {
            Location spawnLocation = currentArena.getRandomSpawnLocation();
            player.teleport(spawnLocation);
        }
    }

    // Start the grace period countdown
    void startGracePeriod() {
        int graceTime = gameConfig.getInt("graceTime");
        // Start a timer for graceTime seconds
        // After the timer ends, call startLavaPhase()
    }

    // Start the lava phase
    void startLavaPhase() {
        currentState = GameState.LAVA;
        // Logic to start rising lava and enable PvP
    }

    // Handle player death
    void handlePlayerDeath(Player player) {
        alivePlayers.remove(player);
        player.teleport(currentArena.getLobbyLocation());
        checkForGameEnd();
    }

    // Check if the game should end
    void checkForGameEnd() {
        if (alivePlayers.size() == 1) {
            Player winner = alivePlayers.get(0);
            broadcastMessage(winner.getName() + " is the winner!");
            endGame();
        }
    }

    // End the game and reset the arena
    void endGame() {
        currentState = GameState.END;
        currentArena.reset();
        playersInArena.clear();
        alivePlayers.clear();
        currentState = GameState.WAITING;
    }

    // Start the deathmatch phase
    void startDeathmatch() {
        currentState = GameState.DEATHMATCH;
        // Stop lava rising
        // Focus on PvP
    }

    // Broadcast a message to all players in the game
    void broadcastMessage(String message) {
        for (Player player : playersInArena) {
            player.sendMessage(message);
        }
    }

    // Logic for when a player wants to leave the game
    void playerLeave(Player player) {
        playersInArena.remove(player);
        alivePlayers.remove(player);
        player.teleport(currentArena.getLobbyLocation());
        // Adjust alive player count and check game state
    }*/
}

