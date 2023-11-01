package com.sirhiggelbottom.lavaescape.plugin;

import com.sirhiggelbottom.lavaescape.plugin.arena.ArenaManager;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

public class Main extends JavaPlugin {
    private HashMap <String, ArenaManager> arenas = new HashMap<>();

    public HashMap<String, ArenaManager> getArenas(){
        return arenas;
    }
    public Location setPos1(Location location) {
        return location;
    }

    public Location setPos2(Location location) {
        return location;
    }
}