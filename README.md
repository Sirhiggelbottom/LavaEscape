# LavaEscape
The hottest PvP/PvE arena game!



## Features
  - An exciting game for your server! Have players fight each other while they try to escape the soaring heat from the rising lava!
  - A Menu system that lets you create and configure arenas, all without needing to input any commands!
  - Add starter items for players in each arena!
  - Stop players from ruining your beautiful decorations by blacklisting certain blocks!
  - Add loot chests! (Not implemented yet)
  - Add a lobby area for players to inspect the arena before the match and for dead players to spectate from!
  - Add a delay called Grace time for players to gather resources and loot without having to worry about each other or the rising lava.
  - Different arena modes!
    - Server mode:
      - Arena runs automatically, when enough players join, the match starts after a delay.
      - Nice to have when running matches over and over again.
    - Competition mode:
      - Lets you start the match exactly when you want! Great for competitions!



## Installation
  - Download the LavaEscape plugin
  - Place it into your server plugins folder
  - Restart the server to enable the plugin



## Dependencies
  - Minecraft 1.20.1
  - Worldedit 7.3.0


## Usage
When you have downloaded and enabled the plugin, use: `/lava menu`.
This will open the menu system, there you will be greeted by the main menu.
### Main menu
Here you can:
- Open a list of all existing arenas
- Create a new arena (**Only for OP**)

- [ ] Add picture of main menu.

### Create a new arena
This will bring up a anvil GUI page (Kudos to WesJD for creating AnvilGUI).  
Input the name of the arena just like you would when renaming an item, then press the output item.  
If you inputed a valid name, you will be asked to confirm the name for the arena. If not you will have to input a valid one again.

Arena creation page:
- [ ] Add picture of AnvilGUI start page

Valid name:
- [ ] Add picture of accepted arenaName

Invalid name:
- [ ] Add picture of invalid arenaName



### Arenas list
Here you can view all existing arenas, when you hover over each arena it will display what game stage it is in, the player amount and the player limit.
When you click on a arena, you get up to five options:\
**Normal players**:
  - Join arena

**OP players**:
- Join arena
- Config arena
- Start arena (**Competition mode**)
- Restart arena (**Competition mode**)
- Reset arena

Arena page for normal players.
- [ ] Add picture of arena page for normal players:

Arena page for OP players.
- [ ] Add picture of arena page for OP players:



### Config
Here you will find all the settings necessary for the arena to work.  
These are sorted in chronological order
Required settings:
- Set areas for the Arena and Lobby:
  - Set arena
  - Set lobby
- Set the spawnpoint area
  - Min Y - Sets the lowest Y-level for players to spawn.
  - Max Y - Sets the highest Y-level for players to spawn.
- Generate spawns
  - Generates 150 spawnspoints for players to spawn at, when a match starts the plugin assigns a spawnpoint to each player.

Optional settings:

- Set the time in between each time the lava rises.
- Opens a anvil GUI page for setting the Rise time.
- Set the Grace time.
  - Opens a anvil GUI page for setting the Grace time.
- Starter items:
  Opens a list displaying each starter item and the option to add new.
  When you click on a starter item you get the option to remove it.
-[ ] Add a picture for the starter items\
- Blacklisted blocks:
  Opens a list displaying each blacklisted block which the players won't be able to destroy, this includes OP players incase they destroy a block on accident.
  When you click on a blacklisted block you get the option to remove it.
-[ ] Add a picture for the blacklisted blocks.

And the option to delete the entire arena.
