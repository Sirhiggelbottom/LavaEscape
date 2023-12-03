package com.sirhiggelbottom.lavaescape.plugin.API;

import com.sirhiggelbottom.lavaescape.plugin.Arena.Arena;
import com.sirhiggelbottom.lavaescape.plugin.LavaEscapePlugin;
import com.sirhiggelbottom.lavaescape.plugin.managers.ArenaManager;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.*;
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
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;


public class WorldeditAPI {
    private final LavaEscapePlugin plugin;
    private final ArenaManager arenaManager;


    public WorldeditAPI(LavaEscapePlugin plugin, ArenaManager arenaManager){
        this.plugin = plugin;
        this.arenaManager = arenaManager;
    }
    public void saveArenaRegionAsSchematic(Player player, String arenaName){
        Arena arena = arenaManager.getArena(arenaName);

        if (arena == null || arena.getArenaLoc1() == null || arena.getArenaLoc2() == null) {
            player.sendMessage("Arena locations not set or arena does not exist.");
            return;
        }


        Location pos1 = arena.getArenaLoc1();
        Location pos2 = arena.getArenaLoc2();



        WorldEditPlugin worldEdit = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");



        if (worldEdit == null) {
            player.sendMessage("WorldEdit not found.");
            return;
        }



        // Set or create shared directory for all the arenas
        File schematicDirs = getDirectories();
        if (!schematicDirs.exists() && !schematicDirs.mkdirs()) {
            player.sendMessage("Error creating schematics directory.");
            return;
        }



        // Set or create directory for specific arena
        File schematicDir = new File(plugin.getDataFolder().getParentFile(), "LavaEscape/schematics/" + arenaName);
        if (!schematicDir.exists() && !schematicDir.mkdirs()) {
            player.sendMessage("Error creating schematic directory.");
            return;
        }



        File schematicFile = new File(schematicDir, arenaName + ".schem");

        World world = BukkitAdapter.adapt(player.getWorld());
        BlockVector3 point1 = BlockVector3.at(pos1.getX(), pos1.getY(), pos1.getZ());
        BlockVector3 point2 = BlockVector3.at(pos2.getX(), pos2.getY(), pos2.getZ());

        CuboidRegion region = new CuboidRegion(world, point1, point2);
        Clipboard clipboard = new BlockArrayClipboard(region);



        ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(
                world, region, clipboard, region.getMinimumPoint()
        );

        try {
            Operations.complete(forwardExtentCopy);
        }catch (Exception e){
            player.sendMessage("Error copying region: " + e.getMessage());
            e.printStackTrace();
        }


        try (ClipboardWriter writer = BuiltInClipboardFormat.SPONGE_SCHEMATIC.getWriter(new FileOutputStream(schematicFile))) {
            writer.write(clipboard);
            player.sendMessage("Schematic saved as " + schematicFile.getName());
        } catch (IOException e) {
            player.sendMessage("Error saving schematic: " + e.getMessage());
            e.printStackTrace();
        }



    }

    public void saveLobbyRegionAsSchematic(Player player, String arenaName){
        Arena arena = arenaManager.getArena(arenaName);

        if (arena == null || arena.getLobbyLoc1() == null || arena.getLobbyLoc2() == null) {
            player.sendMessage("Lobby locations not set or arena does not exist.");
            return;
        }

        Location pos1 = arena.getLobbyLoc1();
        Location pos2 = arena.getLobbyLoc2();

        WorldEditPlugin worldEdit = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
        if (worldEdit == null) {
            player.sendMessage("WorldEdit not found.");
            return;
        }
        // Set or create shared directory for all the arenas
        File schematicDirs = getDirectories();
        if (!schematicDirs.exists() && !schematicDirs.mkdirs()) {
            player.sendMessage("Error creating schematics directory.");
            return;
        }
        // Set or create directory for specific arena
        File schematicDir = new File(plugin.getDataFolder().getParentFile(), "LavaEscape/schematics/" + arenaName);
        if (!schematicDir.exists() && !schematicDir.mkdirs()) {
            player.sendMessage("Error creating schematic directory.");
            return;
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


        try (ClipboardWriter writer = BuiltInClipboardFormat.SPONGE_SCHEMATIC.getWriter(new FileOutputStream(schematicFile))) {
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

    public void deleteSchematic(CommandSender sender,String arenaName, String area) {
        Player player = (Player) sender;
        Arena arena = arenaManager.getArena(arenaName);
        File schematicDir = new File(plugin.getDataFolder().getParentFile(), "LavaEscape/schematics/" + arenaName);
        File arenaSchematicFile = new File(schematicDir, arenaName + ".schem");
        File lobbySchematicFile = new File(schematicDir, arenaName + "_lobby" + ".schem");

        WorldEditPlugin worldEdit = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");

        if (arena == null || !arenaSchematicFile.exists() || !lobbySchematicFile.exists() || worldEdit == null) {
            player.sendMessage("Error: Arena / Lobby not found, file does not exist, or WorldEdit not found.");
            return;
        }
    if(area.equalsIgnoreCase("arena")){
        if (arenaSchematicFile.delete()) {
            player.sendMessage("Schematic deleted successfully.");
        } else {
            player.sendMessage("Error: Could not delete the schematic.");
        }
    } else if(area.equalsIgnoreCase("lobby")){

        if (lobbySchematicFile.delete()) {
            player.sendMessage("Schematic deleted successfully.");
        } else {
            player.sendMessage("Error: Could not delete the schematic.");
        }
    }

    }


    public Clipboard loadArenaSchematic(String arenaName, CommandSender sender) {
        File schematicDir = new File(plugin.getDataFolder().getParentFile(), "LavaEscape/schematics/" + arenaName);
        File schematicFile = new File(schematicDir, arenaName + ".schem");

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
        BlockVector3 point = findArenaMinimumPoint(sender, arenaName);

        try (EditSession editSession = WorldEdit.getInstance().newEditSession(world)) {
            Operation operation = new ClipboardHolder(clipboard)
                    .createPaste(editSession)
                    .to(point)
                    .ignoreAirBlocks(true) // Include this if you want to paste air blocks as well
                    .build();
            Operations.complete(operation);
            sender.sendMessage("Schematic: " + schematicFile + " successfully pasted at: " + point.getX() + " " + point.getY() + " " + point.getZ());
        } catch (Exception e) {
            sender.sendMessage("Error when pasting schematic: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private BlockVector3 findArenaMinimumPoint(CommandSender sender,String arenaName){
        Arena arena = arenaManager.getArena(arenaName);
        Player player = (Player) sender;

        Location pos1 = arena.getArenaLoc1();
        Location pos2 = arena.getArenaLoc2();

        World world = BukkitAdapter.adapt(player.getWorld());
        BlockVector3 point1 = BlockVector3.at(pos1.getX(), pos1.getY(), pos1.getZ());
        BlockVector3 point2 = BlockVector3.at(pos2.getX(), pos2.getY(), pos2.getZ());

        CuboidRegion region = new CuboidRegion(world, point1, point2);

        return region.getMinimumPoint();
    }

}
