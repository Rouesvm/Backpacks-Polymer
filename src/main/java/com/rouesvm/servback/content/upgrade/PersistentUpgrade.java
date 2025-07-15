package com.rouesvm.servback.content.upgrade;

import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;

public interface PersistentUpgrade {
    void readView(ReadView data);
    void writeView(WriteView data);
}
