package com.rouesvm.servback.technical.cosmetic;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class CosmeticManager {
    private static CosmeticManager manager = null;
    public Map<UUID, BackHolder> storedInstances = new Object2ObjectOpenHashMap<>();

    private CosmeticManager() {}

    public static CosmeticManager manager() {
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

    public boolean hasInstance(ServerPlayerEntity player) {
        return manager.storedInstances.get(player.getUuid()) != null;
    }

    public Optional<BackHolder> getInstance(ServerPlayerEntity player) {
        return Optional.ofNullable(manager.storedInstances.getOrDefault(player.getUuid(), null));
    }

    public BackHolder getOrCreateInstance(ServerPlayerEntity player, ItemStack stack) {
        Optional<BackHolder> holder = getInstance(player);
        if (holder.isEmpty()) {
            holder = Optional.of(BackHolder.createDisplay(stack, player));
            manager.storedInstances.put(player.getUuid(), holder.get());
        }

        return holder.get();
    }

    public void removeInstance(ServerPlayerEntity player) {
        getInstance(player).ifPresent(h -> manager.storedInstances.remove(player.getUuid()));
    }
}
