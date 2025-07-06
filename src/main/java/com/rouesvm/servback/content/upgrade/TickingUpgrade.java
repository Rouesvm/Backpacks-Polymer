package com.rouesvm.servback.content.upgrade;

import com.rouesvm.servback.technical.ui.inventory.BackpackInventory;
import net.minecraft.server.network.ServerPlayerEntity;

public interface TickingUpgrade {
    default void tick(ServerPlayerEntity player, BackpackInventory inventory) {};
}
