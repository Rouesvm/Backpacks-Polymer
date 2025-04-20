package com.rouesvm.servback.registry;

import com.rouesvm.servback.items.BasicPolymerItem;
import com.rouesvm.servback.items.ContainerItem;
import com.rouesvm.servback.items.GuiItem;
import com.rouesvm.servback.ui.EnderBackpackGui;
import com.rouesvm.servback.ui.GlobalBackpackGui;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;

import static com.rouesvm.servback.Main.configuration;

public class BackpackItemRegistry {
    // how do I read this
    public static final Item SMALL_BACKPACK = register(new ContainerItem("small", configuration.getInstance().small_backpack_size));
    public static final Item BLACK_SMALL_BACKPACK = register(new ContainerItem("black_small", configuration.getInstance().small_backpack_size));
    public static final Item BLUE_SMALL_BACKPACK = register(new ContainerItem("blue_small", configuration.getInstance().small_backpack_size));
    public static final Item CYAN_SMALL_BACKPACK = register(new ContainerItem("cyan_small", configuration.getInstance().small_backpack_size));
    public static final Item GREEN_SMALL_BACKPACK = register(new ContainerItem("green_small", configuration.getInstance().small_backpack_size));
    public static final Item LIGHT_BLUE_SMALL_BACKPACK = register(new ContainerItem("light_blue_small", configuration.getInstance().small_backpack_size));
    public static final Item LIGHT_GRAY_SMALL_BACKPACK = register(new ContainerItem("light_gray_small", configuration.getInstance().small_backpack_size));
    public static final Item LIME_SMALL_BACKPACK = register(new ContainerItem("lime_small", configuration.getInstance().small_backpack_size));
    public static final Item MAGENTA_SMALL_BACKPACK = register(new ContainerItem("magenta_small", configuration.getInstance().small_backpack_size));
    public static final Item ORANGE_SMALL_BACKPACK = register(new ContainerItem("orange_small", configuration.getInstance().small_backpack_size));
    public static final Item PINK_SMALL_BACKPACK = register(new ContainerItem("pink_small", configuration.getInstance().small_backpack_size));
    public static final Item PURPLE_SMALL_BACKPACK = register(new ContainerItem("purple_small", configuration.getInstance().small_backpack_size));
    public static final Item RED_SMALL_BACKPACK = register(new ContainerItem("red_small", configuration.getInstance().small_backpack_size));
    public static final Item WHITE_SMALL_BACKPACK = register(new ContainerItem("white_small", configuration.getInstance().small_backpack_size));
    public static final Item YELLOW_SMALL_BACKPACK = register(new ContainerItem("yellow_small", configuration.getInstance().small_backpack_size));

    public static final Item MEDIUM_BACKPACK = register(new ContainerItem("medium", configuration.getInstance().medium_backpack_size));
    public static final Item BLACK_MEDIUM_BACKPACK = register(new ContainerItem("black_medium", configuration.getInstance().medium_backpack_size));
    public static final Item BLUE_MEDIUM_BACKPACK = register(new ContainerItem("blue_medium", configuration.getInstance().medium_backpack_size));
    public static final Item CYAN_MEDIUM_BACKPACK = register(new ContainerItem("cyan_medium", configuration.getInstance().medium_backpack_size));
    public static final Item GREEN_MEDIUM_BACKPACK = register(new ContainerItem("green_medium", configuration.getInstance().medium_backpack_size));
    public static final Item LIGHT_BLUE_MEDIUM_BACKPACK = register(new ContainerItem("light_blue_medium", configuration.getInstance().medium_backpack_size));
    public static final Item LIGHT_GRAY_MEDIUM_BACKPACK = register(new ContainerItem("light_gray_medium", configuration.getInstance().medium_backpack_size));
    public static final Item LIME_MEDIUM_BACKPACK = register(new ContainerItem("lime_medium", configuration.getInstance().medium_backpack_size));
    public static final Item MAGENTA_MEDIUM_BACKPACK = register(new ContainerItem("magenta_medium", configuration.getInstance().medium_backpack_size));
    public static final Item ORANGE_MEDIUM_BACKPACK = register(new ContainerItem("orange_medium", configuration.getInstance().medium_backpack_size));
    public static final Item PINK_MEDIUM_BACKPACK = register(new ContainerItem("pink_medium", configuration.getInstance().medium_backpack_size));
    public static final Item PURPLE_MEDIUM_BACKPACK = register(new ContainerItem("purple_medium", configuration.getInstance().medium_backpack_size));
    public static final Item RED_MEDIUM_BACKPACK = register(new ContainerItem("red_medium", configuration.getInstance().medium_backpack_size));
    public static final Item WHITE_MEDIUM_BACKPACK = register(new ContainerItem("white_medium", configuration.getInstance().medium_backpack_size));
    public static final Item YELLOW_MEDIUM_BACKPACK = register(new ContainerItem("yellow_medium", configuration.getInstance().medium_backpack_size));

    public static final Item LARGE_BACKPACK = register(new ContainerItem("large", configuration.getInstance().large_backpack_size));
    public static final Item BLACK_LARGE_BACKPACK = register(new ContainerItem("black_large", configuration.getInstance().large_backpack_size));
    public static final Item BLUE_LARGE_BACKPACK = register(new ContainerItem("blue_large", configuration.getInstance().large_backpack_size));
    public static final Item CYAN_LARGE_BACKPACK = register(new ContainerItem("cyan_large", configuration.getInstance().large_backpack_size));
    public static final Item GREEN_LARGE_BACKPACK = register(new ContainerItem("green_large", configuration.getInstance().large_backpack_size));
    public static final Item LIGHT_BLUE_LARGE_BACKPACK = register(new ContainerItem("light_blue_large", configuration.getInstance().large_backpack_size));
    public static final Item LIGHT_GRAY_LARGE_BACKPACK = register(new ContainerItem("light_gray_large", configuration.getInstance().large_backpack_size));
    public static final Item LIME_LARGE_BACKPACK = register(new ContainerItem("lime_large", configuration.getInstance().large_backpack_size));
    public static final Item MAGENTA_LARGE_BACKPACK = register(new ContainerItem("magenta_large", configuration.getInstance().large_backpack_size));
    public static final Item ORANGE_LARGE_BACKPACK = register(new ContainerItem("orange_large", configuration.getInstance().large_backpack_size));
    public static final Item PINK_LARGE_BACKPACK = register(new ContainerItem("pink_large", configuration.getInstance().large_backpack_size));
    public static final Item PURPLE_LARGE_BACKPACK = register(new ContainerItem("purple_large", configuration.getInstance().large_backpack_size));
    public static final Item RED_LARGE_BACKPACK = register(new ContainerItem("red_large", configuration.getInstance().large_backpack_size));
    public static final Item WHITE_LARGE_BACKPACK = register(new ContainerItem("white_large", configuration.getInstance().large_backpack_size));
    public static final Item YELLOW_LARGE_BACKPACK = register(new ContainerItem("yellow_large", configuration.getInstance().large_backpack_size));

    public static final Item ENDER_BACKPACK = register(new GuiItem("ender") {
        @Override
        public void openGui(ServerPlayerEntity player, ItemStack stack) {
            new EnderBackpackGui(player, stack);
        }
    });
    public static final Item GLOBAL_BACKPACK = register(new GuiItem("global") {
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
