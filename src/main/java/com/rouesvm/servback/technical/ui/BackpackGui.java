package com.rouesvm.servback.technical.ui;

import com.rouesvm.servback.content.item.impl.ContainerItem;
import com.rouesvm.servback.registry.BackpackDataComponentTypes;
import com.rouesvm.servback.technical.BackpackUtils;
import com.rouesvm.servback.technical.data.BackpackDataSaver;
import com.rouesvm.servback.technical.data.BackpackInstance;
import com.rouesvm.servback.technical.data.BackpackManager;
import com.rouesvm.servback.technical.ui.slots.BackpackSlot;
import eu.pb4.sgui.api.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;

public class BackpackGui extends BasicInventoryGui {
    private boolean markDirty = false;
    protected final BackpackInstance instance;
    protected final BackpackInstance frozenInstance;

    public BackpackGui(ServerPlayerEntity player, ItemStack stack, BackpackInstance instance) {
        super(player, stack, instance.inventory());

        this.frozenInstance = instance.copy();

        this.instance = instance;
        this.instance.setLastAccessed();

        if (stack != null) BackpackUtils.convertComponentToBackpackData(instance, stack);
    }

    public BackpackGui(ServerPlayerEntity player, BackpackInstance instance) {
        this(player, null, instance);
    }

    @Override
    public boolean onAnyClick(int index, ClickType type, SlotActionType action) {
        if (index < 0) return true;

        Slot slot = this.screenHandler.getSlot(index);
        if (slot.hasStack()
                && slot.getStack().getItem() instanceof ContainerItem
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
        BackpackManager.saveToBackpackInventory(instance);
        markDirty = true;
    }

    @Override
    public void onClose() {
        if (stack != null) stack.set(BackpackDataComponentTypes.IS_OPENED, false);

        ScreenHandler handler = this.getPlayer().currentScreenHandler;

        if (markDirty) {
            String before = BackpackUtils.hashBackpackContents(frozenInstance.heldInventory());
            String after = BackpackUtils.hashBackpackContents(instance.heldInventory());

            if (!before.equals(after)) {
                BackpackDataSaver.createBackup(player.getServer());
            }

            BackpackManager.save(this.getPlayer().getServer());
        }

        handler.enableSyncing();
        handler.sendContentUpdates();
    }

    @Override
    public void fillChest() {
        for (int i = 0; i < this.slots(); i++)
            this.setSlotRedirect(i, new BackpackSlot(this.inventory, i, i, 0));
    }

}