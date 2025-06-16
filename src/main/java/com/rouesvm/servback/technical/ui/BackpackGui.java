package com.rouesvm.servback.technical.ui;

import com.rouesvm.servback.content.registry.BackpackDataComponentTypes;
import com.rouesvm.servback.technical.data.BackpackInstance;
import com.rouesvm.servback.technical.data.BackpackManager;
import com.rouesvm.servback.technical.data.BackpackUtils;
import com.rouesvm.servback.technical.ui.slots.BackpackSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;

public class BackpackGui extends BasicGui {
    protected final BackpackInstance instance;

    public BackpackGui(ServerPlayerEntity player, ItemStack stack, BackpackInstance instance) {
        super(player, stack, instance.inventory());

        this.instance = instance;
        this.instance.setLastAccessed();

        if (stack != null) BackpackUtils.convertComponentToBackpackData(instance, stack);
    }

    public BackpackGui(ServerPlayerEntity player, BackpackInstance instance) {
        super(player, null, instance.inventory());

        this.instance = instance;
        this.instance.setLastAccessed();
    }

    @Override
    public void slotUpdate() {
        BackpackManager.saveBackpack(instance);
    }

    @Override
    public void onClose() {
        BackpackManager.save(this.getPlayer().getServer());

        if (stack != null) stack.set(BackpackDataComponentTypes.BOOLEAN_TYPE, false);

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