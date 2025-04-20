package com.rouesvm.servback.ui;

import com.rouesvm.servback.items.ContainerItem;
import com.rouesvm.servback.registry.BackpackDataComponentTypes;
import com.rouesvm.servback.ui.slots.BackpackSlot;
import com.rouesvm.servback.utils.BackpackInstance;
import com.rouesvm.servback.utils.BackpackManager;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;

public class BackpackGui extends SimpleGui {
    protected final ItemStack stack;
    protected final BackpackInstance backpackInstance;

    protected int stackIndex;
    protected boolean outOfSlot = false;

    private int size;

    public BackpackGui(ServerPlayerEntity player, ItemStack stack, BackpackInstance instance) {
        super(getHandler(instance.getInventory().size()), player, false);

        stack.set(BackpackDataComponentTypes.BOOLEAN_TYPE, true);

        this.stack = stack;

        this.backpackInstance = instance;
        this.backpackInstance.setLastAccessed();

        this.size = this.backpackInstance.getInventory().size();
        if (this.size > (9*6)) this.size = 9 * 6;

        convertComponentToBackpackData();

        this.setTitle(Text.translatable("item.serverbackpacks.gui_backpack"));

        this.fillChest();

        this.open();
        this.afterOpened();
    }

    private void convertComponentToBackpackData() {
        if (backpackInstance.getInventory().isEmpty() && stack.get(DataComponentTypes.CONTAINER) != null && stack.getItem() instanceof ContainerItem item) {
            DefaultedList<ItemStack> itemStacks = item.getComponentItemList(stack);
            if (backpackInstance.getInventory().insertItems(itemStacks)) {
                backpackInstance.getInventory().setInventoryDirectly(backpackInstance.getInventory().getHeldStacks());
                BackpackManager.getManager().saveBackpack(backpackInstance.getUuid(), backpackInstance.getInventory());
            }
            stack.set(DataComponentTypes.CONTAINER, null);
        }
    }

    public void afterOpened() {
        for (int k = 0; k < 9; ++k) {
            int index = k + (9 * 4 + this.size) - 9;
            if (this.screenHandler.getSlot(index).getStack().equals(this.stack)) {
                this.stackIndex = index;
                break;
            }
        }

        this.getPlayer().currentScreenHandler.addListener(new ScreenHandlerListener() {
            @Override
            public void onSlotUpdate(ScreenHandler handler, int slotId, ItemStack stackSlot) {
                if (handler.getSlot(stackIndex).getStack() != stack) outOfSlot = true;
                BackpackManager.getManager().saveBackpack(backpackInstance);
            }
            @Override
            public void onPropertyUpdate(ScreenHandler handler, int property, int value) {

            }
        });
    }

    @Override
    public void onClose() {
        BackpackManager.getManager().save(this.getPlayer().getServer());
        stack.set(BackpackDataComponentTypes.BOOLEAN_TYPE, false);
    }

    @Override
    public void onTick() {
        if (outOfSlot) this.close();
    }

    public static ScreenHandlerType<?> getHandler(int slots) {
        return switch (slots/9) {
            case 1 -> ScreenHandlerType.GENERIC_9X1;
            case 2 -> ScreenHandlerType.GENERIC_9X2;
            case 3 -> ScreenHandlerType.GENERIC_9X3;
            case 4 -> ScreenHandlerType.GENERIC_9X4;
            case 5 -> ScreenHandlerType.GENERIC_9X5;
            default -> ScreenHandlerType.GENERIC_9X6;
        };
    }

    public void fillChest() {
        for (int j = 0; j < size; ++j)
            this.setSlotRedirect(j, new BackpackSlot(backpackInstance.getInventory(), j, j,0));
    }
}