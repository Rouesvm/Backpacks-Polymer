package com.rouesvm.servback.utils;

import com.rouesvm.servback.items.ContainerItem;
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
    public static BackpackInventory getItemList(ItemStack stack, int maxSlots) {
        if (stack.get(BackpackDataComponentTypes.UUID_TYPE) == null) return null;
        UUID uuid = BackpackManager.getStackUUID(stack);
        return BackpackManager.getInventory(uuid, getExtendedSlots(stack) + maxSlots);
    }

    public static int getExtendedSlots(ItemStack stack) {
        NbtComponent component = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
        NbtCompound compound = component.copyNbt();
        if (compound.contains("level"))
            return 9 * compound.getInt("level", 0);
        else return 0;
    }

    public static void checkEnchantments(ItemStack stack, ServerPlayerEntity player, int maxSlots) {
        BackpackInventory inventory = getItemList(stack, maxSlots);
        if (inventory == null) return;

        DynamicRegistryManager registryManager = player.getWorld().getRegistryManager();
        RegistryEntry.Reference<Enchantment> capacity = registryManager.getOptional(RegistryKeys.ENCHANTMENT).get().getOrThrow(CAPACITY);

        int level = stack.getEnchantments().getLevel(capacity);
        int currentSize = 9 * level;

        NbtCompound compound = new NbtCompound();
        compound.putInt("level", level);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(compound));

        if (currentSize > 0) {
            int totalSlots = currentSize + maxSlots;
            if (inventory.size() != totalSlots)
                resizeAndSaveInventory(stack, inventory, totalSlots);
            return;
        }

        if (inventory.size() > maxSlots)
            dropExcessItems(inventory, maxSlots, player);
        resizeAndSaveInventory(stack, inventory, maxSlots);
    }

    public static void resizeAndSaveInventory(ItemStack stack, BackpackInventory inventory, int newSize) {
        BackpackInventory newInventory = new BackpackInventory(newSize);
        inventory.copyTo(newInventory);
        UUID backpackUUID = BackpackManager.getStackUUID(stack);
        BackpackManager.getManager().saveBackpack(backpackUUID, newInventory);
    }

    public static void dropExcessItems(BackpackInventory inventory, int maxSlots, ServerPlayerEntity player) {
        for (int i = inventory.size(); i > maxSlots; --i) {
            ItemStack excessItem = inventory.getHeldStacks().get(i - 1);
            player.dropItem(excessItem, true);
        }
        ContainerItem.playDropContentsSound(player);
    }
}
