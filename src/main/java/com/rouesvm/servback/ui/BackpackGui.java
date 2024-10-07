package com.rouesvm.servback.ui;

import com.rouesvm.servback.Main;
import com.rouesvm.servback.slots.BackpackSlot;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
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

        stack.set(DataComponentTypes.REPAIR_COST, 1);

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

    public static void saveItemStack(ItemStack stack, SimpleInventory inventory) {
        stack.set(DataComponentTypes.CONTAINER, ContainerComponent.fromStacks(inventory.heldStacks));
    }

    public void fillChest() {
        for (int i = 0; i < inventory.size(); i++)
            setSlotRedirect(i, new BackpackSlot(this.inventory, i, i,0));
    }

    @Override
    public void onTick() {
        if (stack.isEmpty())
            close(false);

        super.onTick();
    }

    @Override
    public void close(boolean screenHandlerIsClosed) {
        saveItemStack(this.stack, this.inventory);
        stack.set(Main.BOOLEAN_TYPE, true);

        super.close(screenHandlerIsClosed);
    }
}
