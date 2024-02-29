package com.sirhiggelbottom.lavaescape.plugin.managers;

import com.sirhiggelbottom.lavaescape.plugin.Arena.Arena;
import com.sirhiggelbottom.lavaescape.plugin.LavaEscapePlugin;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

//@ToDo Need to make logic that creates arena menu's based on how many arenas there are. There can only be 12 arenas per page.
// Remaining: Add logic that figures how to display arenas when there is more than 12 arenas.

public class ItemManager {
        private final LavaEscapePlugin plugin;
        private final ArenaManager arenaManager;
        private final List<Integer> availableArenaSlots;
        private final int amountOfArenasPerPage;
        public Set<UUID> anvilGUIUsers = new HashSet<>();
        public Map<UUID, String> writtenArenaName;
        public Map<UUID, String> writtenMinYValue;
        public Map<UUID, String> writtenMaxYValue;
        public Map<UUID, String> writtenMinPlayersValue;
        public Map<UUID, String> writtenMaxPlayersValue;
        public Map<UUID, String> writtenRiseTimeValue;
        public Map<UUID, String> writtenGraceTimeValue;
        public Map<UUID, String> writtenStarterItemsValue;
        public Map<UUID, String> writtenLootItemsValue;
        public Map<UUID, String> writtenBlacklistetBlocksValue;
        public List<Map<UUID, String>> writtenAnvilItemList;

    public ItemManager(LavaEscapePlugin plugin , ArenaManager arenaManager) {
        this.plugin = plugin;
        this.arenaManager = arenaManager;
        availableArenaSlots = new ArrayList<>(Arrays.asList(
                10, 12, 14, 16, 19, 21, 23, 25, 28, 30, 32, 34));
        amountOfArenasPerPage = 12;

        writtenArenaName = new HashMap<>();
        writtenMinYValue = new HashMap<>();
        writtenMaxYValue = new HashMap<>();
        writtenMinPlayersValue = new HashMap<>();
        writtenMaxPlayersValue = new HashMap<>();
        writtenRiseTimeValue = new HashMap<>();
        writtenGraceTimeValue = new HashMap<>();
        writtenStarterItemsValue = new HashMap<>();
        writtenLootItemsValue = new HashMap<>();
        writtenBlacklistetBlocksValue = new HashMap<>();


        writtenAnvilItemList = new ArrayList<>(Arrays.asList(writtenArenaName, writtenMinYValue, writtenMaxYValue, writtenMinPlayersValue, writtenMaxPlayersValue,
                writtenRiseTimeValue, writtenGraceTimeValue, writtenStarterItemsValue, writtenLootItemsValue, writtenBlacklistetBlocksValue));
    }

    public enum MapList{
        WRITTENARENANAME, WRITTENMINYVALUE, WRITTENMAXYVALUE, WRITTENMINPLAYERSVALUE, WRITTENMAXPLAYERSVALUE,
        WRITTENRISETIMEVALUE, WRITTENGRACETIMEVALUE, WRITTENSTARTERITEMSVALUE, WRITTENLOOTITEMSVALUE , WRITTENBLACKLISTEDBLOCKSVALUE
    }

    private final Map<UUID, Long> lastInteracted = new HashMap<>();

    public void createNewArena(Player player){
        new AnvilGUI.Builder()
                .onClose(stateSnapshot -> closeAnvilGuiPage(stateSnapshot.getPlayer()))
                .onClick((slot, stateSnapshot)->{
                    if(slot != AnvilGUI.Slot.OUTPUT){
                        return Collections.emptyList();
                    }

                    String input = stateSnapshot.getText();

                    stateSnapshot.getPlayer().sendMessage(input);

                    if(!input.isBlank() && !input.equalsIgnoreCase("nameless") && !input.equalsIgnoreCase("error, try again")){

                        closeAnvilGuiPage(stateSnapshot.getPlayer());
                        writtenArenaName.put(stateSnapshot.getPlayer().getUniqueId(), stateSnapshot.getText());

                        return List.of(AnvilGUI.ResponseAction.openInventory(conformationInv(stateSnapshot.getPlayer(), MapList.WRITTENARENANAME, "null", "null")));

                    } else{
                        return List.of(AnvilGUI.ResponseAction.updateTitle("Error, try again.", true));
                    }
                })
                .interactableSlots()
                .itemLeft(getWriteableItem())
                .title("Name your arena.")
                .plugin(plugin)
                .open(openAnvilGuiPage(player));
    }

    public void setMinY(Player player){
        new AnvilGUI.Builder()
                .onClose(stateSnapshot -> closeAnvilGuiPage(stateSnapshot.getPlayer()))
                .onClick((slot, stateSnapshot)->{
                    if(slot != AnvilGUI.Slot.OUTPUT){
                        return Collections.emptyList();
                    }

                    stateSnapshot.getPlayer().sendMessage(stateSnapshot.getText());
                    UUID playerId = stateSnapshot.getPlayer().getUniqueId();

                    String input = stateSnapshot.getText();

                    if(!input.isBlank() && input.matches("^-?\\d+$")){

                        closeAnvilGuiPage(stateSnapshot.getPlayer());
                        writtenMinYValue.put(playerId, input);

                        return List.of(AnvilGUI.ResponseAction.openInventory(conformationInv(stateSnapshot.getPlayer(), MapList.WRITTENMINYVALUE, "null", "null")));
                    } else {
                        return List.of(AnvilGUI.ResponseAction.updateTitle("Error, Try again.", true));
                    }
                })
                .interactableSlots()
                .itemLeft(getWriteableItem())
                .title("Set min Y-level.")
                .plugin(plugin)
                .open(openAnvilGuiPage(player));
    }

    public void setMaxY(Player player){
        new AnvilGUI.Builder()
                .onClose(stateSnapshot -> closeAnvilGuiPage(stateSnapshot.getPlayer()))
                .onClick((slot, stateSnapshot)->{
                    if(slot != AnvilGUI.Slot.OUTPUT){
                        return Collections.emptyList();
                    }

                    stateSnapshot.getPlayer().sendMessage(stateSnapshot.getText());
                    UUID playerId = stateSnapshot.getPlayer().getUniqueId();

                    String input = stateSnapshot.getText();

                    if(!input.isBlank() && input.matches("^-?\\d+$")){

                        closeAnvilGuiPage(stateSnapshot.getPlayer());
                        writtenMaxYValue.put(playerId, input);

                        return List.of(AnvilGUI.ResponseAction.openInventory(conformationInv(stateSnapshot.getPlayer(), MapList.WRITTENMAXYVALUE, "null", "null")));
                    } else {
                        return List.of(AnvilGUI.ResponseAction.updateTitle("Error, Try again.", true));
                    }
                })
                .interactableSlots()
                .itemLeft(getWriteableItem())
                .title("Set max Y-level.")
                .plugin(plugin)
                .open(openAnvilGuiPage(player));
    }

