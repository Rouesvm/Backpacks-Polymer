package com.rouesvm.servback.technical;

import com.rouesvm.servback.content.item.impl.ContainerItem;
import com.rouesvm.servback.technical.data.BackpackInstance;
import com.rouesvm.servback.technical.manager.BackpackManager;
import com.rouesvm.servback.technical.manager.BackpackUUID;
import com.rouesvm.servback.technical.ui.inventory.BackpackInventory;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.rouesvm.servback.ServerBackpacks.CAPACITY;

public class BackpackUtils {
    public static String hashBackpackContents(NonNullList<ItemStack> items) {
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
        if (stack.get(DataComponents.CONTAINER) == null) return;

        NonNullList<ItemStack> itemStacks = item.getComponentItemList(stack);
        if (inventory.insertItems(itemStacks)) {
            instance.copyToInventory(inventory);
            BackpackManager.addBackpack(instance);
        }

        stack.set(DataComponents.CONTAINER, null);
    }

    public static NonNullList<ItemStack> getItemList(ItemStack stack) {
        UUID uuid = BackpackUUID.getStackUUID(stack);
        BackpackInventory inventory = BackpackManager.getInventory(uuid);

        if (inventory != null) {
            NonNullList<ItemStack> stacks = NonNullList.createWithCapacity(inventory.getContainerSize());
            stacks.addAll(inventory.heldStacks());
            return stacks;
        }

        return NonNullList.create();
    }

    public static int getExtendedSlots(ItemStack stack) {
        return getExtendedSlots(stack.getComponents());
    }

    public static int getExtendedSlots(DataComponentGetter getter) {
        CustomData component = getter.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag compound = component.copyTag();
        return 9 * compound.getIntOr("level", 0);
    }

    public static int addCustomData(ServerLevel world, ItemStack stack) {
        RegistryAccess registryManager = world.registryAccess();
        Optional<Registry<Enchantment>> enchantmentReference = registryManager.lookup(Registries.ENCHANTMENT);

        CompoundTag compound = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        compound.putString("name", stack.getItem().toString());

        int level = 0;
        if (enchantmentReference.isPresent()) {
            Optional<Holder.Reference<Enchantment>> capacity = enchantmentReference.get().get(CAPACITY);

            if (capacity.isPresent()) {
                level = stack.getEnchantments().getLevel(capacity.get());
                compound.putInt("level", level);
            }
        }

        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(compound));

        return 9 * level;
    }

    public static void resizeIfIncorrectSize(ServerLevel world, BlockPos pos, ItemStack stack, int maxBackpackSlot) {
        UUID uuid = BackpackUUID.getStackUUID(stack);
        BackpackInventory inventory = BackpackManager.getInventory(uuid);
        if (inventory != null) resize(world, pos, uuid, inventory,
                    maxBackpackSlot + addCustomData(world, stack));
    }

    public static void dropItems(Level world, BlockPos pos, BackpackInventory target, int totalSlots) {
        for (int i = target.getContainerSize() - 1; i >= totalSlots; i--) {
            ItemStack excessItem =  i < target.heldStacks().size() ? target.heldStacks().get(i) : ItemStack.EMPTY;
            if (!excessItem.isEmpty()) {
                ItemEntity entity = new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), excessItem);
                entity.setDefaultPickUpDelay();
                world.addFreshEntity(entity);
            }
        }
    }

    public static void resize(Level world, BlockPos pos, UUID uuid, BackpackInventory target, int totalSlots) {
        if (target.getContainerSize() != totalSlots) {
            dropItems(world, pos, target, totalSlots);
            ContainerItem.playDropContentsSound(world, pos);
            BackpackInventory.resizeInventory(uuid, totalSlots);
        }
    }
}
