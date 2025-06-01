package com.rouesvm.servback.registry;

import com.rouesvm.servback.Main;
import com.rouesvm.servback.config.Configuration;
import com.rouesvm.servback.item.BasicPolymerBlockItem;
import com.rouesvm.servback.item.BundleGuiItem;
import com.rouesvm.servback.item.ContainerItem;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.DyeColor;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class BackpackItemRegistry {
    public static Map<Integer, Map<DyeColor, Item>> BACKPACKS = new HashMap<>();

    public static final Item ENDER_BACKPACK = register(new BundleGuiItem("ender", BackpackBlockRegistry.ENDER_BACKPACK) {
        @Override
        public Inventory getInventory(ServerPlayerEntity player, ItemStack stack) {
            return player.getEnderChestInventory();
        }
    });
    public static final Item GLOBAL_BACKPACK = register(new BundleGuiItem("global", BackpackBlockRegistry.GLOBAL_BACKPACK) {
        @Override
        public Inventory getInventory(ServerPlayerEntity player, ItemStack stack) {
            return Main.getInventory();
        }
    });

    public static Item register(BasicPolymerBlockItem item) {
        return Registry.register(Registries.ITEM, item.getIdentifier(), item);
    }

    public static DyeColor getBackpackDyeColor(ContainerItem item) {
        return BACKPACKS.getOrDefault(item.getSize(), BACKPACKS.get(1))
               .entrySet().stream()
               .filter(entry -> entry.getValue() == item)
               .map(Map.Entry::getKey)
               .findFirst()
               .orElse(DyeColor.BROWN);
    }

    public static Item getBackpack(@NotNull DyeColor color, int order) {
        var defaultMap = BACKPACKS.get(1);
        return BACKPACKS
                .getOrDefault(order, defaultMap)
                .getOrDefault(color, defaultMap.get(DyeColor.BROWN));
    }

    public static void create(Map<DyeColor, Item> itemMap, DyeColor color, String name, int slots) {
        itemMap.put(color, register(new ContainerItem(name, slots)));
    }

    public static void initialize() {
        Configuration.Instance instance = Configuration.getInstance();

        if (instance.types_of_backpacks == null) return;

        for (Map.Entry<Integer, Configuration.BackpackType> entry : instance.types_of_backpacks.entrySet()) {
            int order = entry.getKey();
            Configuration.BackpackType backpackType = entry.getValue();

            int backpackSlots = backpackType.slots();
            String backpackString = backpackType.name();

            Map<DyeColor, Item> sizeMap = new EnumMap<>(DyeColor.class);

            for (DyeColor color : DyeColor.values()) {
                String name = color.name().toLowerCase() + "_";
                if (color == DyeColor.BROWN) name = "";

                create(sizeMap, color, name + backpackString, backpackSlots);
            }

            BACKPACKS.put(order, sizeMap);
        }
    }
}
