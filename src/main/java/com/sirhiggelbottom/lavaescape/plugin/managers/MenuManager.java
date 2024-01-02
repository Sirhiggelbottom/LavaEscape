package com.sirhiggelbottom.lavaescape.plugin.managers;

import com.sirhiggelbottom.lavaescape.plugin.Arena.Arena;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

//@ToDo Need to make logic that creates arena menu's based on how many arenas there are. There can only be 12 arenas per page.
// Remaining: Add logic that figures how to display arenas when there is more than 12 arenas.

public class MenuManager {
        private final ArenaManager arenaManager;
        private final List<Integer> availableArenaSlots;
        private List<String> surplusArenas;
        private Map<UUID, Integer> currentPlayerPage;
        private Map<UUID, Integer> previousPlayerPage;
        private final int amountOfArenasPerPage;
        private static List<String> pageNames;
        private Map<String, Integer> pageList;


    public MenuManager(ArenaManager arenaManager) {
        this.arenaManager = arenaManager;
        availableArenaSlots = new ArrayList<>(Arrays.asList(
                10, 12, 14, 16, 19, 21, 23, 25, 28, 30, 32, 34));
        currentPlayerPage = new HashMap<>();
        previousPlayerPage = new HashMap<>();
        surplusArenas = new ArrayList<>();
        amountOfArenasPerPage = 12;
        ArrayList<String> tempPageNames = new ArrayList<>(Arrays.asList(
                "arenaName", "Config", "Set Arena", "Set Lobby", "Min Y", "Max Y",
                "Min Players", "Max Players", "Rise time", "Grace time",
                "Starter items", "Blacklisted blocks", "Delete arena"));
        pageNames = Collections.unmodifiableList(tempPageNames);
        pageList = new HashMap<>();
    }

