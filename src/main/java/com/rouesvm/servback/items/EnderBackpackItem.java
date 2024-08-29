package com.rouesvm.servback.items;

import com.rouesvm.servback.ui.EnderBackpackGui;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;

public class EnderBackpackItem extends BackpackItem {
    public EnderBackpackItem() {
        super("Ender");
    }
    
    @Override
    public SimpleGui getGui(ServerPlayerEntity player, ItemStack stack) {
       return new EnderBackpackGui(player, stack);
    }
}
