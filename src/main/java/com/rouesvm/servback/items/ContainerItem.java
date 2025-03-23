package com.rouesvm.servback.items;

import com.rouesvm.servback.registry.BackpackItemRegistry;
import com.rouesvm.servback.registry.BackpacksDataComponentTypes;
import com.rouesvm.servback.ui.BackpackGui;
import com.rouesvm.servback.ui.inventory.BackpackInventory;
import com.rouesvm.servback.utils.BackpackManager;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.collection.DefaultedList;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.rouesvm.servback.Main.CAPACITY;

public class ContainerItem extends GuiItem {
    private final int slots;

    public ContainerItem(String name, int slots) {
        super(name);
        this.slots = slots;
    }

    @Override
    public boolean canBeNested() {
        return false;
    }

    @Override
    public Optional<TooltipData> getTooltipData(ItemStack stack) {
        return super.getTooltipData(stack);
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
    public void openGui(ServerPlayerEntity player, ItemStack stack) {
        BackpackManager.createNewUUID(stack);

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
        UUID uuid = BackpackManager.getStackUUID(stack);
        return BackpackManager.getInventory(uuid, getExtendedSlots(stack) + this.slots);
    }

    private int getExtendedSlots(ItemStack stack) {
        NbtComponent component = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
        NbtCompound compound = component.copyNbt();
        if (compound.contains("level"))
            return 9 * compound.getInt("level", 0);
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

        UUID uuid = BackpackManager.getStackUUID(stack);

        if (currentSize > 0) {
            int totalSlots = currentSize + this.slots;
            if (inventory.size() != totalSlots)
                BackpackManager.resizeInventory(uuid, inventory, totalSlots);
            return;
        }

        if (inventory.size() > this.slots)
            dropExcessItems(inventory, this.slots, player);
        BackpackManager.resizeInventory(uuid, inventory, this.slots);
    }

    private void dropExcessItems(BackpackInventory inventory, int maxSlots, ServerPlayerEntity player) {
        for (int i = inventory.size(); i > maxSlots; --i) {
            ItemStack excessItem = inventory.getHeldStacks().get(i - 1);
            player.dropItem(excessItem, true);
        }
        player.playSoundToPlayer(SoundEvents.ITEM_BUNDLE_DROP_CONTENTS, SoundCategory.PLAYERS, 0.8F, 0.8F + player.getWorld().getRandom().nextFloat() * 0.4F);
    }

    public static Map<String, Item> getBackpackMap() {
        return Map.ofEntries(
                Map.entry("WHITE_1", BackpackItemRegistry.WHITE_SMALL_BACKPACK),
                Map.entry("WHITE_2", BackpackItemRegistry.WHITE_MEDIUM_BACKPACK),
                Map.entry("WHITE_3", BackpackItemRegistry.WHITE_LARGE_BACKPACK),

                Map.entry("ORANGE_1", BackpackItemRegistry.ORANGE_SMALL_BACKPACK),
                Map.entry("ORANGE_2", BackpackItemRegistry.ORANGE_MEDIUM_BACKPACK),
                Map.entry("ORANGE_3", BackpackItemRegistry.ORANGE_LARGE_BACKPACK),

                Map.entry("MAGENTA_1", BackpackItemRegistry.MAGENTA_SMALL_BACKPACK),
                Map.entry("MAGENTA_2", BackpackItemRegistry.MAGENTA_MEDIUM_BACKPACK),
                Map.entry("MAGENTA_3", BackpackItemRegistry.MAGENTA_LARGE_BACKPACK),

                Map.entry("LIGHT_BLUE_1", BackpackItemRegistry.LIGHT_BLUE_SMALL_BACKPACK),
                Map.entry("LIGHT_BLUE_2", BackpackItemRegistry.LIGHT_BLUE_MEDIUM_BACKPACK),
                Map.entry("LIGHT_BLUE_3", BackpackItemRegistry.LIGHT_BLUE_LARGE_BACKPACK),

                Map.entry("YELLOW_1", BackpackItemRegistry.YELLOW_SMALL_BACKPACK),
                Map.entry("YELLOW_2", BackpackItemRegistry.YELLOW_MEDIUM_BACKPACK),
                Map.entry("YELLOW_3", BackpackItemRegistry.YELLOW_LARGE_BACKPACK),

                Map.entry("LIME_1", BackpackItemRegistry.LIME_SMALL_BACKPACK),
                Map.entry("LIME_2", BackpackItemRegistry.LIME_MEDIUM_BACKPACK),
                Map.entry("LIME_3", BackpackItemRegistry.LIME_LARGE_BACKPACK),

                Map.entry("PINK_1", BackpackItemRegistry.PINK_SMALL_BACKPACK),
                Map.entry("PINK_2", BackpackItemRegistry.PINK_MEDIUM_BACKPACK),
                Map.entry("PINK_3", BackpackItemRegistry.PINK_LARGE_BACKPACK),

                Map.entry("GRAY_1", BackpackItemRegistry.LIGHT_GRAY_SMALL_BACKPACK),
                Map.entry("GRAY_2", BackpackItemRegistry.LIGHT_GRAY_MEDIUM_BACKPACK),
                Map.entry("GRAY_3", BackpackItemRegistry.LIGHT_GRAY_LARGE_BACKPACK),

                Map.entry("LIGHT_GRAY_1", BackpackItemRegistry.LIGHT_GRAY_SMALL_BACKPACK),
                Map.entry("LIGHT_GRAY_2", BackpackItemRegistry.LIGHT_GRAY_MEDIUM_BACKPACK),
                Map.entry("LIGHT_GRAY_3", BackpackItemRegistry.LIGHT_GRAY_LARGE_BACKPACK),

                Map.entry("CYAN_1", BackpackItemRegistry.CYAN_SMALL_BACKPACK),
                Map.entry("CYAN_2", BackpackItemRegistry.CYAN_MEDIUM_BACKPACK),
                Map.entry("CYAN_3", BackpackItemRegistry.CYAN_LARGE_BACKPACK),

                Map.entry("BLUE_1", BackpackItemRegistry.BLUE_SMALL_BACKPACK),
                Map.entry("BLUE_2", BackpackItemRegistry.BLUE_MEDIUM_BACKPACK),
                Map.entry("BLUE_3", BackpackItemRegistry.BLUE_LARGE_BACKPACK),

                Map.entry("GREEN_1", BackpackItemRegistry.GREEN_SMALL_BACKPACK),
                Map.entry("GREEN_2", BackpackItemRegistry.GREEN_MEDIUM_BACKPACK),
                Map.entry("GREEN_3", BackpackItemRegistry.GREEN_LARGE_BACKPACK),

                Map.entry("RED_1", BackpackItemRegistry.RED_SMALL_BACKPACK),
                Map.entry("RED_2", BackpackItemRegistry.RED_MEDIUM_BACKPACK),
                Map.entry("RED_3", BackpackItemRegistry.RED_LARGE_BACKPACK),

                Map.entry("BLACK_1", BackpackItemRegistry.BLACK_SMALL_BACKPACK),
                Map.entry("BLACK_2", BackpackItemRegistry.BLACK_MEDIUM_BACKPACK),
                Map.entry("BLACK_3", BackpackItemRegistry.BLACK_LARGE_BACKPACK),

                Map.entry("PURPLE_1", BackpackItemRegistry.PURPLE_SMALL_BACKPACK),
                Map.entry("PURPLE_2", BackpackItemRegistry.PURPLE_MEDIUM_BACKPACK),
                Map.entry("PURPLE_3", BackpackItemRegistry.PURPLE_LARGE_BACKPACK)
        );
    }

    public int getSize() {
        return slots / 9;
    }

    public static Item getColoredBackpack(DyeColor color, int size) {
        Item item;
        if (color != null)
            item = getBackpackMap().getOrDefault(color.name() + "_" + size, getDefaultBackpack(size));
        else item = getDefaultBackpack(size);
        return item;
    }

    public static Item getDefaultBackpack(int size) {
        return switch (size) {
            case 1 -> BackpackItemRegistry.SMALL_BACKPACK;
            case 2 -> BackpackItemRegistry.MEDIUM_BACKPACK;
            default -> BackpackItemRegistry.LARGE_BACKPACK;
        };
    }
}

