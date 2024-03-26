package com.sirhiggelbottom.lavaescape.plugin.managers;

import com.sirhiggelbottom.lavaescape.plugin.Arena.Arena;
import com.sirhiggelbottom.lavaescape.plugin.LavaEscapePlugin;
import com.sirhiggelbottom.lavaescape.plugin.LootItem;
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
    private final ItemManager itemManager;
    private final ArenaManager arenaManager;
    private List<String> arenas;
    private List<List<String>> arenasDistributed;
    private Map<UUID, Arena> currentArenaView;
    private Map<UUID, ArenaSubPage> currentSubpageView;
    private Map<UUID, String> currentListPage;
    private List<String> arenaAnvilSubPage;
    private final Map<ArenaSubPage, Inventory> subPages;
    private final Arena arena;
    public Map<UUID, String> deleteItemMap;
    public Map<UUID, String> deleteBlockMap;
    public boolean autoClosed;



    public ArenaMenu(LavaEscapePlugin plugin, ArenaManager arenaManager, ItemManager itemManager, Arena arena) {
        this.plugin = plugin;
        this.itemManager = itemManager;
        this.arena = arena;
        this.arenas = new ArrayList<>();
        this.arenaManager = arenaManager;
        this.subPages =  new HashMap<>();
        this.currentArenaView = new HashMap<>();
        this.currentSubpageView = new HashMap<>();
        this.currentListPage = new HashMap<>();
        this.arenasDistributed = new ArrayList<>();
        arenaAnvilSubPage = new ArrayList<>(Arrays.asList("CREATE_A_NEW_ARENA" , "MIN_Y", "MAX_Y",
                "MIN_PLAYERS", "MAX_PLAYERS", "RISE_TIME", "GRACE_TIME", "STARTER_ITEMS_ADD", "BLACKLISTED_BLOCKS_ADD"));
        deleteItemMap = new HashMap<>();
        deleteBlockMap = new HashMap<>();
        autoClosed = false;
        distributeArenas();
        initializeArenaPages();
    }

    private void reloadArenaList(){
        arenas =  arenaManager.getArenas();
    }

    public enum ArenaSubPage{
        CONFIG, SET_ARENA, SET_LOBBY, MIN_Y, MAX_Y, MIN_PLAYERS, MAX_PLAYERS, RISE_TIME, GRACE_TIME, STARTER_ITEMS, STARTER_ITEMS_ADD, LOOT_ITEMS, LOOT_ITEMS_ADD, BLACKLISTED_BLOCKS, BLACKLISTED_BLOCKS_ADD, DELETE_ARENA,
        ARENA
    }

    // Distributes arenas contents into 6 different arrays.
    private void distributeArenas(){
        reloadArenaList();
        if(arenas == null) return;
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
        if(arenas == null) return;
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
            case SET_ARENA, SET_LOBBY, MIN_Y, MAX_Y, MIN_PLAYERS, MAX_PLAYERS, RISE_TIME, GRACE_TIME, STARTER_ITEMS, LOOT_ITEMS, BLACKLISTED_BLOCKS, DELETE_ARENA ->
                ArenaSubPage.CONFIG;
            case STARTER_ITEMS_ADD -> ArenaSubPage.STARTER_ITEMS;
            case BLACKLISTED_BLOCKS_ADD -> ArenaSubPage.BLACKLISTED_BLOCKS;
            case LOOT_ITEMS_ADD -> ArenaSubPage.LOOT_ITEMS;
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
        if(!arenaContains(player) && !currentListContains(player)){
            return null;
        }

        return mainMenu(player);
    }

    public void backToMainMenu(Player player, Inventory inventory){
        if(currentListContains(player)){
            currentListPage.remove(player.getUniqueId());
        } else if(arenaContains(player)){
            currentArenaView.remove(player.getUniqueId());
        }

        if(subpagesContains(player)){
            currentSubpageView.remove(player.getUniqueId());
        }

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
        List<Integer> usedSlots;
        boolean isAdmin = player.hasPermission("lavaescape.admin");
        int pageSize = isAdmin? 36 : 27;

        Inventory inv = Bukkit.createInventory(null, pageSize, ChatColor.DARK_RED.toString() + ChatColor.BOLD + "LAVA Escape: Main menu");

        if(isAdmin){
            usedSlots = new ArrayList<>(Arrays.asList(10, 12, 14, 16));

            inv.setItem(usedSlots.get(1), itemManager.getSwitchPvPModeItem());

            inv.setItem(usedSlots.get(2), itemManager.getCreateArenaItem());

            inv.setItem(usedSlots.get(3), itemManager.getExitItem());


        } else {
            usedSlots = new ArrayList<>(Arrays.asList(11, 15));
            inv.setItem(usedSlots.get(1), itemManager.getExitItem());
        }

        inv.setItem(usedSlots.get(0), itemManager.getArenasItem());

        for(int i = 0; i < pageSize; i++){
            if(!usedSlots.contains(i)){
                inv.setItem(i, itemManager.getBorderItem());
            }
        }

        return inv;
    }

    private Inventory createArenaPage(String arenaName, Player player){
        int arenaPageSize = 27;
        int backSlot = 0;
        int joinSlot = 10;
        int configSlot = 12;
        int startSlot = 14;
        int stopSlot = 15;
        int resetArenaSlot = 16;
        int exitSlot = 18;
        boolean isAdmin = player.hasPermission("lavaescape.admin");
        Inventory inv = Bukkit.createInventory(null, arenaPageSize, arenaName);

        List<Integer> nonBoarderItems;

        if(isAdmin){
            nonBoarderItems = new ArrayList<>(Arrays.asList(backSlot, joinSlot, configSlot, startSlot, stopSlot, resetArenaSlot, exitSlot));
        } else {
            nonBoarderItems = new ArrayList<>(Arrays.asList(backSlot, joinSlot, exitSlot));
        }

        inv.setItem(joinSlot, itemManager.getJoinItem());

        if(isAdmin){
            inv.setItem(configSlot, itemManager.getConfigItem());
            inv.setItem(startSlot, itemManager.getStartItem(arenaName));
            inv.setItem(stopSlot, itemManager.getStopItem(arenaName));
            inv.setItem(resetArenaSlot, itemManager.getResetArenaItem());
        }

        inv.setItem(backSlot, itemManager.getGoBackItem());
        inv.setItem(exitSlot, itemManager.getExitItem());

        for (int i = 0; i < arenaPageSize; i++){
            if(!nonBoarderItems.contains(i)) {
                inv.setItem(i, itemManager.getBorderItem());
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
    public void reloadPage(Player player, String subPage){
        UUID uniquePlayer = player.getUniqueId();
        Arena arena = currentArenaView.get(uniquePlayer);

        if(arena == null){
            return;
        }

        ArenaSubPage subpage = ArenaSubPage.valueOf(subPage.toUpperCase());

        try {
            player.openInventory(Objects.requireNonNull(createSubPage(player, subpage, arena.getName())));
            //player.openInventory(getSubPageInv(subpage));

        } catch (IllegalArgumentException e){
            Bukkit.broadcastMessage("Error: " + e);
        }
    }

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
                int setLobbySlot = 11;
                int generateSpawnsSlot = 13;
                int minYSlot = 15;
                int maxYSlot = 16;
                int normalSlot = 21;
                int compSlot = 23;
                int riseTimeSlot = 28;
                int graceTimeSlot = 29;
                int deleteArenaSlot = 31;
                int minPlayersSlot = 33;
                int maxPlayersSlot = 34;
                int countdownUntilStartSlot = 37;
                int starterItemsSlot = 39;
                int lootchestsSlot = 40;
                int blacklistedBlocksSlot = 41;
                int exitSlot = 45;



                result = Bukkit.createInventory(null, arenaPageSize, arenaName + " Config");

                nonBoarderItems = new ArrayList<>(Arrays.asList(backSlot, setArenaSlot, setLobbySlot, generateSpawnsSlot, minYSlot, maxYSlot, normalSlot, compSlot,
                        riseTimeSlot, graceTimeSlot, deleteArenaSlot, minPlayersSlot, maxPlayersSlot, countdownUntilStartSlot, starterItemsSlot, lootchestsSlot, blacklistedBlocksSlot, exitSlot));

                if(!arenaManager.getConfigValue(arenaName, "arena.pos1.x.").equalsIgnoreCase("none") &&
                        !arenaManager.getConfigValue(arenaName, "arena.pos1.y.").equalsIgnoreCase("none") &&
                        !arenaManager.getConfigValue(arenaName, "arena.pos1.z.").equalsIgnoreCase("none") &&
                        !arenaManager.getConfigValue(arenaName,"arena.pos2.x").equalsIgnoreCase("none") &&
                        !arenaManager.getConfigValue(arenaName,"arena.pos2.y").equalsIgnoreCase("none") &&
                        !arenaManager.getConfigValue(arenaName,"arena.pos2.z").equalsIgnoreCase("none")){
                    int updateSlot = 22;
                    nonBoarderItems.add(updateSlot);
                    result.setItem(updateSlot, itemManager.getUpdateArenaItem());
                }

                result.setItem(backSlot, itemManager.getGoBackItem());
                result.setItem(setArenaSlot, itemManager.getSetArenaItem(arenaName));
                result.setItem(setLobbySlot, itemManager.getSetLobbyItem(arenaName));
                result.setItem(minPlayersSlot, itemManager.getMinPlayersItem(arenaName));
                result.setItem(maxPlayersSlot, itemManager.getMaxPlayersItem(arenaName));
                result.setItem(minYSlot, itemManager.getMinYLevelItem(arenaName));
                result.setItem(maxYSlot, itemManager.getMaxYLevelItem(arenaName));
                result.setItem(generateSpawnsSlot, itemManager.getGenerateSpawnsItem(arenaName));
                result.setItem(riseTimeSlot, itemManager.getRiseTimeItem(arenaName));
                result.setItem(graceTimeSlot, itemManager.getGraceTimeItem(arenaName));
                result.setItem(normalSlot, itemManager.getNormalModeItem(arenaName));
                result.setItem(compSlot, itemManager.getCompModeItem(arenaName));
                result.setItem(deleteArenaSlot, itemManager.getDeleteArenaItem());
                result.setItem(starterItemsSlot, itemManager.getStarterItemsItem());
                result.setItem(lootchestsSlot, itemManager.getLootchestConfigItem());
                result.setItem(blacklistedBlocksSlot, itemManager.getBlacklistedBlocksItem());
                result.setItem(exitSlot, itemManager.getExitItem());

                for(int i = 0; i < arenaPageSize; i++){
                    if(!nonBoarderItems.contains(i)){
                        result.setItem(i, itemManager.getBorderItem());
                    }
                }
                return result;

            case SET_ARENA:
                arenaPageSize = 36;

                nonBoarderItems = new ArrayList<>(Arrays.asList(backSlot, infoSlot, confirmSlot, cancelSlot, tryAgainSlot));

                result = Bukkit.createInventory(null, arenaPageSize, "Confirm arena location for: " + arenaName);

                result.setItem(backSlot, itemManager.getGoBackItem());
                if(player != null){
                    result.setItem(infoSlot, itemManager.getInfoItem(player,"Arena", arenaName));
                } else result.setItem(infoSlot, itemManager.getInfoItem(null,"Arena", arenaName));
                result.setItem(confirmSlot, itemManager.getConfirmItem("Arena"));
                result.setItem(cancelSlot, itemManager.getCancelItem("Arena"));
                result.setItem(tryAgainSlot, itemManager.getTryAgainItem("Arena"));

                for(int i = 0; i < arenaPageSize; i++){
                    if(!nonBoarderItems.contains(i)){
                        result.setItem(i, itemManager.getBorderItem());
                    }
                }
                return result;

            case SET_LOBBY:
                arenaPageSize = 36;

                nonBoarderItems = new ArrayList<>(Arrays.asList(backSlot, infoSlot, confirmSlot, cancelSlot, tryAgainSlot));

                result = Bukkit.createInventory(null, arenaPageSize, "Confirm lobby location for: " + arenaName);

                result.setItem(backSlot, itemManager.getGoBackItem());
                if(player != null){
                    result.setItem(infoSlot, itemManager.getInfoItem(player,"Lobby", arenaName));
                } else result.setItem(infoSlot, itemManager.getInfoItem(null,"Lobby", arenaName));
                result.setItem(confirmSlot, itemManager.getConfirmItem("Lobby"));
                result.setItem(cancelSlot, itemManager.getCancelItem("Lobby"));
                result.setItem(tryAgainSlot, itemManager.getTryAgainItem("Lobby"));

                for(int i = 0; i < arenaPageSize; i++){
                    if(!nonBoarderItems.contains(i)){
                        result.setItem(i, itemManager.getBorderItem());
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

                result.setItem(backSlot, itemManager.getGoBackItem());

                for(int slot : itemSlots){
                    if(itemIterator.hasNext() && !usedItemSlots.contains(slot)){

                        ItemStack item = itemIterator.next();
                        result.setItem(slot, item);
                        usedItemSlots.add(slot);

                    }
                }

                result.setItem(addSlot, itemManager.getNew("Item"));

                for(int i = 0; i < arenaPageSize; i++){
                    if(!nonBoarderItems.contains(i)){
                        result.setItem(i, itemManager.getBorderItem());
                    }
                }

                return result;

            case LOOT_ITEMS:
                arenaPageSize = 45;
                int lootAddSlot = 39;
                int getLootChestSlot = 41;
                List<Integer> lootItemsSlots = new ArrayList<>(Arrays.asList(10, 11, 12, 13, 14, 15, 16,
                        19, 20, 21, 22, 23, 24 , 25, 28, 29, 30, 31, 32, 33, 34));
                List<Integer> usedLootItemSlots = new ArrayList<>();
                List<LootItem> lootItems = new ArrayList<>(arenaManager.getLootItems(arenaName));
                // Placeholder list.
                /*List<ItemStack> lootItems = new ArrayList<>(Arrays.asList(new ItemStack(Material.DIAMOND_HELMET), new ItemStack(Material.GOLDEN_SWORD),
                        new ItemStack(Material.SHIELD), new ItemStack(Material.GOLDEN_APPLE)));*/
                nonBoarderItems = new ArrayList<>(Arrays.asList(backSlot, lootAddSlot, getLootChestSlot));
                nonBoarderItems.addAll(lootItemsSlots);

                result = Bukkit.createInventory(null, arenaPageSize, "Loot items");
                result.setItem(backSlot, itemManager.getGoBackItem());

                /*if(lootItems != null){

                }*/


                List<ItemStack> lootItemStacks = new ArrayList<>();
                ItemStack item;
                ItemMeta itemMeta;
                for(LootItem lootItem : lootItems){
                    item = lootItem.getItemStack();
                    itemMeta = item.getItemMeta();
                    itemMeta.setLore(List.of(ChatColor.GRAY + "Rarity: " + lootItem.getRarity()));
                    item.setItemMeta(itemMeta);
                    lootItemStacks.add(lootItem.getItemStack());
                }

                Iterator<ItemStack> lootItemIterator = lootItemStacks.iterator();

                for(int slot : lootItemsSlots){
                    if(lootItemIterator.hasNext() && !usedLootItemSlots.contains(slot)){
                        ItemStack lootItem = lootItemIterator.next();
                        result.setItem(slot, lootItem);
                        usedLootItemSlots.add(slot);
                    }
                }

                result.setItem(lootAddSlot, itemManager.getNew("LootItem"));
                result.setItem(getLootChestSlot, itemManager.getLootchestItem());

                for(int i = 0; i < arenaPageSize; i++){
                    if(!nonBoarderItems.contains(i)){
                        result.setItem(i, itemManager.getBorderItem());
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

                result.setItem(backSlot, itemManager.getGoBackItem());

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

                result.setItem(addSlot, itemManager.getNew("Block"));

                for(int i = 0; i < arenaPageSize; i++){
                    if(!nonBoarderItems.contains(i)){
                        result.setItem(i, itemManager.getBorderItem());
                    }
                }

                return result;

            case DELETE_ARENA:

                arenaPageSize = 27;
                int yesSlot = 11;
                int noSlot = 15;

                nonBoarderItems = new ArrayList<>(Arrays.asList(yesSlot, noSlot));

                result = Bukkit.createInventory(null, arenaPageSize, "WARNING! You are about to delete this arena, are you sure?");

                result.setItem(yesSlot, itemManager.getYesItem(arenaName));
                result.setItem(noSlot, itemManager.getNoItem());

                for(int i = 0; i < arenaPageSize; i++){
                    if(!nonBoarderItems.contains(i)){
                        result.setItem(i, itemManager.getBorderItem());
                    }
                }
                return result;
        }

        return null;

    }

    public void goBack(Player player){
        if(!currentListContains(player)){
            if(arenaContains(player)){
                if(subpagesContains(player)){
                    if(currentSubpageView.get(player.getUniqueId()).equals(ArenaSubPage.CONFIG)){
                        // Player is in the config subPage and is sent back to the Arena page
                        closeSubPage(player);
                        openArenaPage(player,currentArenaView.get(player.getUniqueId()));
                    } else {
                        // Player is not in the config page, but is in one of the other subPages and is sent back to the config subPage
                        changeSubPage(player, getPreviousSubPage(player));
                    }
                } else {
                    // Player is in an Arena page and is sent back to the Arena list page
                    createArenaPages(player, 1);
                }
            }
        } else {
            // Player is in Arena list page and is sent back to the main menu
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

        if(currentListContains(player)){
            closeListPage(player);
        }
        player.closeInventory();
    }

    public void openArenaPage(Player player, Arena arena){
        player.openInventory(createArenaPage(arena.getName(), player));

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

    public void closeListPage(Player player){
        currentListPage.remove(player.getUniqueId());
    }

    public void createArenaPages(Player player, int pageNumber){

        if(arenaManager.getArenas() == null) return;

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

        inv.setItem(backSlot, itemManager.getGoBackItem());

        if (pageNumber > 1){
            inv.setItem(previousSlot, itemManager.getPreviousPageItem());
            usedSlots.add(previousSlot);
        }

        inv.setItem(exitSlot, itemManager.getExitItem());

        if(getArenasForPage(pageNumber).size() > maxAmountOfArenasPerPage){
            inv.setItem(nextSlot, itemManager.getNextPageItem());
            usedSlots.add(nextSlot);
        }

        int i = 0;

        for(String arena : getArenasForPage(pageNumber - 1)){
            inv.setItem(arenaSlots.get(i), itemManager.getArenaItem(arena));
            usedSlots.add(arenaSlots.get(i));
            i++;
        }

        for (int j = 0; j < arenaPageSize; j++){
            if(!usedSlots.contains(j)){
                inv.setItem(j, itemManager.getBorderItem());
            }
        }
        currentListPage.put(player.getUniqueId(), "Arena List");
        player.openInventory(inv);
    }
    public boolean currentListContains(Player player){
        return currentListPage.containsKey(player.getUniqueId());
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
        noMeta.setLore(List.of(ChatColor.GRAY + "Goes back to the config page."));

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
                inv.setItem(i, itemManager.getBorderItem());
            }
        }

        player.openInventory(inv);
    }

    public void deleteLootItem(Player player, String itemName){
        int pageSize = 27;
        int yesSlot = 10;
        int infoSlot = 13;
        int noSlot = 16;

        ItemStack yesItem = new ItemStack(Material.TNT);
        ItemMeta yesMeta = yesItem.getItemMeta();
        ItemStack noItem = new ItemStack(Material.CHEST);
        ItemMeta noMeta = noItem.getItemMeta();
        if(yesMeta == null || noMeta == null) return;

        yesMeta.setDisplayName(ChatColor.RED + "Delete loot item");
        yesMeta.setLore(List.of(ChatColor.GRAY + "Removes item from the loot items list"));

        yesItem.setItemMeta(yesMeta);

        noMeta.setDisplayName(ChatColor.GREEN + "Don't delete loot item");
        noMeta.setLore(List.of(ChatColor.GRAY + "Goes back to config page."));

        noItem.setItemMeta(noMeta);

        Inventory inv = Bukkit.createInventory(null, pageSize, "Delete loot item?");

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
                inv.setItem(i, itemManager.getBorderItem());
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
        noMeta.setLore(List.of(ChatColor.GRAY + "Goes back to the config page."));

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
                inv.setItem(i, itemManager.getBorderItem());
            }
        }
        player.openInventory(inv);
    }

}
