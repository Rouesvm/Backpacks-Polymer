package com.rouesvm.servback.technical.ui.slots;

import com.rouesvm.servback.technical.config.Configuration;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.screen.slot.Slot;

public class BackpackSlot extends Slot {
    public BackpackSlot(Inventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        Item item = stack.getItem();
        return (item.canBeNested()) || (stack.isIn(ItemTags.SHULKER_BOXES) && Configuration.instance().allow_shulker_boxes_in_backpacks);
    }
}
