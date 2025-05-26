package com.rouesvm.servback.block;

import com.mojang.serialization.MapCodec;
import com.rouesvm.servback.Main;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.Direction;
import xyz.nucleoid.packettweaker.PacketContext;

public class BasicPolymerBlock extends HorizontalFacingBlock implements PolymerBlock {
    public static final MapCodec<BasicPolymerBlock> CODEC = createCodec(BasicPolymerBlock::new);

    public BasicPolymerBlock(Settings settings) {
        super(settings.nonOpaque());
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    public BlockState getPolymerBlockState(BlockState blockState, PacketContext context) {
        if (Main.isBedrock(context.getPlayer())) return blockState;
        return Blocks.BARRIER.getDefaultState();
    }

    @Override
    public BlockState getPolymerBreakEventBlockState(BlockState state, PacketContext context) {
        if (Main.isBedrock(context.getPlayer())) return state;
        return Blocks.BARRIER.getDefaultState();
    }

    @Override
    protected MapCodec<? extends HorizontalFacingBlock> getCodec() {
        return CODEC;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        return this.getDefaultState().with(FACING, context.getHorizontalPlayerFacing().getOpposite());
    }

    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
}
