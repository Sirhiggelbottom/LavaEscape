package com.sirhiggelbottom.lavaescape.plugin.arena;


import org.bukkit.Location;

public class ArenaManager {

    private Location pos1;
    private Location pos2;

    public ArenaManager(Location point1, Location point2) {
        this.pos1 = null;
        this.pos2 = null;
    }


    public void setPos2(Location pos2) {
        this.pos2 = pos2;
    }

}

