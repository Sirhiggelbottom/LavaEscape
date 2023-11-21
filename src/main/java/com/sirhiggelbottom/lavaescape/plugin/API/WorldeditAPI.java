package com.sirhiggelbottom.lavaescape.plugin.API;

import com.sirhiggelbottom.lavaescape.plugin.Arena.Arena;
import com.sirhiggelbottom.lavaescape.plugin.LavaEscapePlugin;
import com.sirhiggelbottom.lavaescape.plugin.managers.ArenaManager;
import com.sirhiggelbottom.lavaescape.plugin.managers.ConfigManager;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;


public class WorldeditAPI {
    private final LavaEscapePlugin plugin;
    private final ConfigManager configManager;
    private final ArenaManager arenaManager;

    public WorldeditAPI(LavaEscapePlugin plugin, ConfigManager configManager,ArenaManager arenaManager){
        this.plugin = plugin;
        this.configManager = configManager;
        this.arenaManager = arenaManager;
    }
    /*public void saveRegionAsSchematic(Player player, Location pos1, Location pos2, String arenaName) {
        player.sendMessage("Trying to save schematic");
        WorldEditPlugin worldEdit = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
        if (worldEdit == null) {
            player.sendMessage("WorldEdit not found.");
            return;
        }

        arenaManager.tryLogging(configManager::worldEditDIR,"Error when trying to create schematics directory");


        File schematicDir = new File(plugin.getDataFolder().getParentFile(), "LavaEscape/schematics");
        File schematicFile = new File(schematicDir, arenaName + ".schem");




        World world = BukkitAdapter.adapt(player.getWorld());
        BlockVector3 point1 = BlockVector3.at(pos1.getX(), pos1.getY(), pos1.getZ());
        BlockVector3 point2 = BlockVector3.at(pos2.getX(), pos2.getY(), pos2.getZ());

        CuboidRegion region = new CuboidRegion(world, point1, point2);
        Clipboard clipboard = new BlockArrayClipboard(region);

        try (ClipboardWriter writer = Objects.requireNonNull(ClipboardFormats.findByFile(schematicFile)).getWriter(new FileOutputStream(schematicFile))) {
            writer.write(clipboard);
            player.sendMessage("Schematic saved as " + schematicFile.getName());
        } catch (IOException e) {
            player.sendMessage("Error saving schematic: " + e.getMessage());
            e.printStackTrace();
        }

    }*/

    public void saveRegionAsSchematic(Player player, String arenaName) {
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


        try (ClipboardWriter writer = BuiltInClipboardFormat.SPONGE_SCHEMATIC.getWriter(new FileOutputStream(schematicFile))) {
            writer.write(clipboard);
            player.sendMessage("Schematic saved as " + schematicFile.getName());
        } catch (IOException e) {
            player.sendMessage("Error saving schematic: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public ClipboardHolder loadSchematic(String arenaName) {
        File schematicDir = new File(Bukkit.getPluginManager().getPlugin("LavaEscape").getDataFolder(), "schematics");
        File schematicFile = new File(schematicDir, arenaName + ".schem");

        if (!schematicFile.exists()) {
            return null; // Or handle this case as needed
        }

        try (ClipboardReader reader = ClipboardFormats.findByFile(schematicFile).getReader(new FileInputStream(schematicFile))) {
            return new ClipboardHolder(reader.read());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
