package com.rouesvm.servback.technical.data.types.file;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.rouesvm.servback.ServerBackpacks;
import com.rouesvm.servback.technical.data.BackpackDFU;
import com.rouesvm.servback.technical.data.BackpackInstance;
import com.rouesvm.servback.technical.data.codecs.BackpackInstanceData;
import com.rouesvm.servback.technical.data.types.Data;
import com.rouesvm.servback.technical.manager.BackpackManager;
import com.rouesvm.servback.technical.manager.Manager;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.*;
import net.minecraft.util.WorldSavePath;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class BackpackData implements Data {
    private static final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "ServerBackpacks-Data");
        t.setDaemon(true);
        return t;
    });

    private final Map<UUID, BackpackInstance> loadedBackpacks = Object2ObjectMaps.synchronize(new Object2ObjectOpenHashMap<>());

    private final Manager manager;
    private final Path saveDir;

    private  final BackpackDataBackup dataBackup;

    public BackpackData(Manager manager) {
        this.manager = manager;
        this.dataBackup = new BackpackDataBackup(manager, this);

        this.saveDir = manager.server().getSavePath(WorldSavePath.ROOT).resolve("data/backpacks");
        Runtime.getRuntime().addShutdownHook(new Thread(this::onRuntimeEnded));
    }

    public void onRuntimeEnded() {
        executor.shutdown();

        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                ServerBackpacks.LOGGER.warn("Thread did not stop in time, forcing shutdown.");
                executor.shutdownNow();
            } else {
                ServerBackpacks.LOGGER.info("Thread stopped gracefully.");
            }
        } catch (InterruptedException e) {
            ServerBackpacks.LOGGER.error("Error while stopping thread {}", e.getMessage());
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public Set<UUID> getUUIDs() {
        return new HashSet<>(uuids);
    }

    @Override
    public Optional<BackpackInstance> getOrLoadBackpack(UUID uuid) {
        BackpackInstance cached = loadedBackpacks.get(uuid);
        if (cached != null) {
            return Optional.of(cached);
        }

        Optional<BackpackInstance> loaded = loadSingle(saveDir, uuid);
        if (loaded.isPresent()) {
            BackpackInstance instance = loaded.get();
            loadedBackpacks.put(uuid, instance);
            return Optional.of(instance);
        }

        return Optional.empty();
    }

    private Optional<BackpackInstance> loadSingle(Path saveDir, UUID uuid) {
        if (saveDir == null) return Optional.empty();

        Path file = saveDir.resolve(uuid.toString() + ".dat");
        if (!Files.exists(file)) return Optional.empty();

        try (DataInputStream dis = new DataInputStream(Files.newInputStream(file))) {
            NbtCompound nbt = NbtIo.readCompressed(dis, NbtSizeTracker.ofUnlimitedBytes());
            Optional<Integer> data_version = nbt.getInt("data_version");

            int latest = SharedConstants.getGameVersion().dataVersion().id();
            BackpackDFU.applyDataFixToItemStacks(manager.server(), nbt, data_version.orElse(latest), latest);

            DataResult<Pair<BackpackInstanceData, NbtElement>> data =
                    BackpackInstanceData.CODEC.decode(manager.nbtOps(), nbt);
            return data.result().map(pair -> pair.getFirst().toInstance(uuid));
        } catch (IOException | NbtCrashException e) {
            ServerBackpacks.LOGGER.error("Failed to load single backpack {}", uuid, e);
            return Optional.empty();
        }
    }

    @Override
    public boolean loadData(boolean hasLoaded) {
        if (!hasLoaded) {
            return loadData() && !uuids.isEmpty();
        }
        return false;
    }

    @Override
    public BackpackManager.DATA_TYPE getType() {
        return BackpackManager.DATA_TYPE.FILE_DATA;
    }

    private boolean loadData() {
        try {
            Files.createDirectories(saveDir);

            try (Stream<Path> paths = Files.list(saveDir)) {
                paths.filter(p -> p.toString().endsWith(".dat"))
                        .forEach(p -> {
                            String filename = p.getFileName().toString().replace(".dat", "");
                            uuids.add(UUID.fromString(filename));
                        });
            }

            return !uuids.isEmpty();
        } catch (IOException e) {
            ServerBackpacks.LOGGER.error("Failed to check backpack directory", e);
            return false;
        }
    }

    @Override
    public void saveSingleToDisk(BackpackInstance instance, Path saveDir) {
        if (instance == null) return;

        if (!Files.exists(saveDir)) {
            saveDir = manager.server().getSavePath(WorldSavePath.ROOT).resolve("data/backpacks");
            try {
                Files.createDirectories(saveDir);
            } catch (IOException e) {
                ServerBackpacks.LOGGER.error("Error while creating directory {}", e.getMessage());
            }
        }

        Path targetFile = saveDir.resolve(instance.uuid() + ".dat");
        Path tempFile = saveDir.resolve(instance.uuid() + ".dat.tmp");

        try {
            BackpackInstanceData backpackData = instance.toCodec();
            DataResult<NbtElement> data = BackpackInstanceData.CODEC.encodeStart(
                    manager.nbtOps(),
                    backpackData
            );

            Optional<NbtElement> result = data.result();
            if (result.isPresent() && result.get().asCompound().isPresent()) {
                try (OutputStream os = Files.newOutputStream(tempFile,
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                    NbtIo.writeCompressed(result.get().asCompound().get(), os);
                }

                Files.move(tempFile, targetFile,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            }
        } catch (IOException e) {
            ServerBackpacks.LOGGER.error("Failed to save backpack {}: {}", instance.uuid(), e.getStackTrace());
            try {
                Files.deleteIfExists(tempFile);
            } catch (IOException cleanupError) {
                ServerBackpacks.LOGGER.warn("Failed to cleanup temp file: {}",
                        tempFile, cleanupError);
            }
        }
    }

    @Override
    public void saveSingleToDisk(BackpackInstance instance) {
        uuids.add(instance.uuid());

        BackpackInstance finalInstance = instance.copy();
        executor.execute(() -> saveSingleToDisk(finalInstance, saveDir));
    }

    @Override
    public void saveAllToDisk() {
        final List<BackpackInstance> finalStoredInventories = manager.getBackpackInstances().stream()
                .filter(Objects::nonNull)
                .toList();

        executor.execute(() -> {
            for (BackpackInstance instance : finalStoredInventories) {
                saveSingleToDisk(instance, saveDir);
            }
            ServerBackpacks.LOGGER.info("Saving data for Server Backpacks.");
        });
    }

    @Override
    public Set<BackpackInstance> getBackpackInstances() {
        return new HashSet<>(loadedBackpacks.values());
    }

    @Override
    public void replaceStoredInventory(BackpackInstance backpackInstance) {
        loadedBackpacks.put(backpackInstance.uuid(), backpackInstance);
    }

    @Override
    public void replaceStoredInventories(Set<BackpackInstance> backpackInstances) {
        loadedBackpacks.clear();
        for (BackpackInstance instance : backpackInstances) {
            loadedBackpacks.put(instance.uuid(), instance);
        }
    }

    @Override
    public void createSingularBackup(BackpackInstance instance) {
        dataBackup.createSingularBackup(instance);
    }

    @Override
    public void createBackup() {
        dataBackup.createBackup();
    }
}
