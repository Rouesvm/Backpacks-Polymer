package com.rouesvm.servback.ui;

import com.rouesvm.servback.slots.DisabledSlot;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import com.rouesvm.servback.Main;

public class GlobalBackpackGui extends SimpleGui {

    private final int slots = 27;
    private final Inventory inventory = Main.getInventory();

    public GlobalBackpackGui(ServerPlayerEntity player, ItemStack stack) {
        super(ScreenHandlerType.GENERIC_9X3, player, false);

        setTitle(Text.literal("Global Backpack"));
        fillChest();
        open();

        int disableSlot = player.getInventory().getSlotWithStack(stack);
        screenHandler.setSlot(slots + 27 + disableSlot, new DisabledSlot(this.inventory, slots, slots,0));
    }

    public void fillChest() {
        for (int i = 0; i < 27; i++)
            setSlotRedirect(i, new Slot(this.inventory, i, i, 0));
    }
}