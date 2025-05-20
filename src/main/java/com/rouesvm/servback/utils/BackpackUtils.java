package com.rouesvm.servback.utils;

import com.rouesvm.servback.item.ContainerItem;
import com.rouesvm.servback.registry.BackpackDataComponentTypes;
import com.rouesvm.servback.ui.inventory.BackpackInventory;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.UUID;

import static com.rouesvm.servback.Main.CAPACITY;

public class BackpackUtils {
    public static BackpackInventory getItemList(ItemStack stack, int maxPossibleSlot) {
        if (stack.get(BackpackDataComponentTypes.UUID_TYPE) == null) return null;
        UUID uuid = BackpackManager.getStackUUID(stack);
        return BackpackManager.getInventory(uuid, maxPossibleSlot);
    }

    public static int getExtendedSlots(ItemStack stack) {
        NbtComponent component = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
        NbtCompound compound = component.copyNbt();
        if (compound.contains("level"))
            return 9 * compound.getInt("level", 0);
        else return 0;
    }

    public static void checkEnchantments(ItemStack stack, ServerPlayerEntity player, int maxBackpackSlot, int maxExtendedSlot) {
        BackpackInventory inventory = getItemList(stack, maxExtendedSlot + maxBackpackSlot);
        if (inventory == null) return;

        DynamicRegistryManager registryManager = player.getWorld().getRegistryManager();
        RegistryEntry.Reference<Enchantment> capacity = registryManager.getOptional(RegistryKeys.ENCHANTMENT).get().getOrThrow(CAPACITY);

        int level = stack.getEnchantments().getLevel(capacity);
        System.out.println(level);
        int currentExtendedSize = 9 * level;

        NbtCompound compound = new NbtCompound();
        compound.putInt("level", level);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(compound));

        resize(currentExtendedSize, maxBackpackSlot, BackpackManager.getStackUUID(stack), inventory, player);
    }

    public static void resize(int currentExtendedSize, int maxBackpackSlot, UUID uuid, BackpackInventory inventory, ServerPlayerEntity player) {
        if (currentExtendedSize > 0) {
            int totalSlots = currentExtendedSize + maxBackpackSlot;
            if (inventory.size() != totalSlots)
                BackpackManager.resizeInventory(uuid, inventory, totalSlots);
            return;
        }

        if (inventory.size() > maxBackpackSlot)
            dropExcessItems(inventory, maxBackpackSlot, player);
        BackpackManager.resizeInventory(uuid, inventory, maxBackpackSlot);
    }

    public static void dropExcessItems(BackpackInventory inventory, int maxBackpackSlot, ServerPlayerEntity player) {
        for (int i = inventory.size(); i > maxBackpackSlot; --i) {
            ItemStack excessItem = inventory.getHeldStacks().get(i - 1);
            player.dropItem(excessItem, true);
        }
        ContainerItem.playDropContentsSound(player);
    }
}
