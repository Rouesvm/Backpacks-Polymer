package com.rouesvm.servback.ui;

import com.rouesvm.servback.registry.BackpackDataComponentTypes;
import com.rouesvm.servback.ui.inventory.BackpackInventory;
import com.rouesvm.servback.utils.BackpackInstance;
import com.rouesvm.servback.utils.BackpackManager;
import com.rouesvm.servback.utils.BackpackUtils;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.server.network.ServerPlayerEntity;

public class BackpackGui extends BasicGui {
    protected final BackpackInstance backpackInstance;

    protected BlockEntity entity;

    public BackpackGui(ServerPlayerEntity player, ItemStack stack, BackpackInstance instance) {
        super(player, stack, instance.getInventory());

        this.backpackInstance = instance;
        this.backpackInstance.setLastAccessed();

        if (stack != null) BackpackUtils.convertComponentToBackpackData(instance, stack);
    }

    public BackpackGui(ServerPlayerEntity player, BackpackInstance instance) {
        super(player, null, instance.getInventory());

        this.backpackInstance = instance;
        this.backpackInstance.setLastAccessed();
    }

    public BackpackGui(ServerPlayerEntity player, BlockEntity entity, BackpackInstance instance) {
        this(player, instance);

        this.entity = entity;
    }

    @Override
    public void afterOpened() {
        if (stack != null) lockSlot();

        this.getPlayer().currentScreenHandler.addListener(new ScreenHandlerListener() {
            @Override
            public void onSlotUpdate(ScreenHandler handler, int slotId, ItemStack stackSlot) {
                BackpackManager.getManager().saveBackpack(backpackInstance.getUuid(), (BackpackInventory) inventory);
                if (stack != null && handler.getSlot(stackIndex).getStack() != stack) outOfSlot = true;
            }
            @Override
            public void onPropertyUpdate(ScreenHandler handler, int property, int value) {

            }
        });
    }

    @Override
    public void onClose() {
        BackpackManager manager = BackpackManager.getManager();
        manager.saveBackpack(backpackInstance);
        manager.save(this.getPlayer().getServer());

        if (entity != null) entity.getWorld().updateComparators(entity.getPos(), entity.getCachedState().getBlock());
        if (stack != null) stack.set(BackpackDataComponentTypes.BOOLEAN_TYPE, false);

        getPlayer().currentScreenHandler.enableSyncing();
        getPlayer().currentScreenHandler.sendContentUpdates();
    }
}