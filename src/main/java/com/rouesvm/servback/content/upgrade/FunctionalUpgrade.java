package com.rouesvm.servback.content.upgrade;

import com.rouesvm.servback.technical.ui.inventory.BackpackInventory;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;

import java.util.List;

public interface FunctionalUpgrade {
    default SimpleGui open() {
        return null;
    }

    default void readView(ReadView data) {}
    default void writeView(WriteView data) {}

    default void tick(ServerPlayerEntity player, BackpackInventory inventory) {};

    default void addTooltip(List<Text> tooltip, ItemStack stack) {}
}
