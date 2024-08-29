package com.rouesvm.servback.ui;

import com.rouesvm.servback.slots.BackpackSlot;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;

public class BackpackGui extends SimpleGui {

    protected final ItemStack stack;
    protected final Inventory inventory;

    public BackpackGui(ServerPlayerEntity player, ItemStack stack, Inventory inventory) {
        super(getHandler(inventory.size()), player, false);

        this.stack = stack;
        this.inventory = inventory;

        this.setTitle(Text.of("Backpack"));
        this.fillChest();
        this.open();
    }

    public static ScreenHandlerType<?> getHandler(int slots) {
        return switch (slots/9) {
            case 1 -> ScreenHandlerType.GENERIC_9X1;
            case 2 -> ScreenHandlerType.GENERIC_9X2;
            case 3 -> ScreenHandlerType.GENERIC_9X3;
            default -> null;
        };
    }

    public void saveItemStack() {
        DefaultedList<ItemStack> storedItems = DefaultedList.ofSize(this.inventory.size(), ItemStack.EMPTY);

        for (int i = 0; i < storedItems.size(); i++) {
            ItemStack stack = this.inventory.getStack(i);
            storedItems.set(i, stack);
        }

        this.stack.set(DataComponentTypes.CONTAINER, ContainerComponent.fromStacks(storedItems));
    }

    public void fillChest() {
        for (int i = 0; i < this.inventory.size(); i++)
            this.setSlotRedirect(i, new BackpackSlot(this.inventory, i, i,0));
    }

    @Override
    public void onTick() {
        this.saveItemStack();

        if (this.stack.isEmpty()) {
            this.close(false);
        }

        super.onTick();
    }

    @Override
    public void onClose() {
        this.saveItemStack();
    }
}
