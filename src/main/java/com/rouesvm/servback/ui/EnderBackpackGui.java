package com.rouesvm.servback.ui;

import com.rouesvm.servback.slots.NonBackpackSlot;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class EnderBackpackGui extends SimpleGui {
    protected final ItemStack stack;
    protected Inventory inventory;

    public EnderBackpackGui(ServerPlayerEntity player, ItemStack stack) {
        super(ScreenHandlerType.GENERIC_9X3, player, false);

        this.stack = stack;
        this.inventory = player.getEnderChestInventory();

        this.setTitle(Text.literal("Ender Backpack"));
        this.fillChest();
    }

    @Override
    public void onTick() {
        if (stack.isEmpty())
            this.close(false);
        super.onTick();
    }

    public void fillChest() {
        for (int i = 0; i < 27; i++)
            this.setSlotRedirect(i, new NonBackpackSlot(this.inventory, i, i, 0));
    }
}
