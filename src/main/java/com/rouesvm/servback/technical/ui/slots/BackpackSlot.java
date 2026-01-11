package com.rouesvm.servback.technical.ui.slots;

import com.rouesvm.servback.technical.config.Configuration;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class BackpackSlot extends Slot {
    public BackpackSlot(Container inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        Item item = stack.getItem();
        return (item.canFitInsideContainerItems()) || (stack.is(ItemTags.SHULKER_BOXES) && Configuration.instance().allow_shulker_boxes_in_backpacks);
    }
}
