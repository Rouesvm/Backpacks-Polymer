package com.rouesvm.servback.items;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import com.rouesvm.servback.Main;

public class ItemList {

    public static Item SMALL_BACKPACK = register(new BackpackItem("Small", 9), "small");
    public static Item MEDIUM_BACKPACK = register(new BackpackItem("Medium", 9 * 2), "medium");
    public static Item LARGE_BACKPACK = register(new BackpackItem("Large", 9 * 3), "large");

    public static Item ENDER_BACKPACK = register(new EnderBackpackItem(), "ender");
    public static Item GLOBAL_BACKPACK = register(new GlobalBackpackItem(), "global");

    public static Item register(Item item, String id) {
       return Registry.register(Registries.ITEM, Identifier.of(Main.MOD_ID, id), item);
    }

    public static void initialize() {}
}
