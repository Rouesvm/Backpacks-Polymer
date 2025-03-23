package com.rouesvm.servback.state;

import com.rouesvm.servback.state.codecs.SlotData;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.zip.GZIPInputStream;

public class BackpackDataFixer {
    public static void onWorldLoading(MinecraftServer server) {
        Path path = server.getSavePath(WorldSavePath.ROOT).resolve(Path.of("data/serverbackpacks.dat"));
        NbtCompound oldData = null;

        try (DataInputStream dataInputStream = new DataInputStream(
                new GZIPInputStream(new FileInputStream(path.toFile())))) {
            oldData = NbtIo.readCompound(dataInputStream);
        } catch (Exception e) {
            System.out.println("Failed to read with GZIPInputStream: " + e.getMessage());
        }

        if (oldData != null) {
            System.out.println("Successfully read backpack data: " + oldData);
            System.out.println(convertToV2Format(oldData, server.getRegistryManager()));
        }
    }

    private static List<SlotData> convertToV2Format(NbtCompound backpackData, RegistryWrapper.WrapperLookup registryLookup) {
        List<SlotData> slotDataList = new ArrayList<>();

        if (backpackData.contains("Items")) {
            NbtList itemsList = backpackData.getList("Items").get();

            for (int i = 0; i < itemsList.size(); i++) {
                Optional<NbtCompound> itemNbt = itemsList.getCompound(i);
                byte slot = itemNbt.get().getByte("Slot", (byte) 0);

                Optional<ItemStack> stack = ItemStack.fromNbt(registryLookup, itemNbt.get());
                slotDataList.add(new SlotData((int) slot, stack.get()));
            }
        }

        return slotDataList;
    }
}
