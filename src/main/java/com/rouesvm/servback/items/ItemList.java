package com.rouesvm.servback.items;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import com.rouesvm.servback.Main;

public class ItemList {

    public static Item SMALL_BACKPACK = register(new ContainerItem("Small", 9));
    public static Item MEDIUM_BACKPACK = register(new ContainerItem("Medium", 9 * 2));
    public static Item LARGE_BACKPACK = register(new ContainerItem("Large", 9 * 3));

    public static Item ENDER_BACKPACK = register(new EnderBackpackItem());
    public static Item GLOBAL_BACKPACK = register(new GlobalBackpackItem());

    public static Item register(BasicPolymerItem item) {
       return Registry.register(Registries.ITEM, item.getIdentifier(), item);
    }

    public static void initialize() {}
}
