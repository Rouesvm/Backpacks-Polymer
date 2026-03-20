package com.rouesvm.servback.registry.block;

import com.rouesvm.servback.ServerBackpacks;
import com.rouesvm.servback.content.block.BasicBackpackBlock;
import com.rouesvm.servback.content.block.impl.BackpackBlock;
import com.rouesvm.servback.content.block.impl.BaseBackpackBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

public class BackpackBlockRegistry {
    public static final Block ENDER_BACKPACK = registerBackpack("ender_backpack",
            (player, entity) -> player != null ? player.getEnderChestInventory() : null);

    public static final Block GLOBAL_BACKPACK = register("global_backpack", new BaseBackpackBlock("global_backpack"));
    public static final Block LAVA_BACKPACK = register("lava_backpack", new BaseBackpackBlock("lava_backpack"));
    public static final Block BACKPACK = register("backpack", new BackpackBlock());

    private static Block registerBackpack(String id, BiFunction<@Nullable ServerPlayer, @Nullable BlockEntity, Container> inventoryProvider) {
        return Registry.register(BuiltInRegistries.BLOCK, Identifier.fromNamespaceAndPath(ServerBackpacks.MOD_ID, id), new BasicBackpackBlock(id) {
            @Override
            public Container getInventory(@Nullable ServerPlayer player, @Nullable BlockEntity entity) {
                return inventoryProvider.apply(player, entity);
            }
        });
    }

    public static <T extends Block> T register(String name, T block) {
        return Registry.register(BuiltInRegistries.BLOCK, Identifier.fromNamespaceAndPath(ServerBackpacks.MOD_ID, name), block);
    }

    public static void initialize() {}
}
