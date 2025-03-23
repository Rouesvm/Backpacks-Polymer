package com.rouesvm.servback.ui;

import com.rouesvm.servback.ui.slots.DisabledSlot;
import com.rouesvm.servback.ui.slots.NonBackpackSlot;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class EnderBackpackGui extends SimpleGui {
    protected final ItemStack stack;
    protected final Inventory inventory;

    public EnderBackpackGui(ServerPlayerEntity player, ItemStack stack) {
        super(ScreenHandlerType.GENERIC_9X3, player, false);

        this.stack = stack;
        this.inventory = player.getEnderChestInventory();

        this.setTitle(Text.translatable("item.serverbackpacks.gui_ender"));
        this.fillChest();

        this.open();

        for(int j = 0; j <= 3; ++j) {
            for(int k = 0; k < 9; ++k) {
                final int index;
                int slots = 9 * 3;
                if (j == 0) index = k + (9 * 4 + slots) - 9;
                else index = slots + (k + j * 9) - 9;
                this.screenHandler.setSlot(index, new DisabledSlot(stack, player.getInventory(), k + j * 9, k + j * 9, 0));
            }
        }
    }

    @Override
    public void onTick() {
        if (this.stack.isEmpty())
            this.close();
    }

    public void fillChest() {
        for (int i = 0; i < 27; i++)
            this.setSlotRedirect(i, new NonBackpackSlot(this.inventory, i, i, 0));
    }
}
