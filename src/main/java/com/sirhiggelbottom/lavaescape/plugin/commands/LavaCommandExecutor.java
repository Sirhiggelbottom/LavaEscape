package com.sirhiggelbottom.lavaescape.plugin.commands;

import com.sirhiggelbottom.lavaescape.plugin.Arena.Arena;
import com.sirhiggelbottom.lavaescape.plugin.LavaEscapePlugin;
import com.sirhiggelbottom.lavaescape.plugin.managers.ArenaManager;
import com.sirhiggelbottom.lavaescape.plugin.managers.ConfigManager;
import com.sirhiggelbottom.lavaescape.plugin.events.GameEvents;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class LavaCommandExecutor implements CommandExecutor, TabCompleter {
    private final LavaEscapePlugin plugin;
    private final ArenaManager arenaManager;
    private final ConfigManager configManager;
    private final GameEvents gameEvents;

    public LavaCommandExecutor(LavaEscapePlugin plugin, GameEvents gameEvents, ConfigManager configManager, ArenaManager arenaManager) {
        this.plugin = plugin;
        this.arenaManager = arenaManager;
        this.configManager = configManager;
        this.gameEvents = gameEvents;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            return false;
        }

        switch (args[0].toLowerCase()) {
            case "create":
                return handleCreateCommand(sender, args);
            case "lwand":
                return handleLwandCommand(sender);
            case "arena":
            case "lobby":
                return handleAreaCommand(sender, args);
            case "minplayers":
            case "maxplayers":
                return handlePlayerLimitsCommand(sender, args);
            case "mode":
                return handleModeCommand(sender, args);
            case "start":
            case "stop":
                return handleGameControlCommand(sender, args);
            case "reload":
                return handleReloadCommand(sender);
            case "join":
            case "leave":
                return handleJoinLeaveCommand(sender, args);
            case "list":
                return handleListCommand(sender);
            case "help":
                return handleHelpCommand(sender);
            default:
                return false;
        }
    }

    private boolean handleListCommand(CommandSender sender) {
        List<String> arenaNames = arenaManager.getArenaS();
        if(arenaNames == null){
        sender.sendMessage("There are no arenas");
        return true;
        }
        sender.sendMessage(arenaManager.getArenaS().toString());

        return true;
    }

    private boolean handleCreateCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("Usage: /lava create <name>");
            return true;
        }
        String arenaName = args[1];
        if (arenaManager.getArena(arenaName) != null) {
            sender.sendMessage("An arena with this name already exists.");
            return true;
        }
