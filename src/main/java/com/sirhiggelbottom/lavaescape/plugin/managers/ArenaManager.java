package com.sirhiggelbottom.lavaescape.plugin.managers;

import com.sirhiggelbottom.lavaescape.plugin.API.WorldeditAPI;
import com.sirhiggelbottom.lavaescape.plugin.Arena.Arena;
import com.sirhiggelbottom.lavaescape.plugin.LavaEscapePlugin;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class ArenaManager {
    private final LavaEscapePlugin plugin;
    private final ConfigManager configManager;
    private final Map<String, Arena> arenaNames;
    private final WorldeditAPI worldeditAPI;
    private List<Location> usedSpawns;

    private final Arena arena;

    public ArenaManager(LavaEscapePlugin plugin, ConfigManager configManager, Arena arena) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.arena = arena;
        this.arenaNames = new HashMap<>();
        this.worldeditAPI = new WorldeditAPI(plugin, this, configManager);
        usedSpawns = new ArrayList<>();
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

    private Arena loadArena(String arenaName) {
        ConfigurationSection section = configManager.getArenaConfig().getConfigurationSection("arenas." + arenaName);
        if (section == null) return null;

        // Load arena and lobby locations
        Location arenaLoc1 = getLocationFromSection(section, "arena.pos1");
        Location arenaLoc2 = getLocationFromSection(section, "arena.pos2");
        Location lobbyLoc1 = getLocationFromSection(section, "lobby.pos1");
        Location lobbyLoc2 = getLocationFromSection(section, "lobby.pos2");

        return new Arena(arenaName, arenaLoc1, arenaLoc2, lobbyLoc1, lobbyLoc2);
    }

    private Location getLocationFromSection(ConfigurationSection section, String path) {
        World world = plugin.getServer().getWorld(section.getString(path + ".world"));
        int x = section.getInt(path + ".x");
        int y = section.getInt(path + ".y");
        int z = section.getInt(path + ".z");
        return new Location(world, x, y, z);
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
        saveLocationToConfig(basePath + ".lobby.pos2", arena.getLobbyLoc2());
        configManager.saveArenaConfig();
    }

    public void saveNewArena(Arena arena, String worldName){
        // Setting arena path in arena.yml file
        String basePath = "arenas." + arena.getName();

        // Creating a new arena section in arena.yml
        tryLogging(() -> configManager.getArenaConfig().createSection(basePath),
                "an error occurred while creating a new section in arena.yml");

        // Sets the arena name
        tryLogging(() -> configManager.getArenaConfig().set(basePath + ".name", arena.getName()),
                "an error occurred while assigning a name to new arena in arena.yml");

        // Setting placeholder values for the locations and Y-levels to "none"
        tryLogging(() ->{
            ConfigurationSection configurationSection = configManager.getArenaConfig();
            configurationSection.set(basePath + ".arena.pos1", "none");
            configurationSection.set(basePath + ".arena.pos2", "none");
            configurationSection.set(basePath + ".lobby.pos1", "none");
            configurationSection.set(basePath + ".lobby.pos2", "none");
            configurationSection.set(basePath + ".Y-levels.Ymin","none");
            configurationSection.set(basePath + ".Y-levels.Ymax","none");

            // Set min and max players to default values
            configurationSection.set(basePath + ".minPlayers", 2);
            configurationSection.set(basePath + ".maxPlayers", 10);

            // Set the World name
            configurationSection.set(basePath + ".worldName", worldName);
            // Set the lavadelay to default 5 seconds.
            configurationSection.set(basePath + ".lavadelay", 5);

        }, "an error occurred while assigning placeholder values to new arena in arena.yml");

        // Saving the arena config to arena.yml
        tryLogging(configManager::saveArenaConfig,
                "an error occurred while saving the arena config to arena.yml");

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
            return;
        }

        arenaNames.remove(arenaName);

        String basePath = "arenas." + arenaName;

        configManager.getArenaConfig().set(basePath,null);
        worldeditAPI.deleteSchematic(sender, arenaName, "arena");
        worldeditAPI.deleteSchematic(sender, arenaName, "lobby");
        configManager.saveArenaConfig();

    }
    private void saveLocationToConfig(String path, Location location) {
        configManager.getArenaConfig().set(path + ".world", location.getWorld().getName());
        configManager.getArenaConfig().set(path + ".x", location.getBlockX());
        configManager.getArenaConfig().set(path + ".y", location.getBlockY());
        configManager.getArenaConfig().set(path + ".z", location.getBlockZ());
    }

    public void changeGameState(String arenaName, GameState newState) {
        Arena arena = arenaNames.get(arenaName);
        if (arena != null) {
            arena.setGameState(newState);
            // Additional logic for game state change can be implemented here
        }
    }

    public void addPlayerToArena(String arenaName, Player player) {
        Arena arena = arenaNames.get(arenaName);
        if (arena != null) {
            arena.addPlayer(player);
            // Additional logic for adding player to arena can be implemented here
        }
    }

    public void removePlayerFromArena(String arenaName, Player player) {
        Arena arena = arenaNames.get(arenaName);
        if (arena != null) {
            arena.removePlayer(player);
            // Additional logic for removing player from arena can be implemented here
        }
    }

    public Set<Player> getPlayersInArena(String arenaName) {
        Arena arena = arenaNames.get(arenaName);
        if (arena != null) {
            return new HashSet<>(arena.getPlayers());
        }
        return new HashSet<>();
    }

    // Set minimum players for an arena
    public void setMinPlayers(String arenaName, int minPlayers) {
        Arena arena = getArena(arenaName);
        String basePath = "arenas." + arena.getName();
        configManager.getArenaConfig().set(basePath + ".minPlayers", minPlayers);
        configManager.saveArenaConfig();
    }

    // Get minimum players for an arena
    public int getMinPlayers(String arenaName) {
        Arena arena = getArena(arenaName);
        String basePath = "arenas." + arena.getName();
        return configManager.getArenaConfig().getInt(basePath + ".minPlayers", 2); // Default to 2
    }

    public void setMaxPlayers(String arenaName, int minPlayers) {
        Arena arena = getArena(arenaName);
        String basePath = "arenas." + arena.getName();
        configManager.getArenaConfig().set(basePath + ".maxPlayers", minPlayers);
        configManager.saveArenaConfig();
    }

    // Get minimum players for an arena
    public int getMaxPlayers(String arenaName) {
        Arena arena = getArena(arenaName);
        String basePath = "arenas." + arena.getName();
        return configManager.getArenaConfig().getInt(basePath + ".maxPlayers", 10); // Default to 10
    }



    public enum GameState {
        WAITING,
        STARTING,
        GRACE,
        LAVA,
        DEATHMATCH
    }
    public Arena getArena(String arenaName) {
        return arenaNames.get(arenaName);
    }
    public List<String> getArenaS() {
        List<String> arenaNames;
        ConfigurationSection arenasSection = configManager.getArenaConfig().getConfigurationSection("arenas.");
        if (arenasSection != null) {
            arenaNames = new ArrayList<>(arenasSection.getKeys(false));
        } else return null;
        return arenaNames;
    }

    public void setSpawnPoints(String arenaName, CommandSender sender){
        sender.sendMessage("Trying to find spawnpoints, please wait");
        Arena arena = getArena(arenaName);
        String basePath = "arenas." + arena.getName();
        List<Location> spawnPoints = findSpawnPoints(arenaName, sender);

        // Creating a new arena section in spawnPoint.yml
        tryLogging(() -> configManager.getSpawnPointConfig().createSection(basePath),
                "an error occurred while creating a new section in spawnPoint.yml");

        /*// Sets the arena name
        tryLogging(() -> configManager.getSpawnPointConfig().set(basePath + ".name", arena.getName()),
                "an error occurred while assigning a name to new arena in spawnPoint.yml");*/

        int index = 1;
        for(Location loc : spawnPoints){
            String spawnPath = basePath + ".SP" + index;
            configManager.getSpawnPointConfig().set(spawnPath + ".x", loc.getX());
            configManager.getSpawnPointConfig().set(spawnPath + ".y", loc.getY());
            configManager.getSpawnPointConfig().set(spawnPath + ".z", loc.getZ());
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
        ConfigurationSection arenaSection = arenaConfig.getConfigurationSection(basePath);
        assert arenaSection != null;
        String worldName = arenaSection.getString( basePath + ".worldName");
        Bukkit.broadcastMessage("Worldname is: " + worldName);


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

    public boolean setMinYLevel(Arena arena, int i, Player player){
        String basepath = "arenas." + arena.getName();
        ConfigurationSection configurationSection = configManager.getArenaConfig();

        Clipboard clipboard = worldeditAPI.loadArenaSchematic(arena.getName(), (CommandSender) player);
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
        FileConfiguration config = configManager.getArenaConfig();



        int Ymax = config.getInt(basePath + ".Y-levels.Ymax");
        int Ymin = config.getInt(basePath + ".Y-levels.Ymin");

        // Load the schematic and calculate the volume
        Clipboard clipboard = worldeditAPI.loadArenaSchematic(arenaName, sender);
        CuboidRegion region = (CuboidRegion) clipboard.getRegion();



        // Adjust the volume for the Y-level range
        int height = Ymax - Ymin + 1;
        int length = region.getLength();
        int width = region.getWidth();
        int searchSpaceVolume = length * width * height; // Adjusted volume for Y-range

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
                    Block space1 = world.getBlockAt(x, y + 1, z);
                    Block space2 = world.getBlockAt(x, y + 2, z);

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

        Set<Player> players = arena.getPlayers();
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

        Clipboard clipboard = worldeditAPI.loadLobbySchematic(arenaName, sender);
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

    public void setLavaDelay (String arenaName, int seconds){
        Arena arena = getArena(arenaName);
        String basePath = "arenas." + arena.getName();

        ConfigurationSection configurationSection = configManager.getArenaConfig();
        configurationSection.set(basePath + ".lavadelay", seconds);

        configManager.saveArenaConfig();
    }

}

// Additional methods and utilities as necessary

