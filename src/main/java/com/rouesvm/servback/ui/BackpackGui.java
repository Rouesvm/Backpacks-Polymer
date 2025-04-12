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

import java.util.List;
import java.util.UUID;

public class BackpackGui extends SimpleGui {
    protected final UUID uuid;
    protected final BackpackInstance backpackInstance;

    protected ItemStack stack;
    protected int stackIndex;
    protected boolean outOfSlot = false;

    public BackpackGui(ServerPlayerEntity player, ItemStack stack, int slots) {
        super(getHandler(slots), player, false);

        stack.set(BackpackDataComponentTypes.BOOLEAN_TYPE, true);

        this.uuid = BackpackManager.getStackUUID(stack);
        this.stack = stack;

        this.backpackInstance = BackpackManager.getInstance(uuid, slots);
        this.backpackInstance.setLastAccessed();

        convertComponentToBackpackData();

        this.setTitle(Text.translatable("item.serverbackpacks.gui_backpack"));

        this.fillChest();

        this.open();
        this.lockSlot();
        this.afterOpened();
    }

    public BackpackGui(ServerPlayerEntity player, UUID uuid, int slots) {
        super(getHandler(slots), player, false);

        this.uuid = uuid;

        this.backpackInstance = BackpackManager.getInstance(uuid, slots);
        this.backpackInstance.setLastAccessed();

        convertComponentToBackpackData();

        this.setTitle(Text.translatable("item.serverbackpacks.gui_backpack"));

        this.fillChest();

        this.open();
        this.afterOpened();
    }

    private void convertComponentToBackpackData() {
        if (this.stack == null) return;

        if (this.backpackInstance.getInventory().isEmpty() && this.stack.get(DataComponentTypes.CONTAINER) != null && this.stack.getItem() instanceof ContainerItem item) {
            DefaultedList<ItemStack> itemStacks = item.getComponentItemList(this.stack);
            if (backpackInstance.getInventory().insertItems(itemStacks)) {
                backpackInstance.getInventory().setInventoryDirectly(backpackInstance.getInventory().getHeldStacks());
                BackpackManager.getManager().saveBackpack(uuid, backpackInstance.getInventory());
            }
            this.stack.set(DataComponentTypes.CONTAINER, null);
        }
    }

    private void lockSlot() {
        final int slots = backpackInstance.getInventory().size();
        for (int k = 0; k < 9; ++k) {
            int index = k + (9 * 4 + slots) - 9;
            if (this.screenHandler.getSlot(index).getStack().equals(this.stack)) {
                this.stackIndex = index;
                break;
            }
        }
    }

    private void afterOpened() {
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
        if (this.stack != null) this.stack.set(BackpackDataComponentTypes.BOOLEAN_TYPE, false);
        else {
            UUID playerUUID = this.getPlayer().getUuid();
            UUID lastAccessedUUID = BackpackManager.getManager().getLastAccessedUUID(playerUUID);

            if (lastAccessedUUID != null && !lastAccessedUUID.equals(this.uuid)) {
                player.closeHandledScreen();
                new BackpackGui(this.player, lastAccessedUUID, this.size);
                BackpackManager.getManager().removeAccessedUUID(playerUUID, lastAccessedUUID);
            } else if (!BackpackManager.getManager().lastAccessedUUID
                    .getOrDefault(playerUUID, List.of())
                    .contains(this.uuid)) {
                BackpackManager.getManager().putAccessedUUID(playerUUID, this.uuid);
            }
        }
    }

    @Override
    public void close() {
        super.close();
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
        for (int j = 0; j < this.backpackInstance.getInventory().size(); ++j)
            this.setSlotRedirect(j, new BackpackSlot(this.backpackInstance.getInventory(), j, j,0));
    }
}