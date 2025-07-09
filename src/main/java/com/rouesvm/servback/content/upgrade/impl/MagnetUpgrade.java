package com.rouesvm.servback.content.upgrade.impl;

import com.rouesvm.servback.content.item.ContainerItem;
import com.rouesvm.servback.content.upgrade.Upgrade;
import com.rouesvm.servback.registry.BackpackUpgradeRegistry;
import com.rouesvm.servback.technical.ui.inventory.BackpackInventory;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.StackWithSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static com.rouesvm.servback.registry.item.BackpackItemRegistry.MAGNET_UPGRADE;

public class MagnetUpgrade extends Upgrade {
    public static final int MAX_SIZE = 5;

    private int tick = 0;
    private final Queue<ItemEntity> queue = new LinkedList<>();

    private List<Item> list = new ArrayList<>(MAX_SIZE);
    private MODE mode = MODE.BLACKLIST;

    public MagnetUpgrade() {
        super(BackpackUpgradeRegistry.MAGNET);
    }

    public void setList(List<Item> list) {
        this.list = list;
    }

    public MODE getMode() {
        return mode;
    }

    public void setMode(MODE mode) {
        this.mode = mode;
    }

    @Override
    public void readView(ReadView data) {
        this.mode = MODE.values()[data.getInt("mode", 0)];

        for (StackWithSlot stackWithSlot : data.getTypedListView("Items", StackWithSlot.CODEC)) {
            list.add(stackWithSlot.stack().getItem());
        }
    }

    @Override
    public void writeView(WriteView data) {
        data.putInt("mode", mode.ordinal());

        WriteView.ListAppender<StackWithSlot> listAppender = data.getListAppender("Items", StackWithSlot.CODEC);

        for (int i = 0; i < list.size(); ++i) {
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
    public void addTooltip(List<Text> tooltip, ItemStack stack, PacketContext context) {
        if (stack.isOf(MAGNET_UPGRADE)) {
            tooltip.add(Text.translatable("info.serverbackpacks.mode")
                    .append(": ")
                    .formatted(Formatting.GRAY)
                    .append(Text.of(mode.toString())
                            .copy()
                            .formatted(Formatting.GREEN)
                    )
            );

            if (this.list.isEmpty()) return;

            tooltip.add(Text.translatable("info.serverbackpacks.contains").formatted(Formatting.GRAY));
            for (Item item : this.list) tooltip.add(
                    Text.literal(" ")
                            .append(item.getName())
                            .copy()
                            .formatted(Formatting.DARK_AQUA)
            );
        }
    }

    @Override
    public boolean onUsed(World world, ServerPlayerEntity player, ItemStack stack) {
        MODE[] modes = MODE.values();
        MODE prevMode = mode;

        int nextOrdinal = (mode.ordinal() + 1) % modes.length;
        mode = modes[nextOrdinal];

        if (mode == prevMode) return false;

        player.sendMessage(Text.translatable("info.serverbackpacks.mode")
                .append(": ")
                .formatted(Formatting.GRAY)
                .append(Text.of(mode.toString())
                        .copy()
                        .formatted(Formatting.GREEN)
                ), true);

        player.playSoundToPlayer(SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), SoundCategory.UI, 1, 1);

        return true;
    }

    @Override
    public void tick(ServerPlayerEntity player, BackpackInventory inventory) {
        if (inventory == null || !(player.getWorld() instanceof ServerWorld world)) return;
        if (!pickUpItems(player, inventory)) checkForItems(world, player, inventory);
    }

    public boolean pickUpItems(ServerPlayerEntity player, BackpackInventory inventory) {
        if (queue.isEmpty()) return false;

        tick++;

        if (tick % 5 == 0) {
            ItemEntity next = queue.poll();
            if (next == null || !next.isAlive() || next.distanceTo(player) > 10) return false;

            ItemStack stack = next.getStack();
            if (!inventory.canInsert(stack)) {
                next.setPickupDelay(0);
                return true;
            }

            ItemStack remainder = inventory.addStack(stack);
            ContainerItem.playInsertSound(player, 1);

            if (remainder.isEmpty()) {
                next.discard();
                return true;
            }

            next.setStack(remainder);
        }

        if (tick % 2 == 0) {
            queue.forEach(item ->
                    item.setPos(player.getX(), player.getY(), player.getZ())
            );
        }

        return true;
    }

    public void checkForItems(ServerWorld world, ServerPlayerEntity player, BackpackInventory inventory) {
        Box area = new Box(player.getPos().add(-5, -5, -5), player.getPos().add(5, 5, 5));
        List<ItemEntity> items = world.getEntitiesByClass(ItemEntity.class, area, itemEntity -> {
            if (!itemEntity.isAlive()) return false;
            Item item = itemEntity.getStack().getItem();

            return switch (mode) {
                case BLACKLIST -> !list.contains(item);
                case WHITELIST -> list.contains(item);
                case PICKUP    -> true;
            };
        });

        for (ItemEntity item : items) {
            if (!inventory.canInsert(item.getStack())) continue;

            queue.add(item);
            item.setPickupDelay(20);
        }
    }

    public enum MODE {
        BLACKLIST,
        WHITELIST,
        PICKUP
    }
}
