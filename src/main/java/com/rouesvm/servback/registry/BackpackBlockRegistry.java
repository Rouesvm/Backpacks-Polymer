package com.rouesvm.servback.registry;

import com.rouesvm.servback.Main;
import com.rouesvm.servback.block.BasicBackpackBlock;
import com.rouesvm.servback.block.BasicPolymerBlock;
import com.rouesvm.servback.block.backpack.BackpackBlock;
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

    public static final Block GLOBAL_BACKPACK = register("global_backpack", new BasicBackpackBlock("global_backpack") {
        @Override
        public Inventory getInventory(BlockEntity entity, @Nullable ServerPlayerEntity player) {
            return Main.getInventory();
        }

        @Override
        public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
            return BackpackBlockEntityRegistry.GLOBAL_BACKPACK_BLOCK_ENTITY.instantiate(pos, state);
        }
    });

    public static final Block ENDER_BACKPACK = register("ender_backpack", new BasicBackpackBlock("ender_backpack") {
        @Override
        public Inventory getInventory(BlockEntity entity, @Nullable ServerPlayerEntity player) {
            return player != null ? player.getEnderChestInventory() : null;
        }

        @Override
        public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
            return BackpackBlockEntityRegistry.ENDER_BACKPACK_BLOCK_ENTITY.instantiate(pos, state);
        }
    });

    public static Block register(String name, BasicPolymerBlock block) {
        return Registry.register(Registries.BLOCK, Identifier.of(Main.MOD_ID, name), block);
    }

    public static void initialize() {}
}
