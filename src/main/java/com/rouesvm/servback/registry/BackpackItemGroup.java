package com.rouesvm.servback.registry;

import com.rouesvm.servback.Main;
import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import net.minecraft.item.ItemGroup.Entries;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

public class BackpackItemGroup {
    public static DyeColor[] dyeColors = {
            DyeColor.BROWN,
            DyeColor.BLACK,
            DyeColor.LIGHT_GRAY,
            DyeColor.LIGHT_BLUE,
            DyeColor.BLUE,
            DyeColor.CYAN,
            DyeColor.PURPLE,
            DyeColor.MAGENTA,
            DyeColor.PINK,
            DyeColor.RED,
            DyeColor.ORANGE,
            DyeColor.YELLOW,
            DyeColor.GREEN,
            DyeColor.LIME,
            DyeColor.WHITE
    };

    public static void addItems(Entries entries) {
        for (DyeColor color : dyeColors) {
            entries.add(BackpackItemRegistry.getBackpack(color, 1));
            entries.add(BackpackItemRegistry.getBackpack(color, 2));
            entries.add(BackpackItemRegistry.getBackpack(color, 3));
        }

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
