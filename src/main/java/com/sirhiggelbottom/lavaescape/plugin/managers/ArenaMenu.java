package com.sirhiggelbottom.lavaescape.plugin.managers;

import com.sirhiggelbottom.lavaescape.plugin.Arena.Arena;
import com.sirhiggelbottom.lavaescape.plugin.LavaEscapePlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ArenaMenu {
    private final LavaEscapePlugin plugin;
    private final MenuManager menuManager;
    private final ArenaManager arenaManager;
    private List<String> arenas;
    private List<List<String>> arenasDistributed;
    private Map<UUID, Arena> currentArenaView;
    private Map<UUID, ArenaSubPage> currentSubpageView;
    private List<String> arenaAnvilSubPage;
    private final Map<ArenaSubPage, Inventory> subPages;
    private final Arena arena;
    public Map<UUID, String> deleteItemMap;
    public Map<UUID, String> deleteBlockMap;



    public ArenaMenu(LavaEscapePlugin plugin, ArenaManager arenaManager, MenuManager menuManager, Arena arena) {
        this.plugin = plugin;
        this.menuManager = menuManager;
        this.arena = arena;
        this.arenas = new ArrayList<>();
        this.arenaManager = arenaManager;
        this.subPages =  new HashMap<>();
        this.currentArenaView = new HashMap<>();
        this.currentSubpageView = new HashMap<>();
        this.arenasDistributed = new ArrayList<>();
        arenaAnvilSubPage = new ArrayList<>(Arrays.asList("CREATE_A_NEW_ARENA" , "MIN_Y", "MAX_Y",
                "MIN_PLAYERS", "MAX_PLAYERS", "RISE_TIME", "GRACE_TIME", "STARTER_ITEMS_ADD", "BLACKLISTED_BLOCKS_ADD"));
        deleteItemMap = new HashMap<>();
        deleteBlockMap = new HashMap<>();
        distributeArenas();
        initializeArenaPages();
    }

    private void reloadArenaList(){
        arenas =  arenaManager.getArenaS();
    }

    public enum ArenaSubPage{
        CONFIG, SET_ARENA, SET_LOBBY, MIN_Y, MAX_Y, MIN_PLAYERS, MAX_PLAYERS, RISE_TIME, GRACE_TIME, STARTER_ITEMS, STARTER_ITEMS_ADD, BLACKLISTED_BLOCKS, BLACKLISTED_BLOCKS_ADD, DELETE_ARENA,
        ARENA
    }

    // Distributes arenas contents into 6 different arrays.
    private void distributeArenas(){
        reloadArenaList();
        arenasDistributed.clear(); // Clears the list to reset it.

        for(int i = 0; i < 6; i++){
            arenasDistributed.add(new ArrayList<>());
        }

        for(int i = 0; i < arenas.size(); i++){
            int listIndex = i / 12;
            if(listIndex < arenasDistributed.size()){
                arenasDistributed.get(listIndex).add(arenas.get(i));
            }
        }
    }

    private List<String> getArenasForPage(int pageNumber){
        return arenasDistributed.get(pageNumber);
    }

    private void initializeArenaPages(){
        for(String arena : arenas){
            initializeSubPages(arena);
        }
    }

    public ArenaSubPage getPreviousSubPage(Player player){
        if(!subpagesContains(player)){
            return null;
        }

        ArenaSubPage subPage = getSubPage(player);

        return switch (subPage) {
            case SET_ARENA, SET_LOBBY, MIN_Y, MAX_Y, MIN_PLAYERS, MAX_PLAYERS, RISE_TIME, GRACE_TIME, STARTER_ITEMS, BLACKLISTED_BLOCKS, DELETE_ARENA ->
                ArenaSubPage.CONFIG;
            case STARTER_ITEMS_ADD -> ArenaSubPage.STARTER_ITEMS;
            case BLACKLISTED_BLOCKS_ADD -> ArenaSubPage.BLACKLISTED_BLOCKS;
            default -> null;
        };
    }

    public Boolean arenaContains (Player player){
        return currentArenaView.containsKey(player.getUniqueId());
    }

    public Arena getPreviousArenaPage(Player player){

        if(!arenaContains(player)){
            return null;
        }

        if(!subpagesContains(player)){
            return null;
        }

        ArenaSubPage subPage = getSubPage(player);

        if(subPage.equals(ArenaSubPage.CONFIG)){
            return currentArenaView.get(player.getUniqueId());
        }

        return null;
    }

    public Inventory getPreviousMainMenu (Player player){
        if(!arenaContains(player)){
            return null;
        }

        return mainMenu(player);
    }

    public void backToMainMenu(Player player, Inventory inventory){
        currentArenaView.remove(player.getUniqueId());
        player.openInventory(inventory);
    }

    public void setSubPage(Player player, String subPage){
        UUID uniquePlayer = player.getUniqueId();
        Arena arena = currentArenaView.get(uniquePlayer);

        if(arena == null){
            return;
        }

        ArenaSubPage subpage = ArenaSubPage.valueOf(subPage.toUpperCase());

        currentSubpageView.put(player.getUniqueId(), subpage);

    }

    public Inventory mainMenu (Player player){
        int pageSize = 27;
        List<Integer> usedSlots;
        boolean isAdmin = player.hasPermission("lavaescape.admin");

        Inventory inv = Bukkit.createInventory(null, pageSize, ChatColor.DARK_RED.toString() + ChatColor.BOLD + "LAVA Escape: Main menu");

        if(isAdmin){
            usedSlots = new ArrayList<>(Arrays.asList(10, 12, 14, 16));

            inv.setItem(usedSlots.get(1), menuManager.getSwitchPvPModeItem());

            inv.setItem(usedSlots.get(2), menuManager.getCreateArenaItem());

            inv.setItem(usedSlots.get(3), menuManager.getExitItem());
        } else {
            usedSlots = new ArrayList<>(Arrays.asList(11, 15));
            inv.setItem(usedSlots.get(1), menuManager.getExitItem());
        }

        inv.setItem(usedSlots.get(0), menuManager.getArenasItem());

        for(int i = 0; i < pageSize; i++){
            if(!usedSlots.contains(i)){
                inv.setItem(i, menuManager.getBorderItem());
            }
        }

        return inv;
    }

    private Inventory createArenaPage(String arenaName){
        int arenaPageSize = 27;
        int joinSlot = 10;
        int configSlot = 12;
        int normalSlot = 14;
        int compSlot = 16;
        int backSlot = 0;
        int exitSlot = 18;
        Inventory inv = Bukkit.createInventory(null, arenaPageSize, arenaName);

        List<Integer> nonBoarderItems = new ArrayList<>(Arrays.asList(joinSlot, configSlot, normalSlot, compSlot, backSlot, exitSlot));

        inv.setItem(joinSlot, menuManager.getJoinItem());
        inv.setItem(configSlot, menuManager.getConfigItem());
        inv.setItem(normalSlot, menuManager.getNormalModeItem(arenaName));
        inv.setItem(compSlot, menuManager.getCompModeItem(arenaName));
        inv.setItem(backSlot, menuManager.getGoBackItem());
        inv.setItem(exitSlot, menuManager.getExitItem());

        for (int i = 0; i < arenaPageSize; i++){
            if(!nonBoarderItems.contains(i)) {
                inv.setItem(i, menuManager.getBorderItem());
            }
        }
        return inv;
    }

    public void changeSubPage(Player player, ArenaSubPage subPage){
        UUID uniquePlayer = player.getUniqueId();
        Arena arena = currentArenaView.get(uniquePlayer);

        if(arena == null){
            return;
        }

        currentSubpageView.replace(uniquePlayer, currentSubpageView.get(uniquePlayer), subPage); // Moves player from old subPage to new subPage.

        player.openInventory(Objects.requireNonNull(createSubPage(player, subPage, arena.getName())));
        /*createSubPage(subPage, getArenaNamePage(player));
        player.openInventory(getSubPageInv(subPage));*/
    }

    private void initializeSubPages(String arena){

        for (ArenaSubPage subPage : ArenaSubPage.values()){
            if(subPage != ArenaSubPage.ARENA || !arenaAnvilSubPage.contains(subPage.toString())){
                subPages.put(subPage, createSubPage(null, subPage, arena));
            }
        }
    }


    /* List of pages and their contents:
    *   CONFIG:
    *       Contains all the other pages except the arena page itself.
    *   SET_ARENA:
    *       Gives the player a wand, and when the player has selected 2 positions a page pops up and asks the player if they are happy with their choice.
    *   SET_LOBBY:
    *       Same as SET_ARENA.
    *   MIN_Y:
    *       Is an anvil page that the player inputs the lowest Y-level for the spawnpoint creator to search for spawnpoints.
    *   MAX_Y:
    *       The same as MIN_Y but for the highest Y-level.
    *   MIN_PLAYERS:
    *       Anvil page where the player can input the minimum amount of players required for a game to start in normal / server mode.
    *   MAX_PLAYERS:
    *       Same as MIN_PLAYERS.
    *   RISE_TIME:
    *       Anvil page where the player can input the time in between each time the lava rises.
    *   GRACE_TIME:
    *       Anvil page where the player can input the time from when the game starts until the lava starts to rise.
    *   STARTER_ITEMS:
    *       Displays the current starter items, if any. It also has an add button
    *   STARTER_ITEMS_ADD:
    *       Anvil page where the player can add starter items.
    *   BLACKLISTED_BLOCKS:
    *       Displays the current blacklisted blocks, if any. It also has an add button.
    *   DELETE_ARENA:
    *       Has a yes and a no button, the name of the page is: Are you sure you want to delete <areanName>?
    *   NONE:
    *       For normal players it displays a join, back and exit button. For admins it displays the config button as well.
    * */
    private Inventory createSubPage(Player player, ArenaSubPage subPage, String arenaName) {

        int arenaPageSize;
        int backSlot = 0;
        int infoSlot = 13; // Used for both SET_ARENA and SET_LOBBY page
        int confirmSlot = 20; // Used for both SET_ARENA and SET_LOBBY page
        int cancelSlot = 22; // Used for both SET_ARENA and SET_LOBBY page
        int tryAgainSlot = 24; // Used for both SET_ARENA and SET_LOBBY page
        int addSlot = 40; // Used for both STARTER_ITEMS and BLACKLISTED_BLOCKS
        Inventory result = null;
        List<Integer> nonBoarderItems;
        switch (subPage){

            case CONFIG:
                // Add code for CONFIG page.
                arenaPageSize = 54;
                int setArenaSlot = 10;
                int setLobbySlot = 12;
                int minPlayersSlot = 14;
                int maxPlayersSlot = 16;
                int minYSlot = 20;
                int maxYSlot = 22;
                int generateSpawnsSlot = 24;
                int riseTimeSlot = 28;
                int graceTimeSlot = 30;
                int resetArenaSlot = 32;
                int deleteArenaSlot = 34;
                int starterItemsSlot = 38;
                int blacklistedBlocksSlot = 42;
                int exitSlot = 45;
                result = Bukkit.createInventory(null, arenaPageSize, arenaName + " Config");

                nonBoarderItems = new ArrayList<>(Arrays.asList(backSlot, setArenaSlot, setLobbySlot, minPlayersSlot, maxPlayersSlot, minYSlot, maxYSlot,
                        generateSpawnsSlot, riseTimeSlot, graceTimeSlot, resetArenaSlot, deleteArenaSlot, starterItemsSlot, blacklistedBlocksSlot, exitSlot));


                result.setItem(backSlot, menuManager.getGoBackItem());
                result.setItem(setArenaSlot, menuManager.getSetArenaItem(arenaName));
                result.setItem(setLobbySlot, menuManager.getSetLobbyItem(arenaName));
                result.setItem(minPlayersSlot, menuManager.getMinPlayersItem(arenaName));
                result.setItem(maxPlayersSlot, menuManager.getMaxPlayersItem(arenaName));
                result.setItem(minYSlot, menuManager.getMinYLevelItem(arenaName));
                result.setItem(maxYSlot, menuManager.getMaxYLevelItem(arenaName));
                result.setItem(generateSpawnsSlot, menuManager.getGenerateSpawnsItem(arenaName));
                result.setItem(riseTimeSlot, menuManager.getRiseTimeItem(arenaName));
                result.setItem(graceTimeSlot, menuManager.getGraceTimeItem(arenaName));
                result.setItem(resetArenaSlot, menuManager.getResetArenaItem());
                result.setItem(deleteArenaSlot, menuManager.getDeleteArenaItem());
                result.setItem(starterItemsSlot, menuManager.getStarterItemsItem());
                result.setItem(blacklistedBlocksSlot, menuManager.getBlacklistedBlocksItem());
                result.setItem(exitSlot, menuManager.getExitItem());

                for(int i = 0; i < arenaPageSize; i++){
                    if(!nonBoarderItems.contains(i)){
                        result.setItem(i, menuManager.getBorderItem());
                    }
                }
                return result;

            case SET_ARENA:
                arenaPageSize = 36;

                nonBoarderItems = new ArrayList<>(Arrays.asList(backSlot, infoSlot, confirmSlot, cancelSlot, tryAgainSlot));

                result = Bukkit.createInventory(null, arenaPageSize, "Confirm arena location for: " + arenaName);

                result.setItem(backSlot, menuManager.getGoBackItem());
                if(player != null){
                    result.setItem(infoSlot, menuManager.getInfoItem(player,"Arena", arenaName));
                } else result.setItem(infoSlot, menuManager.getInfoItem(null,"Arena", arenaName));
                result.setItem(confirmSlot, menuManager.getConfirmItem("Arena"));
                result.setItem(cancelSlot, menuManager.getCancelItem("Arena"));
                result.setItem(tryAgainSlot, menuManager.getTryAgainItem("Arena"));

                for(int i = 0; i < arenaPageSize; i++){
                    if(!nonBoarderItems.contains(i)){
                        result.setItem(i, menuManager.getBorderItem());
                    }
                }
                return result;

            case SET_LOBBY:
                arenaPageSize = 36;

                nonBoarderItems = new ArrayList<>(Arrays.asList(backSlot, infoSlot, confirmSlot, cancelSlot, tryAgainSlot));

                result = Bukkit.createInventory(null, arenaPageSize, "Confirm lobby location for: " + arenaName);

                result.setItem(backSlot, menuManager.getGoBackItem());
                if(player != null){
                    result.setItem(infoSlot, menuManager.getInfoItem(player,"Lobby", arenaName));
                } else result.setItem(infoSlot, menuManager.getInfoItem(null,"Lobby", arenaName));
                result.setItem(confirmSlot, menuManager.getConfirmItem("Lobby"));
                result.setItem(cancelSlot, menuManager.getCancelItem("Lobby"));
                result.setItem(tryAgainSlot, menuManager.getTryAgainItem("Lobby"));

                for(int i = 0; i < arenaPageSize; i++){
                    if(!nonBoarderItems.contains(i)){
                        result.setItem(i, menuManager.getBorderItem());
                    }
                }

                return result;

            case STARTER_ITEMS:

                arenaPageSize = 45;

                List<Integer> itemSlots = new ArrayList<>(Arrays.asList(10, 11, 12, 13, 14, 15, 16,
                        19, 20, 21, 22, 23, 24 , 25, 28, 29, 30, 31, 32, 33, 34));
                List<Integer> usedItemSlots = new ArrayList<>();
                List<ItemStack> starterItems = new ArrayList<>(arenaManager.getStartingItems(arenaName));

                Iterator<ItemStack> itemIterator = starterItems.iterator();

                nonBoarderItems = new ArrayList<>(Arrays.asList(backSlot, addSlot));
                nonBoarderItems.addAll(itemSlots);

                result = Bukkit.createInventory(null, arenaPageSize, "Starter items");

                result.setItem(backSlot, menuManager.getGoBackItem());

                for(int slot : itemSlots){
                    if(itemIterator.hasNext() && !usedItemSlots.contains(slot)){

                        ItemStack item = itemIterator.next();
                        result.setItem(slot, item);
                        usedItemSlots.add(slot);

                    }
                }

                result.setItem(addSlot, menuManager.getNew("Item"));

                for(int i = 0; i < arenaPageSize; i++){
                    if(!nonBoarderItems.contains(i)){
                        result.setItem(i, menuManager.getBorderItem());
                    }
                }

                return result;

            case BLACKLISTED_BLOCKS:
                arenaPageSize = 45;

                List<Integer> blockSlots = new ArrayList<>(Arrays.asList(10, 11, 12, 13, 14, 15, 16,
                        19, 20, 21, 22, 23, 24 , 25, 28, 29, 30, 31, 32, 33, 34));
                List<Integer> usedBlockSlots = new ArrayList<>();
                List<ItemStack> blacklistedBlocks = arenaManager.getBlacklistedBlocks(arenaName);

                nonBoarderItems = new ArrayList<>(Arrays.asList(backSlot, addSlot));
                nonBoarderItems.addAll(blockSlots);

                result = Bukkit.createInventory(null, arenaPageSize, "Blacklisted blocks");

                result.setItem(backSlot, menuManager.getGoBackItem());

                if(blacklistedBlocks != null){
                    Iterator<ItemStack> blockIterator = blacklistedBlocks.iterator();

                    for(int slot : blockSlots){
                        if(blockIterator.hasNext() && !usedBlockSlots.contains(slot)){

                            ItemStack block = blockIterator.next();
                            result.setItem(slot, block);
                            usedBlockSlots.add(slot);

                        }
                    }
                }

                result.setItem(addSlot, menuManager.getNew("Block"));

                for(int i = 0; i < arenaPageSize; i++){
                    if(!nonBoarderItems.contains(i)){
                        result.setItem(i, menuManager.getBorderItem());
                    }
                }

                return result;

            case DELETE_ARENA:

                arenaPageSize = 27;
                int yesSlot = 11;
                int noSlot = 15;

                nonBoarderItems = new ArrayList<>(Arrays.asList(yesSlot, noSlot));

                result = Bukkit.createInventory(null, arenaPageSize, "WARNING! You are about to delete this arena, are you sure?");

                result.setItem(yesSlot, menuManager.getYesItem(arenaName));
                result.setItem(noSlot, menuManager.getNoItem());

                for(int i = 0; i < arenaPageSize; i++){
                    if(!nonBoarderItems.contains(i)){
                        result.setItem(i, menuManager.getBorderItem());
                    }
                }
                return result;
        }

        return null;

    }

    public void goBack(Player player){
        if (subpagesContains(player)) {
            changeSubPage(player, getPreviousSubPage(player));
        } else if (arenaContains(player)) {
            backToMainMenu(player, getPreviousMainMenu(player));
        }
    }

    public String parseDeleteItem(String item){
        String[] rawString = item.split("\\{");
        String uncleanString = rawString[1];
        String[] cleanerStringArr = uncleanString.split("x");
        String cleanerString = cleanerStringArr[0];

        return cleanerString.replace(" ", "");
    }

    public void closeInventory(Player player){
        if (subpagesContains(player)) { // If player is in subpages, player is removed.
            closeSubPage(player);
        }
        if (arenaContains(player)) { // if player is in arenaPages, player is removed.
            closeArenaPage(player, getPreviousArenaPage(player));
        }
        player.closeInventory();
    }

    public void openArenaPage(Player player, Arena arena){

        player.openInventory(createArenaPage(arena.getName()));

        currentArenaView.put(player.getUniqueId(), arena);
    }

    public String getArenaNamePage(Player player){
        return currentArenaView.get(player.getUniqueId()).getName();
    }

    private Inventory getSubPageInv(ArenaSubPage subPage){
        return subPages.getOrDefault(subPage, null);
    }

    public void openSubPage(Player player, String subPage){
        UUID uniquePlayer = player.getUniqueId();
        Arena arena = currentArenaView.get(uniquePlayer);

        if(arena == null){
            return;
        }

        ArenaSubPage subpage = ArenaSubPage.valueOf(subPage.toUpperCase());

        try {
            player.openInventory(Objects.requireNonNull(createSubPage(player, subpage, arena.getName())));
            //player.openInventory(getSubPageInv(subpage));
            currentSubpageView.put(player.getUniqueId(), subpage);

        } catch (IllegalArgumentException e){
            Bukkit.broadcastMessage("Error: " + e);
        }

    }

    public Boolean subpagesContains(Player player){
        return currentSubpageView.containsKey(player.getUniqueId());
    }
    public ArenaSubPage getSubPage(Player player){
        return currentSubpageView.get(player.getUniqueId());
    }

    public void closeArenaPage(Player player, Arena arena){
        currentArenaView.remove(player.getUniqueId(), arena);
    }

    public void closeSubPage(Player player){
        currentSubpageView.remove(player.getUniqueId(), currentSubpageView.get(player.getUniqueId()));
    }

    public void createArenaPages(Player player, int pageNumber){

        int arenaPageSize = 45;
        int backSlot = 0;
        int previousSlot = 36;
        int exitSlot = 40;
        int nextSlot = 44;
        int maxAmountOfArenasPerPage = 12;
        List<Integer> arenaSlots = new ArrayList<>(Arrays.asList(10, 12, 14, 16, 19, 21, 23, 25, 28, 30, 32, 34));
        List<Integer> usedSlots = new ArrayList<>(Arrays.asList(backSlot, exitSlot));

        distributeArenas(); // This is called to update the arena list page, incase a new arena has been created.

        Inventory inv = Bukkit.createInventory(null, arenaPageSize, "Arena list");

        inv.setItem(backSlot, menuManager.getGoBackItem());

        if (pageNumber > 1){
            inv.setItem(previousSlot, menuManager.getPreviousPageItem());
            usedSlots.add(previousSlot);
        }

        inv.setItem(exitSlot, menuManager.getExitItem());

        if(getArenasForPage(pageNumber).size() > maxAmountOfArenasPerPage){
            inv.setItem(nextSlot, menuManager.getNextPageItem());
            usedSlots.add(nextSlot);
        }

        int i = 0;

        for(String arena : getArenasForPage(pageNumber - 1)){
            inv.setItem(arenaSlots.get(i), menuManager.getArenaItem(arena));
            usedSlots.add(arenaSlots.get(i));
            i++;
        }

        for (int j = 0; j < arenaPageSize; j++){
            if(!usedSlots.contains(j)){
                inv.setItem(j, menuManager.getBorderItem());
            }
        }

        player.openInventory(inv);
    }

    public void deleteStartingItem(Player player, String itemName){
        int pageSize = 27;
        int yesSlot = 10;
        int infoSlot = 13;
        int noSlot = 16;

        ItemStack yesItem = new ItemStack(Material.TNT);
        ItemMeta yesMeta = yesItem.getItemMeta();
        ItemStack noItem = new ItemStack(Material.CHEST);
        ItemMeta noMeta = noItem.getItemMeta();
        if(yesMeta == null || noMeta == null) return;

        yesMeta.setDisplayName(ChatColor.RED + "Delete item");
        yesMeta.setLore(List.of(ChatColor.GRAY + "Removes item from the starter items list"));

        yesItem.setItemMeta(yesMeta);

        noMeta.setDisplayName(ChatColor.GREEN + "Don't delete item");
        noMeta.setLore(List.of(ChatColor.GRAY + "Goes back to the starter items list."));

        noItem.setItemMeta(noMeta);

        Inventory inv = Bukkit.createInventory(null, pageSize, "Delete item?");

        ItemStack infoItem = new ItemStack(Material.getMaterial(itemName));
        if(Material.getMaterial(itemName) == null){
            player.sendMessage("Couldn't find the item");
        }

        inv.setItem(infoSlot, infoItem);
        deleteItemMap.put(player.getUniqueId(), infoItem.toString());

        inv.setItem(yesSlot, yesItem);
        inv.setItem(noSlot, noItem);

        for(int i = 0; i < pageSize; i++){
            if(i != yesSlot && i != noSlot && i != infoSlot){
                inv.setItem(i, menuManager.getBorderItem());
            }
        }

        player.openInventory(inv);
    }

    public void deleteBlacklistedBlock(Player player, String itemName){
        int pageSize = 27;
        int yesSlot = 10;
        int infoSlot = 13;
        int noSlot = 16;

        ItemStack yesItem = new ItemStack(Material.TNT);
        ItemMeta yesMeta = yesItem.getItemMeta();
        ItemStack noItem = new ItemStack(Material.CHEST);
        ItemMeta noMeta = noItem.getItemMeta();
        if(yesMeta == null || noMeta == null) return;

        yesMeta.setDisplayName(ChatColor.RED + "Delete block");
        yesMeta.setLore(List.of(ChatColor.GRAY + "Removes block from the blacklisted blocks list"));

        yesItem.setItemMeta(yesMeta);

        noMeta.setDisplayName(ChatColor.GREEN + "Don't delete block");
        noMeta.setLore(List.of(ChatColor.GRAY + "Goes back to the blacklisted blocks list."));

        noItem.setItemMeta(noMeta);

        Inventory inv = Bukkit.createInventory(null, pageSize, "Delete block?");

        ItemStack infoItem = new ItemStack(Material.getMaterial(itemName));

        if(Material.getMaterial(itemName) == null){
            player.sendMessage("Couldn't find the block");
        }
        inv.setItem(infoSlot, infoItem);
        deleteBlockMap.put(player.getUniqueId(), infoItem.toString());

        inv.setItem(yesSlot, yesItem);
        inv.setItem(noSlot, noItem);

        for(int i = 0; i < pageSize; i++){
            if(i != yesSlot && i != noSlot && i != infoSlot){
                inv.setItem(i, menuManager.getBorderItem());
            }
        }
        player.openInventory(inv);
    }

}
