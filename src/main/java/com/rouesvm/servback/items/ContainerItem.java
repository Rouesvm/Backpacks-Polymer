package com.rouesvm.servback.items;

import com.rouesvm.servback.Main;
import com.rouesvm.servback.components.BackpacksDataComponentTypes;
import com.rouesvm.servback.ui.BackpackGui;
import com.rouesvm.servback.utils.BackpackInventory;
import net.fabricmc.fabric.api.item.v1.EnchantingContext;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.collection.DefaultedList;

import java.util.List;
import java.util.UUID;

import static com.rouesvm.servback.Main.*;

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
        if (itemList.getSimpleInventory().isEmpty()) return;

        int capacityMaxShow = 0;
        int capacityAmount = 0;

        for (ItemStack itemStack : itemList.getInventory()) {
            if (itemStack == ItemStack.EMPTY) continue;

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
    public boolean canBeEnchantedWith(ItemStack stack, RegistryEntry<Enchantment> enchantment, EnchantingContext context) {
        if (slots == 9)
            return false;
        return super.canBeEnchantedWith(stack, enchantment, context);
    }

    @Override
    public void openGui(ServerPlayerEntity player, ItemStack stack) {
        if (stack.get(BackpacksDataComponentTypes.UUID_TYPE) == null)
            stack.set(BackpacksDataComponentTypes.UUID_TYPE, UUID.randomUUID().toString());

        onEnchanted(stack, player);
        player.playSoundToPlayer(SoundEvents.ITEM_BUNDLE_INSERT, SoundCategory.PLAYERS, 0.8F, 0.8F + player.getWorld().getRandom().nextFloat() * 0.4F);

        stack.set(BackpacksDataComponentTypes.BOOLEAN_TYPE, false);
        new BackpackGui(player, stack, this.extendedSlots + this.slots);
    }

    public DefaultedList<ItemStack> getComponentItemList(ItemStack stack) {
        DefaultedList<ItemStack> list = DefaultedList.ofSize(this.slots + this.extendedSlots, ItemStack.EMPTY);
        stack.getOrDefault(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT).copyTo(list);
        return list;
    }

    private BackpackInventory getItemList(ItemStack stack) {
        if (stack.get(BackpacksDataComponentTypes.UUID_TYPE) == null)
            stack.set(BackpacksDataComponentTypes.UUID_TYPE, UUID.randomUUID().toString());
        UUID uuid = UUID.fromString(stack.get(BackpacksDataComponentTypes.UUID_TYPE));
        return Main.backpackManager.getInventory(uuid, this.slots);
    }

    private void onEnchanted(ItemStack stack, ServerPlayerEntity player) {
        BackpackInventory inventory = getItemList(stack);

        DynamicRegistryManager registryManager = player.getWorld().getRegistryManager();
        RegistryEntry.Reference<Enchantment> capacity = registryManager.getOptional(RegistryKeys.ENCHANTMENT).get().getOrThrow(CAPACITY);

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
        UUID backpackUUID = UUID.fromString(stack.get(BackpacksDataComponentTypes.UUID_TYPE));
        Main.backpackManager.saveBackpack(backpackUUID, newInventory);
    }

    private void dropExcessItems(BackpackInventory inventory, int maxSlots, ServerPlayerEntity player) {
        for (int i = inventory.size(); i > maxSlots; --i) {
            ItemStack excessItem = inventory.getInventory().get(i - 1);
            player.dropItem(excessItem, true);
        }
        player.playSoundToPlayer(SoundEvents.ITEM_BUNDLE_DROP_CONTENTS, SoundCategory.PLAYERS, 0.8F, 0.8F + player.getWorld().getRandom().nextFloat() * 0.4F);
    }
}

