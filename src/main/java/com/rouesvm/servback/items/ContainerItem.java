package com.rouesvm.servback.items;

import com.rouesvm.servback.Main;
import com.rouesvm.servback.components.BackpacksDataComponentTypes;
import com.rouesvm.servback.ui.BackpackGui;
import com.rouesvm.servback.utils.BackpackInventory;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.collection.DefaultedList;

import java.util.List;
import java.util.UUID;

import static com.rouesvm.servback.Main.CAPACITY;
import static com.rouesvm.servback.Main.backpackManager;

public class ContainerItem extends GuiItem {
    private final int slots;
    private int extendedSlots;

    public ContainerItem(String name, int slots) {
        super(name);
        this.slots = slots;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        BackpackInventory itemList = this.getItemList(stack);
        if (itemList.getInventory().isEmpty()) return;

        int capacityMaxShow = 0;
        int capacityAmount = 0;

        for (ItemStack itemStack : itemList.getInventory()) {
            if (itemStack.isEmpty()) continue;

            capacityAmount++;

            if (capacityMaxShow <= 4) {
                capacityMaxShow++;
                tooltip.add(Text.translatable("container.shulkerBox.itemCount", itemStack.getName(), itemStack.getCount()).formatted(Formatting.GOLD));
            }
        }

        if (capacityAmount - capacityMaxShow > 0) {
            tooltip.add(Text.translatable("container.shulkerBox.more", capacityAmount - capacityMaxShow).formatted(Formatting.ITALIC).formatted(Formatting.GOLD));
        }
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return (getName().contains(Text.literal("Large")) || getName().contains(Text.literal("Medium")));
    }

    @Override
    public void openGui(ServerPlayerEntity player, ItemStack stack) {
        backpackManager.createNewUUID(stack);

        onEnchanted(stack, player);

        stack.set(BackpacksDataComponentTypes.BOOLEAN_TYPE, false);
        new BackpackGui(player, stack, this.extendedSlots + this.slots);
    }

    public DefaultedList<ItemStack> getComponentItemList(ItemStack stack) {
        DefaultedList<ItemStack> list = DefaultedList.ofSize(this.slots + this.extendedSlots, ItemStack.EMPTY);
        stack.getOrDefault(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT).copyTo(list);
        return list;
    }

    private BackpackInventory getItemList(ItemStack stack) {
        UUID uuid = backpackManager.getStackUUID(stack);
        return backpackManager.getInventory(uuid, this.extendedSlots + this.slots);
    }

    private void onEnchanted(ItemStack stack, ServerPlayerEntity player) {
        BackpackInventory inventory = getItemList(stack);

        DynamicRegistryManager registryManager = player.getWorld().getRegistryManager();
        RegistryEntry.Reference<Enchantment> capacity = registryManager.get(RegistryKeys.ENCHANTMENT).entryOf(CAPACITY);

        int level = stack.getEnchantments().getLevel(capacity);
        this.extendedSlots = 9 * level;

        if (this.extendedSlots > 0) {
            int totalSlots = this.extendedSlots + this.slots;
            if (inventory.size() != totalSlots)
                resizeAndSaveInventory(stack, inventory, totalSlots);
            return;
        }

        if (inventory.size() > this.slots)
            dropExcessItems(inventory, this.slots, player);
        resizeAndSaveInventory(stack, inventory, this.slots);
    }

    private void resizeAndSaveInventory(ItemStack stack, BackpackInventory inventory, int newSize) {
        BackpackInventory newInventory = new BackpackInventory(newSize);
        inventory.copyTo(newInventory);
        UUID backpackUUID = backpackManager.getStackUUID(stack);
        Main.backpackManager.saveBackpack(backpackUUID, newInventory);
    }

    private void dropExcessItems(BackpackInventory inventory, int maxSlots, ServerPlayerEntity player) {
        for (int i = inventory.size(); i > maxSlots; --i) {
            ItemStack excessItem = inventory.getInventory().get(i - 1);
            player.dropItem(excessItem, true);
        }
    }
}

