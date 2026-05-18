package com.rouesvm.servback.content.item;

import com.rouesvm.servback.content.component.UpgradeComponent;
import com.rouesvm.servback.content.upgrade.ClickableUpgrade;
import com.rouesvm.servback.content.upgrade.Upgrade;
import com.rouesvm.servback.content.upgrade.UpgradeType;
import com.rouesvm.servback.registry.BackpackDataComponentTypes;
import com.rouesvm.servback.technical.config.Configuration;
import eu.pb4.polymer.core.api.item.SimplePolymerItem;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ClickType;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;

public class UpgradeItem extends SimplePolymerItem {
    private final UpgradeType<? extends Upgrade> upgradeType;
    private final PolymerModelData customModelId;

    public UpgradeItem(Settings settings, UpgradeType<? extends Upgrade> upgradeType) {
        super(settings,
                Items.POISONOUS_POTATO);
        this.upgradeType = upgradeType;
        this.customModelId = PolymerResourcePackUtils.requestModel(Items.POISONOUS_POTATO,  upgradeType.getId().withSuffixedPath("_upgrade").withPrefixedPath("item/"));
    }

    @Override
    public int getPolymerCustomModelData(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return this.customModelId.value();
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return this.customModelId.item();
    }

    @Override
    public void modifyClientTooltip(List<Text> tooltip, ItemStack stack, @Nullable ServerPlayerEntity player) {
        UpgradeItem upgradeItem = (UpgradeItem) stack.getItem();
        Upgrade upgrade = upgradeItem.getUpgrade(stack);
        if (upgrade != null) upgrade.addTooltip(tooltip, stack, PacketContext.of(player));

        if (Configuration.isDisabled(stack.getItem())
        ) tooltip.add(Text.translatable("tooltip.serverbackpacks.disabled")
                .formatted(Formatting.BOLD)
                .formatted(Formatting.RED));
    }

    @Override
    public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
        UpgradeItem upgradeItem = (UpgradeItem) stack.getItem();

        Upgrade upgrade = upgradeItem.getUpgrade(stack);

        if (upgrade instanceof ClickableUpgrade clickableUpgrade) {
            if (cursorStackReference.get().isEmpty()) cursorStackReference.set(ItemStack.EMPTY);
            return clickableUpgrade.onClicked((ServerPlayerEntity) player, stack, slot, clickType);
        }

        return false;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        UpgradeItem upgradeItem = (UpgradeItem) stack.getItem();

        Upgrade upgrade = upgradeItem.getUpgrade(stack);

        boolean successful = false;
        if (upgrade != null) successful = upgrade.onUsed(world, (ServerPlayerEntity) player, stack);

        if (successful) {
            NbtComponent component = stack.getOrDefault(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT);
            NbtCompound compound = component.copyNbt();

            boolean update = !compound.getBoolean("update");
            compound.putBoolean("update", update);

            stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(compound));

            return TypedActionResult.success(stack, true);
        } else return super.use(world, player, hand);
    }

    public Upgrade getUpgrade(ItemStack stack) {
        UpgradeComponent component = UpgradeComponent.of(upgradeType.create());
        UpgradeComponent stackComponent = stack.getOrDefault(BackpackDataComponentTypes.UPGRADE, component);
        stack.set(BackpackDataComponentTypes.UPGRADE, stackComponent);
        return stackComponent.upgrade();
    }
}
