package com.rouesvm.servback.technical.data.types.persistentData;

import com.rouesvm.servback.ServerBackpacks;
import com.rouesvm.servback.technical.data.BackpackInstance;
import com.rouesvm.servback.technical.data.DATA_TYPE;
import com.rouesvm.servback.technical.data.types.FallbackData;
import com.rouesvm.servback.technical.manager.Manager;
import com.rouesvm.servback.technical.ui.inventory.BackpackInventory;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.TagValueInput;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.zip.GZIPInputStream;

// The 1.21.1 way of loading data.
public class BackpackPersistentData extends FallbackData {
    public BackpackPersistentData(Manager manager) {
        super(manager);
    }

    @Override
    public boolean initializeData(boolean hasLoaded) {
        if (!hasLoaded) {
            return isDataPresent();
        } else return false;
    }

    @Override
    public DATA_TYPE getType() {
        return DATA_TYPE.OLD_MINECRAFT_STATE;
    }

    private boolean isDataPresent() {
        Path path = manager().server().getWorldPath(LevelResource.ROOT).resolve(Path.of("data/serverbackpacks.dat"));
        if (!path.toFile().exists()) return false;

        CompoundTag oldData = null;

        try (DataInputStream dataInputStream = new DataInputStream(
                new GZIPInputStream(new FileInputStream(path.toFile())))) {
            oldData = NbtIo.read(dataInputStream);
        } catch (Exception ignored) {}

        if (oldData != null) {
            Set<BackpackInstance> instances = convertToV2Format(oldData);
            addBackpackInstances(instances);

            try {
                Files.delete(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            return true;
        }

        return false;
    }

    private Set<BackpackInstance> convertToV2Format(CompoundTag nbt) {
        Set<BackpackInstance> instances = new HashSet<>();

        var data = nbt.get("data");
        if (data instanceof CompoundTag compound) {
            Optional<ListTag> list = compound.getList("backpackContents");

            list.ifPresent(nbtElements -> nbtElements.forEach(element ->
                    instances.add(load((CompoundTag) element, manager().server().registryAccess()))));
        }

        return instances;
    }

    private static BackpackInstance load(CompoundTag compound, HolderLookup.Provider registryLookup) {
        return new BackpackInstance(
                UUIDUtil.uuidFromIntArray(compound.getIntArray("uuid").get()),
                loadInventory(compound.getCompound("contents").get(), registryLookup)
        );
    }

    private static BackpackInventory loadInventory(CompoundTag nbtCompound, HolderLookup.Provider registryLookup) {
        NonNullList<ItemStack> itemStacks = NonNullList.withSize(9 * 6, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(TagValueInput.create(new ProblemReporter.ScopedCollector(ServerBackpacks.LOGGER), registryLookup, nbtCompound), itemStacks);
        return new BackpackInventory(itemStacks);
    }
}