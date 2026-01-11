package com.rouesvm.servback.technical.ui;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;

public class VirtualCraftingScreenHandler extends CraftingMenu {
    private final TransientCraftingContainer craftingInventory = new TransientCraftingContainer(this, 3, 3);
    private final ResultContainer resultInventory = new ResultContainer();

    public VirtualCraftingScreenHandler(int syncId, Inventory playerInventory) {
        super(syncId, playerInventory);

        this.slots.clear();
        this.addSlot(new ResultSlot(this.owner(), craftingInventory, resultInventory, 0, 124, 35));

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                this.addSlot(new Slot(craftingInventory, j + i * 3, 30 + j * 18, 17 + i * 18));
            }
        }

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 142));
        }
    }

    @Override
    public void slotsChanged(Container inventory) {
        CraftingMenu.slotChangedCraftingGrid(
                this,
                (ServerLevel) this.owner().level(),
                this.owner(),
                craftingInventory, resultInventory,
                null
        );
    }

    @Override
    public void removed(Player player) {
        AbstractContainerMenu handler = this.owner().containerMenu;

        handler.resumeRemoteUpdates();
        handler.broadcastChanges();

        super.removed(player);
        this.clearContainer(player, craftingInventory);
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
