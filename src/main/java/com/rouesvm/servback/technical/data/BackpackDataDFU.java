package com.rouesvm.servback.technical.data;

import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Dynamic;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class BackpackDataDFU {
    public static void applyDataFixToItemStacks(@NotNull MinecraftServer server, NbtCompound root, int newVersion) {
        DataFixer fixer = server.getDataFixer();

        Optional<Integer> data_version = root.getInt("data_version");
        if (data_version.isEmpty() || data_version.get() == newVersion) return;

        Optional<NbtCompound> contents = root.getCompound("contents");
        if (contents.isEmpty()) return;

        Optional<NbtList> items = contents.get().getList("Items");
        if (items.isEmpty()) return;

        NbtList itemList = items.get();
        for (int j = 0; j < itemList.size(); ++j) {
            Optional<NbtCompound> slotCompound = itemList.getCompound(j);
            if (slotCompound.isEmpty()) continue;

            NbtCompound slot = slotCompound.get();

            Optional<NbtCompound> wrapped = slot.getCompound("itemStacks");
            if (wrapped.isEmpty()) continue;

            Dynamic<NbtElement> inputDynamic = new Dynamic<>(NbtOps.INSTANCE, wrapped.get());
            Dynamic<NbtElement> outputDynamic = fixer.update(
                    TypeReferences.ITEM_STACK, inputDynamic,
                    data_version.get(), newVersion
            );

            itemList.set(j, outputDynamic.getValue());
        }

        root.putInt("data_version", newVersion);
    }
}
