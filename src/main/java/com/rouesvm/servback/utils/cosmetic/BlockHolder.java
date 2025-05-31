package com.rouesvm.servback.utils.cosmetic;

import com.rouesvm.servback.block.BasicBlockEntity;
import com.rouesvm.servback.block.BasicPolymerBlock;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class BlockHolder extends ElementHolder {
    public ItemDisplayElement main;
    public BlockPos pos;
    public ServerWorld world;

    public boolean alreadySetItem = false;

    public BlockHolder(ServerWorld world, BlockState state, BlockPos pos) {
        this.main = new ItemDisplayElement();
        this.main.setYaw(state.get(BasicPolymerBlock.FACING).getPositiveHorizontalDegrees());
        this.main.ignorePositionUpdates();
        this.addElement(main);

        this.world = world;
        this.pos = pos;
    }

    @Override
    protected void onTick() {
        if (!alreadySetItem & world != null) {
            BasicBlockEntity blockEntity = (BasicBlockEntity) world.getBlockEntity(pos);
            if (blockEntity != null) {
                this.setMain(blockEntity.getDefaultStack());
                alreadySetItem = true;
            }
        }
    }

    @Override
    public void destroy() {
        for (ServerPlayNetworkHandler player : this.getWatchingPlayers()) {
            player.sendPacket(new EntitiesDestroyS2CPacket(this.getEntityIds()));
        }

        super.destroy();
    }

    public ItemStack getItem() {
        return this.main.getItem();
    }

    public void setMain(ItemStack stack) {
        CustomModelDataComponent component = new CustomModelDataComponent(List.of(), List.of(), List.of("model"), List.of());
        stack.set(DataComponentTypes.CUSTOM_MODEL_DATA, component);
        this.main.setItem(stack);
    }
}
