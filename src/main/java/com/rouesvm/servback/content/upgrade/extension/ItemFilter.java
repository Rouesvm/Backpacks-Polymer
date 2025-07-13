package com.rouesvm.servback.content.upgrade.extension;

import com.mojang.serialization.Codec;
import com.rouesvm.servback.content.upgrade.SaveableUpgrade;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Identifier;

import java.util.List;

public class ItemFilter implements SaveableUpgrade {
    private MODE mode;
    private final List<String> filterList;

    public ItemFilter(MODE mode, List<String> filters) {
        this.mode = mode;
        this.filterList = filters;
    }

    public boolean matches(ItemStack stack) {
        return filterList.stream().anyMatch(filterID ->
                matchesFilter(filterID, stack.getItem()));
    }

    public static boolean matchesFilter(String filterID, Item item) {
        var itemRegistry = Registries.ITEM;
        var itemEntry = itemRegistry.getEntry(item);

        if (filterID.startsWith("#")) {
            Identifier tagId = Identifier.tryParse(filterID.substring(1));
            if (tagId == null) return false;

            TagKey<Item> tag = TagKey.of(itemRegistry.getKey(), tagId);
            return itemEntry.isIn(tag);
        }

        String itemId = itemEntry.getIdAsString();
        return itemId.equals(filterID);
    }

    @Override
    public void readView(ReadView data) {
        this.mode = MODE.values()[data.getInt("mode", 0)];

        ReadView.TypedListReadView<String> listReadView = data.getTypedListView("Items", Codec.STRING);
        listReadView.forEach(filterList::add);
    }

    @Override
    public void writeView(WriteView data) {
        if (this.mode != null) data.putInt("mode", mode.ordinal());

        WriteView.ListAppender<String> listAppender = data.getListAppender("Items", Codec.STRING);
        filterList.stream()
                .filter((string) -> !string.isEmpty())
                .forEach(listAppender::add);

        if (listAppender.isEmpty()) data.remove("Items");
    }

    public List<String> filterList() {
        return filterList;
    }

    public MODE getMode() {
        return mode;
    }

    public void setMode(MODE mode) {
        this.mode = mode;
    }

    public enum MODE {
        BLACKLIST,
        WHITELIST,
        PICKUP
    }
}
