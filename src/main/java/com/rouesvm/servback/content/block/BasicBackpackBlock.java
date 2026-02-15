package com.rouesvm.servback.content.block;

import com.rouesvm.servback.ServerBackpacks;
import com.rouesvm.servback.compat.geyser.bedrock.BedrockBlock;
import com.rouesvm.servback.compat.trinkets.BackpackTrinket;
import com.rouesvm.servback.content.item.impl.ContainerItem;
import com.rouesvm.servback.registry.block.BackpackBlockEntityRegistry;
import com.rouesvm.servback.technical.BackpackUtils;
import com.rouesvm.servback.technical.config.Configuration;
import com.rouesvm.servback.technical.cosmetic.BlockHolder;
import com.rouesvm.servback.technical.ui.BasicInventoryGui;
import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public class BasicBackpackBlock extends BasicPolymerBlock implements EntityBlock, BlockWithElementHolder, BedrockBlock {
    public BasicBackpackBlock(String name) {
        super(Properties.of()
                .setId(ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(ServerBackpacks.MOD_ID, name)))
                .noCollision()
                .instabreak()
                .pushReaction(PushReaction.DESTROY)
                .isValidSpawn(Blocks::never)
                .noOcclusion()
                .isRedstoneConductor(Blocks::never)
        );
    }

    @Override
    protected @NonNull BlockState updateShape(
            @NonNull BlockState state,
            @NonNull LevelReader world,
            @NonNull ScheduledTickAccess tickView,
            @NonNull BlockPos pos,
            @NonNull Direction direction,
            @NonNull BlockPos neighborPos,
            @NonNull BlockState neighborState,
            @NonNull RandomSource random
    ) {
        return (Configuration.instance().breaks_with_flow && world.getFluidState(neighborPos).shouldRenderBackwardUpFace(world, pos))
                ? Blocks.AIR.defaultBlockState()
                : super.updateShape(state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected int getLightBlock(@NonNull BlockState state) {
        return 1;
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        return new BlockHolder(world, initialBlockState, pos);
    }

    @Override
    public boolean tickElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        return true;
    }

    @Override
    protected @NonNull ItemStack getCloneItemStack(@NonNull LevelReader world, @NonNull BlockPos pos, BlockState state, boolean includeData) {
        ItemStack pickStack = super.getCloneItemStack(world, pos, state, includeData);
        if (!world.isClientSide()) {
            BasicBackpackBlockEntity entity = (BasicBackpackBlockEntity) world.getBlockEntity(pos);
            if (entity == null) return pickStack;

            ItemStack stack = entity.getDefaultStack();
            if (includeData)
                return stack.copy();
            else return stack.getItem().getDefaultInstance();
        }
        return pickStack;
    }

    @Override
    public @NonNull BlockState playerWillDestroy(@NonNull Level world, @NonNull BlockPos pos, @NonNull BlockState state, @NonNull Player player) {
        spawnAfterBreak(state, (ServerLevel) world, pos, null, false);
        return super.playerWillDestroy(world, pos, state, player);
    }

    @Override
    protected void spawnAfterBreak(@NonNull BlockState state, ServerLevel world, @NonNull BlockPos pos, @Nullable ItemStack tool, boolean dropExperience) {
        BasicBackpackBlockEntity entity = (BasicBackpackBlockEntity) world.getBlockEntity(pos);
        if (entity == null) return;

        ItemStack stack = entity.getDefaultStack().copy();
        BackpackUtils.addCustomData(world, stack);
        popResource(world, pos, stack);
    }

    @Override
    protected @NonNull InteractionResult useWithoutItem(@NonNull BlockState state, Level world, @NonNull BlockPos pos, @NonNull Player player, @NonNull BlockHitResult hit) {
        if (!world.isClientSide()) {
            BasicBackpackBlockEntity entity = (BasicBackpackBlockEntity) world.getBlockEntity(pos);
            if (entity == null) return InteractionResult.PASS;

            if (ServerBackpacks.hasTrinketLoaded
                    && player.isShiftKeyDown()
                    && trinketInteraction(entity, (ServerPlayer) player, world, pos))
                return InteractionResult.SUCCESS;

            onOpenGui((ServerPlayer) player, entity);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    public boolean trinketInteraction(BasicBackpackBlockEntity entity, ServerPlayer player, Level world, BlockPos pos) {
        if (BackpackTrinket.isBackSlotOccupied(player)) return false;

        ItemStack stack = entity.getDefaultStack().copy();
        BackpackTrinket.equipStack(player, stack);
        world.destroyBlock(pos, false);
        return true;
    }

    public void onOpenGui(ServerPlayer player, BlockEntity entity) {
        ContainerItem.playOpenSound(player);
        openGui(player, entity);
    }

    public void openGui(ServerPlayer player, BlockEntity entity) {
        new BasicInventoryGui(player, null, getInventory(player, entity));
    }

    public Container getInventory(@Nullable ServerPlayer player, @Nullable BlockEntity entity) {
        return null;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NonNull BlockPos pos, @NonNull BlockState state) {
        return BackpackBlockEntityRegistry.BASIC_BACKPACK_BLOCK_ENTITY.create(pos, state);
    }
}
