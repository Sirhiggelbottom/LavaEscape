package com.sirhiggelbottom.lavaescape.plugin.commands;

import com.sirhiggelbottom.lavaescape.plugin.Arena.Arena;
import com.sirhiggelbottom.lavaescape.plugin.managers.ArenaManager;
import com.sirhiggelbottom.lavaescape.plugin.managers.ArenaMenu;
import com.sirhiggelbottom.lavaescape.plugin.managers.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
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

        /*if(args.length == 0){
            sendUsageMessage(player);
            return true;
        } else if (args.length == 1 && !args[0].equalsIgnoreCase("menu")) {
            sendUsageMessage(player);
            return true;
        }*/

        if(args[0].equalsIgnoreCase("help")){
            sendUsageMessage(player);
            return true;
        } else if(isAdmin && args[0].equalsIgnoreCase("menu")){
            handleMenuCommand(sender);
            return true;
        } else if(args[0].equalsIgnoreCase("join")){
            handleJoinCommand(sender, args[1]);
            return true;
        } else if (args[0].equalsIgnoreCase("leave")) {
            handleLeaveCommand(sender, args[1]);
            return true;
        } else if(isAdmin && args.length == 2 && args[0].equalsIgnoreCase("leftover") && arenaManager.getArenas().contains(args[1])){
            handleLeftoverCommand(player, args[1]);
            return true;
        } else if(isAdmin && args.length == 2 && args[0].equalsIgnoreCase("reloadP") && arenaManager.getArenas().contains(args[1])){
            handleReloadPCommand(sender, args[1]);
            return true;
        } else {
            sendUsageMessage(player);
            return true;
        }


    }

    private void sendUsageMessage(Player player) {
        boolean isAdmin = player.hasPermission("lavaescape.admin");

        if (isAdmin) {
            // Usage messages for admins
            player.sendMessage(ChatColor.GOLD + ("LavaEscape Commands:"));
            player.sendMessage(ChatColor.YELLOW + "/lava menu " + ChatColor.GRAY + "- Opens the admin menu.");
            player.sendMessage(ChatColor.YELLOW + "/lava join <arena> " + ChatColor.GRAY + "- Join an arena.");
            player.sendMessage(ChatColor.YELLOW + "/lava leave <arena> " + ChatColor.GRAY + "- Leave the arena.");
            player.sendMessage(ChatColor.YELLOW + "/lava leftover <arena> " + ChatColor.GRAY + "- Lists players that aren't in a lobby.");
            player.sendMessage(ChatColor.YELLOW + "/lava reloadP <arena> " + ChatColor.GRAY + "- Reloads player amount properties.");
            player.sendMessage(ChatColor.YELLOW + "/lava help " + ChatColor.GRAY + "- Lists LavaEscape commands.");
        } else {
            // Usage messages for normal players
            player.sendMessage(ChatColor.GOLD + ("LavaEscape Commands:"));
            player.sendMessage(ChatColor.YELLOW + "/lava join <arena> " + ChatColor.GRAY + "- Join an arena.");
            player.sendMessage(ChatColor.YELLOW + "/lava leave <arena> " + ChatColor.GRAY + "- Leave the arena.");
            player.sendMessage(ChatColor.YELLOW + "/lava help " + ChatColor.GRAY + "- Lists LavaEscape commands.");
        }
    }

    private void handleReloadPCommand(CommandSender sender, String arg) {
        arenaManager.reloadPlayerLimit((Player) sender, arg);
    }

    private void handleLeftoverCommand(Player player, String arg) {
        Set<Player> onlinePlayers = new HashSet<>(Bukkit.getOnlinePlayers());
        Set<Player> playersInLobby = new HashSet<>(arenaManager.getPlayersInArena(arg));
        Set<UUID> lobbyUUID = new HashSet<>();

        for(Player playerInlobby : playersInLobby){
            UUID playerId = playerInlobby.getUniqueId();
            lobbyUUID.add(playerId);
        }

        List<String> leftoverPlayers = new ArrayList<>();
        UUID playerId;

        for(Player onlinePlayer : onlinePlayers){
            playerId = onlinePlayer.getUniqueId();

            if(!lobbyUUID.contains(playerId)){
                leftoverPlayers.add(onlinePlayer.toString());
            }
        }

        if(!leftoverPlayers.isEmpty()){
            List<String> message = new ArrayList<>();
            String filterString;
            String[] part1;


            for (String leftoverPlayer : leftoverPlayers) {
                part1 = leftoverPlayer.split("\\{name=");
                filterString = part1[1];

                part1 = filterString.split("}]");
                filterString = part1[0];

                message.add(filterString);
            }
            player.sendMessage(message.toString());
        }

    }

    private void handleLeaveCommand(CommandSender sender, String arenaName) {
        if(!isSenderPlayer(sender)) return;
        if(arenaManager.getArena(arenaName) == null) return;

        Arena arena = arenaManager.getArena(arenaName);
        Player player = (Player) sender;

        if(arenaManager.getPlayersInArena(arenaName).contains(player)){

            if(arena.getGameState().toString().equals("STANDBY") || arena.getGameState().toString().equals("WAITING")){

                arenaManager.removePlayerFromLobby(arena.getName(), player);

            } else {

                arenaManager.removePlayerFromArena(arena.getName(), player);
                arenaManager.teleportLobby(player, arena.getName());
                arenaManager.healPlayer(player);
                arenaManager.restorePlayerInventory(player);
                player.setGameMode(GameMode.ADVENTURE);

            }

        }

    }

    private void handleJoinCommand(CommandSender sender, String arenaName) {
        if(!isSenderPlayer(sender)) return;

        Player player = (Player) sender;

        if(arenaManager.getPlayersInArena(arenaName).contains(player)){
            player.sendMessage("You have already joined the lobby, the match will start soon.");
            return;
        }

        if(arenaManager.getGameStage(arenaName).equals("STANDBY") || arenaManager.getGameStage(arenaName).equals("WAITING")){
            arenaManager.addPlayerToArena(arenaName, player);
            arenaManager.teleportLobby(player, arenaName);
            gameManager.isGameReady(arenaName);
        } else {
            player.sendMessage("The game has already started, wait for the next one");
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

        List<String> commands = new ArrayList<>();

        if(sender.hasPermission("lavaescape.admin")){
            commands.add("menu");
            commands.add("join");
            commands.add("leave");
            commands.add("leftover");
            commands.add("reloadP");
            commands.add("help");
        } else {
            commands.add("join");
            commands.add("leave");
            commands.add("help");
        }

        if(args.length == 1){
            return commands;
        }

        if (args.length == 2 && (args[0].equalsIgnoreCase("join") || args[0].equalsIgnoreCase("leave")) ) {
            return arenaManager.getArenas();
        } else if(sender.hasPermission("lavaescape.admin") && args.length == 2 && args[0].equalsIgnoreCase("leftover")){
            return arenaManager.getArenas();
        } else if(sender.hasPermission("lavaescape.admin") && args.length == 2 && args[0].equalsIgnoreCase("reloadP")){
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

