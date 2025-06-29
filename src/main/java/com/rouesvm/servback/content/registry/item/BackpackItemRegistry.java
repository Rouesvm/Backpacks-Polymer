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

import java.util.*;

public class BackpackItemRegistry {
    public static final Map<Integer, Map<Integer, Item>> BACKPACKS = new HashMap<>();
    public static final Map<Integer, Set<Item>> BACKPACKS_UPGRADE_ORDER = new HashMap<>();

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

    public static int getBackpackUpgradeOrder(ContainerItem item) {
        for (var entry : BACKPACKS_UPGRADE_ORDER.entrySet()) {
            if (entry.getValue().contains(item)) {
                return entry.getKey();
            }
        }
        return 1;
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

    private static ContainerItem create(Map<Integer, Item> itemMap, Integer id, String name, int slots) {
        ContainerItem item = register(new ContainerItem(name, slots));
        itemMap.put(id, item);
        return item;
    }

    private static int registerDyeableBackpack(Map<Integer, Item> sizeMap,
                                               List<String> strings,
                                               List<String> blacklistedDyes,
                                               Set<Item> items,
                                               int size
    ) {
        int registeredSize = 0;
        for (String backpackName : strings) {
            items.add(create(sizeMap, 0, backpackName, size));
            registeredSize++;

            for (DyeColor color : DyeColor.values()) {
                String dyeColor = color.name().toLowerCase();
                if (blacklistedDyes != null && blacklistedDyes.contains(dyeColor)) continue;
                items.add(create(sizeMap, color.getIndex() + 1, dyeColor + "_" + backpackName, size));
                registeredSize++;
            }
        }

        return registeredSize;
    }

    public static void initialize() {
        Configuration.Instance instance = Configuration.instance();
        Map<Integer, Configuration.BackpackType> types = instance.types_of_backpacks;
        Map<Integer, Configuration.BackpackType> effectiveTypes = types.isEmpty()
                ? Configuration.defaultInstance.types_of_backpacks
                : types;

        int registeredSize = 0;

        for (Map.Entry<Integer, Configuration.BackpackType> entry : effectiveTypes.entrySet()) {
            int upgradeOrder = entry.getKey();
            Configuration.BackpackType type = entry.getValue();

            int size = type.slots();
            List<String> strings = type.backpacks();

            var sizeMap = new HashMap<Integer, Item>(DyeColor.values().length);
            var items = new HashSet<Item>();

            var defaultName = (strings != null && !strings.isEmpty()) ? strings.getFirst() : upgradeOrder + "_backpack";

            if (strings != null && type.dyeable()) {
                var blacklistedDyes = type.dyeBlacklist();
                registeredSize += registerDyeableBackpack(sizeMap, strings, blacklistedDyes, items, size);
            } else {
                items.add(create(sizeMap, 0, defaultName, size));
                registeredSize++;
            }

            BACKPACKS_UPGRADE_ORDER.put(upgradeOrder, items);
            BACKPACKS.put(upgradeOrder, sizeMap);
        }

        ServerBackpacks.LOGGER.info("Finished registering {} items.", registeredSize);
    }
}
