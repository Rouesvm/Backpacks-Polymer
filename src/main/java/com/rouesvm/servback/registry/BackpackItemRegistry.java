package com.rouesvm.servback.registry;

import com.rouesvm.servback.item.BasicPolymerBlockItem;
import com.rouesvm.servback.item.BasicPolymerItem;
import com.rouesvm.servback.item.BundleContainerItem;
import com.rouesvm.servback.item.GuiItem;
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

    public static Item register(BasicPolymerBlockItem item) {
        return Registry.register(Registries.ITEM, item.getIdentifier(), item);
    }

    // lazy
    public static void create(Map<DyeColor, Item> itemMap, DyeColor color, String name, int size) {
        itemMap.put(color, register(new BundleContainerItem(name, size, color)));
    }

    public static void initialize() {
        for (DyeColor color : DyeColor.values()) {
            String name = color.name().toLowerCase() + "_";

            if (color == DyeColor.GRAY) continue;
            if (color == DyeColor.BROWN) name = "";

            create(SMALL, color, name + "small", configuration.getInstance().small_backpack_size);
            create(MEDIUM, color, name + "medium", configuration.getInstance().medium_backpack_size);
            create(LARGE, color, name + "large", configuration.getInstance().large_backpack_size);
        }

        SMALL.put(DyeColor.GRAY, SMALL.get(DyeColor.LIGHT_GRAY));
        MEDIUM.put(DyeColor.GRAY, MEDIUM.get(DyeColor.LIGHT_GRAY));
        LARGE.put(DyeColor.GRAY, LARGE.get(DyeColor.LIGHT_GRAY));
    }
}
