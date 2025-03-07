package com.rouesvm.servback.ui;

import com.rouesvm.servback.Main;
import com.rouesvm.servback.components.BackpacksDataComponentTypes;
import com.rouesvm.servback.items.ContainerItem;
import com.rouesvm.servback.slots.BackpackSlot;
import com.rouesvm.servback.slots.DisabledSlot;
import com.rouesvm.servback.utils.BackpackInstance;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;

import java.util.UUID;

public class BackpackGui extends SimpleGui {
    protected final ItemStack stack;

    protected final BackpackInstance backpackInstance;

    protected final UUID uuid;

    public BackpackGui(ServerPlayerEntity player, ItemStack stack, int slots) {
        super(getHandler(slots), player, false);

        stack.set(BackpacksDataComponentTypes.BOOLEAN_TYPE, true);

        this.uuid = Main.backpackManager.getStackUUID(stack);
        this.stack = stack;

        this.backpackInstance = Main.backpackManager.getInstance(uuid, slots);
        this.backpackInstance.setLastAccessed();

        if (this.backpackInstance.backpackInventory.isEmpty() && stack.get(DataComponentTypes.CONTAINER) != null && stack.getItem() instanceof ContainerItem item) {
            DefaultedList<ItemStack> itemStacks = item.getComponentItemList(stack);
            if (backpackInstance.backpackInventory.insertItems(itemStacks)) {
                backpackInstance.backpackInventory.setInventoryDirectly(backpackInstance.backpackInventory.getHeldStacks());
                Main.backpackManager.saveBackpack(uuid, backpackInstance.backpackInventory);
            }
            stack.set(DataComponentTypes.CONTAINER, null);
        }

        this.setTitle(Text.translatable("item.serverbackpacks.gui_backpack"));

        this.fillChest();

        this.open();
        this.afterOpened();
    }

    public void afterOpened() {
        final int slots = backpackInstance.backpackInventory.size();
        for(int j = 0; j <= 3; ++j) {
            for(int k = 0; k < 9; ++k) {
                final int index;
                if (j == 0) index = k + (9 * 4 + slots) - 9;
                else index = slots + (k + j * 9) - 9;
                this.screenHandler.setSlot(index, new DisabledSlot(stack, player.getInventory(), k + j * 9, k + j * 9, 0));
            }
        }

        this.getPlayer().currentScreenHandler.addListener(new ScreenHandlerListener() {
            @Override
            public void onSlotUpdate(ScreenHandler handler, int slotId, ItemStack stackSlot) {
                Main.backpackManager.saveBackpack(backpackInstance);
            }
            @Override
            public void onPropertyUpdate(ScreenHandler handler, int property, int value) {

            }
        });
    }

    @Override
    public ItemStack quickMove(int index) {
        if (this.screenHandler.getSlot(index).getStack() == stack) return ItemStack.EMPTY;
        return super.quickMove(index);
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
        for (int j = 0; j < this.backpackInstance.backpackInventory.size(); ++j)
            this.setSlotRedirect(j, new BackpackSlot(this.backpackInstance.backpackInventory, j, j,0));
    }
}