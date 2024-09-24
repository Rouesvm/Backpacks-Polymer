package com.rouesvm.servback.ui;

import com.rouesvm.servback.slots.BackpackSlot;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class EnderBackpackGui extends SimpleGui {

    protected final ItemStack stack;

    public EnderBackpackGui(ServerPlayerEntity player, ItemStack stack) {
        super(ScreenHandlerType.GENERIC_9X3, player, false);

        this.stack = stack;

        setTitle(Text.literal("Ender Backpack"));
        fillChest();
    }

    @Override
    public void onTick() {
        if (stack.isEmpty())
            close(false);
        super.onTick();
    }

    public void fillChest() {
        for (int i = 0; i < 27; i++)
            setSlotRedirect(i, new BackpackSlot(player.getEnderChestInventory(), i, i, 0));
    }
}
