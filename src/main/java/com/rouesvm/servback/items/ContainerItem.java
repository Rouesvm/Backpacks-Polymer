package com.rouesvm.servback.items;

import com.rouesvm.servback.ui.BackpackGui;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.inventory.SimpleInventory;
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

import static com.rouesvm.servback.Main.CAPACITY;

public class ContainerItem extends GuiItem {
    private final int slots;
    private int extendedSlots;

    public ContainerItem(String id, int slots) {
        super(id);
        this.slots = slots;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);

        ContainerComponent containerComponent = stack.getOrDefault(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT);

        int capacityMaxShow = 0;
        int capacityAmount = 0;

        for (ItemStack itemStack : containerComponent.iterateNonEmpty()) {
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
    public SimpleGui createGui(ServerPlayerEntity player, ItemStack stack) {
        hasExtended(stack, player);
        return new BackpackGui(player, stack, new SimpleInventory(getItemList(stack).toArray(ItemStack[]::new)));
    }

    public DefaultedList<ItemStack> getItemList(ItemStack stack) {
        DefaultedList<ItemStack> list = DefaultedList.ofSize(slots + extendedSlots, ItemStack.EMPTY);
        stack.getOrDefault(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT).copyTo(list);
        return list;
    }

    public void hasExtended(ItemStack stack, ServerPlayerEntity player) {
        DefaultedList<ItemStack> inventory = getItemList(stack);

        DynamicRegistryManager registryManager = player.getWorld().getRegistryManager();
        RegistryEntry.Reference<Enchantment> capacity = registryManager.get(RegistryKeys.ENCHANTMENT).entryOf(CAPACITY);

        int level = stack.getEnchantments().getLevel(capacity);
        extendedSlots = 9 * level;

        if (extendedSlots != 0) return;
        if (inventory.size() < slots) return;

        for (int i = inventory.size(); i > slots; --i)
            player.dropItem(inventory.get(i - 1), true);
    }
}

