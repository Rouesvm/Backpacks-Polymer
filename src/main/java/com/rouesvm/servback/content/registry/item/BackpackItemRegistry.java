package com.rouesvm.servback.content.registry.item;

import com.rouesvm.servback.ServerBackpacks;
import com.rouesvm.servback.content.item.BasicPolymerBlockItem;
import com.rouesvm.servback.content.item.BundleGuiItem;
import com.rouesvm.servback.content.item.ContainerItem;
import com.rouesvm.servback.content.registry.block.BackpackBlockRegistry;
import com.rouesvm.servback.technical.config.Configuration;
import com.rouesvm.servback.technical.data.BackpackManager;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.DyeColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class BackpackItemRegistry {
    public static final Map<Integer, Map<Integer, Item>> BACKPACKS = new HashMap<>();

    public static final Item ENDER_BACKPACK = register(new BundleGuiItem("ender", BackpackBlockRegistry.ENDER_BACKPACK) {
        @Override
        public Inventory getInventory(@Nullable ServerPlayerEntity player, @Nullable ItemStack stack) {
            return player != null ? player.getEnderChestInventory() : null;
        }
    });
    public static final Item GLOBAL_BACKPACK = register(new BundleGuiItem("global", BackpackBlockRegistry.GLOBAL_BACKPACK) {
        @Override
        public Inventory getInventory(@Nullable ServerPlayerEntity player, @Nullable ItemStack stack) {
            return BackpackManager.getGlobalInventory();
        }
    });

    private static final DyeColor defaultDye = DyeColor.BROWN;

    public static Item register(BasicPolymerBlockItem item) {
        return Registry.register(Registries.ITEM, item.getIdentifier(), item);
    }

    public static DyeColor getBackpackDyeColor(ContainerItem item) {
        return BACKPACKS.getOrDefault(item.getSize(), BACKPACKS.get(1))
               .entrySet().stream()
               .filter(entry -> entry.getValue().equals(item))
               .map(Map.Entry::getKey)
               .findFirst()
                .map(DyeColor::byId)
               .orElse(defaultDye);
    }

    public static Item getBackpack(@NotNull DyeColor color, int order) {
        var defaultMap = BACKPACKS.get(1);
        return BACKPACKS
                .getOrDefault(order, defaultMap)
                .getOrDefault(color.getId(), defaultMap.get(defaultDye.getId()));
    }

    public static void create(Map<Integer, Item> itemMap, Integer order, String name, int slots) {
        itemMap.put(order, register(new ContainerItem(name, slots)));
    }

    public static void initialize() {
        Configuration.Instance instance = Configuration.instance();

        if (instance.types_of_backpacks == null) {
            Map<Integer, Item> sizeMap = new HashMap<>(DyeColor.values().length);
            create(sizeMap, defaultDye.getId(), "small", 9);
            BACKPACKS.put(1, sizeMap);

            return;
        }

        int registeredSize = 0;

        for (Map.Entry<Integer, Configuration.BackpackType> entry : instance.types_of_backpacks.entrySet()) {
            int order = entry.getKey();
            Configuration.BackpackType backpackType = entry.getValue();

            int backpackSlots = backpackType.slots();
            String backpackString = backpackType.name();

            Map<Integer, Item> sizeMap = new HashMap<>(DyeColor.values().length);

            if (backpackType.dyeable()) {
                for (DyeColor color : DyeColor.values()) {
                    String name = color.name().toLowerCase() + "_";
                    if (color == DyeColor.BROWN) name = "";

                    create(sizeMap, color.getId(), name + backpackString, backpackSlots);
                    registeredSize++;
                }
            } else {
                create(sizeMap, defaultDye.getId(), backpackString, backpackSlots);
                registeredSize++;
            }

            BACKPACKS.put(order, sizeMap);
        }

        ServerBackpacks.LOGGER.info("Finished registering {} items.", registeredSize);
    }
}
