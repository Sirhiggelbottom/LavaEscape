package com.sirhiggelbottom.lavaescape.plugin;

import com.sirhiggelbottom.lavaescape.plugin.arena.Arena;
import com.sirhiggelbottom.lavaescape.plugin.commands.commandLocationWand;
import com.sirhiggelbottom.lavaescape.plugin.config.ConfigManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    private final ConfigManager configManager;
    private final String arenaName;

    public Main(ConfigManager configManager, String arenaName) {
        this.configManager = configManager;
        this.arenaName = arenaName;
    }

    @Override
    public void onEnable() {
        // Initialize the ConfigManager
        ConfigManager configManager = new ConfigManager(this);

        // Save the default config.yml if it doesn't exist
        configManager.saveDefaultConfig();

        // Register listeners
        getServer().getPluginManager().registerEvents(new Arena(this, null, arenaName), this);

        // Register commands
        getCommand("Lwand").setExecutor(new commandLocationWand());
        getCommand("Lwand").setTabCompleter(new commandLocationWand());
    }

    @Override
    public void onDisable() {
        configManager.saveConfig();
    }
}
