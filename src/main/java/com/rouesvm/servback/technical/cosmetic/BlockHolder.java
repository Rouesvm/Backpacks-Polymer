package com.rouesvm.servback.technical.cosmetic;

import com.rouesvm.servback.ServerBackpacks;
import com.rouesvm.servback.content.block.BasicBackpackBlockEntity;
import com.rouesvm.servback.content.block.BasicPolymerBlock;
import com.rouesvm.servback.content.registry.BackpackDataComponentTypes;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class BlockHolder extends ElementHolder {
    public final ItemDisplayElement main;
    public final BlockPos pos;
    public final ServerWorld world;

    public boolean alreadySetItem = false;

    public BlockHolder(ServerWorld world, BlockState state, BlockPos pos) {
        this.main = new ItemDisplayElement();
        this.main.setYaw(state.get(BasicPolymerBlock.FACING).asRotation());
        this.main.ignorePositionUpdates();
        this.addElement(main);

        this.world = world;
        this.pos = pos;
    }

    // wonky
    @Override
    protected void onTick() {
        if (!alreadySetItem & world != null) {
            BasicBackpackBlockEntity blockEntity = (BasicBackpackBlockEntity) world.getBlockEntity(pos);
            if (blockEntity != null) {
                this.setMain(blockEntity.getDefaultStack().getItem());
                alreadySetItem = true;
            }
        }
    }

    public void setMain(Item item) {
        ItemStack stack = item.getDefaultStack();
        stack.set(BackpackDataComponentTypes.EQUIPPED_TYPE, true);
        this.main.setItem(stack);
    }
}
