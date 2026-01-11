package com.rouesvm.servback.content.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;

public interface TickableBlockEntity {
    void tick(Level world, BlockPos pos, BlockState state, BlockEntity entity);

    static <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pWorld) {
        return pWorld.isClientSide() ? null : (world, pos, state, blockEntity) -> {
            if (blockEntity instanceof TickableBlockEntity tickableBlockEntity) {
                tickableBlockEntity.tick(world, pos, state, blockEntity);
            }
        };
    }
}
