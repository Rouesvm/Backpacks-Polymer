package com.rouesvm.servback.technical.ui;

import com.rouesvm.servback.content.item.impl.LavaContainerItem;
import com.rouesvm.servback.technical.data.BackpackInstance;
import com.rouesvm.servback.technical.ui.inventory.BackpackInventory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class LavaBackpackGui extends BackpackGui {
    private boolean isClearing = false;

    public LavaBackpackGui(ServerPlayer player, ItemStack stack, BackpackInstance instance) {
        super(player, stack, instance);
    }

    @Override
    public void slotUpdate() {
        super.slotUpdate();
        if (!isClearing && inventory instanceof BackpackInventory backpackInventory) {
            isClearing = true;
            if (LavaContainerItem.clearOldItems(backpackInventory)) {
                LavaContainerItem.playBurnSound(this.getPlayer());
            }
            isClearing = false;
        }
    }
}
