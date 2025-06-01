package com.rouesvm.servback.content.block;

import com.rouesvm.servback.ServerBackpacks;
import com.rouesvm.servback.compat.trinkets.BackpackTrinket;
import com.rouesvm.servback.technical.config.Configuration;
import com.rouesvm.servback.technical.ui.BasicGui;
import com.rouesvm.servback.data.BackpackUtils;
import com.rouesvm.servback.compat.geyser.bedrock.BedrockBlock;
import com.rouesvm.servback.technical.cosmetic.BlockHolder;
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
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public class BasicBackpackBlock extends BasicPolymerBlock implements BlockEntityProvider, BlockWithElementHolder, BedrockBlock {
    public BasicBackpackBlock(String name) {
        super(Settings.create()
                .registryKey(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(ServerBackpacks.MOD_ID, name)))
                .noCollision()
                .breakInstantly()
                .pistonBehavior(PistonBehavior.DESTROY)
                .allowsSpawning(Blocks::never)
                .nonOpaque()
                .solidBlock(Blocks::never)
        );
    }

    @Override
    protected BlockState getStateForNeighborUpdate(
            BlockState state,
            WorldView world,
            ScheduledTickView tickView,
            BlockPos pos,
            Direction direction,
            BlockPos neighborPos,
            BlockState neighborState,
            Random random
    ) {
        return (Configuration.getInstance().breaks_with_flow && world.getFluidState(neighborPos).canFlowTo(world, pos))
                ? Blocks.AIR.getDefaultState()
                : super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected int getOpacity(BlockState state) {
        return 1;
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
    protected ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state, boolean includeData) {
        if (!world.isClient()) {
            BasicBlockEntity entity = (BasicBlockEntity) world.getBlockEntity(pos);
            if (entity != null) {
                ItemStack stack = entity.getDefaultStack();
                if (includeData)
                    return stack.copy();
                else return stack.getItem().getDefaultStack();
            }
        }
        return super.getPickStack(world, pos, state, includeData);
    }

    // this is definitely used wrongly
    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        BasicBlockEntity entity = (BasicBlockEntity) world.getBlockEntity(pos);
        if (entity != null) {
            ItemStack stack = entity.getDefaultStack().copy();
            BackpackUtils.addCustomData((ServerWorld) world, stack);
            dropStack(world, pos, stack);
        }
        return super.onBreak(world, pos, state, player);
    }

    @Override
    protected void onStacksDropped(BlockState state, ServerWorld world, BlockPos pos, ItemStack tool, boolean dropExperience) {
        BasicBlockEntity entity = (BasicBlockEntity) world.getBlockEntity(pos);
        if (entity != null) {
            ItemStack stack = entity.getDefaultStack().copy();
            BackpackUtils.addCustomData(world, stack);
            dropStack(world, pos, stack);
        }
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient) {
            BasicBlockEntity entity = (BasicBlockEntity) world.getBlockEntity(pos);
            if (entity != null) {
                if (ServerBackpacks.hasTrinketLoaded && player.isSneaking()) {
                    if (!BackpackTrinket.hasStackInBackSlot(player)) {
                        trinketInteraction(entity, (ServerPlayerEntity) player, world, pos);
                        return ActionResult.SUCCESS;
                    }
                }

                openGui(entity, (ServerPlayerEntity) player);
                return ActionResult.SUCCESS;
            }
        }

        return ActionResult.PASS;
    }

    public void trinketInteraction(BasicBlockEntity entity, ServerPlayerEntity player, World world, BlockPos pos) {
        ItemStack stack = entity.getDefaultStack().copy();
        BackpackTrinket.equipStack(player, stack);
        world.breakBlock(pos, false);
    }

    public void openGui(BlockEntity entity, ServerPlayerEntity player) {
        new BasicGui(player, null, getInventory(entity, player));
    }

    public Inventory getInventory(BlockEntity entity, @Nullable ServerPlayerEntity player) {
        return null;
    }

    @ApiStatus.OverrideOnly
    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return null;
    }
}
