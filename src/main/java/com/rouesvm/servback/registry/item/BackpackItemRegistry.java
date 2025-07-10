package com.rouesvm.servback.registry.item;

import com.rouesvm.servback.ServerBackpacks;
import com.rouesvm.servback.content.item.BasicPolymerBlockItem;
import com.rouesvm.servback.content.item.BundleGuiItem;
import com.rouesvm.servback.content.item.UpgradeItem;
import com.rouesvm.servback.registry.BackpackUpgradeRegistry;
import com.rouesvm.servback.registry.block.BackpackBlockRegistry;
import com.rouesvm.servback.technical.config.Configuration;
import com.rouesvm.servback.technical.data.BackpackManager;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class BackpackItemRegistry {
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

    public static final Item MAGNET_UPGRADE = register("magnet_upgrade", new UpgradeItem(new Item.Settings()
            .maxCount(1)
            .registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(ServerBackpacks.MOD_ID, "magnet_upgrade"))
            ), BackpackUpgradeRegistry.MAGNET)
    );

    public static <T extends Item> T register(String name, T item) {
        if (Configuration.instance().disabled_backpacks.contains(name)
        ) return null;

        return Registry.register(Registries.ITEM, Identifier.of(ServerBackpacks.MOD_ID, name), item);
    }

    public static UpgradeItem register(String name, UpgradeItem item) {
        if (!Configuration.instance().enable_upgrades
                || Configuration.instance().disabled_upgrades.contains(name)
        ) return null;

        return Registry.register(Registries.ITEM, Identifier.of(ServerBackpacks.MOD_ID, name), item);
    }

    public static <T extends BasicPolymerBlockItem> T register(T item) {
        return Registry.register(Registries.ITEM, item.getIdentifier(), item);
    }

    public static void initialize() {}
}
