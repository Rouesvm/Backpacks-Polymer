package com.rouesvm.servback.registry;

import com.rouesvm.servback.Main;
import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import net.minecraft.item.ItemGroup.Entries;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModItemGroup {
    public static void addItems(Entries entries) {
        entries.add(ItemRegistry.SMALL_BACKPACK);
        entries.add(ItemRegistry.WHITE_SMALL_BACKPACK);
        entries.add(ItemRegistry.LIGHT_GRAY_SMALL_BACKPACK);
        entries.add(ItemRegistry.BLACK_SMALL_BACKPACK);
        entries.add(ItemRegistry.RED_SMALL_BACKPACK);
        entries.add(ItemRegistry.ORANGE_SMALL_BACKPACK);
        entries.add(ItemRegistry.YELLOW_SMALL_BACKPACK);
        entries.add(ItemRegistry.LIME_SMALL_BACKPACK);
        entries.add(ItemRegistry.GREEN_SMALL_BACKPACK);
        entries.add(ItemRegistry.CYAN_SMALL_BACKPACK);
        entries.add(ItemRegistry.LIGHT_BLUE_SMALL_BACKPACK);
        entries.add(ItemRegistry.BLUE_SMALL_BACKPACK);
        entries.add(ItemRegistry.PURPLE_SMALL_BACKPACK);
        entries.add(ItemRegistry.MAGENTA_SMALL_BACKPACK);
        entries.add(ItemRegistry.PINK_SMALL_BACKPACK);

        entries.add(ItemRegistry.MEDIUM_BACKPACK);
        entries.add(ItemRegistry.WHITE_MEDIUM_BACKPACK);
        entries.add(ItemRegistry.LIGHT_GRAY_MEDIUM_BACKPACK);
        entries.add(ItemRegistry.BLACK_MEDIUM_BACKPACK);
        entries.add(ItemRegistry.RED_MEDIUM_BACKPACK);
        entries.add(ItemRegistry.ORANGE_MEDIUM_BACKPACK);
        entries.add(ItemRegistry.YELLOW_MEDIUM_BACKPACK);
        entries.add(ItemRegistry.LIME_MEDIUM_BACKPACK);
        entries.add(ItemRegistry.GREEN_MEDIUM_BACKPACK);
        entries.add(ItemRegistry.CYAN_MEDIUM_BACKPACK);
        entries.add(ItemRegistry.LIGHT_BLUE_MEDIUM_BACKPACK);
        entries.add(ItemRegistry.BLUE_MEDIUM_BACKPACK);
        entries.add(ItemRegistry.PURPLE_MEDIUM_BACKPACK);
        entries.add(ItemRegistry.MAGENTA_MEDIUM_BACKPACK);
        entries.add(ItemRegistry.PINK_MEDIUM_BACKPACK);

        entries.add(ItemRegistry.LARGE_BACKPACK);
        entries.add(ItemRegistry.WHITE_LARGE_BACKPACK);
        entries.add(ItemRegistry.LIGHT_GRAY_LARGE_BACKPACK);
        entries.add(ItemRegistry.BLACK_LARGE_BACKPACK);
        entries.add(ItemRegistry.RED_LARGE_BACKPACK);
        entries.add(ItemRegistry.ORANGE_LARGE_BACKPACK);
        entries.add(ItemRegistry.YELLOW_LARGE_BACKPACK);
        entries.add(ItemRegistry.LIME_LARGE_BACKPACK);
        entries.add(ItemRegistry.GREEN_LARGE_BACKPACK);
        entries.add(ItemRegistry.CYAN_LARGE_BACKPACK);
        entries.add(ItemRegistry.LIGHT_BLUE_LARGE_BACKPACK);
        entries.add(ItemRegistry.BLUE_LARGE_BACKPACK);
        entries.add(ItemRegistry.PURPLE_LARGE_BACKPACK);
        entries.add(ItemRegistry.MAGENTA_LARGE_BACKPACK);
        entries.add(ItemRegistry.PINK_LARGE_BACKPACK);

        entries.add(ItemRegistry.GLOBAL_BACKPACK);
        entries.add(ItemRegistry.ENDER_BACKPACK);
    }

    public static void initialize() {
        PolymerItemGroupUtils.registerPolymerItemGroup(Identifier.of(Main.MOD_ID + "items"), PolymerItemGroupUtils.builder()
                .icon(() -> new ItemStack(ItemRegistry.ENDER_BACKPACK))
                .displayName(Text.translatable("item.serverbackpacks.gui_backpacks"))
                .entries(((context, entries) -> addItems(entries))).build()
        );
    }
}
