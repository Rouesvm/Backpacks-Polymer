package com.rouesvm.servback.content.item.impl;

import com.rouesvm.servback.technical.BackpackUtils;
import com.rouesvm.servback.technical.data.BackpackInstance;
import com.rouesvm.servback.technical.manager.BackpackManager;
import com.rouesvm.servback.technical.manager.BackpackUUID;
import com.rouesvm.servback.technical.ui.LavaBackpackGui;
import com.rouesvm.servback.technical.ui.inventory.BackpackInventory;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public class LavaContainerItem extends ContainerItem {
    public LavaContainerItem(String name, int slots, Block block) {
        super(name, slots, block);
    }

    public LavaContainerItem(String name, int slots) {
        super(name, slots);
    }

    @Override
    public void inventoryTick(@NonNull ItemStack stack, @NonNull ServerLevel world, @NonNull Entity entity, @Nullable EquipmentSlot slot) {
        super.inventoryTick(stack, world, entity, slot);
        if (entity instanceof ServerPlayer player) {
            Container inventory = getInventory(player, stack);
            if (inventory instanceof BackpackInventory backpackInventory) {
                if (clearOldItems(backpackInventory)) {
                    playBurnSound(player);
                }
            }
        }
    }

    @Override
    public void afterChanged(ItemStack stack, Container inventory) {
        super.afterChanged(stack, inventory);
        if (inventory instanceof BackpackInventory backpackInventory) {
            clearOldItems(backpackInventory);
        }
    }

    @Override
    public void openGui(ServerPlayer player, ItemStack stack) {
        BackpackUUID.getUUIDOrCreateNew(stack);
        BackpackUtils.resizeIfIncorrectSize(player.level(), player.blockPosition(), stack, this.slots);

        Optional<BackpackInstance> instance = BackpackManager.getInstanceAndResize(
                BackpackUUID.getStackUUID(stack),
                this.slots + BackpackUtils.getExtendedSlots(stack));

        instance.ifPresent(backpackInstance -> new LavaBackpackGui(player, stack, backpackInstance));
    }

    public static void playBurnSound(ServerPlayer player) {
        player.level().playSound(null, player.blockPosition(), SoundEvents.GENERIC_BURN, SoundSource.PLAYERS, 0.4F,
                2.0F + player.level().getRandom().nextFloat() * 0.4F);
    }

    public static boolean clearOldItems(BackpackInventory inventory) {
        int size = inventory.getContainerSize();
        for (int i = 0; i < size; i++) {
            if (inventory.getItem(i).isEmpty()) return false;
        }
        for (int i = 0; i < size - 9; i++) {
            inventory.setItem(i, inventory.getItem(i + 9));
        }
        for (int i = size - 9; i < size; i++) {
            inventory.setItem(i, ItemStack.EMPTY);
        }
        return true;
    }
}
