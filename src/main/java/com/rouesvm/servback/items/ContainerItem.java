package com.rouesvm.servback.items;

import com.rouesvm.servback.Main;
import com.rouesvm.servback.components.BackpacksDataComponentsType;
import com.rouesvm.servback.ui.BackpackGui;
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

public class ContainerItem extends GuiItem {
    private final int slots;
    private int extendedSlots;

    public ContainerItem(String name, int slots) {
        super(name);
        this.slots = slots;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
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
    public boolean isEnchantable(ItemStack stack) {
        return (getName().contains(Text.literal("Large")) || getName().contains(Text.literal("Medium")));
    }

    @Override
    public void openGui(ServerPlayerEntity player, ItemStack stack) {
        if (stack.get(BackpacksDataComponentsType.UUID_TYPE) == null)
            stack.set(BackpacksDataComponentsType.UUID_TYPE, UUID.randomUUID().toString());

        onEnchanted(stack, player);

        stack.set(BackpacksDataComponentsType.BOOLEAN_TYPE, false);
        new BackpackGui(player, stack, this.slots);
    }

    public DefaultedList<ItemStack> getComponentItemList(ItemStack stack) {
        DefaultedList<ItemStack> list = DefaultedList.ofSize(this.slots + this.extendedSlots, ItemStack.EMPTY);
        stack.getOrDefault(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT).copyTo(list);
        return list;
    }

    private DefaultedList<ItemStack> getItemList(ItemStack stack) {
        UUID uuid = UUID.fromString(stack.get(BackpacksDataComponentsType.UUID_TYPE));
        return Main.backpackManager.getInventory(uuid, this.slots).getInventory();
    }

    private void onEnchanted(ItemStack stack, ServerPlayerEntity player) {
        DefaultedList<ItemStack> inventory = getItemList(stack);

        DynamicRegistryManager registryManager = player.getWorld().getRegistryManager();
        RegistryEntry.Reference<Enchantment> capacity = registryManager.get(RegistryKeys.ENCHANTMENT).entryOf(CAPACITY);

        int level = stack.getEnchantments().getLevel(capacity);
        extendedSlots = 9 * level;

        if (extendedSlots != 0) return;
        if (inventory.size() < this.slots) return;

        for (int i = inventory.size(); i > this.slots; --i)
            player.dropItem(inventory.get(i - 1), true);
    }
}

