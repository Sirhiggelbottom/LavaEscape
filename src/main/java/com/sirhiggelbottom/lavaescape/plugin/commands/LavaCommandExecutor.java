package com.sirhiggelbottom.lavaescape.plugin.commands;

import com.sirhiggelbottom.lavaescape.plugin.managers.ArenaMenu;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LavaCommandExecutor implements CommandExecutor {
    private final ArenaMenu arenaMenu;

    public LavaCommandExecutor(ArenaMenu arenaMenu) {
        this.arenaMenu = arenaMenu;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!isSenderPlayer(sender)){
            sender.sendMessage("Only players can execute this command!");
            return false;
        } else {
            handleMenuCommand(sender);
            return true;
        }

    }

    private boolean handleMenuCommand(CommandSender sender) {

        Player player = (Player) sender;
        player.openInventory(arenaMenu.mainMenu(player));

        return true;
    }
    private boolean isSenderPlayer(CommandSender sender){
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can execute this command.");
            return false;
        }
        return true;
    }

}

