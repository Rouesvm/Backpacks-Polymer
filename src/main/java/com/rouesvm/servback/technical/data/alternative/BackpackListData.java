package com.rouesvm.servback.technical.data.alternative;

import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.rouesvm.servback.ServerBackpacks;
import com.rouesvm.servback.technical.data.BackpackData;
import com.rouesvm.servback.technical.data.BackpackInstance;
import com.rouesvm.servback.technical.data.codecs.BackpackInstanceData;
import com.rouesvm.servback.technical.manager.BackpackManager;
import net.minecraft.SharedConstants;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class BackpackListData {
    private static List<BackpackInstanceData> storedInventories = new ArrayList<>();
    private static final Codec<List<BackpackInstanceData>> SAVE_CODEC = BackpackInstanceData.CODEC.listOf().fieldOf("backpackContents").codec();

    private static Path saveDir;

    public static Set<BackpackInstance> getBackpackInstances() {
        return storedInventories.stream()
                .map(BackpackData::turnDataToInstance)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public static boolean loadData(MinecraftServer server, boolean hasLoaded) {
        if (!hasLoaded) {
            saveDir = server.getSavePath(WorldSavePath.ROOT).resolve("data/serverbackpacks.data");

            try {
                hasLoaded = loadExistingData(server);
            } catch (IOException e) {
                ServerBackpacks.LOGGER.error("Error while loading list data {}", e.getMessage());
            }

            if (hasLoaded) {
                Set<BackpackInstance> dataInstances = BackpackListData.getBackpackInstances();
                ServerBackpacks.LOGGER.info("Successfully loaded list data!");
                if (!dataInstances.isEmpty()) {
                    BackpackManager.instance().loadIntoStoredInstances(dataInstances);
                    return true;
                }
            }
        }

        return false;
    }

    private static void applyFixToNestedItemStacks(MinecraftServer server, NbtCompound root, int oldVersion, int newVersion) {
        DataFixer fixer = server.getDataFixer();

        Optional<NbtList> backpacksOptional = root.getList("backpackContents");
        if (backpacksOptional.isEmpty()) return;

        NbtList backpacks = backpacksOptional.get();
        for (int i = 0; i < backpacks.size(); ++i) {
            Optional<NbtCompound> backpackEntry = backpacks.getCompound(i);
            if (backpackEntry.isEmpty()) continue;

            Optional<NbtCompound> contents = backpackEntry.get().getCompound("contents");
            if (contents.isEmpty()) continue;

            Optional<NbtList> items = contents.get().getList("Items");
            if (items.isEmpty()) continue;

            NbtList itemList = items.get();
            for (int j = 0; j < itemList.size(); ++j) {
                Optional<NbtCompound> slotCompound = itemList.getCompound(j);
                if (slotCompound.isEmpty()) continue;

                NbtCompound slot = slotCompound.get();

                Optional<NbtCompound> wrapped = slot.getCompound("itemStacks");
                if (wrapped.isEmpty()) continue;

                Dynamic<NbtElement> inputDynamic = new Dynamic<>(NbtOps.INSTANCE, wrapped.get());
                Dynamic<NbtElement> outputDynamic = fixer.update(
                        TypeReferences.ITEM_STACK, inputDynamic,
                        oldVersion, newVersion
                );

                NbtCompound fixed = (NbtCompound) outputDynamic.getValue();
                slot.put("itemStacks", fixed);
            }
        }
    }

    private static boolean loadExistingData(MinecraftServer server) throws IOException {
        try (DataInputStream dis = new DataInputStream(Files.newInputStream(saveDir))) {
            NbtCompound compound = NbtIo.readCompound(dis);

            int newDataVersion = SharedConstants.getGameVersion().dataVersion().id();
            int oldDataVersion = 4440;

            applyFixToNestedItemStacks(server, compound, oldDataVersion, newDataVersion);

            var dataResult = SAVE_CODEC.decode(BackpackManager.nbtOps, compound);

            dataResult.error().ifPresent(err -> {
                ServerBackpacks.LOGGER.error("SAVE_CODEC.decode failed: {}", err.message());
                err.error().ifPresent(e -> ServerBackpacks.LOGGER.error("Decode exception: {}", e.message()));
            });

            var result = dataResult.result();
            if (result.isPresent()) {
                var pair = result.get();
                storedInventories = pair.getFirst();
            } else storedInventories = new ArrayList<>();

            return !storedInventories.isEmpty();
        }
    }
}
