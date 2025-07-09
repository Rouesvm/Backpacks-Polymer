package com.rouesvm.servback.content.upgrade.impl;

import com.rouesvm.servback.content.item.ContainerItem;
import com.rouesvm.servback.content.upgrade.Upgrade;
import com.rouesvm.servback.registry.BackpackUpgradeRegistry;
import com.rouesvm.servback.technical.ui.inventory.BackpackInventory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.StackWithSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static com.rouesvm.servback.registry.item.BackpackItemRegistry.MAGNET_UPGRADE;

public class MagnetUpgrade extends Upgrade {
    private int tickCounter = 0;
    private final Queue<ItemEntity> queue = new LinkedList<>();

    public List<Item> list = new ArrayList<>();

    public MagnetUpgrade() {
        super(BackpackUpgradeRegistry.MAGNET);
    }

    public void setList(List<Item> list) {
        this.list = list;
    }

    @Override
    public void addTooltip(List<Text> tooltip, ItemStack stack) {
        if (stack.isOf(MAGNET_UPGRADE)) {
            if (this.list.isEmpty()) return;
            for (Item item : this.list) tooltip.add(item.getName());
        }
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
        if (inventory == null) return;

        ServerWorld world = player.getWorld();

        if (queue.isEmpty()) {
            Box area = new Box(player.getPos().add(-5), player.getPos().add(5));
            List<ItemEntity> items = world.getEntitiesByClass(ItemEntity.class, area,
                    Entity::isAlive);

            for (ItemEntity item : items) {
                if (inventory.canInsert(item.getStack())) {
                    queue.add(item);
                    item.setPickupDelayInfinite();
                }
            }
        } else {
            tickCounter++;

            if (tickCounter % 5 == 0) {
                ItemEntity next = queue.poll();

                if (next == null) return;
                if (!next.isAlive()) return;
                if (next.distanceTo(player) > 10) return;

                ItemStack stack = next.getStack();
                if (inventory.canInsert(stack)) {
                    stack = inventory.addStack(stack);
                    if (stack.getCount() > 0)
                        next.setStack(stack);
                    else next.discard();
                    ContainerItem.playInsertSound(player, 1);
                } else next.setPickupDelay(0);
            } else if (tickCounter % 2 == 0) queue.forEach(itemEntity ->
                    itemEntity.setPos(player.getX(), player.getY(), player.getZ())
            );
        }
    }
}
