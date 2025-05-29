package com.rouesvm.servback.state;

import com.mojang.serialization.Codec;
import com.rouesvm.servback.state.codecs.BackpackData;
import com.rouesvm.servback.state.codecs.InventoryData;
import com.rouesvm.servback.state.codecs.SlotData;
import com.rouesvm.servback.ui.inventory.BackpackInventory;
import com.rouesvm.servback.utils.BackpackInstance;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class BackpackDataSaver {
    private static Path savePath;
    private static List<BackpackData> storedInventories = new ArrayList<>();

    private static final Codec<List<BackpackData>> SAVE_CODEC = BackpackData.CODEC.listOf().fieldOf("backpackContents").codec();

    public static void onServerStarting(MinecraftServer server) {
        var path = server.getSavePath(WorldSavePath.ROOT).resolve("data/serverbackpacks.data");
        savePath = path;

        if (Files.exists(path)) {
            try {
                var data = SAVE_CODEC.decode(server.getRegistryManager().getOps(NbtOps.INSTANCE), NbtIo.read(path));
                data.result().ifPresentOrElse(result ->
                        storedInventories = result.getFirst(),
                        () -> storedInventories = new ArrayList<>()
                );

            } catch (Throwable e) {
                e.printStackTrace();
            }
        } else {
            save(server);
        }
    }

    public static void save(MinecraftServer server) {
        var data = SAVE_CODEC.encodeStart(server.getRegistryManager().getOps(NbtOps.INSTANCE), List.copyOf(storedInventories));
        if (data.isSuccess()) {
            try {
                Files.writeString(savePath, data.result().get().toString());
            } catch (IOException e) {
                e.printStackTrace();
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
                    new InventoryData(SlotData.writeToCodec(instance.getHeldInventory()))
            );
            storedInventories.add(data);
        });
    }
}
