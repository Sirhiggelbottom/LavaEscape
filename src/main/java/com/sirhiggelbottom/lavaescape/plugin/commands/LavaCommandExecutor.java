package com.sirhiggelbottom.lavaescape.plugin.commands;

import com.sirhiggelbottom.lavaescape.plugin.managers.ArenaManager;
import com.sirhiggelbottom.lavaescape.plugin.managers.ArenaMenu;
import com.sirhiggelbottom.lavaescape.plugin.managers.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;

public class LavaCommandExecutor implements CommandExecutor, TabCompleter {
    private final ArenaManager arenaManager;
    private final GameManager gameManager;
    private final ArenaMenu arenaMenu;

    public LavaCommandExecutor(ArenaManager arenaManager, GameManager gameManager ,ArenaMenu arenaMenu) {
        this.arenaManager = arenaManager;
        this.gameManager = gameManager;
        this.arenaMenu = arenaMenu;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(!isSenderPlayer(sender)){
            sender.sendMessage("Only players can execute this command!");
            return false;
        }

        Player player = (Player) sender;
        boolean isAdmin = player.hasPermission("lavaescape.admin");

        if(args[0].equalsIgnoreCase("menu")){
            handleMenuCommand(sender);
            return true;
        } else if(args[0].equalsIgnoreCase("join")){
            handleJoinCommand(sender, args[1]);
            return true;
        } else if(isAdmin && args.length == 2 && args[0].equalsIgnoreCase("leftover") && arenaManager.getArenas().contains(args[1])){
            handleLeftoverCommand(player, args[1]);
        }

        return false;

    }

    private void handleLeftoverCommand(Player player, String arg) {
        Set<Player> onlinePlayers = new HashSet<>(Bukkit.getOnlinePlayers());
        Set<Player> playersInLobby = new HashSet<>(arenaManager.getPlayersInArena(arg));
        Set<UUID> lobbyUUID = new HashSet<>();

        for(Player playerInlobby : playersInLobby){
            UUID playerId = playerInlobby.getUniqueId();
            lobbyUUID.add(playerId);
        }

        Set<Player> leftoverPlayers = new HashSet<>();
        UUID playerId;

        for(Player onlinePlayer : onlinePlayers){
            playerId = onlinePlayer.getUniqueId();

            if(!lobbyUUID.contains(playerId)){
                leftoverPlayers.add(onlinePlayer);
            }
        }

        if(!leftoverPlayers.isEmpty()){
            player.sendMessage(List.of(leftoverPlayers).toString());
        }

    }

    private void handleJoinCommand(CommandSender sender, String arenaName) {
        Player player = (Player) sender;
        if(arenaManager.getGameStage(arenaName).equals("STANDBY") || arenaManager.getGameStage(arenaName).equals("WAITING")){
            arenaManager.addPlayerToArena(arenaName, player);
            arenaManager.teleportLobby(player, arenaName);
            gameManager.isGameReady(arenaName);
        }

    }

    private void handleMenuCommand(CommandSender sender) {

        Player player = (Player) sender;
        player.openInventory(arenaMenu.mainMenu(player));

    }
    private boolean isSenderPlayer(CommandSender sender){
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can execute this command.");
            return false;
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

        List<String> commands = new ArrayList<>(Arrays.asList("join", "menu"));

        if(sender.hasPermission("lavaescape.admin")){
            commands.add("leftover");
        }

        if(args.length == 1){
            return commands;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("join")) {
            return arenaManager.getArenas();
        } else if(sender.hasPermission("lavaescape.admin") && args.length == 2 && args[0].equalsIgnoreCase("leftover")){
            return arenaManager.getArenas();
        }
        return Collections.emptyList();
    }
    private int argToInt(CommandSender sender, String arg){
        return Integer.parseInt(arg);
    }
    private int argLength(CommandSender sender ,String[] args){
        //sender.sendMessage("argLength debug message: method called");
        return args.length;
    }
}