//        arenaManager.createOrUpdateArena(arenaName, null, null, null, null); // Placeholder for actual locations
        arenaManager.createArena(arenaName); // Placeholder for actual locations
        sender.sendMessage("Arena '" + arenaName + "' has been created.");
        return true;
    }

    // Implementation for /Lava Lwand
    private boolean handleLwandCommand(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only a player can use the wand.");
            return true;
        }

        Player player = (Player) sender;
        ItemStack wand = new ItemStack(Material.STICK, 1);
        ItemMeta meta = wand.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("Wand");
            meta.setLore(Collections.singletonList("LavaEscape Wand"));
            wand.setItemMeta(meta);
        }
        player.getInventory().addItem(wand);
        player.sendMessage("You have received the wand.");
        return true;
    }

    // Implementation for /Lava <name> arena and /Lava <name> lobby
    private boolean handleAreaCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can set arena and lobby areas.");
            return true;
        }
        Player player = (Player) sender;

        if (args.length < 3) {
            player.sendMessage("Usage: /lava <name> arena|lobby");
            return true;
        }

        String arenaName = args[1].toLowerCase();
        Arena arena = arenaManager.getArena(arenaName);

        if (arena == null) {
            player.sendMessage("Arena '" + arenaName + "' does not exist.");
            return true;
        }

        if (!player.hasPermission("lavaescape.admin.setarea")) {
            player.sendMessage("You do not have permission to set arena/lobby areas.");
            return true;
        }

        // Example method calls to get selected positions - these methods need to be implemented
        UUID playerId = player.getUniqueId();
        Location pos1 = gameEvents.getFirstPosition(playerId);
        Location pos2 = gameEvents.getSecondPosition(playerId);

        if (pos1 == null || pos2 == null) {
            player.sendMessage("You must first select two positions with the wand.");
            return true;
        }

        switch (args[2].toLowerCase()) {
            case "arena":
                if(arena.getLobbyLoc1() == null || arena.getLobbyLoc2() == null){
                    arena.setLocations(pos1, pos2, null, null);
                } else {
                    arena.setLocations(pos1, pos2, arena.getLobbyLoc1(), arena.getLobbyLoc2());
                }
                break;

            case "lobby":
                if(arena.getArenaLoc1() == null || arena.getArenaLoc2() == null){
                    arena.setLocations(null, null, pos1, pos2);
                } else{
                    arena.setLocations(arena.getArenaLoc1(), arena.getArenaLoc2(), pos1, pos2);
                }
                break;

            default:
                player.sendMessage("Invalid area type. Use 'arena' or 'lobby'.");
                return true;
        }
        if(!(arena.getArenaLoc1() == null && arena.getArenaLoc2() == null)){
            arenaManager.saveTheArena(arena);
            return true;
        } else if (!(arena.getLobbyLoc1() == null && arena.getLobbyLoc2() == null)) {
            arenaManager.saveTheLobby(arena);
            return true;
        } else if (!(arena.getArenaLoc1() == null && arena.getArenaLoc2() == null && arena.getLobbyLoc1() == null && arena.getLobbyLoc2() == null)) {
            arenaManager.saveArena(arena);
            player.sendMessage("Set the " + args[2].toLowerCase() + " area for arena '" + arenaName + "'.");
            return true;
        } else {
            sender.sendMessage("Error could not save positions");

            return true;
        }
    }




    // Implementation for /Lava <name> minplayers <int> and /Lava <name> maxplayers <int>
    private boolean handlePlayerLimitsCommand(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("Usage: /lava <name> minplayers|maxplayers <int>");
            return true;
        }
        // Logic to set player limits for an arena
        return true;
    }

    // Implementation for /Lava <name> mode competitive and /Lava <name> mode server
    private boolean handleModeCommand(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("Usage: /lava <name> mode competitive|server");
            return true;
        }
        // Logic to set game mode for an arena
        return true;
    }

    // Implementation for /Lava <name> start and /Lava <name> stop
    private boolean handleGameControlCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("Usage: /lava <name> start|stop");
            return true;
        }
        // Logic to control game start and stop
        return true;
    }

    // Implementation for /Lava reload
    private boolean handleReloadCommand(CommandSender sender) {
        if (!sender.hasPermission("lavaescape.admin.reload")) {
            sender.sendMessage("You do not have permission to reload the plugin.");
            return true;
        }
        plugin.reloadConfig(); // Assuming a method in the main class to handle reload
        sender.sendMessage("Plugin configurations reloaded.");
        return true;
    }

    // Implementation for /Lava <name> join and /Lava <name> leave
    private boolean handleJoinLeaveCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only a player can join or leave an arena.");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage("Usage: /lava <name> join|leave");
            return true;
        }
        // Logic for player joining or leaving an arena
        return true;
    }

    private boolean handleHelpCommand(CommandSender sender) {
        sender.sendMessage(this.getListOfCommands().toString());
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return getListOfCommands();
        } else if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "arena":
                case "lobby":
                case "minplayers":
                case "maxplayers":
                case "mode":
                case "start":
                case "stop":
                case "join":
                case "leave":
//                    return getListOfArenaNames();
                    return arenaManager.getArenaS();
                default:
                    break;
            }
        }
        return Collections.emptyList();
    }

    private List<String> getListOfCommands() {
        List<String> commands = new ArrayList<>();
        commands.add("create");
        commands.add("lwand");
        commands.add("arena");
        commands.add("lobby");
        commands.add("minplayers");
        commands.add("maxplayers");
        commands.add("mode");
        commands.add("start");
        commands.add("stop");
        commands.add("reload");
        commands.add("join");
        commands.add("leave");
        commands.add("list");
        commands.add("help");
        return commands;
    }
    // Additional helper methods and logic as necessary
}

