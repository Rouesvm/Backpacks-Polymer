package com.rouesvm.servback.technical.ui.slots;

import com.rouesvm.servback.datagen.ModItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class NonBackpackSlot extends Slot {
    public NonBackpackSlot(Container inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return !stack.is(ModItemTags.BLACKLISTED);
    }
}
