package com.rouesvm.servback.technical.data.alternative;

import com.mojang.serialization.Codec;
import com.rouesvm.servback.ServerBackpacks;
import com.rouesvm.servback.technical.data.BackpackData;
import com.rouesvm.servback.technical.data.BackpackInstance;
import com.rouesvm.servback.technical.data.codecs.BackpackInstanceData;
import com.rouesvm.servback.technical.manager.BackpackManager;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class BackpackListData {
    private static List<BackpackInstanceData> storedInventories = new ArrayList<>();
    private static final Codec<List<BackpackInstanceData>> SAVE_CODEC = BackpackInstanceData.CODEC.listOf().fieldOf("backpackContents").codec();

    private static Path saveDir;

    public static Set<BackpackInstance> getBackpackInstances() {
        return storedInventories.stream()
                .map(BackpackData::turnDataToInstance)
                .collect(Collectors.toSet());
    }

    public static boolean loadData(MinecraftServer server, boolean hasLoaded) {
        if (!hasLoaded) {
            saveDir = server.getSavePath(WorldSavePath.ROOT).resolve("data/serverbackpacks.data");

            try {
                return loadExistingData(server);
            } catch (IOException e) {
                ServerBackpacks.LOGGER.error("Error while loading list data {}", e.getMessage());
            }

            Set<BackpackInstance> dataInstances = BackpackListData.getBackpackInstances();
            if (!dataInstances.isEmpty()) {
                BackpackManager.instance().loadIntoStoredInstances(dataInstances);
                return true;
            }
        }

        return false;
    }

    private static boolean loadExistingData(MinecraftServer server) throws IOException {
        try (DataInputStream dis = new DataInputStream(Files.newInputStream(saveDir))) {
            var data = SAVE_CODEC.decode(server.getRegistryManager().getOps(NbtOps.INSTANCE),
                    NbtIo.readCompound(dis));

            data.result().ifPresentOrElse(result ->
                            storedInventories = result.getFirst(),
                    () -> storedInventories = new ArrayList<>()
            );

            return !storedInventories.isEmpty();
        }
    }
}
