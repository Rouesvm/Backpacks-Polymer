package com.rouesvm.servback.blocks;

import com.rouesvm.servback.Main;
import com.rouesvm.servback.registry.BackpackBlockEntityRegistry;
import com.rouesvm.servback.ui.BackpackGui;
import com.rouesvm.servback.utils.BackpackManager;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class BackpackBlock extends BasicPolymerBlock implements BlockEntityProvider {
    public BackpackBlock(String name) {
        super(Settings.create().registryKey(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(Main.MOD_ID, name))));
    }

    @Override
    public void afterBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        if (!world.isClient) {
            BackpackBlockEntity entity = (BackpackBlockEntity) blockEntity;

            if (entity != null && entity.holder != null) {
                ItemStack stack = entity.holder.getItem();
                dropStack(world, pos, stack);

                entity.holder.destroy();
            }
        }
    }

    @Override
    protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (!world.isClient) {
            BackpackBlockEntity entity = (BackpackBlockEntity) world.getBlockEntity(pos);
            if (entity != null && entity.holder != null) {
                entity.createVisual(state, pos, (ServerWorld) world);
            }
        }
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient) {
            BackpackBlockEntity entity = (BackpackBlockEntity) world.getBlockEntity(pos);
            if (entity != null && entity.getUuid() != null) {
                new BackpackGui((ServerPlayerEntity) player, null,
                        BackpackManager.getInstance(entity.getUuid(), entity.getSize() + entity.getExtraSize()));
                return ActionResult.SUCCESS;
            }
        }

        return ActionResult.PASS;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return BackpackBlockEntityRegistry.BACKPACK_BLOCK_ENTITY.instantiate(pos, state);
    }
}
