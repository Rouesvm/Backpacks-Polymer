package com.rouesvm.servback.technical.data;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.rouesvm.servback.ServerBackpacks;
import com.rouesvm.servback.technical.data.codecs.BackpackInstanceData;
import com.rouesvm.servback.technical.data.codecs.InventoryData;
import com.rouesvm.servback.technical.manager.BackpackManager;
import com.rouesvm.servback.technical.ui.inventory.BackpackInventory;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
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

public class BackpackData {
    private static final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "ServerBackpacks-Data");
        t.setDaemon(true);
        return t;
    });

    private static Path saveDir;

    private static final Map<UUID, BackpackInstance> loadedBackpacks = Object2ObjectMaps.synchronize(new Object2ObjectOpenHashMap<>());
    private static final Set<UUID> discoveredBackpackUUIDs = ObjectSets.synchronize(new ObjectOpenHashSet<>());

    public static void shutdownThread() {
        executor.shutdown();

        try {
            if (executor.awaitTermination(30, TimeUnit.SECONDS)) {
                ServerBackpacks.LOGGER.info("Thread stopped.");
            }
        } catch (InterruptedException e) {
            ServerBackpacks.LOGGER.error("Error while stopping thread {}", e.getMessage());
        }
    }

    public static boolean loadData(MinecraftServer server, boolean hasLoaded) {
        if (!hasLoaded) {
            hasLoaded = BackpackData.loadData(server);

            if (hasLoaded && !discoveredBackpackUUIDs.isEmpty()) {
                BackpackManager.instance().loadDiscoveredBackpackUUIDs(discoveredBackpackUUIDs);
                return true;
            }
        }
        return false;
    }


    private static boolean loadData(MinecraftServer server) {
        saveDir = server.getSavePath(WorldSavePath.ROOT).resolve("data/backpacks");

        try {
            Files.createDirectories(saveDir);

            try (Stream<Path> paths = Files.list(saveDir)) {
                paths.filter(p -> p.toString().endsWith(".dat"))
                        .forEach(p -> {
                            String filename = p.getFileName().toString().replace(".dat", "");
                            discoveredBackpackUUIDs.add(UUID.fromString(filename));
                        });
            }

            return !discoveredBackpackUUIDs.isEmpty();
        } catch (IOException e) {
            ServerBackpacks.LOGGER.error("Failed to check backpack directory", e);
            return false;
        }
    }

    public static Optional<BackpackInstance> getOrLoadBackpack(UUID uuid, MinecraftServer server) {
        BackpackInstance cached = loadedBackpacks.get(uuid);
        if (cached != null) {
            return Optional.of(cached);
        }

        Optional<BackpackInstance> loaded = loadSingle(saveDir, server, uuid);
        if (loaded.isPresent()) {
            BackpackInstance instance = loaded.get();
            loadedBackpacks.put(uuid, instance);
            return Optional.of(instance);
        }

        return Optional.empty();
    }

    private static Optional<BackpackInstance> loadSingle(Path saveDir, MinecraftServer server, UUID uuid) {
        if (saveDir == null) return Optional.empty();

        Path file = saveDir.resolve(uuid.toString() + ".dat");
        if (!Files.exists(file)) return Optional.empty();

        try (DataInputStream dis = new DataInputStream(Files.newInputStream(file))) {
            NbtCompound nbt = NbtIo.readCompressed(dis, NbtSizeTracker.ofUnlimitedBytes());
            BackpackDataDFU.applyDataFixToItemStacks(server, nbt, SharedConstants.getGameVersion().dataVersion().id());

            DataResult<Pair<BackpackInstanceData, NbtElement>> data =
                    BackpackInstanceData.CODEC.decode(server.getRegistryManager().getOps(NbtOps.INSTANCE), nbt);
            return data.result()
                    .map(pair -> turnDataToInstance(pair.getFirst()));
        } catch (IOException | NbtCrashException e) {
            ServerBackpacks.LOGGER.error("Failed to load single backpack {}", uuid, e);
            return Optional.empty();
        }
    }

    public static void saveSingle(Path saveDir, MinecraftServer server, BackpackInstance instance) {
        if (!Files.exists(saveDir)) {
            try {
                Files.createDirectories(saveDir);
            } catch (IOException e) {
                ServerBackpacks.LOGGER.error("Error while creating directory {}", e.getMessage());
            }
        }

        Path targetFile = saveDir.resolve(instance.uuid() + ".dat");
        Path tempFile = saveDir.resolve(instance.uuid() + ".dat.tmp");

        try {
            DataResult<NbtElement> data = BackpackInstanceData.CODEC.encodeStart(
                    server.getRegistryManager().getOps(NbtOps.INSTANCE),
                    turnInstanceToData(instance)
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
            ServerBackpacks.LOGGER.error("Failed to save backpack {}: {}", instance.uuid(), e.getMessage());
            try {
                Files.deleteIfExists(tempFile);
            } catch (IOException cleanupError) {
                ServerBackpacks.LOGGER.warn("Failed to cleanup temp file: {}",
                        tempFile, cleanupError);
            }
        }
    }

    public static void saveSingle(MinecraftServer server, BackpackInstance instance) {
        discoveredBackpackUUIDs.add(instance.uuid());
        saveSingle(saveDir, server, instance);
    }

    public static void save(MinecraftServer server) {
        final List<BackpackInstance> finalStoredInventories = List.copyOf(loadedBackpacks.values());
        executor.execute(() -> {
            for (BackpackInstance instance : finalStoredInventories) {
                saveSingle(server, instance);
            }

            ServerBackpacks.LOGGER.info("Saving data for Server Backpacks.");
        });
    }

    public static BackpackInstanceData turnInstanceToData(BackpackInstance instance) {
        return new BackpackInstanceData(
                instance.uuid(),
                InventoryData.stacksListToData(instance.heldInventory()),
                Optional.of(instance.lastAccessed()),
                Optional.of(instance.size()),
                Optional.of(SharedConstants.getGameVersion().dataVersion().id())
        );
    }

    public static BackpackInstance turnDataToInstance(BackpackInstanceData instance) {
        long lastAccessed = instance.last_accessed().orElse(System.currentTimeMillis());
        int size = instance.size().orElse(9 * 6);

        return new BackpackInstance(
                instance.uuid(),
                new BackpackInventory(
                        InventoryData.getHeldStacks(
                                instance.getInventoryData().itemStacks(),
                                size
                        )
                ),
                lastAccessed
        );
    }

    public static Set<BackpackInstance> getBackpackInstances() {
        return new HashSet<>(loadedBackpacks.values());
    }

    public static void replaceStoredInventory(BackpackInstance backpackInstance) {
        loadedBackpacks.put(backpackInstance.uuid(), backpackInstance);
    }

    public static void replaceStoredInventories(Set<BackpackInstance> backpackInstances) {
        loadedBackpacks.clear();
        for (BackpackInstance instance : backpackInstances) {
            loadedBackpacks.put(instance.uuid(), instance);
        }
    }
}
