package com.rouesvm.servback.items;

import com.rouesvm.servback.ui.EnderBackpackGui;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;

public class EnderBackpackItem extends BackpackItem {
    public EnderBackpackItem() {
        super("Ender");
    }
    
    @Override
    public void getGui(ServerPlayerEntity player, ItemStack stack) {
        new EnderBackpackGui(player, stack);
    }
}
