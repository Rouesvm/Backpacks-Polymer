package com.rouesvm.servback.content.block;

import com.rouesvm.servback.ServerBackpacks;
import com.rouesvm.servback.compat.trinkets.BackpackTrinket;
import com.rouesvm.servback.content.item.impl.ContainerItem;
import com.rouesvm.servback.registry.block.BackpackBlockEntityRegistry;
import com.rouesvm.servback.technical.BackpackUtils;
import com.rouesvm.servback.technical.config.Configuration;
import com.rouesvm.servback.technical.cosmetic.BlockHolder;
import com.rouesvm.servback.technical.ui.BasicInventoryGui;
import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class BasicBackpackBlock extends BasicPolymerBlock implements BlockEntityProvider, BlockWithElementHolder {
    public BasicBackpackBlock(String name) {
        super(Settings.create()
                .noCollision()
                .breakInstantly()
                .pistonBehavior(PistonBehavior.DESTROY)
                .allowsSpawning(Blocks::never)
                .nonOpaque()
                .solidBlock(Blocks::never)
        );
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        return (Configuration.instance().breaks_with_flow && world.getFluidState(neighborPos).canFlowTo(world, pos))
                ? Blocks.AIR.getDefaultState()
                : super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerWorld world, BlockPos pos, BlockState initialBlockState) {
        return new BlockHolder(world, initialBlockState, pos);
    }

    @Override
    public boolean tickElementHolder(ServerWorld world, BlockPos pos, BlockState initialBlockState) {
        return true;
    }

    @Override
    public ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state) {
        ItemStack pickStack = super.getPickStack(world, pos, state);
        if (!world.isClient()) {
            BasicBackpackBlockEntity entity = (BasicBackpackBlockEntity) world.getBlockEntity(pos);
            if (entity == null) return pickStack;

            ItemStack stack = entity.getDefaultStack();
            return stack.getItem().getDefaultStack();
        }
        return pickStack;
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        onStacksDropped(state, (ServerWorld) world, pos, null, false);
        return super.onBreak(world, pos, state, player);
    }

    @Override
    protected void onStacksDropped(BlockState state, ServerWorld world, BlockPos pos, @Nullable ItemStack tool, boolean dropExperience) {
        BasicBackpackBlockEntity entity = (BasicBackpackBlockEntity) world.getBlockEntity(pos);
        if (entity == null) return;

        ItemStack stack = entity.getDefaultStack().copy();
        BackpackUtils.addCustomData(world, stack);
        dropStack(world, pos, stack);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient()) {
            BasicBackpackBlockEntity entity = (BasicBackpackBlockEntity) world.getBlockEntity(pos);
            if (entity == null) return ActionResult.PASS;

            if (ServerBackpacks.hasTrinketLoaded
                    && player.isSneaking()
                    && trinketInteraction(entity, (ServerPlayerEntity) player, world, pos))
                return ActionResult.SUCCESS;

            onOpenGui((ServerPlayerEntity) player, entity);
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    public boolean trinketInteraction(BasicBackpackBlockEntity entity, ServerPlayerEntity player, World world, BlockPos pos) {
        if (BackpackTrinket.isBackSlotOccupied(player)) return false;

        ItemStack stack = entity.getDefaultStack().copy();
        BackpackTrinket.equipStack(player, stack);
        world.breakBlock(pos, false);
        return true;
    }

    public void onOpenGui(ServerPlayerEntity player, BlockEntity entity) {
        ContainerItem.playOpenSound(player);
        openGui(player, entity);
    }

    public void openGui(ServerPlayerEntity player, BlockEntity entity) {
        new BasicInventoryGui(player, null, getInventory(player, entity));
    }

    public Inventory getInventory(@Nullable ServerPlayerEntity player, @Nullable BlockEntity entity) {
        return null;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return BackpackBlockEntityRegistry.BASIC_BACKPACK_BLOCK_ENTITY.instantiate(pos, state);
    }
}
