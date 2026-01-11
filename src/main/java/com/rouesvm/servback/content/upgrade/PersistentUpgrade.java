package com.rouesvm.servback.content.upgrade;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public interface PersistentUpgrade {
    void readView(ValueInput data);
    void writeView(ValueOutput data);
}
