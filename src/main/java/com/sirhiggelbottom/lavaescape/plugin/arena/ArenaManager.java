package com.sirhiggelbottom.lavaescape.plugin.arena;

import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;

public class AreaManager {
    private Map<String, Cuboid> areas = new HashMap<>();

    public void addArea(String key, Location corner1, Location corner2) {
        Cuboid cuboid = new Cuboid(corner1, corner2);
        areas.put(key, cuboid);
    }

    public Cuboid getArea(String key) {
        return areas.get(key);
    }

    // You can add methods for modifying and managing areas as needed
}
