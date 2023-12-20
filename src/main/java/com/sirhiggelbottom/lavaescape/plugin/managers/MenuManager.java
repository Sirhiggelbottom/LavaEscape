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
        private final int amountOfArenasPerPage;


    public MenuManager(ArenaManager arenaManager) {
        this.arenaManager = arenaManager;
        availableArenaSlots = new ArrayList<>(Arrays.asList(10, 12, 14, 16, 19, 21, 23, 25, 28, 30, 32, 34));
        currentPlayerPage = new HashMap<>();
        surplusArenas = new ArrayList<>();
        amountOfArenasPerPage = 12;
    }
    public void playerMenu(CommandSender sender){
        Player player = (Player) sender;

        int invSize = 27;

        Inventory inv = Bukkit.createInventory(null, invSize, ChatColor.RED.toString() + ChatColor.BOLD + "LAVA Escape: Main menu");

        // Button to list all arenas.
        ItemStack arenas = new ItemStack(Material.LAVA_BUCKET);
        int arenasSlot = 11;
        ItemMeta arenasMeta = arenas.getItemMeta();
        assert arenasMeta != null;
        arenasMeta.setDisplayName(ChatColor.RED + "Arenas");
        arenasMeta.setLore(List.of(ChatColor.GRAY + "List of every arena"));
        arenas.setItemMeta(arenasMeta);

        inv.setItem(arenasSlot, arenas);

        // Exit button
        int exitSlot = 15;
        inv.setItem(exitSlot, getExitItem());

        // For loop for filling up the inventory space around the Arenas and Exit button.
        for(int i = 0; i < invSize; i++){
            if(i != arenasSlot && i != exitSlot){
                inv.setItem(i, getBorderItem());
            }
        }

    }
    public void createArenaPage (Player player, int page){
        int arenaPageSize = 45;
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

        if (page > 1){
            inv.setItem(nextSlot, getPageSelectorItem("Next"));
        }
        // Sets pageselector and exit.
        inv.setItem(previousSlot, getPageSelectorItem("Previous"));

        inv.setItem(exitSlot, getExitItem());

        // Fills the menu with a borderitem.
        for(int j = 0; j < arenaPageSize; j++){
            if(!getNonBorderSlots().contains(j)){
                inv.setItem(j, getBorderItem());
            }
        }

    }

    private ItemStack getBorderItem(){
        // Creates a border item for the menu
        ItemStack border = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        assert borderMeta != null;
        borderMeta.setDisplayName("");
        borderMeta.setLore(List.of(""));
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
    private List<Integer> getNonBorderSlots (){
        List<Integer> slotIndexes = new ArrayList<>(Arrays.asList(36, 40, 44));
        slotIndexes.addAll(availableArenaSlots);
        Collections.sort(slotIndexes);

        return slotIndexes;
    }
    // Make a method that collects all the arenas, and returns 12 arenas for the createArenaPage method to use,
    // if there are more than 12 arenas, it adds those to a list.

    private List<String> arenas(){
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
            for(int i = 12; i < amountOfArenas; i++){
                surplusArenas.add(arenaManager.getArenaS().get(i));
            }
        }
        return arenas;
    }

    private int amountOfPages(){
        int totalAmountOfArenas = arenaManager.getArenaS().size();
        return (totalAmountOfArenas + amountOfArenasPerPage - 1) / amountOfArenasPerPage;
    }

}
