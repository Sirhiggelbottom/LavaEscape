package com.sirhiggelbottom.lavaescape.plugin;

import com.sirhiggelbottom.lavaescape.plugin.managers.ConfigManager;
import com.sirhiggelbottom.lavaescape.plugin.managers.ArenaManager;
import org.bukkit.plugin.java.JavaPlugin;

public class LavaEscapePlugin extends JavaPlugin {

    private GameManager gameManager;
    private ConfigManager configManager;
    private ArenaManager arenaManager;
    private LavaCommandExecutor commandExecutor;
    private LobbyManager lobbyManager;
    private UtilityClass utilityClass; // Placeholder for any utility class you may need

    @Override
    public void onEnable() {
        // Initialize ConfigManager
        configManager = new ConfigManager(this);

        // Initialize ArenaManager
        arenaManager = new ArenaManager(this, configManager);

        // Initialize LobbyManager
        lobbyManager = new LobbyManager(this, arenaManager);

        // Initialize GameManager
        gameManager = new GameManager(this, configManager, arenaManager, lobbyManager);

        // Initialize command executor and bind commands
        commandExecutor = new LavaCommandExecutor(this, gameManager, configManager, arenaManager, lobbyManager);
        getCommand("lava").setExecutor(commandExecutor);

        // Implementing Tab Completer for the commands
        getCommand("lava").setTabCompleter((TabCompleter) commandExecutor);

        // Initialize any utility classes
        utilityClass = new UtilityClass();

        // Any additional setup such as event listeners
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        // Save any necessary data and perform cleanup
    }

    // Getters for various managers if needed
    public GameManager getGameManager() {
        return gameManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public ArenaManager getArenaManager() {
        return arenaManager;
    }

    public LobbyManager getLobbyManager() {
        return lobbyManager;
    }

    // Additional methods or utilities
}
