package com.rouesvm.servback.technical.cosmetic;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

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

    public static void initialize() {
        manager = new CosmeticManager();
    }

    public static void destroy() {
        if (manager != null) {
            manager.storedInstances.forEach((uuid, backHolder) -> backHolder.destroy());
            manager.storedInstances.clear();
            manager.storedInstances = new HashMap<>();
            manager = null;
        }
    }

    public boolean hasInstance(ServerPlayer player) {
        return manager.storedInstances.get(player.getUUID()) != null;
    }

    public Optional<BackHolder> getInstance(ServerPlayer player) {
        return Optional.ofNullable(manager.storedInstances.getOrDefault(player.getUUID(), null));
    }

    public BackHolder getOrCreateInstance(ServerPlayer player, ItemStack stack) {
        Optional<BackHolder> holder = getInstance(player);
        if (holder.isEmpty()) {
            holder = Optional.of(BackHolder.createDisplay(stack, player));
            manager.storedInstances.put(player.getUUID(), holder.get());
        }

        return holder.get();
    }

    public void removeInstance(ServerPlayer player) {
        getInstance(player).ifPresent(h -> manager.storedInstances.remove(player.getUUID()));
    }
}
