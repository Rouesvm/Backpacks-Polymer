package com.rouesvm.servback.technical.ui;

import com.rouesvm.servback.content.registry.BackpackDataComponentTypes;
import com.rouesvm.servback.data.BackpackInstance;
import com.rouesvm.servback.data.BackpackManager;
import com.rouesvm.servback.data.BackpackUtils;
import com.rouesvm.servback.technical.ui.slots.BackpackSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

public class BackpackGui extends BasicGui {
    protected final BackpackInstance backpackInstance;

    public BackpackGui(ServerPlayerEntity player, ItemStack stack, BackpackInstance instance) {
        super(player, stack, instance.inventory());

        this.backpackInstance = instance;
        this.backpackInstance.setLastAccessed();

        if (stack != null) BackpackUtils.convertComponentToBackpackData(instance, stack);
    }

    public BackpackGui(ServerPlayerEntity player, BackpackInstance instance) {
        super(player, null, instance.inventory());

        this.backpackInstance = instance;
        this.backpackInstance.setLastAccessed();
    }

    @Override
    public void slotUpdate() {
        BackpackManager.saveBackpack(backpackInstance);
    }

    @Override
    public void onClose() {
        BackpackManager.save(this.getPlayer().getServer());

        if (stack != null) stack.set(BackpackDataComponentTypes.BOOLEAN_TYPE, false);

        getPlayer().currentScreenHandler.enableSyncing();
        getPlayer().currentScreenHandler.sendContentUpdates();
    }

    @Override
    public void fillChest() {
        for (int i = 0; i < this.slots(); i++)
            this.setSlotRedirect(i, new BackpackSlot(this.inventory, i, i, 0));
    }
}