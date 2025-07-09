package com.rouesvm.servback.content.upgrade;

import com.rouesvm.servback.technical.ui.inventory.BackpackInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;

public interface FunctionalUpgrade {
    default boolean onUsed(World world, ServerPlayerEntity player, ItemStack stack) {
        return false;
    }

    default void readView(ReadView data) {}
    default void writeView(WriteView data) {}

    default void tick(ServerPlayerEntity player, BackpackInventory inventory) {}

    default void addTooltip(List<Text> tooltip, ItemStack stack, PacketContext context) {}
}
