package com.sirhiggelbottom.lavaescape.plugin.managers;

import com.sirhiggelbottom.lavaescape.plugin.LavaEscapePlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ConfigManager {
    private final LavaEscapePlugin plugin;
    private File configFile;
    private FileConfiguration config;
    private File arenaFile;
    private FileConfiguration arenaConfig;
    private File spawnPointFile;
    private FileConfiguration spawnPointConfig;

    public ConfigManager(LavaEscapePlugin plugin) {
        this.plugin = plugin;
        this.loadConfig();
    }
    private void loadConfig() {
        // Load or create the config.yml
        configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);

        // Load or create the Arena.yml
        arenaFile = new File(plugin.getDataFolder(), "Arena.yml");
        if (!arenaFile.exists()) {
            try {
                arenaFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create Arena.yml!");
                e.printStackTrace();
            }
        }
        arenaConfig = YamlConfiguration.loadConfiguration(arenaFile);

        //Load or create the spawnPoint.yml
        spawnPointFile = new File (plugin.getDataFolder(), "spawnPoint.yml");
        if(!spawnPointFile.exists()){
            try{
                spawnPointFile.createNewFile();
            }catch (IOException e){
                plugin.getLogger().severe("Could not create spawnPoints.yml!");
                e.printStackTrace();
            }
        }
        spawnPointConfig = YamlConfiguration.loadConfiguration(spawnPointFile);
    }

    // Save the config.yml
    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save config.yml!");
            e.printStackTrace();
        }
    }

    // Save the Arena.yml
    public void saveArenaConfig() {
        try {
            arenaConfig.save(arenaFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save Arena.yml!");
            e.printStackTrace();
        }
    }

    // Save the spawnPoint.yml

    public void saveSpawnPointConfig(){
        try{
            spawnPointConfig.save(spawnPointFile);
        }catch (IOException e){
            plugin.getLogger().severe("Could not save spawnPoints.yml");
            e.printStackTrace();
        }
    }



    // Similar methods for other settings like maxPlayers, arena/lobby positions, etc.

    // Methods for managing game settings in config.yml (grace period, item lists, etc.)

    // Example: Get list of blacklisted blocks
    public List<String> getBlacklistedBlocks() {
        return config.getStringList("blacklisted-blocks");
    }
    // Method to parse item list strings into a list of ItemStacks

    /*public List<ItemStack> parseItemsList(List<Map<String, Object>> itemsList) {
        List<ItemStack> itemList = new ArrayList<>();
        for (Map<String, Object> itemInfo : itemsList) {
            Material material = Material.matchMaterial((String) itemInfo.get("item"));
            int amount = (int) itemInfo.get("amount");
            ItemStack item = new ItemStack(material, amount);
            itemList.add(item);
        }
        return itemList;
    }*/

    // Set and Get methods for game modes
    public void setGameMode(String arenaName, String mode) {
        arenaConfig.set(arenaName + ".mode", mode);
        saveArenaConfig();
    }

    public String getGameMode(String arenaName) {
        return arenaConfig.getString(arenaName + ".mode", "repetitive"); // Default to "repetitive"
    }

    // Set and Get methods for starting items
    public void setStartingItems(List<Map<String, Object>> startingItems) {
        config.set("game-settings.start-items", startingItems);
        saveConfig();
    }

    /*public List<ItemStack> getStarterItems() {
        List<Map<String, Object>> itemsList = (List<Map<String, Object>>) config.getList("game-settings.start-items");
        return parseItemsList(itemsList);
    }*/
    public FileConfiguration getArenaConfig() {
        return arenaConfig;
    }

    public FileConfiguration getSpawnPointConfig(){
        return spawnPointConfig;
    }

    public FileConfiguration getConfig(){
        return config;
    }

    public void worldEditDIR(){
        File schematicDir = new File(plugin.getDataFolder().getParentFile(), "LavaEscape/schematics");
        if (!schematicDir.exists()) {
            schematicDir.mkdirs();
        }
    }

    // Additional utility methods for game settings
}

