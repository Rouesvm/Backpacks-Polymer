package com.rouesvm.servback.items;

import com.rouesvm.servback.ui.EnderBackpackGui;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

public class EnderBackpackItem extends BasicPolymerItem {
    public EnderBackpackItem() {
        super("Ender", 0);
    }
    
    @Override
    public void createGui(ServerPlayerEntity player, ItemStack stack) {
        new EnderBackpackGui(player, stack);
    }
}
