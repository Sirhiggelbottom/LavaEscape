package com.sirhiggelbottom.lavaescape.plugin;

import com.sirhiggelbottom.lavaescape.plugin.API.WorldeditAPI;
import com.sirhiggelbottom.lavaescape.plugin.Arena.Arena;
import com.sirhiggelbottom.lavaescape.plugin.commands.LavaCommandExecutor;
import com.sirhiggelbottom.lavaescape.plugin.events.GameEvents;
import com.sirhiggelbottom.lavaescape.plugin.managers.*;
import org.bukkit.plugin.java.JavaPlugin;

public class LavaEscapePlugin extends JavaPlugin {

//    private GameManager GameManager;
    private ConfigManager configManager;
    private ArenaManager arenaManager;
    private GameEvents gameEvents;
    private WorldeditAPI worldeditAPI;
    private Arena arena;
    private GameManager gameManager;
    private ItemManager itemManager;
    private ArenaMenu arenaMenu;
    private boolean shouldContinueFilling = false;

    @Override
    public void onEnable() {
        // Initialize ConfigManager
        configManager = new ConfigManager(this);

        // Initialize ArenaManager
        arenaManager = new ArenaManager(this, configManager, arena);

        worldeditAPI = new WorldeditAPI(this, arenaManager, configManager);

        gameManager = new GameManager(arenaManager,configManager,worldeditAPI,this);

        itemManager = new ItemManager(this , arenaManager);

        arenaMenu = new ArenaMenu(this, arenaManager, itemManager, arena);

        gameEvents = new GameEvents(this, arenaManager, gameManager, itemManager, arenaMenu, worldeditAPI, configManager);

        this.getServer().getPluginManager().registerEvents(gameEvents, this);

        getCommand("lavaEscape").setExecutor(new LavaCommandExecutor(arenaMenu));

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

    public boolean shouldContinueFilling() {
        return shouldContinueFilling;
    }

    public void setShouldContinueFilling(boolean shouldContinueFilling) {
        this.shouldContinueFilling = shouldContinueFilling;
    }

}
