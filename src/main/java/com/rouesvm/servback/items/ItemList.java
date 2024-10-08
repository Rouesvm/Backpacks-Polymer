package com.rouesvm.servback.items;

import com.rouesvm.servback.ui.EnderBackpackGui;
import com.rouesvm.servback.ui.GlobalBackpackGui;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;

public class ItemList {

    public static Item SMALL_BACKPACK = register(new ContainerItem("Small", 9));
    public static Item MEDIUM_BACKPACK = register(new ContainerItem("Medium", 9 * 2));
    public static Item LARGE_BACKPACK = register(new ContainerItem("Large", 9 * 3));

    public static Item ENDER_BACKPACK = register(new GuiItem("Ender") {
        @Override
        public SimpleGui createGui(ServerPlayerEntity player, ItemStack stack) {
            return new EnderBackpackGui(player, stack);
        }
    });

    public static Item GLOBAL_BACKPACK = register(new GuiItem("Global") {
        @Override
        public SimpleGui createGui(ServerPlayerEntity player, ItemStack stack) {
            return new GlobalBackpackGui(player, stack);
        }
    });

    public static Item ITEM_FILTER = register(new FilterItem("Filter"));

    public static Item register(BasicPolymerItem item) {
       return Registry.register(Registries.ITEM, item.getIdentifier(), item);
    }

    public static void initialize() {}
}
