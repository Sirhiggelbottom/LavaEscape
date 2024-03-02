package com.sirhiggelbottom.lavaescape.plugin;

import org.bukkit.inventory.ItemStack;

public class LootItem {
    private ItemStack itemStack;
    private float rarity;

    public LootItem(ItemStack itemStack, float rarity) {
        this.itemStack = itemStack;
        this.rarity = rarity;
    }

    public ItemStack getItemStack(){
        return itemStack;
    }

    public void setItemStack(ItemStack itemStack){
        this.itemStack = itemStack;
    }

    public void setRarity(int rarity){
        this.rarity = rarity;
    }

    public float getRarity(){
        return rarity;
    }
}