    public void setMinPlayers(Player player){
        new AnvilGUI.Builder()
                .onClose(stateSnapshot -> closeAnvilGuiPage(stateSnapshot.getPlayer()))
                .onClick((slot, stateSnapshot)->{
                    if(slot != AnvilGUI.Slot.OUTPUT){
                        return Collections.emptyList();
                    }

                    stateSnapshot.getPlayer().sendMessage(stateSnapshot.getText());
                    UUID playerId = stateSnapshot.getPlayer().getUniqueId();

                    String input = stateSnapshot.getText();

                    if(!input.isBlank() && input.matches("^\\d+$")){

                        closeAnvilGuiPage(stateSnapshot.getPlayer());
                        writtenMinPlayersValue.put(playerId, input);

                        return List.of(AnvilGUI.ResponseAction.openInventory(conformationInv(stateSnapshot.getPlayer(), MapList.WRITTENMINPLAYERSVALUE, "null", "null")));
                    } else {
                        return List.of(AnvilGUI.ResponseAction.updateTitle("Error, Try again.", true));
                    }
                })
                .interactableSlots()
                .itemLeft(getWriteableItem())
                .title("Set minimum amount of players.")
                .plugin(plugin)
                .open(openAnvilGuiPage(player));
    }

    public void setMaxPlayers(Player player){
        new AnvilGUI.Builder()
                .onClose(stateSnapshot -> closeAnvilGuiPage(stateSnapshot.getPlayer()))
                .onClick((slot, stateSnapshot)->{
                    if(slot != AnvilGUI.Slot.OUTPUT){
                        return Collections.emptyList();
                    }

                    stateSnapshot.getPlayer().sendMessage(stateSnapshot.getText());
                    UUID playerId = stateSnapshot.getPlayer().getUniqueId();

                    String input = stateSnapshot.getText();

                    if(!input.isBlank() && input.matches("^\\d+$")){

                        closeAnvilGuiPage(stateSnapshot.getPlayer());
                        writtenMaxPlayersValue.put(playerId, input);

                        return List.of(AnvilGUI.ResponseAction.openInventory(conformationInv(stateSnapshot.getPlayer(), MapList.WRITTENMAXPLAYERSVALUE, "null", "null")));
                    } else {
                        return List.of(AnvilGUI.ResponseAction.updateTitle("Error, Try again.", true));
                    }
                })
                .interactableSlots()
                .itemLeft(getWriteableItem())
                .title("Set maximum amount of players.")
                .plugin(plugin)
                .open(openAnvilGuiPage(player));
    }

    public void setRiseTime(Player player){
        new AnvilGUI.Builder()
                .onClose(stateSnapshot -> closeAnvilGuiPage(stateSnapshot.getPlayer()))
                .onClick((slot, stateSnapshot)->{
                    if(slot != AnvilGUI.Slot.OUTPUT){
                        return Collections.emptyList();
                    }

                    stateSnapshot.getPlayer().sendMessage(stateSnapshot.getText());
                    UUID playerId = stateSnapshot.getPlayer().getUniqueId();

                    String input = stateSnapshot.getText();

                    if(!input.isBlank() && input.matches("^\\d+$")){

                        closeAnvilGuiPage(stateSnapshot.getPlayer());
                        writtenRiseTimeValue.put(playerId, input);

                        return List.of(AnvilGUI.ResponseAction.openInventory(conformationInv(stateSnapshot.getPlayer(), MapList.WRITTENRISETIMEVALUE, "null", "null")));
                    } else {
                        return List.of(AnvilGUI.ResponseAction.updateTitle("Error, Try again.", true));
                    }
                })
                .interactableSlots()
                .itemLeft(getWriteableItem())
                .title("Set rise time.")
                .plugin(plugin)
                .open(openAnvilGuiPage(player));
    }

    public void setGraceTime(Player player){
        new AnvilGUI.Builder()
                .onClose(stateSnapshot -> closeAnvilGuiPage(stateSnapshot.getPlayer()))
                .onClick((slot, stateSnapshot)->{
                    if(slot != AnvilGUI.Slot.OUTPUT){
                        return Collections.emptyList();
                    }

                    stateSnapshot.getPlayer().sendMessage(stateSnapshot.getText());
                    UUID playerId = stateSnapshot.getPlayer().getUniqueId();

                    String input = stateSnapshot.getText();

                    if(!input.isBlank() && input.matches("^\\d+$")){

                        closeAnvilGuiPage(stateSnapshot.getPlayer());
                        writtenGraceTimeValue.put(playerId, input);

                        return List.of(AnvilGUI.ResponseAction.openInventory(conformationInv(stateSnapshot.getPlayer(), MapList.WRITTENGRACETIMEVALUE, "null", "null")));
                    } else {
                        return List.of(AnvilGUI.ResponseAction.updateTitle("Error, Try again.", true));
                    }
                })
                .interactableSlots()
                .itemLeft(getWriteableItem())
                .title("Set grace time.")
                .plugin(plugin)
                .open(openAnvilGuiPage(player));
    }

    public void setStarterItems(Player player){
        new AnvilGUI.Builder()
                .onClose(stateSnapshot -> closeAnvilGuiPage(stateSnapshot.getPlayer()))
                .onClick((slot, stateSnapshot)->{
                    if(slot != AnvilGUI.Slot.OUTPUT){
                        return Collections.emptyList();
                    }

                    stateSnapshot.getPlayer().sendMessage(stateSnapshot.getText());
                    UUID playerId = stateSnapshot.getPlayer().getUniqueId();

                    String input = stateSnapshot.getText();

                    if(!input.isBlank() && correctItemMaterialName(input)){

                        closeAnvilGuiPage(stateSnapshot.getPlayer());
                        writtenStarterItemsValue.put(playerId, input);

                        return List.of(AnvilGUI.ResponseAction.openInventory(conformationInv(stateSnapshot.getPlayer(), MapList.WRITTENSTARTERITEMSVALUE, "null", "null")));
                    } else {
                        return List.of(AnvilGUI.ResponseAction.updateTitle("Error, Try again.", true));
                    }
                })
                .interactableSlots()
                .itemLeft(getWriteableItem())
                .text("Item, amount")
                .title("Add starter item.")
                .plugin(plugin)
                .open(openAnvilGuiPage(player));
    }

    public void setLootItems(Player player){
        new AnvilGUI.Builder()
                .onClose(stateSnapshot -> closeAnvilGuiPage(stateSnapshot.getPlayer()))
                .onClick((slot, stateSnapshot)->{
                    if(slot != AnvilGUI.Slot.OUTPUT){
                        return Collections.emptyList();
                    }

                    stateSnapshot.getPlayer().sendMessage(stateSnapshot.getText());
                    UUID playerId = stateSnapshot.getPlayer().getUniqueId();

                    String input = stateSnapshot.getText();

                    if(!input.isBlank() && correctItemMaterialName(input)){

                        closeAnvilGuiPage(stateSnapshot.getPlayer());
                        writtenLootItemsValue.put(playerId, input);

                        return List.of(AnvilGUI.ResponseAction.openInventory(conformationInv(stateSnapshot.getPlayer(), MapList.WRITTENLOOTITEMSVALUE, "null", "null")));
                    } else {
                        return List.of(AnvilGUI.ResponseAction.updateTitle("Error, Try again.", true));
                    }
                })
                .interactableSlots()
                .itemLeft(getWriteableItem())
                .text("Item, amount")
                .title("Add loot item.")
                .plugin(plugin)
                .open(openAnvilGuiPage(player));
    }

