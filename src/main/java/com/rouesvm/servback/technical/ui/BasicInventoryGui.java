package com.rouesvm.servback.technical.ui;

import com.rouesvm.servback.ServerBackpacks;
import com.rouesvm.servback.registry.BackpackDataComponentTypes;
import com.rouesvm.servback.technical.ui.slots.NonBackpackSlot;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class BasicInventoryGui extends SimpleGui {
    protected final ItemStack stack;
    protected final Inventory inventory;

    protected int stackIndex = -1;
    protected boolean outOfSlot = false;

    private final int slots;

    public BasicInventoryGui(ServerPlayerEntity player, ItemStack stack, Inventory inventory) {
        super(getHandler(inventory.size()), player, false);

        this.stack = stack;
        this.inventory = inventory;

        this.slots = inventory.size();

        Text title = Text.translatable("item.serverbackpacks.gui_backpack");

        if (this.stack != null) {
            stack.set(BackpackDataComponentTypes.IS_OPENED, true);
            title = title.copy()
                    .append(" (")
                    .append(stack.getName())
                    .append(")");
        }

        if (ServerBackpacks.BEDROCK_PLAYERS.contains(player) && (this.slots != 9*3 && this.slots != 9*6)) {
            title = title.copy().append("chest.row." + this.slots/9);
        }

        this.setTitle(Text.of(title));

        this.fillChest();
        this.open();

        this.afterOpened();
    }

    public int slots() {
        return slots;
    }

    public void slotUpdate() {
    }

    public void afterOpened() {
        if (stack != null) this.lockSlot();

        this.getPlayer().currentScreenHandler.addListener(new ScreenHandlerListener() {
            @Override
            public void onSlotUpdate(ScreenHandler handler, int slotId, ItemStack stackSlot) {
                slotUpdate();
                if (stackIndex != -1 && stack != null && handler.getSlot(stackIndex).getStack() != stack) outOfSlot = true;
            }
            @Override
            public void onPropertyUpdate(ScreenHandler handler, int property, int value) {

            }
        });
    }

    public void lockSlot() {
        for(int j = 0; j <= 3; ++j) {
            for(int k = 0; k < 9; ++k) {
                final int index = j == 0 ? k + (9 * 4 + this.slots) - 9 : this.slots + (k + j * 9) - 9 ;
                if (ItemStack.areItemsAndComponentsEqual(this.screenHandler.getSlot(index).getStack(), this.stack)) {
                    this.stackIndex = index;
                    break;
                }
            }
        }
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
        for (int i = 0; i < this.slots; i++)
            this.setSlotRedirect(i, new NonBackpackSlot(this.inventory, i, i, 0));
    }
}
