package com.rouesvm.servback.registry;

import com.rouesvm.servback.Main;
import com.rouesvm.servback.config.Configuration;
import com.rouesvm.servback.item.BasicPolymerBlockItem;
import com.rouesvm.servback.item.BasicPolymerItem;
import com.rouesvm.servback.item.BundleContainerItem;
import com.rouesvm.servback.item.BundleGuiItem;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.DyeColor;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class BackpackItemRegistry {
    public static Map<DyeColor, Item> SMALL = new HashMap<>(DyeColor.values().length);
    public static Map<DyeColor, Item> MEDIUM = new HashMap<>(DyeColor.values().length);
    public static Map<DyeColor, Item> LARGE = new HashMap<>(DyeColor.values().length);

    public static final Item ENDER_BACKPACK = register(new BundleGuiItem("ender") {
        @Override
        public Inventory getInventory(ServerPlayerEntity player) {
            return player.getEnderChestInventory();
        }
    });
    public static final Item GLOBAL_BACKPACK = register(new BundleGuiItem("global") {
        @Override
        public Inventory getInventory(ServerPlayerEntity player) {
            return Main.getInventory();
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

    public static Item getBackpack(@NotNull DyeColor color, int size) {
        switch (size) {
            case 1 -> {
                return SMALL.get(color);
            } case 2 -> {
                return MEDIUM.get(color);
            } case 3 -> {
                return LARGE.get(color);
            } default -> {
                return SMALL.get(DyeColor.BROWN);
            }
        }
    }
    
    public static void initialize() {
        for (DyeColor color : DyeColor.values()) {
            String name = color.name().toLowerCase() + "_";

            if (color == DyeColor.GRAY) continue;
            if (color == DyeColor.BROWN) name = "";

            create(SMALL, color, name + "small", Configuration.getInstance().small_backpack_size);
            create(MEDIUM, color, name + "medium", Configuration.getInstance().medium_backpack_size);
            create(LARGE, color, name + "large", Configuration.getInstance().large_backpack_size);
        }

        SMALL.put(DyeColor.GRAY, getBackpack(DyeColor.LIGHT_GRAY, 1));
        MEDIUM.put(DyeColor.GRAY, getBackpack(DyeColor.LIGHT_GRAY, 2));
        LARGE.put(DyeColor.GRAY, getBackpack(DyeColor.LIGHT_GRAY, 3));
    }
}
