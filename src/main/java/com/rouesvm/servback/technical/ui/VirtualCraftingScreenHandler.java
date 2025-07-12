package com.rouesvm.servback.technical.ui;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.slot.CraftingResultSlot;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.world.ServerWorld;

public class VirtualCraftingScreenHandler extends CraftingScreenHandler {
    private final CraftingInventory craftingInventory = new CraftingInventory(this, 3, 3);
    private final CraftingResultInventory resultInventory = new CraftingResultInventory();

    public VirtualCraftingScreenHandler(int syncId, PlayerInventory playerInventory) {
        super(syncId, playerInventory);

        this.slots.clear();
        this.addSlot(new CraftingResultSlot(this.getPlayer(), craftingInventory, resultInventory, 0, 124, 35));

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
    public void onContentChanged(Inventory inventory) {
        CraftingScreenHandler.updateResult(
                this,
                (ServerWorld) this.getPlayer().getWorld(),
                this.getPlayer(),
                craftingInventory, resultInventory,
                null
        );
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        this.dropInventory(player, craftingInventory);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }
}
