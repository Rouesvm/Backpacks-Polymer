package com.rouesvm.servback.items;

import com.rouesvm.servback.Main;
import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import net.minecraft.item.ItemGroup.*;

public class ModItemGroup {
    public static void addItems(Entries entries) {
        entries.add(ItemList.SMALL_BACKPACK);
        entries.add(ItemList.MEDIUM_BACKPACK);
        entries.add(ItemList.LARGE_BACKPACK);
        entries.add(ItemList.GLOBAL_BACKPACK);
        entries.add(ItemList.ENDER_BACKPACK);
    }

    public static void initialize() {
        PolymerItemGroupUtils.registerPolymerItemGroup(Identifier.of(Main.MOD_ID + "items"), PolymerItemGroupUtils.builder()
                .icon(() -> new ItemStack(ItemList.ENDER_BACKPACK))
                .displayName(Text.of("Backpacks"))
                .entries(((context, entries) -> addItems(entries))).build()
        );
    }
}
