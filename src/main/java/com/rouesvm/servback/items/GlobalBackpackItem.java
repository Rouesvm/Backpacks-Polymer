package com.rouesvm.servback.items;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import com.rouesvm.servback.ui.GlobalBackpackGui;

public class GlobalBackpackItem extends ContainerItem {
    public GlobalBackpackItem() {
        super("Global", 0);
    }

    @Override
    public void createGui(ServerPlayerEntity player, ItemStack stack) {
        new GlobalBackpackGui(player, stack);
    }
}