    public void setBlacklistedBlocks(Player player){
        new AnvilGUI.Builder()
                .onClose(stateSnapshot -> closeAnvilGuiPage(stateSnapshot.getPlayer()))
                .onClick((slot, stateSnapshot)->{
                    if(slot != AnvilGUI.Slot.OUTPUT){
                        return Collections.emptyList();
                    }

                    stateSnapshot.getPlayer().sendMessage(stateSnapshot.getText());
                    UUID playerId = stateSnapshot.getPlayer().getUniqueId();

                    String input = stateSnapshot.getText();

                    if(!input.isBlank() && correctBlockMaterialName(input)){

                        closeAnvilGuiPage(stateSnapshot.getPlayer());
                        writtenBlacklistetBlocksValue.put(playerId, input);

                        return List.of(AnvilGUI.ResponseAction.openInventory(conformationInv(stateSnapshot.getPlayer(), MapList.WRITTENBLACKLISTEDBLOCKSVALUE, "null", "null")));
                    } else {
                        return List.of(AnvilGUI.ResponseAction.updateTitle("Error, Try again.", true));
                    }
                })
                .interactableSlots()
                .itemLeft(getWriteableItem())
                .title("Add blacklisted block.")
                .plugin(plugin)
                .open(openAnvilGuiPage(player));
    }

    private boolean correctItemMaterialName(String input){
        String[] parts = input.split(",");
        if(parts.length == 0){
            return false;
        }

        String inputName = parts[0].trim().replace(" ", "_").toUpperCase();

        Material material = Material.getMaterial(inputName);

        if(material == null) return false;

        String amount = parts[1].trim().replace(" ", "");
        Bukkit.broadcastMessage("Amount is: " + amount);
        return !amount.isEmpty();
    }

    private boolean correctBlockMaterialName(String input){
        String formattedInput = input.trim().replace(" ", "_").toLowerCase();

        BlockType block = BlockTypes.get(formattedInput);

        return block != null;
    }
    public String parseItem(String rawInput){
        String[] parts = rawInput.split(",");
        if(parts.length == 0){
            return null;
        }

        String inputName = parts[0].trim().replace(" ", "_").toUpperCase();

        Material material = Material.getMaterial(inputName);

        if(material == null){
            return null;
        }

        return material.toString();
    }

    public int parseItemAmount(String rawInput){
        String[] parts = rawInput.split(",");
        if(parts.length == 0){
            return -1;
        }
        String cleanUpValue = parts[1].trim().replace(" ", "");

        return Integer.parseInt(cleanUpValue);
    }

    public String parseBlacklistedBlock(String input){
        String formattedInput = input.trim().replace(" ", "_").toLowerCase();

        BlockType block = BlockTypes.get(formattedInput);

        if(block == null){
            return null;
        }

        return block.toString();
    }
    public Map<UUID, String> getWrittenMap(Player player){
        UUID playerId = player.getUniqueId();

        for(Map<UUID, String> itemMap : writtenAnvilItemList){

            if(itemMap.containsKey(playerId)){
                return itemMap;
            }

        }
        return  null;
    }

    public MapList getWrittenMapEnum(Player player){
        UUID playerId = player.getUniqueId();

        for(Map<UUID, String> enumMap : writtenAnvilItemList){

            if(enumMap.containsKey(playerId)){
                return MapList.valueOf(enumMap.toString().toUpperCase());
            }

        }

        return  null;
    }

    public void clearMap(Player player){
        UUID playerId = player.getUniqueId();

        long lastInteractTime = lastInteracted.getOrDefault(playerId, 0L);
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastInteractTime < 500) {
            return;
        }

        lastInteracted.put(playerId, currentTime);

        Map<UUID, String> writtenList = getWrittenMap(player);

        if(!writtenList.containsKey(playerId)){
            return;
        }

