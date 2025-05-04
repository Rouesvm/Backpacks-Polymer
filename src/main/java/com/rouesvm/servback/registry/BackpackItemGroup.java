package com.rouesvm.servback.registry;

import com.rouesvm.servback.Main;
import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import net.minecraft.item.ItemGroup.Entries;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class BackpackItemGroup {
    public static void addItems(Entries entries) {
        BackpackItemRegistry.SMALL.values().forEach(entries::add);
        BackpackItemRegistry.MEDIUM.values().forEach(entries::add);
        BackpackItemRegistry.LARGE.values().forEach(entries::add);

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
