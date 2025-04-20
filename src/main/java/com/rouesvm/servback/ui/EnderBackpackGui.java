package com.rouesvm.servback.ui;

import com.rouesvm.servback.ui.slots.NonBackpackSlot;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class EnderBackpackGui extends SimpleGui {
    protected final ItemStack stack;
    protected final Inventory inventory;

    protected int stackIndex;
    protected boolean outOfSlot = false;

    public EnderBackpackGui(ServerPlayerEntity player, ItemStack stack) {
        super(ScreenHandlerType.GENERIC_9X3, player, false);

        this.stack = stack;
        this.inventory = player.getEnderChestInventory();

        this.setTitle(Text.translatable("item.serverbackpacks.gui_ender"));
        this.fillChest();

        this.open();

        for (int k = 0; k < 9; ++k) {
            int index = k + (9 * 4 + 27) - 9;
            if (this.screenHandler.getSlot(index).getStack().equals(this.stack)) {
                this.stackIndex = index;
                break;
            }
        }

        this.getPlayer().currentScreenHandler.addListener(new ScreenHandlerListener() {
            @Override
            public void onSlotUpdate(ScreenHandler handler, int slotId, ItemStack stackSlot) {
                if (handler.getSlot(stackIndex).getStack() != stack) outOfSlot = true;
            }
            @Override
            public void onPropertyUpdate(ScreenHandler handler, int property, int value) {

            }
        });
    }

    @Override
    public void onTick() {
        if (outOfSlot) this.close();
    }

    public void fillChest() {
        for (int i = 0; i < 27; i++)
            this.setSlotRedirect(i, new NonBackpackSlot(this.inventory, i, i, 0));
    }
}
