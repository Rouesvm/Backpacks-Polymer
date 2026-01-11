package com.rouesvm.servback.compat.trinkets;

import com.rouesvm.servback.content.component.UpgradeContainerComponent;
import com.rouesvm.servback.content.item.BundleGuiItem;
import com.rouesvm.servback.registry.BackpackDataComponentTypes;
import com.rouesvm.servback.technical.config.Configuration;
import com.rouesvm.servback.technical.cosmetic.BackHolder;
import com.rouesvm.servback.technical.cosmetic.CosmeticManager;
import com.rouesvm.servback.technical.manager.BackpackManager;
import com.rouesvm.servback.technical.manager.BackpackUUID;
import dev.emi.trinkets.api.*;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

import java.util.UUID;

public class BackpackTrinket implements Trinket {
    public static void initialize() {
        UseBlockCallback.EVENT.register(BackpackTrinket::tryPlaceBackpack);
        BuiltInRegistries.ITEM.stream()
                .filter(item -> item instanceof BundleGuiItem)
                .forEach(item -> TrinketsApi.registerTrinket(item, new BackpackTrinket()));
    }

    @Override
    public void tick(ItemStack stack, SlotReference slot, LivingEntity entity) {
        if (!Configuration.instance().display_back) return;

        if (entity instanceof ServerPlayer player) {
            CosmeticManager manager = CosmeticManager.manager();
            if (!manager.hasInstance(player)) manager.getOrCreateInstance(player, stack);

            UUID uuid = BackpackUUID.getStackUUID(stack);

            if (uuid == null) return;
            UpgradeContainerComponent component = stack.get(BackpackDataComponentTypes.UPGRADE_CONTAINER);
            if (component != null) component.baseUpgrades().forEach((upgrade) ->
                    upgrade.tick(player.level(), player.position(), BackpackManager.getInventory(uuid))
            );
        }
    }

    @Override
    public void onEquip(ItemStack stack, SlotReference slot, LivingEntity entity) {
        if (!Configuration.instance().display_back) return;

        if (entity instanceof ServerPlayer player) {
            CosmeticManager.manager().getOrCreateInstance(player, stack);
        }
    }

    @Override
    public void onUnequip(ItemStack stack, SlotReference slot, LivingEntity entity) {
        if (!Configuration.instance().display_back) return;

        if (entity instanceof ServerPlayer player) {
            CosmeticManager manager = CosmeticManager.manager();

            BackHolder holder = manager.getOrCreateInstance(player, stack);
            holder.destroy();
            manager.removeInstance(player);
        }
    }

    public static InteractionResult tryPlaceBackpack(Player player, Level world, InteractionHand hand, BlockHitResult blockHitResult) {
        if (!world.isClientSide()) {
            if (!Configuration.instance().placeable) return InteractionResult.PASS;

            ItemStack stack = getStackInBackSlot(player);
            if (!stack.isEmpty()
                    && player.isShiftKeyDown()
                    && player.getMainHandItem().isEmpty()
                    && player.getOffhandItem().isEmpty())
            {
                BundleGuiItem item = (BundleGuiItem) stack.getItem();
                BlockPlaceContext context = new BlockPlaceContext(player, hand, stack, blockHitResult);
                item.place(context);
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    public static void equipStack(Player player, ItemStack stack) {
        TrinketItem.equipItem(player, stack);
    }

    public static boolean isBackSlotOccupied(Player player) {
        return !getStackInBackSlot(player).isEmpty();
    }

    private static ItemStack getStackInBackSlot(Player player) {
        return TrinketsApi.getTrinketComponent(player)
                .map(BackpackTrinket::findBundleItem)
                .orElse(ItemStack.EMPTY);
    }

    private static ItemStack findBundleItem(TrinketComponent component) {
        for (var group : component.getInventory().values()) {
            for (var inv : group.values()) {
                for (int i = 0; i < inv.getContainerSize(); i++) {
                    ItemStack stack = inv.getItem(i);
                    if (!stack.isEmpty() && stack.getItem() instanceof BundleGuiItem) {
                        return stack;
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }
}
