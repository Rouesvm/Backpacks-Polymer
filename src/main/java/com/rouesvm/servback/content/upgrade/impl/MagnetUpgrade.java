package com.rouesvm.servback.content.upgrade.impl;

import com.rouesvm.servback.content.item.impl.ContainerItem;
import com.rouesvm.servback.content.upgrade.FilterableUpgrade;
import com.rouesvm.servback.content.upgrade.PersistentUpgrade;
import com.rouesvm.servback.content.upgrade.Upgrade;
import com.rouesvm.servback.content.upgrade.extension.ItemFilter;
import com.rouesvm.servback.registry.BackpackUpgradeRegistry;
import com.rouesvm.servback.technical.ui.inventory.BackpackInventory;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.*;

public class MagnetUpgrade extends Upgrade implements PersistentUpgrade, FilterableUpgrade {
    public static final int MAX_SIZE = 5;

    public static final int MAX_RANGE = 3;
    private static final double SCANNING_RANGE = ((double) MAX_RANGE / 2) * 3;

    private int tick = 0;

    private final Set<ItemEntity> queue = new HashSet<>();
    private final ItemFilter itemFilter = new ItemFilter(ItemFilter.MODE.PICKUP, new ArrayList<>(MAX_RANGE));

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
        for (String string : this.itemFilter.filterList()) tooltip.add(
                Text.literal(" ")
                        .append(string).copy()
                        .formatted(Formatting.DARK_AQUA));
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
    public void tick(World world, BlockPos pos, BackpackInventory inventory) {
        if (inventory == null || !(world instanceof ServerWorld serverWorld)) return;
        if (pickUpItems(serverWorld, pos, inventory)) return;

        checkForItems(serverWorld, pos, inventory);
    }

    public boolean pickUpItems(ServerWorld world, BlockPos pos, BackpackInventory inventory) {
        if (queue.isEmpty()) return false;

        if (BackpackInventory.isFull(inventory)) {
            tick = 0;
            for (ItemEntity item : queue) {
                item.setPickupDelay(0);
            }
            return false;
        }

        tick++;

        if (tick % 4 == 0) queue.forEach(item -> {
            tick = 0;

            item.setPosition(pos.toCenterPos());
            item.setPickupDelay(100);
        });

        if (tick % 4 == 0) {
            Iterator<ItemEntity> iterator = queue.iterator();
            if (!iterator.hasNext()) return false;

            ItemEntity next = iterator.next();
            if (next == null || !next.isAlive() || next.squaredDistanceTo(pos.toCenterPos()) > MAX_RANGE) {
                iterator.remove();
                return false;
            }

            ItemStack stack = next.getStack();
            if (!inventory.canInsert(stack)) {
                next.setPickupDelay(0);
                iterator.remove();
                return iterator.hasNext();
            }

            ItemStack remainder = inventory.addStack(stack);
            ContainerItem.playInsertSound(world, pos, 1);

            if (remainder.isEmpty()) {
                next.discard();
                iterator.remove();
                return true;
            } else next.setStack(remainder);
        }

        return true;
    }

    public void checkForItems(ServerWorld world, BlockPos pos, BackpackInventory inventory) {
        Vec3d vec3d = pos.toCenterPos();
        Box area = new Box(vec3d.add(-SCANNING_RANGE), vec3d.add(SCANNING_RANGE));

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

        return switch (itemFilter.getMode()) {
            case BLACKLIST -> !itemFilter.matches(stack);
            case WHITELIST -> itemFilter.matches(stack);
            case PICKUP    -> true;
        };
    }
}
