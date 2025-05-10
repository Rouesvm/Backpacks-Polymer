package com.rouesvm.servback.compat.trinkets;

import com.rouesvm.servback.Main;
import com.rouesvm.servback.item.ContainerItem;
import com.rouesvm.servback.utils.cosmetic.BackHolder;
import com.rouesvm.servback.utils.cosmetic.CosmeticManager;
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

import java.util.Map;
import java.util.Optional;

public class BackpackTrinket implements Trinket {
    public static void initialize() {
        UseBlockCallback.EVENT.register(BackpackTrinket::tryPlaceBackpack);
        Registries.ITEM.stream()
                .filter(item -> item instanceof ContainerItem)
                .forEach(item -> TrinketsApi.registerTrinket(item, new BackpackTrinket()));
    }


    @Override
    public void tick(ItemStack stack, SlotReference slot, LivingEntity entity) {
        if (!Main.configuration.getInstance().display_back) return;

        if (entity instanceof ServerPlayerEntity player) {
            if (CosmeticManager.getManager().getInstance(player) == null)
                CosmeticManager.getManager().getOrCreateInstance(player, stack);
        }
    }

    @Override
    public void onEquip(ItemStack stack, SlotReference slot, LivingEntity entity) {
        if (!Main.configuration.getInstance().display_back) return;

        if (entity instanceof ServerPlayerEntity player) {
            CosmeticManager.getManager().getOrCreateInstance(player, stack);
        }
    }

    @Override
    public void onUnequip(ItemStack stack, SlotReference slot, LivingEntity entity) {
        if (!Main.configuration.getInstance().display_back) return;

        if (entity instanceof ServerPlayerEntity player) {
            BackHolder holder = CosmeticManager.getManager().getOrCreateInstance(player, stack);
            holder.destroy();
            CosmeticManager.getManager().removeInstance(player);
        }
    }

    public static ActionResult tryPlaceBackpack(PlayerEntity player, World world, Hand hand, BlockHitResult blockHitResult) {
        if (!world.isClient) {
            ItemStack stack = getStackInBackSlot(player);
            if (!stack.isEmpty() && player.isSneaking() && player.getMainHandStack().isEmpty() && player.getOffHandStack().isEmpty()) {
                ContainerItem item = (ContainerItem) stack.getItem();
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

    public static boolean hasStackInBackSlot(PlayerEntity player) {
        Optional<TrinketComponent> optional = TrinketsApi.getTrinketComponent(player);
        if (optional.isPresent()) {
            ItemStack stack = getStackInBackSlot(player);
            return !stack.isEmpty();
        }

        return false;
    }

    public static ItemStack getStackInBackSlot(PlayerEntity player) {
        Optional<TrinketComponent> optional = TrinketsApi.getTrinketComponent(player);
        if (optional.isPresent()) {
            TrinketComponent component = optional.get();
            for(Map<String, TrinketInventory> group : component.getInventory().values()) {
                for(TrinketInventory inv : group.values()) {
                    for(int i = 0; i < inv.size(); ++i) {
                        ItemStack stack = inv.getStack(i);
                        if (!stack.isEmpty() && stack.getItem() instanceof ContainerItem) {
                            return stack;
                        }
                    }
                }
            }
        }

        return ItemStack.EMPTY;
    }
}
