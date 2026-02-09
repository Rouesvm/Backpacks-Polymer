package com.rouesvm.servback.technical.ui;

import com.rouesvm.servback.content.item.impl.ContainerItem;
import com.rouesvm.servback.registry.BackpackDataComponentTypes;
import com.rouesvm.servback.technical.BackpackUtils;
import com.rouesvm.servback.technical.data.BackpackInstance;
import com.rouesvm.servback.technical.manager.BackpackManager;
import com.rouesvm.servback.technical.ui.slots.BackpackSlot;
import eu.pb4.sgui.api.ClickType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class BackpackGui extends BasicInventoryGui {
    private boolean markDirty = false;
    protected final BackpackInstance instance;
    protected final BackpackInstance frozenInstance;

    public BackpackGui(ServerPlayer player, ItemStack stack, BackpackInstance instance) {
        super(player, stack, instance.inventory());

        this.frozenInstance = instance.copy();
        this.instance = instance;

        if (stack != null) BackpackUtils.convertComponentToBackpackData(instance, stack);
    }

    public BackpackGui(ServerPlayer player, BackpackInstance instance) {
        this(player, null, instance);
    }

    @Override
    public boolean onAnyClick(int index, ClickType type, net.minecraft.world.inventory.ClickType action) {
        if (index < 0) return true;

        Slot slot = this.screenHandler.getSlot(index);
        if (slot.hasItem()
                && slot.getItem().getItem() instanceof ContainerItem
                && stack.get(BackpackDataComponentTypes.UPGRADE_CONTAINER) != null
                && type.shift
                && type.isRight
        ) {
            new UpgradeContainerGui(this.getPlayer(), stack);
            return false;
        }

        return true;
    }

    @Override
    public void slotUpdate() {
        BackpackManager.writeChangesToInventory(instance);
        markDirty = true;
    }

    @Override
    public void onClose() {
        if (stack != null) stack.set(BackpackDataComponentTypes.IS_OPENED, false);

        AbstractContainerMenu handler = this.getPlayer().containerMenu;

        handler.resumeRemoteUpdates();
        handler.broadcastChanges();

        if (markDirty) {
            String before = BackpackUtils.hashBackpackContents(frozenInstance.heldInventory());
            String after = BackpackUtils.hashBackpackContents(instance.heldInventory());

            if (!before.equals(after)) {
                BackpackManager.createSingularBackupAndSave(instance);
            }
        }
    }

    @Override
    public void fillSlots() {
        int slots = inventory.getContainerSize();
        int rows = (int) Math.ceil(slots / 9.0);

        int amountToPad = (int) Math.ceil((double) slots / rows);
        for (int row = 0; row < rows; row++) {
            int startIndex = row * 9;
            int endIndex = startIndex + 9;

            int rowPadding = 9 - amountToPad;

            int leftPadding = rowPadding / 2;
            int rightPadding = rowPadding - leftPadding;

            for (int i = startIndex + leftPadding; i < endIndex - rightPadding; i++) {
                int inventoryIndex = (row * amountToPad) + (i - (startIndex + leftPadding));
                this.setSlotRedirect(i, new BackpackSlot(this.inventory, inventoryIndex, i, 0));
            }
        }
    }
}