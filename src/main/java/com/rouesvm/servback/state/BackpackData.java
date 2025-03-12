package com.rouesvm.servback.state;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Uuids;

import java.util.List;
import java.util.UUID;

public record BackpackData(UUID uuid, List<ItemStack> inventory) {
    public static final Codec<BackpackData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Uuids.CODEC.fieldOf("id").forGetter(BackpackData::uuid),
            ItemStack.CODEC.listOf().fieldOf("itemStacks").forGetter(BackpackData::inventory)
    ).apply(instance, BackpackData::new));
}

