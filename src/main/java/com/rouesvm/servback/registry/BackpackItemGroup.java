package com.rouesvm.servback.registry;

import com.rouesvm.servback.Main;
import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import net.minecraft.item.ItemGroup.Entries;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class BackpackItemGroup {
    public static void addItems(Entries entries) {
        entries.add(BackpackItemRegistry.SMALL_BACKPACK);
        entries.add(BackpackItemRegistry.WHITE_SMALL_BACKPACK);
        entries.add(BackpackItemRegistry.LIGHT_GRAY_SMALL_BACKPACK);
        entries.add(BackpackItemRegistry.BLACK_SMALL_BACKPACK);
        entries.add(BackpackItemRegistry.RED_SMALL_BACKPACK);
        entries.add(BackpackItemRegistry.ORANGE_SMALL_BACKPACK);
        entries.add(BackpackItemRegistry.YELLOW_SMALL_BACKPACK);
        entries.add(BackpackItemRegistry.LIME_SMALL_BACKPACK);
        entries.add(BackpackItemRegistry.GREEN_SMALL_BACKPACK);
        entries.add(BackpackItemRegistry.CYAN_SMALL_BACKPACK);
        entries.add(BackpackItemRegistry.LIGHT_BLUE_SMALL_BACKPACK);
        entries.add(BackpackItemRegistry.BLUE_SMALL_BACKPACK);
        entries.add(BackpackItemRegistry.PURPLE_SMALL_BACKPACK);
        entries.add(BackpackItemRegistry.MAGENTA_SMALL_BACKPACK);
        entries.add(BackpackItemRegistry.PINK_SMALL_BACKPACK);

        entries.add(BackpackItemRegistry.MEDIUM_BACKPACK);
        entries.add(BackpackItemRegistry.WHITE_MEDIUM_BACKPACK);
        entries.add(BackpackItemRegistry.LIGHT_GRAY_MEDIUM_BACKPACK);
        entries.add(BackpackItemRegistry.BLACK_MEDIUM_BACKPACK);
        entries.add(BackpackItemRegistry.RED_MEDIUM_BACKPACK);
        entries.add(BackpackItemRegistry.ORANGE_MEDIUM_BACKPACK);
        entries.add(BackpackItemRegistry.YELLOW_MEDIUM_BACKPACK);
        entries.add(BackpackItemRegistry.LIME_MEDIUM_BACKPACK);
        entries.add(BackpackItemRegistry.GREEN_MEDIUM_BACKPACK);
        entries.add(BackpackItemRegistry.CYAN_MEDIUM_BACKPACK);
        entries.add(BackpackItemRegistry.LIGHT_BLUE_MEDIUM_BACKPACK);
        entries.add(BackpackItemRegistry.BLUE_MEDIUM_BACKPACK);
        entries.add(BackpackItemRegistry.PURPLE_MEDIUM_BACKPACK);
        entries.add(BackpackItemRegistry.MAGENTA_MEDIUM_BACKPACK);
        entries.add(BackpackItemRegistry.PINK_MEDIUM_BACKPACK);

        entries.add(BackpackItemRegistry.LARGE_BACKPACK);
        entries.add(BackpackItemRegistry.WHITE_LARGE_BACKPACK);
        entries.add(BackpackItemRegistry.LIGHT_GRAY_LARGE_BACKPACK);
        entries.add(BackpackItemRegistry.BLACK_LARGE_BACKPACK);
        entries.add(BackpackItemRegistry.RED_LARGE_BACKPACK);
        entries.add(BackpackItemRegistry.ORANGE_LARGE_BACKPACK);
        entries.add(BackpackItemRegistry.YELLOW_LARGE_BACKPACK);
        entries.add(BackpackItemRegistry.LIME_LARGE_BACKPACK);
        entries.add(BackpackItemRegistry.GREEN_LARGE_BACKPACK);
        entries.add(BackpackItemRegistry.CYAN_LARGE_BACKPACK);
        entries.add(BackpackItemRegistry.LIGHT_BLUE_LARGE_BACKPACK);
        entries.add(BackpackItemRegistry.BLUE_LARGE_BACKPACK);
        entries.add(BackpackItemRegistry.PURPLE_LARGE_BACKPACK);
        entries.add(BackpackItemRegistry.MAGENTA_LARGE_BACKPACK);
        entries.add(BackpackItemRegistry.PINK_LARGE_BACKPACK);

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
