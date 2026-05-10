package com.rouesvm.servback.technical.data.types.list;

import com.mojang.serialization.Codec;
import com.rouesvm.servback.ServerBackpacks;
import com.rouesvm.servback.technical.data.BackpackDFU;
import com.rouesvm.servback.technical.data.BackpackInstance;
import com.rouesvm.servback.technical.data.DATA_TYPE;
import com.rouesvm.servback.technical.data.codecs.BackpackInstanceData;
import com.rouesvm.servback.technical.data.types.FallbackData;
import com.rouesvm.servback.technical.manager.Manager;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class BackpackListData extends FallbackData {
    private static final Codec<List<BackpackInstanceData>> SAVE_CODEC = BackpackInstanceData.CODEC.listOf().fieldOf("backpackContents").codec();

    private Path saveDir;

    public BackpackListData(Manager manager) {
        super(manager);
    }

    public boolean initializeData(boolean hasLoaded) {
        if (!hasLoaded) {
            saveDir = manager().server().getWorldPath(LevelResource.ROOT).resolve("data/serverbackpacks.data");

            try {
                hasLoaded = loadExistingData(manager().server());
            } catch (IOException e) {
                ServerBackpacks.LOGGER.error("Error while loading list data {}", e.getMessage());
            }

            if (hasLoaded) {
                ServerBackpacks.LOGGER.info("Successfully loaded list data!");
                return true;
            }
        }

        return false;
    }

    @Override
    public DATA_TYPE getType() {
        return DATA_TYPE.LIST_FILE_DATA;
    }

    private void applyFixToNestedItemStacks(MinecraftServer server, CompoundTag root, int oldVersion, int newVersion) {
        Optional<ListTag> backpacksOptional = root.getList("backpackContents");
        if (backpacksOptional.isEmpty()) return;

        ListTag backpacks = backpacksOptional.get();
        for (int i = 0; i < backpacks.size(); ++i) {
            Optional<CompoundTag> backpackEntry = backpacks.getCompound(i);
            backpackEntry.ifPresent(nbtCompound ->
                    BackpackDFU.applyDataFixToItemStacks(server, nbtCompound, manager().nbtOps(), oldVersion, newVersion));
        }
    }

    private boolean loadExistingData(MinecraftServer server) throws IOException {
        try (DataInputStream dis = new DataInputStream(Files.newInputStream(saveDir))) {
            CompoundTag compound = NbtIo.read(dis);

            int newDataVersion = SharedConstants.getCurrentVersion().dataVersion().version();
            int oldDataVersion = 4440;

            applyFixToNestedItemStacks(server, compound, oldDataVersion, newDataVersion);

            var dataResult = SAVE_CODEC.decode(manager().nbtOps(), compound);

            dataResult.error().ifPresent(err -> {
                ServerBackpacks.LOGGER.error("SAVE_CODEC.decode failed: {}", err.message());
                err.error().ifPresent(e -> ServerBackpacks.LOGGER.error("Decode exception: {}", e.message()));
            });

            List<BackpackInstanceData> instanceData = new ArrayList<>();
            dataResult.result().ifPresent(pair -> instanceData.addAll(pair.getFirst()));

            Set<BackpackInstance> instances = new HashSet<>();
            instanceData.forEach(backpackInstanceData -> instances.add(backpackInstanceData.toInstance()));
            addBackpackInstances(instances);

            return !instances.isEmpty();
        }
    }
}
