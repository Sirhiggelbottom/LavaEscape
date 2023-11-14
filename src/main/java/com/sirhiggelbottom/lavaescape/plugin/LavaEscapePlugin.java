package com.sirhiggelbottom.lavaescape.plugin;

import com.sirhiggelbottom.lavaescape.plugin.commands.LavaCommandExecutor;
import com.sirhiggelbottom.lavaescape.plugin.events.GameEvents;
import com.sirhiggelbottom.lavaescape.plugin.managers.ConfigManager;
import com.sirhiggelbottom.lavaescape.plugin.managers.ArenaManager;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.java.JavaPlugin;

public class LavaEscapePlugin extends JavaPlugin {

//    private GameManager gameManager;
    private ConfigManager configManager;
    private ArenaManager arenaManager;
    private LavaCommandExecutor commandExecutor;
    private final GameEvents gameEvents;

    public LavaEscapePlugin(GameEvents gameEvents) {
        this.gameEvents = gameEvents;
    }

    @Override
    public void onEnable() {
        // Initialize ConfigManager
        configManager = new ConfigManager(this);

        // Initialize ArenaManager
        arenaManager = new ArenaManager(this, configManager);

        this.getServer().getPluginManager().registerEvents(new GameEvents(this), this);

        // Initialize command executor and bind commands
        commandExecutor = new LavaCommandExecutor(this, gameEvents, configManager, arenaManager);
        getCommand("lava").setExecutor(commandExecutor);

        // Implementing Tab Completer for the commands
        getCommand("lava").setTabCompleter((TabCompleter) commandExecutor);

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