    public static int calculatePageId(int arenaNumber, String itemName) {
        final int firstArenaID = 4;
        final int itemsPerArena = 13;

        String[] itemNames = pageNames.toArray(new String[0]);

        // Find the index of the item name
        int itemIndex = -1;
        for (int i = 0; i < itemNames.length; i++) {
            if (itemNames[i].equals(itemName)) {
                itemIndex = i;
                break;
            }
        }

        if (itemIndex == -1) {
            // Item name not found
            return -1;
        }

        // Calculate the page ID
        return firstArenaID + (arenaNumber - 1) * itemsPerArena + itemIndex;
    }
    private void createPageList(){
        List<String> arenas = arenaManager.getArenaS();
        int arenaNumber = 1;
        if(arenas.isEmpty()){
           Bukkit.broadcastMessage("Arena list is empty, create an arena first.");
            return;
        }
        for(String arena : arenas){
            for (String itemName : pageNames) {
                int pageId = calculatePageId(arenaNumber, itemName);
                pageList.put(itemName, pageId);
            }
            arenaNumber++;
        }
    }
    public void printPages(){
        createPageList();

        if(pageList.isEmpty()){
            Bukkit.broadcastMessage("Pagelist is empty.");
            return;
        }

        for(Map.Entry<String, Integer> entry: pageList.entrySet()){
            String key = entry.getKey();
            int value = entry.getValue();
            Bukkit.broadcastMessage(key + " " + value);
        }
    }
    //@Todo: Create a method that returns the contents of pageList, either as a key / value or only as a value.
    public Map<String, Integer> getPageInfo(String key){
        Map<String, Integer> pageInfo = new HashMap<>();

        return pageInfo;
    }
    public void mainMenu(CommandSender sender){
        Player player = (Player) sender;
        List<Integer> usedSlots;
        int invSize = 27;

        // Adds slots used for buttons to an Array for protection.
        if(player.hasPermission("lavaescape.admin")){
            usedSlots = new ArrayList<>(Arrays.asList(11, 13, 15)); // 11 = List arenas, 13 = Create new arena and 15 = Exit the menu.
        } else usedSlots = new ArrayList<>(Arrays.asList(11, 15));

        Inventory inv = Bukkit.createInventory(null, invSize, ChatColor.DARK_RED.toString() + ChatColor.BOLD + "LAVA Escape: Main menu");

        // Button to list all arenas.
        ItemStack arenas = new ItemStack(Material.LAVA_BUCKET);
        int arenasSlot = 11;
        ItemMeta arenasMeta = arenas.getItemMeta();
        assert arenasMeta != null;
        arenasMeta.setDisplayName(ChatColor.RED + "Arenas");
        arenasMeta.setLore(List.of(ChatColor.GRAY + "List of every arena"));
        arenas.setItemMeta(arenasMeta);

        inv.setItem(arenasSlot, arenas);

        // If player is an Admin or OP, a "Create New Arena" button appears.
        if(player.hasPermission("lavaescape.admin")){
            ItemStack create = new ItemStack(Material.ANVIL);
            int createSlot = 13;
            ItemMeta createMeta = arenas.getItemMeta();
            assert createMeta != null;
            createMeta.setDisplayName(ChatColor.RED + "Create New Arena");
            createMeta.setLore(List.of(ChatColor.GRAY + "Creates a new arena"));
            create.setItemMeta(createMeta);

            inv.setItem(createSlot, create);
        }

        // Exit button
        int exitSlot = 15;
        inv.setItem(exitSlot, getExitItem());

        // For loop for filling up the inventory space around the Arenas, Create new arena and Exit button.
        for(int i = 0; i < invSize; i++){
            if(!usedSlots.contains(i)){
                inv.setItem(i, getBorderItem());
            }
        }

        player.openInventory(inv);

    }
    public void createArenaPage (Player player, int page){
        UUID playerId = player.getUniqueId();

        // Defines Page size and button slots.
        int arenaPageSize = 45;
        int backSlot = 0;
        int exitSlot = 40;
        int previousSlot = 36;
        int nextSlot = 44;

        List<Integer> usedSlots = new ArrayList<>();
        Inventory inv = Bukkit.createInventory(null, arenaPageSize, "LAVA Escape: Arenas page " + page);
        int i = 0;
        // Retrieve all the arenas and create an item for each of them.

        for(String arenaName : arenaManager.getArenaS()){
            // Stops the loop if i exceeds the size of availableSlots.
            if(i >= availableArenaSlots.size()){
                break;
            }

            Arena arena = arenaManager.getArena(arenaName);
            int slotIndex = availableArenaSlots.get(i);
            //int slotIndex = page > 1 ? availableArenaSlots.get(i) : 2;

            ItemStack item = new ItemStack(Material.LAVA_BUCKET);
            ItemMeta meta = item.getItemMeta();
            assert meta != null;
            meta.setDisplayName(arenaName);
            meta.setLore(Arrays.asList(
                    ChatColor.GRAY + "Players: " + arenaManager.getPlayerAmountInArena(arenaName) + "/" + arenaManager.getMaxPlayers(arenaName),
                    ChatColor.GRAY + "Game stage: " + arena.getGameState()
            ));

            item.setItemMeta(meta);

            if(usedSlots.isEmpty()){
                inv.setItem(slotIndex, item);
                usedSlots.add(slotIndex);
            }

            else if(!usedSlots.get(i).equals(availableArenaSlots.get(i))){
                inv.setItem(slotIndex, item);
                usedSlots.add(slotIndex);
            }

            i++;

        }




        // Sets Page selector, Go back and Exit item.
        // Probably don't need to check if amount of pages is more than 1 here, but it doesn't hurt to be sure.
        if(amountOfPages() > 1){
            inv.setItem(nextSlot, getPageSelectorItem("Next"));
        } else if(amountOfPages() > 1 && currentPlayerPage.get(playerId) > 1){
            inv.setItem(previousSlot, getPageSelectorItem("Previous"));
        } else {
            inv.setItem(previousSlot, getBorderItem());
            inv.setItem(nextSlot, getBorderItem());
        }

        inv.setItem(backSlot, getGoBackItem());
        inv.setItem(exitSlot, getExitItem());

        // Fills the menu with borderItem.
        for(int j = 0; j < arenaPageSize; j++){
            if(!getNonBorderSlots().contains(j)){
                inv.setItem(j, getBorderItem());
            }
        }
        player.openInventory(inv);
    }
    private ItemStack getBorderItem(){
        // Creates a border item for the menu
        ItemStack border = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        assert borderMeta != null;
        borderMeta.setDisplayName(" ");
        borderMeta.setLore(List.of(" "));
        border.setItemMeta(borderMeta);

        return border;
    }
    private ItemStack getExitItem(){
        ItemStack exit = new ItemStack(Material.BARRIER);
        ItemMeta exitMeta = exit.getItemMeta();
        assert exitMeta != null;
        exitMeta.setDisplayName(ChatColor.RED + "Exit");
        exitMeta.setLore(List.of(ChatColor.GRAY + "Exits the menu"));
        exit.setItemMeta(exitMeta);

        return exit;
    }
    private ItemStack getPageSelectorItem(String direction){
        ItemStack pageSelector = new ItemStack(Material.ARROW);
        ItemMeta meta = pageSelector.getItemMeta();
        assert meta != null;

        switch (direction){
            case "Next":
                meta.setDisplayName(ChatColor.GOLD + "Next page");
                meta.setLore(List.of(ChatColor.GRAY + "Turns to the next page"));
                pageSelector.setItemMeta(meta);
                return pageSelector;
            case "Previous":
                meta.setDisplayName(ChatColor.GOLD + "Previous page");
                meta.setLore(List.of(ChatColor.GRAY + "Turns to the Previous page"));
                pageSelector.setItemMeta(meta);
                return pageSelector;
            default:
                return null;
        }
    }
    private ItemStack getGoBackItem(){
        ItemStack goBackItem = new ItemStack(Material.ARROW);
        ItemMeta meta = goBackItem.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ChatColor.AQUA + "Go back.");
        meta.setLore(List.of(ChatColor.GRAY + "Returns to previous menu."));
        goBackItem.setItemMeta(meta);

