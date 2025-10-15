package com.rouesvm.servback.registry.item;

import com.rouesvm.servback.ServerBackpacks;
import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup.Entries;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

public class BackpackItemGroup {
    public static void addItems(Entries entries) {
        var entrySet = BackpackItemJsonRegistry.NAME_TO_BACKPACK;

        String[] baseTypes = {"small", "medium", "large"};
        for (String type : baseTypes) {
            Item item = entrySet.get(type);
            if (item != null) entries.add(item);
        }

        for (DyeColor color : DyeColor.values()) {
            for (String type : baseTypes) {
                Item item = entrySet.get(color.asString().toLowerCase() + "_" + type);
                if (item != null) entries.add(item);
            }
        }

        entries.add(BackpackItemRegistry.GLOBAL_BACKPACK);
        entries.add(BackpackItemRegistry.ENDER_BACKPACK);

        entries.add(BackpackItemRegistry.VOID_UPGRADE);
        entries.add(BackpackItemRegistry.MAGNET_UPGRADE);
        entries.add(BackpackItemRegistry.CRAFTING_UPGRADE);
    }

    public static void initialize() {
        PolymerItemGroupUtils.registerPolymerItemGroup(Identifier.of(ServerBackpacks.MOD_ID + "items"), PolymerItemGroupUtils.builder()
                .icon(() -> new ItemStack(BackpackItemRegistry.GLOBAL_BACKPACK))
                .displayName(Text.translatable("item.serverbackpacks.gui_backpacks"))
                .entries(((context, entries) -> addItems(entries))).build()
        );
    }
}
