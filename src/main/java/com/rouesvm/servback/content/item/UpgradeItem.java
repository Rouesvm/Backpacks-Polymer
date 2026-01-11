package com.rouesvm.servback.content.item;

import com.rouesvm.servback.ServerBackpacks;
import com.rouesvm.servback.compat.geyser.bedrock.BedrockItem;
import com.rouesvm.servback.content.component.UpgradeComponent;
import com.rouesvm.servback.content.upgrade.ClickableUpgrade;
import com.rouesvm.servback.content.upgrade.Upgrade;
import com.rouesvm.servback.content.upgrade.UpgradeType;
import com.rouesvm.servback.registry.BackpackDataComponentTypes;
import com.rouesvm.servback.technical.config.Configuration;
import eu.pb4.polymer.core.api.item.SimplePolymerItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;

public class UpgradeItem extends SimplePolymerItem implements BedrockItem {
    private final UpgradeType<? extends Upgrade> upgradeType;

    public UpgradeItem(Properties settings, UpgradeType<? extends Upgrade> upgradeType) {
        super(settings.setId(ResourceKey.create(Registries.ITEM, upgradeType.getId().withSuffix("_upgrade"))),
                Items.POISONOUS_POTATO, true
        );
        this.upgradeType = upgradeType;
    }

    @Override
    public Item getPolymerItem(ItemStack stack, PacketContext context) {
        if (ServerBackpacks.isBedrock(context.getPlayer()))
            return this;
        else return super.getPolymerItem(stack, context);
    }

    @Override
    public void modifyClientTooltip(List<Component> tooltip, ItemStack stack, PacketContext context) {
        UpgradeItem upgradeItem = (UpgradeItem) stack.getItem();
        Upgrade upgrade = upgradeItem.getUpgrade(stack);
        if (upgrade != null) upgrade.addTooltip(tooltip, stack, context);

        if (Configuration.isDisabled(stack.getItem())
        ) tooltip.add(Component.translatable("tooltip.serverbackpacks.disabled")
                .withStyle(ChatFormatting.BOLD)
                .withStyle(ChatFormatting.RED));
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack otherStack, Slot slot, ClickAction clickType, Player player, SlotAccess cursorStackReference) {
        UpgradeItem upgradeItem = (UpgradeItem) stack.getItem();

        Upgrade upgrade = upgradeItem.getUpgrade(stack);

        if (upgrade instanceof ClickableUpgrade clickableUpgrade) {
            if (cursorStackReference.get().isEmpty()) cursorStackReference.set(ItemStack.EMPTY);
            return clickableUpgrade.onClicked((ServerPlayer) player, stack, slot, clickType, false);
        }

        return false;
    }

    @Override
    public InteractionResult use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        UpgradeItem upgradeItem = (UpgradeItem) stack.getItem();

        Upgrade upgrade = upgradeItem.getUpgrade(stack);

        boolean successful = false;
        if (upgrade != null) successful = upgrade.onUsed(world, (ServerPlayer) player, stack);

        if (successful) {
            CustomData component = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
            CompoundTag compound = component.copyTag();
            compound.putBoolean("update", !compound.getBooleanOr("update", false));

            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(compound));

            return InteractionResult.SUCCESS_SERVER;
        } else return super.use(world, player, hand);
    }

    public Upgrade getUpgrade(ItemStack stack) {
        UpgradeComponent component = UpgradeComponent.of(upgradeType.create());
        UpgradeComponent stackComponent = stack.getOrDefault(BackpackDataComponentTypes.UPGRADE, component);
        if (stack.get(BackpackDataComponentTypes.UPGRADE) == null) {
            stack.set(BackpackDataComponentTypes.UPGRADE, stackComponent);
        }
        return stackComponent.upgrade();
    }
}
