package com.rouesvm.servback.content.block;

import com.mojang.serialization.MapCodec;
import com.rouesvm.servback.ServerBackpacks;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import xyz.nucleoid.packettweaker.PacketContext;

public class BasicPolymerBlock extends HorizontalDirectionalBlock implements PolymerBlock {
    public static final MapCodec<BasicPolymerBlock> CODEC = simpleCodec(BasicPolymerBlock::new);

    public BasicPolymerBlock(Properties settings) {
        super(settings.noOcclusion());
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
        if (context != null && ServerBackpacks.isBedrock(context.getPlayer()))
            return state;
        return Blocks.BARRIER.defaultBlockState();
    }

    @Override
    public BlockState getPolymerBreakEventBlockState(BlockState state, PacketContext context) {
        if (context != null && ServerBackpacks.isBedrock(context.getPlayer()))
            return state;
        return Blocks.BARRIER.defaultBlockState();
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
}
