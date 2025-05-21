package com.rouesvm.servback.block;

import com.rouesvm.servback.Main;
import com.rouesvm.servback.compat.trinkets.BackpackTrinket;
import com.rouesvm.servback.registry.BackpackBlockEntityRegistry;
import com.rouesvm.servback.ui.BackpackGui;
import com.rouesvm.servback.utils.BackpackManager;
import com.rouesvm.servback.utils.BackpackUtils;
import com.rouesvm.servback.utils.bedrock.BedrockBlock;
import com.rouesvm.servback.utils.cosmetic.BlockHolder;
import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
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

import static com.rouesvm.servback.utils.BackpackUtils.resize;

public class BackpackBlock extends BasicPolymerBlock implements BlockEntityProvider, BlockWithElementHolder, BedrockBlock {
    public BackpackBlock() {
        super(Settings.create().registryKey(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(Main.MOD_ID, "backpack"))));
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
    public void afterBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        if (!world.isClient) {
            BackpackBlockEntity entity = (BackpackBlockEntity) blockEntity;
            if (entity != null) {
                ItemStack stack = entity.getItemStack().copy();
                BackpackUtils.checkEnchantments(stack, (ServerPlayerEntity) player, entity.getSize(), entity.getExtraSize());
                dropStack(world, pos, stack);
            }
        }
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient) {
            BackpackBlockEntity entity = (BackpackBlockEntity) world.getBlockEntity(pos);
            if (entity != null && entity.getUuid() != null) {
                if (Main.hasTrinketLoaded && player.isSneaking()) {
                    if (!BackpackTrinket.hasStackInBackSlot(player)) {
                        ItemStack stack = entity.getItemStack().copy();
                        BackpackUtils.checkEnchantments(stack, (ServerPlayerEntity) player, entity.getSize(), entity.getExtraSize());
                        BackpackTrinket.equipStack(player, stack);
                        world.breakBlock(pos, false);
                        return ActionResult.SUCCESS;
                    }
                }

                resize(
                        entity.getExtraSize(),
                        entity.getSize(),
                        entity.getUuid(),
                        BackpackManager.getInventory(entity.getUuid(), entity.getSize() + entity.getExtraSize()),
                        (ServerPlayerEntity) player
                );

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
