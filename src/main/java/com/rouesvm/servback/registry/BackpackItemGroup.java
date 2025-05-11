package com.rouesvm.servback.registry;

import com.rouesvm.servback.Main;
import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import net.minecraft.item.ItemGroup.Entries;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

public class BackpackItemGroup {
    public static void addItems(Entries entries) {
        BackpackItemRegistry.SMALL.forEach((color, item) -> {
            if (color == DyeColor.LIGHT_GRAY) return;
            entries.add(item);
        });
        BackpackItemRegistry.MEDIUM.forEach((color, item) -> {
            if (color == DyeColor.LIGHT_GRAY) return;
            entries.add(item);
        });
        BackpackItemRegistry.LARGE.forEach((color, item) -> {
            if (color == DyeColor.LIGHT_GRAY) return;
            entries.add(item);
        });

        entries.add(BackpackItemRegistry.GLOBAL_BACKPACK);
        entries.add(BackpackItemRegistry.ENDER_BACKPACK);
    }

    public static void initialize() {
        PolymerItemGroupUtils.registerPolymerItemGroup(Identifier.of(Main.MOD_ID + "items"), PolymerItemGroupUtils.builder()
                .icon(() -> new ItemStack(BackpackItemRegistry.ENDER_BACKPACK))
                .displayName(Text.translatable("item.serverbackpacks.gui_backpacks"))
                .entries(((context, entries) -> addItems(entries))).build()
        );
    }
}
