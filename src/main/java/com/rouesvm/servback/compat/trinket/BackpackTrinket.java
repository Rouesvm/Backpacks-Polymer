package com.rouesvm.servback.compat.trinket;

import com.rouesvm.servback.items.ContainerItem;
import com.rouesvm.servback.registry.DataComponentRegistry;
import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.Trinket;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Direction;

public class BackpackTrinket implements Trinket {
    public static void initialize() {
        Registries.ITEM.stream()
                .filter(item -> item instanceof ContainerItem)
                .forEach(item -> TrinketsApi.registerTrinket(item, new BackpackTrinket()));
    }

    @Override
    public void tick(ItemStack stack, SlotReference slot, LivingEntity entity) {
        if (entity instanceof ServerPlayerEntity player) {
            ContainerItem item = (ContainerItem) stack.getItem();
            Direction facing = player.getFacing();
            if (facing == Direction.UP
                    && player.isSneaking()
                    && player.isOnGround()
                    && !stack.getOrDefault(DataComponentRegistry.BOOLEAN_TYPE, false)
            ) {
                item.openGui(player, stack);
            }
        }
    }
}
