package com.rouesvm.servback.items;

import com.rouesvm.servback.ui.EnderBackpackGui;
import com.rouesvm.servback.ui.GlobalBackpackGui;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;

public class ItemList {
    public static Item SMALL_BACKPACK = register(new ContainerItem("small", 9));
    public static Item MEDIUM_BACKPACK = register(new ContainerItem("medium", 9 * 2));
    public static Item LARGE_BACKPACK = register(new ContainerItem("large", 9 * 3));

    public static Item ENDER_BACKPACK = register(new GuiItem("ender") {
        @Override
        public void openGui(ServerPlayerEntity player, ItemStack stack) {
            new EnderBackpackGui(player, stack);
        }
    });
    public static Item GLOBAL_BACKPACK = register(new GuiItem("global") {
        @Override
        public void openGui(ServerPlayerEntity player, ItemStack stack) {
            new GlobalBackpackGui(player, stack);
        }
    });

    public static Item register(BasicPolymerItem item) {
       return Registry.register(Registries.ITEM, item.getIdentifier(), item);
    }

    public static void initialize() {}
}
