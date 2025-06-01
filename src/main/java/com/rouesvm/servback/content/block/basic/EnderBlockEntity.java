package com.rouesvm.servback.content.block.basic;


import com.rouesvm.servback.content.block.BasicBlockEntity;
import com.rouesvm.servback.content.registry.BackpackBlockEntityRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class EnderBlockEntity extends BasicBlockEntity {
    public EnderBlockEntity(BlockPos pos, BlockState state) {
        super(BackpackBlockEntityRegistry.ENDER_BACKPACK_BLOCK_ENTITY, pos, state);
    }
}
