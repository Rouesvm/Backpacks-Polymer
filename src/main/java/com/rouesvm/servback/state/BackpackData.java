package com.rouesvm.servback.state;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Uuids;
import net.minecraft.util.collection.DefaultedList;

import java.util.List;
import java.util.UUID;

public class BackpackData {
    private final UUID uuid;
    private final List<ItemStack> itemStacks;

    public BackpackData(UUID uuid, List<ItemStack> inventory) {
        this.uuid = uuid;
        this.itemStacks = inventory;
    }

    public BackpackData() {
        this(UUID.randomUUID(), DefaultedList.ofSize(9 * 3, ItemStack.EMPTY));
    }

    public static final Codec<BackpackData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Uuids.CODEC.fieldOf("id").forGetter(BackpackData::getUuid),
            ItemStack.CODEC.listOf().fieldOf("itemStacks").forGetter(BackpackData::getItemStacks)
    ).apply(instance, BackpackData::new));

    public UUID getUuid() {
        return uuid;
    }

    public List<ItemStack> getItemStacks() {
        return itemStacks;
    }

    public DefaultedList<ItemStack> getHeldStacks() {
        DefaultedList<ItemStack> stacks = DefaultedList.of();
        stacks.addAll(getItemStacks());
        return stacks;
    }
}
