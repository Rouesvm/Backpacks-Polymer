package com.rouesvm.servback.content.upgrade.impl;

import com.rouesvm.servback.content.upgrade.ClickableUpgrade;
import com.rouesvm.servback.content.upgrade.Upgrade;
import com.rouesvm.servback.registry.BackpackUpgradeRegistry;
import com.rouesvm.servback.technical.ui.VirtualCraftingScreenHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ClickType;

public class CraftingUpgrade extends Upgrade implements ClickableUpgrade {
    public CraftingUpgrade() {
        super(BackpackUpgradeRegistry.CRAFTING);
    }

    @Override
    public boolean onClicked(ServerPlayerEntity serverPlayer, ItemStack stack, Slot slot, ClickType clickType, boolean inContainer) {
        return openGui(serverPlayer, clickType == ClickType.RIGHT, inContainer);
    }

    public boolean openGui(ServerPlayerEntity serverPlayer, boolean isRight, boolean inContainer) {
        if (isRight != inContainer) {
            serverPlayer.openHandledScreen(new SimpleNamedScreenHandlerFactory((syncId, inventory, player) ->
                    new VirtualCraftingScreenHandler(syncId, inventory),
                    Text.translatable("container.crafting")
            ));
            return true;
        }

        return false;
    }
}
