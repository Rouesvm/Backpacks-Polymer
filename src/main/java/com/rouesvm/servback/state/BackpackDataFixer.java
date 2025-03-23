package com.rouesvm.servback.state;

import com.rouesvm.servback.utils.BackpackInstance;
import com.rouesvm.servback.utils.BackpackInventory;
import com.rouesvm.servback.utils.BackpackManager;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Uuids;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.collection.DefaultedList;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.zip.GZIPInputStream;

public class BackpackDataFixer {
    public static void onWorldLoading(MinecraftServer server) {
        Path path = server.getSavePath(WorldSavePath.ROOT).resolve(Path.of("data/serverbackpacks.dat"));
        NbtCompound oldData = null;

        try (DataInputStream dataInputStream = new DataInputStream(
                new GZIPInputStream(new FileInputStream(path.toFile())))) {
            oldData = NbtIo.readCompound(dataInputStream);
        } catch (Exception ignored) {}

        if (oldData != null) {
            Set<BackpackInstance> instances = convertToV2Format(oldData, server.getRegistryManager());
            instances.forEach(backpackInstance ->
                    BackpackManager.getManager().storedInstances.put(backpackInstance.getUuid(), backpackInstance));

            BackpackManager.getManager().save(server);

            try {
                Files.delete(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static BackpackInstance load(NbtCompound compound, RegistryWrapper.WrapperLookup registryLookup) {
        DefaultedList<ItemStack> stacks = DefaultedList.of();
        Inventories.readNbt(compound.getCompound("contents").get(), stacks, registryLookup);

        BackpackInstance backpackInstance = new BackpackInstance(
                Uuids.toUuid(compound.getIntArray("uuid").get()),
                new BackpackInventory(stacks)
        );
        backpackInstance.lastAccessed = compound.getLong("lastAccessed").get();

        return backpackInstance;
    }

    public static Set<BackpackInstance> convertToV2Format(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        Set<BackpackInstance> instances = new HashSet<>();

        var data = nbt.get("data");
        if (data instanceof NbtCompound compound) {
            Optional<NbtList> list = compound.getList("backpackContents");

            list.ifPresent(nbtElements -> nbtElements.forEach(element ->
                    instances.add(load((NbtCompound) element, registryLookup))));
        }

        return instances;
    }
}
