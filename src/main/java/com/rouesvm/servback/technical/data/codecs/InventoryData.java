package com.rouesvm.servback.technical.data.codecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

import java.util.List;

public record InventoryData(List<SlotData> itemStacks) {
    public static final Codec<InventoryData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            SlotData.CODEC.listOf().fieldOf("Items").forGetter(InventoryData::itemStacks)
    ).apply(instance, InventoryData::new));

    public static InventoryData stacksListToData(DefaultedList<ItemStack> stacks) {
        return new InventoryData(SlotData.writeToCodec(stacks));
    }

    public static DefaultedList<ItemStack> getHeldStacks(List<SlotData> data, int size) {
        DefaultedList<ItemStack> stacks = DefaultedList.ofSize(size, ItemStack.EMPTY);
        if (!data.isEmpty()) data.forEach(slotData -> stacks.set(slotData.slot(), slotData.getStack()));
        return stacks;
    }
}
