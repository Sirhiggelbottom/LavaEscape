package com.sirhiggelbottom.lavaescape.plugin.commands;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class commandLocationWand implements CommandExecutor {
    public String arena;

    public commandLocationWand(String arena) {
        this.arena = arena;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player){
            if (args[0].equalsIgnoreCase("setarena")){
                String arena = args[1];

                ItemStack stick = new ItemStack(Material.STICK);
                player.getInventory().addItem(stick);
                player.sendMessage("Use this to set the corners of the arena");
            } else {
                sender.sendMessage("You're not a player");
            }
        }
        return true;
    }
}
