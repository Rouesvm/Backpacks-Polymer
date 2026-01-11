package com.rouesvm.servback.technical.ui;

import com.rouesvm.servback.ServerBackpacks;
import com.rouesvm.servback.registry.BackpackDataComponentTypes;
import com.rouesvm.servback.technical.ui.slots.NonBackpackSlot;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

public class BasicInventoryGui extends SimpleGui {
    private static final String BEDROCK_ROW_MARKER = "chest.row.";

    private static final int EXISTING_SMALL_CHEST_SIZE = 9*3;
    private static final int EXISTING_LARGE_CHEST_SIZE = 9*6;

    protected final ItemStack stack;
    protected final Container inventory;

    protected int stackIndex = -1;
    protected boolean outOfSlot = false;

    private final int slots;

    public BasicInventoryGui(ServerPlayer player, ItemStack stack, Container inventory) {
        super(getHandler(inventory.getContainerSize()), player, false);

        this.stack = stack;
        this.inventory = inventory;

        this.slots = inventory.getContainerSize();

        Component title = Component.translatable("item.serverbackpacks.gui_backpack");

        if (this.stack != null) {
            stack.set(BackpackDataComponentTypes.IS_OPENED, true);
            title = title.copy()
                    .append(" (")
                    .append(stack.getHoverName())
                    .append(")");
        }

        if (ServerBackpacks.isBedrock(player) && (
                this.slots != EXISTING_SMALL_CHEST_SIZE
                && this.slots != EXISTING_LARGE_CHEST_SIZE
        )) title = title.copy().append(BEDROCK_ROW_MARKER + this.slots/9);

        this.setTitle(Component.translationArg(title));

        this.fillChest();
        this.open();

        this.afterOpened();
    }

    public int slots() {
        return slots;
    }

    public void slotUpdate() {}

    public void afterOpened() {
        if (stack != null) this.lockSlot();

        this.getPlayer().containerMenu.addSlotListener(new ContainerListener() {
            @Override
            public void slotChanged(AbstractContainerMenu handler, int slotId, ItemStack stackSlot) {
                slotUpdate();
                if (stackIndex != -1 && stack != null && handler.getSlot(stackIndex).getItem() != stack) outOfSlot = true;
            }
            @Override
            public void dataChanged(AbstractContainerMenu handler, int property, int value) {

            }
        });
    }

    public void lockSlot() {
        for(int j = 0; j <= 3; ++j) {
            for(int k = 0; k < 9; ++k) {
                final int index = j == 0 ? k + (9 * 4 + this.slots) - 9 : this.slots + (k + j * 9) - 9 ;
                if (ItemStack.isSameItemSameComponents(this.screenHandler.getSlot(index).getItem(), this.stack)) {
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

    public static MenuType<?> getHandler(int slots) {
        return switch (slots/9) {
            case 1 -> MenuType.GENERIC_9x1;
            case 2 -> MenuType.GENERIC_9x2;
            case 3 -> MenuType.GENERIC_9x3;
            case 4 -> MenuType.GENERIC_9x4;
            case 5 -> MenuType.GENERIC_9x5;
            default -> MenuType.GENERIC_9x6;
        };
    }

    public void fillChest() {
        for (int i = 0; i < this.slots; i++)
            this.setSlotRedirect(i, new NonBackpackSlot(this.inventory, i, i, 0));
    }
}
