package com.sirhiggelbottom.lavaescape.plugin.events;

import com.sirhiggelbottom.lavaescape.plugin.Arena.Arena;
import com.sirhiggelbottom.lavaescape.plugin.LavaEscapePlugin;
import com.sirhiggelbottom.lavaescape.plugin.managers.ArenaManager;
import com.sirhiggelbottom.lavaescape.plugin.managers.GameManager;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GameEvents implements Listener {
    private final LavaEscapePlugin plugin;
    private final Map<UUID, Location[]> playerSelections;
    private final Arena arena;
    private final ArenaManager arenaManager;
    private final GameManager gameManager;

    public GameEvents(LavaEscapePlugin plugin, Arena arena, ArenaManager arenaManager, GameManager gameManager) {
        this.plugin = plugin;
        this.arenaManager = arenaManager;
        this.gameManager = gameManager;
        this.playerSelections = new HashMap<>();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.arena = arena;
    }
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item != null && item.getType() == Material.STICK && item.getItemMeta().getDisplayName().equals("Wand")) {
            Action action = event.getAction();

            // Cooldown logic to stop spam messages
            long lastInteractTime = lastInteract.getOrDefault(player.getUniqueId(), 0L);
            long currentTime = System.currentTimeMillis();
            if(currentTime - lastInteractTime < 500){
                return;
            }
            lastInteract.put(player.getUniqueId(), currentTime);

            // Check for left or right-click on a block
            if (action == Action.LEFT_CLICK_BLOCK || action == Action.RIGHT_CLICK_BLOCK) {
                UUID playerId = event.getPlayer().getUniqueId();
                Location[] selections = playerSelections.computeIfAbsent(playerId, k -> new Location[2]);

                if (action == Action.LEFT_CLICK_BLOCK) {
                    selections[0] = event.getClickedBlock().getLocation();
                    event.getPlayer().sendMessage("First position set.");
                } else if (action == Action.RIGHT_CLICK_BLOCK) {
                    selections[1] = event.getClickedBlock().getLocation();
                    event.getPlayer().sendMessage("Second position set.");
                }

                // Cancel the event to prevent double firing
                event.setCancelled(true);
            }
        }
    }
    @EventHandler
    public void playerWandBlockBreak(BlockBreakEvent event){
        ItemStack item = event.getPlayer().getItemInHand();
        //ItemStack item = event.getPlayer().getItemInUse();
        Block block = event.getBlock();

        if (block == null) return;

        event.setCancelled(item != null && item.getType() == Material.STICK && item.getItemMeta().getDisplayName().equals("Wand"));
        //event.setCancelled(item != null && item.getType() == Material.STICK && Objects.requireNonNull(item.getItemMeta()).getDisplayName().equals("Wand"));

    }
    public Location getFirstPosition(UUID playerId) {
        Location[] selections = playerSelections.get(playerId);
        return (selections != null && selections[0] != null) ? selections[0] : null;
    }
    public Location getSecondPosition(UUID playerId) {
        Location[] selections = playerSelections.get(playerId);
        return (selections != null && selections[1] != null) ? selections[1] : null;
    }
    private final Map<UUID , Long> lastInteract = new HashMap<>();
    @EventHandler
    public void onFatalDamage(EntityDamageEvent event){

        if(event.getEntity() instanceof Player){
            Player player = (Player) event.getEntity();

            if(player.getHealth() - event.getFinalDamage() <= 0){

                event.setCancelled(true);

                Arena playerArena = arenaManager.findPlayerArena(player);
                if(playerArena != null){

                    arenaManager.teleportLobby(player, playerArena.getName());
                    arenaManager.healPlayer(player);
                    arenaManager.restorePlayerInventory(player);
                    player.setGameMode(GameMode.ADVENTURE);
                    arenaManager.removePlayerFromArena(playerArena.getName(),player);

                }
            }
        }

    }
    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {

        UUID playerId = event.getDamager().getUniqueId();
        Player player = event.getDamager().getServer().getPlayer(playerId);
        Arena playerArena = arenaManager.findPlayerArena(player);

        if(playerArena != null){
            event.setCancelled(!playerArena.getGameState().equals(ArenaManager.GameState.LAVA) && !playerArena.getGameState().equals(ArenaManager.GameState.DEATHMATCH));
        } else event.setCancelled(true);

    }
}

