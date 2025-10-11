package com.rouesvm.servback.registry.item;

import com.rouesvm.servback.ServerBackpacks;
import com.rouesvm.servback.content.item.impl.ContainerItem;
import com.rouesvm.servback.technical.config.Configuration;
import net.minecraft.item.Item;
import net.minecraft.util.DyeColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BackpackItemJsonRegistry {
    public static final Map<Integer, Map<Integer, Item>> BACKPACKS = new HashMap<>();

    private static final Map<Integer, Integer> SIZE_TO_ORDER = new HashMap<>();
    private static final Map<Integer, Integer> ORDER_OFFSET = new HashMap<>();
    private static final Map<Item, DyeColor> ITEM_TO_COLOR = new HashMap<>();

    public static @Nullable DyeColor getBackpackDyeColor(ContainerItem item) {
        return ITEM_TO_COLOR.get(item);
    }

    public static int getOffset(int order) {
        return ORDER_OFFSET.getOrDefault(order, 0);
    }

    public static int getBackpackId(ContainerItem item) {
        DyeColor color = ITEM_TO_COLOR.get(item);
        return color != null ? color.getId() + getOffset(BackpackItemJsonRegistry.getBackpackUpgradeOrder(item.getSize())) : 0;
    }

    public static int getBackpackUpgradeOrder(int size) {
        return SIZE_TO_ORDER.getOrDefault(size, 1);
    }

    public static Item getBackpackByOrder(@NotNull DyeColor color, int order) {
        return getBackpackByOrder(color.getId() + getOffset(order), order);
    }

    public static Item getBackpackByOrder(int order) {
        return getBackpackByOrder(0, order);
    }

    public static Item getBackpackByOrder(int id, int order) {
        Map<Integer, Item> defaultMap = BACKPACKS.getOrDefault(order, BACKPACKS.get(1));
        return BACKPACKS
                .getOrDefault(order, BACKPACKS.get(1))
                .getOrDefault(id, defaultMap.get(0));
    }

    public static Item getBackpackBySize(int size) {
        return getBackpackBySize(0, size);
    }

    public static Item getBackpackBySize(int id, int size) {
        return getBackpackByOrder(id, BackpackItemJsonRegistry.getBackpackUpgradeOrder(size));
    }

    private static ContainerItem create(Map<Integer, Item> itemMap, Integer id, String name, int slots) {
        ContainerItem item = BackpackItemRegistry.register(new ContainerItem(name, slots));
        itemMap.put(id, item);
        return item;
    }

    private static int registerBackpacks(Configuration.BackpackType type, Map<Integer, Item> sizeMap, int size, int order) {
        List<String> strings = type.backpacks();

        int offset = 0;
        int registeredSize = 0;
        for (String backpackName : strings) {
            create(sizeMap, offset++, backpackName, size);
            registeredSize++;

            if (!type.dyeable()) continue;

            List<String> blacklistedDyes = type.dyeBlacklist();

            for (DyeColor color : DyeColor.values()) {
                String dyeColor = color.name().toLowerCase();
                if (blacklistedDyes != null && blacklistedDyes.contains(dyeColor)) continue;

                ContainerItem item = create(sizeMap, color.getId() + offset, dyeColor + "_" + backpackName, size);
                ITEM_TO_COLOR.put(item, color);

                registeredSize++;
            }
        }

        ORDER_OFFSET.put(order, offset);
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
            if (strings == null || strings.isEmpty()) continue;

            var sizeMap = new HashMap<Integer, Item>(DyeColor.values().length);

            registeredSize += registerBackpacks(type, sizeMap, size, upgradeOrder);

            SIZE_TO_ORDER.put(size, upgradeOrder);
            BACKPACKS.put(upgradeOrder, sizeMap);
        }

        ServerBackpacks.LOGGER.info("Finished registering {} items.", registeredSize);
    }
}
