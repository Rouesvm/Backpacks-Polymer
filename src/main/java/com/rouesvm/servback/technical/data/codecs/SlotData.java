package com.rouesvm.servback.technical.data.codecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public record SlotData(Integer slot, ItemStack itemStack) {

    public static final Codec<SlotData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ExtraCodecs.UNSIGNED_BYTE.fieldOf("slot").forGetter(SlotData::slot),
            ItemStack.CODEC.fieldOf("itemStacks").forGetter(SlotData::getStack)
    ).apply(instance, SlotData::new));

    public ItemStack getStack() {
        return itemStack;
    }

    public static List<SlotData> writeToCodec(NonNullList<ItemStack> stacks) {
        List<SlotData> data = new ArrayList<>();
        for (int i = 0; i < stacks.size(); i++) {
            if (stacks.get(i) == ItemStack.EMPTY) continue;
            data.add(new SlotData(i, stacks.get(i)));
        }
        return data;
    }

    public static NonNullList<ItemStack> readFromCodec(List<SlotData> data, int size) {
        NonNullList<ItemStack> stacks = NonNullList.withSize(size, ItemStack.EMPTY);
        for (SlotData slotData : data) stacks.set(slotData.slot(), slotData.getStack());
        return stacks;
    }
}
