package com.rouesvm.servback.ui;

import com.rouesvm.servback.Main;
import com.rouesvm.servback.ui.slots.DisabledSlot;
import com.rouesvm.servback.ui.slots.NonBackpackSlot;
import com.rouesvm.servback.utils.BackpackManager;
import com.rouesvm.servback.ui.inventory.BaseInventory;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class GlobalBackpackGui extends SimpleGui {
    protected final ItemStack stack;
    protected final BaseInventory inventory;

    public GlobalBackpackGui(ServerPlayerEntity player, ItemStack stack) {
        super(ScreenHandlerType.GENERIC_9X3, player, false);

        this.stack = stack;
        this.inventory = Main.getInventory();

        this.setTitle(Text.translatable("item.serverbackpacks.gui_global"));
        this.fillChest();

        this.open();
        this.afterOpened();
    }

    public void afterOpened() {
        for(int j = 0; j <= 3; ++j) {
            for(int k = 0; k < 9; ++k) {
                final int index;
                int slots = 9 * 3;
                if (j == 0) index = k + (9 * 4 + slots) - 9;
                else index = slots + (k + j * 9) - 9;
                this.screenHandler.setSlot(index, new DisabledSlot(stack, player.getInventory(), k + j * 9, k + j * 9, 0));
            }
        }

        this.getPlayer().currentScreenHandler.addListener(new ScreenHandlerListener() {
            @Override
            public void onSlotUpdate(ScreenHandler handler, int slotId, ItemStack stackSlot) {
                BackpackManager.setGlobalInventory(inventory.heldStacks);
            }
            @Override
            public void onPropertyUpdate(ScreenHandler handler, int property, int value) {

            }
        });
    }

    @Override
    public void onTick() {
        if (this.stack.isEmpty())
            this.close();
    }

    public void fillChest() {
        for (int i = 0; i < 27; i++)
            this.setSlotRedirect(i, new NonBackpackSlot(this.inventory, i, i, 0));
    }
}