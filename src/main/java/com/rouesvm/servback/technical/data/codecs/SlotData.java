package com.rouesvm.servback.technical.data.codecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.dynamic.Codecs;

import java.util.ArrayList;
import java.util.List;

public record SlotData(Integer slot, ItemStack itemStack) {

    public static final Codec<SlotData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codecs.UNSIGNED_BYTE.fieldOf("slot").forGetter(SlotData::slot),
            ItemStack.CODEC.fieldOf("itemStacks").forGetter(SlotData::getStack)
    ).apply(instance, SlotData::new));

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
        for (SlotData slotData : data) stacks.set(slotData.slot(), slotData.getStack());
        return stacks;
    }
}
