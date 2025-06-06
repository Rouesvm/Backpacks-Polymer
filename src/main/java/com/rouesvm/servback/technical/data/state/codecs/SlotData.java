package com.rouesvm.servback.technical.data.state.codecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.dynamic.Codecs;

import java.util.ArrayList;
import java.util.List;

public class SlotData {
    private final Integer slot;
    private final ItemStack itemStack;

    public SlotData(Integer slot, ItemStack stack) {
        this.slot = slot;
        this.itemStack = stack;
    }

    public static final Codec<SlotData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codecs.UNSIGNED_BYTE.fieldOf("slot").forGetter(SlotData::getSlot),
            ItemStack.CODEC.fieldOf("itemStacks").forGetter(SlotData::getStack)
    ).apply(instance, SlotData::new));

    public Integer getSlot() {
        return slot;
    }

    public ItemStack getStack() {
        return itemStack;
    }

    public static List<SlotData> writeToCodec(DefaultedList<ItemStack> stacks) {
        List<SlotData> data = new ArrayList<>();
        for (int i = 0; i < stacks.size(); i++) {
            if (stacks.get(i) == ItemStack.EMPTY) continue;
            data.add(new SlotData(i, stacks.get(i)));
        }
        return data;
    }

    public static DefaultedList<ItemStack> readFromCodec(List<SlotData> data, int size) {
        DefaultedList<ItemStack> stacks = DefaultedList.ofSize(size, ItemStack.EMPTY);
        for (SlotData slotData : data) stacks.set(slotData.getSlot(), slotData.getStack());
        return stacks;
    }
}
