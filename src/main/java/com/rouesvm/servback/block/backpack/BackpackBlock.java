package com.rouesvm.servback.block.backpack;

import com.rouesvm.servback.block.BasicBackpackBlock;
import com.rouesvm.servback.block.BasicBlockEntity;
import com.rouesvm.servback.compat.trinkets.BackpackTrinket;
import com.rouesvm.servback.registry.BackpackBlockEntityRegistry;
import com.rouesvm.servback.ui.BackpackGui;
import com.rouesvm.servback.utils.BackpackInstance;
import com.rouesvm.servback.utils.BackpackManager;
import com.rouesvm.servback.utils.BackpackUtils;
import com.rouesvm.servback.utils.bedrock.BedrockBlock;
import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import static com.rouesvm.servback.utils.BackpackUtils.resize;

public class BackpackBlock extends BasicBackpackBlock implements BlockEntityProvider, BlockWithElementHolder, BedrockBlock {
    public BackpackBlock() {
        super("backpack");
    }

    @Override
    protected boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    protected int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        Optional<BackpackBlockEntity> blockEntity = world.getBlockEntity(pos, BackpackBlockEntityRegistry.BACKPACK_BLOCK_ENTITY);
        blockEntity.ifPresent(backpackBlockEntity ->
                ScreenHandler.calculateComparatorOutput(getInventory(blockEntity.get(), null)));
        return 0;
    }

    @Override
    public void trinketInteraction(BasicBlockEntity entity, ServerPlayerEntity player, World world, BlockPos pos) {
        BackpackBlockEntity backpackBlockEntity = (BackpackBlockEntity) entity;

        ItemStack stack = backpackBlockEntity.getDefaultStack().copy();
        BackpackUtils.checkEnchantments(stack, player, backpackBlockEntity.getSize(), backpackBlockEntity.getExtraSize());
        BackpackTrinket.equipStack(player, stack);
        world.breakBlock(pos, false);
    }

    @Override
    public void openGui(BlockEntity entity, ServerPlayerEntity player) {
        BackpackBlockEntity backpackBlockEntity = (BackpackBlockEntity) entity;
        BackpackInstance instance = backpackBlockEntity.getInstance();

        resize(
                backpackBlockEntity.getExtraSize(),
                backpackBlockEntity.getSize(),
                backpackBlockEntity.getUuid(),
                instance.getInventory(),
                player
        );

        new BackpackGui(player, null, instance);
    }

    @Override
    public Inventory getInventory(BlockEntity entity, @Nullable ServerPlayerEntity player) {
        BackpackBlockEntity backpackBlockEntity = (BackpackBlockEntity) entity;
        return BackpackManager.getInventory(backpackBlockEntity.getUuid());
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return BackpackBlockEntityRegistry.BACKPACK_BLOCK_ENTITY.instantiate(pos, state);
    }
}
