package com.rouesvm.servback.technical.data;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.rouesvm.servback.ServerBackpacks;
import com.rouesvm.servback.technical.BackpackUtils;
import com.rouesvm.servback.technical.config.Configuration;
import com.rouesvm.servback.technical.data.codecs.BackpackInstanceData;
import com.rouesvm.servback.technical.data.codecs.InventoryData;
import com.rouesvm.servback.technical.manager.BackpackManager;
import com.rouesvm.servback.technical.ui.inventory.BackpackInventory;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    private static Path singularBackupDir;
    private static Path fullBackupDir;

    private static Path saveDir;

    private static final Map<UUID, String> lastSingularHashes = new HashMap<>();
    private static final Map<UUID, String> lastFullHashes = new HashMap<>();

    private static Set<BackpackInstance> storedInventories = new HashSet<>();

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy-HH-mm-ss");

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

    public static void createBackupDirs(MinecraftServer server) {
        Path backupDir = server.getSavePath(WorldSavePath.ROOT).resolve("data/backpacks-backups");

        try {
            Files.createDirectories(backupDir);
        } catch (IOException e) {
            ServerBackpacks.LOGGER.error("Failed to create backup directory", e);
        }

        singularBackupDir = backupDir.resolve("singular");
        fullBackupDir = backupDir.resolve("full");

        try {
            Files.createDirectories(singularBackupDir);
            Files.createDirectories(fullBackupDir);
        } catch (IOException e) {
            ServerBackpacks.LOGGER.error("Failed to create singular/full backup directories", e);
        }
    }

    public static boolean loadData(MinecraftServer server) {
        saveDir = server.getSavePath(WorldSavePath.ROOT).resolve("data/backpacks");

        try {
            Files.createDirectories(saveDir);
        } catch (IOException e) {
            ServerBackpacks.LOGGER.error("Failed to create instance directory", e);
            return false;
        }

        if (Files.exists(saveDir)) {
            List<BackpackInstance> instances = loadExistingData(saveDir, server);
            if (instances != null && !instances.isEmpty()) storedInventories.addAll(instances);
            return instances != null && !instances.isEmpty();
        } else save(server);

        return false;
    }

    private static Optional<BackpackInstance> loadSingle(Path saveDir, MinecraftServer server, UUID uuid) {
        if (saveDir == null) return Optional.empty();

        Path file = saveDir.resolve(uuid.toString() + ".dat");
        if (!Files.exists(file)) return Optional.empty();

        try (DataInputStream dis = new DataInputStream(Files.newInputStream(file))) {
            NbtCompound nbt = NbtIo.readCompressed(dis, NbtSizeTracker.ofUnlimitedBytes());

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
            } catch (IOException ignored) {

            }
        }
    }

    public static void saveSingle(MinecraftServer server, BackpackInstance instance) {
        saveSingle(saveDir, server, instance);
    }

    private static List<BackpackInstance> loadExistingData(Path saveDir, MinecraftServer server) {
        try {
            try (Stream<Path> paths = Files.walk(saveDir)) {
                List<UUID> uuids = new ArrayList<>();
                paths.filter(path -> path.toString().endsWith(".dat")).forEach(path ->
                                uuids.add(UUID.fromString(path.getFileName().toString().replace(".dat", ""))));

                if (uuids.isEmpty()) return null;
                List<BackpackInstance> instances = new ArrayList<>(uuids.size());

                for (UUID uuid : uuids) {
                    Optional<BackpackInstance> instance = loadSingle(saveDir, server, uuid);
                    instance.ifPresent(instances::add);
                }

                return instances;
            }
        } catch (IOException e) {
            ServerBackpacks.LOGGER.error("Failed to load Server Backpack's instance", e);
        }

        return null;
    }

    public static void save(MinecraftServer server) {
        final Set<BackpackInstance> instances = storedInventories;
        executor.execute(() -> {
            for (BackpackInstance instance : instances) {
                saveSingle(server, instance);
            }

            ServerBackpacks.LOGGER.info("Saving data for Server Backpacks.");
        });
    }

    private static Path createTimestampedDir(Path baseDir) {
        String formattedTime = LocalDateTime.now().format(formatter);
        return baseDir.resolve(formattedTime);
    }

    private static void saveBackup(Path dir, MinecraftServer server, BackpackInstance instance, Map<UUID, String> lastHashes) {
        UUID uuid = instance.uuid();
        String hash = BackpackUtils.hashBackpackContents(instance.heldInventory());

        if (!hash.equals(lastHashes.get(uuid))) {
            saveSingle(dir, server, instance);
            lastHashes.put(uuid, hash);
        }
    }

    public static void createSingularBackup(MinecraftServer server, BackpackInstance instance) {
        if (singularBackupDir == null || !Configuration.instance().allow_backups) return;

        storedInventories.remove(instance);
        storedInventories.add(instance);

        executor.execute(() -> saveBackup(createTimestampedDir(singularBackupDir), server, instance, lastSingularHashes));
    }

    public static void createBackup(MinecraftServer server) {
        if (fullBackupDir == null || !Configuration.instance().allow_backups) return;

        setStoredInventories(BackpackManager.instance().backpackInstances());

        final Set<BackpackInstance> instances = storedInventories;
        executor.execute(() -> {
            Path currentDir = createTimestampedDir(fullBackupDir);
            for (BackpackInstance instance : instances) saveBackup(currentDir, server, instance, lastFullHashes);
        });
    }

    public static BackpackInstanceData turnInstanceToData(BackpackInstance instance) {
        return new BackpackInstanceData(
                instance.uuid(),
                InventoryData.stacksListToData(instance.heldInventory()),
                Optional.of(instance.lastAccessed()),
                Optional.of(instance.size())
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
        return storedInventories;
    }

    public static void setStoredInventory(BackpackInstance backpackInstance) {
        storedInventories.remove(backpackInstance);
        storedInventories.add(backpackInstance);
    }

    public static void setStoredInventories(Set<BackpackInstance> backpackInstances) {
        storedInventories = new HashSet<>();
        storedInventories.addAll(backpackInstances);
    }
}
