package com.sirhiggelbottom.lavaescape.plugin.API;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
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



public class WorldeditAPI {

    public void saveRegionAsSchematic(Player player, Location pos1, Location pos2, String arenaName) {
        WorldEditPlugin worldEdit = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
        if (worldEdit == null) {
            player.sendMessage("WorldEdit not found.");
            return;
        }

        File schematicDir = new File(worldEdit.getDataFolder().getParentFile(), "LavaEscape/schematics");
        if (!schematicDir.exists()) {
            schematicDir.mkdirs();
        }

        File schematicFile = new File(schematicDir, arenaName + ".schem");

        World world = BukkitAdapter.adapt(player.getWorld());
        BlockVector3 point1 = BlockVector3.at(pos1.getX(), pos1.getY(), pos1.getZ());
        BlockVector3 point2 = BlockVector3.at(pos2.getX(), pos2.getY(), pos2.getZ());

        CuboidRegion region = new CuboidRegion(world, point1, point2);
        BlockArrayClipboard clipboard = new BlockArrayClipboard(region);

        try (ClipboardWriter writer = ClipboardFormats.findByFile(schematicFile).getWriter(new FileOutputStream(schematicFile))) {
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
