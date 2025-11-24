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
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.ClickType;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import xyz.nucleoid.packettweaker.PacketContext;

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
    public void readView(ReadView data) {
        itemFilter.readView(data);
    }

    @Override
    public void writeView(WriteView data) {
        itemFilter.writeView(data);
    }

    @Override
    public void addTooltip(List<Text> tooltip, ItemStack stack, PacketContext context) {
        tooltip.add(Text.translatable("info.serverbackpacks.mode")
                .append(": ")
                .formatted(Formatting.GRAY)
                .append(Text.translatable("info.serverbackpacks.mode" + "." + this.itemFilter.getMode().toString().toLowerCase())
                            .copy()
                            .formatted(Formatting.GREEN)));

        if (this.itemFilter.filterList().isEmpty()) return;

        tooltip.add(Text.translatable("info.serverbackpacks.contains").formatted(Formatting.GRAY));
        for (String string : this.itemFilter.filterList()) {
            tooltip.add(Text.literal(" ")
                            .append(string).copy()
                            .formatted(Formatting.DARK_AQUA));
        }
    }

    @Override
    public boolean onUsed(World world, ServerPlayerEntity player, ItemStack stack) {
        ItemFilter.MODE[] modes = ItemFilter.MODE.values();

        int nextOrdinal = (this.itemFilter.getMode().ordinal() + 1) % modes.length;
        this.itemFilter.setMode(modes[nextOrdinal]);

        player.sendMessage(Text.translatable("info.serverbackpacks.mode")
                .append(": ")
                .formatted(Formatting.GRAY)
                .append(Text.translatable("info.serverbackpacks.mode" + "." + this.itemFilter.getMode().toString().toLowerCase())
                        .copy()
                        .formatted(Formatting.GREEN)
                ), true);

        player.playSoundToPlayer(SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), SoundCategory.UI, 1, 1);

        return true;
    }

    @Override
    public boolean onClicked(ServerPlayerEntity player, ItemStack stack, Slot slot, ClickType clickType, boolean inContainer) {
        if ((clickType == ClickType.RIGHT) != inContainer) {
            return onUsed(player.getWorld(), player, stack);
        } else return ClickableUpgrade.super.onClicked(player, stack, slot, clickType, inContainer);
    }

    @Override
    public void tick(World world, Vec3d pos, BackpackInventory inventory) {
        if (inventory == null || !(world instanceof ServerWorld serverWorld)) return;

        tick++;

        moveItemsToTarget(pos);
        if (!pickUpItems(serverWorld, pos, inventory)) {
            checkForItems(serverWorld, pos, inventory);
        }
    }

    private void moveItemsToTarget(Vec3d pos) {
        if (queue.isEmpty()) return;

        Vec3d target = new Vec3d(pos.toVector3f());

        queue.forEach(item -> {
            Vec3d current = item.getPos();
            Vec3d delta = target.subtract(current);

            double distance = delta.length();
            if (distance >= MAX_ITEM_ENTITY_DISTANCE_TO_PLAYER) {
                double speed = Math.min(0.6, distance * 0.6);
                Vec3d velocity = delta.normalize().multiply(speed);

                Vec3d smooth = item.getVelocity().lerp(velocity, 0.4);
                item.setVelocity(smooth);
            } else item.setVelocity(Vec3d.ZERO);

            item.velocityModified = true;
            item.setPickupDelay(100);
        });
    }

    private boolean pickUpItems(ServerWorld world, Vec3d pos, BackpackInventory inventory) {
        if (queue.isEmpty()) return false;

        if (BackpackInventory.isFull(inventory)) {
            queue.forEach(entity -> entity.setPickupDelay(0));
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

            if (next.squaredDistanceTo(pos) > MAX_DISTANCE_TO_PLAYER_SQUARED
            ) return false;

            ItemStack stack = next.getStack();
            if (!inventory.canInsert(stack)) {
                next.setPickupDelay(0);
                iterator.remove();
                return iterator.hasNext();
            }

            ItemStack remainder = inventory.addStack(stack);
            ContainerItem.playInsertSound(world, BlockPos.ofFloored(pos), 1);

            if (remainder.isEmpty()) {
                next.discard();
                iterator.remove();
                return true;
            } else next.setStack(remainder);
        }

        return true;
    }

    private void checkForItems(ServerWorld world, Vec3d pos, BackpackInventory inventory) {
        Box area = new Box(pos.add(-MAX_RANGE), pos.add(MAX_RANGE));

        world.getEntitiesByClass(ItemEntity.class, area, (entity ->
                !queue.contains(entity)
                        && !entity.cannotPickup()
                        && checkFilterForItem(entity, inventory)
                        && inventory.canInsert(entity.getStack())
                )).forEach(item -> {
                    queue.add(item);
                    item.setPickupDelay(100);
                });
    }

    private boolean checkFilterForItem(ItemEntity entity, BackpackInventory inventory) {
        if (!entity.isAlive()) return false;
        ItemStack stack = entity.getStack();

        return switch (itemFilter.getMode()) {
            case BLACKLIST -> !itemFilter.matches(stack);
            case WHITELIST -> itemFilter.matches(stack);
            case MATCH_CONTENT -> itemFilter.matches(stack, inventory);
            case PICKUP    -> true;
        };
    }
}
