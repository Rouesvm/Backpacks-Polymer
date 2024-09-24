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

        setTitle(Text.of("Backpack"));
        fillChest();
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

    public void saveItemStack() {
        DefaultedList<ItemStack> storedItems = DefaultedList.ofSize(inventory.size(), ItemStack.EMPTY);

        for (int i = 0; i < storedItems.size(); i++)
            storedItems.set(i, inventory.getStack(i));

        stack.set(DataComponentTypes.CONTAINER, ContainerComponent.fromStacks(storedItems));
    }

    public void fillChest() {
        for (int i = 0; i < inventory.size(); i++)
            setSlotRedirect(i, new BackpackSlot(this.inventory, i, i,0));
    }

    @Override
    public void onTick() {
        saveItemStack();

        if (stack.isEmpty())
            close(false);

        super.onTick();
    }

    @Override
    public void onClose() {
        saveItemStack();
    }
}
