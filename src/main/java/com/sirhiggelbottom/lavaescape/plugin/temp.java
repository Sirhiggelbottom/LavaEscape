package com.sirhiggelbottom.lavaescape.plugin;

public class temp {
    /*switch (displayName.toLowerCase()) {
        case "arenas": // Sends player to the arenas menu
            event.setCancelled(true);
            arenaMenu.createArenaPages(player, 1);
            //menuManager.createArenaPage(player, currentPage);
            break;
        case "create new arena": // Prompts the player to input an arenaName for new arena.
            //@Todo: Create logic equal to /Lava create <arenaName>, prompts player to input arenaName.
            waitingForInput.put(playerId, 1);
            event.setCancelled(true);
            break;
        case "exit": // Closes the menu
            if (arenaMenu.subpagesContains(player)) {
                arenaMenu.closeSubPage(player);
            }
            if (arenaMenu.arenaContains(player)) {
                arenaMenu.closeArenaPage(player, arenaMenu.getPreviousArenaPage(player));
            }
            player.closeInventory();
            event.setCancelled(true);
            break;
                        *//*case "next page": // Sends player to next page in Arenas menu
                            menuManager.createArenaPage(player, currentPage + 1);
                            event.setCancelled(true);
                            break;
                        case "previous page": // Sends player to previous page in Arenas menu
                            menuManager.createArenaPage(player, currentPage - 1);
                            event.setCancelled(true);
                            break;*//*
        case "join": // Makes the player join the selected arena.
            //@Todo: Create logic equal to /Lava join <arenaName>.
            event.setCancelled(true);
            break;
        case "go back": // Sends the player to the previous menu.
            if (arenaMenu.subpagesContains(player)) {
                arenaMenu.changeSubPage(player, arenaMenu.getPreviousSubPage(player));
            } else if (arenaMenu.arenaContains(player)) {
                arenaMenu.backToMainMenu(player, arenaMenu.getPreviousMainMenu(player));
            }
            event.setCancelled(true);
            break;
        case "config": // Sends the player to the config menu.
            arenaMenu.openSubPage(player, displayName);
            event.setCancelled(true);
            break;
        case "normal mode": // Sets the gameMode to Normal or Server mode.
            //@Todo: Create logic that sets the gameMode to normal mode for the selected arena.
            event.setCancelled(true);
            break;
        case "competition mode": // Sets the gameMode to Competition mode.
            //@Todo: Create logic that sets the gameMode to Competition for the selected arena.
            event.setCancelled(true);
            break;
        case "set arena": // Gives the player a wand to set the area for the Arena, when the player has set 2 pos, the positions are then saved.
            //@Todo: Create logic that gives the player a wand, and checks if the player has set 2 positions for the arena.
            event.setCancelled(true);
            break;
        case "set lobby": // Gives the player a wand to set the area for the Lobby, when the player has set 2 pos, the positions are then saved.
            //@Todo: Create logic that gives the player a wand, and checks if the player has set 2 positions for the lobby.
            event.setCancelled(true);
            break;
        case "confirm": // Confirms the selection of positions for creating either arena or lobby.
            //@Todo: Create logic that saves the pos selection to the corresponding area.
            event.setCancelled(true);
            break;
        case "cancel": // Closes the menu.
            player.closeInventory();
            event.setCancelled(true);
            break;
        case "try again": // Lets the player try again.
            //@Todo: Create logic that lets the player select positions again.
            event.setCancelled(true);
            break;
        case "min players": // Sets the minimum amount of players required for the match to start for the specific arena, when in Normal mode.
            //@Todo: Create logic that sets the minimum amount of players required for the match to start for the specific arena, when in Normal mode.
            event.setCancelled(true);
            break;
        case "max players": // Sets the maximum amount players allowed in the specific arena.
            //@Todo: Create logic that sets the maximum amount of players allowed in the specific arena.
            event.setCancelled(true);
            break;
        case "min y": // Sets the lowest y-level for where the spawnpoints can be generated.
            //@Todo: Create logic that sets the minimum Y-level for creating spawnpoints.
            event.setCancelled(true);
            break;
        case "max y": // Sets the highest y-level for where the spawnpoints can be generated.
            //@Todo: Create logic that sets the maximum Y-level for creating spawnpoints.
            event.setCancelled(true);
            break;
        case "generate spawns": // Creates spawnpoints based on the y-levels and the area that has been set for the arena.
            //@Todo: Create logic that generates spawnpoints.
            event.setCancelled(true);
            break;
        case "rise time": // Sets the time between each time the y-level of the lava increases.
            //@Todo: Create logic that sets the Lava delay.
            event.setCancelled(true);
            break;
        case "grace time": // Sets the time from when the match starts to when the lava starts rising.
            //@Todo: Create logic that sets the Grace time.
            event.setCancelled(true);
            break;
        case "reset arena": // Resets the arena
            //@Todo: Create logic that resets the arena.
            event.setCancelled(true);
            break;
        case "delete arena": // Opens up a new menu that asks if the player is sure that they want to delete the selected arena.
            //@Todo: Create logic that sends the player to the deleteMenu.
            event.setCancelled(true);
            break;
        case "yes": // Confirms that the player wants to delete the selected arena.
            //@Todo: Create logic that deletes the selected arena.
            event.setCancelled(true);
            break;
        case "no": // Stops the player from deleting the selected arena, this sends the player back to the config menu for the selected arena.
            //@Todo: Create logic that sends the player back to the config menu for the selected arena.
            event.setCancelled(true);
            break;
        case "starter items": // Sends the player to the menu for starting-items menu
            //@Todo: Create logic that sends the player to the correct menu, and create the menu. It should display all the starting items and the amount.
            event.setCancelled(true);
            break;
        case "blacklisted blocks": // Sends the player to the menu for blacklisted blocks.
            //@Todo: Create logic that sends the player to the correct menu, and create the menu. It should display all the blacklisted blocks.
            event.setCancelled(true);
            break;
        default:
            player.sendMessage("Error, closing the menu " + "\n" + "displayName: " + displayName + "\nWritten item: " + menuManager.writtenItem.get(playerId));
            event.setCancelled(true);
            player.closeInventory();
    }*/
}
