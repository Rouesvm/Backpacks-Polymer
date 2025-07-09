package com.rouesvm.servback.technical.ui;

import com.rouesvm.servback.registry.BackpackDataComponentTypes;
import com.rouesvm.servback.technical.BackpackUtils;
import com.rouesvm.servback.technical.data.BackpackDataSaver;
import com.rouesvm.servback.technical.data.BackpackInstance;
import com.rouesvm.servback.technical.data.BackpackManager;
import com.rouesvm.servback.technical.ui.slots.BackpackSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;

public class BackpackGui extends BasicInventoryGui {
    private boolean markDirty = false;
    protected final BackpackInstance instance;
    protected BackpackInstance frozenInstance;

    public BackpackGui(ServerPlayerEntity player, ItemStack stack, BackpackInstance instance) {
        super(player, stack, instance.inventory());

        this.frozenInstance = instance.copy();

        this.instance = instance;
        this.instance.setLastAccessed();

        if (stack != null) BackpackUtils.convertComponentToBackpackData(instance, stack);
    }

    public BackpackGui(ServerPlayerEntity player, BackpackInstance instance) {
        super(player, null, instance.inventory());

        this.frozenInstance = instance.copy();

        this.instance = instance;
        this.instance.setLastAccessed();
    }

    @Override
    public void slotUpdate() {
        BackpackManager.saveBackpack(instance);
        markDirty = true;
    }

    @Override
    public void onClose() {
        BackpackManager.save(this.getPlayer().getServer());

        if (markDirty) {
            String before = BackpackUtils.hashBackpackContents(frozenInstance.heldInventory());
            String after = BackpackUtils.hashBackpackContents(instance.heldInventory());

            if (!before.equals(after)) BackpackDataSaver.createBackup(player.getServer());
        }

        if (stack != null) stack.set(BackpackDataComponentTypes.IS_OPENED, false);

        ScreenHandler handler = getPlayer().currentScreenHandler;
        handler.enableSyncing();
        handler.sendContentUpdates();
    }

    @Override
    public void fillChest() {
        for (int i = 0; i < this.slots(); i++)
            this.setSlotRedirect(i, new BackpackSlot(this.inventory, i, i, 0));
    }

}