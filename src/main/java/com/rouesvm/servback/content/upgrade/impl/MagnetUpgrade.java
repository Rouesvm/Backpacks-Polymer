package com.rouesvm.servback.content.upgrade.impl;

import com.mojang.serialization.Codec;
import com.rouesvm.servback.content.item.impl.ContainerItem;
import com.rouesvm.servback.content.upgrade.Upgrade;
import com.rouesvm.servback.registry.BackpackUpgradeRegistry;
import com.rouesvm.servback.technical.ui.inventory.BackpackInventory;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.*;

import static com.rouesvm.servback.registry.item.BackpackItemRegistry.MAGNET_UPGRADE;

public class MagnetUpgrade extends Upgrade {
    public static final int MAX_SIZE = 5;

    public static final int MAX_RANGE = 3;
    private static final double SCANNING_RANGE = ((double) MAX_RANGE / 2) * 3;

    private int tick = 0;
    private MODE mode = MODE.PICKUP;

    private final Set<ItemEntity> queue = new HashSet<>();
    private final List<String> filterList = new ArrayList<>(MAX_SIZE);

    public MagnetUpgrade() {
        super(BackpackUpgradeRegistry.MAGNET);
    }

    public void addAllToList(List<String> list) {
        this.filterList.addAll(list);
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

        ReadView.TypedListReadView<String> listReadView = data.getTypedListView("Items", Codec.STRING);
        listReadView.forEach(filterList::add);
    }

    @Override
    public void writeView(WriteView data) {
        data.putInt("mode", mode.ordinal());

        WriteView.ListAppender<String> listAppender = data.getListAppender("Items", Codec.STRING);
        filterList.stream()
                .filter((string) -> !string.isEmpty())
                .forEach(listAppender::add);

        if (listAppender.isEmpty()) data.remove("Items");
    }

    @Override
    public void addTooltip(List<Text> tooltip, ItemStack stack, PacketContext context) {
        if (stack.isOf(MAGNET_UPGRADE)) {
            tooltip.add(Text.translatable("info.serverbackpacks.mode")
                    .append(": ")
                    .formatted(Formatting.GRAY)
                    .append(Text.translatable("info.serverbackpacks.mode" + "." + mode.toString().toLowerCase())
                            .copy()
                            .formatted(Formatting.GREEN)
                    )
            );

            if (this.filterList.isEmpty()) return;

            tooltip.add(Text.translatable("info.serverbackpacks.contains").formatted(Formatting.GRAY));
            for (String string : this.filterList) tooltip.add(
                        Text.literal(" ")
                                .append(string).copy()
                                .formatted(Formatting.DARK_AQUA));
        }
    }

    @Override
    public boolean onUsed(World world, ServerPlayerEntity player, ItemStack stack) {
        MODE[] modes = MODE.values();

        int nextOrdinal = (mode.ordinal() + 1) % modes.length;
        mode = modes[nextOrdinal];

        player.sendMessage(Text.translatable("info.serverbackpacks.mode")
                .append(": ")
                .formatted(Formatting.GRAY)
                .append(Text.translatable("info.serverbackpacks.mode" + "." + mode.toString().toLowerCase())
                        .copy()
                        .formatted(Formatting.GREEN)
                ), true);

        player.playSoundToPlayer(SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), SoundCategory.UI, 1, 1);

        return true;
    }

    @Override
    public void tick(ServerPlayerEntity player, BackpackInventory inventory) {
        if (inventory == null || !(player.getWorld() instanceof ServerWorld world)) return;
        if (pickUpItems(player, inventory)) return;

        checkForItems(world, player, inventory);
    }

    public boolean pickUpItems(ServerPlayerEntity player, BackpackInventory inventory) {
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

            item.setPos(player.getX(), player.getY(), player.getZ());
            item.setPickupDelay(100);
        });

        if (tick % 4 == 0) {
            Iterator<ItemEntity> iterator = queue.iterator();
            if (!iterator.hasNext()) return false;

            ItemEntity next = iterator.next();
            if (next == null || !next.isAlive() || next.distanceTo(player) > MAX_RANGE) {
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
            ContainerItem.playInsertSound(player, 1);

            if (remainder.isEmpty()) {
                next.discard();
                iterator.remove();
                return true;
            } else next.setStack(remainder);
        }

        return true;
    }

    public void checkForItems(ServerWorld world, ServerPlayerEntity player, BackpackInventory inventory) {
        Box area = new Box(player.getPos().add(-SCANNING_RANGE), player.getPos().add(SCANNING_RANGE));

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

        return switch (mode) {
            case BLACKLIST -> !checkList(stack);
            case WHITELIST -> checkList(stack);
            case PICKUP    -> true;
        };
    }

    public boolean checkList(ItemStack stack) {
        return filterList.stream().anyMatch(filterID ->
                        matchesFilter(filterID, stack.getItem()));
    }

    public static boolean matchesFilter(String filterID, Item item) {
        var itemRegistry = Registries.ITEM;
        var itemEntry = itemRegistry.getEntry(item);

        if (filterID.startsWith("#")) {
            Identifier tagId = Identifier.tryParse(filterID.substring(1));
            if (tagId == null) return false;

            TagKey<Item> tag = TagKey.of(itemRegistry.getKey(), tagId);
            return itemEntry.isIn(tag);
        }

        String itemId = itemEntry.getIdAsString();
        return itemId.equals(filterID);
    }

    public enum MODE {
        BLACKLIST,
        WHITELIST,
        PICKUP
    }
}
