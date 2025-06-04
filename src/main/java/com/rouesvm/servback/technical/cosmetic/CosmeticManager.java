package com.rouesvm.servback.technical.cosmetic;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CosmeticManager {
    private static CosmeticManager manager = null;
    public Map<UUID, BackHolder> storedInstances = new Object2ObjectOpenHashMap<>();

    private CosmeticManager() {}

    public static CosmeticManager getManager() {
        return manager;
    }

    public static void setup() {
        manager = new CosmeticManager();
    }

    public static void destroy() {
        if (manager != null) {
            manager.storedInstances.forEach((uuid, backHolder) -> backHolder.destroy());
            manager.storedInstances = new HashMap<>();
            manager = null;
        }
    }

    public BackHolder getInstance(ServerPlayerEntity player) {
        return manager.storedInstances.getOrDefault(player.getUuid(), null);
    }

    public BackHolder getOrCreateInstance(ServerPlayerEntity player, ItemStack stack) {
        UUID uuid = player.getUuid();
        if (getInstance(player) != null)
            return getInstance(player);

        manager.storedInstances.put(uuid,
                BackHolder.createDisplay(
                        stack,
                        player
                )
        );

        return getInstance(player);
    }

    public void removeInstance(ServerPlayerEntity player) {
        BackHolder holder = getInstance(player);
        if (holder != null) {
            manager.storedInstances.remove(player.getUuid());
        }
    }
}
