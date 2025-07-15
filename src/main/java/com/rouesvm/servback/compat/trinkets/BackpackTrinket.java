package com.rouesvm.servback.compat.trinkets;

import com.rouesvm.servback.content.component.UpgradeContainerComponent;
import com.rouesvm.servback.content.item.BundleGuiItem;
import com.rouesvm.servback.registry.BackpackDataComponentTypes;
import com.rouesvm.servback.technical.config.Configuration;
import com.rouesvm.servback.technical.cosmetic.BackHolder;
import com.rouesvm.servback.technical.cosmetic.CosmeticManager;
import com.rouesvm.servback.technical.data.BackpackManager;
import dev.emi.trinkets.api.*;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.World;

import java.util.UUID;

public class BackpackTrinket implements Trinket {
    public static void initialize() {
        UseBlockCallback.EVENT.register(BackpackTrinket::tryPlaceBackpack);
        Registries.ITEM.stream()
                .filter(item -> item instanceof BundleGuiItem)
                .forEach(item -> TrinketsApi.registerTrinket(item, new BackpackTrinket()));
    }

    @Override
    public void tick(ItemStack stack, SlotReference slot, LivingEntity entity) {
        if (!Configuration.instance().display_back) return;

        if (entity instanceof ServerPlayerEntity player) {
            CosmeticManager manager = CosmeticManager.manager();
            if (!manager.hasInstance(player)) manager.getOrCreateInstance(player, stack);

            UUID uuid = BackpackManager.getStackUUID(stack);

            if (uuid == null) return;
            UpgradeContainerComponent component = stack.get(BackpackDataComponentTypes.UPGRADE_CONTAINER);
            if (component != null) component.getBaseUpgrades().forEach((upgrade) ->
                    upgrade.tick(player.getWorld(), player.getBlockPos(), BackpackManager.getInventory(uuid))
            );
        }
    }

    @Override
    public void onEquip(ItemStack stack, SlotReference slot, LivingEntity entity) {
        if (!Configuration.instance().display_back) return;

        if (entity instanceof ServerPlayerEntity player) {
            CosmeticManager.manager().getOrCreateInstance(player, stack);
        }
    }

    @Override
    public void onUnequip(ItemStack stack, SlotReference slot, LivingEntity entity) {
        if (!Configuration.instance().display_back) return;

        if (entity instanceof ServerPlayerEntity player) {
            CosmeticManager manager = CosmeticManager.manager();

            BackHolder holder = manager.getOrCreateInstance(player, stack);
            holder.destroy();
            manager.removeInstance(player);
        }
    }

    public static ActionResult tryPlaceBackpack(PlayerEntity player, World world, Hand hand, BlockHitResult blockHitResult) {
        if (!world.isClient) {
            ItemStack stack = getStackInBackSlot(player);
            if (!stack.isEmpty()
                    && player.isSneaking()
                    && player.getMainHandStack().isEmpty()
                    && player.getOffHandStack().isEmpty())
            {
                BundleGuiItem item = (BundleGuiItem) stack.getItem();
                ItemPlacementContext context = new ItemPlacementContext(player, hand, stack, blockHitResult);
                item.place(context);
                return ActionResult.SUCCESS;
            }
        }

        return ActionResult.PASS;
    }

    public static void equipStack(PlayerEntity player, ItemStack stack) {
        TrinketItem.equipItem(player, stack);
    }

    public static boolean isBackSlotOccupied(PlayerEntity player) {
        return !getStackInBackSlot(player).isEmpty();
    }

    private static ItemStack getStackInBackSlot(PlayerEntity player) {
        return TrinketsApi.getTrinketComponent(player)
                .map(BackpackTrinket::findBundleItem)
                .orElse(ItemStack.EMPTY);
    }

    private static ItemStack findBundleItem(TrinketComponent component) {
        for (var group : component.getInventory().values()) {
            for (var inv : group.values()) {
                for (int i = 0; i < inv.size(); i++) {
                    ItemStack stack = inv.getStack(i);
                    if (!stack.isEmpty() && stack.getItem() instanceof BundleGuiItem) {
                        return stack;
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }
}
