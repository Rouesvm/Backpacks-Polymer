package com.rouesvm.servback.items;

import com.rouesvm.servback.Main;
import com.rouesvm.servback.components.BackpacksDataComponentTypes;
import com.rouesvm.servback.ui.BackpackGui;
import com.rouesvm.servback.utils.BackpackInventory;
import net.fabricmc.fabric.api.item.v1.EnchantingContext;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.collection.DefaultedList;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;
import java.util.UUID;

import static com.rouesvm.servback.Main.*;

public class ContainerItem extends GuiItem {
    private final int slots;

    public ContainerItem(String name, int slots) {
        super(name);
        this.slots = slots;
    }

    @Override
    public void modifyClientTooltip(List<Text> tooltip, ItemStack polymerStack, PacketContext context) {
        BackpackInventory itemList = this.getItemList(polymerStack);
        if (itemList == null) return;
        if (itemList.getHeldStacks().isEmpty()) return;

        int capacityMaxShow = 0;
        int capacityAmount = 0;

        for (ItemStack itemStack : itemList.getHeldStacks()) {
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
    public boolean canBeEnchantedWith(ItemStack stack, RegistryEntry<Enchantment> enchantment, EnchantingContext context) {
        return slots != 9;
    }

    @Override
    public void openGui(ServerPlayerEntity player, ItemStack stack) {
        backpackManager.createNewUUID(stack);

        checkEnchantments(stack, player);
        player.playSoundToPlayer(SoundEvents.ITEM_BUNDLE_INSERT, SoundCategory.PLAYERS, 0.8F, 0.8F + player.getWorld().getRandom().nextFloat() * 0.4F);

        stack.set(BackpacksDataComponentTypes.BOOLEAN_TYPE, false);
        new BackpackGui(player, stack, getExtendedSlots(stack) + this.slots);
    }

    public DefaultedList<ItemStack> getComponentItemList(ItemStack stack) {
        DefaultedList<ItemStack> list = DefaultedList.ofSize(this.slots + getExtendedSlots(stack), ItemStack.EMPTY);
        stack.getOrDefault(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT).copyTo(list);
        return list;
    }

    private BackpackInventory getItemList(ItemStack stack) {
        if (stack.get(BackpacksDataComponentTypes.UUID_TYPE) == null) return null;
        UUID uuid = backpackManager.getStackUUID(stack);
        return backpackManager.getInventory(uuid, getExtendedSlots(stack) + this.slots);
    }

    private int getExtendedSlots(ItemStack stack) {
        NbtComponent component = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
        NbtCompound compound = component.copyNbt();
        if (compound.contains("level"))
            return 9 * compound.getInt("level");
        else return 0;
    }

    private void checkEnchantments(ItemStack stack, ServerPlayerEntity player) {
        BackpackInventory inventory = getItemList(stack);
        if (inventory == null) return;

        DynamicRegistryManager registryManager = player.getWorld().getRegistryManager();
        RegistryEntry.Reference<Enchantment> capacity = registryManager.getOptional(RegistryKeys.ENCHANTMENT).get().getOrThrow(CAPACITY);

        int level = stack.getEnchantments().getLevel(capacity);
        int currentSize = 9 * level;

        NbtCompound compound = new NbtCompound();
        compound.putInt("level", level);
        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(compound));

        if (currentSize > 0) {
            int totalSlots = currentSize + this.slots;
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
            ItemStack excessItem = inventory.getHeldStacks().get(i - 1);
            player.dropItem(excessItem, true);
        }
        player.playSoundToPlayer(SoundEvents.ITEM_BUNDLE_DROP_CONTENTS, SoundCategory.PLAYERS, 0.8F, 0.8F + player.getWorld().getRandom().nextFloat() * 0.4F);
    }
}

