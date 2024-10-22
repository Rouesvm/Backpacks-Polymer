package com.rouesvm.servback.items;

import com.rouesvm.servback.Main;
import com.rouesvm.servback.ui.BackpackGui;
import eu.pb4.sgui.api.gui.SimpleGui;
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

import static com.rouesvm.servback.Main.CAPACITY;

public class ContainerItem extends GuiItem {
    private final int slots;
    private int extendedSlots;

    private final boolean enabled = false;

    protected final int RADIUS = 5;

    public ContainerItem(String name, int slots) {
        super(name);
        this.slots = slots;
    }

    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (!enabled) return;
        if (world.isClient() || !entity.isPlayer()) return;
        ServerPlayerEntity player = (ServerPlayerEntity) entity;

        boolean nbt = stack.getOrDefault(Main.BOOLEAN_TYPE, false);
        if (!nbt) {
            Box area = new Box(player.getPos().add(-RADIUS, -RADIUS, -RADIUS), player.getPos().add(RADIUS, RADIUS, RADIUS));

            List<ItemEntity> itemEntities = world.getEntitiesByType(EntityType.ITEM, area, Entity::isAlive);
            SimpleInventory itemList = getInventory(stack);

            for (ItemEntity item : itemEntities) {
                item.setPosition(player.getX(), player.getY(), player.getZ());
                if (item.cannotPickup())
                    continue;

                item.setPickupDelay(0);
                item.kill();
                itemList.addStack(item.getStack());
            }

            BackpackGui.saveItemStack(stack, itemList);
        }
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
        return this.slots == 9 * 2 || this.slots == 9 * 3;
    }

    @Override
    public SimpleGui createGui(ServerPlayerEntity player, ItemStack stack) {
        onEnchanted(stack, player);

        stack.set(Main.BOOLEAN_TYPE, false);
        return new BackpackGui(player, stack, getInventory(stack));
    }

    public SimpleInventory getInventory(ItemStack stack) {
        return new SimpleInventory(getItemList(stack).toArray(ItemStack[]::new));
    }

    public DefaultedList<ItemStack> getItemList(ItemStack stack) {
        DefaultedList<ItemStack> list = DefaultedList.ofSize(slots + extendedSlots, ItemStack.EMPTY);
        stack.getOrDefault(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT).copyTo(list);
        return list;
    }

    public void onEnchanted(ItemStack stack, ServerPlayerEntity player) {
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

