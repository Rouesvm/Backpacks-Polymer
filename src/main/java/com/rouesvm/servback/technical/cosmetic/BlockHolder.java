package com.rouesvm.servback.technical.cosmetic;

import com.rouesvm.servback.ServerBackpacks;
import com.rouesvm.servback.content.block.BasicBackpackBlockEntity;
import com.rouesvm.servback.content.block.BasicPolymerBlock;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class BlockHolder extends ElementHolder {
    public final ItemDisplayElement main;
    public final BlockPos pos;
    public final ServerLevel world;

    public boolean alreadySetItem = false;

    public BlockHolder(ServerLevel world, BlockState state, BlockPos pos) {
        this.main = new ItemDisplayElement();
        this.main.setYaw(state.getValue(BasicPolymerBlock.FACING).toYRot());
        this.main.ignorePositionUpdates();
        this.addElement(main);

        this.world = world;
        this.pos = pos;
    }

    // wonky
    // I agree with you
    @Override
    protected void onTick() {
        if (alreadySetItem || world == null) return;
        if (!(world.getBlockEntity(pos) instanceof BasicBackpackBlockEntity blockEntity)) return;

        this.setMain(blockEntity.getDefaultStack());
        alreadySetItem = true;
    }

    @Override
    public boolean startWatching(ServerGamePacketListenerImpl client) {
        return !ServerBackpacks.isBedrock(client.getPlayer().getUUID()) && super.startWatching(client);
    }

    public void setMain(ItemStack stack) {
        CustomModelData component = new CustomModelData(List.of(), List.of(), List.of("model"), List.of());
        stack.set(DataComponents.CUSTOM_MODEL_DATA, component);
        this.main.setItem(stack);
    }
}
