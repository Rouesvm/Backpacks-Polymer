package com.rouesvm.servback.content.upgrade.impl;

import com.rouesvm.servback.content.upgrade.BaseUpgrade;
import com.rouesvm.servback.technical.ui.inventory.BackpackInventory;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.StackWithSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.Box;

import java.util.ArrayList;
import java.util.List;

public class MagnetUpgrade extends BaseUpgrade {
    public List<Item> list = new ArrayList<>();

    public MagnetUpgrade() {
        super("magnet");
    }

    @Override
    public void readView(ReadView data) {
        for(StackWithSlot stackWithSlot : data.getTypedListView("Items", StackWithSlot.CODEC)) {
            if (stackWithSlot.isValidSlot(list.size())) {
                list.set(stackWithSlot.slot(), stackWithSlot.stack().getItem());
            }
        }
    }

    @Override
    public void writeView(WriteView data) {
        WriteView.ListAppender<StackWithSlot> listAppender = data.getListAppender("Items", StackWithSlot.CODEC);

        for(int i = 0; i < list.size(); ++i) {
            ItemStack itemStack = list.get(i).getDefaultStack();
            if (!itemStack.isEmpty()) {
                listAppender.add(new StackWithSlot(i, itemStack));
            }
        }

        if (listAppender.isEmpty()) {
            data.remove("Items");
        }
    }

    @Override
    public void tick(ServerPlayerEntity player, BackpackInventory inventory) {
        ServerWorld world = player.getWorld();
        Box area = new Box(player.getPos().add(-2), player.getPos().add(2));

        List<ItemEntity> items = world.getEntitiesByClass(ItemEntity.class, area,
                item -> item.isAlive() && (!world.isClient || item.timeUntilRegen > 1) && !item.cannotPickup());

        if (!items.isEmpty()) {
            items.forEach(itemEntity -> {
                ItemStack stack = itemEntity.getStack();

                inventory.addStack(stack);
                itemEntity.setDespawnImmediately();
                itemEntity.setPickupDelayInfinite();
            });
        }
    }
}
