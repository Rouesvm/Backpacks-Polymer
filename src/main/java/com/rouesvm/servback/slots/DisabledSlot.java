package com.rouesvm.servback.slots;

import com.rouesvm.servback.items.ContainerItem;
import com.rouesvm.servback.items.GuiItem;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class DisabledSlot extends Slot {
    public DisabledSlot(Inventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return !(stack.getItem() instanceof GuiItem);
    }
}
