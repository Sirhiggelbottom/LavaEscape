package com.sirhiggelbottom.lavaescape.plugin.arena;

import org.bukkit.Location;

public class Cuboid {
    private Location corner1;
    private Location corner2;
    // Additional data fields if needed

    public Cuboid(Location corner1, Location corner2) {
        this.corner1 = corner1;
        this.corner2 = corner2;
    }

    public Object getCorner1() {
        return corner1;
    }

    public Object getCorner2() {
        return corner2;
    }

    // Getters and setters for corner1 and corner2
}

