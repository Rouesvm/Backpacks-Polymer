package com.rouesvm.servback.technical.data;

import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.fixes.References;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class BackpackDFU {
    public static void applyDataFixToItemStacks(@NotNull MinecraftServer server, CompoundTag root, int oldVersion, int newVersion) {
        DataFixer fixer = server.getFixerUpper();
        if (oldVersion == newVersion) return;

        Optional<CompoundTag> contents = root.getCompound("contents");
        if (contents.isEmpty()) return;

        Optional<ListTag> items = contents.get().getList("Items");
        if (items.isEmpty()) return;

        ListTag itemList = items.get();
        for (int j = 0; j < itemList.size(); ++j) {
            Optional<CompoundTag> slotCompound = itemList.getCompound(j);
            if (slotCompound.isEmpty()) continue;

            CompoundTag slot = slotCompound.get();

            Optional<CompoundTag> wrapped = slot.getCompound("itemStacks");
            if (wrapped.isEmpty()) continue;

            Dynamic<Tag> inputDynamic = new Dynamic<>(NbtOps.INSTANCE, wrapped.get());
            Dynamic<Tag> outputDynamic = fixer.update(
                    References.ITEM_STACK, inputDynamic,
                    oldVersion, newVersion
            );

            slot.put("itemStacks", outputDynamic.getValue());
        }

        root.putInt("data_version", newVersion);
    }
}
