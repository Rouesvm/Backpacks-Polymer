package com.rouesvm.servback.compat.trinkets;

import com.rouesvm.servback.items.ContainerItem;
import com.rouesvm.servback.utils.cosmetic.BackHolder;
import com.rouesvm.servback.utils.cosmetic.StupidManager;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.Trinket;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;

public class BackpackTrinket implements Trinket {
    public static void initialize() {
        Registries.ITEM.stream()
                .filter(item -> item instanceof ContainerItem)
                .forEach(item -> TrinketsApi.registerTrinket(item, new BackpackTrinket()));
    }

    @Override
    public void onEquip(ItemStack stack, SlotReference slot, LivingEntity entity) {
        if (entity instanceof ServerPlayerEntity player) {
            StupidManager.getManager().getOrCreateInstance(player, stack);
        }
    }

    @Override
    public void onUnequip(ItemStack stack, SlotReference slot, LivingEntity entity) {
        if (entity instanceof ServerPlayerEntity player) {
            BackHolder holder = StupidManager.getManager().getOrCreateInstance(player, stack);
            holder.destroy();
            StupidManager.getManager().removeInstance(player);
        }
    }
}
