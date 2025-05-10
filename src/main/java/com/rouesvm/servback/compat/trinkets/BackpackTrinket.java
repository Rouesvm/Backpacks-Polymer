package com.rouesvm.servback.compat.trinkets;

import com.rouesvm.servback.Main;
import com.rouesvm.servback.item.ContainerItem;
import com.rouesvm.servback.utils.cosmetic.BackHolder;
import com.rouesvm.servback.utils.cosmetic.CosmeticManager;
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
}
