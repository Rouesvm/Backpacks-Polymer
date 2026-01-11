package com.rouesvm.servback.technical.manager;

import com.rouesvm.servback.technical.data.BackpackInstance;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.MinecraftServer;

import java.util.Set;
import java.util.UUID;

public interface Manager {
    MinecraftServer server();
    RegistryOps<Tag> nbtOps();

    Set<BackpackInstance> getBackpackInstances();
    Set<UUID> getBackpackUUIDs();

}
