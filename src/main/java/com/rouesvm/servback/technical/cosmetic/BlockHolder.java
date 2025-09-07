package com.rouesvm.servback.technical.cosmetic;

import com.rouesvm.servback.ServerBackpacks;
import com.rouesvm.servback.content.block.BasicBackpackBlockEntity;
import com.rouesvm.servback.content.block.BasicPolymerBlock;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class BlockHolder extends ElementHolder {
    public final ItemDisplayElement main;
    public final BlockPos pos;
    public final ServerWorld world;

    public boolean alreadySetItem = false;

    public BlockHolder(ServerWorld world, BlockState state, BlockPos pos) {
        this.main = new ItemDisplayElement();
        this.main.setYaw(state.get(BasicPolymerBlock.FACING).getPositiveHorizontalDegrees());
        this.main.ignorePositionUpdates();
        this.addElement(main);

        this.world = world;
        this.pos = pos;
    }

    // wonky
    @Override
    protected void onTick() {
        if (alreadySetItem || world == null) return;
        if (!(world.getBlockEntity(pos) instanceof BasicBackpackBlockEntity blockEntity)) return;

        this.setMain(blockEntity.getDefaultStack());
        alreadySetItem = true;
    }

    @Override
    public boolean startWatching(ServerPlayNetworkHandler player) {
        if (ServerBackpacks.BEDROCK_PLAYERS.contains(player.getPlayer())) {
            return false;
        }
        return super.startWatching(player);
    }

    public void setMain(ItemStack stack) {
        CustomModelDataComponent component = new CustomModelDataComponent(List.of(), List.of(), List.of("model"), List.of());
        stack.set(DataComponentTypes.CUSTOM_MODEL_DATA, component);
        this.main.setItem(stack);
    }
}
