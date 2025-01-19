package com.rouesvm.servback.ui;

import com.rouesvm.servback.utils.BaseInventory;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Optional;

public class FilterMenu extends SimpleGui {
    private final BaseInventory inventory;
    private final ItemStack stack;

    public FilterMenu(ItemStack stack, ServerPlayerEntity player) {
        super(ScreenHandlerType.HOPPER, player, false);

        BaseInventory fakeInventory = new BaseInventory(5);

        ContainerComponent containerComponent = stack.getOrDefault(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT);
        containerComponent.copyTo(fakeInventory.heldStacks);

        this.inventory = fakeInventory;

        this.stack = stack;

        int k;
        int j;
        for (j = 0; j < this.inventory.size(); ++j)
            this.setSlotRedirect(j, new FilteredSlot(this.inventory, j, j, 0));

        open();

        for(j = 0; j <= 3; ++j) {
            for(k = 0; k < 9; ++k) {
                int index;
                if (j == 0) index = k + 41 - 9;
                else index = 5 + (k + j * 9) - 9;

                this.screenHandler.setSlot(index, new DisabledSlot(stack, player.getInventory(), k + j * 9, k + j * 9, 0));
            }
        }

        this.setTitle(Text.translatable("ui.extralent.filter"));
    }

    @Override
    public ItemStack quickMove(int index) {
        return super.quickMove(index);
    }

    @Override
    public void onClose() {
        stack.set(DataComponentTypes.CONTAINER, ContainerComponent.fromStacks(this.inventory.heldStacks));
    }

    private static class DisabledSlot extends Slot {
        private final ItemStack stack;

        public DisabledSlot(ItemStack stack, Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
            this.stack = stack;
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return !stack.equals(this.stack);
        }

        @Override
        public boolean canTakeItems(PlayerEntity playerEntity) {
            return !this.getStack().equals(this.stack);
        }
    }

    private static class FilteredSlot extends Slot {
        public FilteredSlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public void setStack(ItemStack stack) {
            if (stack.isEmpty()) {
                super.setStack(ItemStack.EMPTY);
            } else {
                ItemStack copy = stack.copy();
                copy.setCount(1);
                super.setStack(copy);
            }
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return false;
        }

        @Override
        public ItemStack insertStack(ItemStack stack, int count) {
            ItemStack toInsert = stack.copy();
            toInsert.setCount(count);
            this.setStack(toInsert);
            return stack;
        }

        @Override
        public void onTakeItem(PlayerEntity player, ItemStack stack) {
            this.setStack(ItemStack.EMPTY);
        }

        @Override
        public Optional<ItemStack> tryTakeStackRange(int min, int max, PlayerEntity player) {
            return Optional.of(ItemStack.EMPTY);
        }

        @Override
        public ItemStack takeStackRange(int min, int max, PlayerEntity player) {
            return ItemStack.EMPTY;
        }

        @Override
        public boolean canTakePartial(PlayerEntity player) {
            return false;
        }

        @Override
        public boolean canTakeItems(PlayerEntity player) {
            return true;
        }

        @Override
        public ItemStack takeStack(int amount) {
            this.setStack(ItemStack.EMPTY);
            return ItemStack.EMPTY;
        }

        @Override
        protected void onTake(int amount) {
            this.setStack(ItemStack.EMPTY);
        }
    }
}
