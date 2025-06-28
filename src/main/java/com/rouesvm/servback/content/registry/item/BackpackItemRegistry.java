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
import java.util.List;
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

    public static <T extends BasicPolymerBlockItem> T register(T item) {
        return Registry.register(Registries.ITEM, item.getIdentifier(), item);
    }

    public static DyeColor getBackpackDyeColor(ContainerItem item) {
        return BACKPACKS.getOrDefault(item.getSize(), BACKPACKS.get(1))
               .entrySet().stream()
               .filter(entry -> entry.getValue().equals(item))
               .map(Map.Entry::getKey)
               .findFirst()
                .map(DyeColor::byIndex)
               .orElse(DyeColor.WHITE);
    }

    public static Item getBackpack(@NotNull DyeColor color, int order) {
        return getBackpack(color.getIndex() + 1, order);
    }

    public static Item getBackpack(int id, int order) {
        var defaultMap = BACKPACKS.get(1);
        return BACKPACKS
                .getOrDefault(order, defaultMap)
                .getOrDefault(id, defaultMap.get(0));
    }

    public static void create(Map<Integer, Item> itemMap, Integer order, String name, int slots) {
        itemMap.put(order, register(new ContainerItem(name, slots)));
    }

    public static void initialize() {
        Configuration.Instance instance = Configuration.instance();

        if (instance.types_of_backpacks.isEmpty()) {
            Map<Integer, Item> sizeMap = new HashMap<>(DyeColor.values().length);
            create(sizeMap, 0, "small", 9);
            BACKPACKS.put(1, sizeMap);

            return;
        }

        int registeredSize = 0;

        for (Map.Entry<Integer, Configuration.BackpackType> entry : instance.types_of_backpacks.entrySet()) {
            int order = entry.getKey();
            Configuration.BackpackType backpackType = entry.getValue();

            int backpackSlots = backpackType.slots();
            List<String> backpackStrings = backpackType.backpacks();
            List<String> blacklistedDyes = backpackType.dyeBlacklist();

            Map<Integer, Item> sizeMap = new HashMap<>(DyeColor.values().length);

            String defaultName = backpackStrings != null ? backpackStrings.getFirst() : entry.getKey() + "_backpack";

            if (backpackStrings != null) {
                if (backpackType.dyeable()) {
                    for (String backpackName : backpackStrings) {
                        create(sizeMap, 0, backpackName, backpackSlots);

                        for (DyeColor color : DyeColor.values()) {
                            String dyeColor = color.name().toLowerCase();
                            String name = dyeColor + "_";
                            if (blacklistedDyes != null && blacklistedDyes.contains(dyeColor)) continue;
                            create(sizeMap, color.getIndex() + 1, name + backpackName, backpackSlots);
                            registeredSize++;
                        }
                    }
                } else {
                    create(sizeMap, 1, defaultName, backpackSlots);
                    registeredSize++;
                }
            }

            BACKPACKS.put(order, sizeMap);
        }

        ServerBackpacks.LOGGER.info("Finished registering {} items.", registeredSize);
    }
}
