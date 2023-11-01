package com.sirhiggelbottom.lavaescape.plugin.arena;


import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class Arenas implements Listener {
    private ArenaManager arenaManager;

    public Arenas(ArenaManager arenaManager) {
        this.arenaManager = arenaManager;
    }

    @EventHandler
    public void onPlayerRightClick(PlayerInteractEvent event){
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType().equals(Material.STICK) && event.getAction().equals(Action.RIGHT_CLICK_BLOCK)){
            Location pos2 = Objects.requireNonNull(event.getClickedBlock()).getLocation();

            arenaManager.setPos2(pos2);


        }
    }
}
