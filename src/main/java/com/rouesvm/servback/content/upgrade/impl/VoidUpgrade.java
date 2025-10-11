package com.rouesvm.servback.content.upgrade.impl;

import com.rouesvm.servback.content.upgrade.FilterableUpgrade;
import com.rouesvm.servback.content.upgrade.PersistentUpgrade;
import com.rouesvm.servback.content.upgrade.Upgrade;
import com.rouesvm.servback.content.upgrade.extension.ItemFilter;
import com.rouesvm.servback.registry.BackpackUpgradeRegistry;
import com.rouesvm.servback.technical.ui.inventory.BackpackInventory;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static com.rouesvm.servback.content.upgrade.impl.MagnetUpgrade.MAX_SIZE;

public class VoidUpgrade extends Upgrade implements PersistentUpgrade, FilterableUpgrade {
    public static final double MAX_RANGE = 2.5;

    public static final double MAX_ITEM_ENTITY_DISTANCE_TO_PLAYER = 0.75;
    public static final double MAX_DISTANCE_TO_PLAYER_SQUARED = 1.25*1.25;

    private int tick = 0;

    private final Set<ItemEntity> queue = new HashSet<>();
    private final ItemFilter itemFilter = new ItemFilter(ItemFilter.MODE.PICKUP, new ObjectOpenHashSet<>(MAX_SIZE));

    public VoidUpgrade() {
        super(BackpackUpgradeRegistry.VOID);
    }

    @Override
    public ItemFilter getFilter() {
        return itemFilter;
    }

    @Override
    public void readView(NbtCompound data) {
        itemFilter.readView(data);
    }

    @Override
    public void writeView(NbtCompound data) {
        itemFilter.writeView(data);
    }

    @Override
    public void addTooltip(List<Text> tooltip, ItemStack stack, PacketContext context) {
        if (this.itemFilter.filterList().isEmpty()) return;

        tooltip.add(Text.translatable("info.serverbackpacks.contains").formatted(Formatting.GRAY));
        for (String string : this.itemFilter.filterList()) {
            tooltip.add(Text.literal(" ")
                            .append(string).copy()
                            .formatted(Formatting.DARK_AQUA));
        }
    }

    @Override
    public void tick(World world, Vec3d pos, BackpackInventory inventory) {
        if (inventory == null || !(world instanceof ServerWorld serverWorld)) return;

        tick++;
        moveItemsToTarget(pos);
        if (!voidItems(pos, inventory)
        ) checkForItems(serverWorld, pos, inventory);
    }

    private void moveItemsToTarget(Vec3d pos) {
        if (queue.isEmpty()) return;

        Vec3d target = new Vec3d(pos.toVector3f());

        queue.forEach(item -> {
            Vec3d current = item.getPos();
            Vec3d delta = target.subtract(current);

            double distance = delta.length();
            if (distance >= MAX_ITEM_ENTITY_DISTANCE_TO_PLAYER) {
                double speed = Math.min(0.6, distance * 0.8);
                Vec3d velocity = delta.normalize().multiply(speed);

                Vec3d smooth = item.getVelocity().lerp(velocity, 0.4);
                item.setVelocity(smooth);
            } else item.setVelocity(Vec3d.ZERO);

            item.velocityModified = true;
            item.setPickupDelay(100);
        });
    }

    private boolean voidItems(Vec3d pos, BackpackInventory inventory) {
        if (queue.isEmpty()) return false;

        if (BackpackInventory.isFull(inventory)) {
            queue.forEach(entity -> entity.setPickupDelay(0));
            return false;
        }

        if (tick % 4 == 0) {
            Iterator<ItemEntity> iterator = queue.iterator();
            if (!iterator.hasNext()) return false;

            ItemEntity next = iterator.next();
            if (next == null || !next.isAlive() || next.squaredDistanceTo(pos) > MAX_RANGE) {
                iterator.remove();
                return false;
            }

            if (next.squaredDistanceTo(pos)
                    > MAX_DISTANCE_TO_PLAYER_SQUARED
            ) return false;


            next.setDespawnImmediately();
        }

        return true;
    }

    private void checkForItems(ServerWorld world, Vec3d pos, BackpackInventory inventory) {
        Box area = new Box(pos.add(-MAX_RANGE, -MAX_RANGE, -MAX_RANGE), pos.add(MAX_RANGE, MAX_RANGE, MAX_RANGE));

        world.getEntitiesByClass(ItemEntity.class, area, (entity ->
                !queue.contains(entity)
                        && inventory.canInsert(entity.getStack())
                        && checkFilterForItem(entity))
                ).forEach(item -> {
                    queue.add(item);
                    item.setPickupDelay(100);
                });
    }

    private boolean checkFilterForItem(ItemEntity entity) {
        if (!entity.isAlive()) return false;
        ItemStack stack = entity.getStack();
        return itemFilter.matches(stack);
    }
}
