package com.rouesvm.servback.technical.ui;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.StonecutterMenu;
import org.jspecify.annotations.NonNull;

public class VirtualStonecutterScreenHandler extends StonecutterMenu {
    public VirtualStonecutterScreenHandler(int syncId, Inventory playerInventory) {
        super(syncId, playerInventory);
    }

    @Override
    public void removed(@NonNull Player player) {
        AbstractContainerMenu handler = this;

        handler.resumeRemoteUpdates();
        handler.broadcastChanges();

        super.removed(player);

        player.getInventory().add(this.getSlot(0).getItem());
    }

    @Override
    public boolean stillValid(@NonNull Player player) {
        return true;
    }

}
