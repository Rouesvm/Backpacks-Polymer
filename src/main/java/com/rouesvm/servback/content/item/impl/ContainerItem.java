package com.rouesvm.servback.content.item.impl;

import com.rouesvm.servback.ServerBackpacks;
import com.rouesvm.servback.content.block.impl.BackpackBlockEntity;
import com.rouesvm.servback.content.component.UpgradeContainerComponent;
import com.rouesvm.servback.content.item.BundleGuiItem;
import com.rouesvm.servback.content.upgrade.Upgrade;
import com.rouesvm.servback.registry.BackpackDataComponentTypes;
import com.rouesvm.servback.registry.block.BackpackBlockRegistry;
import com.rouesvm.servback.technical.BackpackUtils;
import com.rouesvm.servback.technical.data.BackpackInstance;
import com.rouesvm.servback.technical.data.BackpackManager;
import com.rouesvm.servback.technical.ui.BackpackGui;
import com.rouesvm.servback.technical.ui.inventory.BackpackInventory;
import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ContainerItem extends BundleGuiItem {
    public final int slots;

    public ContainerItem(String name, int slots) {
        super(name, BackpackBlockRegistry.BACKPACK);
        this.slots = slots;
    }

    public int getSize() {
        return slots;
    }

    @Override
    public boolean canBeNested() {
        return false;
    }

    @Override
    public void modifyClientTooltip(List<Text> tooltip, ItemStack polymerStack, PacketContext context) {
        UUID uuid = polymerStack.get(BackpackDataComponentTypes.BACKPACK_UUID);
        if (ServerBackpacks.isDevEnvironment)
            if (uuid != null) tooltip.add(Text.of("UUID: " + BackpackManager.getStackUUID(polymerStack)));

        addUpgradeTooltip(tooltip, polymerStack);
        addInventoryTooltip(tooltip, polymerStack);
    }

    public static void addUpgradeTooltip(List<Text> tooltip, ItemStack stack) {
        UpgradeContainerComponent upgradeContainer = stack.get(BackpackDataComponentTypes.UPGRADE_CONTAINER);
        if (upgradeContainer == null) return;
        if (upgradeContainer.baseUpgrades.isEmpty()) return;

        tooltip.add(Text.translatable("info.serverbackpacks.upgrades")
                .append(":")
                .formatted(Formatting.GRAY)
        );

        for (Upgrade upgrade : upgradeContainer.baseUpgrades) {
            tooltip.add(Text.literal(" ")
                    .append(upgrade.getType().getTranslationKey())
                    .formatted(Formatting.DARK_GREEN)
            );
        }
    }

    public static void addInventoryTooltip(List<Text> tooltip, ItemStack stack) {
        DefaultedList<ItemStack> itemList = BackpackUtils.getItemList(stack);
        if (itemList.isEmpty()) return;

        tooltip.add(Text.translatable("info.serverbackpacks.contains")
                .append(":")
                .formatted(Formatting.GRAY)
        );

        int capacityMaxShow = 0;
        int capacityAmount = 0;

        for (ItemStack itemStack : itemList) {
            if (itemStack.isEmpty()) continue;

            capacityAmount++;
            if (capacityMaxShow > 4) continue;

            capacityMaxShow++;
            tooltip.add(Text.literal(" ")
                    .append(Text.translatable(
                            "item.container.item_count",
                            itemStack.getName(),
                            itemStack.getCount()
                    )).formatted(Formatting.DARK_AQUA)
            );
        }

        if (capacityAmount - capacityMaxShow > 0) tooltip.add(
                Text.translatable("item.container.more_items", capacityAmount - capacityMaxShow)
                    .formatted(Formatting.ITALIC).formatted(Formatting.DARK_AQUA));
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerWorld world, Entity entity, @Nullable EquipmentSlot slot) {
        if (entity instanceof ServerPlayerEntity player) {
            UpgradeContainerComponent component = stack.get(BackpackDataComponentTypes.UPGRADE_CONTAINER);
            if (component != null) component.baseUpgrades.forEach((upgrade) ->
                    upgrade.tick(player, (BackpackInventory) getInventory(player, stack))
            );
        }
    }

    @Override
    protected boolean postPlacement(BlockPos pos, World world, @Nullable PlayerEntity player, ItemStack stack, BlockState state) {
        if (world.getBlockEntity(pos) instanceof BackpackBlockEntity blockEntity) {
            BackpackUtils.resizeIfIncorrectSize((ServerPlayerEntity) player, stack, this.slots);

            blockEntity.setItem(this);
            blockEntity.setSize(this.slots);
            blockEntity.setExtraSize(BackpackUtils.getExtendedSlots(stack));

            UpgradeContainerComponent component = stack.get(BackpackDataComponentTypes.UPGRADE_CONTAINER);
            if (component != null) {
                blockEntity.setUpgradeList(component.baseUpgrades);
            }

            UUID uuid = BackpackManager.getStackUUID(stack);
            if (uuid == null) {
                uuid = BackpackManager.createNewUUID(stack);
            }

            blockEntity.setUuid(uuid);
            blockEntity.setStorage();

            if (stack.getCustomName() != null) {
                blockEntity.setCustomName(stack.getCustomName());
            }

            blockEntity.markDirty();

            ContainerItem.playOpenSound((ServerPlayerEntity) player);
        }

        return writeNbtToBlockEntity(world, player, pos, stack);
    }

    @Override
    public Inventory getInventory(@Nullable ServerPlayerEntity player, @Nullable ItemStack stack) {
        return stack != null ? BackpackManager.getInventory(BackpackManager.getStackUUID(stack)) : null;
    }

    @Override
    public void openGui(ServerPlayerEntity player, ItemStack stack) {
        BackpackManager.createNewUUID(stack);
        BackpackUtils.resizeIfIncorrectSize(player, stack, this.slots);

        Optional<BackpackInstance> instance = BackpackManager.getInstance(
                BackpackManager.getStackUUID(stack),
                this.slots + BackpackUtils.getExtendedSlots(stack));
        instance.ifPresent(backpackInstance -> new BackpackGui(player, stack, backpackInstance));
    }

    @Override
    public void afterChanged(ItemStack stack, Inventory inventory) {
        UUID uuid = BackpackManager.getStackUUID(stack);
        if (uuid != null) BackpackManager.addBackpack(uuid, (BackpackInventory) inventory);
    }

    public DefaultedList<ItemStack> getComponentItemList(ItemStack stack) {
        DefaultedList<ItemStack> list = DefaultedList.ofSize(this.slots, ItemStack.EMPTY);
        stack.getOrDefault(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT).copyTo(list);
        return list;
    }

    public static void playOpenSound(ServerPlayerEntity player) {
        player.playSoundToPlayer(SoundEvents.ITEM_BUNDLE_DROP_CONTENTS, SoundCategory.PLAYERS, 0.8F, 0.8F + player.getWorld().getRandom().nextFloat() * 0.4F);
        player.playSoundToPlayer(SoundEvents.ITEM_BUNDLE_INSERT, SoundCategory.PLAYERS, 0.8F, 0.8F + player.getWorld().getRandom().nextFloat() * 0.4F);
    }

    public static void playInsertSound(ServerPlayerEntity player, float pitch) {
        player.playSoundToPlayer(SoundEvents.ITEM_BUNDLE_INSERT, SoundCategory.PLAYERS, 0.8F, pitch + player.getWorld().getRandom().nextFloat() * 0.4F);
    }

    public static void playDropContentsSound(ServerPlayerEntity player, float pitch) {
        player.playSoundToPlayer(SoundEvents.ITEM_BUNDLE_DROP_CONTENTS, SoundCategory.PLAYERS, 0.8F, pitch + player.getWorld().getRandom().nextFloat() * 0.4F);
    }

    public static void playInsertFailSound(ServerPlayerEntity player) {
        player.playSoundToPlayer(SoundEvents.ITEM_BUNDLE_INSERT_FAIL, SoundCategory.PLAYERS, 1.0F, 1.0F);
    }

}

