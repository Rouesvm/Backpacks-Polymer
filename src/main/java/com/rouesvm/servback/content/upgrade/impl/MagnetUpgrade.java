package com.rouesvm.servback.content.upgrade.impl;

import com.rouesvm.servback.content.item.impl.ContainerItem;
import com.rouesvm.servback.content.upgrade.ClickableUpgrade;
import com.rouesvm.servback.content.upgrade.FilterableUpgrade;
import com.rouesvm.servback.content.upgrade.PersistentUpgrade;
import com.rouesvm.servback.content.upgrade.Upgrade;
import com.rouesvm.servback.content.upgrade.extension.ItemFilter;
import com.rouesvm.servback.registry.BackpackUpgradeRegistry;
import com.rouesvm.servback.technical.ui.inventory.BackpackInventory;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MagnetUpgrade extends Upgrade implements PersistentUpgrade, FilterableUpgrade, ClickableUpgrade {
    public static final int MAX_SIZE = 5;

    public static final double MAX_RANGE = 2.5;

    public static final double MAX_ITEM_ENTITY_DISTANCE_TO_PLAYER = 0.75;
    public static final double MAX_DISTANCE_TO_PLAYER_SQUARED = 1.25*1.25;

    private int tick = 0;

    private final List<ItemEntity> queue = new ArrayList<>();
    private final ItemFilter itemFilter = new ItemFilter(ItemFilter.MODE.PICKUP, new ObjectOpenHashSet<>(MAX_SIZE));

    public MagnetUpgrade() {
        super(BackpackUpgradeRegistry.MAGNET);
    }

    @Override
    public ItemFilter getFilter() {
        return itemFilter;
    }

    @Override
    public void readView(ValueInput data) {
        itemFilter.readView(data);
    }

    @Override
    public void writeView(ValueOutput data) {
        itemFilter.writeView(data);
    }

    @Override
    public void addTooltip(List<Component> tooltip, ItemStack stack, PacketContext context) {
        tooltip.add(Component.translatable("info.serverbackpacks.mode")
                .append(": ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.translatable("info.serverbackpacks.mode" + "." + this.itemFilter.getMode().toString().toLowerCase())
                            .copy()
                            .withStyle(ChatFormatting.GREEN)));

        if (this.itemFilter.filterList().isEmpty()) return;

        tooltip.add(Component.translatable("info.serverbackpacks.contains").withStyle(ChatFormatting.GRAY));
        for (String string : this.itemFilter.filterList()) {
            tooltip.add(Component.literal(" ")
                            .append(string).copy()
                            .withStyle(ChatFormatting.DARK_AQUA));
        }
    }

    @Override
    public boolean onUsed(Level world, ServerPlayer player, ItemStack stack) {
        ItemFilter.MODE[] modes = ItemFilter.MODE.values();

        int nextOrdinal = (this.itemFilter.getMode().ordinal() + 1) % modes.length;
        this.itemFilter.setMode(modes[nextOrdinal]);

        player.sendSystemMessage(Component.translatable("info.serverbackpacks.mode")
                .append(": ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.translatable("info.serverbackpacks.mode" + "." + this.itemFilter.getMode().toString().toLowerCase())
                        .copy()
                        .withStyle(ChatFormatting.GREEN)
                ), true);

        player.playSound(SoundEvents.NOTE_BLOCK_CHIME.value(), 1, 1);

        return true;
    }

    @Override
    public boolean onClicked(ServerPlayer player, ItemStack stack, ItemStack otherStack, Slot slot, ClickAction clickType, boolean inContainer) {
        if ((clickType == ClickAction.SECONDARY) != inContainer) {
            return onUsed(player.level(), player, stack);
        } else return ClickableUpgrade.super.onClicked(player, stack, otherStack, slot, clickType, inContainer);
    }

    @Override
    public void tick(ServerPlayer player, ItemStack stack, ServerLevel world, Vec3 pos, BackpackInventory inventory) {
        if (inventory == null || !(world instanceof ServerLevel serverWorld)) return;

        tick++;

        moveItemsToTarget(pos);
        if (!pickUpItems(serverWorld, pos, inventory)) {
            checkForItems(serverWorld, pos, inventory);
        }
    }

    private void moveItemsToTarget(Vec3 pos) {
        if (queue.isEmpty()) return;

        Vec3 target = new Vec3(pos.toVector3f());

        queue.forEach(item -> {
            Vec3 current = item.position();
            Vec3 delta = target.subtract(current);

            double distance = delta.length();
            if (distance >= MAX_ITEM_ENTITY_DISTANCE_TO_PLAYER) {
                double speed = Math.min(0.6, distance * 0.6);
                Vec3 velocity = delta.normalize().scale(speed);

                Vec3 smooth = item.getDeltaMovement().lerp(velocity, 0.4);
                item.setDeltaMovement(smooth);
            } else item.setDeltaMovement(Vec3.ZERO);

            item.needsSync = true;
            item.setPickUpDelay(100);
        });
    }

    private boolean pickUpItems(ServerLevel world, Vec3 pos, BackpackInventory inventory) {
        if (queue.isEmpty()) return false;

        if (BackpackInventory.isFull(inventory)) {
            queue.forEach(entity -> entity.setPickUpDelay(0));
            return false;
        }

        if (tick % 4 == 0) {
            Iterator<ItemEntity> iterator = queue.iterator();
            if (!iterator.hasNext()) return false;

            ItemEntity next = iterator.next();
            if (next == null
                    || !next.isAlive()
            ) {
                iterator.remove();
                return false;
            }

            if (next.distanceToSqr(pos) > MAX_DISTANCE_TO_PLAYER_SQUARED
            ) return false;

            ItemStack stack = next.getItem();
            if (!inventory.canInsert(stack)) {
                next.setPickUpDelay(0);
                iterator.remove();
                return iterator.hasNext();
            }

            ItemStack remainder = inventory.addStack(stack);
            ContainerItem.playInsertSound(world, BlockPos.containing(pos), 1);

            if (remainder.isEmpty()) {
                next.discard();
                iterator.remove();
                return true;
            } else next.setItem(remainder);
        }

        return true;
    }

    private void checkForItems(ServerLevel world, Vec3 pos, BackpackInventory inventory) {
        AABB area = new AABB(pos.add(-MAX_RANGE), pos.add(MAX_RANGE));

        world.getEntitiesOfClass(ItemEntity.class, area, (entity ->
                !queue.contains(entity)
                        && !entity.hasPickUpDelay()
                        && checkFilterForItem(entity, inventory)
                        && inventory.canInsert(entity.getItem())
                )).forEach(item -> {
                    queue.add(item);
                    item.setPickUpDelay(100);
                });
    }

    private boolean checkFilterForItem(ItemEntity entity, BackpackInventory inventory) {
        if (!entity.isAlive()) return false;
        ItemStack stack = entity.getItem();

        return switch (itemFilter.getMode()) {
            case BLACKLIST -> !itemFilter.matches(stack);
            case WHITELIST -> itemFilter.matches(stack);
            case MATCH_CONTENT -> itemFilter.matches(stack, inventory);
            case PICKUP    -> true;
        };
    }
}
