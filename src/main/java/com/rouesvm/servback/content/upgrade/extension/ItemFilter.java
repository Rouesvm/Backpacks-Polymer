package com.rouesvm.servback.content.upgrade.extension;

import com.mojang.serialization.Codec;
import com.rouesvm.servback.technical.ui.inventory.BackpackInventory;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.Set;

public class ItemFilter {
    private MODE mode;
    private final Set<String> filterList;

    public ItemFilter(MODE mode, Set<String> filters) {
        this.mode = mode;
        this.filterList = filters;
    }

    public boolean matches(ItemStack stack) {
        return filterList.stream()
                .anyMatch(filterID -> matchesFilter(filterID, stack.getItem()));
    }

    public boolean matches(ItemStack stack, BackpackInventory inventory) {
        return inventory.hasAnyMatching(inventoryStack -> inventoryStack.is(stack.getItem()));
    }

    public static boolean matchesFilter(String filterID, Item item) {
        var itemRegistry = BuiltInRegistries.ITEM;
        var itemEntry = itemRegistry.wrapAsHolder(item);

        if (filterID.startsWith("#")) {
            Identifier tagId = Identifier.tryParse(filterID.substring(1));
            if (tagId == null) return false;

            TagKey<Item> tag = TagKey.create(itemRegistry.key(), tagId);
            return itemEntry.is(tag);
        }

        String itemId = itemEntry.getRegisteredName();
        return itemId.equals(filterID);
    }

    public void readView(ValueInput data) {
        this.mode = MODE.values()[data.getIntOr("mode", 0)];

        ValueInput.TypedInputList<String> listReadView = data.listOrEmpty("Items", Codec.STRING);
        listReadView.forEach(filterList::add);
    }

    public void writeView(ValueOutput data) {
        if (this.mode != null) data.putInt("mode", mode.ordinal());

        ValueOutput.TypedOutputList<String> listAppender = data.list("Items", Codec.STRING);
        filterList.stream()
                .filter((string) -> !string.isEmpty())
                .forEach(listAppender::add);

        if (listAppender.isEmpty()) data.discard("Items");
    }

    public Set<String> filterList() {
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
        MATCH_CONTENT,
        PICKUP
    }
}
