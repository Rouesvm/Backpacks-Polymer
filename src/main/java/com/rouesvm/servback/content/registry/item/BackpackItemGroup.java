package com.rouesvm.servback.content.registry.item;

import com.rouesvm.servback.ServerBackpacks;
import com.rouesvm.servback.technical.config.Configuration;
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
            DyeColor.GRAY,
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
            for (int i = 1; i <= Configuration.instance().types_of_backpacks.keySet().size(); i++) {
                entries.add(BackpackItemRegistry.getBackpack(color, i));
            }
        }

        entries.add(BackpackItemRegistry.GLOBAL_BACKPACK);
        entries.add(BackpackItemRegistry.ENDER_BACKPACK);
    }

    public static void initialize() {
        PolymerItemGroupUtils.registerPolymerItemGroup(Identifier.of(ServerBackpacks.MOD_ID + "items"), PolymerItemGroupUtils.builder()
                .icon(() -> new ItemStack(BackpackItemRegistry.ENDER_BACKPACK))
                .displayName(Text.translatable("item.serverbackpacks.gui_backpacks"))
                .entries(((context, entries) -> addItems(entries))).build()
        );
    }
}
