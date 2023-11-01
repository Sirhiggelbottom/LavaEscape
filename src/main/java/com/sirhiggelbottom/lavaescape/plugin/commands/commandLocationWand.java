
package com.sirhiggelbottom.lavaescape.plugin.commands;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;

public class commandLocationWand implements CommandExecutor {

    public commandLocationWand() {
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        Player player = (Player) sender;

        // Create a custom item (location wand stick)
        ItemStack locationWand = new ItemStack(Material.STICK, 1);
        ItemMeta meta = locationWand.getItemMeta();

        // Set a unique name and lore to distinguish it from regular sticks
        assert meta != null;
        meta.setDisplayName("Location Wand");
        meta.setLore(Collections.singletonList("A magical wand for setting locations"));
        locationWand.setItemMeta(meta);

        // Give the custom location wand stick to the player
        player.getInventory().addItem(locationWand);
        player.sendMessage("You received a Location Wand.");

        return true;
    }
}


