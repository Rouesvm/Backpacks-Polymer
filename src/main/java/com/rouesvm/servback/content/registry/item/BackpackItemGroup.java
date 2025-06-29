package com.rouesvm.servback.content.registry.item;

import com.rouesvm.servback.ServerBackpacks;
import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import net.minecraft.item.ItemGroup.Entries;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

public class BackpackItemGroup {
    public static void addItems(Entries entries) {
        var entrySet = BackpackItemJsonRegistry.BACKPACKS.entrySet();

        for (int i = 0; i < DyeColor.values().length + 1; i++) {
            for (var entry : entrySet) {
                var item = entry.getValue().get(i);
                if (item != null) entries.add(item);
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
