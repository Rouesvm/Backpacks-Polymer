package com.rouesvm.servback.ui;

import com.rouesvm.servback.slots.DisabledSlot;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import com.rouesvm.servback.Main;

public class GlobalBackpackGui extends SimpleGui {
    protected final ItemStack stack;
    protected final SimpleInventory inventory;

    public GlobalBackpackGui(ServerPlayerEntity player, ItemStack stack) {
        super(ScreenHandlerType.GENERIC_9X3, player, false);

        this.stack = stack;
        this.inventory = Main.getInventory();

        setTitle(Text.literal("Global Backpack"));
        fillChest();
    }

    @Override
    public void onTick() {
        if (stack.isEmpty())
            close(false);
        super.onTick();
    }

    @Override
    public void close(boolean screenHandlerIsClosed) {
        Main.setGlobalInventory(inventory);
        super.close(screenHandlerIsClosed);
    }

    public void fillChest() {
        for (int i = 0; i < 27; i++)
            setSlotRedirect(i, new DisabledSlot(this.inventory, i, i, 0));
    }
}