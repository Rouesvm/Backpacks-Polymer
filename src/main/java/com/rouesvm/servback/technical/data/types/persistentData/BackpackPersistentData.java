import com.rouesvm.servback.ServerBackpacks;
import com.rouesvm.servback.technical.data.BackpackInstance;
import com.rouesvm.servback.technical.data.DATA_TYPE;
import com.rouesvm.servback.technical.data.types.FallbackData;
import com.rouesvm.servback.technical.data.types.LegacyData;
import com.rouesvm.servback.technical.manager.BackpackManager;
import com.rouesvm.servback.technical.manager.Manager;
import com.rouesvm.servback.technical.ui.inventory.BackpackInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
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
import java.util.*;
import java.util.zip.GZIPInputStream;

// The 1.21.1 way of loading data.
<<<<<<<< HEAD:src/main/java/com/rouesvm/servback/technical/data/types/state/BackpackPersistentData.java
public class BackpackPersistentData implements LegacyData {
    private final Set<BackpackInstance> loadedBackpackData = new HashSet<>();
    private final Map<UUID, BackpackInstance> loadedBackpacks = new HashMap<>();

    private final Manager manager;

    public BackpackPersistentData(Manager manager) {
        this.manager = manager;
    }

    @Override
    public boolean loadData(boolean hasLoaded) {
========
public class BackpackPersistentData extends FallbackData {
    public BackpackPersistentData(Manager manager) {
        super(manager);
    }

    @Override
    public boolean initializeData(boolean hasLoaded) {
>>>>>>>> 1.21.9:src/main/java/com/rouesvm/servback/technical/data/types/persistentData/BackpackPersistentData.java
        if (!hasLoaded) {
            return isDataPresent();
        } else return false;
    }

    @Override
<<<<<<<< HEAD:src/main/java/com/rouesvm/servback/technical/data/types/state/BackpackPersistentData.java
    public Set<UUID> getUUIDs() {
        return new HashSet<>(uuids);
    }

    @Override
    public Optional<BackpackInstance> getOrLoadBackpack(UUID uuid) {
        BackpackInstance cached = loadedBackpacks.get(uuid);
        if (cached != null) {
            return Optional.of(cached);
        } else return Optional.empty();
    }

    @Override
    public BackpackManager.DATA_TYPE getType() {
        return BackpackManager.DATA_TYPE.OLD_MINECRAFT_STATE;
    }

    private boolean isDataPresent() {
        Path path = manager.server().getSavePath(WorldSavePath.ROOT).resolve(Path.of("data/serverbackpacks.dat"));
========
    public DATA_TYPE getType() {
        return DATA_TYPE.OLD_MINECRAFT_STATE;
    }

    private boolean isDataPresent() {
        Path path = manager().server().getSavePath(WorldSavePath.ROOT).resolve(Path.of("data/serverbackpacks.dat"));
>>>>>>>> 1.21.9:src/main/java/com/rouesvm/servback/technical/data/types/persistentData/BackpackPersistentData.java
        if (!path.toFile().exists()) return false;

        NbtCompound oldData = null;

        try (DataInputStream dataInputStream = new DataInputStream(
                new GZIPInputStream(new FileInputStream(path.toFile())))) {
            oldData = NbtIo.readCompound(dataInputStream);
        } catch (Exception ignored) {}

        if (oldData != null) {
<<<<<<<< HEAD:src/main/java/com/rouesvm/servback/technical/data/types/state/BackpackPersistentData.java
            loadedBackpackData.addAll(convertToV2Format(oldData));
            loadedBackpackData.forEach(backpackInstance -> loadedBackpacks.put(backpackInstance.uuid(), backpackInstance));
            uuids.addAll(loadedBackpacks.keySet());
========
            Set<BackpackInstance> instances = convertToV2Format(oldData);
            addBackpackInstances(instances);
>>>>>>>> 1.21.9:src/main/java/com/rouesvm/servback/technical/data/types/persistentData/BackpackPersistentData.java

            try {
                Files.delete(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            return true;
        }

        return false;
    }

    private Set<BackpackInstance> convertToV2Format(NbtCompound nbt) {
        Set<BackpackInstance> instances = new HashSet<>();

        var data = nbt.get("data");
        if (data instanceof NbtCompound compound) {
            Optional<NbtList> list = compound.getList("backpackContents");

            list.ifPresent(nbtElements -> nbtElements.forEach(element ->
<<<<<<<< HEAD:src/main/java/com/rouesvm/servback/technical/data/types/state/BackpackPersistentData.java
                    instances.add(load((NbtCompound) element, manager.server().getRegistryManager()))));
========
                    instances.add(load((NbtCompound) element, manager().server().getRegistryManager()))));
>>>>>>>> 1.21.9:src/main/java/com/rouesvm/servback/technical/data/types/persistentData/BackpackPersistentData.java
        }

        return instances;
    }

    private static BackpackInstance load(NbtCompound compound, RegistryWrapper.WrapperLookup registryLookup) {
        return new BackpackInstance(
                Uuids.toUuid(compound.getIntArray("uuid").get()),
                loadInventory(compound.getCompound("contents").get(), registryLookup)
        );
    }

    private static BackpackInventory loadInventory(NbtCompound nbtCompound, RegistryWrapper.WrapperLookup registryLookup) {
        DefaultedList<ItemStack> itemStacks = DefaultedList.ofSize(9 * 6, ItemStack.EMPTY);
        Inventories.readData(NbtReadView.create(new ErrorReporter.Logging(ServerBackpacks.LOGGER), registryLookup, nbtCompound), itemStacks);
        return new BackpackInventory(itemStacks);
    }
}
