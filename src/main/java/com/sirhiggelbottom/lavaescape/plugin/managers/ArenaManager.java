package com.sirhiggelbottom.lavaescape.plugin.managers;

import com.sirhiggelbottom.lavaescape.plugin.API.WorldeditAPI;
import com.sirhiggelbottom.lavaescape.plugin.Arena.Arena;
import com.sirhiggelbottom.lavaescape.plugin.LavaEscapePlugin;
import com.sirhiggelbottom.lavaescape.plugin.LootItem;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Location;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class ArenaManager {
    private final LavaEscapePlugin plugin;
    private final ConfigManager configManager;
    private final Map<String, Arena> arenaNames;
    private final WorldeditAPI worldEditAPI;
    private List<Location> usedSpawns;
    private final Arena arena;
    private HashMap<UUID, List<ItemStack>> playerItems;
    private HashMap<UUID, Integer> playerExp;
    public Map<String, List<ItemStack>> startingItemsMap;
    public Map<String, List<LootItem>> lootItemsMap;
    public Map<String, List<ItemStack>> blacklistedBlocksMap;
    public Map<UUID, String> writtenArenaLocation1;
    public Map<UUID, String> writtenArenaLocation2;
    public Map<UUID, String> writtenLobbyLocation1;
    public Map<UUID, String> writtenLobbyLocation2;
    public Map<Arena, List<Location>> lootChestLocations;
    public Map<UUID, Set<Location>> openLootchests;
    private Location arenaPos1;
    private Location arenaPos2;
    private Location lobbyPos1;
    private Location lobbyPos2;

    private boolean globalPvPMode;
    private final Random random;
    public ArenaManager(LavaEscapePlugin plugin, ConfigManager configManager, Arena arena) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.arena = arena;
        this.arenaNames = new HashMap<>();
        this.worldEditAPI = new WorldeditAPI(plugin, this, configManager);
        usedSpawns = new ArrayList<>();
        playerItems = new HashMap<>();
        playerExp = new HashMap<>();
        startingItemsMap = new HashMap<>();
        lootItemsMap = new HashMap<>();
        blacklistedBlocksMap = new HashMap<>();
        writtenArenaLocation1 = new HashMap<>();
        writtenArenaLocation2 = new HashMap<>();
        writtenLobbyLocation1 = new HashMap<>();
        writtenLobbyLocation2 = new HashMap<>();
        lootChestLocations = new HashMap<>();
        openLootchests = new HashMap<>();
        globalPvPMode = getPvpMode();
        random = new Random();
        loadArenas();
    }
    private void loadArenas() {
        ConfigurationSection arenasSection = configManager.getArenaConfig().getConfigurationSection("arenas");
        if (arenasSection != null) {
            for (String arenaName : arenasSection.getKeys(false)) {
                // Create a new Arena object with the specified name and null locations
                Arena arena = new Arena(arenaName, null, null, null, null);
                // Store the arena in the HashMap with the arena's name as the key
                this.arenaNames.put(arenaName, arena);
            }
        }
    }
    public void createArena(String arenaName, World world) {

        if(arenaNames.containsKey(arenaName)){
            return;
        }
        Arena arena = new Arena(arenaName, null, null, null, null);
        arenaNames.put(arenaName, arena);
        String worldName = world.getName();
        saveNewArena(arena, worldName);
    }
    public void saveTheArena(Arena arena){
        String basePath = "arenas." + arena.getName();
        saveLocationToConfig(basePath + ".arena.pos1", arena.getArenaLoc1());
        saveLocationToConfig(basePath + ".arena.pos2", arena.getArenaLoc2());
        configManager.saveArenaConfig();
    }
    public void saveTheLobby(Arena arena){
        String basePath = "arenas." + arena.getName();
        saveLocationToConfig(basePath + ".lobby.pos1", arena.getLobbyLoc1());
        saveLocationToConfig(basePath + ".lobby.pos2", arena.getLobbyLoc1());
        configManager.saveArenaConfig();
    }
    public void saveNewArena(Arena arena, String worldName){
        // Setting arena path in arena.yml file
        String globalSettings = "Global settings.";
        String basePath = "arenas." + arena.getName();
        String playersPath = basePath + ".players";
        String yLevelPath = basePath + ".Y-levels";
        String timePath = basePath + ".timeValues";
        String modePath = basePath + ".mode";
        String itemPath = basePath + ".start-items";
        String lootPath = basePath + ".loot-items";
        String blockPath = basePath + ".blacklisted-blocks";

        List<String> defaultBlacklistedBlocks = new ArrayList<>();
        defaultBlacklistedBlocks.add("BEDROCK");

        // Creating a new arena section in arena.yml
        tryLogging(() -> configManager.getArenaConfig().createSection(basePath),
                "an error occurred while creating a new section in arena.yml");

        // Saving the worldName to arena.yml
        tryLogging(()-> configManager.getArenaConfig().set(basePath + ".worldName", worldName),
        "an error occurred while saving the worldName in arena.yml");

        // Setting placeholder values for the locations and Y-levels to "none"
        tryLogging(() ->{
            ConfigurationSection configurationSection = configManager.getArenaConfig();
            configurationSection.set(basePath + ".arena.pos1", "none");
            configurationSection.set(basePath + ".arena.pos2", "none");
            configurationSection.set(basePath + ".lobby.pos1", "none");
            configurationSection.set(basePath + ".lobby.pos2", "none");
            configurationSection.set(yLevelPath + ".Ymin","none");
            configurationSection.set(yLevelPath + ".Ymax","none");

            // Set min and max players to default values
            configurationSection.set(playersPath + ".minPlayers", 2);
            configurationSection.set(playersPath + ".maxPlayers", 10);

            /*// Set the World name
            configurationSection.set(basePath + ".worldName", worldName);*/
            // Set the gracePeriod to default 60 seconds.
            configurationSection.set(timePath + ".gracePeriod", 60);
            // Set the lavadelay to default 5 seconds.
            configurationSection.set(timePath + ".lavadelay", 5);
            // Set gamemode to server (Repeating) mode by default.
            configurationSection.set(modePath, "server");

            // Set default starting items.
            configurationSection.set(itemPath + ".STONE_SWORD" , 1);
            configurationSection.set(itemPath + ".STONE_PICKAXE" , 1);
            configurationSection.set(itemPath + ".BAKED_POTATO" , 5);
            // Set default loot items.
            configurationSection.set(lootPath + ".GOLDEN_APPLE" + ".amount", 1);
            configurationSection.set(lootPath + ".GOLDEN_APPLE" + ".rarity", 40.0);
            // Set default blacklisted block.
            configurationSection.set(blockPath, defaultBlacklistedBlocks);

            configurationSection.get(globalSettings + ".PvPMode." , false);

        }, "An error occurred while assigning placeholder values to new arena in arena.yml");

        // Saving the arena config to arena.yml
        tryLogging(configManager::saveArenaConfig,
                "An error occurred while saving the arena config to arena.yml");

    }
    public void changeGameMode(String arenaName, String gamemode) {
        Arena arena = arenaNames.get(arenaName);
        if (arena != null) {
            ConfigurationSection config = configManager.getArenaConfig();
            String modePath = "arenas." + arena.getName() + ".mode";

            config.set(modePath, gamemode);
            arena.setCurrentGameMode(gamemode);

            configManager.saveArenaConfig();


        }

    }
    public String getGameMode(String arenaName){
        String path = "arenas." + arenaName + ".mode";

        ConfigurationSection configurationSection = configManager.getArenaConfig();
        return configurationSection.get(path).toString();
    }
    public void changeCurrentPvPMode(){
        globalPvPMode = !globalPvPMode;
    }
    public boolean getCurrentPvPMode(){
        return globalPvPMode;
    }
    public void setMinY(String arenaName, int inputtedY_level, Player player){
        String basePath = "arenas." + arenaName;
        String yLevelPath = basePath + ".Y-levels.Ymin";
        int y_level;

        Clipboard clipboard = worldEditAPI.loadArenaSchematic(arenaName, player);
        CuboidRegion region = (CuboidRegion) clipboard.getRegion();
        BlockVector3 point = region.getMinimumPoint();
        int minY = point.getY();

        if(inputtedY_level < minY){
            y_level = minY;
            player.sendMessage("You have set the Ymin-level too low, Ymin has been set to: " + minY);
        } else y_level = inputtedY_level;

        tryLogging(()->{
            ConfigurationSection configurationSection = configManager.getArenaConfig();

            configurationSection.set(yLevelPath, y_level);
            configManager.saveArenaConfig();
            player.sendMessage("Min Y-level has been set to: " + y_level + ".");
        }, "An error occurred while setting minY");

    }
    public void setMaxY(String arenaName, int inputtedY_level, Player player){
        String basePath = "arenas." + arenaName;
        String yLevelPath = basePath + ".Y-levels.Ymax";

        int y_level;

        Clipboard clipboard = worldEditAPI.loadArenaSchematic(arenaName, player);
        CuboidRegion region = (CuboidRegion) clipboard.getRegion();
        BlockVector3 point = region.getMaximumPoint();
        int maxY = point.getY();

        if(inputtedY_level > maxY){
            y_level = maxY;
            player.sendMessage("You have set the Ymax-level too high, Ymax has been set to: " + maxY);
        } else y_level = inputtedY_level;

        tryLogging(()->{
            ConfigurationSection configurationSection = configManager.getArenaConfig();

            configurationSection.set(yLevelPath, y_level);
            configManager.saveArenaConfig();
            player.sendMessage("Max Y-level has been set to: " + y_level + ".");
        }, "An error occurred while setting maxY");

    }
    public void setStarterItems(String arenaName, String item, int amount, Player player){
        String basePath = "arenas." + arenaName;
        String itemPath = basePath + ".start-items.";

        tryLogging(() -> {
            ConfigurationSection configurationSection = configManager.getArenaConfig();

            configurationSection.set(itemPath + item, amount);
            configManager.saveArenaConfig();

            updateInMemoryStartingItemList(arenaName);

            player.sendMessage("Added: " + amount + " " + item + " to starting items.");
        }, "An error occurred while adding starter item to arena.yml");
    }
    private void updateInMemoryStartingItemList(String arenaName){
        List<ItemStack> updatedItems = getStarterItems(arenaName);
        setStartingItems(arenaName, updatedItems);
    }
    private void setStartingItems(String arenaName, List<ItemStack> items){
        this.startingItemsMap.put(arenaName, items);
    }
    public List<ItemStack> getStartingItems(String arenaName){
        if(this.startingItemsMap.get(arenaName) == null){
            return getStarterItems(arenaName);
        } else return this.startingItemsMap.get(arenaName);
    }
    public List<ItemStack> getStarterItems(String arenaName){
        FileConfiguration arenaConfig = configManager.getArenaConfig();
        String basePath = "arenas." + arenaName + ".start-items";
        ConfigurationSection config = arenaConfig.getConfigurationSection(basePath);
        List<ItemStack> items = new ArrayList<>();
        if(config != null){

            Map<String, Object> objects = config.getValues(false);

            for(Map.Entry<String, Object> entry : objects.entrySet()){
                if(entry.getValue() instanceof Integer){
                    Material material = Material.getMaterial(entry.getKey());
                    if(material != null){
                        ItemStack item = new ItemStack(material, (Integer) entry.getValue());
                        items.add(item);
                    }
                }
            }
        }

        return items;
    }
    public void deleteStarterItem(String arenaName, String item, Player player){
        String basePath = "arenas." + arenaName;
        String itemPath = basePath + ".start-items";




        tryLogging(() -> {
            ConfigurationSection configurationSection = configManager.getArenaConfig();
            Map<String, Object> startItems = configManager.getArenaConfig().getConfigurationSection(itemPath).getValues(false);

            String cleanedItem = parseDeleteItem(item);

            if(startItems.containsKey(cleanedItem)){
                startItems.remove(cleanedItem);
                player.sendMessage("Deleted: " + cleanedItem + " from starting items.");
            }


            configurationSection.set(itemPath, null);

            configurationSection.createSection(itemPath, startItems);

            configManager.saveArenaConfig();

            updateInMemoryStartingItemList(arenaName);

        }, "An error occurred while removing starter item from arena.yml");
    }
    public void setLootItem (String arenaName, String item, int amount, float rarity, Player player){
        String basePath = "arenas." + arenaName;
        String lootItemPath = basePath + ".loot-items.";

        tryLogging(() -> {
            ConfigurationSection configurationSection = configManager.getArenaConfig();

            configurationSection.set(lootItemPath + item + ".amount", amount);
            configurationSection.set(lootItemPath + item + ".rarity", rarity);
            configManager.saveArenaConfig();

            updateInMemoryLootItemList(arenaName);

            player.sendMessage("Added: " + amount + " " + item + " with an rarity of: " + rarity + " to loot items.");
        }, "An error occurred while adding loot item to arena.yml");
    }
    private void updateInMemoryLootItemList(String arenaName){
        List<LootItem> updatedItems = getLootingItems(arenaName);
        setLootItems(arenaName, updatedItems);
    }
    private void setLootItems(String arenaName, List<LootItem> items){
        this.lootItemsMap.put(arenaName, items);
    }
    public List<LootItem> getLootItems(String arenaName){
        if(this.lootItemsMap.get(arenaName) == null){
            return getLootingItems(arenaName);
        } else return this.lootItemsMap.get(arenaName);
    }

    public List<LootItem> getLootingItems(String arenaName){
        FileConfiguration arenaConfig = configManager.getArenaConfig();
        String basePath = "arenas." + arenaName + ".loot-items";
        ConfigurationSection config = arenaConfig.getConfigurationSection(basePath);
        List<LootItem> items = new ArrayList<>();
        if(config != null){

            Map<String, Object> objects = config.getValues(false);

            for(Map.Entry<String, Object> entry : objects.entrySet()){
                if(entry.getValue() instanceof ConfigurationSection itemSection){
                    Material material = Material.getMaterial(entry.getKey());
                    if(material != null){
                        int amount = itemSection.getInt("amount");
                        float rarity = (float) itemSection.getDouble("rarity");
                        LootItem lootItem = new LootItem(new ItemStack(material, amount), rarity);
                        items.add(lootItem);
                    }
                }
            }
        }

        return items;
    }
    public void deleteLootItem(String arenaName, String item, Player player){
        String basePath = "arenas." + arenaName;
        String lootItemPath = basePath + ".loot-items";




        tryLogging(() -> {
            ConfigurationSection configurationSection = configManager.getArenaConfig();
            Map<String, Object> lootItems = configManager.getArenaConfig().getConfigurationSection(lootItemPath).getValues(false);

            String cleanedItem = parseDeleteItem(item);

            if(lootItems.containsKey(cleanedItem)){
                lootItems.remove(cleanedItem);
                player.sendMessage("Deleted: " + cleanedItem + " from loot items.");
            }


            configurationSection.set(lootItemPath, null);

            configurationSection.createSection(lootItemPath, lootItems);

            configManager.saveArenaConfig();

            updateInMemoryLootItemList(arenaName);

        }, "An error occurred while removing loot item from arena.yml");
    }
    public void spawnLootItems(Inventory inv, String arenaName){

        List<LootItem> lootItems = getLootItems(arenaName);
        // Checks if the list of loot items is empty
        if(lootItems.isEmpty()){
            Bukkit.broadcastMessage("Error, there is no loot!");
            Bukkit.broadcastMessage("Tell the admin to check the arena config!");
            return;
        }

        // Since Worldedit can save the contents of chest, this method clears the inventory to avoid problems.
        inv.clear();
        // Shuffles the loot item lists, and chooses a random amount of items from that list to add to the loot chest.
        Collections.shuffle(getLootItems(arenaName));
        List<ItemStack> loot = new ArrayList<>();
        //int itemsInChest = 1 + random.nextInt(getLootItems(arenaName).size());

        for(LootItem lootItem : lootItems){
            float spawnProbability = 1.0f / lootItem.getRarity();
            if(Math.random() <= spawnProbability){
                loot.add(lootItem.getItemStack());
            }
        }

        if(loot.isEmpty()){
            Bukkit.broadcastMessage("The loot chest was empty, trying again");
            spawnLootItems(inv, arenaName);
        } else {
            for(ItemStack item : loot){
                HashMap<Integer, ItemStack> unfittedItems = inv.addItem(item);

                if(!unfittedItems.isEmpty()){
                    Bukkit.broadcastMessage("Couldn't fit all the items, amount of items left: " + unfittedItems.size());
                }
            }
        }

    }
    private String parseDeleteItem(String item){
        String[] rawString = item.split("\\{");
        String uncleanString = rawString[1];
        String[] cleanerStringArr = uncleanString.split("x");
        String cleanerString = cleanerStringArr[0];

        return cleanerString.replace(" ", "");
    }
    public void setBlacklistedBlocks(String arenaName, String block, Player player){
        String basePath = "arenas." + arenaName;
        String blockPath = basePath + ".blacklisted-blocks";

        if(block.contains("minecraft")){
            String[] blockarr = block.split(":");
            block = blockarr[1].toUpperCase();
        } else if (block.contains("ItemStack")) {
            block = parseDeleteItem(block);
        }

        String finalBlock = block;

        List<String> compareBlacklistedBlocks = new ArrayList<>();
        List<ItemStack> oldBlacklistedBlocks = getBlacklistedBlocks(arenaName);
        String itemName;
        for(ItemStack item : oldBlacklistedBlocks){
            itemName = item.toString();

            if(itemName.contains("ItemStack")){
                itemName = parseDeleteItem(itemName);
            }
            else if(itemName.contains("minecraft")){

                String[] blockarr = itemName.split(":");
                itemName = blockarr[1].toUpperCase();

            }

            compareBlacklistedBlocks.add(itemName);

        }

        List<String> newBlacklistedBlocks = new ArrayList<>(compareBlacklistedBlocks);
        newBlacklistedBlocks.add(finalBlock);

        tryLogging(() ->{
            ConfigurationSection configurationSection = configManager.getArenaConfig();

            configurationSection.set(blockPath, null);
            configurationSection.set(blockPath, newBlacklistedBlocks);

            configManager.saveArenaConfig();

            updateBlacklistedBlocks(arenaName);

            player.sendMessage("Added: " + finalBlock + " to blacklisted blocks.");
        }, "An error occurred while adding blacklisted block to arena.yml");
    }
    private void updateBlacklistedBlocks(String arenaName){
        List<ItemStack> updatedBlacklist = getBlacklistedBlocks(arenaName);
        blacklistedBlocksMap.put(arenaName, updatedBlacklist);
    }
    public List<ItemStack> getBlacklistedBlocks(String arenaName) {
        FileConfiguration arenaConfig = configManager.getArenaConfig();
        String basePath = "arenas." + arenaName + ".blacklisted-blocks";
        List<String> blockNames = arenaConfig.getStringList(basePath);
        List<ItemStack> blocks = new ArrayList<>();

        if (blockNames.isEmpty()) {
            Bukkit.broadcastMessage("Couldn't find the blacklisted blocks list or the list is empty.");
            return null;
        }

        for (String blockName : blockNames) {

            if(blockName.contains("minecraft")){
                String[] blocknameArr = blockName.split(":");
                blockName = blocknameArr[1].toUpperCase();
            } else if (blockName.contains("ItemStack")) {
                blockName = parseDeleteItem(blockName);
            }

            Material material = Material.getMaterial(blockName);
            if (material != null) {
                ItemStack block = new ItemStack(material);
                blocks.add(block);
            } else {
                Bukkit.broadcastMessage("Invalid material name in blacklisted blocks: " + blockName);
            }
        }
        return blocks;
    }
    public void deleteBlacklistedItem(String arenaName, String block, Player player){
        String basePath = "arenas." + arenaName + ".blacklisted-blocks";

        tryLogging(() -> {
            ConfigurationSection configurationSection = configManager.getArenaConfig();

            // Map<String, Object> blacklistedBlocks = configManager.getArenaConfig().getConfigurationSection(basePath).getValues(false);
            List<String> blacklistedBlocks = configManager.getArenaConfig().getStringList(basePath);

            String cleanedItem = parseDeleteItem(block);

            if(blacklistedBlocks.contains(cleanedItem)){
                blacklistedBlocks.remove(cleanedItem);
                player.sendMessage("Deleted: " + cleanedItem + " from blacklisted blocks.");
            }

            configurationSection.set(basePath, null);

            configurationSection.set(basePath, blacklistedBlocks);

            configManager.saveArenaConfig();

            updateBlacklistedBlocks(arenaName);

        }, "An error occurred while removing blacklisted block from arena.yml");

    }
    public void setMinPlayers(String arenaName, int minPlayers) {
        Arena arena = getArena(arenaName);
        String basePath = "arenas." + arena.getName() + ".players";
        tryLogging(()->{

            configManager.getArenaConfig().set(basePath + ".minPlayers", minPlayers);
            configManager.saveArenaConfig();

        }, "An error occurred while setting the minimum amount of players.");

    }
    public void setMaxPlayers(String arenaName, int maxPlayers) {
        Arena arena = getArena(arenaName);
        String basePath = "arenas." + arena.getName() + ".players";
        configManager.getArenaConfig().set(basePath + ".maxPlayers", maxPlayers);
        configManager.saveArenaConfig();
    }
    public void setGracePeriod(String arenaName, int seconds){
        Arena arena = getArena(arenaName);
        String basePath = "arenas." + arena.getName() + ".timeValues";

        ConfigurationSection configurationSection = configManager.getArenaConfig();
        configurationSection.set(basePath + ".gracePeriod", seconds);

        configManager.saveArenaConfig();
    }
    public void setRiseTime(String arenaName, int seconds){
        Arena arena = getArena(arenaName);
        String basePath = "arenas." + arena.getName() + ".timeValues";

        ConfigurationSection configurationSection = configManager.getArenaConfig();
        configurationSection.set(basePath + ".lavadelay", seconds);

        configManager.saveArenaConfig();
    }
    public void setPvpMode(boolean value){
        String mode = String.valueOf(value);
        String basePath = "Global settings.";
        ConfigurationSection configurationSection = configManager.getArenaConfig();

        String fullPath = basePath + "PvPMode";
        configurationSection.set(fullPath, mode);

        configManager.saveArenaConfig();
    }
    public Boolean getPvpMode(){
        String basePath = "Global settings.";
        ConfigurationSection configurationSection = configManager.getArenaConfig();
        String mode = configurationSection.get("Global settings.PvPMode").toString();
        if(mode.equalsIgnoreCase("true")){
            return true;
        } else if (mode.equalsIgnoreCase("false")) {
            return false;
        }
        return null;
    }
    public void tryLogging(Runnable action, String errorMessage){
        try {
            action.run();
        } catch (Exception e){
            plugin.getLogger().log(Level.SEVERE, errorMessage, e);
        }
    }
    public void deleteArena(CommandSender sender, String arenaName){

        if(!(arenaNames.containsKey(arenaName))){
            sender.sendMessage("This arena: " + arenaName +  " doesn't exist.");
            return;
        }

        String basePath = "arenas." + arenaName;

        configManager.getArenaConfig().set(basePath,null);
        configManager.getSpawnPointConfig().set(basePath, null);
        worldEditAPI.deleteSchematic(sender, arenaName, "both");
        /*worldeditAPI.deleteSchematic(sender, arenaName, "arena");
        worldeditAPI.deleteSchematic(sender, arenaName, "lobby");*/
        arenaNames.remove(arenaName);
        configManager.saveArenaConfig();
        configManager.saveSpawnPointConfig();

    }
    private void saveLocationToConfig(String path, Location location) {
        configManager.getArenaConfig().set(path + ".x", location.getBlockX());
        configManager.getArenaConfig().set(path + ".y", location.getBlockY());
        configManager.getArenaConfig().set(path + ".z", location.getBlockZ());
    }
    public void addPlayerToArena(String arenaName, Player player) {
        Arena arena = arenaNames.get(arenaName);
        if (arena != null && arena.getPlayers().size() < getMaxPlayers(arenaName)) {
            arena.addPlayer(player);
            // Additional logic for adding player to arena can be implemented here
        } else if (arena != null && arena.getPlayers().size() >= getMaxPlayers(arenaName)) {
            player.sendMessage("Arena is full.");
        }
    }
    public void removePlayerFromArena(String arenaName, Player player) {
        Arena arena = arenaNames.get(arenaName);
        if (arena != null) {
            arena.removePlayer(player);
            openLootchests.remove(player.getUniqueId());
            isGameOver(arenaName);
        }
    }
    public void stopLootChestPlacement(String arenaName, Player player){
        Arena arena = arenaNames.get(arenaName);
        if (arena != null) {
            arena.removePlayer(player);
            openLootchests.remove(player.getUniqueId());
        }
    }
    public void isGameOver(String arenaName){

        Arena arena = this.getArena(arenaName);

        if(arena.getPlayers().size() < 2) {

            arena.cancelLavaTask();

            Player winner = arena.getPlayers().iterator().next();

            healPlayer(winner);
            restorePlayerInventory(winner);

            String winnerName = winner.getName();

            teleportLobby(winner, arenaName);

            // Resets the arena
            worldEditAPI.placeSchematic(winner, arenaName);

            Bukkit.broadcastMessage("The winner is: " + winnerName);
            openLootchests.clear();
            arena.removePlayer(winner);
            arena.clearStartingPlayers();

        }

    }
    public void healArenaPlayers(String arenaName){
        Arena arena = getArena(arenaName);

        if(arena != null){
            for(Player player : arena.getPlayers()){
                double maxHealth = Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getDefaultValue();
                player.setHealth(maxHealth);
                player.setFoodLevel(20);
                player.setFireTicks(0);
            }
        }
    }
    public void healPlayer(Player player){
        double maxHealth = Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getDefaultValue();
        player.setHealth(maxHealth);
        player.setFoodLevel(20);
        player.setFireTicks(0);
    }
    public void setSurvivalGamemode(String arenaName){
        Arena arena = getArena(arenaName);
        if(arena == null) return;

        for(Player player : arena.getStartingPlayers()){
            player.setGameMode(GameMode.SURVIVAL);
        }
    }
    public void setAdventureGamemode(String arenaName){
        Arena arena = getArena(arenaName);
        if(arena == null) return;

        for(Player player : arena.getStartingPlayers()){
            player.setGameMode(GameMode.ADVENTURE);
        }
    }
    public Set<Player> getPlayersInArena(String arenaName) {
        Arena arena = arenaNames.get(arenaName);
        if(arena == null) return null;

        return new HashSet<>(arena.getPlayers());
    }
    public Set<Player> getStartingPlayersInArena(String arenaName){
        Arena arena = arenaNames.get(arenaName);
        if(arena == null) return null;

        return new HashSet<>(arena.getStartingPlayers());
    }
    public int getPlayerAmountInArena(String arenaName){
        return getPlayersInArena(arenaName).size();
    }
    public int getMinPlayers(String arenaName) {
        Arena arena = getArena(arenaName);
        String basePath = "arenas." + arena.getName() + ".players";
        return configManager.getArenaConfig().getInt(basePath + ".minPlayers", 2); // Default to 2
    }
    public int getMaxPlayers(String arenaName) {
        Arena arena = getArena(arenaName);
        String basePath = "arenas." + arena.getName() + ".players";
        return configManager.getArenaConfig().getInt(basePath + ".maxPlayers", 10); // Default to 10
    }

    public Location getLobbyPos1() {
        return lobbyPos1;
    }

    public void setLobbyPos1(Location lobbyPos1) {
        this.lobbyPos1 = lobbyPos1;
    }

    public enum GameState {
        STANDBY,
        WAITING,
        STARTING,
        GRACE,
        LAVA,
        DEATHMATCH
    }
    public Arena getArena(String arenaName) {
        return arenaNames.get(arenaName);
    }
    public List<String> getArenas() {
        List<String> arenaNames;
        ConfigurationSection arenasSection = configManager.getArenaConfig().getConfigurationSection("arenas.");
        if (arenasSection != null) {
            arenaNames = new ArrayList<>(arenasSection.getKeys(false));
        } else return null;
        return arenaNames;
    }
    public Arena findPlayerArena(Player player){
        List<String> arenaNames = getArenas();
        if(arenaNames != null){
            for(String arenaName : arenaNames){
                Arena arena = getArena(arenaName);
                if(arena != null && arena.getPlayers().contains(player)){
                    return arena;
                }
            }
        }
        return null;
    }
    public void setSpawnPoints(String arenaName, CommandSender sender){
        sender.sendMessage("Trying to find spawnpoints, please wait");
        Arena arena = getArena(arenaName);
        String basePath = "arenas." + arena.getName();
        List<Location> spawnPoints = findSpawnPoints(arenaName, sender);
        Bukkit.broadcastMessage("Trying to set spawnpoints for " + arena.getName());
        // Creating a new arena section in spawnPoint.yml

        tryLogging(() -> configManager.getSpawnPointConfig().createSection(basePath),
                "an error occurred while creating a new section in spawnPoint.yml");

        ConfigurationSection configurationSection = configManager.getSpawnPointConfig();
        int index = 1;
        for(Location loc : spawnPoints){
            String spawnPath = basePath + ".SP" + index;
            configurationSection.set(spawnPath + ".x", loc.getX());
            configurationSection.set(spawnPath + ".y", loc.getY());
            configurationSection.set(spawnPath + ".z", loc.getZ());
            index++;
        }
        configManager.saveSpawnPointConfig();
        
    }
    public boolean  areSpawnpointsSet(String arenaName){
        int numberOfSpawnpoints = 0;
        ConfigurationSection arenaSection = configManager.getArenaConfig().getConfigurationSection("arenas." + arenaName);
        if(arenaSection != null){
            for(String key : arenaSection.getKeys(false)){
                if(key.startsWith("SP")){
                    numberOfSpawnpoints++;

                }
            }
        }
        if(numberOfSpawnpoints < 150){
            plugin.getLogger().log(Level.WARNING, "Not enough spawnpoints found");
            return false;
        } else return true;
    }
    public List<Location> getSpawnPoints(String arenaName) {
        FileConfiguration spawnPointConfig = configManager.getSpawnPointConfig();
        FileConfiguration arenaConfig = configManager.getArenaConfig();
        String basePath = "arenas." + arenaName;
        ConfigurationSection spawnSection = spawnPointConfig.getConfigurationSection(basePath);

        String worldName = arenaConfig.getString("arenas." + arenaName + ".worldName");

        if(worldName == null){
            Bukkit.broadcastMessage("This worldName doesn't exist.");
            return null;
        }

        org.bukkit.World bukkitWorld = Bukkit.getWorld(worldName);

        if(bukkitWorld == null){
            return null;
        }

        List<Location> spawnPoints = new ArrayList<>();
        Set<String> keys = spawnSection.getKeys(false);
        for (String key : keys) {
            if (key.startsWith("SP")) {
                ConfigurationSection spawnsSection = spawnSection.getConfigurationSection(key);
                double x = spawnsSection.getDouble("x");
                double y = spawnsSection.getDouble("y");
                double z = spawnsSection.getDouble("z");
                // Assuming the world of the spawn points is known or can be retrieved
                Location loc = new Location(Bukkit.getWorld(worldName), x, y, z);
                spawnPoints.add(loc);
            }
        }
        return spawnPoints;
    }
    // Deprecated
    public boolean setMinYLevel(Arena arena, int i, Player player){
        String basepath = "arenas." + arena.getName();
        ConfigurationSection configurationSection = configManager.getArenaConfig();

        Clipboard clipboard = worldEditAPI.loadArenaSchematic(arena.getName(), (CommandSender) player);
        CuboidRegion region = (CuboidRegion) clipboard.getRegion();
        BlockVector3 point1 = region.getMinimumPoint();
        int minY = point1.getY();

        if(i < minY){
            i = minY;
            player.sendMessage("You have set the Ymin-level too low, Ymin has been set to: " + minY);
            configurationSection.set(basepath + ".Y-levels.Ymin", i);
            configManager.saveArenaConfig();
        } else{
            configurationSection.set(basepath + ".Y-levels.Ymin", i);
            configManager.saveArenaConfig();
            player.sendMessage("Ymin-level set to Ylevel: " + i);
        }
        return true;
    }
    // Deprecated
    public boolean setMaxYLevel(Arena arena, int i,Player player){
        String basepath = "arenas." + arena.getName();
        ConfigurationSection configurationSection = configManager.getArenaConfig();

        configurationSection.set(basepath + ".Y-levels.Ymax", i);
        configManager.saveArenaConfig();
        player.sendMessage("Ymax-level set to Ylevel: " + i);
        return true;
    }
    public List<Location> findSpawnPoints(String arenaName, CommandSender sender) {
        Arena arena = getArena(arenaName);

        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by a player.");
            return null;
        }

        Player player = (Player) sender;
        World world = player.getWorld();

        String basePath = "arenas." + arena.getName();
        String yLevelPath = basePath + ".Y-levels";
        FileConfiguration config = configManager.getArenaConfig();

        int Ymax = config.getInt(yLevelPath + ".Ymax");
        int Ymin = config.getInt(yLevelPath + ".Ymin");

        // Load the schematic and calculate the volume
        Clipboard clipboard = worldEditAPI.loadArenaSchematic(arenaName, sender);
        CuboidRegion region = (CuboidRegion) clipboard.getRegion();

        int height = Ymax - Ymin + 1;
        int length = region.getLength();
        int width = region.getWidth();
        int searchSpaceVolume = length * width * height;

        sender.sendMessage("Number of blocks to iterate through: " + searchSpaceVolume);

        // Ensure we have the region's minimum point to adjust coordinates
        BlockVector3 minPoint = region.getMinimumPoint();

        List<Location> potentialSpawnPoints = new ArrayList<>();
        List<Location> nonVailidSpawns = new ArrayList<>();
        List<Location> notSolidground = new ArrayList<>();
        List<Location> onTopOfTree = new ArrayList<>();

        // Iterate through each block within the region
        for (int x = minPoint.getBlockX(); x <= minPoint.getBlockX() + length; x++) {
            for (int z = minPoint.getBlockZ(); z <= minPoint.getBlockZ() + width; z++) {
                for (int y = Ymin; y <= Ymax; y++) {
                    Block block = world.getBlockAt(x, y, z);

                    if (isValidSpawnPoint(block)) {
                        potentialSpawnPoints.add(block.getLocation());
                    } else {
                        if (!isOnSolidGround(block)) {
                            notSolidground.add(block.getLocation());
                        } else if (isOnTopOfTree(block)) {
                            onTopOfTree.add(block.getLocation());
                        }
                        nonVailidSpawns.add(block.getLocation());
                    }
                }
            }
        }

        Collections.shuffle(potentialSpawnPoints); // Shuffle to randomize the spawn points

        // Pick up to 150 random spawn points from the potential list or all if there are fewer than 150
        List<Location> selectedSpawnPoints = potentialSpawnPoints.stream()
                .limit(150)
                .collect(Collectors.toList());

        // Notify the user about the number of spawn points found
        if (selectedSpawnPoints.isEmpty()) {
            sender.sendMessage("Couldn't find any spawn points.");
            sender.sendMessage("Non valid spawns found: " + nonVailidSpawns.size());
            sender.sendMessage("Not solid spawns found: " + notSolidground.size());
            sender.sendMessage("On top of trees spawns found: " + onTopOfTree.size());
        } else if (selectedSpawnPoints.size() < 150) {
            sender.sendMessage("Unable to find 150 valid spawn points. Found only " + selectedSpawnPoints.size());
            sender.sendMessage("Non valid spawns found: " + nonVailidSpawns.size());
            sender.sendMessage("Not solid spawns found: " + notSolidground.size());
            sender.sendMessage("On top of trees spawns found: " + onTopOfTree.size());
        } else {
            sender.sendMessage("Found 150 spawn points.");
        }

        return selectedSpawnPoints;
    }
    private boolean isValidSpawnPoint(Block feetBlock) {
        Block groundBlock = feetBlock.getRelative(BlockFace.DOWN);
        Block headBlock = feetBlock.getRelative(BlockFace.UP);
        Block aboveHeadBlock = headBlock.getRelative(BlockFace.UP);

        // The block at the feet should be air, the ground should be solid, and there should be air at head and above head levels.
        return feetBlock.getType() == Material.AIR && groundBlock.getType().isSolid()
                && !isOnTopOfTree(groundBlock) && headBlock.getType() == Material.AIR
                && aboveHeadBlock.getType() == Material.AIR;
    }
    private boolean isOnSolidGround(Block block) {
        Block belowBlock = block.getRelative(BlockFace.DOWN);
        return belowBlock.getType().isSolid();
    }
    private boolean isOnTopOfTree(Block block) {
        Material type = block.getType();
        return type.toString().endsWith("_LOG") || type.toString().endsWith("_LEAVES");
    }
    public void randomArenaTeleport (String arenaName, List<Location> spawnPoints){
        Arena arena = getArena(arenaName);
        Set<Player> players = arena.getStartingPlayers();
        List<Location> availableSpawns = spawnPoints.stream()
                .filter(spawn -> !usedSpawns.contains(spawn))
                .toList();
        if (availableSpawns.isEmpty()) {
            Bukkit.broadcastMessage("No available spawn points.");
            return;
        }

        Random random = new Random();

        for(Player player : players){

            int randomIndex = random.nextInt(availableSpawns.size());
            Location randomSpawnpoint = availableSpawns.get(randomIndex);

            player.teleport(randomSpawnpoint);
            usedSpawns.add(randomSpawnpoint);

        }

    }
    public void checkOnlinePlayers(String arenaName){
        Set<Player> onlinePlayers = new HashSet<>(Bukkit.getOnlinePlayers());

        for(Player startingPlayer : getStartingPlayersInArena(arenaName)){
            if(!onlinePlayers.contains(startingPlayer)){
                arena.removeStartingPlayer(startingPlayer);
            }
        }
        for(Player activePlayer : getPlayersInArena(arenaName)){
            if(!onlinePlayers.contains(activePlayer)){
                arena.removePlayer(activePlayer);
            }
        }
    }
    public void teleportLobby(CommandSender sender, String arenaName) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by a player.");
            return;
        }

        Player player = (Player) sender;
        World world = player.getWorld();

        Arena arena = getArena(arenaName);
        if (arena == null) {
            sender.sendMessage("Arena not found.");
            return;
        }

        Clipboard clipboard = worldEditAPI.loadLobbySchematic(arenaName, sender);
        if (clipboard == null) {
            sender.sendMessage("Failed to load schematic.");
            return;
        }

        CuboidRegion region = (CuboidRegion) clipboard.getRegion();
        BlockVector3 center = region.getCenter().toBlockPoint();

        // Find the highest non-air block at the center
        int highestY = world.getHighestBlockYAt(center.getX(), center.getZ());
        Location teleportLocation = new Location(world, center.getX(), highestY + 1, center.getZ());

        player.teleport(teleportLocation);
        sender.sendMessage("Teleported to the lobby.");
    }
    public boolean checkYlevels(String arenaName){
        Arena arena = getArena(arenaName);
        String basepath = "arenas." + arena.getName();
        ConfigurationSection configurationSection = configManager.getArenaConfig();
        Object objectminY = configurationSection.get(basepath + ".Y-levels.Ymin");
        Object objectmaxY = configurationSection.get(basepath + ".Y-levels.Ymax");

        if(objectminY instanceof String || objectmaxY instanceof String){
            return false;
        } else return objectminY instanceof Integer && objectmaxY instanceof Integer;

    }
    public void setWorld(String arenaName, World world){
        Arena arena = getArena(arenaName);
        String basePath = "arenas." + arena.getName();
        String worldName = world.getName();


        ConfigurationSection configurationSection = configManager.getArenaConfig();
        configurationSection.set(basePath + ".worldName", worldName);

        Bukkit.broadcastMessage("Saving worldname as: " + worldName);

        configManager.saveArenaConfig();

    }
    public void storeAndClearPlayersInventory(String arenaName) {

        for(Player player : getStartingPlayersInArena(arenaName)){
            UUID playerId = player.getUniqueId();

            // Store items
            List<ItemStack> items = new ArrayList<>();
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null) {
                    items.add(item);
                }
            }
            playerItems.put(playerId, items);

            // Store experience
            int exp = player.getTotalExperience();
            playerExp.put(playerId, exp);

            // Clear inventory and experience
            player.getInventory().clear();
            player.setLevel(0);
            player.setExp(0);
            player.setTotalExperience(0);
        }

    }
    public void restorePlayerInventory(Player player) {

        UUID playerId = player.getUniqueId();

        player.getInventory().clear();

        // Restore items
        List<ItemStack> items = playerItems.get(playerId);
        if (items != null) {
            for (ItemStack item : items) {
                player.getInventory().addItem(item);
            }
            playerItems.remove(playerId);
        }

        // Restore experience
        Integer exp = playerExp.get(playerId);
        if (exp != null) {
            player.setTotalExperience(exp);
            playerExp.remove(playerId);
        }


    }
    public String getConfigValue(String arenaName, String configItem){
        String configItemWithPrefix = "." + configItem;
        String basePath = "arenas." + arenaName + configItemWithPrefix;

        return configManager.getArenaConfig().getString(basePath, "none");
    }

    public Location getLocationFromConfig(String arenaName, String area, String position){

        String path = area.equalsIgnoreCase("arena") ? "arenas." + arenaName + ".arena." + position : "arenas." + arenaName + ".lobby." + position; // Path is defined based on which area is set when method is called.
        FileConfiguration arenaConfig = configManager.getArenaConfig();

        if(arenaConfig.getConfigurationSection(path) == null){
            Bukkit.broadcastMessage("Error couldn't load: " + path);
            return null;
        }

        ConfigurationSection areaSection = arenaConfig.getConfigurationSection(path);

        Arena arena = getArena(arenaName);
        if(arena == null) return null;
        World world = BukkitAdapter.adapt(worldEditAPI.loadWorld(arenaName)); // worldEditAPI.loadWorld() returns worldEdit world and not Bukkit world, so it needs to be adapted into a bukkit world.

        double posX = areaSection.getDouble(".x");
        double posY = areaSection.getDouble(".y");
        double posZ = areaSection.getDouble(".z");

        return new Location(world, posX, posY, posZ);

    }

    public int getLootChestsAmount(String arenaName){
        String path = "arenas." + arenaName + ".lootchest-locations.";
        FileConfiguration arenaConfig = configManager.getArenaConfig();
        ConfigurationSection config = arenaConfig.getConfigurationSection(path);

        if(config == null){
            return 0;
        }

        Map<String, Object> objects = config.getValues(false);

        String objectToString;
        String pos = "pos";
        int amount = 0;

        for(Map.Entry<String, Object> entry : objects.entrySet()){
            objectToString = entry.toString();
            if(objectToString.contains(pos)){
                amount++;
            } else {
                Bukkit.broadcastMessage("Error, couldn't find lootchest location");
            }

        }

        return amount;

    }

    public void setLootChestLocation(String arenaName, int posIndex, Location location, Player player){


        ConfigurationSection configurationSection = configManager.getArenaConfig();
        String path = "arenas." + arenaName + ".lootchest-locations.";

        configurationSection.set(path + "pos" + posIndex + ".x", location.getBlockX());
        configurationSection.set(path + "pos" + posIndex + ".y", location.getBlockY());
        configurationSection.set(path + "pos" + posIndex + ".z", location.getBlockZ());

        tryLogging(configManager::saveArenaConfig, "Error, couldn't save the location of the lootchest.");
    }

    private Location getArenaLocationFromYml(String arenaName, String pos){
        String path = "arenas." + arenaName + ".arena";
        FileConfiguration arenaConfig = configManager.getArenaConfig();
        if(arenaConfig.getConfigurationSection(path) == null) return null;

        World world = BukkitAdapter.adapt(worldEditAPI.loadWorld(arenaName));
        ConfigurationSection arenaLocationConfig = arenaConfig.getConfigurationSection(path);
        if(arenaLocationConfig == null) return null;

        ConfigurationSection positionConfig = arenaLocationConfig.getConfigurationSection(pos);
        if(positionConfig == null) return null;

        double posX = positionConfig.getDouble("x");
        double posY = positionConfig.getDouble("y");;
        double posZ = positionConfig.getDouble("z");;

        return new Location(world, posX, posY, posZ);

    }

    public Location getArenaLocation(String arenaName, String pos){
        if(arenaPos1 == null){
            this.arenaPos1 = getArenaLocationFromYml(arenaName, pos);
        }
        if(arenaPos2 == null){
            this.arenaPos2 = getArenaLocationFromYml(arenaName, pos);
        }

        return pos.equalsIgnoreCase("pos1") ? arenaPos1 : arenaPos2;
    }

    private Location getLobbyLocationFromYml(String arenaName, String pos){
        String path = "arenas." + arenaName + ".lobby";
        FileConfiguration arenaConfig = configManager.getArenaConfig();
        if(arenaConfig.getConfigurationSection(path) == null) return null;

        World world = BukkitAdapter.adapt(worldEditAPI.loadWorld(arenaName));
        ConfigurationSection lobbyLocationConfig = arenaConfig.getConfigurationSection(path);
        if(lobbyLocationConfig == null) return null;

        ConfigurationSection positionConfig = lobbyLocationConfig.getConfigurationSection(pos);
        if(positionConfig == null) return null;

        double posX = positionConfig.getDouble("x");
        double posY = positionConfig.getDouble("y");;
        double posZ = positionConfig.getDouble("z");;

        return new Location(world, posX, posY, posZ);

    }

    public Location getLobbyLocation(String arenaName, String pos){
        if(lobbyPos1 == null){
            this.lobbyPos1 = getLobbyLocationFromYml(arenaName, pos);
        }
        if(lobbyPos2 == null){
            this.lobbyPos2 = getLobbyLocationFromYml(arenaName, pos);
        }

        return pos.equalsIgnoreCase("pos1") ? lobbyPos1 : lobbyPos2;
    }

    public List <Location> getLootChestLocations(String arenaName, Player player){

        String path = "arenas." + arenaName + ".lootchest-locations";
        FileConfiguration arenaConfig = configManager.getArenaConfig();

        if(arenaConfig.getConfigurationSection(path) == null) return new ArrayList<>();

        ConfigurationSection lootchestsConfig = arenaConfig.getConfigurationSection(path);

        World world = BukkitAdapter.adapt(worldEditAPI.loadWorld(arenaName));

        List<Location> lootchestLocations = new ArrayList<>();

        double posX;
        double posY;
        double posZ;

        if (lootchestsConfig == null){
            player.sendMessage("Error, couldn't load: " + path);
            return new ArrayList<>();
        }
        ConfigurationSection lootPosSection;
        Location lootchestLocation;
        Set<String> locations = lootchestsConfig.getKeys(false);
        for(String location : locations){
            if(location.startsWith("pos")){
                lootPosSection = lootchestsConfig.getConfigurationSection(location);
                posX = lootPosSection.getDouble("x");
                //player.sendMessage("X coordinate: " + posX);
                posY = lootPosSection.getDouble("y");
                //player.sendMessage("Y coordinate: " + posY);
                posZ = lootPosSection.getDouble("z");
                //player.sendMessage("Z coordinate: " + posZ);

                if(posX != 0.0 && posY != 0.0 && posZ != 0.0){
                    lootchestLocation = new Location(world, posX, posY, posZ);
                    lootchestLocations.add(lootchestLocation);

                    //player.sendMessage("Lootchest location: " + lootchestLocation);
                }
            }

        }
        return lootchestLocations.isEmpty() ? new ArrayList<>() : lootchestLocations;

    }

    public List<Location> findLootChestLocations(Player player){
        String path = "arenas.";
        FileConfiguration arenaConfig = configManager.getArenaConfig();
        if(arenaConfig.getConfigurationSection(path) == null) return new ArrayList<>();
        ConfigurationSection config = arenaConfig.getConfigurationSection(path);
        if(config == null) return new ArrayList<>();

        World world;
        Set<String> arenas = config.getKeys(false);
        Set<String> locations;
        ConfigurationSection arenaSection;
        String worldName;
        ConfigurationSection lootChestLocationsSection;
        ConfigurationSection positions;

        double x;
        double y;
        double z;

        Location lootChestLocation;
        List<Location> lootChestLocations = new ArrayList<>();

        for(String arena : arenas){
            arenaSection = config.getConfigurationSection(arena);

            if(arenaSection == null) return new ArrayList<>();
            worldName = arenaSection.getString(".worldName");

            if(worldName == null){
                return new ArrayList<>();
            }

            world = Bukkit.getWorld(worldName);

            lootChestLocationsSection = config.getConfigurationSection("." + arena + ".lootchest-locations");
            if(lootChestLocationsSection == null){
                continue;
            }

            locations = lootChestLocationsSection.getKeys(false);
            for(String location : locations){
                positions = lootChestLocationsSection.getConfigurationSection(location);

                if(positions == null){
                    continue;
                }

                x = positions.getDouble("x");
                y = positions.getDouble("y");
                z = positions.getDouble("z");

                lootChestLocation = new Location(world, x, y, z);
                lootChestLocations.add(lootChestLocation);
            }
        }

        if(lootChestLocations.isEmpty()){
            player.sendMessage("Couldn't find any chests");
            return new ArrayList<>();
        } else {
            player.sendMessage("Found some chests");
            return lootChestLocations;
        }
    }

    public void deleteLootChestlocation (String arenaName, Location lootChestLocation, Player player){
        String path = "arenas." + arenaName + ".lootchest-locations";
        FileConfiguration arenaConfig = configManager.getArenaConfig();

        if(arenaConfig.getConfigurationSection(path) == null) return;

        ConfigurationSection lootchestsConfig = arenaConfig.getConfigurationSection(path);

        if(lootchestsConfig == null){
            player.sendMessage("Error, couldn't load section in Arena.yml");
            return;
        }

        double x = lootChestLocation.getX();
        double y = lootChestLocation.getY();
        double z = lootChestLocation.getZ();

        boolean foundAndDeleted = false;

        ConfigurationSection lootPosSection;
        Set<String> entries = lootchestsConfig.getKeys(false);
        for(String entry : entries){
            lootPosSection = lootchestsConfig.getConfigurationSection(entry);
            if(lootPosSection == null){
                player.sendMessage("Error, couldn't load: " + entry);
                return;
            }

            if(lootPosSection.getDouble("x") == x && lootPosSection.getDouble("y") == y && lootPosSection.getDouble("z") == z){
                lootchestsConfig.set(entry, null);
                foundAndDeleted = true;
                break;
            }
        }

        if(foundAndDeleted){
            player.sendMessage("Loot chest location removed successfully");
            tryLogging(configManager::saveArenaConfig, "Error, couldn't update the Arena.yml file.");
        } else {
            player.sendMessage("No matching loot chest location found.");
        }

    }

    public void giveStartingItems(String arenaName){

        List<ItemStack> startingItems = getStarterItems(arenaName);
        for(Player player : getStartingPlayersInArena(arenaName)){
            for(ItemStack item : startingItems){
                player.getInventory().addItem(item);
            }
        }
    }
    public Integer getPlayers(String arenaName){
        Arena currentArena = getArena(arenaName);

        return currentArena.getPlayers().size();
    }
    public String getGameStage(String arenaName){
        Arena arena = getArena(arenaName);
        return arena.getGameState().toString();
    }

    private final Map<UUID , Long> lastInteract = new HashMap<>();
}

// Additional methods and utilities as necessary
