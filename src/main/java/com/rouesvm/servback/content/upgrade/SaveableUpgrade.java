package com.rouesvm.servback.content.upgrade;

import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;

public interface SaveableUpgrade {
    default void readView(ReadView data) {}
    default void writeView(WriteView data) {}
}