        return goBackItem;
    }
    private List<Integer> getNonBorderSlots (){
        List<Integer> slotIndexes = new ArrayList<>(Arrays.asList(0, 36, 40, 44));
        slotIndexes.addAll(availableArenaSlots);
        Collections.sort(slotIndexes);

        return slotIndexes;
    }
    private int amountOfPages(){
        int totalAmountOfArenas = arenaManager.getArenaS().size();
        return (totalAmountOfArenas + amountOfArenasPerPage - 1) / amountOfArenasPerPage;
    }

    //@Todo: Unsure if this method needs to be used.
    /*
     Make a method that collects all the arenas, and returns 12 arenas for the createArenaPage method to use,
     if there are more than 12 arenas, it adds those to a list.
    */
    /*private List<String> arenas(){
        List<String> arenas = new ArrayList<>();
        int amountOfArenas = arenaManager.getArenaS().size();

        if(!surplusArenas.isEmpty()){
            int size = Math.min(12, surplusArenas.size());
            for(int i = 0; i < size; i++){
                arenas.add(surplusArenas.get(0));
                surplusArenas.remove(0);
            }

        }
        else {
            int size = Math.min(amountOfArenas, 12);
            // Fills the arena Array up to the 12th object in getArenaS()
            for(int i = 0; i < size; i++){
                arenas.add(arenaManager.getArenaS().get(i));
            }
            // Fills surplusArenas Array with the remaining objects from getArenas() if there is any.
            // There is only 12 arenas per page
            for(int i = 12; i < amountOfArenas; i++){
                surplusArenas.add(arenaManager.getArenaS().get(i));
            }
        }
        return arenas;
    }
    */

}
