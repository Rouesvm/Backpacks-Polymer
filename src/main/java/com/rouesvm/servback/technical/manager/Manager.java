package com.rouesvm.servback.technical.manager;

import com.rouesvm.servback.technical.data.BackpackInstance;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryOps;
import net.minecraft.server.MinecraftServer;

import java.util.Set;
import java.util.UUID;

public interface Manager {
    MinecraftServer server();
    RegistryOps<NbtElement> nbtOps();

    Set<BackpackInstance> getBackpackInstances();
    Set<UUID> getBackpackUUIDs();

}
