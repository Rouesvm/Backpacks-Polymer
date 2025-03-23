package com.rouesvm.servback.state.codecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

import java.util.List;

public class InventoryData {
    private final List<SlotData> itemStacks;

    public InventoryData(List<SlotData> inventory) {
        this.itemStacks = inventory;
    }

    public static final Codec<InventoryData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            SlotData.CODEC.listOf().fieldOf("Items").forGetter(InventoryData::getItemStacks)
    ).apply(instance, InventoryData::new));

    public List<SlotData> getItemStacks() {
        return itemStacks;
    }

    public static DefaultedList<ItemStack> getHeldStacks(List<SlotData> data) {
        DefaultedList<ItemStack> stacks = DefaultedList.ofSize(9 * 6, ItemStack.EMPTY);
        if (!data.isEmpty()) data.forEach(slotData -> stacks.set(slotData.getSlot(), slotData.getStack()));
        return stacks;
    }
}
