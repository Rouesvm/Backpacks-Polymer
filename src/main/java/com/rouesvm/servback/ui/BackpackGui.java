package com.rouesvm.servback.ui;

import com.rouesvm.servback.slots.BackpackSlot;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class BackpackGui extends SimpleGui {
    protected final ItemStack stack;
    protected final SimpleInventory inventory;

    public BackpackGui(ServerPlayerEntity player, ItemStack stack, SimpleInventory inventory) {
        super(getHandler(inventory.size()), player, false);

        this.stack = stack;
        this.inventory = inventory;

        this.setTitle(Text.of("Backpack"));
        this.fillChest();

        this.open();
        this.afterOpened();
    }

    public void afterOpened() {
        this.getPlayer().currentScreenHandler.addListener(new ScreenHandlerListener() {
            @Override
            public void onSlotUpdate(ScreenHandler handler, int slotId, ItemStack stackSlot) {
                ContainerComponent newContents = ContainerComponent.fromStacks(inventory.getHeldStacks());
                stack.set(DataComponentTypes.CONTAINER, newContents);
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

    public static ScreenHandlerType<?> getHandler(int slots) {
        return switch (slots/9) {
            case 1 -> ScreenHandlerType.GENERIC_9X1;
            case 2 -> ScreenHandlerType.GENERIC_9X2;
            case 3 -> ScreenHandlerType.GENERIC_9X3;
            case 4 -> ScreenHandlerType.GENERIC_9X4;
            case 5 -> ScreenHandlerType.GENERIC_9X5;
            case 6 -> ScreenHandlerType.GENERIC_9X6;
            default -> null;
        };
    }

    public void fillChest() {
        for (int i = 0; i < this.inventory.size(); i++)
            this.setSlotRedirect(i, new BackpackSlot(this.inventory, i, i,0));
    }
}
