package com.rouesvm.servback.utils.cosmetic;

import com.rouesvm.servback.block.BackpackBlockEntity;
import com.rouesvm.servback.block.BasicPolymerBlock;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.EntitiesDestroyS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.joml.Vector3f;

import java.util.List;

public class BlockHolder extends ElementHolder {
    public ItemDisplayElement main;
    public BlockPos pos;
    public ServerWorld world;

    public boolean alreadySetItem = false;

    public BlockHolder(ServerWorld world, BlockState state, BlockPos pos) {
        this.main = new ItemDisplayElement();
        this.main.setDisplaySize(1, 1);
        this.main.setTranslation(new Vector3f(-0.03F, -0.125F, 0F));
        this.main.setYaw(state.get(BasicPolymerBlock.FACING).getPositiveHorizontalDegrees());
        this.main.setItemDisplayContext(ItemDisplayContext.FIXED);
        this.main.ignorePositionUpdates();
        this.addElement(main);

        this.world = world;
        this.pos = pos;
    }

    @Override
    protected void onTick() {
        if (!alreadySetItem & world != null) {
            BackpackBlockEntity blockEntity = (BackpackBlockEntity) world.getBlockEntity(pos);
            if (blockEntity != null) {
                this.setMain(blockEntity.getItemStack().getItem());
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

    public void setMain(Item item) {
        ItemStack stack = item.getDefaultStack();
        CustomModelDataComponent component = new CustomModelDataComponent(List.of(), List.of(), List.of("model"), List.of());
        stack.set(DataComponentTypes.CUSTOM_MODEL_DATA, component);
        this.main.setItem(stack);
    }
}
