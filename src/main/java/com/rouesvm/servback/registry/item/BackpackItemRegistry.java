package com.rouesvm.servback.registry.item;

import com.rouesvm.servback.ServerBackpacks;
import com.rouesvm.servback.content.item.BasicPolymerBlockItem;
import com.rouesvm.servback.content.item.BundleGuiItem;
import com.rouesvm.servback.content.item.UpgradeItem;
import com.rouesvm.servback.content.item.impl.ContainerItem;
import com.rouesvm.servback.registry.BackpackUpgradeRegistry;
import com.rouesvm.servback.registry.block.BackpackBlockRegistry;
import com.rouesvm.servback.technical.config.Configuration;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

public class BackpackItemRegistry {
    public static final Item ENDER_BACKPACK = registerBackpack("ender", BackpackBlockRegistry.ENDER_BACKPACK,
            (player, stack) -> player != null ? player.getEnderChestInventory() : null);

    public static final Item GLOBAL_BACKPACK = register(new ContainerItem("global", Configuration.instance().global_backpack_size, BackpackBlockRegistry.GLOBAL_BACKPACK));

    public static final Item VOID_UPGRADE = register("void_upgrade", new UpgradeItem(
            new Item.Properties().stacksTo(1),
            BackpackUpgradeRegistry.VOID
    ));

    public static final Item MAGNET_UPGRADE = register("magnet_upgrade", new UpgradeItem(
            new Item.Properties().stacksTo(1),
            BackpackUpgradeRegistry.MAGNET
    ));

    public static final Item CRAFTING_UPGRADE = register("crafting_upgrade", new UpgradeItem(
            new Item.Properties().stacksTo(1),
            BackpackUpgradeRegistry.CRAFTING
    ));

    public static final Item JUKEBOX_UPGRADE = register("jukebox_upgrade", new UpgradeItem(
            new Item.Properties().stacksTo(1),
            BackpackUpgradeRegistry.JUKEBOX
    ));

    public static UpgradeItem register(String name, UpgradeItem item) {
        return Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(ServerBackpacks.MOD_ID, name), item);
    }

    public static Item registerBackpack(String id, Block block, BiFunction<@Nullable ServerPlayer, @Nullable ItemStack, Container> inventoryProvider) {
        var item = new BundleGuiItem(id, block) {
            @Override
            public Container getInventory(@Nullable ServerPlayer player, @Nullable ItemStack stack) {
                return inventoryProvider.apply(player, stack);
            }
        };

        return Registry.register(BuiltInRegistries.ITEM, item.getIdentifier(), item);
    }

    public static <T extends BasicPolymerBlockItem> T register(T item) {
        return Registry.register(BuiltInRegistries.ITEM, item.getIdentifier(), item);
    }

    public static void initialize() {}
}
