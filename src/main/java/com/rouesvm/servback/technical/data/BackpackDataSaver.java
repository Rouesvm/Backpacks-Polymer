package com.rouesvm.servback.technical.data;

import com.mojang.serialization.Codec;
import com.rouesvm.servback.ServerBackpacks;
import com.rouesvm.servback.technical.config.Configuration;
import com.rouesvm.servback.technical.data.state.codecs.BackpackData;
import com.rouesvm.servback.technical.data.state.codecs.InventoryData;
import com.rouesvm.servback.technical.data.state.codecs.SlotData;
import com.rouesvm.servback.technical.ui.inventory.BackpackInventory;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.Nullable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class BackpackDataSaver {
    private static Path savePath;
    private static Path backupPath;

    private static List<BackpackData> storedInventories = new ArrayList<>();

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy-HH-mm-ss");
    private static final Codec<List<BackpackData>> SAVE_CODEC = BackpackData.CODEC.listOf().fieldOf("backpackContents").codec();

    public static boolean loadData(MinecraftServer server) {
        savePath = server.getSavePath(WorldSavePath.ROOT).resolve("data/serverbackpacks.data");

        try {
            Files.createDirectories(savePath.getParent());
        } catch (IOException e) {
            ServerBackpacks.LOGGER.error("Failed to create data directory", e);
            return false;
        }

        if (Files.exists(savePath)) {
            try {
                return loadExistingData(server);
            } catch (IOException e) {
                ServerBackpacks.LOGGER.error("Failed to load Server Backpack's data", e);
            }
        } else save(server);

        return false;
    }

    private static boolean loadExistingData(MinecraftServer server) throws IOException {
        try (DataInputStream dis = new DataInputStream(Files.newInputStream(savePath))) {
            var data = SAVE_CODEC.decode(server.getRegistryManager().getOps(NbtOps.INSTANCE),
                    NbtIo.readCompound(dis));

            data.result().ifPresentOrElse(result ->
                            storedInventories = result.getFirst(),
                    () -> storedInventories = new ArrayList<>()
            );

            return !storedInventories.isEmpty();
        }
    }

    public static void save(MinecraftServer server) {
        writeToFile(savePath, server);
    }

    public static void writeToFile(@NonNull Path path, MinecraftServer server) {
        var registry = server.getRegistryManager();
        var data = SAVE_CODEC.encodeStart(registry.getOps(NbtOps.INSTANCE),
                List.copyOf(storedInventories));

        if (data.isSuccess()) {
            @Nullable Path finalPath = path;
            data.result().ifPresent(nbtElement -> {
                try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(finalPath.toFile()))) {
                    NbtIo.write(nbtElement, dos);
                } catch (IOException e) {
                    ServerBackpacks.LOGGER.error("Failed to save Server Backpack's data to {}", finalPath, e);
                }
            });
        } else ServerBackpacks.LOGGER.error("Failed to encode Server Backpack's data: {}", data.error());
    }

    public static void createBackup(MinecraftServer server) {
        if (backupPath == null) return;
        if (!Configuration.instance().allow_backups) return;

        BackpackDataSaver.setStoredInventories(BackpackManager.instance.getBackpackInstances());

        LocalDateTime currentTime = LocalDateTime.now();
        String formattedCurrentTime = currentTime.format(formatter);

        writeToFile(backupPath.resolve("serverbackpacks-backup-" + formattedCurrentTime + ".data"), server);
    }

    public static void createBackupDir(MinecraftServer server) {
        backupPath = server.getSavePath(WorldSavePath.ROOT).resolve("data/backpacks-backups");

        if (!Files.exists(backupPath)) {
            try {
                Files.createDirectories(backupPath);
            } catch (IOException e) {
                ServerBackpacks.LOGGER.error("Failed to create backup directory.", e);
            }
        }

        createBackup(server);
    }

    public static Set<BackpackInstance> getBackpackInstances() {
        return storedInventories.stream()
                .map(data -> new BackpackInstance(
                        data.getUuid(),
                        new BackpackInventory(InventoryData.getHeldStacks(data.getInventoryData().getItemStacks()))
                ))
                .collect(Collectors.toSet());
    }

    public static void setStoredInventories(Set<BackpackInstance> backpackInstances) {
        storedInventories = new ArrayList<>();
        backpackInstances.forEach(instance -> {
            BackpackData data = new BackpackData(
                    instance.getUuid(),
                    new InventoryData(SlotData.writeToCodec(instance.heldInventory()))
            );
            storedInventories.add(data);
        });
    }
}
