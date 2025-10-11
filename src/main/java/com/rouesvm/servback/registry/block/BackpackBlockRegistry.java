package com.rouesvm.servback.registry.block;

import com.rouesvm.servback.ServerBackpacks;
import com.rouesvm.servback.content.block.BasicBackpackBlock;
import com.rouesvm.servback.content.block.impl.BackpackBlock;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

public class BackpackBlockRegistry {
    public static final Block ENDER_BACKPACK = registerBackpack("ender_backpack",
            (player, entity) -> player != null ? player.getEnderChestInventory() : null);

    public static final Block GLOBAL_BACKPACK = register("global_backpack", new BackpackBlock());
    public static final Block BACKPACK = register("backpack", new BackpackBlock());

    private static Block registerBackpack(String id, BiFunction<@Nullable ServerPlayerEntity, @Nullable BlockEntity, Inventory> inventoryProvider) {
        return Registry.register(Registries.BLOCK, Identifier.of(ServerBackpacks.MOD_ID, id), new BasicBackpackBlock(id) {
            @Override
            public Inventory getInventory(@Nullable ServerPlayerEntity player, @Nullable BlockEntity entity) {
                return inventoryProvider.apply(player, entity);
            }
        });
    }

    public static <T extends Block> T register(String name, T block) {
        return Registry.register(Registries.BLOCK, Identifier.of(ServerBackpacks.MOD_ID, name), block);
    }

    public static void initialize() {}
}
