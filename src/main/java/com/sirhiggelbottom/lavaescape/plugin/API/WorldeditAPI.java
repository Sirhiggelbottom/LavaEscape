package com.sirhiggelbottom.lavaescape.plugin.API;

import com.sirhiggelbottom.lavaescape.plugin.Arena.Arena;
import com.sirhiggelbottom.lavaescape.plugin.LavaEscapePlugin;
import com.sirhiggelbottom.lavaescape.plugin.managers.ArenaManager;
import com.sirhiggelbottom.lavaescape.plugin.managers.ConfigManager;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.*;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;


public class WorldeditAPI {
    private final LavaEscapePlugin plugin;
    private final ConfigManager configManager;
    private final ArenaManager arenaManager;

    public WorldeditAPI(LavaEscapePlugin plugin, ConfigManager configManager,ArenaManager arenaManager){
        this.plugin = plugin;
        this.configManager = configManager;
        this.arenaManager = arenaManager;
    }

    public void saveRegionAsSchematic(Player player, String arenaName){
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

        File schematicDir = new File(plugin.getDataFolder().getParentFile(), "LavaEscape/schematics");
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
//        clipboard.setOrigin(region.getMinimumPoint());

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

    public void deleteSchematic(CommandSender sender,String arenaName) {
        Player player = (Player) sender;
        Arena arena = arenaManager.getArena(arenaName);
        File schematicDir = new File(plugin.getDataFolder().getParentFile(), "LavaEscape/schematics");
        File schematicFile = new File(schematicDir, arenaName + ".schem");
        WorldEditPlugin worldEdit = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");

        if (arena == null || !schematicFile.exists() || worldEdit == null) {
            player.sendMessage("Error: Arena not found, file does not exist, or WorldEdit not found.");
            return;
        }

        if (schematicFile.delete()) {
            player.sendMessage("Schematic deleted successfully.");
        } else {
            player.sendMessage("Error: Could not delete the schematic.");
        }
    }


    public Clipboard loadSchematic(String arenaName, CommandSender sender) {
        File schematicDir = new File(plugin.getDataFolder().getParentFile(), "LavaEscape/schematics");
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

    public void placeSchematic(CommandSender sender, String arenaName) {
        File schematicDir = new File(plugin.getDataFolder().getParentFile(), "LavaEscape/schematics");
        File schematicFile = new File(schematicDir, arenaName + ".schem");
        Clipboard clipboard = loadSchematic(arenaName, sender);
        if (clipboard == null) {
            sender.sendMessage("Failed to load schematic.");
            return;
        }

        Player player = (Player) sender;
        World world = BukkitAdapter.adapt(player.getWorld());
        BlockVector3 point = findMinimumPoint(sender, arenaName);

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

    private BlockVector3 findMinimumPoint(CommandSender sender,String arenaName){
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
