package com.sirhiggelbottom.lavaescape.plugin.arena;

import com.sirhiggelbottom.lavaescape.plugin.Main;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;

public class ArenaManager {

    private final Main plugin;

    public ArenaManager(Main plugin) {
        this.plugin = plugin;
    }

    public void saveArena(String arenaName, Block pos1, Block pos2) {
        FileConfiguration config = getConfig();

        String path = "arenas." + arenaName + ".";
        config.set(path + "world", pos1.getWorld().getName());
        config.set(path + "pos1.x", pos1.getX());
        config.set(path + "pos1.y", pos1.getY());
        config.set(path + "pos1.z", pos1.getZ());
        config.set(path + "pos2.x", pos2.getX());
        config.set(path + "pos2.y", pos2.getY());
        config.set(path + "pos2.z", pos2.getZ());

        saveConfig(config);
    }

    private FileConfiguration getConfig() {
        return plugin.getConfig();
    }

    private void saveConfig(FileConfiguration config) {
        File configFile = new File(plugin.getDataFolder(), "Arenas.yml");
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
