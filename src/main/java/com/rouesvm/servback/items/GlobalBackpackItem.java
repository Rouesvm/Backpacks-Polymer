package com.rouesvm.servback.items;

import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import com.rouesvm.servback.ui.GlobalBackpackGui;

public class GlobalBackpackItem extends BackpackItem {
    public GlobalBackpackItem() {
        super("Global");
    }

    @Override
    public SimpleGui getGui(ServerPlayerEntity player, ItemStack stack) {
        return new GlobalBackpackGui(player, stack);
    }
}
