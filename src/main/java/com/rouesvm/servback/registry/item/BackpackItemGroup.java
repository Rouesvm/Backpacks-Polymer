package com.rouesvm.servback.registry.item;

import com.rouesvm.servback.ServerBackpacks;
import eu.pb4.polymer.core.api.item.PolymerCreativeModeTabUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab.Output;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class BackpackItemGroup {
    public static void addItems(Output entries) {
        var entrySet = BackpackItemJsonRegistry.NAME_TO_BACKPACK;

        String[] baseTypes = {"small", "medium", "large"};
        for (String type : baseTypes) {
            Item item = entrySet.get(type);
            if (item != null) entries.accept(item);
        }

        for (DyeColor color : DyeColor.values()) {
            for (String type : baseTypes) {
                Item item = entrySet.get(color.getSerializedName().toLowerCase() + "_" + type);
                if (item != null) entries.accept(item);
            }
        }

        entries.accept(BackpackItemRegistry.GLOBAL_BACKPACK);
        entries.accept(BackpackItemRegistry.ENDER_BACKPACK);
        entries.accept(BackpackItemRegistry.LAVA_BACKPACK);

        entries.accept(BackpackItemRegistry.VOID_UPGRADE);
        entries.accept(BackpackItemRegistry.MAGNET_UPGRADE);
        entries.accept(BackpackItemRegistry.JUKEBOX_UPGRADE);

        entries.accept(BackpackItemRegistry.CRAFTING_UPGRADE);
        entries.accept(BackpackItemRegistry.STONECUTTER_UPGRADE);
    }

    public static void initialize() {
        PolymerCreativeModeTabUtils.registerPolymerCreativeModeTab(Identifier.parse(ServerBackpacks.MOD_ID + "items"), PolymerCreativeModeTabUtils.builder()
                .icon(() -> new ItemStack(BackpackItemRegistry.GLOBAL_BACKPACK))
                .title(Component.translatable("item.serverbackpacks.gui_backpacks"))
                .displayItems(((context, entries) -> addItems(entries))).build()
        );
    }
}
