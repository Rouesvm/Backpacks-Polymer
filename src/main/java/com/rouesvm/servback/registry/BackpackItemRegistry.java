package com.rouesvm.servback.registry;

import com.rouesvm.servback.items.BasicPolymerItem;
import com.rouesvm.servback.items.BundleContainerItem;
import com.rouesvm.servback.items.GuiItem;
import com.rouesvm.servback.ui.EnderBackpackGui;
import com.rouesvm.servback.ui.GlobalBackpackGui;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.DyeColor;

import java.util.HashMap;
import java.util.Map;

import static com.rouesvm.servback.Main.configuration;

public class BackpackItemRegistry {
    public static Map<DyeColor, Item> SMALL = new HashMap<>(DyeColor.values().length);
    public static Map<DyeColor, Item> MEDIUM = new HashMap<>(DyeColor.values().length);
    public static Map<DyeColor, Item> LARGE = new HashMap<>(DyeColor.values().length);

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

    public static void initialize() {
        for (DyeColor color : DyeColor.values()) {
            String name = color.name().toLowerCase() + "_";

            if (color == DyeColor.GRAY) continue;
            if (color == DyeColor.BROWN) name = "";

            SMALL.put(color, register(new BundleContainerItem(name + "small", configuration.getInstance().small_backpack_size, color)));
            MEDIUM.put(color, register(new BundleContainerItem(name + "medium", configuration.getInstance().medium_backpack_size, color)));
            LARGE.put(color, register(new BundleContainerItem(name + "large", configuration.getInstance().large_backpack_size, color)));
        }

        SMALL.put(DyeColor.LIGHT_GRAY, SMALL.get(DyeColor.LIGHT_GRAY));
        MEDIUM.put(DyeColor.LIGHT_GRAY, MEDIUM.get(DyeColor.LIGHT_GRAY));
        LARGE.put(DyeColor.LIGHT_GRAY, LARGE.get(DyeColor.LIGHT_GRAY));
    }
}
