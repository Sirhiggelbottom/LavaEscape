package com.sirhiggelbottom.lavaescape.plugin.Arena;

import com.sirhiggelbottom.lavaescape.plugin.managers.ArenaManager;
import com.sirhiggelbottom.lavaescape.plugin.managers.ConfigManager;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ArenaUtil {
    private final Arena arena;
    private final ArenaManager arenaManager;
    private final ConfigManager configManager;

    public ArenaUtil(Arena arena, ArenaManager arenaManager, ConfigManager configManager) {
        this.arena = arena;
        this.arenaManager = arenaManager;
        this.configManager = configManager;
    }

    public List<Location> findSpawnPoints(Clipboard clipboard, String arenaName, CommandSender sender) {
        Arena arena = arenaManager.getArena(arenaName);

        Player player = (Player) sender;
        World world = player.getWorld();

        String basePath = "arenas." + arena.getName();
        FileConfiguration config = configManager.getArenaConfig();

        int Ymax = config.getInt(basePath + ".Y-levels.Ymax");
        int Ymin = config.getInt(basePath + ".Y-levels.Ymin");

        List<Location> spawnPoints = new ArrayList<>();

        clipboard.getRegion().forEach(blockVector3 -> {
            int y = blockVector3.getBlockY();
            if (y >= Ymin && y <= Ymax) {
                Block block = world.getBlockAt(blockVector3.getBlockX(), y, blockVector3.getBlockZ());
                if (isValidSpawnPoint(block)) {
                    spawnPoints.add(block.getLocation());
                }
            }
        });

        return spawnPoints;
    }
//ToDo Add logic to figure out if a block is a valid spawnpoint
    private boolean isValidSpawnPoint(Block block) {
        // Implement checks for solid ground, not on top of trees, not inside blocks, and enough space above
        // This is pseudo-code and needs to be filled in with actual logic based on your Minecraft server's API
        return isOnSolidGround(block) && !isOnTopOfTree(block) && !isInsideBlock(block) && hasEnoughSpaceAbove(block);
    }

    // Placeholder methods for the criteria checks
    private boolean isOnSolidGround(Block block) {
        // Check if the block below is solid
        return true; // Replace with actual logic
    }

    private boolean isOnTopOfTree(Block block) {
        // Check if the block is on top of a tree
        return false; // Replace with actual logic
    }

    private boolean isInsideBlock(Block block) {
        // Check if the block is not an air block
        return false; // Replace with actual logic
    }

    private boolean hasEnoughSpaceAbove(Block block) {
        // Check if there is enough space above the block
        return true; // Replace with actual logic
    }
}

