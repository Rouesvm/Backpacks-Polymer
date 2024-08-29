package com.rouesvm.servback.ui;

import com.rouesvm.servback.slots.DisabledSlot;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import com.rouesvm.servback.slots.BackpackSlot;

public class BackpackGui extends SimpleGui {
    private final ItemStack stack;
    private final Inventory inventory;

    public BackpackGui(ServerPlayerEntity player, ItemStack stack, int slots) {
        super(getHandler(slots), player, false);

        this.stack = stack;
        this.inventory = new SimpleInventory(readItemStack(this.stack, slots).toArray(ItemStack[]::new));

        setTitle(Text.of("Backpack"));
        fillChest();
        open();
    }

    public static ScreenHandlerType<?> getHandler(int slots) {
        return switch (slots/9) {
            case 1 -> ScreenHandlerType.GENERIC_9X1;
            case 2 -> ScreenHandlerType.GENERIC_9X2;
            case 3 -> ScreenHandlerType.GENERIC_9X3;
            default -> null;
        };
    }


    public DefaultedList<ItemStack> readItemStack(ItemStack stack, int slots) {
        ContainerComponent component = stack.getOrDefault(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT);

        DefaultedList<ItemStack> list = DefaultedList.ofSize(slots, ItemStack.EMPTY);
        component.copyTo(list);
        return list;
    }

    public void saveItemStack() {
        DefaultedList<ItemStack> storedItems = DefaultedList.ofSize(this.inventory.size(), ItemStack.EMPTY);

        for (int i = 0; i < storedItems.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            storedItems.set(i, stack);
        }

        this.stack.set(DataComponentTypes.CONTAINER, ContainerComponent.fromStacks(storedItems));
    }

    public void fillChest() {
        for (int i = 0; i < this.inventory.size(); i++)
            setSlotRedirect(i, new BackpackSlot(this.inventory, i, i,0));
    }

    @Override
    public void onTick() {
        saveItemStack();

        if (this.stack.isEmpty()) {
            close(false);
        }

        super.onTick();
    }

    @Override
    public void onClose() {
        saveItemStack();
    }
}
