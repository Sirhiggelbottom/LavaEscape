package com.sirhiggelbottom.lavaescape.plugin.API;


import com.sirhiggelbottom.lavaescape.plugin.Arena.Arena;
import com.sirhiggelbottom.lavaescape.plugin.LavaEscapePlugin;
import com.sirhiggelbottom.lavaescape.plugin.managers.ArenaManager;
import com.sirhiggelbottom.lavaescape.plugin.managers.ConfigManager;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;


public class WorldeditAPI {
    private final LavaEscapePlugin plugin;
    private final ArenaManager arenaManager;
    private final ConfigManager configManager;


    public WorldeditAPI(LavaEscapePlugin plugin, ArenaManager arenaManager, ConfigManager configManager){
        this.plugin = plugin;
        this.arenaManager = arenaManager;
        this.configManager = configManager;
    }


    public void saveArenaRegionAsSchematic(Player player, String arenaName, boolean reloadSchematic){

        if(arenaManager.getArena(arenaName) == null){
            player.sendMessage("Arena doesn't exist.");
            return;
        }

        Arena arena = arenaManager.getArena(arenaName);

        Location pos1;
        Location pos2;

        if(reloadSchematic){
           pos1 =  arenaManager.getLocationFromConfig(arenaName, "arena", "pos1");
           pos2 = arenaManager.getLocationFromConfig(arenaName, "arena", "pos2");

           if(pos1 == null || pos2 == null) {
               player.sendMessage("Error, couldn't load arena positions!");
               return;
           }

        } else {

            if (arena.getArenaLoc1() == null || arena.getArenaLoc2() == null) {
                player.sendMessage("Arena locations are not set.");
                return;
            } else {
                pos1 = arena.getArenaLoc1();
                pos2 = arena.getArenaLoc2();
            }

        }

        if(Bukkit.getServer().getPluginManager().getPlugin("WorldEdit") == null){
            player.sendMessage("WorldEdit not found.");
            return;
        }

        // Set or create shared directory for all arenas
        File schematicDirs = getDirectories();
        if (!schematicDirs.exists() && !schematicDirs.mkdirs()) {
            player.sendMessage("Error creating schematics directory.");
            return;
        }

        // Set or create directory for specific arena
        File schematicDir = new File(plugin.getDataFolder().getParentFile(), "LavaEscape/schematics/" + arenaName);
        if (!schematicDir.exists() && !schematicDir.mkdirs()) {
            player.sendMessage("Error creating schematic directory for: " + arenaName);
            return;
        }

        File schematicFile = new File(schematicDir, arenaName + ".schem");

        World world = loadWorld(arenaName);

        if(world != null){
            BlockVector3 point1 = BlockVector3.at(pos1.getX(), pos1.getY(), pos1.getZ());
            BlockVector3 point2 = BlockVector3.at(pos2.getX(), pos2.getY(), pos2.getZ());

            CuboidRegion region = new CuboidRegion(world, point1, point2);
            Clipboard clipboard = new BlockArrayClipboard(region);

            ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(
                    world, region, clipboard, region.getMinimumPoint());

            try {
                Operations.complete(forwardExtentCopy);
            }catch (Exception e){
                player.sendMessage("Error copying region: " + e.getMessage());
                e.printStackTrace();
            }


            try (ClipboardWriter writer = ClipboardFormats.findByAlias("sponge.3").getWriter(new FileOutputStream(schematicFile))) {
                writer.write(clipboard);
                player.sendMessage("Schematic saved as " + schematicFile.getName());
            } catch (IOException e) {
                player.sendMessage("Error saving schematic: " + e.getMessage());
                e.printStackTrace();
            }

        }else{
            Bukkit.broadcastMessage("World is not set.");
        }

    }

