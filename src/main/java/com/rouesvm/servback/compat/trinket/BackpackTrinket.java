package com.rouesvm.servback.compat.trinket;

import com.rouesvm.servback.items.ContainerItem;
import dev.emi.trinkets.api.Trinket;
import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.registry.Registries;

public class BackpackTrinket implements Trinket {
    public static void initialize() {
        Registries.ITEM.stream()
                .filter(item -> item instanceof ContainerItem)
                .forEach(item -> TrinketsApi.registerTrinket(item, new BackpackTrinket()));
    }

    //**
    //     @Override
    //    public void tick(ItemStack stack, SlotReference slot, LivingEntity entity) {
    //        if (true) {
    //            ContainerItem item = (ContainerItem) stack.getItem();
    //            Direction facing = player.getFacing();
    //            if (facing == Direction.UP
    //                    && player.isSneaking()
    //                    && player.isOnGround()
    //                    && !stack.getOrDefault(BackpackDataComponentTypes.BOOLEAN_TYPE, false)
    //            ) {
    //                item.openGui(player, stack);
    //            }
    //        }
    //    }
    // **
}
