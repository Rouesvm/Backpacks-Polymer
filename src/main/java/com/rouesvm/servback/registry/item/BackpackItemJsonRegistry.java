package com.rouesvm.servback.registry.item;

import com.rouesvm.servback.ServerBackpacks;
import com.rouesvm.servback.content.item.impl.ContainerItem;
import com.rouesvm.servback.technical.config.BackpackItemConfiguration;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BackpackItemJsonRegistry {
    public static final Set<Item> BACKPACKS = new ObjectOpenHashSet<>();

    public static final Map<Item, String> BACKPACKS_TO_NAME = new Object2ObjectOpenHashMap<>();
    public static final Map<String, Item> NAME_TO_BACKPACK = new Object2ObjectOpenHashMap<>();

    public static final Map<String, Set<Item>> NAME_TO_BACKPACKS = new Object2ObjectOpenHashMap<>();

    public static final Map<Item, Item> ITEM_TO_UPGRADED = new Object2ObjectOpenHashMap<>();
    public static final Map<Item, String> ITEM_TO_UPGRADED_STRING = new Object2ObjectOpenHashMap<>();

    public static final Map<Item, DyeColor> ITEM_TO_DYE = new Object2ObjectOpenHashMap<>();

    public static final Map<Item, BackpackItemConfiguration.BackCosmetic> ITEM_TO_BACK_COSMETIC = new Object2ObjectOpenHashMap<>();

    public static Set<Item> getBackpacksByName(String name) {
        return NAME_TO_BACKPACKS.get(name);
    }

    public static Item getBackpackByName(String name) {
        return NAME_TO_BACKPACK.get(name);
    }

    public static Item getBackpackUpgrade(Item item) {
        return ITEM_TO_UPGRADED.get(item);
    }

    public static String getBackpackUpgradeName(Item item) {
        return BACKPACKS_TO_NAME.get(ITEM_TO_UPGRADED.get(item));
    }

    public static DyeColor getBackpackDyeColor(ContainerItem item) {
        return ITEM_TO_DYE.get(item);
    }

    public static BackpackItemConfiguration.BackCosmetic getBackpackCosmetic(ContainerItem item) {
        return ITEM_TO_BACK_COSMETIC.getOrDefault(item, BackpackItemConfiguration.DEFAULT_COSMETIC);
    }

    private static ContainerItem create(String upgrade, String name, int slots) {
        ContainerItem item = BackpackItemRegistry.register(new ContainerItem(name, slots));
        BACKPACKS.add(item);
        BACKPACKS_TO_NAME.put(item, name);
        NAME_TO_BACKPACK.put(name, item);

        if (!upgrade.isEmpty()) {
            ITEM_TO_UPGRADED_STRING.put(item, upgrade);
        }

        return item;
    }

    private static int registerBackpacks(BackpackItemConfiguration.BackpackDefinedType type) {
        Set<Item> items = new HashSet<>();

        if (!NAME_TO_BACKPACK.containsKey(type.backpack())) {
            items.add(create(type.upgradeBackpack(), type.backpack(), type.slots()));

            if (type.dyeable()) {
                List<String> blacklistedDyes = type.dyeBlacklist();

                for (DyeColor color : DyeColor.values()) {
                    String dyeColor = color.name().toLowerCase();
                    if (blacklistedDyes != null && blacklistedDyes.contains(dyeColor)) continue;

                    ContainerItem item = create(dyeColor + "_" + type.upgradeBackpack(), dyeColor + "_" + type.backpack(), type.slots());
                    items.add(item);
                    ITEM_TO_DYE.put(item, color);
                }
            }

            NAME_TO_BACKPACKS.put(type.backpack(), items);
        }

        for (Item item : items) {
            ITEM_TO_BACK_COSMETIC.put(item, type.cosmetic());
        }

        return items.size();
    }

    public static void initialize() {
        List<BackpackItemConfiguration.BackpackDefinedType> configuration = BackpackItemConfiguration.manager.backpackTypes;

        int registeredSize = 0;

        for (BackpackItemConfiguration.BackpackDefinedType backpackDefinedType : configuration) {
            registeredSize += registerBackpacks(backpackDefinedType);
        }

        for (Item backpack : BACKPACKS) {
            String upgradeName = ITEM_TO_UPGRADED_STRING.get(backpack);
            ITEM_TO_UPGRADED.put(backpack, NAME_TO_BACKPACK.get(upgradeName));
        }

        ServerBackpacks.LOGGER.info("Finished registering {} items.", registeredSize);
    }
}
