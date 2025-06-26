package com.rouesvm.servback.content.block;

import com.rouesvm.servback.ServerBackpacks;
import com.rouesvm.servback.compat.geyser.bedrock.BedrockBlock;
import com.rouesvm.servback.compat.trinkets.BackpackTrinket;
import com.rouesvm.servback.content.item.ContainerItem;
import com.rouesvm.servback.content.registry.block.BackpackBlockEntityRegistry;
import com.rouesvm.servback.technical.config.Configuration;
import com.rouesvm.servback.technical.cosmetic.BlockHolder;
import com.rouesvm.servback.technical.data.BackpackUtils;
import com.rouesvm.servback.technical.ui.BasicGui;
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
        return (Configuration.instance().breaks_with_flow && world.getFluidState(neighborPos).canFlowTo(world, pos))
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
            BasicBackpackBlockEntity entity = (BasicBackpackBlockEntity) world.getBlockEntity(pos);
            if (entity != null) {
                ItemStack stack = entity.getDefaultStack();
                if (includeData)
                    return stack.copy();
                else return stack.getItem().getDefaultStack();
            }
        }
        return super.getPickStack(world, pos, state, includeData);
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        onStacksDropped(state, (ServerWorld) world, pos, null, false);
        return super.onBreak(world, pos, state, player);
    }

    @Override
    protected void onStacksDropped(BlockState state, ServerWorld world, BlockPos pos, @Nullable ItemStack tool, boolean dropExperience) {
        BasicBackpackBlockEntity entity = (BasicBackpackBlockEntity) world.getBlockEntity(pos);
        if (entity != null) {
            ItemStack stack = entity.getDefaultStack().copy();
            BackpackUtils.addCustomData(world, stack);
            dropStack(world, pos, stack);
        }
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient) {
            BasicBackpackBlockEntity entity = (BasicBackpackBlockEntity) world.getBlockEntity(pos);
            if (entity != null) {
                if (ServerBackpacks.hasTrinketLoaded
                        && player.isSneaking()
                        && !trinketInteraction(entity, (ServerPlayerEntity) player, world, pos))
                    return ActionResult.SUCCESS;

                onOpenGui((ServerPlayerEntity) player, entity);
                return ActionResult.SUCCESS;
            }
        }

        return ActionResult.PASS;
    }

    public boolean trinketInteraction(BasicBackpackBlockEntity entity, ServerPlayerEntity player, World world, BlockPos pos) {
        if (BackpackTrinket.isStackEmptyInBackSlot(player)) {
            ItemStack stack = entity.getDefaultStack().copy();
            BackpackTrinket.equipStack(player, stack);
            world.breakBlock(pos, false);
            return true;
        }

        return false;
    }

    public void onOpenGui(ServerPlayerEntity player, BlockEntity entity) {
        ContainerItem.playOpenSound(player);
        openGui(player, entity);
    }

    public void openGui(ServerPlayerEntity player, BlockEntity entity) {
        new BasicGui(player, null, getInventory(player, entity));
    }

    public Inventory getInventory(@Nullable ServerPlayerEntity player, @Nullable BlockEntity entity) {
        return null;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return BackpackBlockEntityRegistry.BASIC_BACKPACK_BLOCK_ENTITY.instantiate(pos, state);
    }
}
