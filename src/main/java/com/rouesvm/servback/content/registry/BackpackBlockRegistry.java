package com.rouesvm.servback.content.registry;

import com.rouesvm.servback.ServerBackpacks;
import com.rouesvm.servback.content.block.BasicBackpackBlock;
import com.rouesvm.servback.content.block.BasicPolymerBlock;
import com.rouesvm.servback.content.block.backpack.BackpackBlock;
import com.rouesvm.servback.data.BackpackManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class BackpackBlockRegistry {
    public static final Block BACKPACK = register("backpack", new BackpackBlock());

    public static final Block ENDER_BACKPACK = register("ender_backpack", new BasicBackpackBlock("ender_backpack") {
        @Override
        public Inventory getInventory(@Nullable ServerPlayerEntity player, @Nullable BlockEntity entity) {
            return player != null ? player.getEnderChestInventory() : null;
        }

        @Override
        public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
            return BackpackBlockEntityRegistry.ENDER_BACKPACK_BLOCK_ENTITY.instantiate(pos, state);
        }
    });
    public static final Block GLOBAL_BACKPACK = register("global_backpack", new BasicBackpackBlock("global_backpack") {
        @Override
        public Inventory getInventory(@Nullable ServerPlayerEntity player, @Nullable BlockEntity entity) {
            return BackpackManager.getGlobalInventory();
        }

        @Override
        public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
            return BackpackBlockEntityRegistry.GLOBAL_BACKPACK_BLOCK_ENTITY.instantiate(pos, state);
        }
    });

    public static Block register(String name, BasicPolymerBlock block) {
        return Registry.register(Registries.BLOCK, Identifier.of(ServerBackpacks.MOD_ID, name), block);
    }

    public static void initialize() {}
}
