package com.rouesvm.servback.content.upgrade;

import net.minecraft.nbt.NbtCompound;

public interface PersistentUpgrade {
    void readView(NbtCompound data);
    void writeView(NbtCompound data);
}
