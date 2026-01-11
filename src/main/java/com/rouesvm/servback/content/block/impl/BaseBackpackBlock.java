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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import static com.rouesvm.servback.technical.BackpackUtils.resize;

public class BaseBackpackBlock extends BasicBackpackBlock implements EntityBlock, BlockWithElementHolder, BedrockBlock {
    public BaseBackpackBlock(String name) {
        super(name);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        return TickableBlockEntity.getTicker(world);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level world, BlockPos pos, Direction direction) {
        return AbstractContainerMenu.getRedstoneSignalFromContainer(getInventory(
                null, world.getBlockEntity(pos, BackpackBlockEntityRegistry.BACKPACK_BLOCK_ENTITY).get()
        ));
    }

    @Override
    public boolean trinketInteraction(BasicBackpackBlockEntity entity, ServerPlayer player, Level world, BlockPos pos) {
        if (BackpackTrinket.isBackSlotOccupied(player)) return false;

        BackpackBlockEntity backpackBlockEntity = (BackpackBlockEntity) entity;

        ItemStack stack = backpackBlockEntity.getDefaultStack().copy();
        BackpackUtils.resizeIfIncorrectSize(player, stack, backpackBlockEntity.getSize());
        BackpackTrinket.equipStack(player, stack);
        world.destroyBlock(pos, false);
        return true;
    }

    @Override
    public void openGui(ServerPlayer player, BlockEntity entity) {
        if (!(entity instanceof BackpackBlockEntity backpackBlockEntity)) return;

        BackpackInstance instance = backpackBlockEntity.getInstance();
        resize(player, backpackBlockEntity.getUuid(), instance.inventory(),
                backpackBlockEntity.getSize() + backpackBlockEntity.getExtraSize());
        new BackpackGui(player, instance);
    }

    @Override
    public Container getInventory(@Nullable ServerPlayer player, @Nullable BlockEntity entity) {
        if (entity == null) return null;

        BackpackBlockEntity backpackBlockEntity = (BackpackBlockEntity) entity;
        return BackpackManager.getInventory(backpackBlockEntity.getUuid());
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return BackpackBlockEntityRegistry.BACKPACK_BLOCK_ENTITY.create(pos, state);
    }
}
