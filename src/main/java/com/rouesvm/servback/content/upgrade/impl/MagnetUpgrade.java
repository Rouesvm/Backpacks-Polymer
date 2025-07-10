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
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static com.rouesvm.servback.registry.item.BackpackItemRegistry.MAGNET_UPGRADE;

public class MagnetUpgrade extends Upgrade {
    public static final int MAX_SIZE = 5;

    private int tick = 0;
    private final Queue<ItemEntity> queue = new LinkedList<>();

    private final List<String> list = new ArrayList<>(MAX_SIZE);
    private MODE mode = MODE.PICKUP;

    public MagnetUpgrade() {
        super(BackpackUpgradeRegistry.MAGNET);
    }

    public void addAllToList(List<String> list) {
        this.list.addAll(list);
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

        for (String string : data.getTypedListView("Items", Codec.STRING)) {
            list.add(string);
        }
    }

    @Override
    public void writeView(WriteView data) {
        data.putInt("mode", mode.ordinal());

        WriteView.ListAppender<String> listAppender = data.getListAppender("Items", Codec.STRING);

        for (String string : list) {
            if (!string.isEmpty()) listAppender.add(string);
        }

        if (listAppender.isEmpty()) data.remove("Items");
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
            for (String string : this.list) tooltip.add(
                        Text.literal(" ")
                                .append(string).copy()
                                .formatted(Formatting.DARK_AQUA));
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
            ItemStack stack = itemEntity.getStack();

            return switch (mode) {
                case BLACKLIST -> !checkList(stack);
                case WHITELIST -> checkList(stack);
                case PICKUP    -> true;
            };
        });

        for (ItemEntity item : items) {
            if (!inventory.canInsert(item.getStack())) continue;

            queue.add(item);
            item.setPickupDelay(20);
        }
    }

    public boolean checkList(ItemStack stack) {
        boolean matched = false;
        Item item = stack.getItem();
        RegistryEntry<Item> entry = Registries.ITEM.getEntry(item);

        for (String s : list) {
            if (s.startsWith("#")) {
                Identifier tagId = Identifier.tryParse(s.substring(1));
                TagKey<Item> tag = TagKey.of(RegistryKeys.ITEM, tagId);

                if (entry.isIn(tag)) {
                    matched = true;
                    break;
                }
            } else {
                Identifier itemId = Registries.ITEM.getId(item);
                if (itemId.toString().equals(s)) {
                    matched = true;
                    break;
                }
            }
        }

        return matched;
    }

    public enum MODE {
        BLACKLIST,
        WHITELIST,
        PICKUP
    }
}
