package com.sirhiggelbottom.lavaescape.plugin;

import com.sirhiggelbottom.lavaescape.plugin.API.WorldeditAPI;
import com.sirhiggelbottom.lavaescape.plugin.Arena.Arena;
import com.sirhiggelbottom.lavaescape.plugin.commands.LavaCommandExecutor;
import com.sirhiggelbottom.lavaescape.plugin.events.GameEvents;
import com.sirhiggelbottom.lavaescape.plugin.managers.ArenaManager;
import com.sirhiggelbottom.lavaescape.plugin.managers.ConfigManager;
import org.bukkit.plugin.java.JavaPlugin;

public class LavaEscapePlugin extends JavaPlugin {

//    private GameManager GameManager;
    private ConfigManager configManager;
    private ArenaManager arenaManager;
    private GameEvents gameEvents;
    private WorldeditAPI worldeditAPI;
    private Arena arena;


    @Override
    public void onEnable() {
        // Initialize ConfigManager
        configManager = new ConfigManager(this);

        // Initialize ArenaManager
        arenaManager = new ArenaManager(this, configManager);

        gameEvents = new GameEvents(this);
        this.getServer().getPluginManager().registerEvents(gameEvents, this);

        worldeditAPI = new WorldeditAPI(this, arenaManager);



        // Initialize command executor and bind commands
        LavaCommandExecutor commandExecutor = new LavaCommandExecutor(this, gameEvents, configManager, arenaManager, worldeditAPI);

        getCommand("lava").setExecutor(commandExecutor);

        // Implementing Tab Completer for the commands
        getCommand("lava").setTabCompleter(commandExecutor);

        // Any additional setup such as event listeners
    }

    @Override
    public void onDisable() {

        // Plugin shutdown logic
        // Save any necessary data and perform cleanup
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public ArenaManager getArenaManager() {
        return arenaManager;
    }

}
