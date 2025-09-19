package com.rouesvm.servback.technical.ui.slots;

import com.rouesvm.servback.datagen.ModItemTags;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class NonBackpackSlot extends Slot {
    public NonBackpackSlot(Inventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return !stack.isIn(ModItemTags.BLACKLISTED);
    }
}
