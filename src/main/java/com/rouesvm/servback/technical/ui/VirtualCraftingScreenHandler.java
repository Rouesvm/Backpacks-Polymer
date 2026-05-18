package com.rouesvm.servback.technical.ui;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.ScreenHandler;

public class VirtualCraftingScreenHandler extends CraftingScreenHandler {
    private final PlayerEntity player;

    public VirtualCraftingScreenHandler(int syncId, PlayerInventory playerInventory) {
        super(syncId, playerInventory);
        this.player = playerInventory.player;
    }

    private PlayerEntity getPlayer() {
        return this.player;
    }

    @Override
    public void onContentChanged(Inventory inventory) {
        super.onContentChanged(inventory);
    }

    @Override
    public void onClosed(PlayerEntity player) {
        ScreenHandler handler = this.getPlayer().currentScreenHandler;

        handler.enableSyncing();
        handler.sendContentUpdates();

        int i = 9;
        for (ItemStack stack : handler.getStacks()) {
            i--;
            player.getInventory().offerOrDrop(stack);
            if (i < 0) break;
        }

        super.onClosed(player);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }
}
