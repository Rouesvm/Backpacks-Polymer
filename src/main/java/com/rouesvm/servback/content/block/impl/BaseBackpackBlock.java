package com.rouesvm.servback.content.block.impl;

import com.rouesvm.servback.compat.geyser.bedrock.BedrockBlock;
import com.rouesvm.servback.compat.trinkets.BackpackTrinket;
import com.rouesvm.servback.content.block.BasicBackpackBlock;
import com.rouesvm.servback.content.block.BasicBackpackBlockEntity;
import com.rouesvm.servback.content.block.TickableBlockEntity;
import com.rouesvm.servback.registry.block.BackpackBlockEntityRegistry;
import com.rouesvm.servback.technical.BackpackUtils;
import com.rouesvm.servback.technical.data.BackpackInstance;
import com.rouesvm.servback.technical.manager.BackpackManager;
import com.rouesvm.servback.technical.ui.BackpackGui;
import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import static com.rouesvm.servback.technical.BackpackUtils.resize;

public class BaseBackpackBlock extends BasicBackpackBlock implements BlockEntityProvider, BlockWithElementHolder, BedrockBlock {
    public BaseBackpackBlock(String name) {
        super(name);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return TickableBlockEntity.getTicker(world);
    }

    @Override
    protected boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    protected int getComparatorOutput(BlockState state, World world, BlockPos pos, Direction direction) {
        return ScreenHandler.calculateComparatorOutput(getInventory(
                null, world.getBlockEntity(pos, BackpackBlockEntityRegistry.BACKPACK_BLOCK_ENTITY).get()
        ));
    }

    @Override
    public boolean trinketInteraction(BasicBackpackBlockEntity entity, ServerPlayerEntity player, World world, BlockPos pos) {
        if (BackpackTrinket.isBackSlotOccupied(player)) return false;

        BackpackBlockEntity backpackBlockEntity = (BackpackBlockEntity) entity;

        ItemStack stack = backpackBlockEntity.getDefaultStack().copy();
        BackpackUtils.resizeIfIncorrectSize(player, stack, backpackBlockEntity.getSize());
        BackpackTrinket.equipStack(player, stack);
        world.breakBlock(pos, false);
        return true;
    }

    @Override
    public void openGui(ServerPlayerEntity player, BlockEntity entity) {
        if (!(entity instanceof BackpackBlockEntity backpackBlockEntity)) return;

        BackpackInstance instance = backpackBlockEntity.getInstance();
        resize(player, backpackBlockEntity.getUuid(), instance.inventory(),
                backpackBlockEntity.getSize() + backpackBlockEntity.getExtraSize());
        new BackpackGui(player, instance);
    }

    @Override
    public Inventory getInventory(@Nullable ServerPlayerEntity player, @Nullable BlockEntity entity) {
        if (entity == null) return null;

        BackpackBlockEntity backpackBlockEntity = (BackpackBlockEntity) entity;
        return BackpackManager.getInventory(backpackBlockEntity.getUuid());
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return BackpackBlockEntityRegistry.BACKPACK_BLOCK_ENTITY.instantiate(pos, state);
    }
}
