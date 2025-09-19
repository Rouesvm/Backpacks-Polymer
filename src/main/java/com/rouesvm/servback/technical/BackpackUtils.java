package com.rouesvm.servback.technical;

import com.rouesvm.servback.content.item.impl.ContainerItem;
import com.rouesvm.servback.technical.data.BackpackInstance;
import com.rouesvm.servback.technical.manager.BackpackManager;
import com.rouesvm.servback.technical.manager.BackpackUUID;
import com.rouesvm.servback.technical.ui.inventory.BackpackInventory;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.collection.DefaultedList;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.rouesvm.servback.ServerBackpacks.CAPACITY;

public class BackpackUtils {
    public static String hashBackpackContents(DefaultedList<ItemStack> items) {
        Map<String, Integer> contents = new HashMap<>();

        for (ItemStack stack : items) {
            if (stack.isEmpty()) continue;

            int componentHash = stack.getComponents().hashCode();
            String key = stack.getItem().toString() + ":" + componentHash;

            contents.merge(key, stack.getCount(), Integer::sum);
        }

        return contents.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining(","));
    }

    public static void convertComponentToBackpackData(BackpackInstance instance, ItemStack stack) {
        BackpackInventory inventory = instance.inventory();

        if (!(stack.getItem() instanceof ContainerItem item)) return;
        if (!inventory.isEmpty()) return;
        if (stack.get(DataComponentTypes.CONTAINER) == null) return;

        DefaultedList<ItemStack> itemStacks = item.getComponentItemList(stack);
        if (inventory.insertItems(itemStacks)) {
            instance.copyToInventory(inventory);
            BackpackManager.addBackpack(instance);
        }

        stack.set(DataComponentTypes.CONTAINER, null);
    }

    public static DefaultedList<ItemStack> getItemList(ItemStack stack) {
        UUID uuid = BackpackUUID.getStackUUID(stack);
        BackpackInventory inventory = BackpackManager.getInventory(uuid);

        if (inventory != null) {
            DefaultedList<ItemStack> stacks = DefaultedList.ofSize(inventory.size());
            stacks.addAll(inventory.heldStacks());
            return stacks;
        }

        return DefaultedList.of();
    }

    public static int getExtendedSlots(ItemStack stack) {
        NbtComponent component = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
        NbtCompound compound = component.copyNbt();
        return 9 * compound.getInt("level", 0);
    }

    public static int addCustomData(ServerWorld world, ItemStack stack) {
        DynamicRegistryManager registryManager = world.getRegistryManager();
        RegistryEntry.Reference<Enchantment> capacity = registryManager.getOptional(RegistryKeys.ENCHANTMENT).get().getOrThrow(CAPACITY);

        int level = stack.getEnchantments().getLevel(capacity);

        NbtCompound compound = new NbtCompound();
        compound.putInt("level", level);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(compound));

        return 9 * level;
    }

    public static void resizeIfIncorrectSize(ServerPlayerEntity player, ItemStack stack, int maxBackpackSlot) {
        UUID uuid = BackpackUUID.getStackUUID(stack);
        BackpackInventory inventory = BackpackManager.getInventory(uuid);
        if (inventory != null) resize(player, uuid, inventory,
                    maxBackpackSlot + addCustomData(player.getEntityWorld(), stack));
    }

    public static void dropExcessItems(ServerPlayerEntity player, BackpackInventory target, int totalSlots) {
        for (int i = target.size() - 1; i >= totalSlots; i--) {
            ItemStack excessItem =  i < target.heldStacks().size() ? target.heldStacks().get(i) : ItemStack.EMPTY;
            player.dropItem(excessItem, true);
        }
        ContainerItem.playDropContentsSound(player, -0.2F);
    }

    public static void resize(ServerPlayerEntity player, UUID uuid, BackpackInventory target, int totalSlots) {
        if (target.size() != totalSlots) {
            dropExcessItems(player, target, totalSlots);
            BackpackInventory.resizeInventory(uuid, totalSlots);
        }
    }
}
