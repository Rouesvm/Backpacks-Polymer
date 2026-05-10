package com.rouesvm.servback.technical.data;

import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class BackpackDFU {
    public static void applyDataFixToItemStacks(@NotNull MinecraftServer server, CompoundTag root, RegistryOps<Tag> ops, int oldVersion, int newVersion) {
        DataFixer fixer = server.getFixerUpper();

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

            DataResult<Dynamic<Tag>> result = ItemStack.CODEC
                    .decode(ops, slot.getCompoundOrEmpty("itemStacks"))
                    .map(Pair::getFirst)
                    .map(stack -> {
                        var encoded = ItemStack.CODEC.encodeStart(ops, stack);

                        if (encoded.resultOrPartial().isPresent()) {
                            Dynamic<Tag> dynamic = new Dynamic<>(ops, encoded.resultOrPartial().get());
                            return fixer.update(References.ITEM_STACK, dynamic, oldVersion, newVersion);
                        }

                        return fixer.update(References.ITEM_STACK, new Dynamic<>(ops, wrapped.get()), oldVersion, newVersion);
                    });

            result.resultOrPartial().ifPresent(dynamic ->
                    slot.put("itemStacks", dynamic.getValue())
            );
        }

        root.putInt("data_version", newVersion);
    }
}
