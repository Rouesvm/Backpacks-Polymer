package com.rouesvm.servback.compat.trinkets;

import com.rouesvm.servback.content.component.UpgradeContainerComponent;
import com.rouesvm.servback.content.item.BundleGuiItem;
import com.rouesvm.servback.registry.BackpackDataComponentTypes;
import com.rouesvm.servback.technical.config.Configuration;
import com.rouesvm.servback.technical.cosmetic.BackHolder;
import com.rouesvm.servback.technical.cosmetic.CosmeticManager;
import com.rouesvm.servback.technical.manager.BackpackManager;
import com.rouesvm.servback.technical.manager.BackpackUUID;
import eu.pb4.trinkets.api.TrinketAttachment;
import eu.pb4.trinkets.api.TrinketInventory;
import eu.pb4.trinkets.api.TrinketSlotAccess;
import eu.pb4.trinkets.api.TrinketsApi;
import eu.pb4.trinkets.api.callback.TrinketCallback;
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

public class BackpackTrinket implements TrinketCallback {
    public static void initialize() {
        UseBlockCallback.EVENT.register(BackpackTrinket::tryPlaceBackpack);
        BuiltInRegistries.ITEM.stream()
               .filter(item -> item instanceof BundleGuiItem)
               .forEach(item -> TrinketCallback.setCallback(item, new BackpackTrinket()));
    }

    public void tick(ItemStack stack, TrinketSlotAccess slot, LivingEntity entity) {
        if (!Configuration.instance().display_back) return;

        if (entity instanceof ServerPlayer player) {
            CosmeticManager manager = CosmeticManager.manager();
            if (!manager.hasInstance(player)) manager.getOrCreateInstance(player, stack);

            UUID uuid = BackpackUUID.getStackUUID(stack);

            if (uuid == null) return;
            UpgradeContainerComponent component = stack.get(BackpackDataComponentTypes.UPGRADE_CONTAINER);
            if (component != null) component.baseUpgrades().forEach((upgrade) ->
                    upgrade.tick(player, stack, player.level(), player.position(), BackpackManager.getInventory(uuid)));
        }
    }

    public void onEquip(ItemStack stack, TrinketSlotAccess slot, LivingEntity entity) {
        if (!Configuration.instance().display_back) return;

        if (entity instanceof ServerPlayer player) {
            CosmeticManager.manager().getOrCreateInstance(player, stack);
        }
    }

    public void onUnequip(ItemStack stack, TrinketSlotAccess slot, LivingEntity entity) {
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
        TrinketAttachment attachment = TrinketsApi.getAttachment(player);
        TrinketInventory inventory = attachment.getInventory("chest/back");

        if (inventory == null) return;

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack trinket = inventory.getItem(i);
            if (trinket.isEmpty()) {
                inventory.setItem(i, stack);
                break;
            }
        }
    }

    public static boolean isBackSlotOccupied(Player player) {
        TrinketAttachment attachment = TrinketsApi.getAttachment(player);
        TrinketInventory inventory = attachment.getInventory("chest/back");

        if (inventory == null) return false;

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty()) return true;
        }

        return false;
    }

    public static ItemStack getStackInBackSlot(Player player) {
        TrinketAttachment attachment = TrinketsApi.getAttachment(player);
        TrinketInventory inventory = attachment.getInventory("chest/back");

        if (inventory == null) return ItemStack.EMPTY;

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty()) return stack;
        }

        return ItemStack.EMPTY;
    }
}
