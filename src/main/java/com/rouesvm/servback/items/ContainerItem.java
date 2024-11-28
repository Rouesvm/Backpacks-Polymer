package com.rouesvm.servback.items;

import com.rouesvm.servback.Main;
import com.rouesvm.servback.components.BackpacksDataComponentTypes;
import com.rouesvm.servback.ui.BackpackGui;
import net.fabricmc.fabric.api.item.v1.EnchantingContext;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.*;
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
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

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

        stack.set(BackpacksDataComponentTypes.BOOLEAN_TYPE, false);
        new BackpackGui(player, stack, this.slots);    }

    public DefaultedList<ItemStack> getComponentItemList(ItemStack stack) {
        DefaultedList<ItemStack> list = DefaultedList.ofSize(this.slots + this.extendedSlots, ItemStack.EMPTY);
        stack.getOrDefault(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT).copyTo(list);
        return list;
    }

    private DefaultedList<ItemStack> getItemList(ItemStack stack) {
        UUID uuid = UUID.fromString(stack.get(BackpacksDataComponentTypes.UUID_TYPE));
        return Main.backpackManager.getInventory(uuid, this.slots).getInventory();
    }

    private void onEnchanted(ItemStack stack, ServerPlayerEntity player) {
        DefaultedList<ItemStack> inventory = getItemList(stack);

        DynamicRegistryManager registryManager = player.getWorld().getRegistryManager();
        RegistryEntry.Reference<Enchantment> capacity = registryManager.getOptional(RegistryKeys.ENCHANTMENT).get().getOrThrow(CAPACITY);

        int level = stack.getEnchantments().getLevel(capacity);
        extendedSlots = 9 * level;

        if (extendedSlots != 0) return;
        if (inventory.size() < this.slots) return;

        for (int i = inventory.size(); i > this.slots; --i)
            player.dropItem(inventory.get(i - 1), true);
    }}

