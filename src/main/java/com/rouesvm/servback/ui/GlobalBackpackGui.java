package com.rouesvm.servback.ui;

import com.rouesvm.servback.slots.NonBackpackSlot;
import com.rouesvm.servback.utils.BaseInventory;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import com.rouesvm.servback.Main;

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
        this.getPlayer().currentScreenHandler.addListener(new ScreenHandlerListener() {
            @Override
            public void onSlotUpdate(ScreenHandler handler, int slotId, ItemStack stackSlot) {
                Main.backpackManager.setGlobalInventory(inventory.heldStacks);
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