    public void saveLobbyRegionAsSchematic(Player player, String arenaName, boolean reloadSchematic){

        if(arenaManager.getArena(arenaName) == null){
            player.sendMessage("Arena doesn't exist.");
            return;
        }

        Arena arena = arenaManager.getArena(arenaName);

        Location pos1;
        Location pos2;

        if(reloadSchematic){
            pos1 =  arenaManager.getLocationFromConfig(arenaName, "lobby", "pos1");
            pos2 = arenaManager.getLocationFromConfig(arenaName, "lobby", "pos2");

            if(pos1 == null || pos2 == null) {
                player.sendMessage("Error, couldn't load lobby positions!");
                return;
            }

        } else {

            if (arena.getLobbyLoc1() == null || arena.getLobbyLoc2() == null) {
                player.sendMessage("lobby locations are not set.");
                return;
            } else {
                pos1 = arena.getLobbyLoc1();
                pos2 = arena.getLobbyLoc2();
            }

        }

        if(Bukkit.getServer().getPluginManager().getPlugin("WorldEdit") == null){
            player.sendMessage("WorldEdit not found.");
            return;
        }

        WorldEditPlugin worldEdit = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
        if (worldEdit == null) {
            player.sendMessage("WorldEdit not found.");
            return;
        }

        // Set or create shared directory for all arenas
        File schematicDirs = getDirectories();
        if (!schematicDirs.exists()) {
            if(!schematicDirs.mkdirs()){
                player.sendMessage("Error creating schematics directory.");
                return;
            }
        }

        // Set or create directory for specific arena
        File schematicDir = new File(plugin.getDataFolder().getParentFile(), "LavaEscape/schematics/" + arenaName);
        if (!schematicDir.exists()) {
            if(!schematicDir.mkdirs()){
                player.sendMessage("Error creating schematic directory for: " + arenaName);
                return;
            }
        }

        File schematicFile = new File(schematicDir, arenaName + "_lobby" + ".schem");

        World world = BukkitAdapter.adapt(player.getWorld());
        BlockVector3 point1 = BlockVector3.at(pos1.getX(), pos1.getY(), pos1.getZ());
        BlockVector3 point2 = BlockVector3.at(pos2.getX(), pos2.getY(), pos2.getZ());

        CuboidRegion region = new CuboidRegion(world, point1, point2);
        Clipboard clipboard = new BlockArrayClipboard(region);


        ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(
                world, region, clipboard, region.getMinimumPoint()
        );
        // configure here
        try {
            Operations.complete(forwardExtentCopy);
        }catch (Exception e){
            player.sendMessage("Error copying region: " + e.getMessage());
            e.printStackTrace();
        }



        try (ClipboardWriter writer = ClipboardFormats.findByAlias("sponge.3").getWriter(new FileOutputStream(schematicFile))) {
            writer.write(clipboard);
            player.sendMessage("Schematic saved as " + schematicFile.getName());
        } catch (IOException e) {
            player.sendMessage("Error saving schematic: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private File getDirectories() {
        return new File(plugin.getDataFolder().getParentFile(), "LavaEscape/schematics");
    }

    public void deleteSchematic(CommandSender sender, String arenaName, String area) {
        Player player = (Player) sender;
        Arena arena = arenaManager.getArena(arenaName);
        File schematicDir = new File(plugin.getDataFolder().getParentFile(), "LavaEscape/schematics/" + arenaName);
        File arenaSchematicFile = new File(schematicDir, arenaName + ".schem");
        File lobbySchematicFile = new File(schematicDir, arenaName + "_lobby" + ".schem");

        WorldEditPlugin worldEdit = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");

        if (arena == null) {
            player.sendMessage("Error: Arena not found.");
            return;
        } else if (worldEdit == null) {
            player.sendMessage("Error: WorldEdit not found.");
            return;
        } else if(!schematicDir.exists() || !schematicDir.isDirectory()){
            player.sendMessage("Error: Schematic directory not present.");
            return;
        }

        if(arenaSchematicFile.exists() && arenaSchematicFile.isFile() && lobbySchematicFile.exists() && lobbySchematicFile.isFile()){
            if(arenaSchematicFile.delete() && lobbySchematicFile.delete()){
                schematicDir.delete();
            }
        } else if(arenaSchematicFile.exists() && arenaSchematicFile.isFile() && !lobbySchematicFile.exists() && !lobbySchematicFile.isFile()){
            if(arenaSchematicFile.delete()){
                schematicDir.delete();
            }
        } else if (!arenaSchematicFile.exists() && !arenaSchematicFile.isFile() && lobbySchematicFile.exists() && lobbySchematicFile.isFile()) {
            if(lobbySchematicFile.delete()){
                schematicDir.delete();
            }
        } else {
            player.sendMessage("Error, no schematics present");
        }

    }

    public boolean doesArenaSchematicExist(String arenaName){
        File schematicDir = new File(plugin.getDataFolder().getParentFile(), "LavaEscape/schematics/" + arenaName);
        File schematicFile = new File(schematicDir, arenaName + ".schem");


        return schematicFile.exists();

    }

    public boolean doesLobbySchematicExist(String arenaName){
        File schematicDir = new File (plugin.getDataFolder().getParentFile(), "LavaEscape/schematics/" + arenaName);
        File schematicFile = new File(schematicDir, arenaName + "_lobby" + ".schem");

        return schematicFile.exists();

    }

    public Clipboard loadArenaSchematic(String arenaName, CommandSender sender) {
        File schematicDir = new File(plugin.getDataFolder().getParentFile(), "LavaEscape/schematics/" + arenaName);
        File schematicFile = new File(schematicDir, arenaName + ".schem");

        if (!schematicFile.exists()) {
            // Or handle this case as needed
            if(sender != null){
                sender.sendMessage("schematic doesn't exist");
            }
            return null; // Or handle this case as needed
        }

        Clipboard clipboard;

        ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
        try (ClipboardReader reader = format.getReader(new FileInputStream(schematicFile))){
            clipboard = reader.read();
            }catch (Exception e){
                if(sender != null){
                    sender.sendMessage("Error saving schematic: " + e.getMessage());
                }
                e.printStackTrace();
                return null;
            }

        return clipboard;

    }

    public Clipboard loadLobbySchematic(String arenaName, CommandSender sender) {
        File schematicDir = new File (plugin.getDataFolder().getParentFile(), "LavaEscape/schematics/" + arenaName);
        File schematicFile = new File(schematicDir, arenaName + "_lobby" + ".schem");

        sender.sendMessage("Trying to load: " + schematicFile);

        if (!schematicFile.exists()) {
            sender.sendMessage("schematic doesn't exist");
            return null; // Or handle this case as needed
        }

        Clipboard clipboard;

        ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
        try (ClipboardReader reader = format.getReader(new FileInputStream(schematicFile))){
            clipboard = reader.read();
        }catch (Exception e){
            sender.sendMessage("Error saving schematic: " + e.getMessage());
            e.printStackTrace();
            return null;
        }

        return clipboard;

    }

    public void placeSchematic(CommandSender sender, String arenaName) {
        File schematicDir = new File(plugin.getDataFolder().getParentFile(), "LavaEscape/schematics/" + arenaName);
        File schematicFile = new File(schematicDir, arenaName + ".schem");
        Clipboard clipboard = loadArenaSchematic(arenaName, sender);
        if (clipboard == null) {
            sender.sendMessage("Failed to load schematic.");
            return;
        }

        Player player = (Player) sender;
        World world = BukkitAdapter.adapt(player.getWorld());
        BlockVector3 point = findArenaMinimumPointNonDebug(arenaName);

        try (EditSession editSession = WorldEdit.getInstance().newEditSession(world)) {
            Operation operation = new ClipboardHolder(clipboard)
                    .createPaste(editSession)
                    .to(point)
                    .ignoreAirBlocks(false)
                    .build();
            Operations.complete(operation);
            //sender.sendMessage("Schematic: " + schematicFile + " successfully pasted at: " + point.getX() + " " + point.getY() + " " + point.getZ());
        } catch (Exception e) {
            sender.sendMessage("Error when pasting schematic: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private BlockVector3 findArenaMinimumPoint(CommandSender sender,String arenaName){
        Arena arena = arenaManager.getArena(arenaName);
        Player player = (Player) sender;
        World world = BukkitAdapter.adapt(player.getWorld());
        String basePath = "." + arena.getName();
        ConfigurationSection arenaSection = configManager.getArenaConfig().getConfigurationSection("arenas");

        int pos1X = arenaSection.getInt(basePath + ".arena.pos1.x");
        int pos1Y = arenaSection.getInt(basePath + ".arena.pos1.y");
        int pos1Z = arenaSection.getInt(basePath + ".arena.pos1.z");

        int pos2X = arenaSection.getInt(basePath + ".arena.pos2.x");
        int pos2Y = arenaSection.getInt(basePath + ".arena.pos2.y");
        int pos2Z = arenaSection.getInt(basePath + ".arena.pos2.z");

        BlockVector3 point1 = BlockVector3.at(pos1X, pos1Y, pos1Z);
        BlockVector3 point2 = BlockVector3.at(pos2X, pos2Y, pos2Z);

        CuboidRegion region = new CuboidRegion(world, point1, point2);

        return region.getMinimumPoint();
    }

    public World loadWorld(String arenaName){
        Arena arena = arenaManager.getArena(arenaName);
        String basePath = "." + arena.getName();
        ConfigurationSection arenaSection = configManager.getArenaConfig().getConfigurationSection("arenas");

        if(arenaSection != null){
            String worldName = arenaSection.getString(basePath + ".worldName");

            if(worldName == null){
                Bukkit.broadcastMessage("This worldName doesn't exist.");
                return null;
            }

            org.bukkit.World bukkitWorld = Bukkit.getWorld(worldName);

            if(bukkitWorld == null){
                return null;
            }

            return BukkitAdapter.adapt(bukkitWorld);
        }
        else {
            return null;
        }
    }

    public BlockVector3 findArenaMinimumPointNonDebug(String arenaName){
        Arena arena = arenaManager.getArena(arenaName);
        String basePath = "." + arena.getName();
        ConfigurationSection arenaSection = configManager.getArenaConfig().getConfigurationSection("arenas");

        if(arenaSection != null){

            World world = loadWorld(arenaName);

            int pos1X = arenaSection.getInt(basePath + ".arena.pos1.x");
            int pos1Y = arenaSection.getInt(basePath + ".arena.pos1.y");
            int pos1Z = arenaSection.getInt(basePath + ".arena.pos1.z");

            int pos2X = arenaSection.getInt(basePath + ".arena.pos2.x");
            int pos2Y = arenaSection.getInt(basePath + ".arena.pos2.y");
            int pos2Z = arenaSection.getInt(basePath + ".arena.pos2.z");

            BlockVector3 point1 = BlockVector3.at(pos1X, pos1Y, pos1Z);
            BlockVector3 point2 = BlockVector3.at(pos2X, pos2Y, pos2Z);

            CuboidRegion region = new CuboidRegion(world, point1, point2);

            return region.getMinimumPoint();
        } else {
            return null;
        }

    }
    public BlockVector3 findArenaMaximumPointNonDebug(String arenaName){
        Arena arena = arenaManager.getArena(arenaName);
        String basePath = "." + arena.getName();
        ConfigurationSection arenaSection = configManager.getArenaConfig().getConfigurationSection("arenas");

        String worldName = arenaSection.getString(basePath + ".worldName");

        if(worldName == null){
            Bukkit.broadcastMessage("This worldName doesn't exist.");
            return null;
        }

        org.bukkit.World bukkitWorld = Bukkit.getWorld(worldName);

        if(bukkitWorld == null){
            return null;
        }

        World world = BukkitAdapter.adapt(bukkitWorld);

        int pos1X = arenaSection.getInt(basePath + ".arena.pos1.x");
        int pos1Y = arenaSection.getInt(basePath + ".arena.pos1.y");
        int pos1Z = arenaSection.getInt(basePath + ".arena.pos1.z");

        int pos2X = arenaSection.getInt(basePath + ".arena.pos2.x");
        int pos2Y = arenaSection.getInt(basePath + ".arena.pos2.y");
        int pos2Z = arenaSection.getInt(basePath + ".arena.pos2.z");

        BlockVector3 point1 = BlockVector3.at(pos1X, pos1Y, pos1Z);
        BlockVector3 point2 = BlockVector3.at(pos2X, pos2Y, pos2Z);

        CuboidRegion region = new CuboidRegion(world, point1, point2);

        return region.getMaximumPoint();
    }

}
