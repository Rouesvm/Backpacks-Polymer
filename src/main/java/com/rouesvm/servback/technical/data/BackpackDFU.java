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

public class BackpackDFU {
    public static void applyDataFixToItemStacks(@NotNull MinecraftServer server, NbtCompound root, int oldVersion, int newVersion) {
        DataFixer fixer = server.getDataFixer();
        if (oldVersion == newVersion) return;

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
                    oldVersion, newVersion
            );

            slot.put("itemStacks", outputDynamic.getValue());
        }

        root.putInt("data_version", newVersion);
    }
}
