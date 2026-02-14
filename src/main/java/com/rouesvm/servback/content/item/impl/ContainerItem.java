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
import com.rouesvm.servback.technical.manager.BackpackManager;
import com.rouesvm.servback.technical.manager.BackpackUUID;
import com.rouesvm.servback.technical.ui.BackpackGui;
import com.rouesvm.servback.technical.ui.inventory.BackpackInventory;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ContainerItem extends BundleGuiItem {
    public final int slots;

    public ContainerItem(String name, int slots, Block block) {
        super(name, block);
        this.slots = slots;
    }

    public ContainerItem(String name, int slots) {
        this(name, slots, BackpackBlockRegistry.BACKPACK);
    }

    public int getSize() {
        return slots;
    }

    @Override
    public boolean canFitInsideContainerItems() {
        return false;
    }

    @Override
    public void modifyClientTooltip(List<Component> tooltip, ItemStack polymerStack, PacketContext context) {
        UUID uuid = polymerStack.get(BackpackDataComponentTypes.BACKPACK_UUID);
        if (ServerBackpacks.isDevEnvironment
        && uuid != null) tooltip.add(Component.nullToEmpty("UUID: " + BackpackUUID.getStackUUID(polymerStack)));

        addUpgradeTooltip(tooltip, polymerStack);
        addInventoryTooltip(tooltip, polymerStack);

        super.modifyClientTooltip(tooltip, polymerStack, context);
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel world, Entity entity, @Nullable EquipmentSlot slot) {
        if (entity instanceof ServerPlayer player) {
            UpgradeContainerComponent component = stack.get(BackpackDataComponentTypes.UPGRADE_CONTAINER);
            if (component != null) component.baseUpgrades().forEach((upgrade) -> {
                upgrade.tick(player.level(), player.position(), (BackpackInventory) getInventory(player, stack));
                upgrade.tick(player, (BackpackInventory) getInventory(player, stack));
            });
        }
    }

    @Override
    protected boolean updateCustomBlockEntityTag(BlockPos pos, Level world, @Nullable Player player, ItemStack stack, BlockState state) {
        if (world.getBlockEntity(pos) instanceof BackpackBlockEntity blockEntity) {
            BackpackUtils.resizeIfIncorrectSize((ServerPlayer) player, stack, this.slots);

            blockEntity.setItem(this);
            blockEntity.setSize(this.slots);
            blockEntity.setExtraSize(BackpackUtils.getExtendedSlots(stack));

            UpgradeContainerComponent component = stack.get(BackpackDataComponentTypes.UPGRADE_CONTAINER);
            if (component != null
            ) blockEntity.setUpgradeList(component.baseUpgrades());

            UUID uuid = BackpackUUID.getUUIDOrCreateNew(stack);

            blockEntity.setUuid(uuid);
            blockEntity.setStorage();

            if (stack.getCustomName() != null
            ) blockEntity.setCustomName(stack.getCustomName());

            blockEntity.setChanged();

            ContainerItem.playOpenSound((ServerPlayer) player);
        }

        return updateCustomBlockEntityTag(world, player, pos, stack);
    }

    @Override
    public Container getInventory(@Nullable ServerPlayer player, @Nullable ItemStack stack) {
        return stack != null ? BackpackManager.getInventory(BackpackUUID.getStackUUID(stack)) : null;
    }

    @Override
    public void openGui(ServerPlayer player, ItemStack stack) {
        BackpackUUID.getUUIDOrCreateNew(stack);
        BackpackUtils.resizeIfIncorrectSize(player, stack, this.slots);

        Optional<BackpackInstance> instance = BackpackManager.getInstanceAndResize(
                BackpackUUID.getStackUUID(stack),
                this.slots + BackpackUtils.getExtendedSlots(stack));

        instance.ifPresent(backpackInstance -> new BackpackGui(player, stack, backpackInstance));
    }

    @Override
    public void afterChanged(ItemStack stack, Container inventory) {
        UUID uuid = BackpackUUID.getStackUUID(stack);
        if (uuid != null) BackpackManager.addBackpack(uuid, (BackpackInventory) inventory);
    }

    public NonNullList<ItemStack> getComponentItemList(ItemStack stack) {
        NonNullList<ItemStack> list = NonNullList.withSize(this.slots, ItemStack.EMPTY);
        stack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(list);
        return list;
    }

    public static void addUpgradeTooltip(List<Component> tooltip, ItemStack stack) {
        UpgradeContainerComponent upgradeContainer = stack.get(BackpackDataComponentTypes.UPGRADE_CONTAINER);
        if (upgradeContainer == null) return;
        if (upgradeContainer.baseUpgrades().isEmpty()) return;

        tooltip.add(Component.translatable("info.serverbackpacks.upgrades")
                .append(":")
                .withStyle(ChatFormatting.GRAY)
        );

        for (Upgrade upgrade : upgradeContainer.baseUpgrades()) {
            tooltip.add(Component.literal(" ")
                    .append(upgrade.getType().getTranslationKey())
                    .withStyle(ChatFormatting.DARK_GREEN)
            );
        }
    }

    public static void addInventoryTooltip(List<Component> tooltip, ItemStack stack) {
        NonNullList<ItemStack> itemList = BackpackUtils.getItemList(stack);
        if (itemList.isEmpty()) return;

        tooltip.add(Component.translatable("info.serverbackpacks.contains")
                .append(":")
                .withStyle(ChatFormatting.GRAY)
        );

        int capacityMaxShow = 0;
        int capacityAmount = 0;

        for (ItemStack itemStack : itemList) {
            if (itemStack.isEmpty()) continue;

            capacityAmount++;
            if (capacityMaxShow > 4) continue;

            capacityMaxShow++;
            tooltip.add(Component.literal(" ")
                    .append(Component.translatable(
                            "item.container.item_count",
                            itemStack.getHoverName(),
                            itemStack.getCount()
                    )).withStyle(ChatFormatting.DARK_AQUA)
            );
        }

        if (capacityAmount - capacityMaxShow > 0) tooltip.add(
                Component.translatable("item.container.more_items", capacityAmount - capacityMaxShow)
                        .withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.DARK_AQUA));
    }

    public static void playOpenSound(ServerPlayer player) {
        player.level().playSound(null, player.blockPosition(), SoundEvents.BUNDLE_DROP_CONTENTS, SoundSource.UI, 0.8F, 0.8F
                + player.level().getRandom().nextFloat() * 0.4F);
        player.level().playSound(null, player.blockPosition(), SoundEvents.BUNDLE_INSERT, SoundSource.UI, 0.8F, 0.8F
                + player.level().getRandom().nextFloat() * 0.4F);
    }

    public static void playInsertSound(Level world, BlockPos pos, float pitch) {
        world.playSound(null, pos, SoundEvents.BUNDLE_INSERT, SoundSource.UI, 0.8F, pitch + world.getRandom().nextFloat() * 0.4F);
    }

    public static void playInsertSound(ServerPlayer player, float pitch) {
        playInsertSound(player.level(), player.blockPosition(), pitch);
    }

    public static void playDropContentsSound(ServerPlayer player, float pitch) {
        player.level().playSound(null, player.blockPosition(), SoundEvents.BUNDLE_DROP_CONTENTS, SoundSource.UI, 0.8F, pitch
                + player.level().getRandom().nextFloat() * 0.4F);
    }

    public static void playInsertFailSound(ServerPlayer player) {
        player.level().playSound(null, player.blockPosition(), SoundEvents.BUNDLE_DROP_CONTENTS, SoundSource.UI, 1.0F, 1.0F);
    }

}

