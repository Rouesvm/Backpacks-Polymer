package com.rouesvm.servback.technical.ui;

import com.rouesvm.servback.ServerBackpacks;
import com.rouesvm.servback.registry.BackpackDataComponentTypes;
import com.rouesvm.servback.technical.ui.slots.NonBackpackSlot;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.network.HashedStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

public class BasicInventoryGui extends SimpleGui {
    private static final String BEDROCK_ROW_MARKER = "chest.row.";

    private static final int EXISTING_SMALL_CHEST_SIZE = 3;
    private static final int EXISTING_LARGE_CHEST_SIZE = 6;

    protected final ItemStack stack;
    protected final Container inventory;

    protected int stackIndex = -1;
    protected boolean outOfSlot = false;

    private final int slots;
    public final int containerSize;

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

        int rows = (int) Math.ceil(slots / 9.0);
        if (ServerBackpacks.isBedrock(player.getUUID()) && (
                rows != EXISTING_SMALL_CHEST_SIZE
                && rows != EXISTING_LARGE_CHEST_SIZE
        )) title = title.copy().append(BEDROCK_ROW_MARKER + rows);

        this.containerSize = rows * 9;

        this.setTitle(Component.translationArg(title));

        this.fillSlots();
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
            public void slotChanged(@NonNull AbstractContainerMenu handler, int slotId, @NonNull ItemStack stackSlot) {
                slotUpdate();
                if (stack != null && ItemStack.isSameItemSameComponents(stackSlot, stack)) outOfSlot = true;
            }
            @Override
            public void dataChanged(@NonNull AbstractContainerMenu handler, int property, int value) {

            }
        });
    }

    @Override
    public void onManualClose() {
        onClose();
    }

    @Override
    public void onPlayerClose(boolean success) {
        if (success) onClose();
    }

    public void onClose() {
        player.inventoryMenu.setRemoteCarried(HashedStack.create(Items.DIRT.getDefaultInstance(), Objects::hashCode));
    }

        @Override
    public void onTick() {
        if (outOfSlot) this.close();
    }

    public static MenuType<?> getHandler(int slots) {
        int rows = (int) Math.ceil(slots / 9.0);

        return switch (rows) {
            case 1 -> MenuType.GENERIC_9x1;
            case 2 -> MenuType.GENERIC_9x2;
            case 3 -> MenuType.GENERIC_9x3;
            case 4 -> MenuType.GENERIC_9x4;
            case 5 -> MenuType.GENERIC_9x5;
            default -> MenuType.GENERIC_9x6;
        };
    }

    public void lockSlot() {
        for(int y = 0; y <= 3; ++y) {
            for(int x = 0; x < 9; ++x) {
                final int index = y == 0 ? x + (9 * 4 + this.slots) - 9 : this.slots + (x + y * 9) - 9 ;
                if (ItemStack.isSameItemSameComponents(this.inventory.getItem(index), this.stack)) {
                    this.stackIndex = index;
                    break;
                }
            }
        }
    }

    public void fillSlots() {
        for (int slot = 0; slot < containerSize; slot++) {
            this.setSlot(slot, new NonBackpackSlot(this.inventory, slot, slot, 0));
        }
    }
}
