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

import java.io.*;
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

    private static final Codec<List<BackpackData>> SAVE_CODEC = BackpackData.CODEC.listOf().fieldOf("backpackContents").codec();

    public static boolean onServerStarting(MinecraftServer server) {
        savePath = server.getSavePath(WorldSavePath.ROOT).resolve("data/serverbackpacks.data");

        if (Files.exists(savePath)) {
            try {
                var data = SAVE_CODEC.decode(NbtOps.INSTANCE, NbtIo.readCompound(new DataInputStream(
                        new FileInputStream(savePath.toFile()))));
                data.result().ifPresentOrElse(result ->
                        storedInventories = result.getFirst(),
                        () -> storedInventories = new ArrayList<>()
                );

                if (!storedInventories.isEmpty()) return true;
            } catch (Throwable e) {
               ServerBackpacks.LOGGER.error("Failed to load Server Backpack's data.");
            }
        } else {
            save();
        }

        setupBackup(server);

        return false;
    }

    public static void setupBackup(MinecraftServer server) {
        backupPath = server.getSavePath(WorldSavePath.ROOT).resolve("data/backpacks-backups");

        if (!Files.exists(backupPath)) {
            try {
                Files.createDirectories(backupPath);
            } catch (IOException e) {
                ServerBackpacks.LOGGER.error("Failed to create backup directory.");
            }
        }

        createBackup();
    }

    public static void save() {
        if (savePath == null) return;

        var data = SAVE_CODEC.encodeStart(NbtOps.INSTANCE, List.copyOf(storedInventories));
        if (data.isSuccess()) {
            try {
                NbtIo.write(data.result().get(), new DataOutputStream(new FileOutputStream(savePath.toFile())));
            } catch (IOException e) {
                ServerBackpacks.LOGGER.error("Failed to save Server Backpack's data.");
            }
        }
    }

    public static void createBackup() {
        if (backupPath == null) return;
        if (!Configuration.instance().allow_backups) return;

        LocalDateTime deathTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy-HH-mm-ss");
        String formattedDeathTime = deathTime.format(formatter);

        Path backupFile = backupPath.resolve("serverbackpacks-backup-" + formattedDeathTime + ".data");

        var data = SAVE_CODEC.encodeStart(NbtOps.INSTANCE, List.copyOf(storedInventories));
        if (data.isSuccess()) {
            try {
                NbtIo.write(data.result().get(), new DataOutputStream(new FileOutputStream(backupFile.toFile())));
            } catch (IOException e) {
                ServerBackpacks.LOGGER.error("Failed to backup Server Backpack's data.");
            }
        }
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
