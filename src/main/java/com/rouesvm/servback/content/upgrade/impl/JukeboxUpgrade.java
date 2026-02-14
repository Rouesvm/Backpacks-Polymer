package com.rouesvm.servback.content.upgrade.impl;

import com.rouesvm.servback.content.upgrade.ClickableUpgrade;
import com.rouesvm.servback.content.upgrade.Upgrade;
import com.rouesvm.servback.registry.BackpackUpgradeRegistry;
import com.rouesvm.servback.technical.ui.VirtualCraftingScreenHandler;
import com.rouesvm.servback.technical.ui.inventory.BackpackInventory;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class JukeboxUpgrade extends Upgrade implements ClickableUpgrade {
    public JukeboxUpgrade() {
        super(BackpackUpgradeRegistry.JUKEBOX);
    }

    @Override
    public boolean onClicked(ServerPlayer serverPlayer, ItemStack stack, Slot slot, ClickAction clickType, boolean inContainer) {
        return openGui(serverPlayer, clickType == ClickAction.SECONDARY, inContainer);
    }

    @Override
    public void tick(ServerPlayer player, BackpackInventory inventory) {
    }

    public boolean openGui(ServerPlayer serverPlayer, boolean isRight, boolean inContainer) {

        if (isRight != inContainer) {
            serverPlayer.openMenu(new SimpleMenuProvider((syncId, inventory, player) ->
                    new VirtualCraftingScreenHandler(syncId, inventory),
                    Component.translatable("container.crafting")
            ));
            return true;
        }

        return false;
    }
}
