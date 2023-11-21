package com.sirhiggelbottom.lavaescape.plugin.commands;

import com.sirhiggelbottom.lavaescape.plugin.Arena.Arena;
import com.sirhiggelbottom.lavaescape.plugin.LavaEscapePlugin;
import com.sirhiggelbottom.lavaescape.plugin.managers.ArenaManager;
import com.sirhiggelbottom.lavaescape.plugin.managers.ConfigManager;
import com.sirhiggelbottom.lavaescape.plugin.events.GameEvents;
import com.sirhiggelbottom.lavaescape.plugin.API.WorldeditAPI;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class LavaCommandExecutor implements CommandExecutor, TabCompleter {
    private final LavaEscapePlugin plugin;
    private final ArenaManager arenaManager;
    private final ConfigManager configManager;
    private final GameEvents gameEvents;
    private final WorldeditAPI worldeditAPI;


    public LavaCommandExecutor(LavaEscapePlugin plugin, GameEvents gameEvents, ConfigManager configManager, ArenaManager arenaManager, WorldeditAPI worldeditAPI) {
        this.plugin = plugin;
        this.arenaManager = arenaManager;
        this.configManager = configManager;
        this.gameEvents = gameEvents;
        this.worldeditAPI = worldeditAPI;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            return false;
        }
/* Command hierarchy:
            arg[0]   arg[1]   arg[2]        arg[3]        arg[4]
   /Lava -> arena -> arenaName -> start
                  -> arenaName -> stop
                  -> arenaName -> delete
                  -> arenaName -> config  -> minplayers -> int
                                         -> maxplayers -> int
                                         -> mode -> string
                                         -> set-area -> arena
                                                     -> lobby
                                         -> miny     -> int
                                         -> maxy     -> int
         -> create -> arenaName
         -> reload
         -> list
         -> help
         -> lwand
         -> join -> arenaName
         -> leave -> arenaName
         */
        switch (args[0].toLowerCase()) {
            case "arena":
                switch(args[2].toLowerCase()){
                    case "start":
                    case "stop":
                        return handleGameControlCommand(sender, args);



                    case "delete":
                        return handleDeleteCommand(sender, args);


                    case "config":

                        switch (args[3].toLowerCase()){
                            case "minplayers":
                            case "maxplayers":
                                return handlePlayerLimitsCommand(sender, args);


                            case "miny":
                            case "maxy":
                                return handleYlevelCommand(sender, args);


                            case "mode":
                                switch (args[4].toLowerCase()){
                                    case "competitive":
                                    case "server":
                                        return handleModeCommand(sender, args);

                                    default:
                                        break;
                                }


                            case "set-area":
                                switch (args[4].toLowerCase()){
                                    case "arena":
                                    case "lobby":
                                        return handleAreaCommand(sender, args);

                                    default:
                                        break;
                                }
                        }break;
                }break;
            case "create":
                return handleCreateCommand(sender, args);

            case "reload":
                return handleReloadCommand(sender);

            case "list":
                return handleListCommand(sender);

            case "help":
                return handleHelpCommand(sender , args);

            case "lwand":
                return handleLwandCommand(sender);

            case "join":
            case "leave":
                return handleJoinLeaveCommand(sender, args);

            default:
                return false;
        }return true;
    }

    private boolean handleYlevelCommand(CommandSender sender, String[] args) {

        Player player = (Player) sender;

        String arenaName = args[1];
        if (!(sender instanceof Player)) {
            player.sendMessage("Only players can set arena and lobby areas.");
            return true;
        }
        if (!player.hasPermission("lavaescape.admin")) {
            player.sendMessage("You do not have permission to set min/max y-level.");
            return true;
        }
        switch (args[3].toLowerCase()){
            case "miny":
                return arenaManager.setMinYLevel(arenaManager.getArena(arenaName),argToInt(sender,args[4]),player);
            case "maxy":
                return arenaManager.setMaxYLevel(arenaManager.getArena(arenaName),argToInt(sender,args[4]),player);
            default:
                break;
        }
        return true;
    }

    private boolean handleDeleteCommand(CommandSender sender, String[] args) {
        List<String> arenaNames = arenaManager.getArenaS();

        if (args.length < 2) {
            sender.sendMessage("Usage: /lava delete <name>");
            return true;
        }

        if(arenaNames == null){
            sender.sendMessage("There are no arenas");
            return true;
        }

        String arenaName = args[1];
        if (arenaManager.getArena(arenaName) == null) {
            sender.sendMessage("This arena doesn't exist.");
            return true;
        }
        arenaManager.deleteArena(arenaName);
        sender.sendMessage("Arena '" + arenaName + "' has been deleted.");
        return true;

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
        String arenaName = args[1];
        if (args.length < 2) {
            sender.sendMessage("Usage: /lava create <name>");
            return true;
        }

        if (arenaManager.getArena(arenaName) != null) {
            sender.sendMessage("An arena with this name already exists.");
            sender.sendMessage("This was an error message from the create command.");
            return true;
        }
        sender.sendMessage("trying to create arena");
        arenaManager.createArena(arenaName);
        configManager.worldEditDIR();
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
            player.sendMessage("Usage: /lava <name> arena or lobby");
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

        switch (args[4].toLowerCase()) {
            case "arena":
                arena.setArenaLocations(pos1 , pos2);
                arenaManager.saveTheArena(arena);
                worldeditAPI.saveRegionAsSchematic(player,arenaName);

                player.sendMessage(arenaName + " arena area set");
                return true;

            case "lobby":
                arena.setLobbyLocations(pos1 , pos2);
                arenaManager.saveTheLobby(arena);
                player.sendMessage(arenaName + " lobby area set");
                return true;

            default:
                player.sendMessage("Invalid area type. Use 'arena' or 'lobby'.");
                return false;
        }



    }




    // Implementation for /Lava <name> minplayers <int> and /Lava <name> maxplayers <int>
    private boolean handlePlayerLimitsCommand(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("Usage: /lava <name> minplayers or maxplayers <int>");
            return true;
        }
        // Logic to set player limits for an arena
        return true;
    }

    // Implementation for /Lava <name> mode competitive and /Lava <name> mode server
    private boolean handleModeCommand(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("Usage: /lava <name> mode competitive or server");
            return true;
            /*If mode == competitive then set gameMode to competitive mode
              if mode == server then set gameMode to server mode*/
        }
        // Logic to set game mode for an arena
        return true;
    }

    // Implementation for /Lava <name> start and /Lava <name> stop
    private boolean handleGameControlCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("Usage: /lava <name> start or stop");
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
            sender.sendMessage("Usage: /lava <name> join or leave");
            return true;
        }
        // Logic for player joining or leaving an arena
        return true;
    }

    private boolean handleHelpCommand(CommandSender sender , String[] args) {
        if(sender.hasPermission("lavaescape.admin")){
            sender.sendMessage(this.getListOfAdminCommands(sender , args).toString());
        } else {
            sender.sendMessage(this.getListOfPlayerCommands().toString());
        }
        return true;
    }

    /* Command hierarchy:
            arg[0]   arg[1]   arg[2]        arg[3]        arg[4]
   /Lava -> arena -> start -> arenaName
                  -> stop -> arenaName
                  -> create -> arenaName
                  -> delete -> arenaName
                  -> config -> arenaName -> minplayers -> int
                                         -> maxplayers -> int
                                         -> mode -> string
                                         -> set-area -> arena
                                                     -> lobby
                                         -> miny     -> int
                                         -> maxy     -> int
         -> reload
         -> list
         -> help
         -> lwand
         -> join -> arenaName
         -> leave -> arenaName
         */

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

        if(sender.hasPermission("lavaescape.admin")){
            return getListOfAdminCommands(sender, args);
        }

        if (args.length == 2 && !(sender.hasPermission("lavaescape.admin"))) {
            switch (args[0].toLowerCase()) {
                case "list":
                case "join":
                case "leave":
                    return arenaManager.getArenaS();
                default:
                    break;
            }
        }
        return Collections.emptyList();
    }
    private int argToInt(CommandSender sender, String arg){
        sender.sendMessage("Number is: " + Integer.parseInt(arg));
        return Integer.parseInt(arg);
    }
    private int argLength(CommandSender sender ,String[] args){
        //sender.sendMessage("argLength debug message: method called");
        return args.length;
    }
    /* Command hierarchy:
            arg[0]   arg[1]   arg[2]        arg[3]        arg[4]
   /Lava -> arena -> start -> arenaName
                  -> stop -> arenaName
                  -> create -> arenaName
                  -> delete -> arenaName
                  -> config -> arenaName -> minplayers -> int
                                         -> maxplayers -> int
                                         -> mode -> string
                                         -> set-area -> arena
                                                     -> lobby
                                         -> miny     -> int
                                         -> maxy     -> int
         -> reload
         -> list
         -> help
         -> lwand
         -> join -> arenaName
         -> leave -> arenaName
         */
    private List<String> getListOfAdminCommands(CommandSender sender, String[] args) {
        List<String> commands = new ArrayList<>();
        //sender.sendMessage("Admin list debug message: Method called");

        switch (argLength(sender , args)){
            case 1:
//                sender.sendMessage("Admin list debug message: argLength case 1 started");
                commands.add("arena");
                commands.add("reload");
                commands.add("create");
                commands.add("delete");
                commands.add("list");
                commands.add("help");
                commands.add("lwand");
                commands.add("join");
                commands.add("leave");
                break;
            case 2:
//                sender.sendMessage("Admin list debug message: argLength case 2 started");
                switch (args[0].toLowerCase()){
                    case "arena":
                    case "reload":
                    case "join":
                    case "leave":
                    case "delete":
                        return arenaManager.getArenaS();

                    default:
                        break;
                }

                break;

            case 3:
//                sender.sendMessage("Admin list debug message: argLength case 3 started");

                boolean matchFound = arenaManager.getArenaS().stream().anyMatch(item -> item.equalsIgnoreCase(args[1]));

                if (matchFound) {
                        commands.add("start");
                        commands.add("stop");
                        commands.add("config");

                }
                break;

            case 4:
//                sender.sendMessage("Admin list debug message: argLength case 4 started");
                if(args[2].equalsIgnoreCase("config")){
                    commands.add("minplayers");
                    commands.add("maxplayers");
                    commands.add("mode");
                    commands.add("set-area");
                    commands.add("miny");
                    commands.add("maxy");
                }
                break;
            case 5:

                switch (args[3].toLowerCase()){
                    case "set-area":
                        commands.add("arena");
                        commands.add("lobby");
                        break;
                    case "mode":
                        commands.add("competitive");
                        commands.add("server");
                        break;
                    default:
                        break;
                }

            default:
                break;
        }
        return commands;
    }

    private List<String> getListOfPlayerCommands(){
        List<String> commands = new ArrayList<>();
        commands.add("list");
        commands.add("join");
        commands.add("leave");
        commands.add("help");
        return commands;
    }
    // Additional helper methods and logic as necessary
}

