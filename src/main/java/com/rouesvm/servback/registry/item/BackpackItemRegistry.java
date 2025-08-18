package com.rouesvm.servback.registry.item;

import com.rouesvm.servback.ServerBackpacks;
import com.rouesvm.servback.content.item.BasicPolymerBlockItem;
import com.rouesvm.servback.content.item.BundleGuiItem;
import com.rouesvm.servback.content.item.UpgradeItem;
import com.rouesvm.servback.registry.BackpackUpgradeRegistry;
import com.rouesvm.servback.registry.block.BackpackBlockRegistry;
import com.rouesvm.servback.technical.manager.BackpackManager;
import net.minecraft.block.Block;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

public class BackpackItemRegistry {
    public static final Item ENDER_BACKPACK = registerBackpack("ender", BackpackBlockRegistry.ENDER_BACKPACK,
            (player, stack) -> player != null ? player.getEnderChestInventory() : null);

    public static final Item GLOBAL_BACKPACK = registerBackpack("global", BackpackBlockRegistry.GLOBAL_BACKPACK,
            (player, stack) -> BackpackManager.globalInventory());

    public static final Item VOID_UPGRADE = register("void_upgrade", new UpgradeItem(
            new Item.Settings().maxCount(1),
            BackpackUpgradeRegistry.VOID
    ));

    public static final Item MAGNET_UPGRADE = register("magnet_upgrade", new UpgradeItem(
            new Item.Settings().maxCount(1),
            BackpackUpgradeRegistry.MAGNET
    ));

    public static final Item CRAFTING_UPGRADE = register("crafting_upgrade", new UpgradeItem(
            new Item.Settings().maxCount(1),
            BackpackUpgradeRegistry.CRAFTING
    ));

    public static UpgradeItem register(String name, UpgradeItem item) {
        return Registry.register(Registries.ITEM, Identifier.of(ServerBackpacks.MOD_ID, name), item);
    }

    private static Item registerBackpack(String id, Block block, BiFunction<@Nullable ServerPlayerEntity, @Nullable ItemStack, Inventory> inventoryProvider) {
        var item = new BundleGuiItem(id, block) {
            @Override
            public Inventory getInventory(@Nullable ServerPlayerEntity player, @Nullable ItemStack stack) {
                return inventoryProvider.apply(player, stack);
            }
        };

        return Registry.register(Registries.ITEM, item.getIdentifier(), item);
    }

    public static <T extends BasicPolymerBlockItem> T register(T item) {
        return Registry.register(Registries.ITEM, item.getIdentifier(), item);
    }

    public static void initialize() {}
}
