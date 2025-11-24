package com.rouesvm.servback.technical.data.alternative;

import com.rouesvm.servback.ServerBackpacks;
import com.rouesvm.servback.technical.data.BackpackInstance;
import com.rouesvm.servback.technical.manager.BackpackManager;
import com.rouesvm.servback.technical.ui.inventory.BackpackInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.storage.NbtReadView;
import net.minecraft.util.ErrorReporter;
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

public class BackpackStateUpper {
    // The 1.21.1 way of loading data.

    public static boolean loadData(MinecraftServer server, boolean hasLoaded) {
        if (!hasLoaded) {
            return isDataPresent(server);
        } else return false;
    }

    private static boolean isDataPresent(MinecraftServer server) {
        Path path = server.getSavePath(WorldSavePath.ROOT).resolve(Path.of("data/serverbackpacks.dat"));
        if (!path.toFile().exists()) return false;

        NbtCompound oldData = null;

        try (DataInputStream dataInputStream = new DataInputStream(
                new GZIPInputStream(new FileInputStream(path.toFile())))) {
            oldData = NbtIo.readCompound(dataInputStream);
        } catch (Exception ignored) {}

        if (oldData != null) {
            Set<BackpackInstance> instances = convertToV2Format(oldData, server.getRegistryManager());
            BackpackManager.instance().loadIntoStoredInstances(instances);

            try {
                Files.delete(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            return true;
        }

        return false;
    }

    private static BackpackInstance load(NbtCompound compound, RegistryWrapper.WrapperLookup registryLookup) {
        return new BackpackInstance(
                Uuids.toUuid(compound.getIntArray("uuid").get()),
                loadInventory(compound.getCompound("contents").get(), registryLookup)
        );
    }

    private static Set<BackpackInstance> convertToV2Format(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        Set<BackpackInstance> instances = new HashSet<>();

        var data = nbt.get("data");
        if (data instanceof NbtCompound compound) {
            Optional<NbtList> list = compound.getList("backpackContents");

            list.ifPresent(nbtElements -> nbtElements.forEach(element ->
                    instances.add(load((NbtCompound) element, registryLookup))));
        }

        return instances;
    }

    private static BackpackInventory loadInventory(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup registryLookup) {
        DefaultedList<ItemStack> itemStacks = DefaultedList.ofSize(9 * 6, ItemStack.EMPTY);
        Inventories.readData(NbtReadView.create(new ErrorReporter.Logging(ServerBackpacks.LOGGER), registryLookup, nbtCompound), itemStacks);
        return new BackpackInventory(itemStacks);
    }
}
