package com.rouesvm.servback.items;

import com.rouesvm.servback.ui.EnderBackpackGui;
import com.rouesvm.servback.ui.GlobalBackpackGui;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;

public class ItemRegistry {
    public static Item SMALL_BACKPACK = register(new ContainerItem("small", 9));
    public static Item BLACK_SMALL_BACKPACK = register(new ContainerItem("black_small", 9));
    public static Item BLUE_SMALL_BACKPACK = register(new ContainerItem("blue_small", 9));
    public static Item CYAN_SMALL_BACKPACK = register(new ContainerItem("cyan_small", 9));
    public static Item GREEN_SMALL_BACKPACK = register(new ContainerItem("green_small", 9));
    public static Item LIGHT_BLUE_SMALL_BACKPACK = register(new ContainerItem("light_blue_small", 9));
    public static Item LIGHT_GRAY_SMALL_BACKPACK = register(new ContainerItem("light_gray_small", 9));
    public static Item LIME_SMALL_BACKPACK = register(new ContainerItem("lime_small", 9));
    public static Item MAGENTA_SMALL_BACKPACK = register(new ContainerItem("magenta_small", 9));
    public static Item ORANGE_SMALL_BACKPACK = register(new ContainerItem("orange_small", 9));
    public static Item PINK_SMALL_BACKPACK = register(new ContainerItem("pink_small", 9));
    public static Item PURPLE_SMALL_BACKPACK = register(new ContainerItem("purple_small", 9));
    public static Item RED_SMALL_BACKPACK = register(new ContainerItem("red_small", 9));
    public static Item WHITE_SMALL_BACKPACK = register(new ContainerItem("white_small", 9));
    public static Item YELLOW_SMALL_BACKPACK = register(new ContainerItem("yellow_small", 9));

    public static Item MEDIUM_BACKPACK = register(new ContainerItem("medium", 9 * 2));
    public static Item BLACK_MEDIUM_BACKPACK = register(new ContainerItem("black_medium", 9 * 2));
    public static Item BLUE_MEDIUM_BACKPACK = register(new ContainerItem("blue_medium", 9 * 2));
    public static Item CYAN_MEDIUM_BACKPACK = register(new ContainerItem("cyan_medium", 9 * 2));
    public static Item GREEN_MEDIUM_BACKPACK = register(new ContainerItem("green_medium", 9 * 2));
    public static Item LIGHT_BLUE_MEDIUM_BACKPACK = register(new ContainerItem("light_blue_medium", 9 * 2));
    public static Item LIGHT_GRAY_MEDIUM_BACKPACK = register(new ContainerItem("light_gray_medium", 9 * 2));
    public static Item LIME_MEDIUM_BACKPACK = register(new ContainerItem("lime_medium", 9 * 2));
    public static Item MAGENTA_MEDIUM_BACKPACK = register(new ContainerItem("magenta_medium", 9 * 2));
    public static Item ORANGE_MEDIUM_BACKPACK = register(new ContainerItem("orange_medium", 9 * 2));
    public static Item PINK_MEDIUM_BACKPACK = register(new ContainerItem("pink_medium", 9 * 2));
    public static Item PURPLE_MEDIUM_BACKPACK = register(new ContainerItem("purple_medium", 9 * 2));
    public static Item RED_MEDIUM_BACKPACK = register(new ContainerItem("red_medium", 9 * 2));
    public static Item WHITE_MEDIUM_BACKPACK = register(new ContainerItem("white_medium", 9 * 2));
    public static Item YELLOW_MEDIUM_BACKPACK = register(new ContainerItem("yellow_medium", 9 * 2));

    public static Item LARGE_BACKPACK = register(new ContainerItem("large", 9 * 3));
    public static Item BLACK_LARGE_BACKPACK = register(new ContainerItem("black_large", 9 * 3));
    public static Item BLUE_LARGE_BACKPACK = register(new ContainerItem("blue_large", 9 * 3));
    public static Item CYAN_LARGE_BACKPACK = register(new ContainerItem("cyan_large", 9 * 3));
    public static Item GREEN_LARGE_BACKPACK = register(new ContainerItem("green_large", 9 * 3));
    public static Item LIGHT_BLUE_LARGE_BACKPACK = register(new ContainerItem("light_blue_large", 9 * 3));
    public static Item LIGHT_GRAY_LARGE_BACKPACK = register(new ContainerItem("light_gray_large", 9 * 3));
    public static Item LIME_LARGE_BACKPACK = register(new ContainerItem("lime_large", 9 * 3));
    public static Item MAGENTA_LARGE_BACKPACK = register(new ContainerItem("magenta_large", 9 * 3));
    public static Item ORANGE_LARGE_BACKPACK = register(new ContainerItem("orange_large", 9 * 3));
    public static Item PINK_LARGE_BACKPACK = register(new ContainerItem("pink_large", 9 * 3));
    public static Item PURPLE_LARGE_BACKPACK = register(new ContainerItem("purple_large", 9 * 3));
    public static Item RED_LARGE_BACKPACK = register(new ContainerItem("red_large", 9 * 3));
    public static Item WHITE_LARGE_BACKPACK = register(new ContainerItem("white_large", 9 * 3));
    public static Item YELLOW_LARGE_BACKPACK = register(new ContainerItem("yellow_large", 9 * 3));

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
