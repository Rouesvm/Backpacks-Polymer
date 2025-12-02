package com.rouesvm.servback.technical.data.types.file;

import com.rouesvm.servback.ServerBackpacks;
import com.rouesvm.servback.technical.BackpackUtils;
import com.rouesvm.servback.technical.config.Configuration;
import com.rouesvm.servback.technical.data.BackpackInstance;
import com.rouesvm.servback.technical.data.types.Data;
import com.rouesvm.servback.technical.manager.Manager;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class BackpackDataBackup {
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "ServerBackpacks-DataBackup");
        t.setDaemon(true);
        return t;
    });

    public void shutdownThread() {
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

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy-HH-mm-ss");

    private Path singularBackupDir;
    private final Map<UUID, Path> backupDirsUUID = new HashMap<>();

    private Path fullBackupDir;

    private final Map<UUID, String> lastSingularHashes = new Object2ObjectOpenHashMap<>();
    private final Map<UUID, String> lastFullHashes = new Object2ObjectOpenHashMap<>();

    private final Manager manager;
    private final Data data;

    public BackpackDataBackup(Manager manager, Data data) {
        this.data = data;
        this.manager = manager;

        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdownThread));
        createBackupDirs();
    }

    public void createBackupDirs() {
        Path backupDir = manager.server().getSavePath(WorldSavePath.ROOT).resolve("data/backpacks-backups");

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
            singularBackupDir = null;
            fullBackupDir = null;
        }
    }

    private Path createTimestampedDir(Path baseDir) {
        String formattedTime = LocalDateTime.now().format(formatter);
        return baseDir.resolve(formattedTime);
    }

    private void saveBackup(Path dir, MinecraftServer server, BackpackInstance instance, Map<UUID, String> lastHashes) {
        UUID uuid = instance.uuid();
        String hash = BackpackUtils.hashBackpackContents(instance.heldInventory());

        if (!hash.equals(lastHashes.get(uuid))) {
            data.saveSingleToDisk(instance, dir);
            lastHashes.put(uuid, hash);
        }
    }

    public void createSingularBackup(MinecraftServer server, BackpackInstance instance) {
        if (singularBackupDir == null || !Configuration.instance().allow_backups) return;
        backupDirsUUID.computeIfAbsent(instance.uuid(), k -> singularBackupDir.resolve(instance.uuid().toString()));

        Path backupDir = backupDirsUUID.get(instance.uuid());
        if (!Files.exists(backupDir)) {
            try {
                Files.createDirectories(backupDir);
            } catch (IOException e) {
                ServerBackpacks.LOGGER.error("Failed to create uuid's backup directory", e);
                backupDir = null;
                backupDirsUUID.remove(instance.uuid());
            }
        }

        if (backupDir != null) {
            Path finalBackupDir = backupDir;
            executor.execute(() -> saveBackup(createTimestampedDir(finalBackupDir), server, instance, lastSingularHashes));
        }
    }

    public void createBackup(MinecraftServer server) {
        if (fullBackupDir == null || !Configuration.instance().allow_backups) return;
        final List<BackpackInstance> finalStoredInventories = data.getBackpackInstances().stream()
                .filter(Objects::nonNull)
                .toList();

        executor.execute(() -> {
            Path currentDir = createTimestampedDir(fullBackupDir);
            for (BackpackInstance instance : finalStoredInventories) saveBackup(currentDir, server, instance, lastFullHashes);
        });
    }
}