        writtenList.remove(playerId, writtenList.get(playerId));

    }

    public Player openAnvilGuiPage(Player player){
        UUID playerId = player.getUniqueId();
        anvilGUIUsers.add(playerId);
        return player;
    }

    public void closeAnvilGuiPage(Player player){
        UUID playerId = player.getUniqueId();
        anvilGUIUsers.remove(playerId);
    }

    public Inventory conformationInv(Player player, MapList writtenMap, String arenaName, String place){
        int pageSize = 36;
        int infoSlot = 13;
        int confirmSlot = 20;
        int cancelSlot = 22;
        int tryAgainSlot = 24;
        String placeString = "ArenaName";

        List<Integer> nonBoarderItems = new ArrayList<>(Arrays.asList(infoSlot, confirmSlot, cancelSlot, tryAgainSlot));

        Inventory inv = Bukkit.createInventory(null, pageSize, "Are you sure?");

        if(writtenMap != null){
            switch(writtenMap){
                case WRITTENARENANAME:
                    inv.setItem(infoSlot, getWrittenItem(player, writtenArenaName));
                    inv.setItem(confirmSlot, getConfirmItem(placeString));
                    inv.setItem(tryAgainSlot, getTryAgainItem(placeString));
                    break;
                case WRITTENMINYVALUE:
                    inv.setItem(infoSlot, getWrittenItem(player, writtenMinYValue));
                    inv.setItem(confirmSlot, getConfirmItem("minY"));
                    inv.setItem(tryAgainSlot, getTryAgainItem("minY"));
                    break;
                case WRITTENMAXYVALUE:
                    inv.setItem(infoSlot, getWrittenItem(player, writtenMaxYValue));
                    inv.setItem(confirmSlot, getConfirmItem("maxY"));
                    inv.setItem(tryAgainSlot, getTryAgainItem("maxY"));
                    break;
                case WRITTENMINPLAYERSVALUE:
                    inv.setItem(infoSlot, getWrittenItem(player, writtenMinPlayersValue));
                    inv.setItem(confirmSlot, getConfirmItem("minPlayers"));
                    inv.setItem(tryAgainSlot, getTryAgainItem("minPlayers"));
                    break;
                case WRITTENMAXPLAYERSVALUE:
                    inv.setItem(infoSlot, getWrittenItem(player, writtenMaxPlayersValue));
                    inv.setItem(confirmSlot, getConfirmItem("maxPlayers"));
                    inv.setItem(tryAgainSlot, getTryAgainItem("maxPlayers"));
                    break;
                case WRITTENRISETIMEVALUE:
                    inv.setItem(infoSlot, getWrittenItem(player, writtenRiseTimeValue));
                    inv.setItem(confirmSlot, getConfirmItem("riseTime"));
                    inv.setItem(tryAgainSlot, getTryAgainItem("riseTime"));
                    break;
                case WRITTENGRACETIMEVALUE:
                    inv.setItem(infoSlot, getWrittenItem(player, writtenGraceTimeValue));
                    inv.setItem(confirmSlot, getConfirmItem("graceTime"));
                    inv.setItem(tryAgainSlot, getTryAgainItem("graceTime"));
                    break;
                case WRITTENSTARTERITEMSVALUE:
                    inv.setItem(infoSlot, getWrittenItem(player, writtenStarterItemsValue));
                    inv.setItem(confirmSlot, getConfirmItem("starterItems"));
                    inv.setItem(tryAgainSlot, getTryAgainItem("starterItems"));
                    break;
                case WRITTENLOOTITEMSVALUE:
                    inv.setItem(infoSlot, getWrittenItem(player, writtenLootItemsValue));
                    inv.setItem(confirmSlot, getConfirmItem("lootitems"));
                    inv.setItem(tryAgainSlot, getTryAgainItem("lootitems"));
                    break;
                case WRITTENBLACKLISTEDBLOCKSVALUE:
                    inv.setItem(infoSlot, getWrittenItem(player, writtenBlacklistetBlocksValue));
                    inv.setItem(confirmSlot, getConfirmItem("blacklistedBlocks"));
                    inv.setItem(tryAgainSlot, getTryAgainItem("blacklistedBlocks"));
                    break;
            }
        } else if (arenaManager.getArena(arenaName) != null && place.equalsIgnoreCase("arena")) {
            inv.setItem(infoSlot, getInfoItem(player, place, arenaName));
            inv.setItem(confirmSlot, getConfirmItem(place));
            inv.setItem(tryAgainSlot, getTryAgainItem(place));
        } else if (arenaManager.getArena(arenaName) != null && place.equalsIgnoreCase("lobby")){
            inv.setItem(infoSlot, getInfoItem(player, place, arenaName));
            inv.setItem(confirmSlot, getConfirmItem(place));
            inv.setItem(tryAgainSlot, getTryAgainItem(place));
        }

        /*if(!place.equalsIgnoreCase("null") && !arenaName.equalsIgnoreCase("null")){
            inv.setItem(infoSlot, getInfoItem(place, arenaName));
            inv.setItem(confirmSlot, getConfirmItem(place));
            inv.setItem(tryAgainSlot, getTryAgainItem(place));
        }*/

        inv.setItem(cancelSlot, getCancelItem(placeString));

        for(int i = 0; i < pageSize; i++){
            if(!nonBoarderItems.contains(i)){
                inv.setItem(i, getBorderItem());
            }
        }

        return inv;

    }

    public ItemStack getWrittenItem(Player player, Map<UUID, String> writtenMap){
        UUID playerId = player.getUniqueId();
        ItemStack nameItem = new ItemStack(Material.PAPER);
        ItemMeta meta = nameItem.getItemMeta();

        if(meta == null){
          return null;
        }

        meta.setDisplayName(writtenMap.get(playerId));
        meta.setLore(List.of(ChatColor.GRAY + "Chosen input"));
        nameItem.setItemMeta(meta);

        return nameItem;
    }

    public ItemStack getCreateArenaItem(){
        ItemStack create = new ItemStack(Material.ANVIL);
        ItemMeta createMeta = create.getItemMeta();
        assert createMeta != null;
        createMeta.setDisplayName(ChatColor.RED + "Create new arena");
        createMeta.setLore(List.of(ChatColor.GRAY + "Creates a new arena"));
        create.setItemMeta(createMeta);

        return create;
    }

    public ItemStack getWriteableItem(){
        ItemStack writeableItem = new ItemStack(Material.PAPER);
        ItemMeta meta = writeableItem.getItemMeta();
        if(meta == null) return null;
        meta.setDisplayName("Nameless");
        meta.setLore(List.of(ChatColor.GRAY + "Nameless"));
        writeableItem.setItemMeta(meta);
        return writeableItem;
    }

    public ItemStack getArenasItem(){
        ItemStack arenas = new ItemStack(Material.LAVA_BUCKET);
        ItemMeta arenasMeta = arenas.getItemMeta();
        if(arenasMeta == null) return null;
        arenasMeta.setDisplayName(ChatColor.RED + "Arenas");
        arenasMeta.setLore(List.of(ChatColor.GRAY + "List of every arena"));
        arenas.setItemMeta(arenasMeta);

        return arenas;
    }
    public ItemStack getBorderItem(){
        // Creates a border item for the menu
        ItemStack border = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        if(borderMeta == null) return null;
        borderMeta.setDisplayName("Border");
        borderMeta.setLore(List.of(" "));
        border.setItemMeta(borderMeta);

        return border;
    }
    public ItemStack getExitItem(){
        ItemStack exit = new ItemStack(Material.BARRIER);
        ItemMeta exitMeta = exit.getItemMeta();
        if(exitMeta == null) return null;
        exitMeta.setDisplayName(ChatColor.RED + "Exit");
        exitMeta.setLore(List.of(ChatColor.GRAY + "Exits the menu"));
        exit.setItemMeta(exitMeta);

        return exit;
    }
    public ItemStack getPageSelectorItem(String direction){
        ItemStack pageSelector = new ItemStack(Material.ARROW);
        ItemMeta meta = pageSelector.getItemMeta();
        if(meta == null) return null;

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
    public ItemStack getGoBackItem(){
        ItemStack goBackItem = new ItemStack(Material.ARROW);
        ItemMeta meta = goBackItem.getItemMeta();
        if(meta == null) return null;
        meta.setDisplayName(ChatColor.AQUA + "Go back.");
        meta.setLore(List.of(ChatColor.GRAY + "Returns to previous menu."));
        goBackItem.setItemMeta(meta);

        return goBackItem;
    }

    public ItemStack getPreviousPageItem(){
        ItemStack previousPageItem = new ItemStack(Material.ARROW);
        ItemMeta meta = previousPageItem.getItemMeta();
        if(meta == null) return null;
        meta.setDisplayName(ChatColor.AQUA + "Previous page.");
        meta.setLore(List.of(ChatColor.GRAY + "Returns to previous page."));
        previousPageItem.setItemMeta(meta);

        return previousPageItem;
    }

    public ItemStack getNextPageItem(){
        ItemStack nextPageItem = new ItemStack(Material.ARROW);
        ItemMeta meta = nextPageItem.getItemMeta();
        if(meta == null) return null;
        meta.setDisplayName(ChatColor.AQUA + "Next page.");
        meta.setLore(List.of(ChatColor.GRAY + "Goes to next page."));
        nextPageItem.setItemMeta(meta);

        return nextPageItem;
    }

    public ItemStack getJoinItem(){
        ItemStack joinItem = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta meta = joinItem.getItemMeta();
        if(meta == null) return null;
        meta.setDisplayName(ChatColor.GREEN + "Join Arena");
        meta.setLore(List.of(ChatColor.GRAY + "Joins selected arena"));
        joinItem.setItemMeta(meta);

        return joinItem;
    }

    public ItemStack getConfigItem(){
        ItemStack configItem = new ItemStack(Material.WRITABLE_BOOK);
        ItemMeta meta = configItem.getItemMeta();
        if(meta == null) return null;
        meta.setDisplayName(ChatColor.YELLOW + "Config");
        meta.setLore(List.of(ChatColor.GRAY + "Navigates to the config menu"));
        configItem.setItemMeta(meta);

        return configItem;
    }

    public ItemStack getNormalModeItem(String arenaName){
        Arena arena = arenaManager.getArena(arenaName);
        ItemStack normalModeItem = new ItemStack(Material.CAMPFIRE);
        ItemMeta meta = normalModeItem.getItemMeta();
        String loreInfo;
        if(meta == null) return null;

        if(arena.getCurrentGameMode() == null){
            loreInfo = arenaManager.getGameMode(arenaName);
        } else loreInfo = arena.getCurrentGameMode();


        meta.setDisplayName(ChatColor.DARK_AQUA + "normal mode");
        meta.setLore(Arrays.asList(ChatColor.GRAY + "Sets the arena mode to normal.", "Current mode: " + loreInfo));
        normalModeItem.setItemMeta(meta);

        return normalModeItem;
    }

    public ItemStack getCompModeItem(String arenaName){
        Arena arena = arenaManager.getArena(arenaName);
        ItemStack  compModeItem = new ItemStack(Material.SOUL_CAMPFIRE);
        ItemMeta meta = compModeItem.getItemMeta();
        String loreInfo;
        if(meta == null) return null;

        if(arena.getCurrentGameMode() == null){
            loreInfo = arenaManager.getGameMode(arenaName);
        } else loreInfo = arena.getCurrentGameMode();

        meta.setDisplayName(ChatColor.DARK_RED + "Competition mode");
        meta.setLore(Arrays.asList(ChatColor.GRAY + "Sets the arena mode to competition.", "Current mode: " + loreInfo));
        compModeItem.setItemMeta(meta);

        return compModeItem;
    }

    public ItemStack getSetArenaItem(String arenaName){
        ItemStack setArenaItem = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = setArenaItem.getItemMeta();
        if(meta == null) return null;

        Location pos1 = arenaManager.getArenaLocation(arenaName, "pos1") == null ? null : arenaManager.getArenaLocation(arenaName, "pos1");
        Location pos2 = arenaManager.getArenaLocation(arenaName, "pos2") == null ? null :  arenaManager.getArenaLocation(arenaName, "pos2");

        List<String> loreInfo = getStrings(pos1, pos2);

        meta.setDisplayName(ChatColor.GOLD + "Set arena area");
        meta.setLore(Arrays.asList(ChatColor.GRAY + "Player gets a selection tool for choosing where the arena is.", ChatColor.GRAY + "Current values:", ChatColor.GRAY + "Pos1 X: " + loreInfo.get(0), ChatColor.GRAY + "Pos1 Y: " + loreInfo.get(1), ChatColor.GRAY + "Pos1 Z: " + loreInfo.get(2),
                loreInfo.get(3),ChatColor.GRAY + "Pos2 X: " + loreInfo.get(4), ChatColor.GRAY + "Pos2 Y: " + loreInfo.get(5), ChatColor.GRAY + "Pos2 Z: " + loreInfo.get(6)));
        setArenaItem.setItemMeta(meta);

        return setArenaItem;
    }

    private static List<String> getStrings(Location pos1, Location pos2) {
        List<String> loreInfo = new ArrayList<>();

        if(pos1 != null){
            loreInfo.add(String.valueOf(pos1.getX()));
            loreInfo.add(String.valueOf(pos1.getY()));
            loreInfo.add(String.valueOf(pos1.getZ()));
        } else {
            loreInfo.add("");
            loreInfo.add("");
            loreInfo.add("");
        }

        loreInfo.add("");

        if(pos2 != null){
            loreInfo.add(String.valueOf(pos2.getX()));
            loreInfo.add(String.valueOf(pos2.getY()));
            loreInfo.add(String.valueOf(pos2.getZ()));
        } else {
            loreInfo.add("");
            loreInfo.add("");
            loreInfo.add("");

        }
        return loreInfo;
    }

    public ItemStack getSetLobbyItem(String arenaName){
        ItemStack setLobbyItem = new ItemStack(Material.BAMBOO);
        ItemMeta meta = setLobbyItem.getItemMeta();
        if(meta == null) return null;

        Location pos1 = arenaManager.getLobbyLocation(arenaName, "pos1") == null ? null : arenaManager.getLobbyLocation(arenaName, "pos1");
        Location pos2 = arenaManager.getLobbyLocation(arenaName, "pos2") == null ? null : arenaManager.getLobbyLocation(arenaName, "pos2");

        List<String> loreInfo = getStrings(pos1, pos2);

        meta.setDisplayName(ChatColor.DARK_GRAY + "Set lobby area");
        meta.setLore(List.of(ChatColor.GRAY + "Player gets a selection tool for choosing where the lobby is.", ChatColor.GRAY + "Current values:", ChatColor.GRAY + "Pos1 X: " + loreInfo.get(0), ChatColor.GRAY + "Pos1 Y: " + loreInfo.get(1), ChatColor.GRAY + "Pos1 Z: " + loreInfo.get(2),
                loreInfo.get(3), ChatColor.GRAY + "Pos2 X: " + loreInfo.get(4), ChatColor.GRAY + "Pos2 Y: " + loreInfo.get(5), ChatColor.GRAY + "Pos2 Z: " + loreInfo.get(6)));
        setLobbyItem.setItemMeta(meta);

        return setLobbyItem;
    }

    public ItemStack getMinPlayersItem(String arenaName){
        ItemStack minPlayersItem = new ItemStack(Material.SOUL_TORCH);
        ItemMeta meta = minPlayersItem.getItemMeta();
        if(meta == null) return null;

        String loreInfo = arenaManager.getConfigValue(arenaName, "players.minPlayers");

        meta.setDisplayName(ChatColor.BLUE + "Set minimum players");
        meta.setLore(Arrays.asList(ChatColor.GRAY + "Lets player enter the minimum amount of players", ChatColor.GRAY + "required for a game to start in normal mode.", ChatColor.GRAY + "Current value: " + loreInfo));
        // meta.setLore(List.of(ChatColor.GRAY + "Lets player enter the minimum amount of players \n required for a game to start in normal mode. \nCurrent value: " + loreInfo));
        minPlayersItem.setItemMeta(meta);

        return minPlayersItem;
    }

    public ItemStack getMaxPlayersItem(String arenaName){
        ItemStack maxPlayersItem = new ItemStack(Material.SOUL_LANTERN);
        ItemMeta meta = maxPlayersItem.getItemMeta();
        if(meta == null) return null;

        String loreInfo = arenaManager.getConfigValue(arenaName, "players.maxPlayers");

        meta.setDisplayName(ChatColor.DARK_BLUE + "Set maximum players");
        meta.setLore(Arrays.asList(ChatColor.GRAY + "Lets player enter the minimum amount of players allowed in a game.", ChatColor.GRAY + "Current value: " + loreInfo));
        // meta.setLore(List.of(ChatColor.GRAY + "Lets player enter the minimum amount of players allowed in a game. \n Current value: " + loreInfo));
        maxPlayersItem.setItemMeta(meta);

        return maxPlayersItem;
    }

    public ItemStack getMinYLevelItem(String arenaName){
        ItemStack minYLevelItem = new ItemStack(Material.WILD_ARMOR_TRIM_SMITHING_TEMPLATE);
        ItemMeta meta = minYLevelItem.getItemMeta();
        if(meta == null) return null;

        String loreInfo = arenaManager.getConfigValue(arenaName, "Y-levels.Ymin");

        meta.setDisplayName(ChatColor.RED + "Set min Y-level");
        meta.setLore(List.of(ChatColor.GRAY + "Lets player enter lowest Y-level for spawns. \nCurrent value: " + loreInfo));
        minYLevelItem.setItemMeta(meta);

        return minYLevelItem;
    }

    public ItemStack getMaxYLevelItem(String arenaName){
        ItemStack maxYLevelItem = new ItemStack(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE);
        ItemMeta meta = maxYLevelItem.getItemMeta();
        if(meta == null) return null;

        String loreInfo = arenaManager.getConfigValue(arenaName, "Y-levels.Ymax");

        meta.setDisplayName(ChatColor.RED + "Set max Y-level");
        meta.setLore(List.of(ChatColor.GRAY + "Lets player enter highest Y-level for spawns. \nCurrent value: " +loreInfo));
        maxYLevelItem.setItemMeta(meta);

        return maxYLevelItem;
    }

    public ItemStack getGenerateSpawnsItem(String arenaName){
        ItemStack generateSpawnsItem = new ItemStack(Material.ENDER_EYE);
        ItemMeta meta = generateSpawnsItem.getItemMeta();
        if(meta == null) return null;

        String loreInfo = arenaManager.checkYlevels(arenaName) ? "Arena is ready for generating spawns" : "You haven't set the Y-levels yet!";

        meta.setDisplayName(ChatColor.DARK_AQUA + "Generate spawns");
        meta.setLore(Arrays.asList(ChatColor.GRAY + "Generates 150 spawnpoints.", "Current status: " + loreInfo));
        generateSpawnsItem.setItemMeta(meta);

        return generateSpawnsItem;
    }

    public ItemStack getRiseTimeItem(String arenaName){
        ItemStack riseTimeItem = new ItemStack(Material.REPEATER);
        ItemMeta meta = riseTimeItem.getItemMeta();
        if(meta == null) return null;

        String loreInfo = arenaManager.getConfigValue(arenaName, "timeValues.lavadelay");

        meta.setDisplayName(ChatColor.RED + "Set rise time");
        meta.setLore(Arrays.asList(ChatColor.GRAY + "Changes the duration between each time the lava rises.", ChatColor.GRAY + "Current duration: " + loreInfo + " seconds."));
        // meta.setLore(List.of(ChatColor.GRAY + "Lets player enter rise time duration. \nCurrent value: " + loreInfo));
        riseTimeItem.setItemMeta(meta);

        return riseTimeItem;
    }

    public ItemStack getGraceTimeItem(String arenaName){
        ItemStack graceTimeItem = new ItemStack(Material.CLOCK);
        ItemMeta meta = graceTimeItem.getItemMeta();
        if(meta == null) return null;

        String loreInfo = arenaManager.getConfigValue(arenaName, "timeValues.gracePeriod");

        meta.setDisplayName(ChatColor.GOLD + "Set grace time");
        meta.setLore(Arrays.asList(ChatColor.GRAY + "Changes how long the grace time lasts.", ChatColor.GRAY + "Current duration: " + loreInfo + " seconds."));
        // meta.setLore(List.of(ChatColor.GRAY + "Lets player enter grace time duration. \nCurrent value: " + loreInfo));
        graceTimeItem.setItemMeta(meta);

        return graceTimeItem;
    }

    public ItemStack getResetArenaItem(){
        ItemStack resetArenaItem = new ItemStack(Material.TOTEM_OF_UNDYING);
        ItemMeta meta = resetArenaItem.getItemMeta();
        if(meta == null) return null;
        meta.setDisplayName(ChatColor.DARK_PURPLE + "Reset arena");
        meta.setLore(List.of(ChatColor.GRAY + "Lets player reset the arena."));
        resetArenaItem.setItemMeta(meta);

        return resetArenaItem;
    }

    public ItemStack getDeleteArenaItem(){
        ItemStack deleteArenaItem = new ItemStack(Material.TNT);
        ItemMeta meta = deleteArenaItem.getItemMeta();
        if(meta == null) return null;
        meta.setDisplayName(ChatColor.RED + "Delete arena");
        meta.setLore(List.of(ChatColor.GRAY + "Warning: This deletes the arena!"));
        deleteArenaItem.setItemMeta(meta);

        return deleteArenaItem;
    }

    public ItemStack getYesItem(String arenaName){
        ItemStack yesItem = new ItemStack(Material.TNT);
        ItemMeta meta = yesItem.getItemMeta();
        if(meta == null) return null;
        meta.setDisplayName(ChatColor.RED + "Yes I want to delete");
        meta.setLore(List.of(ChatColor.GRAY + "Warning! You are about to delete: " + arenaName));
        yesItem.setItemMeta(meta);

        return yesItem;
    }

    public ItemStack getNoItem(){
        ItemStack noItem = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = noItem.getItemMeta();
        if(meta == null) return null;
        meta.setDisplayName(ChatColor.GREEN + "No, what was I thinking?");
        meta.setLore(List.of(ChatColor.GRAY + "You get to keep this arena."));
        noItem.setItemMeta(meta);

        return noItem;
    }

    public ItemStack getSwitchPvPModeItem(){
        ItemStack switchPvPModeItem = new ItemStack(Material.STONE_SWORD);
        ItemMeta meta = switchPvPModeItem.getItemMeta();
        if(meta == null) return null;

        boolean infoBool = arenaManager.getCurrentPvPMode();

        meta.setDisplayName(ChatColor.GREEN + "Switch global PvP mode");
        // meta.setLore(List.of(ChatColor.GRAY + "Turns global PvP off or on."));
        meta.setLore(Arrays.asList(ChatColor.GRAY + "Turns global PvP off or on.", ChatColor.GRAY + "Current mode: " + infoBool));
        switchPvPModeItem.setItemMeta(meta);

        return switchPvPModeItem;
    }

    public ItemStack getStarterItemsItem(){
        ItemStack starterItemsItem = new ItemStack(Material.CHEST);
        ItemMeta meta = starterItemsItem.getItemMeta();

        if (meta == null){
            return null;
        }

        meta.setDisplayName(ChatColor.RED + "Set starter items");
        meta.setLore(List.of(ChatColor.GRAY + "Lets the player view and enter starter items."));
        starterItemsItem.setItemMeta(meta);

        return starterItemsItem;
    }

    public ItemStack getBlacklistedBlocksItem(){
        ItemStack blacklistedBlocksItem = new ItemStack(Material.CHEST);
        ItemMeta meta = blacklistedBlocksItem.getItemMeta();
        if(meta == null) return null;
        meta.setDisplayName(ChatColor.RED + "Set blacklisted blocks");
        meta.setLore(List.of(ChatColor.GRAY + "Lets the player view and enter blacklisted blocks."));
        blacklistedBlocksItem.setItemMeta(meta);

        return blacklistedBlocksItem;
    }

    public ItemStack getInfoItem(Player player, String place, String arenaName){
        if(arenaManager.getArena(arenaName) == null) return null;

        ItemStack infoItem = new ItemStack(Material.PAPER);
        ItemMeta meta = infoItem.getItemMeta();

        if(meta == null){
            return null;
        }

        if(player != null){
            UUID playerId = player.getUniqueId();
            String loc1 = place.equals("arena")  ? arenaManager.writtenArenaLocation1.get(playerId) : arenaManager.writtenLobbyLocation1.get(playerId);
            String loc2 = place.equals("arena")  ? arenaManager.writtenArenaLocation2.get(playerId) : arenaManager.writtenLobbyLocation2.get(playerId);

            meta.setDisplayName(ChatColor.WHITE + place + " locations");
            meta.setLore(Arrays.asList(ChatColor.GRAY + "Pos1: " + parseLocation(loc1),ChatColor.GRAY + "Pos2: " + parseLocation(loc2)));
            infoItem.setItemMeta(meta);

        } else {

            meta.setDisplayName(ChatColor.WHITE + place + " locations");
            meta.setLore(List.of(ChatColor.GRAY + "Pos1: " + "none" + " " + "Pos2: " + "none"));
            infoItem.setItemMeta(meta);

        }





        return infoItem;

    }

    private List<String> parseLocation(String input){
        List<String> result = new ArrayList<>();
        String[] filteredInput = input.split(",");
        String posX = filteredInput[1];
        String posY = filteredInput[2];
        String[] unfilteredPosZ = filteredInput[3].split(".p");
        String posZ = unfilteredPosZ[0];

        result.add(posX);
        result.add(posY);
        result.add(posZ);
        return result;
    }

    public ItemStack getConfirmItem(String item){
        ItemStack confirmItem = new ItemStack(Material.POLISHED_BLACKSTONE_BUTTON);
        ItemMeta meta = confirmItem.getItemMeta();

        if(meta == null){
            return null;
        }

        switch (item.toLowerCase()){
            case "arenaname":
                meta.setDisplayName(ChatColor.RED + "Confirm name");
                meta.setLore(List.of(ChatColor.GRAY + "Confirms the inputted name."));
                confirmItem.setItemMeta(meta);
                break;
            case "miny":
                meta.setDisplayName(ChatColor.RED + "Confirm MinY");
                meta.setLore(List.of(ChatColor.GRAY + "Confirms the inputted MinY value."));
                confirmItem.setItemMeta(meta);
                break;
            case "maxy":
                meta.setDisplayName(ChatColor.RED + "Confirm MaxY");
                meta.setLore(List.of(ChatColor.GRAY + "Confirms the inputted MaxY value."));
                confirmItem.setItemMeta(meta);
                break;
            case "minplayers":
                meta.setDisplayName(ChatColor.RED + "Confirm minimum players");
                meta.setLore(List.of(ChatColor.GRAY + "Confirms the inputted minimum players value."));
                confirmItem.setItemMeta(meta);
                break;
            case "maxplayers":
                meta.setDisplayName(ChatColor.RED + "Confirm maximum players");
                meta.setLore(List.of(ChatColor.GRAY + "Confirms the inputted maximum players value."));
                confirmItem.setItemMeta(meta);
                break;
            case "risetime":
                meta.setDisplayName(ChatColor.RED + "Confirm rise time");
                meta.setLore(List.of(ChatColor.GRAY + "Confirms the inputted rise time value."));
                confirmItem.setItemMeta(meta);
                break;
            case "gracetime":
                meta.setDisplayName(ChatColor.RED + "Confirm grace time");
                meta.setLore(List.of(ChatColor.GRAY + "Confirms the inputted grace time value."));
                confirmItem.setItemMeta(meta);
                break;
            case "starteritems":
                meta.setDisplayName(ChatColor.RED + "Confirm starting item");
                meta.setLore(List.of(ChatColor.GRAY + "Confirms the inputted starter item."));
                confirmItem.setItemMeta(meta);
                break;
            case "lootitems":
                meta.setDisplayName(ChatColor.RED + "Confirm loot item");
                meta.setLore(List.of(ChatColor.GRAY + "Confirms the inputted loot item."));
                confirmItem.setItemMeta(meta);
                break;
            case "blacklistedblocks":
                meta.setDisplayName(ChatColor.RED + "Confirm blacklisted block");
                meta.setLore(List.of(ChatColor.GRAY + "Confirms the inputted blacklisted block."));
                confirmItem.setItemMeta(meta);
                break;

            default:
                meta.setDisplayName(ChatColor.RED + "Confirm " + item + " placement");
                meta.setLore(List.of(ChatColor.GRAY + "Confirms the placement of the " + item +"."));
                confirmItem.setItemMeta(meta);
        }

        return confirmItem;
    }

    public ItemStack getCancelItem(String place){
        ItemStack cancelItem = new ItemStack(Material.BARRIER);
        ItemMeta meta = cancelItem.getItemMeta();

        if(meta == null){
            return null;
        }

        meta.setDisplayName(ChatColor.RED + "Cancel");
        meta.setLore(List.of(ChatColor.GRAY + "Cancel the " + place + " placement selection."));
        cancelItem.setItemMeta(meta);

        return cancelItem;
    }

    public ItemStack getTryAgainItem(String item){
        ItemStack tryAgainItem = new ItemStack(Material.SPYGLASS);
        ItemMeta meta = tryAgainItem.getItemMeta();

        if(meta == null){
            return null;
        }

        switch(item.toLowerCase()){
            case "arenaname":
                meta.setDisplayName(ChatColor.RED + "Try again");
                meta.setLore(List.of(ChatColor.GRAY + "Lets the player input the " + item + " name again."));
                tryAgainItem.setItemMeta(meta);
                break;
            case "miny", "maxy", "minplayers", "maxplayers", "risetime", "gracetime", "starteritems", "lootitems" , "blacklistedblocks":
                meta.setDisplayName(ChatColor.RED + "Try again");
                meta.setLore(List.of(ChatColor.GRAY + "Lets the player input the " + item + " value again."));
                tryAgainItem.setItemMeta(meta);
                break;
            default:
                meta.setDisplayName(ChatColor.RED + "Try again");
                meta.setLore(List.of(ChatColor.GRAY + "Lets the player input the " + item + "placement again."));
                tryAgainItem.setItemMeta(meta);
        }

        return tryAgainItem;
    }

    public ItemStack getArenaItem(String arenaName){
        ItemStack arenaItem = new ItemStack(Material.LAVA_BUCKET);
        ItemMeta meta = arenaItem.getItemMeta();
        if(meta == null) return null;
        meta.setDisplayName(ChatColor.RED + arenaName);

        ArrayList<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Players: " + arenaManager.getPlayers(arenaName) + "/" + arenaManager.getMaxPlayers(arenaName));
        lore.add(ChatColor.GRAY + "Current gamestage: " + arenaManager.getGameStage(arenaName));
        meta.setLore(lore);

        arenaItem.setItemMeta(meta);

        return arenaItem;
    }

    public ItemStack getArenaWandItem(Player player){
        ItemStack arenaWandItem = new ItemStack(Material.STICK, 1);
        ItemMeta meta = arenaWandItem.getItemMeta();
        if(meta == null) return null;

        meta.setDisplayName(ChatColor.GREEN + "ArenaWand");
        meta.setLore(List.of(ChatColor.GRAY + "Wand for selecting the locations for the arena."));
        arenaWandItem.setItemMeta(meta);

        return arenaWandItem;
    }

    public ItemStack getLobbyWandItem(Player player){
        ItemStack lobbyWandItem = new ItemStack(Material.STICK, 1);
        ItemMeta meta = lobbyWandItem.getItemMeta();
        if(meta == null) return null;

        meta.setDisplayName(ChatColor.GREEN + "LobbyWand");
        meta.setLore(List.of(ChatColor.GRAY + "Wand for selecting the locations for the lobby."));
        lobbyWandItem.setItemMeta(meta);

        return lobbyWandItem;
    }

    public ItemStack getNew(String type){
        ItemStack newStarterItem = new ItemStack(Material.WRITABLE_BOOK);
        ItemMeta meta = newStarterItem.getItemMeta();

        if(meta == null){
            return null;
        }

        switch (type.toLowerCase()){
            case "item":
                meta.setDisplayName(ChatColor.RED + "Add new starter item");
                meta.setLore(List.of(ChatColor.GRAY + "Adds new starter item."));
                newStarterItem.setItemMeta(meta);
                break;
            case "lootitem":
                meta.setDisplayName(ChatColor.RED + "Add new Loot item");
                meta.setLore(List.of(ChatColor.GRAY + "Adds new loot item"));
                newStarterItem.setItemMeta(meta);
                break;
            case "block":
                meta.setDisplayName(ChatColor.RED + "Add new blacklisted block");
                meta.setLore(List.of(ChatColor.GRAY + "Adds new blacklisted block."));
                newStarterItem.setItemMeta(meta);
                return newStarterItem;
        }



        /*if(type.equalsIgnoreCase("item")){

            meta.setDisplayName(ChatColor.RED + "Add new starter item");
            meta.setLore(List.of(ChatColor.GRAY + "Adds new starter item."));
            newStarterItem.setItemMeta(meta);

        } else if(type.equalsIgnoreCase("block")){

            meta.setDisplayName(ChatColor.RED + "Add new blacklisted block");
            meta.setLore(List.of(ChatColor.GRAY + "Adds new blacklisted block."));
            newStarterItem.setItemMeta(meta);

        }*/

        return newStarterItem;

    }

    public ItemStack getUpdateArenaItem(){
        ItemStack reloadItem = new ItemStack(Material.CREEPER_HEAD);
        ItemMeta meta = reloadItem.getItemMeta();
        if(meta == null) return null;

        meta.setDisplayName(ChatColor.BLUE + "Update arena");
        meta.setLore(List.of(ChatColor.GRAY + "Use after you have changed the arena or lobby landscape."));

        reloadItem.setItemMeta(meta);

        return reloadItem;
    }

    public ItemStack getStartItem(String arenaName){
        ItemStack startItem = new ItemStack(Material.PISTON);
        ItemMeta meta = startItem.getItemMeta();
        if(meta == null) return null;

        meta.setDisplayName(ChatColor.GREEN + "Start match");
        meta.setLore(Arrays.asList(ChatColor.GRAY + "Starts a match in the selected arena.", ChatColor.GRAY + "Players: " + arenaManager.getPlayers(arenaName) + "/" + arenaManager.getMaxPlayers(arenaName)));
        startItem.setItemMeta(meta);

        return startItem;
    }

    public ItemStack getRestartItem(String arenaName){
        ItemStack restartItem = new ItemStack(Material.STICKY_PISTON);
        ItemMeta meta = restartItem.getItemMeta();
        if(meta == null) return null;

        meta.setDisplayName(ChatColor.GREEN + "Restart match");
        meta.setLore(List.of(ChatColor.GRAY + "Restarts the match."));
        restartItem.setItemMeta(meta);

        return restartItem;
    }

    public ItemStack getLootchestItem(){
        ItemStack lootchest = new ItemStack(Material.CHEST);
        ItemMeta meta = lootchest.getItemMeta();
        if(meta == null) return null;

        meta.setDisplayName(ChatColor.GOLD + "Get loot chest");
        meta.setLore(List.of(ChatColor.GRAY + "A Loot chest! Wonder what's inside?"));
        lootchest.setItemMeta(meta);

        return lootchest;
    }

    public ItemStack getLootchestConfigItem(){
        ItemStack lootchestConfig = new ItemStack(Material.CHEST);
        ItemMeta meta = lootchestConfig.getItemMeta();
        if(meta == null) return null;

        meta.setDisplayName(ChatColor.GOLD + "Loot chest config");
        meta.setLore(List.of(ChatColor.GRAY + "Config loot items inside the loot chest."));
        lootchestConfig.setItemMeta(meta);

        return lootchestConfig;
    }

    public ItemStack getExitLootplacementModeItem(){
        ItemStack exitLootplacementModeItem = new ItemStack(Material.BARRIER);
        ItemMeta meta = exitLootplacementModeItem.getItemMeta();
        if(meta == null) return null;

        meta.setDisplayName("Stop lootchest placement");
        meta.setLore(List.of(ChatColor.GRAY + "Exits the lootchest placement mode"));

        exitLootplacementModeItem.setItemMeta(meta);

        return exitLootplacementModeItem;

    }

    public int parseValueToInt(Player player){
        UUID playerId = player.getUniqueId();
        String rawValue;

        for(Map<UUID, String> entry : writtenAnvilItemList){
            if(entry.containsKey(playerId)){
                rawValue = entry.get(playerId);
                return Integer.parseInt(rawValue);
            }
        }

        return -1; // Returns -1 if the method couldn't find anything.
    }

    private List<Integer> getNonBorderSlots (){
        List<Integer> slotIndexes = new ArrayList<>(Arrays.asList(0, 36, 40, 44));
        slotIndexes.addAll(availableArenaSlots);
        Collections.sort(slotIndexes);

        return slotIndexes;
    }
    private int amountOfPages(){
        int totalAmountOfArenas = arenaManager.getArenas().size();
        return (totalAmountOfArenas + amountOfArenasPerPage - 1) / amountOfArenasPerPage;
    }


}
